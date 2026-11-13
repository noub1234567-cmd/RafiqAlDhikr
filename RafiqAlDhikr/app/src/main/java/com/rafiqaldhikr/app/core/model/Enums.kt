package com.rafiqaldhikr.app.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode(val labelAr: String) {
    SYSTEM("تلقائي (حسب النظام)"),
    LIGHT("الوضع الفاتح"),
    DARK("الوضع الداكن")
}

@Serializable
enum class AdhkarCategory(val labelAr: String, val defaultHour: Int, val defaultMinute: Int) {
    MORNING("أذكار الصباح", 6, 0),
    EVENING("أذكار المساء", 16, 30),
    SLEEP("أذكار النوم", 22, 0),
    WAKEUP("أذكار الاستيقاظ", 5, 30),
    GENERAL("أذكار يومية عامة", 12, 30)
}

@Serializable
enum class ActivityCategory(val labelAr: String) {
    ADHKAR("الأذكار"),
    QURAN("القرآن الكريم"),
    MEMORIZATION("الحفظ والمراجعة"),
    PRAYER_SUNNAH("الصلوات والنوافل"),
    FASTING("الصيام"),
    CHARITY_TIES("الصدقة وصلة الرحم"),
    FRIDAY("سنن الجمعة"),
    SEASONAL("العبادات الموسمية"),
    CUSTOM("عبادة مخصصة")
}

@Serializable
enum class ScheduleType(val labelAr: String) {
    DAILY("يومي"),
    WEEKLY("أسبوعي"),
    SPECIFIC_WEEKDAYS("أيام محددة من الأسبوع"),
    ONE_TIME("مرة واحدة بتاريخ محدد"),
    CUSTOM_INTERVAL("تكرار كل عدة أيام")
}

@Serializable
enum class ActivityStatus(val labelAr: String) {
    PENDING("قيد الانتظار"),
    COMPLETED("مكتمل"),
    MISSED("فائت"),
    RESCHEDULED("مُعاد جدولته")
}

@Serializable
enum class GoalPeriod(val labelAr: String) {
    DAILY("هدف يومي"),
    WEEKLY("هدف أسبوعي")
}

@Serializable
enum class QuranGoalType(val labelAr: String, val unitAr: String) {
    PAGES("صفحات يومية", "صفحة"),
    HIZB("أحزاب يومية", "حزب"),
    JUZ("أجزاء يومية", "جزء"),
    CUSTOM("هدف مخصص", "وحدة")
}

@Serializable
enum class FastingOccasion(val labelAr: String) {
    MONDAY_THURSDAY("الإثنين والخميس"),
    WHITE_DAYS("الأيام البيض (١٣، ١٤، ١٥)"),
    ARAFAH("يوم عرفة (٩ ذو الحجة)"),
    ASHURA("عاشوراء (١٠ محرم)"),
    SHAWWAL_SIX("ست من شوال"),
    VOLUNTARY("صيام تطوع")
}

@Serializable
enum class NotificationSoundMode(val labelAr: String) {
    NORMAL("إشعار عادي"),
    GENTLE_SOUND("صوت هادئ")
}
