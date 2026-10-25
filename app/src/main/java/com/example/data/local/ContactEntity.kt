package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ContactCategory
import com.example.data.model.PoliceContact

@Entity(tableName = "police_contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val stationOrDesignation: String,
    val rank: String = "",
    val officerName: String = "",
    val generalPhone: String = "",
    val mobilePhone: String = "",
    val officePhone2: String = "",
    val officePhone3: String = "",
    val pvtNumber: String = "",
    val fax: String = "",
    val email: String = "",
    val oicTraffic: String = "",
    val oicCrime: String = "",
    val oicVice: String = "",
    val oicCommunityPolicing: String = "",
    val locationCoordinates: String = "",
    val locationAddress: String = "",
    val categoryName: String = "POLICE",
    val isFavorite: Boolean = false,
    val attachedOfficersJson: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

fun ContactEntity.toPoliceContact(): PoliceContact {
    val cat = try {
        ContactCategory.valueOf(categoryName)
    } catch (e: Exception) {
        ContactCategory.POLICE
    }
    val officers = if (attachedOfficersJson.isNotBlank()) {
        try {
            val list = mutableListOf<com.example.data.model.StationOfficer>()
            val array = org.json.JSONArray(attachedOfficersJson)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(
                    com.example.data.model.StationOfficer(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        rank = obj.optString("rank"),
                        designation = obj.optString("designation"),
                        station = obj.optString("station"),
                        primaryPhone = obj.optString("primaryPhone"),
                        secondaryPhone = obj.optString("secondaryPhone"),
                        email = obj.optString("email")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    } else emptyList()

    return PoliceContact(
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
        category = cat,
        isFavorite = isFavorite,
        attachedOfficers = officers
    )
}

fun PoliceContact.toEntity(): ContactEntity {
    val officersJson = if (attachedOfficers.isNotEmpty()) {
        try {
            val array = org.json.JSONArray()
            for (o in attachedOfficers) {
                val obj = org.json.JSONObject()
                obj.put("id", o.id)
                obj.put("name", o.name)
                obj.put("rank", o.rank)
                obj.put("designation", o.designation)
                obj.put("station", o.station)
                obj.put("primaryPhone", o.primaryPhone)
                obj.put("secondaryPhone", o.secondaryPhone)
                obj.put("email", o.email)
                array.put(obj)
            }
            array.toString()
        } catch (e: Exception) {
            ""
        }
    } else ""

    return ContactEntity(
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
        categoryName = category.name,
        isFavorite = isFavorite,
        attachedOfficersJson = officersJson
    )
}
