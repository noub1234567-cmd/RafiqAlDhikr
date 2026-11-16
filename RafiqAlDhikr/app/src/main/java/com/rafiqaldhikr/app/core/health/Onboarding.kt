package com.rafiqaldhikr.app.core.health

import android.content.Context
import android.os.Build

object Onboarding {
    private const val PREFS = "onboarding"
    private const val KEY_DONE = "done"
    private const val KEY_BATTERY_HINT_DISMISSED = "battery_hint_dismissed"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isDone(context: Context): Boolean = prefs(context).getBoolean(KEY_DONE, false)

    fun setDone(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_DONE, value).apply()
    }

    /** The battery warning is advice, so the user may dismiss it; the others cannot be dismissed. */
    fun isBatteryHintDismissed(context: Context): Boolean =
        prefs(context).getBoolean(KEY_BATTERY_HINT_DISMISSED, false)

    fun dismissBatteryHint(context: Context) {
        prefs(context).edit().putBoolean(KEY_BATTERY_HINT_DISMISSED, true).apply()
    }

    /** Phone-maker specific advice. Menu names differ a little between system versions. */
    fun oemTips(manufacturer: String = Build.MANUFACTURER): Pair<String, String> {
        val m = manufacturer.lowercase()
        return when {
            m.contains("realme") || m.contains("oppo") || m.contains("oneplus") ->
                "هاتف Realme / OPPO / OnePlus" to
                    "من الإعدادات ← التطبيقات ← رفيقك في الذكر ← استخدام البطارية: اسمح بالنشاط في الخلفية وبالتشغيل التلقائي. " +
                    "ثم اقفل التطبيق في قائمة التطبيقات الأخيرة (اضغط مطولاً على بطاقته ثم «قفل»)، فسحبه دون قفل قد يوقف تذكيراته."
            m.contains("xiaomi") || m.contains("redmi") || m.contains("poco") ->
                "هاتف Xiaomi / Redmi / POCO" to
                    "فعّل «التشغيل التلقائي» (Autostart) للتطبيق من الأمان ← الأذونات، وفي إعدادات بطارية التطبيق اختر «بلا قيود»، " +
                    "ثم اقفل التطبيق في قائمة التطبيقات الأخيرة."
            m.contains("huawei") || m.contains("honor") ->
                "هاتف Huawei / Honor" to
                    "من إعدادات البطارية ← تشغيل التطبيقات: عطّل «الإدارة التلقائية» للتطبيق وفعّل التشغيل التلقائي والتشغيل في الخلفية."
            m.contains("samsung") ->
                "هاتف Samsung" to
                    "من الإعدادات ← البطارية ← حدود استخدام الخلفية: أزل التطبيق من «التطبيقات النائمة» و«التطبيقات الخاملة»."
            m.contains("vivo") || m.contains("iqoo") ->
                "هاتف vivo / iQOO" to
                    "اسمح للتطبيق بالعمل في الخلفية وبالتشغيل التلقائي من إعدادات البطارية وإدارة التطبيقات، ثم اقفل التطبيق في قائمة التطبيقات الأخيرة."
            else ->
                "هاتفك" to
                    "إن لاحظتَ تأخر التذكيرات بعد إغلاق التطبيق، ابحث في إعدادات البطارية عن خيار يمنع النظام من إيقاف التطبيق في الخلفية، وفعّل التشغيل التلقائي إن وُجد."
        }
    }
}
