package com.rafiqaldhikr.app.core.model

data class VerifiedDhikrTemplate(
    val key: String,
    val category: AdhkarCategory,
    val title: String,
    val arabicText: String,
    val targetCount: Int,
    val sourceReference: String,
    val virtueNote: String = ""
)

object VerifiedAdhkarCatalog {

    private const val AYAT_AL_KURSI =
        "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ\n﴿اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ﴾"

    private const val SURAH_IKHLAS =
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n﴿قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ﴾"

    private const val SURAH_FALAQ =
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n﴿قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِن شَرِّ مَا خَلَقَ ۝ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ﴾"

    private const val SURAH_NAS =
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n﴿قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ﴾"

    private const val SAYYID_AL_ISTIGHFAR =
        "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ."

    private const val BISMILLAH_LA_YADURRU =
        "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ."

    private const val SUBHANALLAH_WA_BIHAMDIH =
        "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ."

    private const val TAHLIL_TEN =
        "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ."

    val defaultEntries: List<VerifiedDhikrTemplate> = listOf(
        // أذكار الصباح
        VerifiedDhikrTemplate(
            key = "morning_kursi",
            category = AdhkarCategory.MORNING,
            title = "آية الكرسي",
            arabicText = AYAT_AL_KURSI,
            targetCount = 1,
            sourceReference = "سورة البقرة: آية ٢٥٥"
        ),
        VerifiedDhikrTemplate(
            key = "morning_ikhlas",
            category = AdhkarCategory.MORNING,
            title = "سورة الإخلاص",
            arabicText = SURAH_IKHLAS,
            targetCount = 3,
            sourceReference = "سورة الإخلاص — رواه أبو داود (٥٠٨٢) والترمذي (٣٥٧٥)"
        ),
        VerifiedDhikrTemplate(
            key = "morning_falaq",
            category = AdhkarCategory.MORNING,
            title = "سورة الفلق",
            arabicText = SURAH_FALAQ,
            targetCount = 3,
            sourceReference = "سورة الفلق — رواه أبو داود (٥٠٨٢) والترمذي (٣٥٧٥)"
        ),
        VerifiedDhikrTemplate(
            key = "morning_nas",
            category = AdhkarCategory.MORNING,
            title = "سورة الناس",
            arabicText = SURAH_NAS,
            targetCount = 3,
            sourceReference = "سورة الناس — رواه أبو داود (٥٠٨٢) والترمذي (٣٥٧٥)"
        ),
        VerifiedDhikrTemplate(
            key = "morning_sayyid_istighfar",
            category = AdhkarCategory.MORNING,
            title = "سيد الاستغفار",
            arabicText = SAYYID_AL_ISTIGHFAR,
            targetCount = 1,
            sourceReference = "صحيح البخاري (٦٣٠٦)"
        ),
        VerifiedDhikrTemplate(
            key = "morning_asbahna",
            category = AdhkarCategory.MORNING,
            title = "اللهم بك أصبحنا",
            arabicText = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ.",
            targetCount = 1,
            sourceReference = "سنن الترمذي (٣٣٩١) وسنن أبي داود (٥٠٦٨)"
        ),
        VerifiedDhikrTemplate(
            key = "morning_bismillah",
            category = AdhkarCategory.MORNING,
            title = "بسم الله الذي لا يضر مع اسمه شيء",
            arabicText = BISMILLAH_LA_YADURRU,
            targetCount = 3,
            sourceReference = "سنن أبي داود (٥٠٨٨) وسنن الترمذي (٣٣٨٨)"
        ),
        VerifiedDhikrTemplate(
            key = "morning_subhanallah",
            category = AdhkarCategory.MORNING,
            title = "سبحان الله وبحمده",
            arabicText = SUBHANALLAH_WA_BIHAMDIH,
            targetCount = 100,
            sourceReference = "صحيح البخاري (٦٤٠٥) وصحيح مسلم (٢٦٩٢)"
        ),

        // أذكار المساء
        VerifiedDhikrTemplate(
            key = "evening_kursi",
            category = AdhkarCategory.EVENING,
            title = "آية الكرسي",
            arabicText = AYAT_AL_KURSI,
            targetCount = 1,
            sourceReference = "سورة البقرة: آية ٢٥٥"
        ),
        VerifiedDhikrTemplate(
            key = "evening_ikhlas",
            category = AdhkarCategory.EVENING,
            title = "سورة الإخلاص",
            arabicText = SURAH_IKHLAS,
            targetCount = 3,
            sourceReference = "سورة الإخلاص — رواه أبو داود (٥٠٨٢) والترمذي (٣٥٧٥)"
        ),
        VerifiedDhikrTemplate(
            key = "evening_falaq",
            category = AdhkarCategory.EVENING,
            title = "سورة الفلق",
            arabicText = SURAH_FALAQ,
            targetCount = 3,
            sourceReference = "سورة الفلق — رواه أبو داود (٥٠٨٢) والترمذي (٣٥٧٥)"
        ),
        VerifiedDhikrTemplate(
            key = "evening_nas",
            category = AdhkarCategory.EVENING,
            title = "سورة الناس",
            arabicText = SURAH_NAS,
            targetCount = 3,
            sourceReference = "سورة الناس — رواه أبو داود (٥٠٨٢) والترمذي (٣٥٧٥)"
        ),
        VerifiedDhikrTemplate(
            key = "evening_sayyid_istighfar",
            category = AdhkarCategory.EVENING,
            title = "سيد الاستغفار",
            arabicText = SAYYID_AL_ISTIGHFAR,
            targetCount = 1,
            sourceReference = "صحيح البخاري (٦٣٠٦)"
        ),
        VerifiedDhikrTemplate(
            key = "evening_amsayna",
            category = AdhkarCategory.EVENING,
            title = "اللهم بك أمسينا",
            arabicText = "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ.",
            targetCount = 1,
            sourceReference = "سنن الترمذي (٣٣٩١) وسنن أبي داود (٥٠٦٨)"
        ),
        VerifiedDhikrTemplate(
            key = "evening_bismillah",
            category = AdhkarCategory.EVENING,
            title = "بسم الله الذي لا يضر مع اسمه شيء",
            arabicText = BISMILLAH_LA_YADURRU,
            targetCount = 3,
            sourceReference = "سنن أبي داود (٥٠٨٨) وسنن الترمذي (٣٣٨٨)"
        ),
        VerifiedDhikrTemplate(
            key = "evening_subhanallah",
            category = AdhkarCategory.EVENING,
            title = "سبحان الله وبحمده",
            arabicText = SUBHANALLAH_WA_BIHAMDIH,
            targetCount = 100,
            sourceReference = "صحيح البخاري (٦٤٠٥) وصحيح مسلم (٢٦٩٢)"
        ),

        // أذكار النوم
        VerifiedDhikrTemplate(
            key = "sleep_kursi",
            category = AdhkarCategory.SLEEP,
            title = "آية الكرسي عند النوم",
            arabicText = AYAT_AL_KURSI,
            targetCount = 1,
            sourceReference = "سورة البقرة: آية ٢٥٥ — صحيح البخاري (٢٣١١)"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_bismika",
            category = AdhkarCategory.SLEEP,
            title = "دعاء النوم",
            arabicText = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا.",
            targetCount = 1,
            sourceReference = "صحيح البخاري (٦٣٢٤)"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_tasbeeh",
            category = AdhkarCategory.SLEEP,
            title = "التسبيح قبل النوم (٣٣)",
            arabicText = "سُبْحَانَ اللَّهِ (٣٣) — الْحَمْدُ لِلَّهِ (٣٣) — اللَّهُ أَكْبَرُ (٣٤).",
            targetCount = 1,
            sourceReference = "صحيح البخاري (٣١١٣) وصحيح مسلم (٢٧٢٧)"
        ),

        // أذكار الاستيقاظ
        VerifiedDhikrTemplate(
            key = "wakeup_hamd",
            category = AdhkarCategory.WAKEUP,
            title = "ذكر الاستيقاظ من النوم",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ.",
            targetCount = 1,
            sourceReference = "صحيح البخاري (٦٣١٢)"
        ),

        // أذكار يومية عامة
        VerifiedDhikrTemplate(
            key = "general_tahlil",
            category = AdhkarCategory.GENERAL,
            title = "التهليل",
            arabicText = TAHLIL_TEN,
            targetCount = 10,
            sourceReference = "صحيح البخاري (٣٢٩٣) وصحيح مسلم (٢٦٩١)"
        ),
        VerifiedDhikrTemplate(
            key = "general_istighfar",
            category = AdhkarCategory.GENERAL,
            title = "الاستغفار والتوبة",
            arabicText = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ.",
            targetCount = 100,
            sourceReference = "صحيح البخاري (٦٣٠٧) وصحيح مسلم (٢٧٠٢)"
        ),
        VerifiedDhikrTemplate(
            key = "general_salawat",
            category = AdhkarCategory.GENERAL,
            title = "الصلاة الإبراهيمية على النبي ﷺ",
            arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ.",
            targetCount = 10,
            sourceReference = "صحيح البخاري (٣٣٧٠) وصحيح مسلم (٤٠٦)"
        )
    )

    val dailySuggestions: List<Pair<String, String>> = listOf(
        "قراءة وردك اليومي من القرآن الكريم بتدبّر وهدوء",
        "المحافظة على ركعتي الضحى عند تيسر الوقت",
        "الإكثار من الاستغفار والصلاة على النبي ﷺ",
        "التواصل مع أحد الوالدين أو صلة الرحم بكلمة طيبة",
        "تخصيص دقائق لأذكار الصباح أو المساء بخشوع",
        "الصدقة ولو بالقليل أو إدخال السرور على قلب مسلم",
        "الدعاء بظهر الغيب لمن تحب من أهلك وإخوانك"
    )
}
