package com.rafiqaldhikr.app.core.model

/** One dhikr said right after the obligatory prayer. [countAfter] overrides [count] for the listed prayers; [only] limits it to them. */
data class AfterPrayerDhikr(
    val key: String,
    val title: String,
    val text: String,
    val count: Int,
    val source: String,
    val countAfter: Map<String, Int> = emptyMap(),
    val only: Set<String> = emptySet()
) {
    fun appliesTo(prayerKey: String) = only.isEmpty() || prayerKey in only
    fun countFor(prayerKey: String) = countAfter[prayerKey] ?: count
}

/**
 * "الأذكار بعد السلام من الصلاة" from Hisn al-Muslim. The text is taken verbatim from the open JSON edition of the book
 * (github.com/rn0x/hisn_almuslim_json) with only mechanical clean-up; sources are the book's own footnotes.
 * Prayer keys are the names of `PrayerItem` (FAJR, DHUHR, ASR, MAGHRIB, ISHA).
 */
object AfterPrayerAdhkar {
    val all: List<AfterPrayerDhikr> = listOf(
        AfterPrayerDhikr(
            key = "astaghfir",
            title = "الاستغفار",
            text = "أستغفر الله",
            count = 3,
            source = "حصن المسلم — مسلم ١/٤١٤",
            countAfter = emptyMap(),
            only = emptySet()
        ),
        AfterPrayerDhikr(
            key = "anta_salam",
            title = "اللهم أنت السلام",
            text = "اللهم أنت السلام، ومنك السلام، تباركت يا ذا الجلال والإكرام",
            count = 1,
            source = "حصن المسلم — مسلم ١/٤١٤",
            countAfter = emptyMap(),
            only = emptySet()
        ),
        AfterPrayerDhikr(
            key = "la_mani",
            title = "لا إله إلا الله… لا مانع لما أعطيت",
            text = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ وَلَا مُعْطِيَ لِمَا مَنَعْتَ وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
            count = 1,
            source = "حصن المسلم — البخاري ١/٢٥٥، ومسلم ١/٤١٤",
            countAfter = emptyMap(),
            only = emptySet()
        ),
        AfterPrayerDhikr(
            key = "tahlil_long",
            title = "لا إله إلا الله… ولا نعبد إلا إياه",
            text = "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شئ قدير، لا حول ولا قوة إلا بالله، لا إله إلا الله، ولا نعبد إلا إياه، له النعمة وله الفضل، وله الثناء الحسن، لا إله إلا الله مخلصين له الدين ولو كره الكافرون",
            count = 1,
            source = "حصن المسلم — مسلم ١/٤١٥",
            countAfter = emptyMap(),
            only = emptySet()
        ),
        AfterPrayerDhikr(
            key = "subhan",
            title = "سبحان الله",
            text = "سبحان الله",
            count = 33,
            source = "حصن المسلم — مسلم ١/٤١٨",
            countAfter = emptyMap(),
            only = emptySet()
        ),
        AfterPrayerDhikr(
            key = "hamd",
            title = "الحمد لله",
            text = "الحمد لله",
            count = 33,
            source = "حصن المسلم — مسلم ١/٤١٨",
            countAfter = emptyMap(),
            only = emptySet()
        ),
        AfterPrayerDhikr(
            key = "takbir",
            title = "الله أكبر",
            text = "الله أكبر",
            count = 33,
            source = "حصن المسلم — مسلم ١/٤١٨",
            countAfter = emptyMap(),
            only = emptySet()
        ),
        AfterPrayerDhikr(
            key = "tahlil_seal",
            title = "تمام المائة بالتهليل",
            text = "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير",
            count = 1,
            source = "حصن المسلم — مسلم ١/٤١٨ (من قال ذلك دبر كل صلاة غُفرت خطاياه وإن كانت مثل زبد البحر)",
            countAfter = emptyMap(),
            only = emptySet()
        ),
        AfterPrayerDhikr(
            key = "kursi",
            title = "آية الكرسي",
            text = "﴿اللّهُ لاَ إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ﴾",
            count = 1,
            source = "حصن المسلم — النسائي في عمل اليوم والليلة (١٠٠)، وابن السني (١٢١)",
            countAfter = emptyMap(),
            only = emptySet()
        ),
        AfterPrayerDhikr(
            key = "muawwidhat",
            title = "الإخلاص والمعوذتان",
            text = "بسم الله الرحمن الرحيم ﴿قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُواً أَحَدٌ﴾ بسم الله الرحمن الرحيم ﴿قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِن شَرِّ مَا خَلَقَ ۝ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ﴾ بسم الله الرحمن الرحيم ﴿قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَهِ النَّاسِ ۝ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ﴾",
            count = 1,
            source = "حصن المسلم — أبو داود ٢/٨٦، والنسائي ٣/٦٨ (ثلاث مرات بعد المغرب والفجر)",
            countAfter = mapOf("MAGHRIB" to 3, "FAJR" to 3),
            only = emptySet()
        ),
        AfterPrayerDhikr(
            key = "tahlil_ten",
            title = "التهليل عشر مرات",
            text = "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد، يحيي ويميت وهو على كل شئ قدير",
            count = 10,
            source = "حصن المسلم — الترمذي ٥/٥١٥، وأحمد ٤/٢٢٧",
            countAfter = emptyMap(),
            only = setOf("MAGHRIB", "FAJR")
        ),
        AfterPrayerDhikr(
            key = "ilman_nafia",
            title = "اللهم إني أسألك علماً نافعاً",
            text = "اللهم إني أسألك علماً نافعاً، ورزقاً طيباً، وعملاً متقبلاً",
            count = 1,
            source = "حصن المسلم — ابن ماجه",
            countAfter = emptyMap(),
            only = setOf("FAJR")
        )
    )

    fun forPrayer(prayerKey: String): List<AfterPrayerDhikr> = all.filter { it.appliesTo(prayerKey) }
}
