package com.example.util

import com.example.data.model.PoliceContact
import java.util.concurrent.ConcurrentHashMap

/**
 * Intelligent Multilingual Search Helper for Sri Lankan Police & Emergency Services.
 * Seamlessly resolves Sinhala and Tamil queries against English Google Sheet data
 * using a high-precision dual-layer engine:
 * 1. Comprehensive Sri Lankan Administrative, Geographical & Police terminology dictionary.
 * 2. Full phonetic Sinhala-to-Latin and Tamil-to-Latin transliteration algorithms.
 * 3. Multi-token matching and caching to prevent any UI stutter or crash on real devices.
 */
object MultiLanguageSearchHelper {

    // Range checks for unicode scripts
    fun isSinhala(text: String): Boolean = text.any { it in '\u0D80'..'\u0DFF' }
    fun isTamil(text: String): Boolean = text.any { it in '\u0B80'..'\u0BFF' }
    fun isNonAsciiScript(text: String): Boolean = isSinhala(text) || isTamil(text)

    /**
     * Dictionary of Sri Lankan administrative divisions, districts, major towns,
     * police ranks, operational departments, and emergency keywords.
     * Maps Sinhala & Tamil forms to standard English terms found in Google Sheets.
     */
    private val DICTIONARY: Map<String, List<String>> = mapOf(
        // Districts & Provinces
        "කොළඹ" to listOf("colombo"),
        "කොලඹ" to listOf("colombo"),
        "கொழும்பு" to listOf("colombo"),
        "ගම්පහ" to listOf("gampaha"),
        "கம்பஹா" to listOf("gampaha"),
        "කළුතර" to listOf("kalutara", "kaluthara"),
        "කලුතර" to listOf("kalutara", "kaluthara"),
        "களுத்துறை" to listOf("kalutara"),
        "මහනුවර" to listOf("kandy", "mahanuwara"),
        "නුවර" to listOf("kandy"),
        "கண்டி" to listOf("kandy"),
        "මාතලේ" to listOf("matale"),
        "மாத்தளை" to listOf("matale"),
        "නුවරඑළිය" to listOf("nuwara eliya", "nuwaraeliya"),
        "නුවර එළිය" to listOf("nuwara eliya"),
        "நுவரெலியா" to listOf("nuwara eliya"),
        "ගාල්ල" to listOf("galle"),
        "காலி" to listOf("galle"),
        "මාතර" to listOf("matara", "mathara"),
        "මාතறை" to listOf("matara"),
        "மாத்தறை" to listOf("matara"),
        "හම්බන්තොට" to listOf("hambantota"),
        "ஹம்பாந்தோட்டை" to listOf("hambantota"),
        "அம்பாந்தோட்டை" to listOf("hambantota"),
        "යාපනය" to listOf("jaffna"),
        "யாழ்ப்பாணம்" to listOf("jaffna"),
        "කිලිනොච්චිය" to listOf("kilinochchi"),
        "கிளிநொச்சி" to listOf("kilinochchi"),
        "මන්නාරම" to listOf("mannar"),
        "மன்னார்" to listOf("mannar"),
        "වවුනියාව" to listOf("vavuniya", "wawuniya"),
        "வவுனியா" to listOf("vavuniya"),
        "මුලතිව්" to listOf("mullaitivu", "mullativu"),
        "මුලතිව්" to listOf("mullaitivu"),
        "முல்லைத்தீவு" to listOf("mullaitivu"),
        "මඩකලපුව" to listOf("batticaloa"),
        "மட்டக்களப்பு" to listOf("batticaloa"),
        "අම්පාර" to listOf("ampara"),
        "அம்பாறை" to listOf("ampara"),
        "ත්‍රිකුණාමලය" to listOf("trincomalee", "trinco"),
        "ත්රිකුණාමලය" to listOf("trincomalee"),
        "திருகோணமலை" to listOf("trincomalee"),
        "කුරුණෑගල" to listOf("kurunegala"),
        "குருணாகல்" to listOf("kurunegala"),
        "පුත්තලම" to listOf("puttalam"),
        "புத்தளம்" to listOf("puttalam"),
        "අනුරාධපුර" to listOf("anuradhapura"),
        "අනුරාධපුරය" to listOf("anuradhapura"),
        "அனுராதபுரம்" to listOf("anuradhapura"),
        "පොළොන්නරුව" to listOf("polonnaruwa"),
        "පොලොන්නරුව" to listOf("polonnaruwa"),
        "பொலன்னறுவை" to listOf("polonnaruwa"),
        "බදුල්ල" to listOf("badulla"),
        "பதுளை" to listOf("badulla"),
        "මොනරාගල" to listOf("monaragala"),
        "மொனராகலை" to listOf("monaragala"),
        "රත්නපුර" to listOf("ratnapura"),
        "රත්නපුරය" to listOf("ratnapura"),
        "இரத்தினபுரி" to listOf("ratnapura"),
        "කෑගල්ල" to listOf("kegalle"),
        "கேகாலை" to listOf("kegalle"),

        // Colombo Major Suburbs & Stations
        "කොටුව" to listOf("fort", "kotuwa"),
        "கோட்டை" to listOf("fort"),
        "පිටකොටුව" to listOf("pettah"),
        "புறக்கோட்டை" to listOf("pettah"),
        "මරදාන" to listOf("maradana"),
        "மருதானை" to listOf("maradana"),
        "බොරැල්ල" to listOf("borella"),
        "பொரளை" to listOf("borella"),
        "කොල්ලුපිටිය" to listOf("kollupitiya", "colpetty"),
        "கொள்ளுப்பிட்டி" to listOf("kollupitiya", "colpetty"),
        "බම්බලපිටිය" to listOf("bambalapitiya"),
        "பம்பலப்பிட்டி" to listOf("bambalapitiya"),
        "වැල්ලවත්ත" to listOf("wellawatte", "wellawatta"),
        "வெள்ளவத்தை" to listOf("wellawatte"),
        "දෙහිවල" to listOf("dehiwala"),
        "தெஹிவளை" to listOf("dehiwala"),
        "ගල්කිස්ස" to listOf("mount lavinia", "mount", "galkissa"),
        "கல்கிசை" to listOf("mount lavinia"),
        "මොරටුව" to listOf("moratuwa"),
        "மொறட்டுவை" to listOf("moratuwa"),
        "පානදුර" to listOf("panadura"),
        "பாணந்துறை" to listOf("panadura"),
        "නුගේගොඩ" to listOf("nugegoda"),
        "මහරගම" to listOf("maharagama"),
        "හෝමාගම" to listOf("homagama"),
        "කොට්ටාව" to listOf("kottawa"),
        "අවිස්සාවේල්ල" to listOf("avissawella"),
        "කඩුවෙල" to listOf("kaduwela"),
        "කඩවත" to listOf("kadawatha"),
        "කැළණිය" to listOf("kelaniya"),
        "කැලණිය" to listOf("kelaniya"),
        "ජාඇල" to listOf("ja-ela", "ja ela"),
        "කටුනායක" to listOf("katunayake", "airport"),
        "මීගමුව" to listOf("negombo"),
        "நீர்கொழும்பு" to listOf("negombo"),
        "රාගම" to listOf("ragama"),
        "කිරිබත්ගොඩ" to listOf("kiribathgoda"),
        "වත්තල" to listOf("wattala"),
        "බියගම" to listOf("biyagama"),
        "මිනුවන්ගොඩ" to listOf("minuwangoda"),
        "මීරිගම" to listOf("mirigama"),
        "නිට්ටඹුව" to listOf("nittambuwa"),
        "වේයන්ගොඩ" to listOf("veyangoda"),
        "හංවැල්ල" to listOf("hanwella"),
        "පාදුක්ක" to listOf("padukka"),
        "පිළියන්දල" to listOf("piliyandala"),
        "කැස්බෑව" to listOf("kesbewa"),
        "කොහුවල" to listOf("kohuwala"),
        "මිරිහාන" to listOf("mirihana"),
        "තලංගම" to listOf("thalangama", "talangama"),
        "වැලිසර" to listOf("welisara"),
        "වැලිගම" to listOf("weligama"),
        "අකුරැස්ස" to listOf("akuressa"),
        "දෙණියාය" to listOf("deniyaya"),
        "තංගල්ල" to listOf("tangalle", "tangalla"),
        "බෙලිඅත්ත" to listOf("beliatta"),
        "මිද්දෙනිය" to listOf("middeniya"),
        "තිස්සමහාරාමය" to listOf("tissamaharama"),
        "අම්බලන්තොට" to listOf("ambalantota"),
        "කටරගම" to listOf("kataragama"),
        "ඇඹිලිපිටිය" to listOf("ambilipitiya", "embilipitiya"),
        "බලංගොඩ" to listOf("balangoda"),
        "පැල්මඩුල්ල" to listOf("pelmadulla"),
        "කලවාන" to listOf("kalawana"),
        "ඇහැලියගොඩ" to listOf("eheliyagoda"),
        "මාවනැල්ල" to listOf("mawanella"),
        "වරකාපොල" to listOf("warakapola"),
        "රඹුක්කන" to listOf("rambukkana"),
        "දඹුල්ල" to listOf("dambulla"),
        "ගලේවෙල" to listOf("galewela"),
        "හබරණ" to listOf("habarana"),
        "සිගිරිය" to listOf("sigiriya"),
        "කුලියාපිටිය" to listOf("kuliyapitiya"),
        "නිකවැරටිය" to listOf("nikaweratiya"),
        "මහෝ" to listOf("maho"),
        "පන්නල" to listOf("pannala"),
        "හලාවත" to listOf("chilaw"),
        "මාරවිල" to listOf("marawila"),
        "වෙන්නප්පුව" to listOf("wennappuwa"),
        "අනමඩුව" to listOf("anamaduwa"),
        "කල්පිටිය" to listOf("kalpitiya"),
        "නොච්චියාගම" to listOf("nochchiyagama"),
        "කැකිරාව" to listOf("kekirawa"),
        "මැදවච්චිය" to listOf("medawachchiya"),
        "තඹුත්තේගම" to listOf("thambuttegama"),
        "හිඟුරක්ගොඩ" to listOf("hingurakgoda"),
        "මැදිරිගිරිය" to listOf("medirigiriya"),
        "බණ්ඩාරවෙල" to listOf("bandarawela"),
        "හපුතලේ" to listOf("haputale"),
        "වැලිමඩ" to listOf("welimada"),
        "මහියංගනය" to listOf("mahiyangana"),
        "සියඹලාණ්ඩුව" to listOf("siyambalanduwa"),
        "බිබිල" to listOf("bibile"),
        "වැල්ලවාය" to listOf("wellawaya"),
        "බුත්තල" to listOf("buttala"),
        "කල්මුණේ" to listOf("kalmunai"),
        "සමන්තුරේ" to listOf("sammanthurai"),
        "අක්කරපත්තුව" to listOf("akkaraipattu"),
        "පොතුවිල්" to listOf("pottuvil"),
        "කින්නියා" to listOf("kinniya"),
        "මුතූර්" to listOf("muttur"),
        "කන්තලේ" to listOf("kantale"),
        "වල්වෙට්ටිතුරෙයි" to listOf("valvettithurai"),
        "පේදුරුතුඩුව" to listOf("point pedro"),
        "චාවකච්චේරි" to listOf("chavakachcheri"),

        // Sri Lanka Police Specialized Units & Emergency Keywords
        "හදිසි" to listOf("emergency", "119", "hotline"),
        "හදිසි ඇමතුම්" to listOf("emergency", "119", "hotline"),
        "පොලිස්" to listOf("police"),
        "පොලිසිය" to listOf("police", "station"),
        "පොලිස් ස්ථානය" to listOf("police station"),
        "පොලිස් මූලස්ථානය" to listOf("police headquarters", "phq"),
        "පොලිස්පති" to listOf("igp", "inspector general"),
        "නියෝජ්‍ය පොලිස්පති" to listOf("dig", "deputy"),
        "ජ්‍යෙෂ්ඨ පොලිස් අධිකාරී" to listOf("ssp"),
        "පොලිස් අධිකාරී" to listOf("sp"),
        "ස්ථානාධිපති" to listOf("oic", "officer in charge"),
        "රථවාහන" to listOf("traffic", "oic traffic"),
        "අපරාධ" to listOf("crime", "oic crime"),
        "ළමා හා කාන්තා" to listOf("children", "women", "w&cb"),
        "ළමා" to listOf("child", "children"),
        "කාන්තා" to listOf("women", "female"),
        "විශේෂ කාර්ය බලකාය" to listOf("stf", "special task force"),
        "එස්ටීඑෆ්" to listOf("stf"),
        "ගිනි නිවන" to listOf("fire", "fire brigade", "110"),
        "රෝහල" to listOf("hospital", "1990", "suwaseriya"),
        "සුවසැරිය" to listOf("1990", "suwaseriya", "ambulance"),
        "ගිලන්රථ" to listOf("ambulance", "1990"),
        "අපරාධ පරීක්ෂණ" to listOf("cid"),
        "මත්ද්‍රව්‍ය" to listOf("narcotics", "pnbe"),
        "පරිසර" to listOf("environmental"),
        "නාවික" to listOf("marine"),
        "අධ්‍යක්ෂ" to listOf("director"),
        "අධ්‍යක්ෂක" to listOf("director")
    )

