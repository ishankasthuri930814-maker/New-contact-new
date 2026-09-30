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
    val lastUpdated: Long = System.currentTimeMillis()
)

fun ContactEntity.toPoliceContact(): PoliceContact {
    val cat = try {
        ContactCategory.valueOf(categoryName)
    } catch (e: Exception) {
        ContactCategory.POLICE
    }
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
        isFavorite = isFavorite
    )
}

fun PoliceContact.toEntity(): ContactEntity {
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
        isFavorite = isFavorite
    )
}
