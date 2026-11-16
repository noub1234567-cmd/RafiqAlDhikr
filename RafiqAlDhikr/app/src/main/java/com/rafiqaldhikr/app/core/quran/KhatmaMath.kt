package com.rafiqaldhikr.app.core.quran

import com.rafiqaldhikr.app.core.model.QuranSurahs
import kotlin.math.ceil

/** Position and pacing arithmetic for a khatma, counted in ayahs (the Mushaf has 6236). */
object KhatmaMath {
    val TOTAL_AYAHS: Int = QuranSurahs.all.sumOf { it.ayahCount }

    /** Roughly how many ayahs fit on one page of the Madinah Mushaf (6236 ayahs over 604 pages). */
    private const val AYAHS_PER_PAGE = 6236.0 / 604.0

    /** Ayahs in all surahs before [surahNumber]. */
    fun ayahsBefore(surahNumber: Int): Int =
        QuranSurahs.all.filter { it.number < surahNumber }.sumOf { it.ayahCount }

    /** [readAyahs] for "I have reached ayah [ayah] of surah [surahNumber]" (ayah is clamped to the surah). */
    fun readAyahsFor(surahNumber: Int, ayah: Int): Int {
        val surah = QuranSurahs.byNumber(surahNumber)
        return ayahsBefore(surah.number) + ayah.coerceIn(0, surah.ayahCount)
    }

    /** The last ayah read as (surah number, ayah in surah), or null when nothing has been read. */
    fun positionOf(readAyahs: Int): Pair<Int, Int>? {
        if (readAyahs <= 0) return null
        var left = readAyahs.coerceAtMost(TOTAL_AYAHS)
        for (s in QuranSurahs.all) {
            if (left <= s.ayahCount) return s.number to left
            left -= s.ayahCount
        }
        return null
    }

    fun remaining(readAyahs: Int): Int = (TOTAL_AYAHS - readAyahs).coerceAtLeast(0)

    /** Days from [todayEpoch] to [targetEpochDay] inclusive, never less than one. */
    fun daysLeft(todayEpoch: Long, targetEpochDay: Long): Int =
        (targetEpochDay - todayEpoch + 1).coerceAtLeast(1).toInt()

    /** Ayahs to read per remaining day (today included) to finish on the target date. */
    fun dailyWirdAyahs(readAyahs: Int, todayEpoch: Long, targetEpochDay: Long): Int =
        ceil(remaining(readAyahs).toDouble() / daysLeft(todayEpoch, targetEpochDay)).toInt()

    fun approxPages(ayahs: Int): Double = Math.round(ayahs / AYAHS_PER_PAGE * 10.0) / 10.0

    /**
     * Positive = ahead of an even pace, negative = behind, measured in ayahs, if the read amount were
     * spread evenly from the start day to the target day (counting today as complete).
     */
    fun aheadBy(readAyahs: Int, startEpoch: Long, targetEpoch: Long, todayEpoch: Long): Int {
        val totalDays = (targetEpoch - startEpoch + 1).coerceAtLeast(1)
        val elapsed = (todayEpoch - startEpoch + 1).coerceIn(0, totalDays)
        val expected = (TOTAL_AYAHS.toDouble() * elapsed / totalDays).toInt()
        return readAyahs - expected
    }
}
