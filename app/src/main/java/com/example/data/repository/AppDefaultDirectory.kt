package com.example.data.repository

import com.example.data.model.ContactCategory
import com.example.data.model.PoliceContact

object AppDefaultDirectory {

    fun getAllDefaultContacts(): List<PoliceContact> {
        return getEmergencyAndGovtContacts() +
                getRailwayContacts() +
                getPostOfficeContacts() +
                getSchoolContacts() +
                getBusStandContacts() +
                getPradeshiyaLekamAndNagaraSabhaContacts()
    }

    fun getRailwayContacts(): List<PoliceContact> {
        return listOf(
            // Railway Hotlines & Head Office
            PoliceContact(
                id = "rail_1971",
                stationOrDesignation = "1971 Sri Lanka Railways Inquiry Hotline",
                rank = "HOTLINE (24/7)",
                officerName = "දුම්රිය තොරතුරු විමසීම් ක්ෂණික ඇමතුම් අංකය",
                generalPhone = "1971",
                mobilePhone = "1971",
                officePhone2 = "011-2434215",
                locationAddress = "Sri Lanka Railways Head Office, Olcott Mawatha, Colombo 10",
                category = ContactCategory.RAILWAY
            ),
            PoliceContact(
                id = "rail_hq",
                stationOrDesignation = "Railways Department Head Office (General)",
                rank = "HEAD OFFICE",
                officerName = "General Manager Railways (GMR)",
                generalPhone = "011-2434215",
                officePhone2 = "011-2432128",
                officePhone3 = "011-2431909",
                locationAddress = "Olcott Mawatha, Colombo 10",
                category = ContactCategory.RAILWAY
            ),
            PoliceContact(
                id = "rail_ctc",
                stationOrDesignation = "Central Train Control Office (Maradana)",
                rank = "TRAIN CONTROL",
                officerName = "Train Operations & Control Room",
                generalPhone = "011-2434215",
                officePhone2 = "011-2431909",
                locationAddress = "Maradana Railway Complex, Colombo 10",
                category = ContactCategory.RAILWAY
            ),
            PoliceContact(
                id = "rail_booking",
                stationOrDesignation = "Railway Ticket Reservation & Seat Booking (Mobitel/SLT)",
                rank = "TICKET BOOKING",
                officerName = "M-Ticketing / Train Seat Reservation",
                generalPhone = "365",
                officePhone2 = "1971",
                locationAddress = "Island-wide Railway Booking System",
                category = ContactCategory.RAILWAY
            ),

            // Major Railway Stations - Colombo & Western Province
            PoliceContact(id = "rail_fort", stationOrDesignation = "Colombo Fort Railway Station", rank = "MAIN STATION", officerName = "Station Master / Inquiries", generalPhone = "011-2434215", officePhone2 = "011-2432128", locationAddress = "Olcott Mawatha, Colombo Fort", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_maradana", stationOrDesignation = "Maradana Railway Station", rank = "JUNCTION STATION", officerName = "Station Master / Inquiries", generalPhone = "011-2692287", officePhone2 = "011-2434215", locationAddress = "Maradana Road, Colombo 10", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_dematagoda", stationOrDesignation = "Dematagoda Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "011-2694215", locationAddress = "Baseline Road, Dematagoda", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_kelaniya", stationOrDesignation = "Kelaniya Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "011-2911271", locationAddress = "Kelaniya, Gampaha", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_ragama", stationOrDesignation = "Ragama Railway Junction", rank = "JUNCTION STATION", officerName = "Station Master / Main Inquiries", generalPhone = "011-2958271", officePhone2 = "011-2958215", locationAddress = "Ragama Junction, Gampaha", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_gampaha", stationOrDesignation = "Gampaha Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "033-2222271", locationAddress = "Station Road, Gampaha", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_veyangoda", stationOrDesignation = "Veyangoda Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "033-2287271", locationAddress = "Veyangoda, Gampaha", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_mirigama", stationOrDesignation = "Mirigama Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "033-2273271", locationAddress = "Mirigama, Gampaha", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_jaela", stationOrDesignation = "Ja-Ela Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "011-2236271", locationAddress = "Station Road, Ja-Ela", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_negombo", stationOrDesignation = "Negombo Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "031-2222271", locationAddress = "Station Road, Negombo", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_katunayake", stationOrDesignation = "Katunayake South (Airport) Railway Station", rank = "AIRPORT LINK", officerName = "Station Master", generalPhone = "011-2252271", locationAddress = "Katunayake, Airport Access Road", category = ContactCategory.RAILWAY),

            // Coastal Line (මුහුදුබඩ මාර්ගය)
            PoliceContact(id = "rail_kollupitiya", stationOrDesignation = "Kollupitiya Railway Station", rank = "COASTAL STATION", officerName = "Station Master", generalPhone = "011-2324215", locationAddress = "Marine Drive, Kollupitiya, Colombo 03", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_bambalapitiya", stationOrDesignation = "Bambalapitiya Railway Station", rank = "COASTAL STATION", officerName = "Station Master", generalPhone = "011-2588215", locationAddress = "Marine Drive, Bambalapitiya, Colombo 04", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_wellawatte", stationOrDesignation = "Wellawatte Railway Station", rank = "COASTAL STATION", officerName = "Station Master", generalPhone = "011-2583271", locationAddress = "Marine Drive, Wellawatte, Colombo 06", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_dehiwala", stationOrDesignation = "Dehiwala Railway Station", rank = "COASTAL STATION", officerName = "Station Master", generalPhone = "011-2717271", locationAddress = "Station Road, Dehiwala", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_mount_lavinia", stationOrDesignation = "Mount Lavinia Railway Station", rank = "COASTAL STATION", officerName = "Station Master", generalPhone = "011-2712271", locationAddress = "Hotel Road, Mount Lavinia", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_ratmalana", stationOrDesignation = "Ratmalana Railway Station & Workshops", rank = "WORKSHOPS STATION", officerName = "Chief Mechanical Engineer / Station Master", generalPhone = "011-2636271", locationAddress = "Station Road, Ratmalana", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_moratuwa", stationOrDesignation = "Moratuwa Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "011-2645271", locationAddress = "Station Road, Moratuwa", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_panadura", stationOrDesignation = "Panadura Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "038-2232271", locationAddress = "Station Road, Panadura", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_wadduwa", stationOrDesignation = "Wadduwa Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "038-2232571", locationAddress = "Galle Road, Wadduwa", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_kalutara_north", stationOrDesignation = "Kalutara North Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "034-2222272", locationAddress = "Main Road, Kalutara North", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_kalutara_south", stationOrDesignation = "Kalutara South Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "034-2222271", locationAddress = "Station Road, Kalutara South", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_beruwala", stationOrDesignation = "Beruwala Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "034-2276271", locationAddress = "Beruwala, Kalutara", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_aluthgama", stationOrDesignation = "Aluthgama Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "034-2275271", locationAddress = "Station Road, Aluthgama", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_bentota", stationOrDesignation = "Bentota Railway Station", rank = "TOURIST STATION", officerName = "Station Master", generalPhone = "034-2275171", locationAddress = "Bentota National Holiday Resort", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_ambalangoda", stationOrDesignation = "Ambalangoda Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "091-2258271", locationAddress = "Station Road, Ambalangoda", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_hikkaduwa", stationOrDesignation = "Hikkaduwa Railway Station", rank = "TOURIST STATION", officerName = "Station Master", generalPhone = "091-2277271", locationAddress = "Galle Road, Hikkaduwa", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_galle", stationOrDesignation = "Galle Railway Station", rank = "MAIN TERMINAL", officerName = "Station Master / Inquiries", generalPhone = "091-2234958", officePhone2 = "091-2234215", locationAddress = "Colombo Road, Galle", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_koggala", stationOrDesignation = "Koggala Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "091-2283271", locationAddress = "Koggala Export Zone Road", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_weligama", stationOrDesignation = "Weligama Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "041-2250271", locationAddress = "Station Road, Weligama", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_matara", stationOrDesignation = "Matara Railway Station", rank = "MAIN TERMINAL", officerName = "Station Master / Inquiries", generalPhone = "041-2222275", officePhone2 = "041-2222271", locationAddress = "Station Road, Matara", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_beliatta", stationOrDesignation = "Beliatta Railway Station (Southern Extension)", rank = "TERMINAL STATION", officerName = "Station Master / Inquiries", generalPhone = "041-2244271", locationAddress = "New Railway Extension, Beliatta", category = ContactCategory.RAILWAY),

            // Main Line & Hill Country (ප්‍රධාන කඳුකර මාර්ගය)
            PoliceContact(id = "rail_alawwa", stationOrDesignation = "Alawwa Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "037-2278271", locationAddress = "Alawwa, Kurunegala", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_polgahawela", stationOrDesignation = "Polgahawela Railway Junction", rank = "MAJOR JUNCTION", officerName = "Station Master / Train Inquiries", generalPhone = "037-2244271", officePhone2 = "037-2244215", locationAddress = "Polgahawela Junction", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_rambukkana", stationOrDesignation = "Rambukkana Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "035-2265271", locationAddress = "Rambukkana, Kegalle", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_kadugannawa", stationOrDesignation = "Kadugannawa Railway Station", rank = "HILL STATION", officerName = "Station Master", generalPhone = "081-2571271", locationAddress = "Kadugannawa Pass, Kandy", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_peradeniya", stationOrDesignation = "Peradeniya Railway Junction", rank = "MAJOR JUNCTION", officerName = "Station Master", generalPhone = "081-2388271", locationAddress = "Peradeniya Junction, Kandy", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_kandy", stationOrDesignation = "Kandy Railway Station", rank = "MAIN TERMINAL", officerName = "Station Master / Inquiries", generalPhone = "081-2222271", officePhone2 = "081-2222215", locationAddress = "Station Road, Kandy", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_matale", stationOrDesignation = "Matale Railway Station", rank = "TERMINAL STATION", officerName = "Station Master", generalPhone = "066-2222271", locationAddress = "Station Road, Matale", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_gampola", stationOrDesignation = "Gampola Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "081-2352271", locationAddress = "Station Road, Gampola", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_nawalapitiya", stationOrDesignation = "Nawalapitiya Railway Station", rank = "DIVISIONAL HQ", officerName = "Divisional Transportation Superintendent", generalPhone = "054-2222271", officePhone2 = "054-2222215", locationAddress = "Nawalapitiya Railway Division", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_hatton", stationOrDesignation = "Hatton Railway Station (Adam's Peak Link)", rank = "MAIN HILL STATION", officerName = "Station Master / Inquiries", generalPhone = "051-2222271", locationAddress = "Station Road, Hatton", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_talawakele", stationOrDesignation = "Talawakele Railway Station", rank = "HILL STATION", officerName = "Station Master", generalPhone = "051-2258271", locationAddress = "Talawakele, Nuwara Eliya", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_nanuoya", stationOrDesignation = "Nanu Oya Railway Station (Nuwara Eliya Link)", rank = "MAIN HILL STATION", officerName = "Station Master / Inquiries", generalPhone = "052-2236271", locationAddress = "Nanu Oya, Nuwara Eliya", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_ambewela", stationOrDesignation = "Ambewela Railway Station", rank = "HIGHLAND STATION", officerName = "Station Master", generalPhone = "052-2236272", locationAddress = "Ambewela Farm Road", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_pattipola", stationOrDesignation = "Pattipola Railway Station (Highest Station in SL)", rank = "HIGHEST SUMMIT (1,897m)", officerName = "Station Master", generalPhone = "052-2222272", locationAddress = "Pattipola, Horton Plains Link", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_ohiya", stationOrDesignation = "Ohiya Railway Station", rank = "HILL STATION", officerName = "Station Master", generalPhone = "057-2229272", locationAddress = "Ohiya, Badulla", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_haputale", stationOrDesignation = "Haputale Railway Station", rank = "HILL STATION", officerName = "Station Master", generalPhone = "057-2268271", locationAddress = "Haputale Town, Badulla", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_diyatalawa", stationOrDesignation = "Diyatalawa Railway Station", rank = "GARRISON STATION", officerName = "Station Master", generalPhone = "057-2229271", locationAddress = "Diyatalawa, Badulla", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_bandarawela", stationOrDesignation = "Bandarawela Railway Station", rank = "MAIN HILL STATION", officerName = "Station Master", generalPhone = "057-2222271", locationAddress = "Main Street, Bandarawela", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_ella", stationOrDesignation = "Ella Railway Station (Nine Arch Bridge Link)", rank = "MAJOR TOURIST STATION", officerName = "Station Master / Inquiries", generalPhone = "057-2228571", locationAddress = "Ella Town, Badulla", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_demodara", stationOrDesignation = "Demodara Railway Station (Loop)", rank = "SPIRAL LOOP STATION", officerName = "Station Master", generalPhone = "057-2228572", locationAddress = "Demodara, Badulla", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_badulla", stationOrDesignation = "Badulla Railway Station", rank = "MAIN TERMINAL", officerName = "Station Master / Inquiries", generalPhone = "055-2222271", officePhone2 = "055-2222215", locationAddress = "Station Road, Badulla", category = ContactCategory.RAILWAY),

            // Northern Line & Mannar (උතුරු මාර්ගය)
            PoliceContact(id = "rail_kurunegala", stationOrDesignation = "Kurunegala Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "037-2222271", locationAddress = "Station Road, Kurunegala", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_maho", stationOrDesignation = "Maho Railway Junction", rank = "MAJOR JUNCTION", officerName = "Station Master", generalPhone = "037-2275271", locationAddress = "Maho Junction, Kurunegala", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_anuradhapura", stationOrDesignation = "Anuradhapura Railway Station", rank = "MAIN TERMINAL", officerName = "Station Master / Inquiries", generalPhone = "025-2222271", officePhone2 = "025-2222215", locationAddress = "Maithripala Senanayake Mawatha, Anuradhapura", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_anuradhapura_new", stationOrDesignation = "Anuradhapura New Town Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "025-2222371", locationAddress = "New Town, Anuradhapura", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_medawachchiya", stationOrDesignation = "Medawachchiya Railway Junction", rank = "JUNCTION STATION", officerName = "Station Master", generalPhone = "025-2245271", locationAddress = "Medawachchiya Junction", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_vavuniya", stationOrDesignation = "Vavuniya Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "024-2222271", locationAddress = "Station Road, Vavuniya", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_mankulam", stationOrDesignation = "Mankulam Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "021-2285272", locationAddress = "A9 Road, Mankulam", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_kilinochchi", stationOrDesignation = "Kilinochchi Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "021-2285271", locationAddress = "Station Road, Kilinochchi", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_pallai", stationOrDesignation = "Pallai Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "021-2285371", locationAddress = "Pallai, Kilinochchi", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_jaffna", stationOrDesignation = "Jaffna Railway Station", rank = "MAIN TERMINAL", officerName = "Station Master / Inquiries", generalPhone = "021-2222271", officePhone2 = "021-2222215", locationAddress = "Station Road, Jaffna", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_kks", stationOrDesignation = "Kankesanthurai (KKS) Railway Station", rank = "TERMINAL STATION", officerName = "Station Master", generalPhone = "021-2222471", locationAddress = "Kankesanthurai Port Road", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_mannar", stationOrDesignation = "Mannar Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "023-2222271", locationAddress = "Station Road, Mannar", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_talaimannar", stationOrDesignation = "Talaimannar Pier Railway Station", rank = "PIER TERMINAL", officerName = "Station Master", generalPhone = "023-2222371", locationAddress = "Talaimannar Pier", category = ContactCategory.RAILWAY),

            // Eastern & North Central (නැගෙනහිර හා පොලොන්නරුව මාර්ගය)
            PoliceContact(id = "rail_habarana", stationOrDesignation = "Habarana Railway Station", rank = "CROSSROADS STATION", officerName = "Station Master", generalPhone = "066-2270271", locationAddress = "Habarana, Anuradhapura Road", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_galoya", stationOrDesignation = "Galoya Railway Junction", rank = "JUNCTION STATION", officerName = "Station Master", generalPhone = "027-2246271", locationAddress = "Galoya Junction, Polonnaruwa", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_polonnaruwa", stationOrDesignation = "Polonnaruwa Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "027-2222271", locationAddress = "Kaduruwela, Polonnaruwa", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_hingurakgoda", stationOrDesignation = "Hingurakgoda Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "027-2246272", locationAddress = "Hingurakgoda Town", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_valachchenai", stationOrDesignation = "Valachchenai Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "065-2257271", locationAddress = "Valachchenai, Batticaloa", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_batticaloa", stationOrDesignation = "Batticaloa Railway Station", rank = "MAIN TERMINAL", officerName = "Station Master / Inquiries", generalPhone = "065-2222271", locationAddress = "Station Road, Batticaloa", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_kantale", stationOrDesignation = "Kantale Railway Station", rank = "SUB STATION", officerName = "Station Master", generalPhone = "026-2240271", locationAddress = "Kantale, Trincomalee", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_trincomalee", stationOrDesignation = "Trincomalee Railway Station", rank = "MAIN TERMINAL", officerName = "Station Master / Inquiries", generalPhone = "026-2222271", locationAddress = "Inner Harbour Road, Trincomalee", category = ContactCategory.RAILWAY),

            // Puttalam Line (පුත්තලම මාර්ගය)
            PoliceContact(id = "rail_chilaw", stationOrDesignation = "Chilaw Railway Station", rank = "MAIN STATION", officerName = "Station Master", generalPhone = "032-2222271", locationAddress = "Station Road, Chilaw", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_puttalam", stationOrDesignation = "Puttalam Railway Station", rank = "MAIN TERMINAL", officerName = "Station Master", generalPhone = "032-2265271", locationAddress = "Station Road, Puttalam", category = ContactCategory.RAILWAY),

            // Kelani Valley Line (කැළණි වැලි මාර්ගය)
            PoliceContact(id = "rail_nugegoda", stationOrDesignation = "Nugegoda Railway Station", rank = "COMMUTER STATION", officerName = "Station Master", generalPhone = "011-2822271", locationAddress = "Station Road, Nugegoda", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_maharagama", stationOrDesignation = "Maharagama Railway Station", rank = "COMMUTER STATION", officerName = "Station Master", generalPhone = "011-2850271", locationAddress = "Railway Station Road, Maharagama", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_homagama", stationOrDesignation = "Homagama Railway Station", rank = "COMMUTER STATION", officerName = "Station Master", generalPhone = "011-2855271", locationAddress = "Homagama Town", category = ContactCategory.RAILWAY),
            PoliceContact(id = "rail_avissawella", stationOrDesignation = "Avissawella Railway Station", rank = "KV TERMINAL", officerName = "Station Master", generalPhone = "036-2222271", locationAddress = "Station Road, Avissawella", category = ContactCategory.RAILWAY)
        )
    }

    fun getPostOfficeContacts(): List<PoliceContact> {
        return listOf(
            // Postal Short Code & Head Office
            PoliceContact(
                id = "post_1950",
                stationOrDesignation = "1950 Sri Lanka Postal Department Inquiry Hotline",
                rank = "HOTLINE (24/7)",
                officerName = "ශ්‍රී ලංකා තැපැල් දෙපාර්තමේන්තුව ක්ෂණික ඇමතුම්",
                generalPhone = "1950",
                mobilePhone = "1950",
                officePhone2 = "011-2328301",
                locationAddress = "Postal Headquarters, D.R. Wijewardena Mawatha, Colombo 10",
                category = ContactCategory.POST_OFFICE
            ),
            PoliceContact(
                id = "post_gpo",
                stationOrDesignation = "General Post Office Colombo (GPO)",
                rank = "GENERAL POST OFFICE",
                officerName = "Postmaster General / Public Inquiry",
                generalPhone = "011-2326203",
                officePhone2 = "011-2447844",
                officePhone3 = "011-2498800",
                locationAddress = "D.R. Wijewardena Mawatha, Colombo 10",
                category = ContactCategory.POST_OFFICE
            ),
            PoliceContact(
                id = "post_cme",
                stationOrDesignation = "Central Mail Exchange (CME Colombo)",
                rank = "CENTRAL MAIL EXCHANGE",
                officerName = "Parcel Post / Foreign Mail Exchange",
                generalPhone = "011-2328301",
                officePhone2 = "011-2447844",
                locationAddress = "D.R. Wijewardena Mawatha, Colombo 10",
                category = ContactCategory.POST_OFFICE
            ),
            PoliceContact(
                id = "post_ems",
                stationOrDesignation = "Speed Post (EMS Courier Service Sri Lanka)",
                rank = "EXPRESS COURIER",
                officerName = "EMS Customer Service & Tracking",
                generalPhone = "011-2328301",
                officePhone2 = "1950",
                locationAddress = "Central Mail Exchange, Colombo 10",
                category = ContactCategory.POST_OFFICE
            ),

            // District Main Post Offices (දිස්ත්‍රික් ප්‍රධාන තැපැල් කාර්යාල)
            PoliceContact(id = "post_kandy", stationOrDesignation = "Kandy General Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster / Counter Service", generalPhone = "081-2222200", officePhone2 = "081-2222288", locationAddress = "Dalada Veediya, Kandy", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_galle", stationOrDesignation = "Galle Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "091-2234288", locationAddress = "Main Street, Galle Fort", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_matara", stationOrDesignation = "Matara Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "041-2222288", locationAddress = "Anagarika Dharmapala Mawatha, Matara", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_kurunegala", stationOrDesignation = "Kurunegala Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "037-2222288", locationAddress = "Dambulla Road, Kurunegala", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_jaffna", stationOrDesignation = "Jaffna Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "021-2222288", locationAddress = "Hospital Road, Jaffna", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_anuradhapura", stationOrDesignation = "Anuradhapura Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "025-2222288", locationAddress = "Maithripala Senanayake Mawatha, Anuradhapura", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_badulla", stationOrDesignation = "Badulla Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "055-2222288", locationAddress = "Bazaar Street, Badulla", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_ratnapura", stationOrDesignation = "Ratnapura Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "045-2222288", locationAddress = "Main Street, Ratnapura", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_gampaha", stationOrDesignation = "Gampaha Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "033-2222288", locationAddress = "Bauddhaloka Mawatha, Gampaha", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_kalutara", stationOrDesignation = "Kalutara Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "034-2222288", locationAddress = "Galle Road, Kalutara South", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_nuwara_eliya", stationOrDesignation = "Nuwara Eliya Historic Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "052-2222288", locationAddress = "Queen Elizabeth Drive, Nuwara Eliya", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_trincomalee", stationOrDesignation = "Trincomalee Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "026-2222288", locationAddress = "Dockyard Road, Trincomalee", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_batticaloa", stationOrDesignation = "Batticaloa Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "065-2222288", locationAddress = "Central Road, Batticaloa", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_hambantota", stationOrDesignation = "Hambantota Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "047-2220288", locationAddress = "Main Street, Hambantota", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_kegalle", stationOrDesignation = "Kegalle Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "035-2222288", locationAddress = "Kandy Road, Kegalle", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_polonnaruwa", stationOrDesignation = "Polonnaruwa Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "027-2222288", locationAddress = "Old Town, Polonnaruwa", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_matale", stationOrDesignation = "Matale Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "066-2222288", locationAddress = "Trincomalee Street, Matale", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_monaragala", stationOrDesignation = "Monaragala Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "055-2276288", locationAddress = "Wellawaya Road, Monaragala", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_ampara", stationOrDesignation = "Ampara Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "063-2222288", locationAddress = "Inginiyagala Road, Ampara", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_puttalam", stationOrDesignation = "Puttalam Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "032-2265288", locationAddress = "Service Road, Puttalam", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_mannar", stationOrDesignation = "Mannar Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "023-2222288", locationAddress = "Main Street, Mannar", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_vavuniya", stationOrDesignation = "Vavuniya Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "024-2222288", locationAddress = "2nd Cross Street, Vavuniya", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_kilinochchi", stationOrDesignation = "Kilinochchi Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "021-2285288", locationAddress = "A9 Road, Kilinochchi", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_mullaitivu", stationOrDesignation = "Mullaitivu Main Post Office", rank = "MAIN POST OFFICE", officerName = "Postmaster", generalPhone = "021-2290288", locationAddress = "Beach Road, Mullaitivu", category = ContactCategory.POST_OFFICE),

            // Major Suburb & Town Post Offices
            PoliceContact(id = "post_negombo", stationOrDesignation = "Negombo Post Office", rank = "TOWN POST OFFICE", officerName = "Postmaster", generalPhone = "031-2222288", locationAddress = "Main Street, Negombo", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_panadura", stationOrDesignation = "Panadura Post Office", rank = "TOWN POST OFFICE", officerName = "Postmaster", generalPhone = "038-2232288", locationAddress = "Galle Road, Panadura", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_moratuwa", stationOrDesignation = "Moratuwa Post Office", rank = "SUBURB POST OFFICE", officerName = "Postmaster", generalPhone = "011-2645288", locationAddress = "Galle Road, Moratuwa", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_dehiwala", stationOrDesignation = "Dehiwala Post Office", rank = "SUBURB POST OFFICE", officerName = "Postmaster", generalPhone = "011-2715288", locationAddress = "Galle Road, Dehiwala", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_nugegoda", stationOrDesignation = "Nugegoda Post Office", rank = "SUBURB POST OFFICE", officerName = "Postmaster", generalPhone = "011-2852288", locationAddress = "Stanley Thilakaratne Mawatha, Nugegoda", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_maharagama", stationOrDesignation = "Maharagama Post Office", rank = "SUBURB POST OFFICE", officerName = "Postmaster", generalPhone = "011-2850288", locationAddress = "High Level Road, Maharagama", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_battaramulla", stationOrDesignation = "Battaramulla Post Office", rank = "ADMINISTRATIVE POST OFFICE", officerName = "Postmaster", generalPhone = "011-2862288", locationAddress = "Main Street, Battaramulla", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_homagama", stationOrDesignation = "Homagama Post Office", rank = "TOWN POST OFFICE", officerName = "Postmaster", generalPhone = "011-2855288", locationAddress = "High Level Road, Homagama", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_avissawella", stationOrDesignation = "Avissawella Post Office", rank = "TOWN POST OFFICE", officerName = "Postmaster", generalPhone = "036-2222288", locationAddress = "Ratnapura Road, Avissawella", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_bandarawela", stationOrDesignation = "Bandarawela Post Office", rank = "TOWN POST OFFICE", officerName = "Postmaster", generalPhone = "057-2222288", locationAddress = "Main Street, Bandarawela", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_hatton", stationOrDesignation = "Hatton Post Office", rank = "TOWN POST OFFICE", officerName = "Postmaster", generalPhone = "051-2222288", locationAddress = "Dunbar Road, Hatton", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_chilaw", stationOrDesignation = "Chilaw Post Office", rank = "TOWN POST OFFICE", officerName = "Postmaster", generalPhone = "032-2222288", locationAddress = "Kurunegala Road, Chilaw", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_dambulla", stationOrDesignation = "Dambulla Post Office", rank = "TOWN POST OFFICE", officerName = "Postmaster", generalPhone = "066-2284288", locationAddress = "Kandy Road, Dambulla", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_tangalle", stationOrDesignation = "Tangalle Post Office", rank = "TOWN POST OFFICE", officerName = "Postmaster", generalPhone = "047-2240288", locationAddress = "Galle Road, Tangalle", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_hikkaduwa", stationOrDesignation = "Hikkaduwa Post Office", rank = "TOWN POST OFFICE", officerName = "Postmaster", generalPhone = "091-2277288", locationAddress = "Galle Road, Hikkaduwa", category = ContactCategory.POST_OFFICE),
            PoliceContact(id = "post_aluthgama", stationOrDesignation = "Aluthgama Post Office", rank = "TOWN POST OFFICE", officerName = "Postmaster", generalPhone = "034-2275288", locationAddress = "Station Road, Aluthgama", category = ContactCategory.POST_OFFICE)
        )
    }

    fun getSchoolContacts(): List<PoliceContact> {
        return listOf(
            // Education Hotlines
            PoliceContact(
                id = "sch_1988",
                stationOrDesignation = "1988 Ministry of Education Public Hotline",
                rank = "HOTLINE (24/7)",
                officerName = "අධ්‍යාපන අමාත්‍යාංශය මහජන සම්බන්ධතා ක්ෂණික ඇමතුම්",
                generalPhone = "1988",
                mobilePhone = "1988",
                officePhone2 = "011-2785141",
                locationAddress = "Ministry of Education, Isurupaya, Battaramulla",
                category = ContactCategory.SCHOOLS
            ),
            PoliceContact(
                id = "sch_1911",
                stationOrDesignation = "1911 Department of Examinations Hotline",
                rank = "EXAMINATIONS HOTLINE",
                officerName = "විභාග දෙපාර්තමේන්තුව විමසීම් අංශය",
                generalPhone = "1911",
                officePhone2 = "011-2785211",
                officePhone3 = "011-2785212",
                locationAddress = "Department of Examinations, Pelawatta, Battaramulla",
                category = ContactCategory.SCHOOLS
            ),

            // Colombo District Schools
            PoliceContact(id = "sch_royal", stationOrDesignation = "Royal College Colombo", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "011-2691024", officePhone2 = "011-2691025", locationAddress = "Rajakeeya Mawatha, Colombo 07", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_ananda", stationOrDesignation = "Ananda College Colombo", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "011-2695503", officePhone2 = "011-2695504", locationAddress = "Kularatne Mawatha, Colombo 10", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_nalanda", stationOrDesignation = "Nalanda College Colombo", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "011-2692484", officePhone2 = "011-2695333", locationAddress = "Sri Dhamma Mawatha, Colombo 10", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_visakha", stationOrDesignation = "Visakha Vidyalaya Colombo", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "011-2581908", locationAddress = "Vajira Road, Colombo 04", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_devi", stationOrDesignation = "Devi Balika Vidyalaya Colombo", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "011-2692292", locationAddress = "Sri Jayawardenepura Mawatha, Colombo 08", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_ds", stationOrDesignation = "D.S. Senanayake College Colombo", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "011-2697858", locationAddress = "R.G. Senanayake Mawatha, Colombo 07", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_mahanama", stationOrDesignation = "Mahanama College Colombo", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "011-2575293", locationAddress = "R.A. De Mel Mawatha, Colombo 03", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_sirimavo", stationOrDesignation = "Sirimavo Bandaranaike Vidyalaya Colombo", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "011-2588147", locationAddress = "Stanmore Crescent, Colombo 07", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_joseph", stationOrDesignation = "St. Joseph's College Colombo", rank = "SEMI-GOVT SCHOOL", officerName = "Rector / Office", generalPhone = "011-2692794", locationAddress = "T.B. Jayah Mawatha, Colombo 10", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_peter", stationOrDesignation = "St. Peter's College Colombo", rank = "SEMI-GOVT SCHOOL", officerName = "Rector / Office", generalPhone = "011-2588242", locationAddress = "Galle Road, Colombo 04", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_thurstan", stationOrDesignation = "Thurstan College Colombo", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "011-2691768", locationAddress = "Kumaratunga Munidasa Mawatha, Colombo 07", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_isipathana", stationOrDesignation = "Isipathana College Colombo", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "011-2588246", locationAddress = "Isipathana Mawatha, Colombo 05", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_anula", stationOrDesignation = "Anula Vidyalaya Nugegoda", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "011-2821213", locationAddress = "High Level Road, Nugegoda", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_john", stationOrDesignation = "St. John's College Nugegoda", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "011-2822987", locationAddress = "Station Road, Nugegoda", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_dharmapala", stationOrDesignation = "Dharmapala Vidyalaya Pannipitiya", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "011-2840049", locationAddress = "High Level Road, Pannipitiya", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_zahira", stationOrDesignation = "Zahira College Colombo", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "011-2692242", locationAddress = "Orabi Pasha Street, Maradana, Colombo 10", category = ContactCategory.SCHOOLS),

            // Kandy & Central Province Schools
            PoliceContact(id = "sch_dharmaraja", stationOrDesignation = "Dharmaraja College Kandy", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "081-2223434", locationAddress = "Dharmaraja Hill, Kandy", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_kingswood", stationOrDesignation = "Kingswood College Kandy", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "081-2222384", locationAddress = "Peradeniya Road, Kandy", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_trinity", stationOrDesignation = "Trinity College Kandy", rank = "INDEPENDENT SCHOOL", officerName = "Principal / Office", generalPhone = "081-2222479", locationAddress = "Trinity College, Kandy", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_mahamaya", stationOrDesignation = "Mahamaya Girls' College Kandy", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "081-2223204", locationAddress = "Sanghamitta Mawatha, Kandy", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_anthony", stationOrDesignation = "St. Anthony's College Kandy", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "081-2492244", locationAddress = "Katugastota, Kandy", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_girls_high", stationOrDesignation = "Girls' High School Kandy", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "081-2222464", locationAddress = "Katukele, Kandy", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_pushpadana", stationOrDesignation = "Pushpadana Girls' College Kandy", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "081-2222359", locationAddress = "Ampitiya Road, Kandy", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_vidyartha", stationOrDesignation = "Vidyartha College Kandy", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "081-2223243", locationAddress = "Katugastota Road, Kandy", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_sylvester", stationOrDesignation = "St. Sylvester's College Kandy", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "081-2222343", locationAddress = "Lady Anderson Road, Kandy", category = ContactCategory.SCHOOLS),

            // Galle & Southern Province Schools
            PoliceContact(id = "sch_richmond", stationOrDesignation = "Richmond College Galle", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "091-2234259", locationAddress = "Richmond Hill, Galle", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_mahinda", stationOrDesignation = "Mahinda College Galle", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "091-2234257", locationAddress = "Elliot Road, Galle", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_southlands", stationOrDesignation = "Southlands College Galle", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "091-2234272", locationAddress = "Lighthouse Street, Galle Fort", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_sanghamitta", stationOrDesignation = "Sanghamitta Balika Vidyalaya Galle", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "091-2234256", locationAddress = "Wakwella Road, Galle", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_aloysius", stationOrDesignation = "St. Aloysius' College Galle", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "091-2234268", locationAddress = "Galle Fort", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_rahula", stationOrDesignation = "Rahula College Matara", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "041-2222262", locationAddress = "Rahula Road, Matara", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_sujatha", stationOrDesignation = "Sujatha Vidyalaya Matara", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "041-2222261", locationAddress = "Rahula Road, Matara", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_thomas_matara", stationOrDesignation = "St. Thomas' College Matara", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "041-2222265", locationAddress = "Kumaratunga Mawatha, Matara", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_dharmasoka", stationOrDesignation = "Dharmasoka College Ambalangoda", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "091-2258242", locationAddress = "Galle Road, Ambalangoda", category = ContactCategory.SCHOOLS),

            // Kurunegala & North Western Province Schools
            PoliceContact(id = "sch_maliyadeva", stationOrDesignation = "Maliyadeva College Kurunegala", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "037-2222434", locationAddress = "Negombo Road, Kurunegala", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_maliyadeva_balika", stationOrDesignation = "Maliyadeva Balika Vidyalaya Kurunegala", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "037-2222244", locationAddress = "Colombo Road, Kurunegala", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_anne", stationOrDesignation = "St. Anne's College Kurunegala", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "037-2222442", locationAddress = "St. Anne's Road, Kurunegala", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_holy_family", stationOrDesignation = "Holy Family Convent Kurunegala", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "037-2222452", locationAddress = "Colombo Road, Kurunegala", category = ContactCategory.SCHOOLS),

            // Jaffna & Northern Province Schools
            PoliceContact(id = "sch_jaffna_central", stationOrDesignation = "Jaffna Central College", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "021-2222242", locationAddress = "Central College Road, Jaffna", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_john_jaffna", stationOrDesignation = "St. John's College Jaffna", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "021-2222393", locationAddress = "Chundikuli, Jaffna", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_hartley", stationOrDesignation = "Hartley College Point Pedro", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "021-2263242", locationAddress = "College Road, Point Pedro", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_vembadi", stationOrDesignation = "Vembadi Girls' High School Jaffna", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "021-2222243", locationAddress = "Vembadi Road, Jaffna", category = ContactCategory.SCHOOLS),

            // Gampaha District Schools
            PoliceContact(id = "sch_bandaranayake", stationOrDesignation = "Bandaranayake College Gampaha", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "033-2222242", locationAddress = "Miriswatta Road, Gampaha", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_rathnavali", stationOrDesignation = "Rathnavali Balika Vidyalaya Gampaha", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "033-2222442", locationAddress = "Bauddhaloka Mawatha, Gampaha", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_maris_stella", stationOrDesignation = "Maris Stella College Negombo", rank = "SEMI-GOVT SCHOOL", officerName = "Principal / Office", generalPhone = "031-2222242", locationAddress = "Negombo Road, Negombo", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_newstead", stationOrDesignation = "Newstead Girls College Negombo", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "031-2222243", locationAddress = "Main Street, Negombo", category = ContactCategory.SCHOOLS),

            // Kalutara District Schools
            PoliceContact(id = "sch_taxila", stationOrDesignation = "Taxila Central College Horana", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "034-2261242", locationAddress = "Anguruwatota Road, Horana", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_sri_sumangala", stationOrDesignation = "Sri Sumangala College Panadura", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "038-2232242", locationAddress = "Walana, Panadura", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_agamothi", stationOrDesignation = "Agamothi Balika Vidyalaya Panadura", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "038-2232243", locationAddress = "Panadura Town", category = ContactCategory.SCHOOLS),

            // Anuradhapura, Badulla, Ratnapura, Matale, Batticaloa, Trincomalee & Other Districts
            PoliceContact(id = "sch_anuradhapura_central", stationOrDesignation = "Anuradhapura Central College", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "025-2222243", locationAddress = "Harischandra Mawatha, Anuradhapura", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_swarnapali", stationOrDesignation = "Swarnapali Balika Vidyalaya Anuradhapura", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "025-2222244", locationAddress = "Anuradhapura New Town", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_seevali", stationOrDesignation = "Seevali Central College Ratnapura", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "045-2222242", locationAddress = "Hidellana, Ratnapura", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_ferguson", stationOrDesignation = "Ferguson High School Ratnapura", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "045-2222243", locationAddress = "Bandaranayake Mawatha, Ratnapura", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_dharmadutha", stationOrDesignation = "Dharmadutha College Badulla", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "055-2222242", locationAddress = "Bandarawela Road, Badulla", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_visakha_badulla", stationOrDesignation = "Visakha Girls' High School Badulla", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "055-2222243", locationAddress = "Badulla Town", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_thomas_bandarawela", stationOrDesignation = "St. Thomas' College Bandarawela", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "057-2222242", locationAddress = "Gurutalawa Road, Bandarawela", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_kegalu", stationOrDesignation = "Kegalu Vidyalaya Kegalle", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "035-2222242", locationAddress = "Kandy Road, Kegalle", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_joseph_kegalle", stationOrDesignation = "St. Joseph's Balika Maha Vidyalaya Kegalle", rank = "NATIONAL GIRLS SCHOOL", officerName = "Principal / Office", generalPhone = "035-2222243", locationAddress = "Kegalle Town", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_royal_polonnaruwa", stationOrDesignation = "Royal Central College Polonnaruwa", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "027-2222242", locationAddress = "Kaduruwela, Polonnaruwa", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_thomas_matale", stationOrDesignation = "St. Thomas' College Matale", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "066-2222242", locationAddress = "Hulangamuwa Road, Matale", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_michael_batti", stationOrDesignation = "St. Michael's College Batticaloa", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "065-2222242", locationAddress = "Central Road, Batticaloa", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_st_joseph_trinco", stationOrDesignation = "St. Joseph's College Trincomalee", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "026-2222242", locationAddress = "Dockyard Road, Trincomalee", category = ContactCategory.SCHOOLS),
            PoliceContact(id = "sch_ds_ampara", stationOrDesignation = "D.S. Senanayake National College Ampara", rank = "NATIONAL SCHOOL", officerName = "Principal / Office", generalPhone = "063-2222242", locationAddress = "Ampara Town", category = ContactCategory.SCHOOLS)
        )
    }

    fun getEmergencyAndGovtContacts(): List<PoliceContact> {
        return listOf(
            // Emergency Services
            PoliceContact(
                id = "em_119",
                stationOrDesignation = "119 Emergency Police Hotline",
                rank = "EMERGENCY HOTLINE",
                officerName = "Police Emergency Service (24/7)",
                generalPhone = "119",
                mobilePhone = "119",
                email = "telligp@police.gov.lk",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_1990",
                stationOrDesignation = "1990 Suwa Seriya Ambulance",
                rank = "AMBULANCE",
                officerName = "Emergency Pre-Hospital Care Service",
                generalPhone = "1990",
                mobilePhone = "1990",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_110",
                stationOrDesignation = "110 Fire & Rescue Brigade",
                rank = "FIRE SERVICE",
                officerName = "Fire & Ambulance Emergency Brigade",
                generalPhone = "110",
                officePhone2 = "011-2422222",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_118",
                stationOrDesignation = "118 National Security Emergency",
                rank = "HOTLINE",
                officerName = "Ministry of Defense / Police",
                generalPhone = "118",
                mobilePhone = "118",
                email = "info@police.gov.lk",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_bomb",
                stationOrDesignation = "Bomb Disposal Squad (Army)",
                rank = "ARMY SQUAD",
                officerName = "Army Emergency Ordnance Squad",
                generalPhone = "011-2434251",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_accident",
                stationOrDesignation = "Accident Service (General Hospital)",
                rank = "NATIONAL HOSPITAL",
                officerName = "National Hospital Emergency Unit",
                generalPhone = "011-2691111",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_stjohn",
                stationOrDesignation = "St. Johns Ambulance Brigade",
                rank = "AMBULANCE",
                officerName = "St. Johns Emergency Service",
                generalPhone = "011-2437744",
                category = ContactCategory.EMERGENCY
            ),
            PoliceContact(
                id = "em_redcross",
                stationOrDesignation = "Sri Lanka Red Cross Society",
                rank = "HUMANITARIAN",
                officerName = "Disaster Response Unit",
                generalPhone = "011-2695434",
                category = ContactCategory.EMERGENCY
            ),

            // Short Codes
            PoliceContact(id = "sc_1919", stationOrDesignation = "1919 Government Information Centre (GIC)", rank = "HOTLINE", generalPhone = "1919", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1971", stationOrDesignation = "1971 Railway Information Hotline", rank = "HOTLINE", generalPhone = "1971", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1950", stationOrDesignation = "1950 Postal Information Hotline", rank = "HOTLINE", generalPhone = "1950", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1988", stationOrDesignation = "1988 Ministry of Education Hotline", rank = "HOTLINE", generalPhone = "1988", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1911", stationOrDesignation = "1911 Department of Examinations Hotline", rank = "HOTLINE", generalPhone = "1911", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1938", stationOrDesignation = "1938 Women's Help Line", rank = "HOTLINE", generalPhone = "1938", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1929", stationOrDesignation = "1929 Child Helpline (NCPA)", rank = "HOTLINE", generalPhone = "1929", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1999", stationOrDesignation = "1999 Suwasariya Health Hotline", rank = "HOTLINE", generalPhone = "1999", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_117", stationOrDesignation = "117 Disaster Management Centre", rank = "HOTLINE", generalPhone = "117", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1989", stationOrDesignation = "1989 Tourist Police Hotline", rank = "HOTLINE", generalPhone = "1989", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1905", stationOrDesignation = "1905 Consumer Affairs Authority", rank = "HOTLINE", generalPhone = "1905", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1912", stationOrDesignation = "1912 Tourist Information Hotline", rank = "HOTLINE", generalPhone = "1912", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1920", stationOrDesignation = "1920 Agricultural Advisory Service", rank = "HOTLINE", generalPhone = "1920", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1910", stationOrDesignation = "1910 LECO Power Complaints", rank = "HOTLINE", generalPhone = "1910", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1987", stationOrDesignation = "1987 CEB Electricity Complaints", rank = "HOTLINE", generalPhone = "1987", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1939", stationOrDesignation = "1939 Water Board Hotline", rank = "HOTLINE", generalPhone = "1939", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1984", stationOrDesignation = "1984 Dangerous Drugs Control Board", rank = "HOTLINE", generalPhone = "1984", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1955", stationOrDesignation = "1955 National Transport Commission", rank = "HOTLINE", generalPhone = "1955", category = ContactCategory.SHORT_CODES),
            PoliceContact(id = "sc_1918", stationOrDesignation = "1918 Higher Education Ministry Hotline", rank = "HOTLINE", generalPhone = "1918", category = ContactCategory.SHORT_CODES),

            // Hospitals
            PoliceContact(id = "hosp_nhsl", stationOrDesignation = "National Hospital of Sri Lanka (Colombo)", rank = "GOVT HOSPITAL", generalPhone = "011-2691111", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_csth", stationOrDesignation = "Colombo South Teaching Hospital (Kalubowila)", rank = "GOVT HOSPITAL", generalPhone = "011-2763064", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_cnth", stationOrDesignation = "Colombo North Teaching Hospital (Ragama)", rank = "GOVT HOSPITAL", generalPhone = "011-2959261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_lrsp", stationOrDesignation = "Lady Ridgeway Hospital for Children", rank = "GOVT HOSPITAL", generalPhone = "011-2693711", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_cwh", stationOrDesignation = "Castle Street Hospital for Women", rank = "GOVT HOSPITAL", generalPhone = "011-2696224", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_dmh", stationOrDesignation = "De Soysa Hospital for Women", rank = "GOVT HOSPITAL", generalPhone = "011-2696225", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_eye", stationOrDesignation = "National Eye Hospital Colombo", rank = "GOVT HOSPITAL", generalPhone = "011-2693911", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_dental", stationOrDesignation = "Dental Institute Colombo", rank = "GOVT HOSPITAL", generalPhone = "011-2693106", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_idhe", stationOrDesignation = "National Institute of Infectious Diseases (IDH)", rank = "GOVT HOSPITAL", generalPhone = "011-2569999", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_kandy", stationOrDesignation = "National Hospital Kandy", rank = "GOVT HOSPITAL", generalPhone = "081-2233337", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_peradeniya", stationOrDesignation = "Teaching Hospital Peradeniya", rank = "GOVT HOSPITAL", generalPhone = "081-2388001", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_karapitiya", stationOrDesignation = "Teaching Hospital Karapitiya (Galle)", rank = "GOVT HOSPITAL", generalPhone = "091-2232250", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_matara", stationOrDesignation = "General Hospital Matara", rank = "GOVT HOSPITAL", generalPhone = "041-2222227", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_jaffna", stationOrDesignation = "Teaching Hospital Jaffna", rank = "GOVT HOSPITAL", generalPhone = "021-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_anuradhapura", stationOrDesignation = "Teaching Hospital Anuradhapura", rank = "GOVT HOSPITAL", generalPhone = "025-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_kurunegala", stationOrDesignation = "Teaching Hospital Kurunegala", rank = "GOVT HOSPITAL", generalPhone = "037-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_badulla", stationOrDesignation = "Provincial General Hospital Badulla", rank = "GOVT HOSPITAL", generalPhone = "055-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_ratnapura", stationOrDesignation = "Provincial General Hospital Ratnapura", rank = "GOVT HOSPITAL", generalPhone = "045-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_batticaloa", stationOrDesignation = "Teaching Hospital Batticaloa", rank = "GOVT HOSPITAL", generalPhone = "065-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_trincomalee", stationOrDesignation = "District General Hospital Trincomalee", rank = "GOVT HOSPITAL", generalPhone = "026-2222261", category = ContactCategory.HOSPITALS),
            PoliceContact(id = "hosp_sri_jayawardenepura", stationOrDesignation = "Sri Jayewardenepura General Hospital", rank = "GOVT HOSPITAL", generalPhone = "011-2778610", category = ContactCategory.HOSPITALS),

            // Govt Services & Depts
            PoliceContact(id = "govt_immigration", stationOrDesignation = "Department of Immigration & Emigration (Passports)", rank = "GOVT DEPT", generalPhone = "011-5329000", officePhone2 = "1962", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_nic", stationOrDesignation = "Department for Registration of Persons (NIC)", rank = "GOVT DEPT", generalPhone = "011-2862217", officePhone2 = "011-2862228", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_dmt", stationOrDesignation = "Department of Motor Traffic (Werahera / Driving Licences)", rank = "GOVT DEPT", generalPhone = "011-2517544", officePhone2 = "011-2694331", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_customs", stationOrDesignation = "Sri Lanka Customs Department", rank = "GOVT DEPT", generalPhone = "011-2470945", officePhone2 = "011-2221333", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_cbsl", stationOrDesignation = "Central Bank of Sri Lanka (CBSL)", rank = "GOVT DEPT", generalPhone = "011-2477000", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_dmc", stationOrDesignation = "Disaster Management Centre (DMC)", rank = "GOVT DEPT", generalPhone = "011-2136136", officePhone2 = "117", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_meteorology", stationOrDesignation = "Department of Meteorology (Weather Info)", rank = "GOVT DEPT", generalPhone = "011-2694846", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_ceb", stationOrDesignation = "Ceylon Electricity Board (CEB Head Office)", rank = "GOVT DEPT", generalPhone = "011-2324471", officePhone2 = "1987", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_water", stationOrDesignation = "National Water Supply & Drainage Board", rank = "GOVT DEPT", generalPhone = "011-2638888", officePhone2 = "1939", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_trcsl", stationOrDesignation = "Telecommunications Regulatory Commission (TRCSL)", rank = "GOVT DEPT", generalPhone = "011-2689345", officePhone2 = "1900", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_sltb", stationOrDesignation = "Sri Lanka Transport Board (SLTB Head Office)", rank = "GOVT DEPT", generalPhone = "011-2581120", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "govt_tourism", stationOrDesignation = "Sri Lanka Tourism Development Authority (SLTDA)", rank = "TOURISM", generalPhone = "011-2426900", officePhone2 = "1912", category = ContactCategory.GOVT_SERVICES)
        )
    }

    fun getBusStandContacts(): List<PoliceContact> {
        return listOf(
            // Transport Hotlines & Central Terminals
            PoliceContact(id = "bus_ntc_1955", stationOrDesignation = "1955 National Transport Commission (NTC Hotline)", rank = "HOTLINE (24/7)", officerName = "ජාතික ගමනාගමන කොමිෂන් සභාව ක්ෂණික ඇමතුම්", generalPhone = "1955", mobilePhone = "1955", officePhone2 = "011-2587372", locationAddress = "241 Park Road, Colombo 05", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_sltb_hq", stationOrDesignation = "Sri Lanka Transport Board (SLTB Central Operations)", rank = "HEAD OFFICE", officerName = "SLTB Bus Operations & Inquiries", generalPhone = "011-2581120", officePhone2 = "011-2581125", locationAddress = "200 Kirula Road, Narahenpita, Colombo 05", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_bastian", stationOrDesignation = "Bastian Mawatha Long Distance Bus Terminal (Pettah)", rank = "CENTRAL BUS STAND", officerName = "Station Superintendent / Inquiries", generalPhone = "011-2329606", officePhone2 = "011-2329607", locationAddress = "Bastian Mawatha, Pettah, Colombo 11", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_gunasinghepura", stationOrDesignation = "Gunasinghepura Private Bus Terminal (Colombo Fort)", rank = "CENTRAL BUS STAND", officerName = "Private Bus Operators Terminal Office", generalPhone = "011-2434215", locationAddress = "Gunasinghepura, Colombo Fort", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_mmtc_makumbura", stationOrDesignation = "Makumbura Multimodal Transport Center (MMTC Kottawa)", rank = "EXPRESSWAY HUB", officerName = "Expressway & Multimodal Bus Operations", generalPhone = "011-2858110", officePhone2 = "011-2858111", locationAddress = "High Level Road, Makumbura, Kottawa", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_kadawatha", stationOrDesignation = "Kadawatha Expressway Bus Terminal", rank = "EXPRESSWAY HUB", officerName = "Expressway Bus Terminal Office", generalPhone = "011-2925188", locationAddress = "Kandy Road, Kadawatha", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_katunayake", stationOrDesignation = "Katunayake Airport Bus Stand & Expressway Terminal", rank = "AIRPORT TERMINAL", officerName = "Airport Bus Inquiries", generalPhone = "011-2252844", locationAddress = "Bandaranaike International Airport, Katunayake", category = ContactCategory.TRAVEL),

            // Western Province Major Bus Stands & Depots
            PoliceContact(id = "bus_colombo_central", stationOrDesignation = "Central Bus Stand Pettah (SLTB)", rank = "CENTRAL BUS STAND", officerName = "Inquiry Officer", generalPhone = "011-2329604", locationAddress = "Olcott Mawatha, Pettah, Colombo 11", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_dehiwala", stationOrDesignation = "Dehiwala Bus Stand & Depot", rank = "BUS STAND / DEPOT", officerName = "Depot Superintendent", generalPhone = "011-2717250", locationAddress = "Galle Road, Dehiwala", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_moratuwa", stationOrDesignation = "Moratuwa Bus Stand & SLTB Depot", rank = "BUS STAND / DEPOT", officerName = "Depot Superintendent", generalPhone = "011-2645250", locationAddress = "Station Road, Moratuwa", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_panadura", stationOrDesignation = "Panadura Central Bus Stand & Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent / Inquiries", generalPhone = "038-2232250", locationAddress = "Galle Road, Panadura", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_kalutara", stationOrDesignation = "Kalutara Central Bus Stand & SLTB Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "034-2222250", locationAddress = "Galle Road, Kalutara", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_horana", stationOrDesignation = "Horana Central Bus Stand & SLTB Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "034-2261250", locationAddress = "Anguruwatota Road, Horana", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_negombo", stationOrDesignation = "Negombo Central Bus Stand & SLTB Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "031-2222250", locationAddress = "Main Street, Negombo", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_gampaha", stationOrDesignation = "Gampaha Central Bus Stand & SLTB Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "033-2222250", locationAddress = "Yakkala Road, Gampaha", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_avissawella", stationOrDesignation = "Avissawella Central Bus Stand & SLTB Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "036-2222250", locationAddress = "Ratnapura Road, Avissawella", category = ContactCategory.TRAVEL),

            // Central Province Major Bus Stands & Depots
            PoliceContact(id = "bus_kandy_goodsshed", stationOrDesignation = "Kandy Goods Shed Central Bus Stand", rank = "MAIN TERMINAL", officerName = "Station Master / Inquiries", generalPhone = "081-2222221", officePhone2 = "081-2222215", locationAddress = "Goods Shed Road, Kandy", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_kandy_clocktower", stationOrDesignation = "Kandy Clock Tower Bus Stand (City Service)", rank = "CITY BUS STAND", officerName = "City Bus Control", generalPhone = "081-2222230", locationAddress = "Clock Tower Plaza, Kandy", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_matale", stationOrDesignation = "Matale Central Bus Stand & SLTB Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "066-2222250", locationAddress = "Trincomalee Street, Matale", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_nuwaraeliya", stationOrDesignation = "Nuwara Eliya Central Bus Stand & SLTB Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "052-2222250", locationAddress = "Kandy Road, Nuwara Eliya", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_hatton", stationOrDesignation = "Hatton Central Bus Stand & Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "051-2222250", locationAddress = "Dunbar Road, Hatton", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_dambulla", stationOrDesignation = "Dambulla Central Bus Stand", rank = "CENTRAL BUS STAND", officerName = "Bus Control Room", generalPhone = "066-2284250", locationAddress = "Kurunegala Road, Dambulla", category = ContactCategory.TRAVEL),

            // Southern Province Major Bus Stands & Depots
            PoliceContact(id = "bus_galle", stationOrDesignation = "Galle Central Bus Stand & SLTB Depot", rank = "MAIN TERMINAL", officerName = "Depot Superintendent / Inquiries", generalPhone = "091-2234250", officePhone2 = "091-2234251", locationAddress = "Colombo Road, Galle", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_matara", stationOrDesignation = "Matara Central Bus Stand & SLTB Depot", rank = "MAIN TERMINAL", officerName = "Depot Superintendent / Inquiries", generalPhone = "041-2222280", officePhone2 = "041-2222281", locationAddress = "Anagarika Dharmapala Mawatha, Matara", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_hambantota", stationOrDesignation = "Hambantota Central Bus Stand & Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "047-2220250", locationAddress = "Main Street, Hambantota", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_tangalle", stationOrDesignation = "Tangalle Central Bus Stand & Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "047-2240250", locationAddress = "Galle Road, Tangalle", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_ambalangoda", stationOrDesignation = "Ambalangoda Central Bus Stand & Depot", rank = "BUS STAND / DEPOT", officerName = "Depot Superintendent", generalPhone = "091-2258250", locationAddress = "Galle Road, Ambalangoda", category = ContactCategory.TRAVEL),

            // North Western & North Central Provinces
            PoliceContact(id = "bus_kurunegala", stationOrDesignation = "Kurunegala Central Bus Stand & SLTB Depot", rank = "MAIN TERMINAL", officerName = "Depot Superintendent / Inquiries", generalPhone = "037-2222240", officePhone2 = "037-2222241", locationAddress = "Dambulla Road, Kurunegala", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_anuradhapura", stationOrDesignation = "Anuradhapura Central Bus Stand (Old & New)", rank = "MAIN TERMINAL", officerName = "Depot Superintendent / Inquiries", generalPhone = "025-2222250", officePhone2 = "025-2222255", locationAddress = "Maithripala Senanayake Mawatha, Anuradhapura", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_polonnaruwa", stationOrDesignation = "Polonnaruwa Kaduruwela Central Bus Stand", rank = "MAIN TERMINAL", officerName = "Depot Superintendent", generalPhone = "027-2222250", locationAddress = "Batticaloa Road, Kaduruwela, Polonnaruwa", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_puttalam", stationOrDesignation = "Puttalam Central Bus Stand & Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "032-2265250", locationAddress = "Service Road, Puttalam", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_chilaw", stationOrDesignation = "Chilaw Central Bus Stand & Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "032-2222250", locationAddress = "Kurunegala Road, Chilaw", category = ContactCategory.TRAVEL),

            // Northern & Eastern Provinces
            PoliceContact(id = "bus_jaffna", stationOrDesignation = "Jaffna Central Bus Stand & SLTB Depot", rank = "MAIN TERMINAL", officerName = "Depot Superintendent / Inquiries", generalPhone = "021-2222250", officePhone2 = "021-2222251", locationAddress = "Hospital Road, Jaffna", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_vavuniya", stationOrDesignation = "Vavuniya Central Bus Stand & Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "024-2222250", locationAddress = "Kandy Road, Vavuniya", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_kilinochchi", stationOrDesignation = "Kilinochchi Central Bus Stand", rank = "CENTRAL BUS STAND", officerName = "Station Master", generalPhone = "021-2285250", locationAddress = "A9 Road, Kilinochchi", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_mannar", stationOrDesignation = "Mannar Central Bus Stand & Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "023-2222250", locationAddress = "Main Street, Mannar", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_mullaitivu", stationOrDesignation = "Mullaitivu Central Bus Stand", rank = "CENTRAL BUS STAND", officerName = "Station Master", generalPhone = "021-2290250", locationAddress = "Beach Road, Mullaitivu", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_trincomalee", stationOrDesignation = "Trincomalee Central Bus Stand & Depot", rank = "MAIN TERMINAL", officerName = "Depot Superintendent", generalPhone = "026-2222250", locationAddress = "Inner Harbour Road, Trincomalee", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_batticaloa", stationOrDesignation = "Batticaloa Central Bus Stand & Depot", rank = "MAIN TERMINAL", officerName = "Depot Superintendent", generalPhone = "065-2222250", locationAddress = "Central Road, Batticaloa", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_ampara", stationOrDesignation = "Ampara Central Bus Stand & Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "063-2222250", locationAddress = "Inginiyagala Road, Ampara", category = ContactCategory.TRAVEL),

            // Uva & Sabaragamuwa Provinces
            PoliceContact(id = "bus_badulla", stationOrDesignation = "Badulla Central Bus Stand & SLTB Depot", rank = "MAIN TERMINAL", officerName = "Depot Superintendent", generalPhone = "055-2222250", locationAddress = "Bazaar Street, Badulla", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_bandarawela", stationOrDesignation = "Bandarawela Central Bus Stand", rank = "CENTRAL BUS STAND", officerName = "Station Master", generalPhone = "057-2222250", locationAddress = "Main Street, Bandarawela", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_monaragala", stationOrDesignation = "Monaragala Central Bus Stand & Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "055-2276250", locationAddress = "Wellawaya Road, Monaragala", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_ratnapura", stationOrDesignation = "Ratnapura Central Bus Stand & SLTB Depot", rank = "MAIN TERMINAL", officerName = "Depot Superintendent", generalPhone = "045-2222250", locationAddress = "Bandaranayake Mawatha, Ratnapura", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_embilipitiya", stationOrDesignation = "Embilipitiya Central Bus Stand & Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "047-2230250", locationAddress = "Town Center, Embilipitiya", category = ContactCategory.TRAVEL),
            PoliceContact(id = "bus_kegalle", stationOrDesignation = "Kegalle Central Bus Stand & SLTB Depot", rank = "CENTRAL BUS STAND", officerName = "Depot Superintendent", generalPhone = "035-2222250", locationAddress = "Kandy Road, Kegalle", category = ContactCategory.TRAVEL)
        )
    }

    fun getPradeshiyaLekamAndNagaraSabhaContacts(): List<PoliceContact> {
        return listOf(
            // Colombo District Councils & Divisional Secretariats
            PoliceContact(id = "ds_colombo", stationOrDesignation = "Colombo Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary / Public Inquiries", generalPhone = "011-2320141", officePhone2 = "011-2439174", locationAddress = "Dam Street, Colombo 12", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_colombo", stationOrDesignation = "Colombo Municipal Council (CMC Town Hall)", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner / Public Desk", generalPhone = "011-2695121", officePhone2 = "011-2695122", locationAddress = "Town Hall, Colombo 07", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_thimbirigasyaya", stationOrDesignation = "Thimbirigasyaya Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "011-2589578", locationAddress = "Jawardene Place, Colombo 05", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_kotte", stationOrDesignation = "Sri Jayewardenepura Kotte Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "011-2877000", officePhone2 = "011-2877001", locationAddress = "Rajagiriya Road, Kotte", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_kotte", stationOrDesignation = "Sri Jayewardenepura Kotte Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "011-2862214", locationAddress = "Battaramulla, Kotte", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_dehiwala", stationOrDesignation = "Dehiwala-Mount Lavinia Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "011-2717000", officePhone2 = "011-2717001", locationAddress = "Galle Road, Dehiwala", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_dehiwala", stationOrDesignation = "Dehiwala Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "011-2722141", locationAddress = "Station Road, Dehiwala", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_moratuwa", stationOrDesignation = "Moratuwa Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "011-2645000", locationAddress = "Galle Road, Moratuwa", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_moratuwa", stationOrDesignation = "Moratuwa Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "011-2645214", locationAddress = "Rawathawatta, Moratuwa", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "uc_maharagama", stationOrDesignation = "Maharagama Urban Council", rank = "URBAN COUNCIL", officerName = "Secretary / Public Inquiries", generalPhone = "011-2850000", locationAddress = "High Level Road, Maharagama", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_maharagama", stationOrDesignation = "Maharagama Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "011-2850214", locationAddress = "Church Road, Maharagama", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "uc_kesbewa", stationOrDesignation = "Kesbewa Urban Council (Piliyandala)", rank = "URBAN COUNCIL", officerName = "Secretary", generalPhone = "011-2708000", locationAddress = "Piliyandala Town", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_kesbewa", stationOrDesignation = "Kesbewa Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "011-2708214", locationAddress = "Dampe Road, Piliyandala", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ps_homagama", stationOrDesignation = "Homagama Pradeshiya Sabha", rank = "PRADESHIYA SABHA", officerName = "Secretary", generalPhone = "011-2855000", locationAddress = "High Level Road, Homagama", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_homagama", stationOrDesignation = "Homagama Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "011-2855214", locationAddress = "Court Road, Homagama", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_kaduwela", stationOrDesignation = "Kaduwela Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "011-2536000", locationAddress = "Nawagamuwa Road, Kaduwela", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_kaduwela", stationOrDesignation = "Kaduwela Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "011-2536214", locationAddress = "Malabe Road, Kaduwela", category = ContactCategory.GOVT_SERVICES),

            // Gampaha District Councils & Secretariats
            PoliceContact(id = "mc_gampaha", stationOrDesignation = "Gampaha Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "033-2222000", locationAddress = "Bauddhaloka Mawatha, Gampaha", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_gampaha", stationOrDesignation = "Gampaha Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "033-2222214", locationAddress = "Yakkala Road, Gampaha", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_negombo", stationOrDesignation = "Negombo Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "031-2222000", locationAddress = "Main Street, Negombo", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_negombo", stationOrDesignation = "Negombo Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "031-2222214", locationAddress = "St. Joseph's Street, Negombo", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_kelaniya", stationOrDesignation = "Kelaniya Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "011-2911214", locationAddress = "Waragoda Road, Kelaniya", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ps_kelaniya", stationOrDesignation = "Kelaniya Pradeshiya Sabha", rank = "PRADESHIYA SABHA", officerName = "Secretary", generalPhone = "011-2911000", locationAddress = "Biyagama Road, Kelaniya", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "uc_wattala", stationOrDesignation = "Wattala-Mabole Urban Council", rank = "URBAN COUNCIL", officerName = "Secretary", generalPhone = "011-2930000", locationAddress = "Negombo Road, Wattala", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_wattala", stationOrDesignation = "Wattala Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "011-2930214", locationAddress = "Averiwatta, Wattala", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "uc_jaela", stationOrDesignation = "Ja-Ela Urban Council", rank = "URBAN COUNCIL", officerName = "Secretary", generalPhone = "011-2236000", locationAddress = "Negombo Road, Ja-Ela", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_jaela", stationOrDesignation = "Ja-Ela Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "011-2236214", locationAddress = "Station Road, Ja-Ela", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_mirigama", stationOrDesignation = "Mirigama Divisional Secretariat & PS", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "033-2273214", locationAddress = "Main Street, Mirigama", category = ContactCategory.GOVT_SERVICES),

            // Kalutara District Councils & Secretariats
            PoliceContact(id = "uc_kalutara", stationOrDesignation = "Kalutara Urban Council", rank = "URBAN COUNCIL", officerName = "Secretary", generalPhone = "034-2222000", locationAddress = "Galle Road, Kalutara", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_kalutara", stationOrDesignation = "Kalutara Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "034-2222214", locationAddress = "Main Street, Kalutara", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "uc_panadura", stationOrDesignation = "Panadura Urban Council", rank = "URBAN COUNCIL", officerName = "Secretary", generalPhone = "038-2232000", locationAddress = "Galle Road, Panadura", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_panadura", stationOrDesignation = "Panadura Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "038-2232214", locationAddress = "Arthur V. Dias Mawatha, Panadura", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "uc_horana", stationOrDesignation = "Horana Urban Council", rank = "URBAN COUNCIL", officerName = "Secretary", generalPhone = "034-2261000", locationAddress = "Panadura Road, Horana", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_horana", stationOrDesignation = "Horana Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "034-2261214", locationAddress = "Anguruwatota Road, Horana", category = ContactCategory.GOVT_SERVICES),

            // Central Province (Kandy, Matale, Nuwara Eliya)
            PoliceContact(id = "mc_kandy", stationOrDesignation = "Kandy Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "081-2222000", officePhone2 = "081-2222275", locationAddress = "Town Hall, Kandy", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_kandy", stationOrDesignation = "Kandy Four Gravets Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "081-2222214", locationAddress = "William Gopallawa Mawatha, Kandy", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_matale", stationOrDesignation = "Matale Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "066-2222000", locationAddress = "Trincomalee Street, Matale", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_matale", stationOrDesignation = "Matale Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "066-2222214", locationAddress = "Hulangamuwa Road, Matale", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_nuwaraeliya", stationOrDesignation = "Nuwara Eliya Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "052-2222000", locationAddress = "Queen Elizabeth Drive, Nuwara Eliya", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_nuwaraeliya", stationOrDesignation = "Nuwara Eliya Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "052-2222214", locationAddress = "Badulla Road, Nuwara Eliya", category = ContactCategory.GOVT_SERVICES),

            // Southern Province (Galle, Matara, Hambantota)
            PoliceContact(id = "mc_galle", stationOrDesignation = "Galle Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "091-2234000", locationAddress = "Town Hall, Galle", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_galle", stationOrDesignation = "Galle Four Gravets Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "091-2234214", locationAddress = "Wakwella Road, Galle", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_matara", stationOrDesignation = "Matara Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "041-2222000", locationAddress = "Hakmana Road, Matara", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_matara", stationOrDesignation = "Matara Four Gravets Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "041-2222214", locationAddress = "Fort, Matara", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_hambantota", stationOrDesignation = "Hambantota Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "047-2220000", locationAddress = "Main Street, Hambantota", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_hambantota", stationOrDesignation = "Hambantota Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "047-2220214", locationAddress = "Siribopura, Hambantota", category = ContactCategory.GOVT_SERVICES),

            // North Western & North Central
            PoliceContact(id = "mc_kurunegala", stationOrDesignation = "Kurunegala Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "037-2222000", locationAddress = "Town Hall, Kurunegala", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_kurunegala", stationOrDesignation = "Kurunegala Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "037-2222214", locationAddress = "Colombo Road, Kurunegala", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_anuradhapura", stationOrDesignation = "Anuradhapura Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "025-2222000", locationAddress = "Maithripala Senanayake Mawatha, Anuradhapura", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_anuradhapura", stationOrDesignation = "Nuwaragam Palatha Central Divisional Secretariat (Anuradhapura)", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "025-2222214", locationAddress = "New Town, Anuradhapura", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_polonnaruwa", stationOrDesignation = "Thamankaduwa Divisional Secretariat (Polonnaruwa)", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "027-2222214", locationAddress = "Kaduruwela, Polonnaruwa", category = ContactCategory.GOVT_SERVICES),

            // Northern, Eastern, Uva & Sabaragamuwa
            PoliceContact(id = "mc_jaffna", stationOrDesignation = "Jaffna Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "021-2222000", locationAddress = "Point Pedro Road, Nallur, Jaffna", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_jaffna", stationOrDesignation = "Jaffna Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "021-2222214", locationAddress = "Beach Road, Jaffna", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_batticaloa", stationOrDesignation = "Batticaloa Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "065-2222000", locationAddress = "Central Road, Batticaloa", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_batticaloa", stationOrDesignation = "Manmunai North Divisional Secretariat (Batticaloa)", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "065-2222214", locationAddress = "Batticaloa Town", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "uc_trincomalee", stationOrDesignation = "Trincomalee Urban Council", rank = "URBAN COUNCIL", officerName = "Secretary", generalPhone = "026-2222000", locationAddress = "Dockyard Road, Trincomalee", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_trincomalee", stationOrDesignation = "Town and Gravets Divisional Secretariat (Trincomalee)", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "026-2222214", locationAddress = "Inner Harbour Road, Trincomalee", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_badulla", stationOrDesignation = "Badulla Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "055-2222000", locationAddress = "Bazaar Street, Badulla", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_badulla", stationOrDesignation = "Badulla Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "055-2222214", locationAddress = "Lower King Street, Badulla", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "mc_ratnapura", stationOrDesignation = "Ratnapura Municipal Council", rank = "MUNICIPAL COUNCIL", officerName = "Municipal Commissioner", generalPhone = "045-2222000", locationAddress = "Town Hall, Ratnapura", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_ratnapura", stationOrDesignation = "Ratnapura Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "045-2222214", locationAddress = "Main Street, Ratnapura", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "uc_kegalle", stationOrDesignation = "Kegalle Urban Council", rank = "URBAN COUNCIL", officerName = "Secretary", generalPhone = "035-2222000", locationAddress = "Kandy Road, Kegalle", category = ContactCategory.GOVT_SERVICES),
            PoliceContact(id = "ds_kegalle", stationOrDesignation = "Kegalle Divisional Secretariat", rank = "DIVISIONAL SECRETARIAT", officerName = "Divisional Secretary", generalPhone = "035-2222214", locationAddress = "Court Road, Kegalle", category = ContactCategory.GOVT_SERVICES)
        )
    }
}
