package com.rafiqaldhikr.app.core.stats

import com.rafiqaldhikr.app.core.model.ActivityCategory
import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.core.planner.PlannerLog
import com.rafiqaldhikr.app.core.yearlog.YearLogInputs
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity
import com.rafiqaldhikr.app.data.local.db.entities.PrayerLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WeeklySummaryTest {
    private val start = 20_000L

    private fun prayer(day: Long, key: String) =
        PrayerLogEntity(profileId = 1, dateEpochDay = day, itemKey = key, completedAtMillis = 0)

    private fun log(day: Long, activityId: Long, status: ActivityStatus) = ActivityLogEntity(
        profileId = 1, activityId = activityId, activityTitleSnapshot = "x", categorySnapshot = ActivityCategory.CUSTOM,
        dateEpochDay = day, scheduledAtMillis = 0, status = status
    )

    @Test fun countsOnlyTheWeekAndSplitsPrayers() {
        val input = YearLogInputs(
            prayerLogs = listOf(
                prayer(start, "FAJR"), prayer(start, "DHUHR"), prayer(start + 6, "ISHA"),
                prayer(start, "DUHA"), prayer(start - 1, "FAJR"), prayer(start + 7, "FAJR")
            )
        )
        val s = WeeklySummary.compute(start, input, emptySet(), 0, 0)
        assertEquals(3, s.fardPrayers)
        assertEquals(1, s.sunnahPrayers)
    }

    @Test fun plannerLogsAreLeftOutOfActivityCounts() {
        val input = YearLogInputs(
            activityLogs = listOf(
                log(start, 1, ActivityStatus.COMPLETED),
                log(start + 1, 2, ActivityStatus.COMPLETED), // planner mirror
                log(start + 2, 1, ActivityStatus.MISSED)
            ),
            fastingDays = listOf(
                FastingDayEntity(profileId = 1, dateEpochDay = start + 3, isCompleted = true)
            ),
            tasbeehLogs = listOf(TasbeehLogEntity(profileId = 1, tasbeehTypeId = 1, dateEpochDay = start, count = 33))
        )
        val s = WeeklySummary.compute(start, input, setOf(2L), plannerDone = 4, plannerPlanned = 6)
        assertEquals(1, s.activitiesDone)
        assertEquals(1, s.activitiesMissed)
        assertEquals(1, s.fastingDays)
        assertEquals(33, s.tasbeehTotal)
        assertEquals(4, s.plannerDone)
        assertEquals(6, s.plannerPlanned)
    }

    @Test fun plannerKeyIsStablePerNameAndType() {
        assertEquals(PlannerLog.templateKey("QURAN", "ورد  القرآن"), PlannerLog.templateKey("QURAN", " ورد القرآن "))
        assertNotEquals(PlannerLog.templateKey("QURAN", "ورد"), PlannerLog.templateKey("DHIKR", "ورد"))
        assertEquals(ActivityCategory.QURAN, PlannerLog.category("QURAN"))
        assertEquals(ActivityCategory.CUSTOM, PlannerLog.category("???"))
    }
}
