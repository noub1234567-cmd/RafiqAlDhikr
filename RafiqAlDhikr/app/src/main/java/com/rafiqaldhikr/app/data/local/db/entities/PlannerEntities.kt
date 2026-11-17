package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** One week's table. [weekStartEpochDay] is the Saturday that starts the week. */
@Serializable
@Entity(
    tableName = "planner_weeks",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId", "weekStartEpochDay"], unique = true)]
)
data class PlannerWeekEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val weekStartEpochDay: Long,
    val rowCount: Int,
    /** When true the plan is copied into the next week automatically. */
    val autoRepeat: Boolean
)

/**
 * A cell of the table. [timeMinutes] is minutes after midnight, or -1 for no alarm.
 * Slots created together by "repeat on days" share a [groupId] but are otherwise independent.
 */
@Serializable
@Entity(
    tableName = "planner_slots",
    foreignKeys = [
        ForeignKey(
            entity = PlannerWeekEntity::class,
            parentColumns = ["id"],
            childColumns = ["weekId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["weekId", "rowIndex", "dayIndex"], unique = true)]
)
data class PlannerSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekId: Long,
    val rowIndex: Int,
    val dayIndex: Int,
    val title: String,
    val type: String,
    val timeMinutes: Int,
    val groupId: Long,
    /** 0 while not done. */
    val doneAtMillis: Long
)

/** Archived summary of a finished week (or month). Only ever deleted by the user. */
@Serializable
@Entity(
    tableName = "planner_reports",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId", "kind", "startEpochDay"], unique = true)]
)
data class PlannerReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    /** "WEEK" or "MONTH". */
    val kind: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    /** JSON list of ReportLine. */
    val summaryJson: String,
    val createdAtMillis: Long
)
