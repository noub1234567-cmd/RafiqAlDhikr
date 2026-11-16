package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * A full reading of the Quran with a finish date.
 * [readAyahs] counts ayahs read from the start of the Mushaf (0..6236); [finishedEpochDay] is 0 while unfinished.
 */
@Serializable
@Entity(
    tableName = "khatmas",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class KhatmaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val title: String,
    val startEpochDay: Long,
    val targetEpochDay: Long,
    val readAyahs: Int,
    val finishedEpochDay: Long
)
