package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "tasbeeh_types",
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
data class TasbeehTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val title: String,
    val phraseArabic: String,
    val dailyTarget: Int = 100,
    val isBuiltIn: Boolean = false,
    val sortOrder: Int = 0
)

@Serializable
@Entity(
    tableName = "tasbeeh_logs",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TasbeehTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["tasbeehTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("profileId"),
        Index("tasbeehTypeId"),
        Index(value = ["profileId", "tasbeehTypeId", "dateEpochDay"], unique = true)
    ]
)
data class TasbeehLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val tasbeehTypeId: Long,
    val dateEpochDay: Long,
    val count: Int = 0,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
