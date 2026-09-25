package com.example.data.remote

import com.squareup.moshi.Json

data class GoogleSheetsResponse(
    @Json(name = "range") val range: String? = null,
    @Json(name = "majorDimension") val majorDimension: String? = null,
    @Json(name = "values") val values: List<List<String>>? = null
)

data class SpreadsheetMetadataResponse(
    @Json(name = "spreadsheetId") val spreadsheetId: String? = null,
    @Json(name = "sheets") val sheets: List<SheetItem>? = null
)

data class SheetItem(
    @Json(name = "properties") val properties: SheetProperties? = null
)

data class SheetProperties(
    @Json(name = "sheetId") val sheetId: Long? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "index") val index: Int? = null
)
