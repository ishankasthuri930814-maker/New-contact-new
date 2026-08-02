package com.example.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface PoliceApiService {
    @GET
    suspend fun getSheetValues(@Url url: String): Response<GoogleSheetsResponse>

    companion object {
        const val PRIMARY_SHEET_URL = "https://sheets.googleapis.com/v4/spreadsheets/1tMu-Wpwht7dH0NF4YSfiWjlttS_WOsgrbfJ3_zsJYd0/values/Sheet1!A1:Z1000?key=AIzaSyC6wDD_qFJmgK3tQICKrmgh6IgtWUbI0ps"
        const val SECONDARY_SHEET_URL = "https://sheets.googleapis.com/v4/spreadsheets/1VTch65JpwfuZkUdnrrLpv7AA0YV-ekSLSY-fXToPpHs/values/Sheet1!A1:Z1000?key=AIzaSyC6wDD_qFJmgK3tQICKrmgh6IgtWUbI0ps"
        const val DEFAULT_SHEET_URL = PRIMARY_SHEET_URL
        const val FALLBACK_SHEET_URL = "https://sheets.googleapis.com/v1/spreadsheets/1tMu-Wpwht7dH0NF4YSfiWjlttS_WOsgrbfJ3_zsJYd0/values/Sheet1!A1:Z1000?key=AIzaSyC6wDD_qFJmgK3tQICKrmgh6IgtWUbI0ps"
    }
}
