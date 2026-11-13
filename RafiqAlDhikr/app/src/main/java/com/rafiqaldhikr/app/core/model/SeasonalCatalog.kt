package com.rafiqaldhikr.app.core.model

data class SeasonalWorshipItem(
    val id: String,
    val title: String,
    val timeDescription: String,
    val guidanceText: String,
    val suggestedActivities: List<String>
)

object SeasonalCatalog {
    val seasons: List<SeasonalWorshipItem> = listOf(
        SeasonalWorshipItem(
            id = "ramadan",
            title = "شهر رمضان المبارك",
            timeDescription = "الشهر التاسع من السنة الهجرية",
            guidanceText = "موسم عظيم للصيام والقيام وتلاوة القرآن والإنفاق وإفطار الصائمين، ويُعان فيه المسلم على تهذيب نفسه والإكثار من الطاعات.",
            suggestedActivities = listOf(
                "تلاوة ورد يومي متزايد من القرآن الكريم",
                "المحافظة على صلاة التراويح والقيام",
                "تحري الدعاء عند الفطر وفي الأسحار",
                "الصدقة اليومية ولو بالقليل"
            )
        ),
        SeasonalWorshipItem(
            id = "last_ten_ramadan",
            title = "العشر الأواخر من رمضان",
            timeDescription = "من ليلة ٢١ إلى نهاية شهر رمضان",
            guidanceText = "أيام وليالٍ فاضلة يُستحب فيها الاجتهاد في قيام الليل والذكر والدعاء، ومنها تحري ليلة القدر.",
            suggestedActivities = listOf(
                "القيام والدعاء في الليالي العشر",
                "دعاء: اللهم إنك عفو تحب العفو فاعف عني (رواه الترمذي ٣٥١٣)",
                "الإكثار من تلاوة القرآن والاعتكاف المتاح"
            )
        ),
        SeasonalWorshipItem(
            id = "dhul_hijjah_ten",
            title = "عشر ذي الحجة",
            timeDescription = "الأيام العشرة الأولى من شهر ذي الحجة",
            guidanceText = "أيام فاضلة يُشرع فيها الإكثار من الأعمال الصالحة والذكر من التكبير والتهليل والتحميد، مع مراعاة سعة الاجتهاد حسب القدرة.",
            suggestedActivities = listOf(
                "الإكثار من التكبير والتحميد والتهليل",
                "صيام ما تيسر من الأيام الثمانية الأولى لغير الحاج",
                "الصدقة وصلة الرحم وتلاوة القرآن"
            )
        ),
        SeasonalWorshipItem(
            id = "arafah",
            title = "يوم عرفة",
            timeDescription = "التاسع من شهر ذي الحجة",
            guidanceText = "يوم عظيم الشأن، يُستحب صيامه لغير الحاج لما ورد في فضله، والإكثار فيه من الذكر والدعاء.",
            suggestedActivities = listOf(
                "صيام يوم عرفة (لغير الحاج)",
                "الإكثار من: لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير",
                "الاجتهاد في الدعاء بخشوع"
            )
        ),
        SeasonalWorshipItem(
            id = "ashura",
            title = "يوم عاشوراء",
            timeDescription = "العاشر من شهر الله المحرم",
            guidanceText = "يوم يُشرع صيامه شكراً لله تعالى، ويستحب عند كثير من أهل العلم صيام التاسع معه، مع مراعاة اختلاف تفاصيل الاستحباب في الصيام قبله أو بعده.",
            suggestedActivities = listOf(
                "صيام يوم عاشوراء (١٠ محرم)",
                "صيام اليوم التاسع من محرم لمن تيسر له",
                "التوبة الصادقة وكثرة الاستغفار"
            )
        ),
        SeasonalWorshipItem(
            id = "sacred_months",
            title = "الأشهر الحرم",
            timeDescription = "ذو القعدة، ذو الحجة، المحرم، ورجب",
            guidanceText = "أشهر عظّمها الله تعالى، يُذكّر المسلم فيها نفسه بتعظيم حرمات الله والبعد عن الظلم والمحافظة على الفرائض والنوافل.",
            suggestedActivities = listOf(
                "تعاهد الفرائض والسنن الرواتب",
                "تذكّر تجنب الظلم للنفس والآخرين",
                "صيام الأيام الفاضلة المعتادة كالإثنين والخميس والبيض"
            )
        ),
        SeasonalWorshipItem(
            id = "hajj_season",
            title = "موسم الحج",
            timeDescription = "أشهر الحج وعشر ذي الحجة وأيام التشريق",
            guidanceText = "موسم يستحضر فيه المسلم معاني التوحيد والتلبية والطاعة، ويجتهد غير الحاج في الذكر والدعاء والعمل الصالح.",
            suggestedActivities = listOf(
                "التكبير في أيام عشر ذي الحجة وأيام التشريق",
                "الدعاء للحجاج وللمسلمين بالقبول والتيسير",
                "إحياء معاني الشكر في يوم النحر وأيام التشريق"
            )
        )
    )
}