    /**
     * Algorithmic phonetic transliterator for Sinhala text into Roman characters.
     */
    fun transliterateSinhalaToEnglish(input: String): String {
        if (input.isBlank()) return ""
        val cleanInput = input.replace("\u200D", "").replace("\u200C", "")
        val sb = StringBuilder()
        var i = 0
        val len = cleanInput.length

        while (i < len) {
            val c = cleanInput[i]
            when (c) {
                // Independent Vowels
                'අ' -> sb.append("a")
                'ආ' -> sb.append("aa")
                'ඇ' -> sb.append("ae")
                'ඈ' -> sb.append("aae")
                'ඉ' -> sb.append("i")
                'ඊ' -> sb.append("ee")
                'උ' -> sb.append("u")
                'ඌ' -> sb.append("oo")
                'ඍ' -> sb.append("ru")
                'එ' -> sb.append("e")
                'ඒ' -> sb.append("ee")
                'ඓ' -> sb.append("ai")
                'ඔ' -> sb.append("o")
                'ඕ' -> sb.append("oo")
                'ඖ' -> sb.append("au")

                // Consonants
                'ක', 'ඛ', 'ග', 'ඝ', 'ඞ', 'ඟ',
                'ච', 'ඡ', 'ජ', 'ඣ', 'ඤ', 'ඥ', 'ඦ',
                'ට', 'ඨ', 'ඩ', 'ඪ', 'ණ', 'ඬ',
                'ත', 'ථ', 'ද', 'ධ', 'න', 'ඳ',
                'ප', 'ඵ', 'බ', 'භ', 'ම', 'ඹ',
                'ය', 'ර', 'ල', 'ව', 'ශ', 'ෂ', 'ස', 'හ', 'ළ', 'ෆ' -> {
                    val base = when (c) {
                        'ක' -> "k"
                        'ඛ' -> "kh"
                        'ග' -> "g"
                        'ඝ' -> "gh"
                        'ඞ', 'ඟ' -> "ng"
                        'ච' -> "ch"
                        'ඡ' -> "chh"
                        'ජ' -> "j"
                        'ඣ' -> "jh"
                        'ඤ', 'ඥ' -> "gn"
                        'ඦ' -> "nj"
                        'ට' -> "t"
                        'ඨ' -> "th"
                        'ඩ' -> "d"
                        'ඪ' -> "dh"
                        'ණ' -> "n"
                        'ඬ' -> "nd"
                        'ත' -> "th"
                        'ථ' -> "th"
                        'ද' -> "d"
                        'ධ' -> "dh"
                        'න' -> "n"
                        'ඳ' -> "nd"
                        'ප' -> "p"
                        'ඵ' -> "ph"
                        'බ' -> "b"
                        'භ' -> "bh"
                        'ම' -> "m"
                        'ඹ' -> "mb"
                        'ය' -> "y"
                        'ර' -> "r"
                        'ල' -> "l"
                        'ව' -> "w"
                        'ශ', 'ෂ' -> "sh"
                        'ස' -> "s"
                        'හ' -> "h"
                        'ළ' -> "l"
                        'ෆ' -> "f"
                        else -> ""
                    }
                    sb.append(base)

                    val next = if (i + 1 < len) cleanInput[i + 1] else null
                    var consumedNext = false

                    if (next != null) {
                        when (next) {
                            '්' -> {
                                consumedNext = true
                            }
                            'ා' -> {
                                sb.append("a")
                                consumedNext = true
                            }
                            'ැ' -> {
                                sb.append("a")
                                consumedNext = true
                            }
                            'ෑ' -> {
                                sb.append("ae")
                                consumedNext = true
                            }
                            'ි' -> {
                                sb.append("i")
                                consumedNext = true
                            }
                            'ී' -> {
                                sb.append("ee")
                                consumedNext = true
                            }
                            'ු' -> {
                                sb.append("u")
                                consumedNext = true
                            }
                            'ූ' -> {
                                sb.append("oo")
                                consumedNext = true
                            }
                            'ෘ' -> {
                                sb.append("ru")
                                consumedNext = true
                            }
                            'ෲ' -> {
                                sb.append("roo")
                                consumedNext = true
                            }
                            'ෙ' -> {
                                sb.append("e")
                                consumedNext = true
                            }
                            'ේ' -> {
                                sb.append("e")
                                consumedNext = true
                            }
                            'ෛ' -> {
                                sb.append("ai")
                                consumedNext = true
                            }
                            'ො' -> {
                                sb.append("o")
                                consumedNext = true
                            }
                            'ෝ' -> {
                                sb.append("o")
                                consumedNext = true
                            }
                            'ෞ' -> {
                                sb.append("au")
                                consumedNext = true
                            }
                            else -> {
                                sb.append("a")
                            }
                        }
                    } else {
                        sb.append("a")
                    }

                    if (consumedNext) i++
                }

                // Miscellaneous marks
                'ං' -> sb.append("n")
                'ඃ' -> sb.append("h")
                ' ' -> sb.append(" ")
                '-' -> sb.append("-")
                else -> {
                    if (c.isLetterOrDigit()) sb.append(c)
                }
            }
            i++
        }
        return sb.toString().trim()
    }

