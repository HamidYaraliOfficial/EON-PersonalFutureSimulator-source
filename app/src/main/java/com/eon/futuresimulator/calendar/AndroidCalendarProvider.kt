package com.eon.futuresimulator.calendar

import android.content.Context
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.eon.futuresimulator.core.util.DataSource
import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.core.util.IdGenerator
import com.eon.futuresimulator.data.database.entity.CalendarEventEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calendar Integration Layer — reads (never writes) the on-device Android Calendar via
 * [CalendarContract], strictly after the user has granted READ_CALENDAR at runtime.
 * Results are mapped to [CalendarEventEntity] and cached in Room by CalendarSyncWorker
 * so Routine/Time-Budget analysis keeps working fully offline.
 */
@Singleton
class AndroidCalendarProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    suspend fun readEvents(fromEpochMillis: Long, toEpochMillis: Long): List<CalendarEventEntity> = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext emptyList()

        val results = mutableListOf<CalendarEventEntity>()
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY,
        )
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        android.content.ContentUris.appendId(builder, fromEpochMillis)
        android.content.ContentUris.appendId(builder, toEpochMillis)

        runCatching {
            context.contentResolver.query(
                builder.build(),
                arrayOf(
                    CalendarContract.Instances.EVENT_ID,
                    CalendarContract.Instances.CALENDAR_ID,
                    CalendarContract.Instances.TITLE,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Instances.ALL_DAY,
                ),
                null, null, "${CalendarContract.Instances.BEGIN} ASC",
            )?.use { cursor ->
                val idxEvent = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                val idxCal = cursor.getColumnIndex(CalendarContract.Instances.CALENDAR_ID)
                val idxTitle = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
                val idxBegin = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
                val idxEnd = cursor.getColumnIndex(CalendarContract.Instances.END)
                val idxAllDay = cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY)

                while (cursor.moveToNext()) {
                    results += CalendarEventEntity(
                        id = IdGenerator.newId(),
                        externalEventId = cursor.getString(idxEvent),
                        externalCalendarId = cursor.getString(idxCal),
                        title = cursor.getString(idxTitle) ?: "",
                        startEpochMillis = cursor.getLong(idxBegin),
                        endEpochMillis = cursor.getLong(idxEnd),
                        isAllDay = cursor.getInt(idxAllDay) == 1,
                        linkedGoalId = null,
                        linkedTaskId = null,
                        createdAt = DateTimeUtils.nowEpochMillis(),
                        updatedAt = DateTimeUtils.nowEpochMillis(),
                        source = DataSource.CALENDAR_SYNC,
                        version = 1,
                    )
                }
            }
        }
        results
    }
}
