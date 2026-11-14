package com.rafiqaldhikr.app.core.calendar

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.ZoneId

/** Emits today's date immediately and again every time the local day changes (midnight, timezone change on next tick). */
fun todayFlow(): Flow<LocalDate> = flow {
    while (true) {
        val zone = ZoneId.systemDefault()
        val now = LocalDate.now(zone)
        emit(now)
        val nextMidnight = now.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        // Wake at most once a minute so clock/timezone changes are picked up too.
        delay((nextMidnight - System.currentTimeMillis() + 500L).coerceIn(1_000L, 60_000L))
    }
}.distinctUntilChanged()
