package com.example

import com.example.data.model.ContactCategory
import com.example.data.model.PoliceContact
import com.example.data.repository.PoliceGpsDirectory
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testPoliceGpsDirectory_findsStationCoordinates() {
    val fortLoc = PoliceGpsDirectory.findLocation("Fort Police Station")
    assertNotNull("Fort police station GPS should be found", fortLoc)
    assertTrue("Latitude should be around 6.9", fortLoc!!.lat > 6.0 && fortLoc.lat < 8.0)
    assertTrue("Longitude should be around 79.8", fortLoc.lng > 79.0 && fortLoc.lng < 82.0)

    val kandyLoc = PoliceGpsDirectory.findLocation("Kandy")
    assertNotNull("Kandy police station GPS should be found", kandyLoc)
    assertTrue("Latitude should be around 7.2", kandyLoc!!.lat > 7.0 && kandyLoc.lat < 8.0)
  }

  @Test
  fun testPoliceGpsDirectory_enrichContact() {
    val rawContact = PoliceContact(
      id = "test_1",
      stationOrDesignation = "OIC Fort",
      generalPhone = "0112433744",
      category = ContactCategory.POLICE
    )
    val enriched = PoliceGpsDirectory.enrichContact(rawContact)
    assertTrue("Location coordinates must be populated", enriched.locationCoordinates.isNotBlank())
    assertTrue("Location address must be populated", enriched.locationAddress.isNotBlank())
  }

  @Test
  fun testContactCategory_policeIsFirstCategory() {
    val categories = ContactCategory.values()
    assertEquals(ContactCategory.POLICE, categories[0])
    assertEquals("Police Contacts", categories[0].displayName)
    assertEquals("පොලිස් ඇමතුම්", categories[0].sinhalaName)
  }

  @Test
  fun testContact_allShareFieldsIncluded() {
    val contact = PoliceContact(
      id = "test_full",
      stationOrDesignation = "Colombo Central Police",
      officerName = "Chief Inspector Silva",
      rank = "CI",
      generalPhone = "0112111111",
      mobilePhone = "0771234567",
      officePhone2 = "0112222222",
      officePhone3 = "0112333333",
      pvtNumber = "0719999999",
      fax = "0112444444",
      email = "colombo@police.lk",
      category = ContactCategory.POLICE,
      oicTraffic = "0711111111",
      oicCrime = "0722222222",
      oicVice = "0733333333",
      oicCommunityPolicing = "0744444444",
      locationAddress = "Colombo 01, Sri Lanka",
      locationCoordinates = "6.9344, 79.8428"
    )

    assertTrue(contact.stationOrDesignation.isNotBlank())
    assertTrue(contact.officePhone2.isNotBlank())
    assertTrue(contact.officePhone3.isNotBlank())
    assertTrue(contact.pvtNumber.isNotBlank())
    assertTrue(contact.fax.isNotBlank())
    assertTrue(contact.oicTraffic.isNotBlank())
    assertTrue(contact.oicCrime.isNotBlank())
    assertTrue(contact.oicVice.isNotBlank())
    assertTrue(contact.oicCommunityPolicing.isNotBlank())
  }
}

