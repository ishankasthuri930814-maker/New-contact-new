package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.provider.ContactsContract
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.data.model.PoliceContact
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrCodeUtils {

    /**
     * Generates a standard vCard 3.0 representation of the police contact.
     * This format is universally recognized by iOS & Android camera and QR scanners
     * to prompt "Add to Contacts" / "Save Contact".
     */
    fun generateVCard(contact: PoliceContact): String {
        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCARD")
        sb.appendLine("VERSION:3.0")
        
        // Full Name (Station / Designation)
        val name = contact.stationOrDesignation.trim()
        sb.appendLine("FN:$name")
        sb.appendLine("N:;$name;;;")

        // Organization
        sb.appendLine("ORG:Sri Lanka Police")

        // Title / Officer Name / Rank
        val titleParts = mutableListOf<String>()
        if (contact.rank.isNotBlank()) titleParts.add(contact.rank)
        if (contact.officerName.isNotBlank()) titleParts.add(contact.officerName)
        if (titleParts.isNotEmpty()) {
            sb.appendLine("TITLE:${titleParts.joinToString(" - ")}")
        }

        // Phone Numbers
        if (contact.generalPhone.isNotBlank()) {
            sb.appendLine("TEL;TYPE=WORK,VOICE:${contact.generalPhone}")
        }
        if (contact.mobilePhone.isNotBlank()) {
            sb.appendLine("TEL;TYPE=CELL,VOICE:${contact.mobilePhone}")
        }
        if (contact.officePhone2.isNotBlank()) {
            sb.appendLine("TEL;TYPE=WORK,VOICE:${contact.officePhone2}")
        }
        if (contact.pvtNumber.isNotBlank()) {
            sb.appendLine("TEL;TYPE=OTHER,VOICE:${contact.pvtNumber}")
        }

        // Email
        if (contact.email.isNotBlank()) {
            sb.appendLine("EMAIL;TYPE=WORK:${contact.email}")
        }

        // Address
        if (contact.locationAddress.isNotBlank()) {
            val cleanAddr = contact.locationAddress.replace("\n", ", ")
            sb.appendLine("ADR;TYPE=WORK:;;$cleanAddr;;;;")
        }

        // Note with category
        sb.appendLine("NOTE:Sri Lanka Police Directory - Category: ${contact.category.name}")
        sb.appendLine("END:VCARD")
        return sb.toString()
    }

    /**
     * Generates an ImageBitmap representing the QR code for the given text.
     */
    fun generateQrCodeBitmap(text: String, size: Int = 512): ImageBitmap? {
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
                put(EncodeHintType.MARGIN, 1) // 1 module margin
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Launches Android's native Contacts Insert screen to directly save contact details
     * into the user's phone address book.
     */
    fun launchSaveToContactsIntent(context: Context, contact: PoliceContact) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                val displayName = if (contact.officerName.isNotBlank() && !contact.stationOrDesignation.contains(contact.officerName)) {
                    "${contact.stationOrDesignation} (${contact.officerName})"
                } else {
                    contact.stationOrDesignation
                }
                putExtra(ContactsContract.Intents.Insert.NAME, displayName)
                putExtra(ContactsContract.Intents.Insert.COMPANY, "Sri Lanka Police")

                if (contact.rank.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.JOB_TITLE, contact.rank)
                }

                if (contact.generalPhone.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.PHONE, contact.generalPhone)
                    putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                } else if (contact.mobilePhone.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.PHONE, contact.mobilePhone)
                    putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                }

                if (contact.mobilePhone.isNotBlank() && contact.mobilePhone != contact.generalPhone) {
                    putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, contact.mobilePhone)
                    putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                }

                if (contact.email.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.EMAIL, contact.email)
                    putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                }

                if (contact.locationAddress.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.POSTAL, contact.locationAddress)
                    putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
                }

                putExtra(ContactsContract.Intents.Insert.NOTES, "Sri Lanka Police Directory - ${contact.category.name}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic contacts insert intent
            val fallback = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
                putExtra(ContactsContract.Intents.Insert.NAME, contact.stationOrDesignation)
                if (contact.generalPhone.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.PHONE, contact.generalPhone)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }
}
