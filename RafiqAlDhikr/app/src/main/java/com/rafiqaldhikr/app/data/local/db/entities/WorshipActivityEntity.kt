package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rafiqaldhikr.app.core.model.ActivityCategory
import com.rafiqaldhikr.app.core.model.GoalPeriod
import com.rafiqaldhikr.app.core.model.NotificationSoundMode
import com.rafiqaldhikr.app.core.model.ScheduleType
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "worship_activities",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class WorshipActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val templateKey: String? = null,
    val title: String,
    val description: String = "",
    val category: ActivityCategory = ActivityCategory.CUSTOM,
    val goalPeriod: GoalPeriod = GoalPeriod.DAILY,
    val scheduleType: ScheduleType = ScheduleType.DAILY,
    val weekdaysMask: Int = 0b1111111, // bit 0 = Monday .. bit 6 = Sunday
    val oneTimeDateEpochDay: Long? = null,
    val customIntervalDays: Int = 1,
    val anchorEpochDay: Long = 0,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val soundMode: NotificationSoundMode = NotificationSoundMode.NORMAL,
    val targetRepetitions: Int = 1,
    val isEnabled: Boolean = true,
    val isOptionalBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
