package com.example.util

import com.example.data.model.PoliceContact

/**
 * Intelligent Multilingual Search Helper for Sri Lankan Police & Emergency Services.
 * Seamlessly resolves Sinhala and Tamil queries against English Google Sheet data
 * using a high-precision dual-layer engine:
 * 1. Comprehensive Sri Lankan Administrative, Geographical & Police terminology dictionary.
 * 2. Full phonetic Sinhala-to-Latin and Tamil-to-Latin transliteration algorithms.
 * 3. Multi-token and fuzzy phonetic matching (w/v, th/t, k/c, ee/i, oo/u).
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
        "ஹம்பாந்தோட்டை" to listOf("hambantota"),
        "හම්බන්තොට" to listOf("hambantota"),
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
        "දෙහිවල" to listOf("dehiwala", "dehiwela"),
        "தெஹிவளை" to listOf("dehiwala"),
        "ගල්කිස්ස" to listOf("mount lavinia", "galkissa"),
        "මොරටුව" to listOf("moratuwa"),
        "மொறட்டுவை" to listOf("moratuwa"),
        "නුගේගොඩ" to listOf("nugegoda"),
        "මහරගම" to listOf("maharagama"),
        "හෝමාගම" to listOf("homagama"),
        "කොට්ටාව" to listOf("kottawa"),
        "මිරිහාන" to listOf("mirihana"),
        "තලංගම" to listOf("talangama", "thalangama"),
        "බත්තරමුල්ල" to listOf("battaramulla"),
        "රාජගිරිය" to listOf("rajagiriya"),
        "කඩුවෙල" to listOf("kaduwela"),
        "හංවැල්ල" to listOf("hanwella"),
        "පාදුක්ක" to listOf("padukka"),
        "අවිස්සාවේල්ල" to listOf("avissawella"),
        "නාරාහේන්පිට" to listOf("narahenpita"),
        "நாரஹேன்பிட்ட" to listOf("narahenpita"),
        "දෙමටගොඩ" to listOf("dematagoda"),
        "தெமட்டகொட" to listOf("dematagoda"),
        "ග්‍රෑන්ඩ්පාස්" to listOf("grandpass"),
        "கிராண்ட்பாஸ்" to listOf("grandpass"),
        "කොටහේන" to listOf("kotahena"),
        "கொட்டாஞ்சேனை" to listOf("kotahena"),
        "මට්ටක්කුලිය" to listOf("mattakkuliya"),
        "மட்டக்குளி" to listOf("mattakkuliya"),
        "මෝදර" to listOf("modara", "mutwal"),
        "කොම්පඤ්ඤවීදිය" to listOf("slave island", "kompannaveediya"),
        "කුරුඳුවත්ත" to listOf("cinnamon gardens", "kurunduwatta"),
        "මාළිගාවත්ත" to listOf("maligawatta"),
        "වැල්ලම්පිටිය" to listOf("wellampitiya"),
        "කොහුවල" to listOf("kohuwala", "kohuwela"),
        "වැලිසර" to listOf("welisara"),
        "රාගම" to listOf("ragama"),
        "කැලණිය" to listOf("kelaniya"),
        "களனி" to listOf("kelaniya"),
        "කිරිබත්ගොඩ" to listOf("kiribathgoda"),
        "කඩවත" to listOf("kadawatha"),
        "වත්තල" to listOf("wattala"),
        "வத்தளை" to listOf("wattala"),
        "ජාඇල" to listOf("ja-ela", "ja ela"),
        "සීදුව" to listOf("seeduwa"),
        "කටුනායක" to listOf("katunayake"),
        "මීගමුව" to listOf("negombo"),
        "நீர்கொழும்பு" to listOf("negombo"),
        "මිනුවන්ගොඩ" to listOf("minuwangoda"),
        "මීරිගම" to listOf("mirigama"),
        "නිට්ටඹුව" to listOf("nittambuwa"),
        "වේයන්ගොඩ" to listOf("veyangoda"),
        "දිවුලපිටිය" to listOf("divulapitiya"),

        // Western & Southern Towns
        "පානදුර" to listOf("panadura"),
        "பாணந்துறை" to listOf("panadura"),
        "වාද්දුව" to listOf("wadduwa"),
        "හොරණ" to listOf("horana"),
        "බණ්ඩාරගම" to listOf("bandaragama"),
        "මතුගම" to listOf("matugama"),
        "බේරුවල" to listOf("beruwala"),
        "பேருவளை" to listOf("beruwala"),
        "අලුත්ගම" to listOf("aluthgama"),
        "ඉංගිරිය" to listOf("ingiriya"),
        "බුලත්සිංහල" to listOf("bulathsinhala"),
        "අගලවත්ත" to listOf("agalawatta"),
        "හික්කඩුව" to listOf("hikkaduwa"),
        "අම්බලන්ගොඩ" to listOf("ambalangoda"),
        "ඇල්පිටිය" to listOf("elpitiya"),
        "බෙන්තොට" to listOf("bentota"),
        "බද්දේගම" to listOf("baddegama"),
        "හබරාදූව" to listOf("habaraduwa"),
        "අහංගම" to listOf("ahangama"),
        "වැලිගම" to listOf("weligama"),
        "අකුරැස්ස" to listOf("akuressa"),
        "දික්වැල්ල" to listOf("dickwella"),
        "කඹුරුපිටිය" to listOf("kamburupitiya"),
        "හක්මන" to listOf("hakmana"),
        "දෙනියාය" to listOf("deniyaya"),
        "තංගල්ල" to listOf("tangalle", "tangalla"),
        "බෙලිඅත්ත" to listOf("beliatta"),
        "වීරකැටිය" to listOf("weeraketiya"),
        "අම්බලන්තොට" to listOf("ambalantota"),
        "තිස්සමහාරාම" to listOf("tissamaharama"),
        "කටරගම" to listOf("kataragama"),
        "කතරගම" to listOf("kataragama"),
        "මිද්දෙනිය" to listOf("middeniya"),
        "වලස්මුල්ල" to listOf("walasmulla"),

        // Central & Uva Towns
        "පේරාදෙණිය" to listOf("peradeniya"),
        "කටුගස්තොට" to listOf("katugastota"),
        "ගම්පොල" to listOf("gampola"),
        "නාවලපිටිය" to listOf("nawalapitiya"),
        "තෙල්දෙණිය" to listOf("teldeniya"),
        "හසලක" to listOf("hasalaka"),
        "දඹුල්ල" to listOf("dambulla"),
        "සීගිරිය" to listOf("sigiriya"),
        "ගලේවෙල" to listOf("galewela"),
        "නාවුල" to listOf("naula"),
        "හැටන්" to listOf("hatton"),
        "ஹற்றன்" to listOf("hatton"),
        "තලවාකැලේ" to listOf("talawakele"),
        "මස්කෙළිය" to listOf("maskeliya"),
        "බණ්ඩාරවෙල" to listOf("bandarawela"),
        "பண்டாரவளை" to listOf("bandarawela"),
        "හපුතලේ" to listOf("haputale"),
        "දියතලාව" to listOf("diyatalawa"),
        "වැලිමඩ" to listOf("welimada"),
        "මහියංගනය" to listOf("mahiyanganaya"),
        "පස්සර" to listOf("passara"),
        "ඇල්ල" to listOf("ella"),
        "වැල්ලවාය" to listOf("wellawaya"),
        "බුත්තල" to listOf("buttala"),
        "බිබිල" to listOf("bibile"),

        // North Western & North Central Towns
        "කුලියාපිටිය" to listOf("kuliyapitiya"),
        "හලාවත" to listOf("chilaw"),
        "சிலாபம்" to listOf("chilaw"),
        "වෙන්නප්පුව" to listOf("wennappuwa"),
        "මාරවිල" to listOf("marawila"),
        "දංකොටුව" to listOf("dankotuwa"),
        "නාත්තන්ඩිය" to listOf("nattandiya"),
        "ආනමඩුව" to listOf("anamaduwa"),
        "මහව" to listOf("maho", "mahawa"),
        "නාරම්මල" to listOf("narammala"),
        "වාරියපොල" to listOf("wariyapola"),
        "ගිරිඋල්ල" to listOf("giriulla"),
        "අලව්ව" to listOf("alawwa"),
        "පොල්ගහවෙල" to listOf("polgahawela"),
        "මාවතගම" to listOf("mawathagama"),
        "නිකවැරටිය" to listOf("nikaweratiya"),
        "ගල්ගමුව" to listOf("galgamuwa"),
        "කැකිරාව" to listOf("kekirawa"),
        "මැදවච්චිය" to listOf("medawachchiya"),
        "තඹුත්තේගම" to listOf("tambuttegama"),
        "එප්පාවල" to listOf("eppawala"),
        "නොච්චියාගම" to listOf("nochchiyagama"),
        "මිහින්තලේ" to listOf("mihintale"),
        "හබරණ" to listOf("habarana"),
        "හිඟුරක්ගොඩ" to listOf("hingurakgoda"),
        "මැදිරිගිරිය" to listOf("medirigiriya"),
        "මින්නේරිය" to listOf("minneriya"),

        // Sabaragamuwa Towns
        "ඇඹිලිපිටිය" to listOf("embilipitiya"),
        "බලංගොඩ" to listOf("balangoda"),
        "පැල්මඩුල්ල" to listOf("pelmadulla"),
        "කුරුවිට" to listOf("kuruwita"),
        "ඇහැලියගොඩ" to listOf("eheliyagoda"),
        "කලවාන" to listOf("kalawana"),
        "මාවනැල්ල" to listOf("mawanella"),
        "වරකාපොල" to listOf("warakapola"),
        "වරකපොල" to listOf("warakapola"),
        "රුවන්වැල්ල" to listOf("ruwanwella"),
        "යටියන්තොට" to listOf("yatiyantota"),
        "දැරණියගල" to listOf("deraniyagala"),
        "කිතුල්ගල" to listOf("kitulgala"),

        // Northern & Eastern Towns
        "චාවකච්චේරි" to listOf("chavakachcheri"),
        "சாவகச்சேரி" to listOf("chavakachcheri"),
        "චුන්නාකම්" to listOf("chunnakam"),
        "சுன்னாகம்" to listOf("chunnakam"),
        "පේදුරුතුඩුව" to listOf("point pedro"),
        "பருத்தித்துறை" to listOf("point pedro"),
        "කන්කසන්තුරේ" to listOf("kankesanthurai"),
        "காங்கேசன்துறை" to listOf("kankesanthurai"),
        "නල්ලූර්" to listOf("nallur"),
        "நல்லூர்" to listOf("nallur"),
        "අලිමංකඩ" to listOf("elephant pass"),
        "ஆனையிறவு" to listOf("elephant pass"),
        "අක්කරපත්තුව" to listOf("akkaraipattu"),
        "அக்கரைப்பற்று" to listOf("akkaraipattu"),
        "කල්මුණේ" to listOf("kalmunai"),
        "கல்முனை" to listOf("kalmunai"),
        "සම්මන්තුරේ" to listOf("sammanthurai"),
        "පොතුවිල්" to listOf("pottuvil"),
        "பொத்துவில்" to listOf("pottuvil"),
        "කාත්තන්කුඩි" to listOf("kattankudy"),
        "காத்தான்குடி" to listOf("kattankudy"),
        "එරාවුර්" to listOf("eravur"),
        "වාලච්චේන" to listOf("valachchenai"),
        "කින්නියා" to listOf("kinniya"),
        "கிண்ணியா" to listOf("kinniya"),
        "මුතූර්" to listOf("mutur"),
        "மூதூர்" to listOf("mutur"),
        "කන්තලේ" to listOf("kantale"),
        "கந்தளாய்" to listOf("kantale"),

        // Police Ranks, Units, Designations & Emergency Terms
        "පොලිස්" to listOf("police"),
        "පොලිසිය" to listOf("police", "station"),
        "பொலிஸ்" to listOf("police"),
        "நிலையம்" to listOf("station"),
        "ස්ථානාධිපති" to listOf("oic", "officer in charge", "hqi"),
        "ස්ථානාධිපතිතුමා" to listOf("oic", "officer in charge"),
        "பொறுப்பதிகாரி" to listOf("oic"),
        "රථවාහන" to listOf("traffic"),
        "රථ වාහන" to listOf("traffic"),
        "ගමනාගමන" to listOf("traffic"),
        "போக்குவரத்து" to listOf("traffic"),
        "අපරාධ" to listOf("crime"),
        "අපරාද" to listOf("crime"),
        "குற்றம்" to listOf("crime"),
        "දුෂණ" to listOf("vice"),
        "දූෂණ" to listOf("vice"),
        "ප්‍රජා" to listOf("community"),
        "ප්‍රජා පොලිස්" to listOf("community"),
        "මූලස්ථාන" to listOf("hq", "headquarters"),
        "ප්‍රධාන" to listOf("hq", "chief", "head"),
        "தலைமையகம்" to listOf("headquarters", "hq"),
        "කොට්ඨාස" to listOf("division"),
        "කොට්ඨාසය" to listOf("division"),
        "පிரிவு" to listOf("division"),
        "කලාප" to listOf("range"),
        "කලාපය" to listOf("range"),
        "පොලිස්පති" to listOf("igp", "inspector general"),
        "නියෝජ්‍ය පොලිස්පති" to listOf("dig", "deputy inspector general"),
        "ජ්‍යෙෂ්ඨ පොලිස් අධිකාරී" to listOf("ssp", "senior superintendent"),
        "පොලිස් අධිකාරී" to listOf("sp", "superintendent"),
        "සහකාර පොලිස් අධිකාරී" to listOf("asp", "assistant superintendent"),
        "ප්‍රධාන පොලිස් පරීක්ෂක" to listOf("ci", "chief inspector", "hqi"),
        "පොලිස් පරීක්ෂක" to listOf("ip", "inspector"),
        "උප පොලිස් පරීක්ෂක" to listOf("si", "sub inspector"),
        "සැරයන්" to listOf("sergeant", "ps"),
        "කොස්තාපල්" to listOf("constable", "pc"),
        "කාන්තා" to listOf("women"),
        "ළමා" to listOf("children"),
        "කාන්තා හා ළමා" to listOf("women & children", "children"),
        "හදිසි" to listOf("emergency", "119"),
        "අනතුරු" to listOf("emergency", "accident"),
        "அவசரம்" to listOf("emergency"),
        "ගිනි" to listOf("fire", "110"),
        "ගිනි නිවන" to listOf("fire"),
        "தீயணைப்பு" to listOf("fire"),
        "රෝහල" to listOf("hospital"),
        "රෝහල්" to listOf("hospital"),
        "வைத்தியசாலை" to listOf("hospital"),
        "ගිලන්රථ" to listOf("ambulance", "1990"),
        "ගිලන් රථ" to listOf("ambulance", "1990"),
        "ආරෝග්‍යශාලා" to listOf("hospital"),
        "සංචාරක" to listOf("tourist"),
        "විශේෂ කාර්ය" to listOf("stf", "special task force"),
        "විශේෂ කාර්ය බලකාය" to listOf("stf"),
        "රහස් පොලිසිය" to listOf("cid"),
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
        val sb = StringBuilder()
        var i = 0
        val len = input.length

        while (i < len) {
            val c = input[i]
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

                // Consonants (mapped with default 'a' unless modified by next character)
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

                    // Check if followed by vowel sign or hal
                    val next = if (i + 1 < len) input[i + 1] else null
                    var consumedNext = false

                    if (next != null) {
                        when (next) {
                            '්' -> { // Hal mark / virama: suppress inherent vowel
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
                                // Inherent vowel 'a' if next is another consonant or word end
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
        val sb = StringBuilder()
        var i = 0
        val len = input.length

        while (i < len) {
            val c = input[i]
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

                    val next = if (i + 1 < len) input[i + 1] else null
                    var consumedNext = false

                    if (next != null) {
                        when (next) {
                            '்' -> { // Pulli: suppresses inherent vowel
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

    /**
     * Resolves a raw search query (which may be in Sinhala, Tamil, or English)
     * into a comprehensive list of English search tokens and candidate terms.
     */
    fun extractSearchTokens(rawQuery: String): List<List<String>> {
        val clean = rawQuery.trim().lowercase()
        if (clean.isBlank()) return emptyList()

        // 1. Direct dictionary full-phrase match check
        val fullDictMatch = DICTIONARY[clean]
        if (!fullDictMatch.isNullOrEmpty()) {
            return listOf(fullDictMatch)
        }

        // 2. Tokenize by whitespace
        val words = clean.split(Regex("\\s+")).filter { it.isNotBlank() }
        val resultTokens = mutableListOf<List<String>>()

        for (word in words) {
            val tokenCandidates = mutableSetOf<String>()

            // (a) Original word
            tokenCandidates.add(word)

            // (b) Dictionary lookup for this word
            DICTIONARY[word]?.let {
                tokenCandidates.addAll(it)
            }

            // Check partial dictionary prefixes or suffixes
            for ((key, englishList) in DICTIONARY) {
                if (word.contains(key) || key.contains(word)) {
                    tokenCandidates.addAll(englishList)
                }
            }

            // (c) Algorithmic transliteration if word is Sinhala or Tamil
            if (isSinhala(word)) {
                val trans = transliterateSinhalaToEnglish(word)
                if (trans.isNotBlank()) {
                    tokenCandidates.add(trans)
                    // Common Sinhala transliteration variants
                    tokenCandidates.add(trans.replace("w", "v"))
                    tokenCandidates.add(trans.replace("v", "w"))
                    tokenCandidates.add(trans.replace("th", "t"))
                    tokenCandidates.add(trans.replace("ee", "i"))
                    tokenCandidates.add(trans.replace("oo", "u"))
                }
            } else if (isTamil(word)) {
                val trans = transliterateTamilToEnglish(word)
                if (trans.isNotBlank()) {
                    tokenCandidates.add(trans)
                    tokenCandidates.add(trans.replace("v", "w"))
                    tokenCandidates.add(trans.replace("w", "v"))
                    tokenCandidates.add(trans.replace("th", "t"))
                    tokenCandidates.add(trans.replace("ee", "i"))
                    tokenCandidates.add(trans.replace("oo", "u"))
                }
            }

            resultTokens.add(tokenCandidates.toList())
        }

        return resultTokens
    }

    /**
     * Checks if a PoliceContact matches the given search query across all fields,
     * supporting Sinhala, Tamil, and English inputs.
     */
    fun matchesContact(contact: PoliceContact, rawQuery: String): Boolean {
        val q = rawQuery.trim().lowercase()
        if (q.isBlank()) return true

        // Build a searchable corpus from all contact fields
        val corpus = buildString {
            append(contact.stationOrDesignation.lowercase()).append(" ")
            append(contact.officerName.lowercase()).append(" ")
            append(contact.rank.lowercase()).append(" ")
            append(contact.locationAddress.lowercase()).append(" ")
            append(contact.generalPhone.lowercase()).append(" ")
            append(contact.mobilePhone.lowercase()).append(" ")
            append(contact.pvtNumber.lowercase()).append(" ")
            append(contact.officePhone2.lowercase()).append(" ")
            append(contact.officePhone3.lowercase()).append(" ")
            append(contact.fax.lowercase()).append(" ")
            append(contact.email.lowercase()).append(" ")
            append(contact.oicTraffic.lowercase()).append(" ")
            append(contact.oicCrime.lowercase()).append(" ")
            append(contact.oicVice.lowercase()).append(" ")
            append(contact.oicCommunityPolicing.lowercase())
        }

        // Fast-path: Direct substring match in corpus
        if (corpus.contains(q)) return true

        // Normalized search: check if corpus matches all query tokens
        val tokenGroups = extractSearchTokens(q)
        if (tokenGroups.isEmpty()) return true

        // Every token in the query must have at least one matching candidate in the corpus
        return tokenGroups.all { candidateList ->
            candidateList.any { candidate ->
                if (candidate.isBlank()) return@any false
                corpus.contains(candidate) ||
                        fuzzyMatch(corpus, candidate)
            }
        }
    }

    /**
     * Fuzzy phonetic match comparing word boundaries and phonetic variations
     */
    private fun fuzzyMatch(corpus: String, candidate: String): Boolean {
        if (candidate.length < 3) return false
        if (corpus.contains(candidate)) return true

        // Normalize w/v, double consonants, th/t
        val normalizedCandidate = normalizePhonetic(candidate)
        val words = corpus.split(Regex("[^a-zA-Z0-9]+"))

        for (word in words) {
            if (word.length >= 3) {
                val normalizedWord = normalizePhonetic(word)
                if (normalizedWord.contains(normalizedCandidate) || normalizedCandidate.contains(normalizedWord)) {
                    return true
                }
            }
        }
        return false
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
                if (candidate.isBlank()) return@any false
                corpus.contains(candidate) || fuzzyMatch(corpus, candidate)
            }
        }
    }

    private fun normalizePhonetic(str: String): String {
        return str.lowercase()
            .replace("w", "v")
            .replace("th", "t")
            .replace("ph", "f")
            .replace("c", "k")
            .replace("ee", "i")
            .replace("oo", "u")
            .replace("ll", "l")
            .replace("tt", "t")
            .replace("pp", "p")
            .replace("kk", "k")
            .replace("dd", "d")
            .replace("mm", "m")
            .replace("nn", "n")
    }
}
