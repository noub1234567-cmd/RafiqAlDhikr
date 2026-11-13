package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rafiqaldhikr.app.core.model.AdhkarCategory
import com.rafiqaldhikr.app.core.model.NotificationSoundMode
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "adhkar_entries",
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
data class AdhkarEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val templateKey: String? = null,
    val category: AdhkarCategory,
    val title: String,
    val arabicText: String,
    val targetCount: Int,
    val sourceReference: String,
    val isCustom: Boolean = false,
    val sortOrder: Int = 0
)

@Serializable
@Entity(
    tableName = "adhkar_progress",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AdhkarEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["adhkarEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("profileId"),
        Index("adhkarEntryId"),
        Index(value = ["profileId", "adhkarEntryId", "dateEpochDay"], unique = true)
    ]
)
data class AdhkarProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val adhkarEntryId: Long,
    val dateEpochDay: Long,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null
)

@Serializable
@Entity(
    tableName = "adhkar_reminders",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId", "category"], unique = true)]
)
data class AdhkarReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val category: AdhkarCategory,
    val enabled: Boolean = false,
    val hour: Int = 6,
    val minute: Int = 0,
    val soundMode: NotificationSoundMode = NotificationSoundMode.NORMAL
)
