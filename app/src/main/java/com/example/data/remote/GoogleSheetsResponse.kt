package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleSheetsResponse(
    @Json(name = "range") val range: String? = null,
    @Json(name = "majorDimension") val majorDimension: String? = null,
    @Json(name = "values") val values: List<List<String>>? = null
)

@JsonClass(generateAdapter = true)
data class SpreadsheetMetadataResponse(
    @Json(name = "spreadsheetId") val spreadsheetId: String? = null,
    @Json(name = "sheets") val sheets: List<SheetItem>? = null
)

@JsonClass(generateAdapter = true)
data class SheetItem(
    @Json(name = "properties") val properties: SheetProperties? = null
)

@JsonClass(generateAdapter = true)
data class SheetProperties(
    @Json(name = "sheetId") val sheetId: Long? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "index") val index: Int? = null
)
