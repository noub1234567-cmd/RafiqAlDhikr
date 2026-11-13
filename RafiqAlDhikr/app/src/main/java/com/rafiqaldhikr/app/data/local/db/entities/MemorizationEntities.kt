package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rafiqaldhikr.app.core.model.ScheduleType
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "memorization_plans",
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
data class MemorizationPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val title: String,
    val surahNumber: Int,
    val startAyah: Int,
    val endAyah: Int,
    val currentAyah: Int,
    val versesPerSession: Int = 5,
    val memScheduleType: ScheduleType = ScheduleType.DAILY,
    val memWeekdaysMask: Int = 0b1111111,
    val memReminderEnabled: Boolean = false,
    val memReminderHour: Int = 7,
    val memReminderMinute: Int = 0,
    val revScheduleType: ScheduleType = ScheduleType.DAILY,
    val revWeekdaysMask: Int = 0b1111111,
    val revReminderEnabled: Boolean = false,
    val revReminderHour: Int = 18,
    val revReminderMinute: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(
    tableName = "memorization_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemorizationPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("planId")]
)
data class MemorizationSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val planId: Long,
    val isRevision: Boolean,
    val fromAyah: Int,
    val toAyah: Int,
    val dateEpochDay: Long,
    val completedAtMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)
