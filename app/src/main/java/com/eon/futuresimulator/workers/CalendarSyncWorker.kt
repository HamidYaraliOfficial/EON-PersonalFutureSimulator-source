package com.eon.futuresimulator.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eon.futuresimulator.calendar.AndroidCalendarProvider
import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.domain.repository.CalendarRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Periodic background refresh of the local Calendar cache (see AndroidCalendarProvider). */
@HiltWorker
class CalendarSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val calendarProvider: AndroidCalendarProvider,
    private val calendarRepository: CalendarRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!calendarProvider.hasPermission()) return Result.success()
        val now = DateTimeUtils.nowEpochMillis()
        val events = calendarProvider.readEvents(now - 7L * 86_400_000L, now + 60L * 86_400_000L)
        calendarRepository.cacheEvents(events)
        return Result.success()
    }
}
