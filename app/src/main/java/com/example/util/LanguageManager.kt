package com.example.util

import android.content.Context
import android.content.SharedPreferences

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val englishName: String,
    val flag: String
) {
    SINHALA("si", "සිංහල", "Sinhala", "🇱🇰"),
    ENGLISH("en", "English", "English", "🇬🇧"),
    TAMIL("ta", "தமிழ்", "Tamil", "🇱🇰");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SINHALA
        }
    }
}

object LanguageManager {
    private const val PREFS_NAME = "app_language_prefs"
    private const val KEY_LANGUAGE = "selected_language_code"

    fun getSavedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, AppLanguage.SINHALA.code) ?: AppLanguage.SINHALA.code
        return AppLanguage.fromCode(code)
    }

    fun saveLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }
}

/**
 * Centralized multilingual dictionary for Sinhala, English, and Tamil.
 */
object AppStrings {

    fun appTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "ශ්‍රී ලංකා පොලිස් තොරතුරු නාමාවලිය"
        AppLanguage.ENGLISH -> "Sri Lanka Police Directory"
        AppLanguage.TAMIL -> "இலங்கை பொலிஸ் தொடர்பு விபரங்கள்"
    }

    fun appSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "හදිසි ඇමතුම් සහ සේවා සම්බන්ධීකරණය"
        AppLanguage.ENGLISH -> "Emergency & Public Safety Directory"
        AppLanguage.TAMIL -> "அவசர மற்றும் பாதுகாப்பு சேவைகள்"
    }

    fun searchPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "ස්ථානය, නිලධාරී, දුරකථන අංක හෝ ඊමේල් සොයන්න..."
        AppLanguage.ENGLISH -> "Search station, officer, phone or email..."
        AppLanguage.TAMIL -> "நிலையம், அதிகாரி, தொலைபேசி அல்லது மின்னஞ்சல் தேடுக..."
    }

    fun recentSearches(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "මෑත සෙවුම් (Recent Searches)"
        AppLanguage.ENGLISH -> "Recent Searches"
        AppLanguage.TAMIL -> "சமீபத்திய தேடல்கள் (Recent Searches)"
    }

    fun clearAll(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "සියල්ල මකන්න"
        AppLanguage.ENGLISH -> "Clear all"
        AppLanguage.TAMIL -> "அனைத்தையும் அழி"
    }

    fun aiSearch(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "✨ AI සෙවුම"
        AppLanguage.ENGLISH -> "✨ AI Search"
        AppLanguage.TAMIL -> "✨ AI தேடல்"
    }

    fun quickEmergency(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "හදිසි ඇමතුම් (Emergency Lines)"
        AppLanguage.ENGLISH -> "Quick Emergency Lines"
        AppLanguage.TAMIL -> "அவசர துரித அழைப்புகள்"
    }

    fun offlineBanner(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "Offline මාදිලිය: පෙර ලබාගත් දත්ත පෙන්වයි"
        AppLanguage.ENGLISH -> "Offline Mode: Showing cached data"
        AppLanguage.TAMIL -> "இணையமற்ற நிலை: சேமிக்கப்பட்ட விபரங்கள்"
    }

    fun call(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "ඇමතුමක් ගන්න"
        AppLanguage.ENGLISH -> "Call"
        AppLanguage.TAMIL -> "அழைக்கவும்"
    }

    fun share(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "බෙදාගන්න"
        AppLanguage.ENGLISH -> "Share"
        AppLanguage.TAMIL -> "பகிரவும்"
    }

    fun copy(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "පිටපත් කරන්න"
        AppLanguage.ENGLISH -> "Copy"
        AppLanguage.TAMIL -> "நகலெடு"
    }

    fun copiedToast(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "අංකය පිටපත් කරගන්නා ලදී"
        AppLanguage.ENGLISH -> "Number copied to clipboard"
        AppLanguage.TAMIL -> "எண் நகலெடுக்கப்பட்டது"
    }

    fun noResults(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "කිසිදු සම්බන්ධතාවයක් හමු නොවීය"
        AppLanguage.ENGLISH -> "No contacts found matching your search"
        AppLanguage.TAMIL -> "தொடர்புகள் எதுவும் கிடைக்கவில்லை"
    }

    fun generalPhone(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "ප්‍රධාන දුරකථන අංකය"
        AppLanguage.ENGLISH -> "General Line"
        AppLanguage.TAMIL -> "பொது தொலைபேசி எண்"
    }

    fun oic(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "ස්ථානාධිපති (OIC)"
        AppLanguage.ENGLISH -> "Officer In Charge (OIC)"
        AppLanguage.TAMIL -> "பொறுப்பதிகாரி (OIC)"
    }

    fun oicTraffic(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "ස්ථානාධිපති (රථවාහන)"
        AppLanguage.ENGLISH -> "OIC Traffic"
        AppLanguage.TAMIL -> "போக்குவரத்து பொறுப்பதிகாரி"
    }

    fun oicCrime(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "ස්ථානාධිපති (අපරාධ)"
        AppLanguage.ENGLISH -> "OIC Crime"
        AppLanguage.TAMIL -> "குற்றவியல் பொறுப்பதிகாரி"
    }

    fun email(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "විද්‍යුත් තැපෑල (Email)"
        AppLanguage.ENGLISH -> "Email Address"
        AppLanguage.TAMIL -> "மின்னஞ்சல் முகவரி"
    }

    fun close(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "වසන්න"
        AppLanguage.ENGLISH -> "Close"
        AppLanguage.TAMIL -> "மூடுக"
    }

    fun languageSwitched(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "භාෂාව සිංහල ලෙස මාරු කරන ලදී 🇱🇰"
        AppLanguage.ENGLISH -> "Language switched to English 🇬🇧"
        AppLanguage.TAMIL -> "மொழி தமிழுக்கு மாற்றப்பட்டது 🇱🇰"
    }

    fun selectLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "භාෂාව තෝරන්න (Select Language)"
        AppLanguage.ENGLISH -> "Select Language"
        AppLanguage.TAMIL -> "மொழியைத் தேர்ந்தெடுக்கவும்"
    }

    // Bottom Navigation
    fun navDirectory(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "නාමාවලිය"
        AppLanguage.ENGLISH -> "Directory"
        AppLanguage.TAMIL -> "விபரங்கள்"
    }

    fun navChat(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "සංවාද (Chat)"
        AppLanguage.ENGLISH -> "Community Chat"
        AppLanguage.TAMIL -> "அரட்டை (Chat)"
    }

    fun navProfile(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "මගේ Profile"
        AppLanguage.ENGLISH -> "My Profile"
        AppLanguage.TAMIL -> "எனது சுயவிவரம்"
    }

    // Chat Strings
    fun chatTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "පරිශීලක ප්‍රජා සංවාද මණ්ඩපය"
        AppLanguage.ENGLISH -> "Community Safety Chat"
        AppLanguage.TAMIL -> "சமூக பாதுகாப்பு அரட்டை"
    }

    fun chatSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "ක්ෂණික හදිසි සහ පොලිස් සේවා විමසීම්"
        AppLanguage.ENGLISH -> "Real-time Public Inquiries & Alerts"
        AppLanguage.TAMIL -> "நிகழ்நேர வினவல்கள் & எச்சரிக்கைகள்"
    }

    fun send(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "යවන්න"
        AppLanguage.ENGLISH -> "Send"
        AppLanguage.TAMIL -> "அனுப்புக"
    }

    fun typeMessage(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "ඔබගේ පණිවිඩය මෙහි ටයිප් කරන්න..."
        AppLanguage.ENGLISH -> "Type your message here..."
        AppLanguage.TAMIL -> "உங்கள் செய்தியை தட்டச்சு செய்க..."
    }

    fun saveProfile(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "Profile එක සුරකින්න"
        AppLanguage.ENGLISH -> "Save Profile"
        AppLanguage.TAMIL -> "சுயவிவரத்தை சேமிக்கவும்"
    }

    fun profileSaved(lang: AppLanguage): String = when (lang) {
        AppLanguage.SINHALA -> "Profile තොරතුරු සාර්ථකව සුරකින ලදී!"
        AppLanguage.ENGLISH -> "Profile updated successfully!"
        AppLanguage.TAMIL -> "சுயவிவரம் வெற்றிகரமாக சேமிக்கப்பட்டது!"
    }
}
