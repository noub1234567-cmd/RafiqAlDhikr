package com.rafiqaldhikr.app.core.model

/**
 * Adhkar taken from "حصن المسلم" (Hisn al-Muslim) by Sa'id bin Ali bin Wahf al-Qahtani: the morning and evening
 * adhkar, the sleep adhkar and the waking adhkar that are not already in [VerifiedAdhkarCatalog.coreEntries].
 *
 * The Arabic text comes verbatim from the open JSON edition of the book (github.com/rn0x/hisn_almuslim_json),
 * with only mechanical clean-up (spacing, verse separators, removal of count/footnote markers). Where a dhikr has a
 * separate evening wording, it is produced with the substitutions the book itself prescribes
 * (أصبح ← أمسى, اليوم ← الليلة). Sources are the citations given in the book's footnotes.
 */
object HisnAdhkarCatalog {
    val entries: List<VerifiedDhikrTemplate> = listOf(
        VerifiedDhikrTemplate(
            key = "morning_mulk_lillah",
            category = AdhkarCategory.MORNING,
            title = "أصبحنا وأصبح الملك لله",
            arabicText = "أصبحنا وأصبح الملك لله، والحمد لله، لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير، ربِّ أسألك خير ما في هذا اليوم وخير ما بعده وأعوذ بك من شر ما في هذا اليوم وشر ما بعده ربِّ أعوذ بك من الكسل وسوء الكبر، ربَّ أعوذ بك من عذابٍ في النار وعذاب في القبر",
            targetCount = 1,
            sourceReference = "حصن المسلم — مسلم ٤/٢٠٨٨"
        ),
        VerifiedDhikrTemplate(
            key = "evening_mulk_lillah",
            category = AdhkarCategory.EVENING,
            title = "أمسينا وأمسى الملك لله",
            arabicText = "أمسينا وأمسى الملك لله، والحمد لله، لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير، ربِّ أسألك خير ما في هذه الليلة وخير ما بعدها وأعوذ بك من شر ما في هذه الليلة وشر ما بعدها ربِّ أعوذ بك من الكسل وسوء الكبر، ربَّ أعوذ بك من عذابٍ في النار وعذاب في القبر",
            targetCount = 1,
            sourceReference = "حصن المسلم — مسلم ٤/٢٠٨٨"
        ),
        VerifiedDhikrTemplate(
            key = "morning_ashhaduka",
            category = AdhkarCategory.MORNING,
            title = "اللهم إني أصبحت أشهدك",
            arabicText = "اللهم إني أصبحت أشهدك وأشهد حملة عرشك، وملائكتك وجميع خلقك، أنك أنت الله لا إله إلا أنت وحدك لا شريك لك، وأن محمداً عبدك ورسولك",
            targetCount = 4,
            sourceReference = "حصن المسلم — أبو داود ٤/٣١٧، والنسائي في عمل اليوم والليلة (٩)"
        ),
        VerifiedDhikrTemplate(
            key = "evening_ashhaduka",
            category = AdhkarCategory.EVENING,
            title = "اللهم إني أمسيت أشهدك",
            arabicText = "اللهم إني أمسيت أشهدك وأشهد حملة عرشك، وملائكتك وجميع خلقك، أنك أنت الله لا إله إلا أنت وحدك لا شريك لك، وأن محمداً عبدك ورسولك",
            targetCount = 4,
            sourceReference = "حصن المسلم — أبو داود ٤/٣١٧، والنسائي في عمل اليوم والليلة (٩)"
        ),
        VerifiedDhikrTemplate(
            key = "morning_ma_asbaha",
            category = AdhkarCategory.MORNING,
            title = "اللهم ما أصبح بي من نعمة",
            arabicText = "اللهم ما أصبح بي من نعمة أو بأحد من خلقك فمنك وحدك لا شريك لك، فلك الحمد ولك الشكر",
            targetCount = 1,
            sourceReference = "حصن المسلم — أبو داود ٤/٣١٨، والنسائي في عمل اليوم والليلة (٧)، وابن حبان"
        ),
        VerifiedDhikrTemplate(
            key = "evening_ma_asbaha",
            category = AdhkarCategory.EVENING,
            title = "اللهم ما أمسى بي من نعمة",
            arabicText = "اللهم ما أمسى بي من نعمة أو بأحد من خلقك فمنك وحدك لا شريك لك، فلك الحمد ولك الشكر",
            targetCount = 1,
            sourceReference = "حصن المسلم — أبو داود ٤/٣١٨، والنسائي في عمل اليوم والليلة (٧)، وابن حبان"
        ),
        VerifiedDhikrTemplate(
            key = "morning_afini",
            category = AdhkarCategory.MORNING,
            title = "اللهم عافني في بدني",
            arabicText = "اللهم عافني في بَدَني، اللهم عافني في سمعي، اللهم عافني في بصري، لا إله إلا أنت.اللهم إني أعوذ بك من الكفر، والفقر، اللهم إني أعوذ بك من عذاب القبر لا إله إلا أنت",
            targetCount = 3,
            sourceReference = "حصن المسلم — أبو داود ٤/٣٢٤، وأحمد ٥/٤٢، والنسائي في عمل اليوم والليلة (٢٢)"
        ),
        VerifiedDhikrTemplate(
            key = "evening_afini",
            category = AdhkarCategory.EVENING,
            title = "اللهم عافني في بدني",
            arabicText = "اللهم عافني في بَدَني، اللهم عافني في سمعي، اللهم عافني في بصري، لا إله إلا أنت.اللهم إني أعوذ بك من الكفر، والفقر، اللهم إني أعوذ بك من عذاب القبر لا إله إلا أنت",
            targetCount = 3,
            sourceReference = "حصن المسلم — أبو داود ٤/٣٢٤، وأحمد ٥/٤٢، والنسائي في عمل اليوم والليلة (٢٢)"
        ),
        VerifiedDhikrTemplate(
            key = "morning_hasbiya",
            category = AdhkarCategory.MORNING,
            title = "حسبي الله لا إله إلا هو",
            arabicText = "حسبي الله لا إله إلا هو عليه توكلت وهو رب العرش العظيم",
            targetCount = 7,
            sourceReference = "حصن المسلم — ابن السني (٧١)، وأبو داود ٤/٣٢١ موقوفاً"
        ),
        VerifiedDhikrTemplate(
            key = "evening_hasbiya",
            category = AdhkarCategory.EVENING,
            title = "حسبي الله لا إله إلا هو",
            arabicText = "حسبي الله لا إله إلا هو عليه توكلت وهو رب العرش العظيم",
            targetCount = 7,
            sourceReference = "حصن المسلم — ابن السني (٧١)، وأبو داود ٤/٣٢١ موقوفاً"
        ),
        VerifiedDhikrTemplate(
            key = "morning_afw",
            category = AdhkarCategory.MORNING,
            title = "اللهم إني أسألك العفو والعافية",
            arabicText = "اللهم إني أسألك العفو والعافية في الدنيا والآخرة، اللهم إني أسألك العفو والعافية في ديني ودنياي وأهلي ومالي، اللهم استر عوراتي وآمن روعاتي، اللهم احفظني من بين يديَّ ومن خلفي وعن يميني وعن شمالي ومن فوقي وأعوذ بعظمتك أن أغتال من تحتي",
            targetCount = 1,
            sourceReference = "حصن المسلم — أبو داود وابن ماجه"
        ),
        VerifiedDhikrTemplate(
            key = "evening_afw",
            category = AdhkarCategory.EVENING,
            title = "اللهم إني أسألك العفو والعافية",
            arabicText = "اللهم إني أسألك العفو والعافية في الدنيا والآخرة، اللهم إني أسألك العفو والعافية في ديني ودنياي وأهلي ومالي، اللهم استر عوراتي وآمن روعاتي، اللهم احفظني من بين يديَّ ومن خلفي وعن يميني وعن شمالي ومن فوقي وأعوذ بعظمتك أن أغتال من تحتي",
            targetCount = 1,
            sourceReference = "حصن المسلم — أبو داود وابن ماجه"
        ),
        VerifiedDhikrTemplate(
            key = "morning_alim_ghayb",
            category = AdhkarCategory.MORNING,
            title = "اللهم عالم الغيب والشهادة",
            arabicText = "اللهم عَالِمَ الغيب والشَّهادة، فاطر السموات والأرض، رب كل شيء ومليكه، أشهد أن لا إله إلا أنت أعوذ بك من شر نفسي ومن شر الشيطان وشركه وأن اقترف على نفسي سوءًا أو أجُره إلى مسلم",
            targetCount = 1,
            sourceReference = "حصن المسلم — الترمذي وأبو داود"
        ),
        VerifiedDhikrTemplate(
            key = "evening_alim_ghayb",
            category = AdhkarCategory.EVENING,
            title = "اللهم عالم الغيب والشهادة",
            arabicText = "اللهم عَالِمَ الغيب والشَّهادة، فاطر السموات والأرض، رب كل شيء ومليكه، أشهد أن لا إله إلا أنت أعوذ بك من شر نفسي ومن شر الشيطان وشركه وأن اقترف على نفسي سوءًا أو أجُره إلى مسلم",
            targetCount = 1,
            sourceReference = "حصن المسلم — الترمذي وأبو داود"
        ),
        VerifiedDhikrTemplate(
            key = "morning_radheetu",
            category = AdhkarCategory.MORNING,
            title = "الرضا بالله ربّاً",
            arabicText = "رضيت بالله رباً، وبالإسلام ديناً وبمحمد صلى الله عليه وسلم نبياً",
            targetCount = 3,
            sourceReference = "حصن المسلم — أحمد ٤/٣٣٧، والنسائي في عمل اليوم والليلة (٤)، وأبو داود ٤/٤١٨، والترمذي ٥/٤٦٥"
        ),
        VerifiedDhikrTemplate(
            key = "evening_radheetu",
            category = AdhkarCategory.EVENING,
            title = "الرضا بالله ربّاً",
            arabicText = "رضيت بالله رباً، وبالإسلام ديناً وبمحمد صلى الله عليه وسلم نبياً",
            targetCount = 3,
            sourceReference = "حصن المسلم — أحمد ٤/٣٣٧، والنسائي في عمل اليوم والليلة (٤)، وأبو داود ٤/٤١٨، والترمذي ٥/٤٦٥"
        ),
        VerifiedDhikrTemplate(
            key = "morning_ya_hayy",
            category = AdhkarCategory.MORNING,
            title = "يا حي يا قيوم برحمتك أستغيث",
            arabicText = "يا حي يا قيوم برحمتك أستغيث أصلح لي شأني كُله ولا تَكِلْني إلى نفسي طرفة عين",
            targetCount = 1,
            sourceReference = "حصن المسلم — الحاكم ١/٥٤٥"
        ),
        VerifiedDhikrTemplate(
            key = "evening_ya_hayy",
            category = AdhkarCategory.EVENING,
            title = "يا حي يا قيوم برحمتك أستغيث",
            arabicText = "يا حي يا قيوم برحمتك أستغيث أصلح لي شأني كُله ولا تَكِلْني إلى نفسي طرفة عين",
            targetCount = 1,
            sourceReference = "حصن المسلم — الحاكم ١/٥٤٥"
        ),
        VerifiedDhikrTemplate(
            key = "morning_fath_alyawm",
            category = AdhkarCategory.MORNING,
            title = "اللهم إني أسألك خير هذا اليوم",
            arabicText = "أصبحنا وأصبح الملك لله رب العالمين، اللهم إني أسألك خير هذا اليوم: فتحه، ونصره، ونوره، وبركته، وهداه، وأعوذ بك من شر ما فيه وشر ما بعده",
            targetCount = 1,
            sourceReference = "حصن المسلم — أبو داود ٤/٣٢٢"
        ),
        VerifiedDhikrTemplate(
            key = "evening_fath_alyawm",
            category = AdhkarCategory.EVENING,
            title = "اللهم إني أسألك خير هذه الليلة",
            arabicText = "أمسينا وأمسى الملك لله رب العالمين، اللهم إني أسألك خير هذه الليلة: فتحها، ونصرها، ونورها، وبركتها، وهداها، وأعوذ بك من شر ما فيها وشر ما بعدها",
            targetCount = 1,
            sourceReference = "حصن المسلم — أبو داود ٤/٣٢٢"
        ),
        VerifiedDhikrTemplate(
            key = "morning_fitrat_islam",
            category = AdhkarCategory.MORNING,
            title = "أصبحنا على فطرة الإسلام",
            arabicText = "أصبحنا على فطرة الإسلام وعلى كلمة الإخلاص، وعلى دين نبيَّنا محمد صلى الله عليه وسلم وعلى ملَّة أبينا إبراهيم حنيفاً مسلماً وما كان من المشركين",
            targetCount = 1,
            sourceReference = "حصن المسلم — أحمد ٣/٤٠٦ و٤٠٧، وابن السني في عمل اليوم والليلة (٣٤)"
        ),
        VerifiedDhikrTemplate(
            key = "evening_fitrat_islam",
            category = AdhkarCategory.EVENING,
            title = "أمسينا على فطرة الإسلام",
            arabicText = "أمسينا على فطرة الإسلام وعلى كلمة الإخلاص، وعلى دين نبيَّنا محمد صلى الله عليه وسلم وعلى ملَّة أبينا إبراهيم حنيفاً مسلماً وما كان من المشركين",
            targetCount = 1,
            sourceReference = "حصن المسلم — أحمد ٣/٤٠٦ و٤٠٧، وابن السني في عمل اليوم والليلة (٣٤)"
        ),
        VerifiedDhikrTemplate(
            key = "morning_tahlil_ten",
            category = AdhkarCategory.MORNING,
            title = "التهليل (عشر مرات)",
            arabicText = "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير",
            targetCount = 10,
            sourceReference = "حصن المسلم — النسائي في عمل اليوم والليلة (٢٤)، وأبو داود ٤/٣١٩ (مرة واحدة عند الكسل)"
        ),
        VerifiedDhikrTemplate(
            key = "evening_tahlil_ten",
            category = AdhkarCategory.EVENING,
            title = "التهليل (عشر مرات)",
            arabicText = "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير",
            targetCount = 10,
            sourceReference = "حصن المسلم — النسائي في عمل اليوم والليلة (٢٤)، وأبو داود ٤/٣١٩ (مرة واحدة عند الكسل)"
        ),
        VerifiedDhikrTemplate(
            key = "morning_tahlil_100",
            category = AdhkarCategory.MORNING,
            title = "التهليل (مائة مرة)",
            arabicText = "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير",
            targetCount = 100,
            sourceReference = "حصن المسلم — البخاري ٤/٩٥، ومسلم ٤/٢٠٧١"
        ),
        VerifiedDhikrTemplate(
            key = "morning_adada_khalqihi",
            category = AdhkarCategory.MORNING,
            title = "سبحان الله وبحمده عدد خلقه",
            arabicText = "سبحان الله وبحمده عدد خلقهِ ورِضَا نفسِهِ وزِنُة عَرشِهِ ومِداد كلماته",
            targetCount = 3,
            sourceReference = "حصن المسلم — مسلم ٤/٢٠٩٠"
        ),
        VerifiedDhikrTemplate(
            key = "morning_ilman_nafia",
            category = AdhkarCategory.MORNING,
            title = "اللهم إني أسألك علماً نافعاً",
            arabicText = "اللهم إني أسألك علماً نافعاً، ورزقاً طيباً، وعملاً متقبلاً",
            targetCount = 1,
            sourceReference = "حصن المسلم — ابن ماجه (٩٢٥)، وابن السني في عمل اليوم والليلة (٥٤)"
        ),
        VerifiedDhikrTemplate(
            key = "evening_kalimat_tammat",
            category = AdhkarCategory.EVENING,
            title = "أعوذ بكلمات الله التامات",
            arabicText = "أعوذ بكلمات الله التامات من شر ما خلق",
            targetCount = 3,
            sourceReference = "حصن المسلم — أحمد ٢/٢٩٠، والنسائي في عمل اليوم والليلة (٥٩٠)"
        ),
        VerifiedDhikrTemplate(
            key = "morning_salat_nabi",
            category = AdhkarCategory.MORNING,
            title = "الصلاة والسلام على النبي ﷺ",
            arabicText = "اللهم صل وسلم على نبينا محمد",
            targetCount = 10,
            sourceReference = "حصن المسلم — الطبراني، وانظر صحيح الترغيب والترهيب ١/٢٧٣"
        ),
        VerifiedDhikrTemplate(
            key = "evening_salat_nabi",
            category = AdhkarCategory.EVENING,
            title = "الصلاة والسلام على النبي ﷺ",
            arabicText = "اللهم صل وسلم على نبينا محمد",
            targetCount = 10,
            sourceReference = "حصن المسلم — الطبراني، وانظر صحيح الترغيب والترهيب ١/٢٧٣"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_muawwidhat",
            category = AdhkarCategory.SLEEP,
            title = "الإخلاص والمعوذتان عند النوم",
            arabicText = "يجمع كفيه ثم ينفث فيهما فيقرأ: بسم الله الرحمن الرحيم ﴿قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُواً أَحَدٌ﴾ بسم الله الرحمن الرحيم ﴿قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِن شَرِّ مَا خَلَقَ ۝ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ﴾ بسم الله الرحمن الرحيم ﴿قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَهِ النَّاسِ ۝ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ﴾ ثم يمسح بهما ما استطاع من جسده يبدأ بهما على رأسه ووجهه وما أقبل من جسده",
            targetCount = 3,
            sourceReference = "حصن المسلم — البخاري مع الفتح ٩/٦٢، ومسلم ٤/١٧٢٣"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_baqarah_end",
            category = AdhkarCategory.SLEEP,
            title = "آخر آيتين من سورة البقرة",
            arabicText = "﴿آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ مِن رَّبِّهِ وَالْمُؤْمِنُونَ كُلٌّ آمَنَ بِاللّهِ وَمَلآئِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ لاَ نُفَرِّقُ بَيْنَ أَحَدٍ مِّن رُّسُلِهِ وَقَالُواْ سَمِعْنَا وَأَطَعْنَا غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيرُ ۝ لاَ يُكَلِّفُ اللّهُ نَفْساً إِلاَّ وُسْعَهَا لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ رَبَّنَا لاَ تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا رَبَّنَا وَلاَ تَحْمِلْ عَلَيْنَا إِصْراً كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِن قَبْلِنَا رَبَّنَا وَلاَ تُحَمِّلْنَا مَا لاَ طَاقَةَ لَنَا بِهِ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا أَنتَ مَوْلاَنَا فَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ﴾",
            targetCount = 1,
            sourceReference = "حصن المسلم — البخاري مع الفتح ٩/٩٤، ومسلم ١/٥٥٤ (سورة البقرة ٢٨٥–٢٨٦)"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_bismika_rabbi",
            category = AdhkarCategory.SLEEP,
            title = "باسمك ربي وضعت جنبي",
            arabicText = "باسمك ربي وضعت جنبي وبك أرفعه إن أمسكت نفسي فارحمها، وإن أرسلتها فاحفظها بما تحفظ به عبادك الصالحين",
            targetCount = 1,
            sourceReference = "حصن المسلم — البخاري ١١/١٢٦، ومسلم ٤/٢٠٨٤"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_khalaqta_nafsi",
            category = AdhkarCategory.SLEEP,
            title = "اللهم خلقت نفسي وأنت توفاها",
            arabicText = "اللهم خَلَقْتَ نفسي وأنت توفَّاها لك مماتها ومَحْياها، إن أحييتها فاحفظها وإن أمتها فاغفر لها، اللهم إني أسألك العافية",
            targetCount = 1,
            sourceReference = "حصن المسلم — مسلم ٤/٢٠٨٣، وأحمد ٢/٧٩"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_qini_adhabak",
            category = AdhkarCategory.SLEEP,
            title = "اللهم قني عذابك يوم تبعث عبادك",
            arabicText = "اللهم قني عذابك يوم تبعث عبادك",
            targetCount = 3,
            sourceReference = "حصن المسلم — أبو داود ٤/٣١١، وانظر صحيح الترمذي ٣/١٤٣"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_rabb_samawat",
            category = AdhkarCategory.SLEEP,
            title = "اللهم رب السموات السبع",
            arabicText = "اللهم ربَّ السموات السبع، ورب العرش العظيم، ربنا ورب كل شيء، فالق الحب والنوى، ومُنزل التوراة والإنجيل والفرقان، أعوذ بك من شر كل شيء أنت آخذٌ بناصيته، اللهم أنت الأول فليس قبلك شيء، وأنت الآخر فليس بعدك شيء، وأنت الظاهر فليس فوقك شيء، وأنت الباطن فليس دونك شيء، اقض عنا الدَّيْنَ واغننا من الفقر",
            targetCount = 1,
            sourceReference = "حصن المسلم — مسلم ٤/٢٠٨٤"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_alhamdu_at3amana",
            category = AdhkarCategory.SLEEP,
            title = "الحمد لله الذي أطعمنا وسقانا",
            arabicText = "الحمد لله الذي أطعمنا وسقانا وكفانا وآوانا فكم ممن لا كافي له ولا مؤوي",
            targetCount = 1,
            sourceReference = "حصن المسلم — مسلم ٤/٢٠٨٥"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_alim_ghayb",
            category = AdhkarCategory.SLEEP,
            title = "اللهم عالم الغيب والشهادة",
            arabicText = "اللهم عالم الغيب والشهادة، فاطر السموات والأرض، رب كل شيء ومليكه أشهد أن لا إله إلا أنت أعوذ بك من شر نفسي، ومن شر الشيطان وشركه وأن أقترف على نفسي سوءًا أو أجرَّه إلى مسلم",
            targetCount = 1,
            sourceReference = "حصن المسلم — أبو داود ٤/٣١٧، وانظر صحيح الترمذي ٣/١٤٢"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_sajda_mulk",
            category = AdhkarCategory.SLEEP,
            title = "سورتا السجدة والملك",
            arabicText = "يقرأ: ﴿ألم﴾ تنزيل السجدة، و ﴿تبارك الذي بيده الملك﴾",
            targetCount = 1,
            sourceReference = "حصن المسلم — الترمذي والنسائي، وانظر صحيح الجامع ٤/٢٥٥"
        ),
        VerifiedDhikrTemplate(
            key = "sleep_aslamtu",
            category = AdhkarCategory.SLEEP,
            title = "اللهم أسلمت نفسي إليك",
            arabicText = "اللهم أسلمت نفسي إليك، وفوضت أمري إليك وألجأت ظهري إليك رهبةً ورغبةً إليك، لا ملجأ ولا منجا منك إلا إليك، آمنت بكتابك الذي أنزلت وبنبيك الذي أرسلت",
            targetCount = 1,
            sourceReference = "حصن المسلم — البخاري مع الفتح ١١/١١٣، ومسلم ٤/٢٠٨١"
        ),
        VerifiedDhikrTemplate(
            key = "wakeup_afani",
            category = AdhkarCategory.WAKEUP,
            title = "الحمد لله الذي عافاني في جسدي",
            arabicText = "الحَمْدُ لله الذِي عَافَانِي في جَسَدِي ورَدَّ عَلَيَّ رُوحِي، وأَذِنَ لي بِذِكْرهِ",
            targetCount = 1,
            sourceReference = "حصن المسلم — الترمذي ٥/٤٧٣، وانظر صحيح الترمذي ٣/١٤٤"
        ),
        VerifiedDhikrTemplate(
            key = "wakeup_tawwar",
            category = AdhkarCategory.WAKEUP,
            title = "ذكر من تعارّ من الليل",
            arabicText = "قال رسول الله صلى الله عليه وسلم مَنْ تَعَارَ مِنَ اللَّيْل فقال حين يستيقظ: لا إلَهَ إلاَّ الله وحْدَهُ لا شَرِيكَ لَهُ، لَهُ المُلْكُ ولَهُ الحَمْدُ وهُوَ على كلِّ شيءٍ قَدير، سُبْحانَ الله والحَمْدُ لله ولا إله إلا الله والله أكبر ولا حَولَ ولا قُوةَ إلا بالله العلي العظيم ثم دعا: اللَّهُمَّ اغْفِرْ لي، غُفِرَ له، قال الوليد: أو قال: دعا استُجيبَ لهُ، فإن قام فتوضأَ ثم صَلّى قُبِلَتْ صَلاتُهُ.",
            targetCount = 1,
            sourceReference = "حصن المسلم — البخاري مع الفتح ٣/١٤٤، وابن ماجه، وانظر صحيح ابن ماجه ٢/٣٣٥"
        ),
        VerifiedDhikrTemplate(
            key = "wakeup_imran",
            category = AdhkarCategory.WAKEUP,
            title = "آيات آل عمران (١٩٠–٢٠٠)",
            arabicText = "﴿إِنَّ فِي خَلْقِ السَّمَاوَاتِ وَالأَرْضِ وَاخْتِلاَفِ اللَّيْلِ وَالنَّهَارِ لآيَاتٍ لِّأُوْلِي الألْبَابِ ۝ الَّذِينَ يَذْكُرُونَ اللّهَ قِيَاماً وَقُعُوداً وَعَلَىَ جُنُوبِهِمْ وَيَتَفَكَّرُونَ فِي خَلْقِ السَّمَاوَاتِ وَالأَرْضِ رَبَّنَا مَا خَلَقْتَ هَذا بَاطِلاً سُبْحَانَكَ فَقِنَا عَذَابَ النَّارِ ۝ رَبَّنَا إِنَّكَ مَن تُدْخِلِ النَّارَ فَقَدْ أَخْزَيْتَهُ وَمَا لِلظَّالِمِينَ مِنْ أَنصَارٍ ۝ رَّبَّنَا إِنَّنَا سَمِعْنَا مُنَادِياً يُنَادِي لِلإِيمَانِ أَنْ آمِنُواْ بِرَبِّكُمْ فَآمَنَّا رَبَّنَا فَاغْفِرْ لَنَا ذُنُوبَنَا وَكَفِّرْ عَنَّا سَيِّئَاتِنَا وَتَوَفَّنَا مَعَ الأبْرَارِ ۝ رَبَّنَا وَآتِنَا مَا وَعَدتَّنَا عَلَى رُسُلِكَ وَلاَ تُخْزِنَا يَوْمَ الْقِيَامَةِ إِنَّكَ لاَ تُخْلِفُ الْمِيعَادَ ۝ فَاسْتَجَابَ لَهُمْ رَبُّهُمْ أَنِّي لاَ أُضِيعُ عَمَلَ عَامِلٍ مِّنكُم مِّن ذَكَرٍ أَوْ أُنثَى بَعْضُكُم مِّن بَعْضٍ فَالَّذِينَ هَاجَرُواْ وَأُخْرِجُواْ مِن دِيَارِهِمْ وَأُوذُواْ فِي سَبِيلِي وَقَاتَلُواْ وَقُتِلُواْ لأُكَفِّرَنَّ عَنْهُمْ سَيِّئَاتِهِمْ وَلأُدْخِلَنَّهُمْ جَنَّاتٍ تَجْرِي مِن تَحْتِهَا الأَنْهَارُ ثَوَاباً مِّن عِندِ اللّهِ وَاللّهُ عِندَهُ حُسْنُ الثَّوَابِ ۝ لاَ يَغُرَّنَّكَ تَقَلُّبُ الَّذِينَ كَفَرُواْ فِي الْبِلاَدِ ۝ مَتَاعٌ قَلِيلٌ ثُمَّ مَأْوَاهُمْ جَهَنَّمُ وَبِئْسَ الْمِهَادُ ۝ لَكِنِ الَّذِينَ اتَّقَوْاْ رَبَّهُمْ لَهُمْ جَنَّاتٌ تَجْرِي مِن تَحْتِهَا الأَنْهَارُ خَالِدِينَ فِيهَا نُزُلاً مِّنْ عِندِ اللّهِ وَمَا عِندَ اللّهِ خَيْرٌ لِّلأَبْرَارِ ۝ وَإِنَّ مِنْ أَهْلِ الْكِتَابِ لَمَن يُؤْمِنُ بِاللّهِ وَمَا أُنزِلَ إِلَيْكُمْ وَمَا أُنزِلَ إِلَيْهِمْ خَاشِعِينَ لِلّهِ لاَ يَشْتَرُونَ بِآيَاتِ اللّهِ ثَمَناً قَلِيلاً أُوْلَئِكَ لَهُمْ أَجْرُهُمْ عِندَ رَبِّهِمْ إِنَّ اللّهَ سَرِيعُ الْحِسَابِ ۝ يَا أَيُّهَا الَّذِينَ آمَنُواْ اصْبِرُواْ وَصَابِرُواْ وَرَابِطُواْ وَاتَّقُواْ اللّهَ لَعَلَّكُمْ تُفْلِحُونَ﴾",
            targetCount = 1,
            sourceReference = "حصن المسلم — سورة آل عمران ١٩٠–٢٠٠، البخاري مع الفتح ٨/٢٣٨، ومسلم ١/٥٣٠"
        )
    )
}
