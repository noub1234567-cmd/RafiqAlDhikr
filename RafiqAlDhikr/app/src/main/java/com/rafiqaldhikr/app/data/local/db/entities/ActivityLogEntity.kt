package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rafiqaldhikr.app.core.model.ActivityCategory
import com.rafiqaldhikr.app.core.model.ActivityStatus
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "activity_logs",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorshipActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("profileId"),
        Index("activityId"),
        Index(value = ["profileId", "activityId", "dateEpochDay"], unique = true)
    ]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val activityId: Long,
    val activityTitleSnapshot: String,
    val categorySnapshot: ActivityCategory,
    val dateEpochDay: Long,
    val scheduledAtMillis: Long,
    val status: ActivityStatus = ActivityStatus.PENDING,
    val completedAtMillis: Long? = null,
    val rescheduledToMillis: Long? = null,
    val notes: String = ""
)