    /**
     * Algorithmic phonetic transliterator for Tamil text into Roman characters.
     */
    fun transliterateTamilToEnglish(input: String): String {
        if (input.isBlank()) return ""
        val cleanInput = input.replace("\u200D", "").replace("\u200C", "")
        val sb = StringBuilder()
        var i = 0
        val len = cleanInput.length

        while (i < len) {
            val c = cleanInput[i]
            when (c) {
                // Independent Vowels
                'அ' -> sb.append("a")
                'ஆ' -> sb.append("aa")
                'இ' -> sb.append("i")
                'ஈ' -> sb.append("ee")
                'உ' -> sb.append("u")
                'ஊ' -> sb.append("oo")
                'எ' -> sb.append("e")
                'ஏ' -> sb.append("ee")
                'ஐ' -> sb.append("ai")
                'ஒ' -> sb.append("o")
                'ஓ' -> sb.append("oo")
                'ஔ' -> sb.append("au")

                // Consonants
                'க', 'ங', 'ச', 'ஞ', 'ட', 'ண', 'த', 'ந', 'ப', 'ம',
                'ய', 'ர', 'ல', 'வ', 'ழ', 'ள', 'ற', 'ன', 'ஜ', 'ஷ', 'ஸ', 'ஹ' -> {
                    val base = when (c) {
                        'க' -> "k"
                        'ங' -> "ng"
                        'ச' -> "s"
                        'ஞ' -> "gn"
                        'ட' -> "t"
                        'ண' -> "n"
                        'த' -> "th"
                        'ந' -> "n"
                        'ப' -> "p"
                        'ம' -> "m"
                        'ய' -> "y"
                        'ர' -> "r"
                        'ல' -> "l"
                        'வ' -> "v"
                        'ழ' -> "zh"
                        'ள' -> "l"
                        'ற' -> "r"
                        'ன' -> "n"
                        'ஜ' -> "j"
                        'ஷ' -> "sh"
                        'ஸ' -> "s"
                        'ஹ' -> "h"
                        else -> ""
                    }
                    sb.append(base)

                    val next = if (i + 1 < len) cleanInput[i + 1] else null
                    var consumedNext = false

                    if (next != null) {
                        when (next) {
                            '்' -> {
                                consumedNext = true
                            }
                            'ா' -> {
                                sb.append("a")
                                consumedNext = true
                            }
                            'ி' -> {
                                sb.append("i")
                                consumedNext = true
                            }
                            'ீ' -> {
                                sb.append("ee")
                                consumedNext = true
                            }
                            'ு' -> {
                                sb.append("u")
                                consumedNext = true
                            }
                            'ூ' -> {
                                sb.append("oo")
                                consumedNext = true
                            }
                            'ெ' -> {
                                sb.append("e")
                                consumedNext = true
                            }
                            'ே' -> {
                                sb.append("ee")
                                consumedNext = true
                            }
                            'ை' -> {
                                sb.append("ai")
                                consumedNext = true
                            }
                            'ொ' -> {
                                sb.append("o")
                                consumedNext = true
                            }
                            'ோ' -> {
                                sb.append("oo")
                                consumedNext = true
                            }
                            'ௌ' -> {
                                sb.append("au")
                                consumedNext = true
                            }
                            else -> {
                                sb.append("a")
                            }
                        }
                    } else {
                        sb.append("a")
                    }

                    if (consumedNext) i++
                }

                'ஃ' -> sb.append("h")
                ' ' -> sb.append(" ")
                '-' -> sb.append("-")
                else -> {
                    if (c.isLetterOrDigit()) sb.append(c)
                }
            }
            i++
        }
        return sb.toString().trim()
    }

