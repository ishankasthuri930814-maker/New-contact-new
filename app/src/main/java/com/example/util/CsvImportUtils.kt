package com.example.util

import com.example.data.model.ContactCategory
import com.example.data.model.PoliceContact
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

object CsvImportUtils {

    const val SAMPLE_CSV_TEMPLATE = """Station,Category,OfficerName,Rank,GeneralPhone,MobilePhone,Address,Coordinates,Email
Colombo Central Police,POLICE,Mr. Perera,Chief Inspector,0112433333,0718591111,Dam Street Colombo 12,6.9382 79.8542,colombocentral@police.lk
Kandy Police Station,POLICE,Mr. Bandara,HQI,0812222222,0718592222,Kandy City,7.2906 80.6337,kandy@police.lk
National Emergency Hotline,EMERGENCY,Emergency Ops,Hotline,119,119,Police Headquarters Colombo,,police@emergency.lk
Galle Fire Service,FIRE_STATIONS,Fire Command,OIC,0912222333,0771234567,Galle Fort,6.0329 80.2168,fire@galle.mc.gov.lk
"""

    /**
     * Parses CSV text into a list of PoliceContact objects.
     */
    fun parseCsv(csvContent: String): List<PoliceContact> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val parsedContacts = mutableListOf<PoliceContact>()

        // Check if first line is header
        val firstLine = lines.first().lowercase()
        val hasHeader = firstLine.contains("station") || firstLine.contains("category") ||
                firstLine.contains("phone") || firstLine.contains("name")

        val dataLines = if (hasHeader) lines.drop(1) else lines

        for (line in dataLines) {
            val tokens = parseCsvLine(line)
            if (tokens.isEmpty()) continue

            // We need at least station name and a phone or category
            val station = tokens.getOrNull(0)?.trim() ?: ""
            if (station.isBlank()) continue

            val rawCategory = tokens.getOrNull(1)?.trim()?.uppercase() ?: "POLICE"
            val officerName = tokens.getOrNull(2)?.trim() ?: ""
            val rank = tokens.getOrNull(3)?.trim() ?: ""
            val generalPhone = tokens.getOrNull(4)?.trim() ?: ""
            val mobilePhone = tokens.getOrNull(5)?.trim() ?: ""
            val address = tokens.getOrNull(6)?.trim() ?: ""
            val coordinates = tokens.getOrNull(7)?.trim() ?: ""
            val email = tokens.getOrNull(8)?.trim() ?: ""
            val officePhone2 = tokens.getOrNull(9)?.trim() ?: ""
            val pvtNumber = tokens.getOrNull(10)?.trim() ?: ""

            val category = try {
                when {
                    rawCategory.contains("EMERGENCY") || station.contains("119") || station.contains("118") -> ContactCategory.EMERGENCY
                    rawCategory.contains("FIRE") || station.contains("Fire", ignoreCase = true) -> ContactCategory.FIRE_STATIONS
                    rawCategory.contains("HOSPITAL") || station.contains("Hospital", ignoreCase = true) -> ContactCategory.HOSPITALS
                    rawCategory.contains("GOVT") || rawCategory.contains("GOVERNMENT") -> ContactCategory.GOVT_SERVICES
                    rawCategory.contains("TRAVEL") || rawCategory.contains("TRANSPORT") -> ContactCategory.TRAVEL
                    rawCategory.contains("SHORT") || rawCategory.contains("CODE") -> ContactCategory.SHORT_CODES
                    rawCategory.contains("DIVISION") -> ContactCategory.DIVISIONS
                    rawCategory.contains("RANGE") -> ContactCategory.RANGES
                    else -> ContactCategory.POLICE
                }
            } catch (e: Exception) {
                ContactCategory.POLICE
            }

            parsedContacts.add(
                PoliceContact(
                    id = "bulk_${UUID.randomUUID()}",
                    stationOrDesignation = station,
                    category = category,
                    officerName = officerName,
                    rank = rank,
                    generalPhone = generalPhone,
                    mobilePhone = mobilePhone,
                    officePhone2 = officePhone2,
                    pvtNumber = pvtNumber,
                    locationAddress = address,
                    locationCoordinates = coordinates,
                    email = email
                )
            )
        }

        return parsedContacts
    }

    fun parseCsvFromStream(inputStream: InputStream): List<PoliceContact> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val content = reader.use { it.readText() }
        return parseCsv(content)
    }

    /**
     * Splits a CSV line handling standard quotes like "Dam Street, Colombo 12".
     */
    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }
}
