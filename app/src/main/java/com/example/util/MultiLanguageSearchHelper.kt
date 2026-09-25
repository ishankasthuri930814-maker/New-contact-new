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
