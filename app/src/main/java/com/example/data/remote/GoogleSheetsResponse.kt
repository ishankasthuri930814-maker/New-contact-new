package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleSheetsResponse(
    @Json(name = "range") val range: String? = null,
    @Json(name = "majorDimension") val majorDimension: String? = null,
    @Json(name = "values") val values: List<List<String>>? = null
)
