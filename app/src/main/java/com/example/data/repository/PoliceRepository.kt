package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.ContactCategory
import com.example.data.model.PoliceContact
import com.example.data.remote.GoogleSheetsResponse
import com.example.data.remote.PoliceApiService
import com.example.util.AppUpdateManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class PoliceRepository(private val context: Context) {

    val networkMonitor = com.example.util.NetworkMonitor(context)

    private val prefs: SharedPreferences =
        context.getSharedPreferences("police_directory_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val apiService: PoliceApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://sheets.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PoliceApiService::class.java)
    }

    suspend fun getPoliceContacts(forceRefresh: Boolean = false): Result<List<PoliceContact>> =
        withContext(Dispatchers.IO) {
            val favorites = getFavoritesSet()

            // 1. If device is currently offline and forceRefresh is not requested, immediately return offline cache
            if (!networkMonitor.isOnline && !forceRefresh) {
                val fastCached = getCachedContactsFast()
                return@withContext Result.success(fastCached)
            }

            // 2. Try fetching live data from Google Sheets when network is available
            try {
                val remoteContacts = fetchAndCacheRemote(favorites)
                if (remoteContacts.isNotEmpty()) {
                    return@withContext Result.success(remoteContacts)
                }
            } catch (e: Exception) {
                Log.e("PoliceRepo", "Remote fetch failed, falling back to local offline cache", e)
            }

            // 3. Fallback to local disk/persistent cache if remote fetch failed
            val cached = getCachedContactsFast()
            Result.success(cached)
        }

    private suspend fun fetchAndCacheRemote(favorites: Set<String>): List<PoliceContact> {
        val allParsed = mutableListOf<PoliceContact>()

        // 0. Fetch contacts from GitHub Raw JSON (Live GitHub Sync without APK updates)
        try {
            val githubContacts = fetchGitHubRawContacts()
            if (githubContacts.isNotEmpty()) {
                allParsed.addAll(githubContacts)
                Log.d("PoliceRepo", "Loaded ${githubContacts.size} contacts from GitHub Raw JSON")
            }
        } catch (e: Exception) {
            Log.e("PoliceRepo", "GitHub Raw contacts fetch failed", e)
        }

        // 1. Fetch all sheets/tabs dynamically from User's spreadsheet (1VTch65JpwfuZkUdnrrLpv7AA0YV-ekSLSY-fXToPpHs)
        try {
            val userContacts = fetchAllSheetsFromSpreadsheet(PoliceApiService.USER_SPREADSHEET_ID)
            allParsed.addAll(userContacts)
            Log.d("PoliceRepo", "Loaded ${userContacts.size} contacts from user spreadsheet (all tabs)")
        } catch (e: Exception) {
            Log.e("PoliceRepo", "User spreadsheet multi-sheet fetch failed", e)
        }

        // 2. Fetch all sheets/tabs dynamically from Primary spreadsheet (1tMu-Wpwht7dH0NF4YSfiWjlttS_WOsgrbfJ3_zsJYd0)
        try {
            val primaryContacts = fetchAllSheetsFromSpreadsheet(PoliceApiService.PRIMARY_SPREADSHEET_ID)
            allParsed.addAll(primaryContacts)
            Log.d("PoliceRepo", "Loaded ${primaryContacts.size} contacts from primary spreadsheet")
        } catch (e: Exception) {
            Log.e("PoliceRepo", "Primary spreadsheet multi-sheet fetch failed", e)
        }

        // Fallback if all failed
        if (allParsed.isEmpty()) {
            try {
                val respFb = apiService.getSheetValues(PoliceApiService.FALLBACK_SHEET_URL)
                respFb.body()?.values?.let { rowsFb ->
                    allParsed.addAll(parseSheetRows(rowsFb))
                }
            } catch (e: Exception) {
                Log.e("PoliceRepo", "Fallback fetch failed", e)
            }
        }

        // Merge contacts from all sheets and emergency defaults
        val mergedMap = mutableMapOf<String, PoliceContact>()

        for (raw in (getDefaultEmergencyContacts() + allParsed)) {
            val c = PoliceGpsDirectory.enrichContact(raw)
            val key = c.stationOrDesignation.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
            if (key.isBlank()) continue

            val existing = mergedMap[key]
            if (existing == null) {
                mergedMap[key] = c
            } else {
                mergedMap[key] = existing.copy(
                    rank = existing.rank.ifBlank { c.rank },
                    officerName = existing.officerName.ifBlank { c.officerName },
                    generalPhone = existing.generalPhone.ifBlank { c.generalPhone },
                    mobilePhone = existing.mobilePhone.ifBlank { c.mobilePhone },
                    officePhone2 = existing.officePhone2.ifBlank { c.officePhone2 },
                    officePhone3 = existing.officePhone3.ifBlank { c.officePhone3 },
                    pvtNumber = existing.pvtNumber.ifBlank { c.pvtNumber },
                    fax = existing.fax.ifBlank { c.fax },
                    email = existing.email.ifBlank { c.email },
                    oicTraffic = existing.oicTraffic.ifBlank { c.oicTraffic },
                    oicCrime = existing.oicCrime.ifBlank { c.oicCrime },
                    oicVice = existing.oicVice.ifBlank { c.oicVice },
                    oicCommunityPolicing = existing.oicCommunityPolicing.ifBlank { c.oicCommunityPolicing },
                    locationCoordinates = existing.locationCoordinates.ifBlank { c.locationCoordinates },
                    locationAddress = existing.locationAddress.ifBlank { c.locationAddress }
                )
            }
        }

        // Also ensure every police station from PoliceGpsDirectory (Google My Maps) is present
        for (loc in PoliceGpsDirectory.allLocations) {
            val key = loc.station.lowercase().replace(Regex("[^a-z0-9]"), "")
            if (!mergedMap.containsKey(key)) {
                val existsInAny = mergedMap.keys.any { (it.contains(key) || key.contains(it)) && it.length >= 6 }
                if (!existsInAny) {
                    val id = "gps_station_${loc.station.hashCode()}"
                    mergedMap[key] = PoliceContact(
                        id = id,
                        stationOrDesignation = loc.station,
                        rank = "POLICE STATION",
                        officerName = "${loc.station} Duty Office",
                        generalPhone = "119",
                        locationCoordinates = "${loc.lat},${loc.lng}",
                        locationAddress = loc.address.ifBlank { "${loc.station}, ${loc.division}, Sri Lanka" },
                        category = ContactCategory.DIVISIONS
                    )
                }
            }
        }

        val allContacts = mergedMap.values.map { PoliceGpsDirectory.enrichContact(it) }

        // Save merged data to local disk cache
        saveToLocalCache(allContacts)

        return allContacts.map { it.copy(isFavorite = favorites.contains(it.id)) }
    }

    private suspend fun fetchAllSheetsFromSpreadsheet(spreadsheetId: String): List<PoliceContact> {
        val contacts = mutableListOf<PoliceContact>()
        try {
            // 1. Fetch metadata to discover ALL sheets/tabs dynamically
            val metadataResp = apiService.getSpreadsheetMetadata(PoliceApiService.getMetadataUrl(spreadsheetId))
            val sheetTitles = metadataResp.body()?.sheets?.mapNotNull { it.properties?.title } ?: emptyList()

            if (sheetTitles.isNotEmpty()) {
                Log.d("PoliceRepo", "Discovered ${sheetTitles.size} sheet tabs in $spreadsheetId: $sheetTitles")
                for (title in sheetTitles) {
                    try {
                        val sheetUrl = PoliceApiService.getSheetRangeUrl(spreadsheetId, title)
                        val resp = apiService.getSheetValues(sheetUrl)
                        resp.body()?.values?.let { rows ->
                            val parsed = parseSheetRows(rows)
                            Log.d("PoliceRepo", "Parsed ${parsed.size} contacts from sheet tab '$title'")
                            contacts.addAll(parsed)
                        }
                    } catch (e: Exception) {
                        Log.e("PoliceRepo", "Error fetching tab '$title' from $spreadsheetId", e)
                    }
                }
            } else {
                // Fallback to Sheet1 if metadata sheets list was empty
                val resp = apiService.getSheetValues(PoliceApiService.getSheetRangeUrl(spreadsheetId, "Sheet1"))
                resp.body()?.values?.let { rows ->
                    contacts.addAll(parseSheetRows(rows))
                }
            }
        } catch (e: Exception) {
            Log.e("PoliceRepo", "Failed to fetch metadata for spreadsheet $spreadsheetId, trying direct Sheet1..5 fallback", e)
            // Direct tab fallbacks: Sheet1, Sheet2, Sheet3, Sheet4, Sheet5, Sheet6
            for (tab in listOf("Sheet1", "Sheet2", "Sheet3", "Sheet4", "Sheet5", "Sheet6")) {
                try {
                    val resp = apiService.getSheetValues(PoliceApiService.getSheetRangeUrl(spreadsheetId, tab))
                    resp.body()?.values?.let { rows ->
                        if (rows.isNotEmpty()) {
                            contacts.addAll(parseSheetRows(rows))
                        }
                    }
                } catch (ignored: Exception) {}
            }
        }
        return contacts
    }

    private suspend fun fetchGitHubRawContacts(): List<PoliceContact> = withContext(Dispatchers.IO) {
        val repo = AppUpdateManager.getGitHubRepo(context)
        val branches = listOf("main", "master")
        val fileNames = listOf("contacts.json", "contact.json", "police_contacts.json", "data.json")

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        for (branch in branches) {
            for (fileName in fileNames) {
                val url = "https://raw.githubusercontent.com/$repo/$branch/$fileName"
                try {
                    val request = Request.Builder()
                        .url(url)
                        .header("Cache-Control", "no-cache")
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            if (!body.isNullOrBlank()) {
                                val parsed = parseContactsJson(body)
                                if (parsed.isNotEmpty()) {
                                    Log.d("PoliceRepo", "Successfully fetched ${parsed.size} contacts from $url")
                                    return@withContext parsed
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("PoliceRepo", "GitHub contacts fetch attempt failed for $url: ${e.message}")
                }
            }
        }
        emptyList()
    }

    private fun parseContactsJson(jsonString: String): List<PoliceContact> {
        val contacts = mutableListOf<PoliceContact>()
        try {
            val trimmed = jsonString.trim()
            val jsonArray = when {
                trimmed.startsWith("[") -> JSONArray(trimmed)
                trimmed.startsWith("{") -> {
                    val obj = JSONObject(trimmed)
                    when {
                        obj.has("contacts") -> obj.optJSONArray("contacts")
                        obj.has("data") -> obj.optJSONArray("data")
                        obj.has("police") -> obj.optJSONArray("police")
                        obj.has("items") -> obj.optJSONArray("items")
                        else -> null
                    }
                }
                else -> null
            }

            if (jsonArray != null) {
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.optJSONObject(i) ?: continue

                    fun getField(vararg keys: String): String {
                        for (k in keys) {
                            if (item.has(k)) {
                                val v = item.optString(k, "").trim()
                                if (v.isNotBlank()) return v
                            }
                        }
                        return ""
                    }

                    val stationOrDesignation = getField("stationOrDesignation", "station", "name", "title", "designation", "station_name")
                    if (stationOrDesignation.isBlank()) continue

                    val rank = getField("rank", "designation_rank", "post", "position")
                    val officerName = getField("officerName", "officer", "contact_person", "officer_name", "person")
                    val generalPhone = getField("generalPhone", "phone", "general", "telephone", "office_no", "general_phone", "tel", "officePhone")
                    val mobilePhone = getField("mobilePhone", "mobile", "mobile_no", "cell")
                    val officePhone2 = getField("officePhone2", "office_no2", "phone2")
                    val officePhone3 = getField("officePhone3", "office_no3", "phone3")
                    val pvtNumber = getField("pvtNumber", "pvt", "private_no", "private_number")
                    val fax = getField("fax", "fax_no")
                    val email = getField("email", "mail")
                    val oicTraffic = getField("oicTraffic", "traffic", "traffic_oic")
                    val oicCrime = getField("oicCrime", "crime", "crime_oic")
                    val oicVice = getField("oicVice", "vice", "vice_oic")
                    val oicCommunityPolicing = getField("oicCommunityPolicing", "community", "community_policing")
                    val locationCoordinates = getField("locationCoordinates", "coords", "coordinates", "let-lang", "lat_lng", "latlng", "location_coordinates")
                    val locationAddress = getField("locationAddress", "address", "location", "location_address")
                    val categoryStr = getField("category", "type", "group").uppercase()

                    val category = when {
                        categoryStr.contains("EMERGENCY") || categoryStr.contains("හදිසි") -> ContactCategory.EMERGENCY
                        categoryStr.contains("FIRE") || categoryStr.contains("ගිනි") -> ContactCategory.FIRE_STATIONS
                        categoryStr.contains("HOSPITAL") || categoryStr.contains("රෝහල්") -> ContactCategory.HOSPITALS
                        categoryStr.contains("GOVT") || categoryStr.contains("GOVERNMENT") || categoryStr.contains("රජයේ") -> ContactCategory.GOVT_SERVICES
                        categoryStr.contains("TRAVEL") || categoryStr.contains("TRANSPORT") || categoryStr.contains("ගමන්") -> ContactCategory.TRAVEL
                        categoryStr.contains("SHORT") || categoryStr.contains("CODE") || categoryStr.contains("කෙටි") -> ContactCategory.SHORT_CODES
                        categoryStr.contains("DIVISION") || rank.contains("SSP", ignoreCase = true) || rank.contains("SP", ignoreCase = true) || stationOrDesignation.contains("Division", ignoreCase = true) -> ContactCategory.DIVISIONS
                        categoryStr.contains("RANGE") || rank.contains("DIG", ignoreCase = true) || stationOrDesignation.contains("DIG", ignoreCase = true) -> ContactCategory.RANGES
                        categoryStr.contains("SENIOR") || stationOrDesignation.contains("Senior", ignoreCase = true) || rank.contains("IGP", ignoreCase = true) -> ContactCategory.SENIOR_OFFICERS
                        else -> ContactCategory.POLICE
                    }

                    val id = getField("id").ifBlank { "gh_${i}_${stationOrDesignation.hashCode()}" }

                    val parsed = PoliceContact(
                        id = id,
                        stationOrDesignation = stationOrDesignation,
                        rank = rank,
                        officerName = officerName,
                        generalPhone = generalPhone,
                        mobilePhone = mobilePhone,
                        officePhone2 = officePhone2,
                        officePhone3 = officePhone3,
                        pvtNumber = pvtNumber,
                        fax = fax,
                        email = email,
                        oicTraffic = oicTraffic,
                        oicCrime = oicCrime,
                        oicVice = oicVice,
                        oicCommunityPolicing = oicCommunityPolicing,
                        locationCoordinates = locationCoordinates,
                        locationAddress = locationAddress,
                        category = category
                    )
                    contacts.add(PoliceGpsDirectory.enrichContact(parsed))
                }
            }
        } catch (e: Exception) {
            Log.e("PoliceRepo", "Error parsing contacts.json", e)
        }
        return contacts
    }

    private fun parseSheetRows(rows: List<List<String>>): List<PoliceContact> {
        if (rows.isEmpty()) return emptyList()

        val contacts = mutableListOf<PoliceContact>()
        val firstRow = rows[0].map { it.trim().lowercase().replace("\ufeff", "") }

        val hasHeader = firstRow.any {
            it.contains("designation") || it.contains("station") || it.contains("rank") || it.contains("office")
        }

        val startIndex = if (hasHeader) 1 else 0

        // Helper to find column index by keywords
        fun findColIdx(vararg keywords: String, defaultIdx: Int): Int {
            if (!hasHeader) return defaultIdx
            for (kw in keywords) {
                for ((idx, headerName) in firstRow.withIndex()) {
                    if (headerName.contains(kw)) return idx
                }
            }
            return -1
        }

        val colDesig = findColIdx("designation", "station", defaultIdx = 0)
        val colRank = findColIdx("rank", defaultIdx = 1)
        val colName = findColIdx("name", defaultIdx = 2)
        val colOffice1 = findColIdx("office_no", "general", "office", defaultIdx = 3)
        val colOffice2 = findColIdx("office_no2", defaultIdx = -1)
        val colMobile = findColIdx("mobile", defaultIdx = 4)
        val colOffice3 = findColIdx("office_no3", defaultIdx = -1)
        val colPvtNumber = findColIdx("pvt", "private", defaultIdx = 13)
        val colFax = findColIdx("fax", defaultIdx = -1)
        val colEmail = findColIdx("email", defaultIdx = 5)
        val colTraffic = findColIdx("traffic", defaultIdx = -1)
        val colCrime = findColIdx("crime", defaultIdx = -1)
        val colVice = findColIdx("vice", defaultIdx = -1)
        val colComm = findColIdx("community", "policying", defaultIdx = -1)
        val colCoords = findColIdx("let-lang", "coords", defaultIdx = -1)
        val colAddr = findColIdx("adress", "address", defaultIdx = -1)

        for (i in startIndex until rows.size) {
            val row = rows[i]
            if (row.isEmpty()) continue

            fun getVal(idx: Int): String {
                if (idx < 0 || idx >= row.size) return ""
                return row[idx].trim().replace("\ufeff", "")
            }

            val stationOrDesignation = getVal(if (colDesig >= 0) colDesig else 0)
            if (stationOrDesignation.isBlank()) continue

            val rank = getVal(colRank)
            val officerName = getVal(colName)
            val generalPhone = getVal(colOffice1)
            val officePhone2 = getVal(colOffice2)
            val mobilePhone = getVal(colMobile)
            val officePhone3 = getVal(colOffice3)
            val pvtNumber = getVal(colPvtNumber)
            val fax = getVal(colFax)
            val email = getVal(colEmail)
            val oicTraffic = getVal(colTraffic)
            val oicCrime = getVal(colCrime)
            val oicVice = getVal(colVice)
            val oicComm = getVal(colComm)
            val locCoords = getVal(colCoords)
            val locAddr = getVal(colAddr)

            val category = when {
                rank.contains("DIG", ignoreCase = true) || stationOrDesignation.contains("DIG", ignoreCase = true) -> ContactCategory.RANGES
                rank.contains("SSP", ignoreCase = true) || rank.contains("SP", ignoreCase = true) || stationOrDesignation.contains("Division", ignoreCase = true) -> ContactCategory.DIVISIONS
                stationOrDesignation.contains("Snr", ignoreCase = true) || stationOrDesignation.contains("Senior", ignoreCase = true) || rank.contains("IGP", ignoreCase = true) -> ContactCategory.SENIOR_OFFICERS
                else -> ContactCategory.POLICE
            }

            val id = "contact_${i}_${stationOrDesignation.hashCode()}"

            val parsedContact = PoliceContact(
                id = id,
                stationOrDesignation = stationOrDesignation,
                rank = rank,
                officerName = officerName,
                generalPhone = generalPhone,
                mobilePhone = mobilePhone,
                officePhone2 = officePhone2,
                officePhone3 = officePhone3,
                pvtNumber = pvtNumber,
                fax = fax,
                email = email,
                oicTraffic = oicTraffic,
                oicCrime = oicCrime,
                oicVice = oicVice,
                oicCommunityPolicing = oicComm,
                locationCoordinates = locCoords,
                locationAddress = locAddr,
                category = category
            )
            contacts.add(PoliceGpsDirectory.enrichContact(parsedContact))
        }
        return contacts
    }

    fun toggleFavorite(contactId: String): Boolean {
        val favorites = getFavoritesSet().toMutableSet()
        val isFavNow = if (favorites.contains(contactId)) {
            favorites.remove(contactId)
            false
        } else {
            favorites.add(contactId)
            true
        }
        prefs.edit().putStringSet("favorites", favorites).apply()
        return isFavNow
    }

    private fun getFavoritesSet(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }

    suspend fun saveContact(contact: PoliceContact): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val current = loadFromLocalCache().toMutableList()
            val existingIdx = current.indexOfFirst { it.id == contact.id }
            if (existingIdx >= 0) {
                current[existingIdx] = contact
            } else {
                current.add(0, contact)
            }
            saveToLocalCache(current)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PoliceRepo", "Error saving contact to local cache", e)
            Result.failure(e)
        }
    }

    suspend fun saveContactsBulk(newContacts: List<PoliceContact>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val current = loadFromLocalCache().toMutableList()
            var addedOrUpdated = 0
            for (contact in newContacts) {
                val existingIdx = current.indexOfFirst { 
                    it.id == contact.id || 
                    (it.stationOrDesignation.equals(contact.stationOrDesignation, ignoreCase = true) && 
                     it.generalPhone == contact.generalPhone && contact.generalPhone.isNotBlank())
                }
                if (existingIdx >= 0) {
                    current[existingIdx] = contact
                } else {
                    current.add(0, contact)
                }
                addedOrUpdated++
            }
            saveToLocalCache(current)
            Result.success(addedOrUpdated)
        } catch (e: Exception) {
            Log.e("PoliceRepo", "Error bulk saving contacts to local cache", e)
            Result.failure(e)
        }
    }

    suspend fun deleteContact(contactId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val current = loadFromLocalCache().toMutableList()
            current.removeAll { it.id == contactId }
            saveToLocalCache(current)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PoliceRepo", "Error deleting contact from local cache", e)
            Result.failure(e)
        }
    }

    private fun saveToLocalCache(contacts: List<PoliceContact>) {
        if (contacts.isEmpty()) return
        try {
            val listType = Types.newParameterizedType(List::class.java, PoliceContact::class.java)
            val adapter = moshi.adapter<List<PoliceContact>>(listType)
            val json = adapter.toJson(contacts)

            // 1. Save to persistent internal storage (filesDir) - permanent, survives cache clearance
            val persistentFile = File(context.filesDir, "police_contacts_offline.json")
            persistentFile.writeText(json)

            // 2. Also save to cacheDir for compatibility
            val cacheFile = File(context.cacheDir, "police_contacts_cache.json")
            cacheFile.writeText(json)

            prefs.edit()
                .putLong("last_sync_time", System.currentTimeMillis())
                .putInt("cached_contacts_count", contacts.size)
                .apply()
            Log.d("PoliceRepo", "Successfully saved ${contacts.size} contacts to persistent offline storage")
        } catch (e: Exception) {
            Log.e("PoliceRepo", "Error saving offline cache", e)
        }
    }

    fun loadFromLocalCache(): List<PoliceContact> {
        val listType = Types.newParameterizedType(List::class.java, PoliceContact::class.java)
        val adapter = moshi.adapter<List<PoliceContact>>(listType)

        // 1. Try persistent internal storage (filesDir) first
        try {
            val persistentFile = File(context.filesDir, "police_contacts_offline.json")
            if (persistentFile.exists() && persistentFile.length() > 0) {
                val json = persistentFile.readText()
                val parsed = adapter.fromJson(json)
                if (!parsed.isNullOrEmpty()) {
                    Log.d("PoliceRepo", "Loaded ${parsed.size} contacts from persistent offline filesDir")
                    return parsed
                }
            }
        } catch (e: Exception) {
            Log.e("PoliceRepo", "Error loading from persistent offline storage", e)
        }

        // 2. Fallback to cacheDir
        try {
            val cacheFile = File(context.cacheDir, "police_contacts_cache.json")
            if (cacheFile.exists() && cacheFile.length() > 0) {
                val json = cacheFile.readText()
                val parsed = adapter.fromJson(json)
                if (!parsed.isNullOrEmpty()) {
                    Log.d("PoliceRepo", "Loaded ${parsed.size} contacts from cacheDir")
                    return parsed
                }
            }
        } catch (e: Exception) {
            Log.e("PoliceRepo", "Error loading from cacheDir", e)
        }

        return emptyList()
    }

    fun getCachedContactsFast(): List<PoliceContact> {
        val favorites = getFavoritesSet()
        val cached = loadFromLocalCache()
        return if (cached.isNotEmpty()) {
            cached.map { PoliceGpsDirectory.enrichContact(it).copy(isFavorite = favorites.contains(it.id)) }
        } else {
            getDefaultEmergencyContacts().map {
                PoliceGpsDirectory.enrichContact(it).copy(isFavorite = favorites.contains(it.id))
            }
        }
    }

    fun getLastSyncTimeString(): String {
        val lastSync = prefs.getLong("last_sync_time", 0L)
        if (lastSync == 0L) return "Not synced yet"
        val diff = System.currentTimeMillis() - lastSync
        val minutes = diff / (1000 * 60)
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes mins ago"
            else -> "${minutes / 60} hours ago"
        }
    }

    private fun getDefaultEmergencyContacts(): List<PoliceContact> {
        return listOf(
            // Emergency Services
            PoliceContact(
                id = "em_119",
                stationOrDesignation = "119 Emergency Police Hotline",
                rank = "EMERGENCY HOTLINE",
                officerName = "Police Emergency Service (24/7)",
                generalPhone = "119",
                mobilePhone = "119",
                email = "telligp@police.gov.lk",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_1990",
                stationOrDesignation = "1990 Suwa Seriya Ambulance",
                rank = "AMBULANCE",
                officerName = "Emergency Pre-Hospital Care Service",
                generalPhone = "1990",
                mobilePhone = "1990",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_110",
                stationOrDesignation = "110 Fire & Rescue Brigade",
                rank = "FIRE SERVICE",
                officerName = "Fire & Ambulance Emergency Brigade",
                generalPhone = "110",
                officePhone2 = "011-2422222",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_118",
                stationOrDesignation = "118 National Security Emergency",
                rank = "HOTLINE",
                officerName = "Ministry of Defense / Police",
                generalPhone = "118",
                mobilePhone = "118",
                email = "info@police.gov.lk",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_bomb",
                stationOrDesignation = "Bomb Disposal Squad (Army)",
                rank = "ARMY SQUAD",
                officerName = "Army Emergency Ordnance Squad",
                generalPhone = "011-2434251",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_accident",
                stationOrDesignation = "Accident Service (General Hospital)",
                rank = "NATIONAL HOSPITAL",
                officerName = "National Hospital Emergency Unit",
                generalPhone = "011-2691111",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_stjohn",
                stationOrDesignation = "St. Johns Ambulance Brigade",
                rank = "AMBULANCE",
                officerName = "St. Johns Emergency Service",
                generalPhone = "011-2437744",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_redcross",
                stationOrDesignation = "Sri Lanka Red Cross Society",
                rank = "AMBULANCE",
                officerName = "Red Cross Disaster & Ambulance",
                generalPhone = "011-2691095",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_certis",
                stationOrDesignation = "Certis Lanka Emergency Service",
                rank = "SECURITY",
                officerName = "Certis Emergency Response Unit",
                generalPhone = "011-2585777",
                category = ContactCategory.EMERGENCY
            ),

            // Sri Lanka Fire Stations (ගිනි නිවීම් සේවා)
            PoliceContact(id = "fire_col_hq", stationOrDesignation = "CMC Main Fire Station (HQ)", rank = "FIRE HQ", officerName = "Colombo Main Fire Brigade HQ", generalPhone = "011-2686087", officePhone2 = "011-2422222", officePhone3 = "110", locationAddress = "T.B. Jaya Mawatha, Colombo 10, Colombo District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_col_sub1", stationOrDesignation = "CMC Sub Station 01 - Hettiyawaththa", rank = "FIRE STATION", officerName = "Hettiyawaththa Fire Unit", generalPhone = "011-2430348", locationAddress = "George R. De Silva Mawatha, Colombo 13, Colombo District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_col_sub2", stationOrDesignation = "CMC Sub Station 02 - Grandpass", rank = "FIRE STATION", officerName = "Grandpass Fire Unit", generalPhone = "011-2431353", locationAddress = "Sirimavo Bandaranaike Mawatha, Colombo 14, Colombo District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_col_sub3", stationOrDesignation = "CMC Sub Station 03 - Wellawatta Training Academy", rank = "FIRE ACADEMY", officerName = "Wellawatta Fire Training Academy", generalPhone = "011-2364040", locationAddress = "Roxy Garden, Wellawatta, Colombo 06, Colombo District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_col_sub4", stationOrDesignation = "CMC Sub Station 04 - Old Town Hall", rank = "FIRE STATION", officerName = "Pettah Fire Unit", generalPhone = "011-2395000", locationAddress = "Pettah, Colombo 11/15, Colombo District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_col_sub5", stationOrDesignation = "CMC Sub Station 05 - Parliament", rank = "FIRE STATION", officerName = "Parliament Fire Unit", generalPhone = "011-2778497", locationAddress = "Parliament Member Housing Complex, Sri Jayawardenepura Kotte, Colombo District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_gampaha", stationOrDesignation = "Gampaha Fire Service", rank = "FIRE BRIGADE", officerName = "Gampaha Municipal Fire Brigade", generalPhone = "033-2224444", locationAddress = "Gampaha, Gampaha District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_negombo", stationOrDesignation = "Negombo Fire Station", rank = "FIRE STATION", officerName = "Negombo Municipal Fire Brigade", generalPhone = "031-2224063", locationAddress = "Negombo, Gampaha District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_kalutara", stationOrDesignation = "Kalutara Fire Brigade", rank = "FIRE BRIGADE", officerName = "Kalutara Fire Service Unit", generalPhone = "034-2228080", locationAddress = "Kalutara, Kalutara District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_kandy", stationOrDesignation = "Kandy Fire Brigade", rank = "FIRE BRIGADE", officerName = "Kandy Municipal Fire Brigade", generalPhone = "081-2204844", officePhone2 = "110", locationAddress = "Kandy, Kandy District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_matale", stationOrDesignation = "Fire Brigade Matale", rank = "FIRE BRIGADE", officerName = "Matale Fire Service Station", generalPhone = "066-3122222", locationAddress = "Matale, Matale District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_nuwara_eliya", stationOrDesignation = "Fire and Rescue Unit Nuwara Eliya", rank = "FIRE & RESCUE", officerName = "Nuwara Eliya Fire Service", generalPhone = "052-2222121", locationAddress = "Nuwara Eliya, Nuwara Eliya District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_galle", stationOrDesignation = "Galle Municipal Fire Brigade", rank = "FIRE BRIGADE", officerName = "Galle Municipal Fire Service", generalPhone = "091-2244445", locationAddress = "Galle, Galle District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_matara", stationOrDesignation = "Fire and Rescue Unit Matara", rank = "FIRE & RESCUE", officerName = "Matara Municipal Fire Brigade", generalPhone = "041-2222275", locationAddress = "Matara, Matara District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_tangalle", stationOrDesignation = "Tangalle Fire Brigade", rank = "FIRE BRIGADE", officerName = "Tangalle Fire Service Unit", generalPhone = "047-2241780", locationAddress = "Tangalle, Hambantota District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_jaffna", stationOrDesignation = "Jaffna Municipal Fire Brigade", rank = "FIRE BRIGADE", officerName = "Jaffna Fire Service Unit", generalPhone = "021-2228888", locationAddress = "AB20, Jaffna, Jaffna District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_kilinochchi", stationOrDesignation = "Fire Service Division - Kilinochchi", rank = "FIRE DIVISION", officerName = "Kilinochchi Fire & Rescue Unit", generalPhone = "110", locationAddress = "Kilinochchi, Kilinochchi District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_mannar", stationOrDesignation = "Mannar Fire Service", rank = "FIRE STATION", officerName = "Mannar Fire & Rescue Unit", generalPhone = "110", locationAddress = "Mannar, Mannar District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_vavuniya", stationOrDesignation = "Vavuniya Fire Brigade / Municipal Fire Brigade", rank = "FIRE BRIGADE", officerName = "Vavuniya Fire Service Unit", generalPhone = "110", locationAddress = "Vavuniya, Vavuniya District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_mullaitivu", stationOrDesignation = "SLAF Station Mullaitivu - Fire & Rescue Section", rank = "SLAF FIRE UNIT", officerName = "Mullaitivu Fire & Rescue Section", generalPhone = "110", locationAddress = "SLAF Station, Mullaitivu, Mullaitivu District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_batticaloa", stationOrDesignation = "Batticaloa Fire Service", rank = "FIRE STATION", officerName = "Batticaloa Fire & Rescue Unit", generalPhone = "110", locationAddress = "Batticaloa, Batticaloa District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_ampara", stationOrDesignation = "Fire & Rescue Unit - Ampara", rank = "FIRE & RESCUE", officerName = "Ampara Fire Service Unit", generalPhone = "110", locationAddress = "Ampara, Ampara District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_trincomalee", stationOrDesignation = "Trincomalee Fire and Rescue Unit", rank = "FIRE & RESCUE", officerName = "Trincomalee Fire Service Station", generalPhone = "026-2222100", officePhone2 = "110", locationAddress = "Main Street, Trincomalee 31000, Trincomalee District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_kurunegala", stationOrDesignation = "Municipal Fire Brigade - Kurunegala", rank = "FIRE BRIGADE", officerName = "Kurunegala Municipal Fire Brigade", generalPhone = "037-2222270", locationAddress = "Kurunegala Municipal Council, Kurunegala 60000, Kurunegala District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_chilaw", stationOrDesignation = "Fire Brigade - Chilaw", rank = "FIRE BRIGADE", officerName = "Chilaw Fire Service Station", generalPhone = "032-2220055", locationAddress = "Chilaw-Wariyapola Road, Chilaw, Puttalam District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_palavi", stationOrDesignation = "Palavi Municipal Council Fire Brigade", rank = "FIRE BRIGADE", officerName = "Palavi Fire Service Unit", generalPhone = "110", locationAddress = "Palavi, Puttalam, Puttalam District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_anuradhapura", stationOrDesignation = "Anuradhapura Municipal Fire Brigade", rank = "FIRE BRIGADE", officerName = "Anuradhapura Fire Service Station", generalPhone = "025-2227799", locationAddress = "Anuradhapura, Anuradhapura District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_polonnaruwa", stationOrDesignation = "Polonnaruwa Fire Brigade", rank = "FIRE BRIGADE", officerName = "Polonnaruwa Fire Service Unit", generalPhone = "027-2226668", locationAddress = "Polonnaruwa, Polonnaruwa District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_badulla", stationOrDesignation = "Badulla Municipal Council Fire Brigade", rank = "FIRE BRIGADE", officerName = "Badulla Fire Service Unit", generalPhone = "055-4934529", locationAddress = "Badulla Municipal Council, Bandarawela Rd, Badulla, Badulla District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_monaragala", stationOrDesignation = "Monaragala Fire Brigade & Department", rank = "FIRE BRIGADE", officerName = "Monaragala Fire Service Unit", generalPhone = "110", locationAddress = "Monaragala, Monaragala District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_ratnapura", stationOrDesignation = "Ratnapura Fire Brigade", rank = "FIRE BRIGADE", officerName = "Ratnapura Fire Service Station", generalPhone = "110", locationAddress = "Sripada Mawatha, Ratnapura, Ratnapura District", category = ContactCategory.FIRE_STATIONS),
            PoliceContact(id = "fire_mawanella", stationOrDesignation = "Mawanella Fire and Rescue Unit", rank = "FIRE & RESCUE", officerName = "Mawanella Fire Service Unit", generalPhone = "110", locationAddress = "Mawanella, Kegalle District", category = ContactCategory.FIRE_STATIONS),

            // Short Codes (කෙටි සංකේත)
            PoliceContact(id = "sc_115", stationOrDesignation = "Colombo Municipal Council Operational Unit", rank = "SHORT CODE", generalPhone = "115", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_116", stationOrDesignation = "Sri Lanka Air Force Emergency Service", rank = "SHORT CODE", generalPhone = "116", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1212", stationOrDesignation = "1212 SLT Directory Assistance", rank = "SHORT CODE", officerName = "Directory Information Assistance", generalPhone = "1212", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1344", stationOrDesignation = "Durdans Hospital Hotline", rank = "SHORT CODE", generalPhone = "1344", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1717", stationOrDesignation = "Mobitel Helpline", rank = "SHORT CODE", generalPhone = "1717", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1900", stationOrDesignation = "TRCSL - Telecommunications Regulatory Commission", rank = "SHORT CODE", generalPhone = "1900", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1901", stationOrDesignation = "Power & Energy Ministry Complaints", rank = "SHORT CODE", generalPhone = "1901", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1908", stationOrDesignation = "Presidential Secretariat Office", rank = "SHORT CODE", generalPhone = "1908", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1910", stationOrDesignation = "LECO Power Supply Breakdowns", rank = "SHORT CODE", generalPhone = "1910", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1912", stationOrDesignation = "Tourism Ministry Hotline", rank = "SHORT CODE", generalPhone = "1912", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1917", stationOrDesignation = "Ministry of Megapolis & Western Development", rank = "SHORT CODE", generalPhone = "1917", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1919", stationOrDesignation = "1919 Government Information Centre", rank = "SHORT CODE", officerName = "Public Info Helpline", generalPhone = "1919", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1929", stationOrDesignation = "Child Help Line", rank = "SHORT CODE", officerName = "Min of Child Development & Women Empowerment", generalPhone = "1929", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1939", stationOrDesignation = "National Water Supply & Drainage Board", rank = "SHORT CODE", generalPhone = "1939", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1944", stationOrDesignation = "Inland Revenue Department", rank = "SHORT CODE", generalPhone = "1944", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1948", stationOrDesignation = "National Authority on Tobacco & Alcohol", rank = "SHORT CODE", generalPhone = "1948", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1954", stationOrDesignation = "Bribery Commission (CIABOC)", rank = "SHORT CODE", generalPhone = "1954", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1955", stationOrDesignation = "National Transport Commission (NTC)", rank = "SHORT CODE", generalPhone = "1955", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1969", stationOrDesignation = "Southern Highway Emergency Hotline", rank = "SHORT CODE", generalPhone = "1969", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1973", stationOrDesignation = "SriLankan Airlines Flight Info", rank = "SHORT CODE", generalPhone = "1973", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1984", stationOrDesignation = "National Dangerous Drugs Control Board", rank = "SHORT CODE", generalPhone = "1984", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1987", stationOrDesignation = "Ceylon Electricity Board (CEB Breakdowns)", rank = "SHORT CODE", generalPhone = "1987", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1989", stationOrDesignation = "Sri Lanka Bureau of Foreign Employment (SLBFE)", rank = "SHORT CODE", generalPhone = "1989", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1991", stationOrDesignation = "Ministry of Environment & Natural Resources", rank = "SHORT CODE", generalPhone = "1991", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1996", stationOrDesignation = "Human Rights Commission of Sri Lanka", rank = "SHORT CODE", generalPhone = "1996", category = ContactCategory.SHORT_CODES),

            // Hospitals (රෝහල්)
            PoliceContact(id = "hosp_col", stationOrDesignation = "Colombo General National Hospital", rank = "GOVT HOSPITAL", generalPhone = "011-2691111", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_sjh", stationOrDesignation = "Sri Jayewardenepura General Hospital", rank = "GOVT HOSPITAL", generalPhone = "011-2778610", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_apeksha", stationOrDesignation = "Apeksha Hospital Maharagama (Cancer Hospital)", rank = "GOVT HOSPITAL", generalPhone = "011-2842052", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_castle", stationOrDesignation = "Castle Street Hospital for Women", rank = "GOVT HOSPITAL", generalPhone = "011-2696231", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_welisara", stationOrDesignation = "Chest Hospital Welisara Ragama", rank = "GOVT HOSPITAL", generalPhone = "011-2958271", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_ragama", stationOrDesignation = "Ragama Teaching Hospital", rank = "GOVT HOSPITAL", generalPhone = "011-2959261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_kalubowila", stationOrDesignation = "Kalubowila Colombo South Teaching Hospital", rank = "GOVT HOSPITAL", generalPhone = "011-2763261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_dental", stationOrDesignation = "Dental Institute Colombo", rank = "GOVT HOSPITAL", generalPhone = "011-2677618", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_desoysa", stationOrDesignation = "De Soysa Maternity Hospital for Women", rank = "GOVT HOSPITAL", generalPhone = "011-2696224", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_eye", stationOrDesignation = "National Eye Hospital Colombo", rank = "GOVT HOSPITAL", generalPhone = "011-2693911", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_gampaha", stationOrDesignation = "Gampaha General Hospital", rank = "GOVT HOSPITAL", generalPhone = "033-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_kalutara", stationOrDesignation = "Kalutara General Hospital", rank = "GOVT HOSPITAL", generalPhone = "034-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_lrh", stationOrDesignation = "Lady Ridgeway Hospital for Children (LRH)", rank = "GOVT HOSPITAL", generalPhone = "011-2693711", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_angoda", stationOrDesignation = "National Institute of Mental Health Angoda", rank = "GOVT HOSPITAL", generalPhone = "011-2578234", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_negombo", stationOrDesignation = "Negombo Base Hospital", rank = "GOVT HOSPITAL", generalPhone = "031-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_mulleriyawa", stationOrDesignation = "Mulleriyawa Teaching Hospital", rank = "GOVT HOSPITAL", generalPhone = "011-2578226", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_batticaloa", stationOrDesignation = "Batticaloa Teaching Hospital", rank = "GOVT HOSPITAL", generalPhone = "065-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_peradeniya", stationOrDesignation = "Peradeniya Teaching Hospital", rank = "GOVT HOSPITAL", generalPhone = "081-2388001", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_anuradhapura", stationOrDesignation = "Anuradhapura Teaching Hospital", rank = "GOVT HOSPITAL", generalPhone = "025-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_kandy", stationOrDesignation = "Kandy Teaching Hospital", rank = "GOVT HOSPITAL", generalPhone = "081-2233337", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_kurunegala", stationOrDesignation = "Kurunegala Teaching Hospital", rank = "GOVT HOSPITAL", generalPhone = "037-2233909", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_karapitiya", stationOrDesignation = "Karapitiya Teaching Hospital Galle", rank = "GOVT HOSPITAL", generalPhone = "091-2232267", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_mahamodara", stationOrDesignation = "Mahamodara Teaching Hospital", rank = "GOVT HOSPITAL", generalPhone = "091-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_jaffna", stationOrDesignation = "Jaffna Teaching Hospital", rank = "GOVT HOSPITAL", generalPhone = "021-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_mri", stationOrDesignation = "Medical Research Institute (MRI)", rank = "GOVT INSTITUTE", generalPhone = "011-2693533", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_neville", stationOrDesignation = "Dr. Neville Fernando Teaching Hospital", rank = "TEACHING HOSP", generalPhone = "011-2407600", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_nawaloka", stationOrDesignation = "Nawaloka Hospital", rank = "PVT HOSPITAL", generalPhone = "011-2544444", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_asiri", stationOrDesignation = "Asiri Hospital PLC", rank = "PVT HOSPITAL", generalPhone = "011-4523300", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_asiri_surg", stationOrDesignation = "Asiri Surgical Hospital PLC", rank = "PVT HOSPITAL", generalPhone = "011-4524400", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_central", stationOrDesignation = "Central Hospital (Pvt) Ltd", rank = "PVT HOSPITAL", generalPhone = "011-4665500", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_lanka", stationOrDesignation = "Lanka Hospitals", rank = "PVT HOSPITAL", generalPhone = "011-5530000", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_oasis", stationOrDesignation = "Oasis Hospital", rank = "PVT HOSPITAL", generalPhone = "011-4514770", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_park", stationOrDesignation = "Park Hospital", rank = "PVT HOSPITAL", generalPhone = "011-2590200", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_durdans", stationOrDesignation = "Durdans Hospital", rank = "PVT HOSPITAL", generalPhone = "011-5410000", officePhone2 = "1344", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_hemas", stationOrDesignation = "Hemas Hospital", rank = "PVT HOSPITAL", generalPhone = "011-7888888", category = ContactCategory.HOSPITALS),

            // Govt Services & Depts (රජයේ සේවා)
            PoliceContact(id = "govt_leco", stationOrDesignation = "LECO (Power Breakdowns & Info)", rank = "GOVT DEPT", generalPhone = "011-2371625", officePhone2 = "1910", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_gem", stationOrDesignation = "National Gem & Jewellery Authority", rank = "GOVT DEPT", generalPhone = "011-2325364", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_boi", stationOrDesignation = "Board of Investment of Sri Lanka (BOI)", rank = "GOVT DEPT", generalPhone = "011-2434403", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_library", stationOrDesignation = "National Library & Documentation Services Board", rank = "GOVT DEPT", generalPhone = "011-2698847", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_nlb", stationOrDesignation = "National Lotteries Board", rank = "GOVT DEPT", generalPhone = "011-2470662", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_edb", stationOrDesignation = "Sri Lanka Export Development Board (EDB)", rank = "GOVT DEPT", generalPhone = "011-2300705", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_trcsl", stationOrDesignation = "Telecommunications Regulatory Commission (TRCSL)", rank = "GOVT DEPT", generalPhone = "011-2689345", officePhone2 = "1900", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_cbsl", stationOrDesignation = "Central Bank of Sri Lanka (CBSL)", rank = "GOVT DEPT", generalPhone = "011-2477000", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_hrc", stationOrDesignation = "Human Rights Commission of Sri Lanka", rank = "GOVT DEPT", generalPhone = "011-2505575", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_botanic", stationOrDesignation = "National Botanic Gardens Peradeniya", rank = "GOVT DEPT", generalPhone = "081-2388654", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_museums", stationOrDesignation = "National Museums Department", rank = "GOVT DEPT", generalPhone = "011-2694767", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_zoo", stationOrDesignation = "National Zoological Gardens Dehiwala", rank = "GOVT DEPT", generalPhone = "011-2712752", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_postal", stationOrDesignation = "Postal Department Sri Lanka", rank = "GOVT DEPT", generalPhone = "011-2328301", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_telecom_min", stationOrDesignation = "Ministry of Telecommunication & Digital Infrastructure", rank = "MINISTRY", generalPhone = "011-2577777", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_fin_min", stationOrDesignation = "Ministry of Finance", rank = "MINISTRY", generalPhone = "011-2513459", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_wildlife", stationOrDesignation = "Department of Wildlife Conservation", rank = "GOVT DEPT", generalPhone = "011-2888585", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_army", stationOrDesignation = "Army Headquarters Sri Lanka", rank = "DEFENSE", generalPhone = "011-2432682", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_airforce", stationOrDesignation = "Air Force Headquarters Sri Lanka", rank = "DEFENSE", generalPhone = "011-2441044", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_navy", stationOrDesignation = "Navy Headquarters Sri Lanka", rank = "DEFENSE", generalPhone = "011-2445368", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_nic", stationOrDesignation = "Department for Registration of Persons (NIC)", rank = "GOVT DEPT", generalPhone = "011-2862217", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_immigration", stationOrDesignation = "Department of Immigration & Emigration (Passports)", rank = "GOVT DEPT", generalPhone = "011-5329000", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_customs", stationOrDesignation = "Sri Lanka Customs Department", rank = "GOVT DEPT", generalPhone = "011-2470945", category = ContactCategory.GOVT_SERVICES),

            // Travel & Transport (ගමන් බිමන්)
            PoliceContact(id = "trv_bia", stationOrDesignation = "Bandaranaike International Airport (Katunayake BIA)", rank = "AIRPORT", generalPhone = "011-2264444", officePhone2 = "011-2252861", category = ContactCategory.TRAVEL),
            PoliceContact(id = "trv_mria", stationOrDesignation = "Mattala Rajapaksa International Airport (MRIA)", rank = "AIRPORT", generalPhone = "047-2031000", category = ContactCategory.TRAVEL),
            PoliceContact(id = "trv_flight_info", stationOrDesignation = "Flight Information (All Airlines)", rank = "AIRPORT INFO", generalPhone = "011-2263047", category = ContactCategory.TRAVEL),
            PoliceContact(id = "trv_srilankan", stationOrDesignation = "SriLankan Airlines General Hotline", rank = "AIRLINE", generalPhone = "019-7335555", officePhone2 = "011-7800300", category = ContactCategory.TRAVEL),
            PoliceContact(id = "trv_bus_pettah", stationOrDesignation = "Central Bus Stand Pettah (SLTB)", rank = "BUS STAND", generalPhone = "011-2328081", category = ContactCategory.TRAVEL),
            PoliceContact(id = "trv_pvt_bus", stationOrDesignation = "Private Bus Stand Pettah", rank = "BUS STAND", generalPhone = "011-2333222", category = ContactCategory.TRAVEL),
            PoliceContact(id = "trv_fort_rail", stationOrDesignation = "Fort Railway Station Information", rank = "RAILWAY", generalPhone = "011-2434215", category = ContactCategory.TRAVEL),
            PoliceContact(id = "trv_tourism", stationOrDesignation = "Sri Lanka Tourism Information Centre", rank = "TOURISM", generalPhone = "011-2437059", category = ContactCategory.TRAVEL),
            PoliceContact(id = "trv_caa", stationOrDesignation = "Civil Aviation Authority of Sri Lanka", rank = "AVIATION", generalPhone = "011-2358800", category = ContactCategory.TRAVEL)
        )
    }
}