    private val queryTokensCache = ConcurrentHashMap<String, List<List<String>>>()

    /**
     * Resolves a raw search query (which may be in Sinhala, Tamil, or English)
     * into a comprehensive list of English search tokens and candidate terms.
     */
    fun extractSearchTokens(rawQuery: String): List<List<String>> {
        val clean = rawQuery.trim().lowercase()
        if (clean.isBlank()) return emptyList()

        queryTokensCache[clean]?.let { return it }

        val resultTokens = mutableListOf<List<String>>()

        // 1. Direct dictionary full-phrase match check
        val fullDictMatch = DICTIONARY[clean]
        if (!fullDictMatch.isNullOrEmpty()) {
            val list = listOf(fullDictMatch + listOf(clean))
            if (queryTokensCache.size < 300) queryTokensCache[clean] = list
            return list
        }

        // 2. Tokenize by whitespace
        val words = clean.split(' ').filter { it.isNotBlank() }

        for (word in words) {
            val tokenCandidates = LinkedHashSet<String>()

            // (a) Original word
            tokenCandidates.add(word)

            // (b) Exact dictionary lookup for this word
            DICTIONARY[word]?.let {
                tokenCandidates.addAll(it)
            }

            // Substring lookup (Beginning, Middle, End) if word length >= 2
            if (word.length >= 2) {
                var matchesFound = 0
                for ((key, englishList) in DICTIONARY) {
                    if (key.contains(word) || word.contains(key)) {
                        tokenCandidates.addAll(englishList)
                        matchesFound++
                        if (matchesFound >= 6) break
                    }
                }
            }

            // (c) Algorithmic transliteration if word is Sinhala or Tamil
            try {
                if (isSinhala(word)) {
                    val trans = transliterateSinhalaToEnglish(word)
                    if (trans.isNotBlank()) {
                        tokenCandidates.add(trans)
                        tokenCandidates.add(trans.replace("w", "v"))
                        tokenCandidates.add(trans.replace("th", "t"))
                        tokenCandidates.add(trans.replace("ee", "i"))
                        tokenCandidates.add(trans.replace("oo", "u"))
                    }
                } else if (isTamil(word)) {
                    val trans = transliterateTamilToEnglish(word)
                    if (trans.isNotBlank()) {
                        tokenCandidates.add(trans)
                        tokenCandidates.add(trans.replace("w", "v"))
                        tokenCandidates.add(trans.replace("th", "t"))
                        tokenCandidates.add(trans.replace("ee", "i"))
                        tokenCandidates.add(trans.replace("oo", "u"))
                    }
                }
            } catch (e: Exception) {
                tokenCandidates.add(word)
            }

            resultTokens.add(tokenCandidates.take(6).toList())
        }

        if (queryTokensCache.size < 300) {
            queryTokensCache[clean] = resultTokens
        }
        return resultTokens
    }

