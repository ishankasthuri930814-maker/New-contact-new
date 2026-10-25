package com.example.util

object PhoneUtils {

    /**
     * Normalizes a phone number for accurate deduplication comparison.
     * Converts Sri Lankan prefixes (+94, 0094, 94) to local '0' prefix and strips non-digits.
     * Examples:
     *   "+94 77 370 3353" -> "0773703353"
     *   "077-370-3353"    -> "0773703353"
     *   "(011) 224 2111"  -> "0112242111"
     */
    fun normalize(phone: String?): String {
        if (phone.isNullOrBlank()) return ""
        val digits = phone.replace(Regex("[^0-9]"), "").trim()
        if (digits.isBlank()) return ""

        return when {
            digits.startsWith("0094") && digits.length >= 13 -> "0" + digits.substring(4)
            digits.startsWith("94") && digits.length >= 11 -> "0" + digits.substring(2)
            digits.startsWith("0") -> digits
            digits.length == 9 && !digits.startsWith("0") -> "0$digits"
            else -> digits
        }
    }

    /**
     * Checks if a phone number is valid (not blank, dummy zeros, or placeholder text).
     */
    fun isValidPhone(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return false
        val trimmed = phone.trim().lowercase()
        if (trimmed in listOf("0", "00", "000", "0000", "null", "nil", "none", "n/a", "-", "--", "na")) {
            return false
        }
        val norm = normalize(phone)
        if (norm.length < 3) return false
        if (norm.all { it == '0' }) return false
        return true
    }

    /**
     * Deduplicates phone numbers from raw inputs so that identical numbers
     * are NEVER returned more than once.
     */
    fun getDistinctPhones(vararg rawPhones: String?): List<String> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<String>()

        for (raw in rawPhones) {
            if (raw.isNullOrBlank()) continue
            val trimmed = raw.trim()
            if (!isValidPhone(trimmed)) continue

            val norm = normalize(trimmed)
            if (norm.isNotBlank() && seen.add(norm)) {
                result.add(trimmed)
            }
        }
        return result
    }

    /**
     * Returns true if the fax number is null, invalid, or matches any of the given phone numbers.
     */
    fun isFaxDuplicate(fax: String?, vararg phones: String?): Boolean {
        if (!isValidPhone(fax)) return true
        val normFax = normalize(fax)
        return phones.any { isValidPhone(it) && normalize(it) == normFax }
    }

    /**
     * Checks if a number is a Sri Lankan mobile number capable of receiving WhatsApp.
     */
    fun isWhatsAppSupported(phone: String?): Boolean {
        val norm = normalize(phone)
        return norm.length == 10 && norm.startsWith("07")
    }

    /**
     * Cleans an email address: returns empty string if invalid or placeholder ("0", "00", "null").
     */
    fun cleanEmail(email: String?): String {
        if (email.isNullOrBlank()) return ""
        val trimmed = email.trim()
        if (trimmed in listOf("0", "00", "null", "none", "n/a", "-", "--") || !trimmed.contains("@")) {
            return ""
        }
        return trimmed
    }
}
