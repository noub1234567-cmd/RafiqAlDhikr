package com.rafiqaldhikr.app.core.planner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class PlannerWeekTest {
    @Test fun saturdayStartsTheWeek() {
        val sat = LocalDate.of(2026, 11, 14) // a Saturday
        assertEquals(sat, PlannerWeek.weekStart(sat))
        assertEquals(sat, PlannerWeek.weekStart(sat.plusDays(6))) // Friday
        assertEquals(sat.plusDays(7), PlannerWeek.weekStart(sat.plusDays(7)))
        assertEquals(sat, PlannerWeek.weekStart(LocalDate.of(2026, 11, 17))) // Tuesday
    }

    @Test fun dayIndexCountsFromSaturday() {
        val sat = LocalDate.of(2026, 11, 14)
        assertEquals(0, PlannerWeek.dayIndexOf(sat, sat))
        assertEquals(6, PlannerWeek.dayIndexOf(sat.plusDays(6), sat))
    }

    @Test fun triggerCombinesDayAndTime() {
        val zone = ZoneOffset.UTC
        val start = LocalDate.of(2026, 11, 14).toEpochDay()
        val t = PlannerWeek.triggerMillis(start, 2, 5 * 60 + 30, zone)!!
        assertEquals(
            LocalDate.of(2026, 11, 16).atTime(5, 30).toInstant(ZoneOffset.UTC).toEpochMilli(), t
        )
        assertNull(PlannerWeek.triggerMillis(start, 2, PlannerWeek.NO_TIME, zone))
        assertNull(PlannerWeek.triggerMillis(start, 7, 60, zone))
    }

    @Test fun nextBoundaryIsFollowingSaturdayJustAfterMidnight() {
        val zone: ZoneId = ZoneOffset.UTC
        val tue = LocalDate.of(2026, 11, 17).atTime(12, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        val expected = LocalDate.of(2026, 11, 21).atTime(0, 1).toInstant(ZoneOffset.UTC).toEpochMilli()
        assertEquals(expected, PlannerWeek.nextBoundaryMillis(tue, zone))
        // Exactly on a boundary moves to the next week.
        assertEquals(expected + 7L * 24 * 3600 * 1000, PlannerWeek.nextBoundaryMillis(expected, zone))
    }

    @Test fun rowsAreClamped() {
        assertEquals(1, PlannerWeek.clampRows(0))
        assertEquals(30, PlannerWeek.clampRows(99))
        assertEquals(6, PlannerWeek.clampRows(6))
    }

    @Test fun unknownTypeFallsBackToOther() {
        assertEquals(PlannerType.OTHER, PlannerType.fromKey("???"))
        assertEquals(PlannerType.QURAN, PlannerType.fromKey("QURAN"))
    }

    @Test fun reportGroupsByTitleAndType() {
        val lines = PlannerReports.summarize(
            listOf(
                Triple("ورد القرآن", "QURAN", true),
                Triple("ورد  القرآن ", "QURAN", false),
                Triple("ورد القرآن", "QURAN", true),
                Triple("صدقة", "CHARITY", true),
                Triple("   ", "OTHER", true)
            )
        )
        assertEquals(2, lines.size)
        assertEquals(ReportLine("ورد القرآن", "QURAN", 3, 2), lines[0])
        assertEquals(ReportLine("صدقة", "CHARITY", 1, 1), lines[1])
        assertTrue(PlannerReports.summarize(emptyList()).isEmpty())
    }

    @Test fun formatTime() {
        assertEquals("05:30", PlannerWeek.formatTime(330))
        assertEquals("", PlannerWeek.formatTime(-1))
    }
}

class PlannerMergeTest {
    @Test fun mergeSumsSameWorship() {
        val merged = PlannerReports.merge(
            listOf(
                listOf(ReportLine("ورد", "QURAN", 3, 2), ReportLine("صدقة", "CHARITY", 1, 1)),
                listOf(ReportLine("ورد ", "QURAN", 2, 2))
            )
        )
        assertEquals(ReportLine("ورد", "QURAN", 5, 4), merged[0])
        assertEquals(ReportLine("صدقة", "CHARITY", 1, 1), merged[1])
    }
}