    // Dynamic Translation Map for English Google Sheet entries into Sinhala
    private val SINHALA_TRANSLATION_RULES: List<Pair<Regex, String>> = listOf(
        // Multi-word Phrases & Special Ranks
        Regex("(?i)\\bsenior deputy inspector general\\b|\\bsenior dig\\b") to "ජ්‍යෙෂ්ඨ නියෝජ්‍ය පොලිස්පති (Senior DIG)",
        Regex("(?i)\\binspector general of police\\b|\\bigp\\b") to "පොලිස්පති (IGP)",
        Regex("(?i)\\bdeputy inspector general\\b|\\bdig\\b") to "නියෝජ්‍ය පොලිස්පති (DIG)",
        Regex("(?i)\\bsenior superintendent of police\\b|\\bssp\\b") to "ජ්‍යෙෂ්ඨ පොලිස් අධිකාරී (SSP)",
        Regex("(?i)\\bsuperintendent of police\\b|\\bsp\\b") to "පොලිස් අධිකාරී (SP)",
        Regex("(?i)\\bassistant superintendent of police\\b|\\basp\\b") to "සහකාර පොලිස් අධිකාරී (ASP)",
        Regex("(?i)\\bpolice headquarters\\b") to "පොලිස් මූලස්ථානය",
        Regex("(?i)\\bpolice station\\b") to "පොලිස් ස්ථානය",
        Regex("(?i)\\bpolice division\\b") to "පොලිස් කොට්ඨාසය",
        Regex("(?i)\\bpolice range\\b") to "පොලිස් කලාපය",
        Regex("(?i)\\bspecial task force\\b") to "විශේෂ කාර්ය බලකාය (STF)",
        Regex("(?i)\\bchildren [&s]+ women bureau\\b|\\bchild [&s]+ women bureau\\b") to "ළමා හා කාන්තා කාර්යාංශය",
        Regex("(?i)\\bfire station\\b|\\bfire brigade\\b") to "ගිනි නිවන හමුදා මධ්‍යස්ථානය",
        Regex("(?i)\\boic traffic\\b") to "ස්ථානාධිපති (රථවාහන)",
        Regex("(?i)\\boic crime\\b") to "ස්ථානාධිපති (අපරාධ)",
        Regex("(?i)\\boic vice\\b") to "ස්ථානාධිපති (විනීතකම්)",
        Regex("(?i)\\boic community policing\\b") to "ස්ථානාධිපති (ප්‍රජා පොලිස්)",
        Regex("(?i)\\boic\\b|\\bofficer in charge\\b") to "ස්ථානාධිපති (OIC)",
        Regex("(?i)\\btransport [&s]+ logistics\\b") to "ප්‍රවාහන හා සම්පත්",
        Regex("(?i)\\btraffic [&s]+ road safety\\b") to "රථවාහන සහ මාර්ග ආරක්ෂණ",
        Regex("(?i)\\bfield force headquarters\\b") to "ක්ෂේත්‍ර බලකා මූලස්ථානය",
        Regex("(?i)\\bfield force\\b") to "ක්ෂේත්‍ර බලකා",
        Regex("(?i)\\bstate intelligence service\\b|\\bsis\\b") to "රාජ්‍ය බුද්ධි සේවය (SIS)",
        Regex("(?i)\\bcriminal investigation department\\b|\\bcid\\b") to "අපරාධ පරීක්ෂණ දෙපාර්තමේන්තුව (CID)",
        Regex("(?i)\\bpolice media division\\b") to "පොලිස් මාධ්‍ය කොට්ඨාසය",
        Regex("(?i)\\bmounted division\\b") to "අශ්වාරෝහක කොට්ඨාසය",
        Regex("(?i)\\bkennel division\\b") to "පොලිස් සුනඛ කොට්ඨාසය",

        // Provinces
        Regex("(?i)\\bwestern province\\b") to "බස්නාහිර පළාත",
        Regex("(?i)\\bcentral province\\b") to "මධ්‍යම පළාත",
        Regex("(?i)\\bsouthern province\\b") to "දකුණු පළාත",
        Regex("(?i)\\bnorthern province\\b") to "උතුරු පළාත",
        Regex("(?i)\\beastern province\\b") to "නැගෙනහිර පළාත",
        Regex("(?i)\\bnorth western province\\b") to "වයඹ පළාත",
        Regex("(?i)\\bnorth central province\\b") to "උතුරු මැද පළාත",
        Regex("(?i)\\buva province\\b") to "ඌව පළාත",
        Regex("(?i)\\bsabaragamuwa province\\b") to "සබරගමුව පළාත",

        // Key Operational Terms
        Regex("(?i)\\btransport\\b") to "ප්‍රවාහන",
        Regex("(?i)\\bheadquarters\\b|\\bhq\\b") to "මූලස්ථානය",
        Regex("(?i)\\bdivision\\b") to "කොට්ඨාසය",
        Regex("(?i)\\brange\\b") to "කලාපය",
        Regex("(?i)\\btraffic\\b") to "රථවාහන",
        Regex("(?i)\\bcrime\\b") to "අපරාධ",
        Regex("(?i)\\bchildren\\b|\\bchild\\b") to "ළමා",
        Regex("(?i)\\bwomen\\b") to "කාන්තා",
        Regex("(?i)\\bdirector\\b") to "අධ්‍යක්ෂක",
        Regex("(?i)\\bdeputy\\b") to "නියෝජ්‍ය",
        Regex("(?i)\\bsenior\\b") to "ජ්‍යෙෂ්ඨ",
        Regex("(?i)\\bpolice\\b") to "පොලිස්",
        Regex("(?i)\\bstation\\b") to "ස්ථානය",
        Regex("(?i)\\bhospital\\b") to "රෝහල",
        Regex("(?i)\\bemergency\\b") to "හදිසි",
        Regex("(?i)\\bmarine\\b") to "නාවික",
        Regex("(?i)\\benvironmental\\b") to "පරිසර",
        Regex("(?i)\\bnarcotics\\b") to "මත්ද්‍රව්‍ය නාශක",
        Regex("(?i)\\bbureau\\b") to "කාර්යාංශය",
        Regex("(?i)\\bunit\\b") to "ඒකකය",
        Regex("(?i)\\bbranch\\b") to "අංශය",
        Regex("(?i)\\bdepartment\\b") to "දෙපාර්තමේන්තුව",
        Regex("(?i)\\blogistics\\b") to "සම්පත් හා සැපයුම්",
        Regex("(?i)\\badministration\\b") to "පාලන",
        Regex("(?i)\\bwelfare\\b") to "සුබසාධන",
        Regex("(?i)\\bcommunication\\b") to "සන්නිවේදන",
        Regex("(?i)\\btourist\\b") to "සංචාරක",
        Regex("(?i)\\bsecurity\\b") to "ආරක්ෂක",
        Regex("(?i)\\binvestigation\\b") to "පරීක්ෂණ",
        Regex("(?i)\\bintelligence\\b") to "බුද්ධි",

        // Major Towns & Cities
        Regex("(?i)\\bfort\\b") to "කොටුව",
        Regex("(?i)\\bpettah\\b") to "පිටකොටුව",
        Regex("(?i)\\bmaradana\\b") to "මරදාන",
        Regex("(?i)\\bborella\\b") to "බොරැල්ල",
        Regex("(?i)\\bkollupitiya\\b|\\bcolpetty\\b") to "කොල්ලුපිටිය",
        Regex("(?i)\\bbambalapitiya\\b") to "බම්බලපිටිය",
        Regex("(?i)\\bwellawatte\\b|\\bwellawatta\\b") to "වැල්ලවත්ත",
        Regex("(?i)\\bdehiwala\\b") to "දෙහිවල",
        Regex("(?i)\\bmount lavinia\\b|\\bgalkissa\\b") to "ගල්කිස්ස",
        Regex("(?i)\\bmoratuwa\\b") to "මොරටුව",
        Regex("(?i)\\bpanadura\\b") to "පානදුර",
        Regex("(?i)\\bnugegoda\\b") to "නුගේගොඩ",
        Regex("(?i)\\bmaharagama\\b") to "මහරගම",
        Regex("(?i)\\bhomagama\\b") to "හෝමාගම",
        Regex("(?i)\\bkottawa\\b") to "කොට්ටාව",
        Regex("(?i)\\bavissawella\\b") to "අවිස්සාවේල්ල",
        Regex("(?i)\\bkaduwela\\b") to "කඩුවෙල",
        Regex("(?i)\\bkadawatha\\b") to "කඩවත",
        Regex("(?i)\\bkelaniya\\b") to "කැලණිය",
        Regex("(?i)\\bja-ela\\b|\\bja ela\\b") to "ජාඇල",
        Regex("(?i)\\bkatunayake\\b") to "කටුනායක",
        Regex("(?i)\\bnegombo\\b") to "මීගමුව",
        Regex("(?i)\\bragama\\b") to "රාගම",
        Regex("(?i)\\bwattala\\b") to "වත්තල",
        Regex("(?i)\\bkiribathgoda\\b") to "කිරිබත්ගොඩ",
        Regex("(?i)\\bgampaha\\b") to "ගම්පහ",
        Regex("(?i)\\bkalutara\\b|\\bkaluthara\\b") to "කළුතර",
        Regex("(?i)\\bkandy\\b|\\bmahanuwara\\b") to "මහනුවර",
        Regex("(?i)\\bmatale\\b") to "මාතලේ",
        Regex("(?i)\\bnuwara eliya\\b|\\bnuwaraeliya\\b") to "නුවරඑළිය",
        Regex("(?i)\\bgalle\\b") to "ගාල්ල",
        Regex("(?i)\\bmatara\\b|\\bmathara\\b") to "මාතර",
        Regex("(?i)\\bhambantota\\b") to "හම්බන්තොට",
        Regex("(?i)\\bjaffna\\b") to "යාපනය",
        Regex("(?i)\\bkilinochchi\\b") to "කිලිනොච්චිය",
        Regex("(?i)\\bmannar\\b") to "මන්නාරම",
        Regex("(?i)\\bvavuniya\\b") to "වවුනියාව",
        Regex("(?i)\\bmullaitivu\\b|\\bmullativu\\b") to "මුලතිව්",
        Regex("(?i)\\bbatticaloa\\b") to "මඩකලපුව",
        Regex("(?i)\\bampara\\b") to "අම්පාර",
        Regex("(?i)\\btrincomalee\\b|\\btrinco\\b") to "ත්‍රිකුණාමලය",
        Regex("(?i)\\bkurunegala\\b") to "කුරුණෑගල",
        Regex("(?i)\\bputtalam\\b") to "පුත්තලම",
        Regex("(?i)\\banuradhapura\\b") to "අනුරාධපුරය",
        Regex("(?i)\\bpolonnaruwa\\b") to "පොළොන්නරුව",
        Regex("(?i)\\bbadulla\\b") to "බදුල්ල",
        Regex("(?i)\\bmonaragala\\b") to "මොනරාගල",
        Regex("(?i)\\bratnapura\\b") to "රත්නපුර",
        Regex("(?i)\\bkegalle\\b") to "කෑගල්ල",
        Regex("(?i)\\bcolombo\\b") to "කොළඹ"
    )

