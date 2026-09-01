package com.example.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url
import java.net.URLEncoder

interface PoliceApiService {
    @GET
    suspend fun getSheetValues(@Url url: String): Response<GoogleSheetsResponse>

    @GET
    suspend fun getSpreadsheetMetadata(@Url url: String): Response<SpreadsheetMetadataResponse>

    companion object {
        const val API_KEY = "AIzaSyC6wDD_qFJmgK3tQICKrmgh6IgtWUbI0ps"
        const val USER_SPREADSHEET_ID = "1VTch65JpwfuZkUdnrrLpv7AA0YV-ekSLSY-fXToPpHs"
        const val PRIMARY_SPREADSHEET_ID = "1tMu-Wpwht7dH0NF4YSfiWjlttS_WOsgrbfJ3_zsJYd0"

        fun getMetadataUrl(spreadsheetId: String): String {
            return "https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId?key=$API_KEY"
        }

        fun getSheetRangeUrl(spreadsheetId: String, sheetTitle: String): String {
            val encodedTitle = try {
                URLEncoder.encode(sheetTitle, "UTF-8").replace("+", "%20")
            } catch (e: Exception) {
                sheetTitle
            }
            return "https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId/values/$encodedTitle!A1:Z5000?key=$API_KEY"
        }

        const val PRIMARY_SHEET_URL = "https://sheets.googleapis.com/v4/spreadsheets/1tMu-Wpwht7dH0NF4YSfiWjlttS_WOsgrbfJ3_zsJYd0/values/Sheet1!A1:Z1000?key=AIzaSyC6wDD_qFJmgK3tQICKrmgh6IgtWUbI0ps"
        const val SECONDARY_SHEET_URL = "https://sheets.googleapis.com/v4/spreadsheets/1VTch65JpwfuZkUdnrrLpv7AA0YV-ekSLSY-fXToPpHs/values/Sheet1!A1:Z1000?key=AIzaSyC6wDD_qFJmgK3tQICKrmgh6IgtWUbI0ps"
        const val DEFAULT_SHEET_URL = PRIMARY_SHEET_URL
        const val FALLBACK_SHEET_URL = "https://sheets.googleapis.com/v1/spreadsheets/1tMu-Wpwht7dH0NF4YSfiWjlttS_WOsgrbfJ3_zsJYd0/values/Sheet1!A1:Z1000?key=AIzaSyC6wDD_qFJmgK3tQICKrmgh6IgtWUbI0ps"

        const val AUTH_SPREADSHEET_ID = "16PxPfbpkgj9EVRp59EYgf7hkurn-fwM6xgVHjxlYoXE"
        const val AUTH_SHEET_URL = "https://sheets.googleapis.com/v4/spreadsheets/16PxPfbpkgj9EVRp59EYgf7hkurn-fwM6xgVHjxlYoXE/values/Sheet1!A1:B1000?key=AIzaSyDz3ArrD8jOAtU8bzXONnkE0FnCElObw4Q"
    }
}

