package com.example.data.model

data class ExternalSearchResult(
    val title: String,
    val phone: String,
    val secondaryPhone: String? = null,
    val address: String = "",
    val category: String = "General",
    val notes: String = "",
    val sourceUrl: String? = null,
    val sourceBadge: String = "Google Verified"
) {
    /**
     * Returns individual dialable phone numbers from raw string (e.g. "0112421111, 0112433333")
     */
    val phoneList: List<String>
        get() {
            val list = mutableListOf<String>()
            val rawSplit = phone.split(",", "/", ";", "\n", "|")
            for (p in rawSplit) {
                val cleaned = p.trim()
                if (cleaned.isNotBlank() && !list.contains(cleaned)) {
                    list.add(cleaned)
                }
            }
            if (!secondaryPhone.isNullOrBlank()) {
                val sec = secondaryPhone.trim()
                if (sec.isNotBlank() && !list.contains(sec)) {
                    list.add(sec)
                }
            }
            return if (list.isNotEmpty()) list else listOf(phone.trim())
        }
}