    // Dynamic Translation Map for English Google Sheet entries into Tamil
    private val TAMIL_TRANSLATION_RULES: List<Pair<Regex, String>> = listOf(
        Regex("(?i)\\bsenior deputy inspector general\\b|\\bsenior dig\\b") to "ஜேஷ்ட பிரதி பொலிஸ் மா அதிபர் (Senior DIG)",
        Regex("(?i)\\binspector general of police\\b|\\bigp\\b") to "பொலிஸ் மா அதிபர் (IGP)",
        Regex("(?i)\\bdeputy inspector general\\b|\\bdig\\b") to "பிரதி பொலிஸ் மா அதிபர் (DIG)",
        Regex("(?i)\\bsenior superintendent of police\\b|\\bssp\\b") to "ஜேஷ்ட பொலிஸ் அத்தியட்சகர் (SSP)",
        Regex("(?i)\\bsuperintendent of police\\b|\\bsp\\b") to "பொலிஸ் அத்தியட்சகர் (SP)",
        Regex("(?i)\\bpolice headquarters\\b") to "பொலிஸ் தலைமையகம்",
        Regex("(?i)\\bpolice station\\b") to "பொலிஸ் நிலையம்",
        Regex("(?i)\\bpolice division\\b") to "பொலிஸ் கோட்டம்",
        Regex("(?i)\\bpolice range\\b") to "பொலிஸ் வலயம்",
        Regex("(?i)\\bspecial task force\\b") to "விசேட அதிரடிப் படை (STF)",
        Regex("(?i)\\bfire station\\b|\\bfire brigade\\b") to "தீயணைப்புப் படை நிலையம்",
        Regex("(?i)\\boic traffic\\b") to "நிலைய பொறுப்பதிகாரி (போக்குவரத்து)",
        Regex("(?i)\\boic crime\\b") to "நிலைய பொறுப்பதிகாரி (குற்றப்பிரிவு)",
        Regex("(?i)\\boic\\b|\\bofficer in charge\\b") to "நிலைய பொறுப்பதிகாரி (OIC)",
        Regex("(?i)\\bwestern province\\b") to "மேல் மாகாணம்",
        Regex("(?i)\\bcentral province\\b") to "மத்திய மாகாணம்",
        Regex("(?i)\\bsouthern province\\b") to "தென் மாகாணம்",
        Regex("(?i)\\bnorthern province\\b") to "வட மாகாணம்",
        Regex("(?i)\\beastern province\\b") to "கிழக்கு மாகாணம்",
        Regex("(?i)\\btransport\\b") to "போக்குவரத்து",
        Regex("(?i)\\bheadquarters\\b|\\bhq\\b") to "தலைமையகம்",
        Regex("(?i)\\bdivision\\b") to "கோட்டம்",
        Regex("(?i)\\brange\\b") to "வலயம்",
        Regex("(?i)\\btraffic\\b") to "போக்குவரத்து",
        Regex("(?i)\\bcrime\\b") to "குற்றப்பிரிவு",
        Regex("(?i)\\bchildren\\b|\\bchild\\b") to "சிறுவர்",
        Regex("(?i)\\bwomen\\b") to "மகளிர்",
        Regex("(?i)\\bdirector\\b") to "இயக்குனர்",
        Regex("(?i)\\bdeputy\\b") to "பிரதி",
        Regex("(?i)\\bsenior\\b") to "சிரேஷ்ட",
        Regex("(?i)\\bpolice\\b") to "பொலிஸ்",
        Regex("(?i)\\bstation\\b") to "நிலையம்",
        Regex("(?i)\\bhospital\\b") to "வைத்தியசாலை",
        Regex("(?i)\\bemergency\\b") to "அவசர",
        Regex("(?i)\\bbureau\\b") to "பணியகம்",
        Regex("(?i)\\bunit\\b") to "பிரிவு",
        Regex("(?i)\\bbranch\\b") to "கிளை",
        Regex("(?i)\\bdepartment\\b") to "திணைக்களம்",
        Regex("(?i)\\blogistics\\b") to "தளவாடங்கள்",
        Regex("(?i)\\badministration\\b") to "நிர்வாகம்",
        Regex("(?i)\\bwelfare\\b") to "நலன்புரி",
        Regex("(?i)\\bcommunication\\b") to "தொடர்பாடல்",
        Regex("(?i)\\btourist\\b") to "சுற்றுலா",
        Regex("(?i)\\bsecurity\\b") to "பாதுகாப்பு",
        Regex("(?i)\\binvestigation\\b") to "விசாரணை",
        Regex("(?i)\\bintelligence\\b") to "நுண்ணறிவு",
        Regex("(?i)\\bcolombo\\b") to "கொழும்பு",
        Regex("(?i)\\bgampaha\\b") to "கம்பஹா",
        Regex("(?i)\\bkalutara\\b") to "களுத்துறை",
        Regex("(?i)\\bkandy\\b") to "கண்டி",
        Regex("(?i)\\bmatale\\b") to "மாத்தளை",
        Regex("(?i)\\bnuwara eliya\\b") to "நுவரெலியா",
        Regex("(?i)\\bgalle\\b") to "காலி",
        Regex("(?i)\\bmatara\\b") to "மாத்தறை",
        Regex("(?i)\\bhambantota\\b") to "ஹம்பாந்தோட்டை",
        Regex("(?i)\\bjaffna\\b") to "யாழ்ப்பாணம்",
        Regex("(?i)\\bkilinochchi\\b") to "கிளிநொச்சி",
        Regex("(?i)\\bmannar\\b") to "மன்னார்",
        Regex("(?i)\\bvavuniya\\b") to "வவுனியா",
        Regex("(?i)\\bmullaitivu\\b") to "முல்லைத்தீவு",
        Regex("(?i)\\bbatticaloa\\b") to "மட்டக்களப்பு",
        Regex("(?i)\\bampara\\b") to "அம்பாறை",
        Regex("(?i)\\btrincomalee\\b") to "திருகோணமலை",
        Regex("(?i)\\bkurunegala\\b") to "குருணாகல்",
        Regex("(?i)\\bputtalam\\b") to "புத்தளம்",
        Regex("(?i)\\banuradhapura\\b") to "அனுராதபுரம்",
        Regex("(?i)\\bpolonnaruwa\\b") to "பொலன்னறுவை",
        Regex("(?i)\\bbadulla\\b") to "பதுளை",
        Regex("(?i)\\bmonaragala\\b") to "மொனராகலை",
        Regex("(?i)\\bratnapura\\b") to "இரத்தினபுரி",
        Regex("(?i)\\bkegalle\\b") to "கேகாலை"
    )

