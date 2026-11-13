package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rafiqaldhikr.app.core.model.NotificationSoundMode
import com.rafiqaldhikr.app.core.model.QuranGoalType
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "quran_goals",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"], unique = true)]
)
data class QuranGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val goalType: QuranGoalType = QuranGoalType.PAGES,
    val customUnitLabel: String = "صفحة",
    val targetAmount: Double = 4.0,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    val soundMode: NotificationSoundMode = NotificationSoundMode.NORMAL,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(
    tableName = "quran_reading_logs",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("dateEpochDay")]
)
data class QuranReadingLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val dateEpochDay: Long,
    val amountRead: Double,
    val goalType: QuranGoalType,
    val unitLabel: String,
    val surahNote: String = "",
    val loggedAtMillis: Long = System.currentTimeMillis()
)
