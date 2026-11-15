package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** One completed prayer item (a fard or a sunnah) on one day. A row exists only while it is ticked. */
@Serializable
@Entity(
    tableName = "prayer_logs",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId", "dateEpochDay", "itemKey"], unique = true)]
)
data class PrayerLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val dateEpochDay: Long,
    val itemKey: String,
    val completedAtMillis: Long
)