    fun translateText(text: String, targetLanguage: com.example.util.AppLanguage): String {
        if (text.isBlank()) return text
        if (isSinhala(text) || isTamil(text)) return text

        var translated = text
        val rules = when (targetLanguage) {
            com.example.util.AppLanguage.SINHALA -> SINHALA_TRANSLATION_RULES
            com.example.util.AppLanguage.TAMIL -> TAMIL_TRANSLATION_RULES
            else -> emptyList()
        }

        for ((regex, replacement) in rules) {
            translated = regex.replace(translated, replacement)
        }

        return translated
    }

    fun translateContactForDisplay(
        contact: PoliceContact,
        targetLanguage: com.example.util.AppLanguage,
        query: String = ""
    ): PoliceContact {
        val lang = when {
            isSinhala(query) -> com.example.util.AppLanguage.SINHALA
            isTamil(query) -> com.example.util.AppLanguage.TAMIL
            else -> targetLanguage
        }

        if (lang == com.example.util.AppLanguage.ENGLISH) return contact

        return contact.copy(
            stationOrDesignation = translateText(contact.stationOrDesignation, lang),
            rank = translateText(contact.rank, lang),
            officerName = translateText(contact.officerName, lang),
            locationAddress = translateText(contact.locationAddress, lang)
        )
    }

