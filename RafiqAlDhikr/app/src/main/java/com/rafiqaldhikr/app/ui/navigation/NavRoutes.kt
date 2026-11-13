package com.rafiqaldhikr.app.ui.navigation

sealed class NavRoute(val route: String, val labelAr: String) {
    data object Dashboard : NavRoute("dashboard", "الرئيسية")
    data object Adhkar : NavRoute("adhkar", "الأذكار")
    data object Quran : NavRoute("quran", "القرآن")
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
    data object Settings : NavRoute("settings", "الإعدادات")
}
