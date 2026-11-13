package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rafiqaldhikr.app.core.model.FastingOccasion
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "fasting_days",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("profileId"),
        Index(value = ["profileId", "dateEpochDay"], unique = true)
    ]
)
data class FastingDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val dateEpochDay: Long,
    val occasion: FastingOccasion = FastingOccasion.VOLUNTARY,
    val isPlanned: Boolean = true,
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val notes: String = ""
)