    /**
     * Checks if a PoliceContact matches the given search query across all fields,
     * supporting Sinhala, Tamil, and English inputs safely and fast.
     */
    fun matchesContact(contact: PoliceContact, rawQuery: String): Boolean {
        val q = rawQuery.trim().lowercase()
        if (q.isBlank()) return true

        val corpus = contact.stationOrDesignation.lowercase() + " " +
                contact.officerName.lowercase() + " " +
                contact.rank.lowercase() + " " +
                contact.locationAddress.lowercase() + " " +
                contact.generalPhone + " " +
                contact.mobilePhone + " " +
                contact.pvtNumber + " " +
                contact.email.lowercase()

        // Fast-path: Direct substring match in corpus
        if (corpus.contains(q)) return true

        val tokenGroups = extractSearchTokens(q)
        if (tokenGroups.isEmpty()) return true

        return tokenGroups.all { candidateList ->
            candidateList.any { candidate ->
                candidate.isNotBlank() && (
                    corpus.contains(candidate) ||
                    (candidate.length >= 3 && corpus.contains(candidate.take(3)))
                )
            }
        }
    }

    /**
     * Checks if a User matches search query across display name, district, and badge.
     */
    fun matchesUser(displayName: String, district: String, badge: String, rawQuery: String): Boolean {
        val q = rawQuery.trim().lowercase()
        if (q.isBlank()) return true

        val corpus = "$displayName $district $badge".lowercase()
        if (corpus.contains(q)) return true

        val tokenGroups = extractSearchTokens(q)
        if (tokenGroups.isEmpty()) return true

        return tokenGroups.all { candidateList ->
            candidateList.any { candidate ->
                candidate.isNotBlank() && (
                    corpus.contains(candidate) ||
                    (candidate.length >= 3 && corpus.contains(candidate.take(3)))
                )
            }
        }
    }
}
