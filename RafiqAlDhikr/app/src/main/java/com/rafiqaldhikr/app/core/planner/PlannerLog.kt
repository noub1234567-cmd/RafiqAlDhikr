package com.rafiqaldhikr.app.core.planner

import com.rafiqaldhikr.app.core.model.ActivityCategory

/**
 * A planner slot marked "تمّت" is mirrored into the normal activity log so statistics, streaks and the
 * year log count it. The log hangs on a hidden activity (template key "sys-planner-…") per worship name.
 */
object PlannerLog {
    const val KEY_PREFIX = "sys-planner-"

    private fun normalize(title: String) = title.trim().replace(Regex("\\s+"), " ")

    fun templateKey(type: String, title: String): String =
        KEY_PREFIX + PlannerType.fromKey(type).name + "-" + Integer.toHexString(normalize(title).hashCode())

    fun category(type: String): ActivityCategory = when (PlannerType.fromKey(type)) {
        PlannerType.PRAYER -> ActivityCategory.PRAYER_SUNNAH
        PlannerType.QURAN -> ActivityCategory.QURAN
        PlannerType.DHIKR, PlannerType.DUA -> ActivityCategory.ADHKAR
        PlannerType.CHARITY, PlannerType.KINSHIP -> ActivityCategory.CHARITY_TIES
        PlannerType.FASTING -> ActivityCategory.FASTING
        PlannerType.KNOWLEDGE, PlannerType.OTHER -> ActivityCategory.CUSTOM
    }
}
