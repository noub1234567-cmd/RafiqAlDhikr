package com.rafiqaldhikr.app.ui.navigation

sealed class NavRoute(val route: String, val labelAr: String) {
    data object Dashboard : NavRoute("dashboard", "الرئيسية")
    data object Adhkar : NavRoute("adhkar", "الأذكار")
    data object Quran : NavRoute("quran", "القرآن")
    data object DuaHub : NavRoute("dua_hub", "الأدعية")
    data object DuaSection : NavRoute("dua_section/{id}", "قسم الأدعية") {
        fun create(id: String) = "dua_section/$id"
    }
    data object DuaSearch : NavRoute("dua_search", "بحث في الأذكار والأدعية")
    data object DuaNow : NavRoute("dua_now", "ماذا أقول الآن؟")
    data object Favorites : NavRoute("favorites", "مفضلتي")
    data object NightReview : NavRoute("night_review", "محاسبة النفس")
    data object DuaMemo : NavRoute("dua_memo", "حفظ الأذكار والأحاديث")
    data object Jumua : NavRoute("jumua", "سنن الجمعة")
    data object DuaQuiz : NavRoute("dua_quiz", "اختبار الأذكار")
    data object Fasting : NavRoute("fasting", "الصيام")
    data object More : NavRoute("more", "المزيد")

    data object Memorization : NavRoute("memorization", "الحفظ والمراجعة")
    data object Tasbeeh : NavRoute("tasbeeh", "المسبحة")
    data object Dua : NavRoute("dua", "دفتر الأدعية")
    data object WorshipActs : NavRoute("worship_acts", "العبادات والأهداف")
    data object Seasons : NavRoute("seasons", "المواسم الإسلامية")
    data object History : NavRoute("history", "السجل")
    data object Statistics : NavRoute("statistics", "الإحصائيات")
    data object Profiles : NavRoute("profiles", "الملفات الشخصية")
    data object YearLog : NavRoute("year_log", "السجل السنوي")
    data object Ramadan : NavRoute("ramadan", "رمضان")
    data object Khatma : NavRoute("khatma", "ختمة القرآن")
    data object Planner : NavRoute("planner", "جدول العبادات الأسبوعي")
    data object PlannerReports : NavRoute("planner_reports", "تقارير الجدول")
    data object WeeklyReport : NavRoute("weekly_report", "تقرير الأسبوع")
    data object PrayerTracker : NavRoute("prayer_tracker", "متابعة الصلاة")
    data object Qibla : NavRoute("qibla", "اتجاه القبلة")
    data object Settings : NavRoute("settings", "الإعدادات")
}
