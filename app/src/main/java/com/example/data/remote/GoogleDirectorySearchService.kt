package com.example.data.remote

import android.util.Log
import com.aistudio.policedirectory.zxklm.BuildConfig
import com.example.data.model.ExternalSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class GoogleDirectorySearchService {

    companion object {
        private const val TAG = "GoogleDirectorySearch"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun searchSriLankaDirectory(query: String): Result<List<ExternalSearchResult>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please configure GEMINI_API_KEY in the Secrets panel.")
            )
        }

        val prompt = buildString {
            append("The user is searching for a contact in Sri Lanka: '$query'. ")
            append("Using Google Search, find the official and verified telephone numbers, emergency hotline, OIC mobile/office number, address, and location details in Sri Lanka (Police Station, Hospital, Public Service, Government Office, Ministry, or Business). ")
            append("Provide accurate, real Sri Lanka telephone numbers. ")
            append("Format your answer ONLY as a JSON object with this exact structure (no surrounding markdown or conversation):\n")
            append("{\n")
            append("  \"results\": [\n")
            append("    {\n")
            append("      \"title\": \"Name of station, hospital, or entity in Sinhala/English\",\n")
            append("      \"phone\": \"Primary phone number(s), e.g. 011-2421111\",\n")
            append("      \"secondaryPhone\": \"Mobile or alternative phone if found\",\n")
            append("      \"address\": \"Town, District or street address\",\n")
            append("      \"category\": \"Police / Hospital / Emergency / Government / Business\",\n")
            append("      \"notes\": \"Key details, e.g. OIC contact or 24/7 service\",\n")
            append("      \"sourceUrl\": \"Official website or directory source link\"\n")
            append("    }\n")
            append("  ]\n")
            append("}")
        }

        try {
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val toolsArray = JSONArray().apply {
                    put(JSONObject().put("googleSearch", JSONObject()))
                }
                put("tools", toolsArray)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error code: ${response.code}, body: $responseBody")
                return@withContext Result.failure(Exception("Google Search API Error (${response.code})"))
            }

            val results = parseSearchResults(responseBody, query)
            if (results.isEmpty()) {
                return@withContext Result.failure(Exception("No directory listings found for '$query'"))
            }

            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Search execution error", e)
            Result.failure(e)
        }
    }

    private fun parseSearchResults(rawJson: String, originalQuery: String): List<ExternalSearchResult> {
        val list = mutableListOf<ExternalSearchResult>()
        try {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()
            if (candidates.length() == 0) return emptyList()

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content") ?: return emptyList()
            val parts = content.optJSONArray("parts") ?: return emptyList()
            if (parts.length() == 0) return emptyList()

            val rawText = parts.getJSONObject(0).optString("text", "")
            if (rawText.isBlank()) return emptyList()

            // Extract JSON substring if wrapped in markdown code fence
            val jsonString = cleanJsonMarkdown(rawText)

            try {
                val parsedObj = JSONObject(jsonString)
                val resultsArray = parsedObj.optJSONArray("results")
                if (resultsArray != null && resultsArray.length() > 0) {
                    for (i in 0 until resultsArray.length()) {
                        val item = resultsArray.getJSONObject(i)
                        val title = item.optString("title", "").trim()
                        val phone = item.optString("phone", "").trim()
                        val secPhone = item.optString("secondaryPhone", "").trim()
                        val address = item.optString("address", "").trim()
                        val category = item.optString("category", "Directory").trim()
                        val notes = item.optString("notes", "").trim()
                        val sourceUrl = item.optString("sourceUrl", "").trim()

                        if (title.isNotEmpty() && (phone.isNotEmpty() || secPhone.isNotEmpty())) {
                            list.add(
                                ExternalSearchResult(
                                    title = title,
                                    phone = if (phone.isNotEmpty()) phone else secPhone,
                                    secondaryPhone = if (phone.isNotEmpty() && secPhone.isNotEmpty()) secPhone else null,
                                    address = address,
                                    category = category,
                                    notes = notes,
                                    sourceUrl = if (sourceUrl.startsWith("http")) sourceUrl else null,
                                    sourceBadge = "Google Verified"
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed parsing JSON block, falling back to regex: ${e.message}")
            }

            // Fallback Regex Parser if structured parsing was empty but response has text with phone numbers
            if (list.isEmpty()) {
                val phonePattern = Pattern.compile("(\\+?94\\s?\\d{1,2}\\s?\\d{3}\\s?\\d{4}|0\\d{2,3}[\\s-]?\\d{6,7}|119|1990|1919)")
                val matcher = phonePattern.matcher(rawText)
                val foundNumbers = mutableListOf<String>()
                while (matcher.find()) {
                    val num = matcher.group().trim()
                    if (!foundNumbers.contains(num)) {
                        foundNumbers.add(num)
                    }
                }

                if (foundNumbers.isNotEmpty()) {
                    list.add(
                        ExternalSearchResult(
                            title = originalQuery.trim(),
                            phone = foundNumbers.first(),
                            secondaryPhone = if (foundNumbers.size > 1) foundNumbers[1] else null,
                            address = "Sri Lanka",
                            category = "Directory",
                            notes = rawText.take(200).replace("\n", " "),
                            sourceBadge = "Google Grounded"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in parseSearchResults", e)
        }
        return list
    }

    private fun cleanJsonMarkdown(text: String): String {
        var str = text.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json").trim()
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```").trim()
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```").trim()
        }
        val firstBrace = str.indexOf('{')
        val lastBrace = str.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return str.substring(firstBrace, lastBrace + 1)
        }
        return str
    }
}
