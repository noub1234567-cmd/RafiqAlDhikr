package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** A memorization plan over a list of texts (a section or one chapter of it) spread over [totalDays] days. */
@Serializable
@Entity(
    tableName = "memo_plans",
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
data class MemoPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val title: String,
    val sourceId: String,
    val groupTitle: String,
    val startEpochDay: Long,
    val totalDays: Int,
    /** 0 = automatic spaced repetition, 1 = manual (review the last [reviewCount] learned items daily). */
    val reviewMode: Int,
    val reviewCount: Int,
    val createdAtMillis: Long
)

/** One text of a plan with its own copy of the wording, so the plan survives library changes. */
@Serializable
@Entity(
    tableName = "memo_plan_items",
    foreignKeys = [
        ForeignKey(
            entity = MemoPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["planId"])]
)
data class MemoPlanItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long,
    val position: Int,
    val itemKey: String,
    val title: String,
    val text: String,
    val source: String,
    /** Epoch day it was learned, or -1 while still to learn. */
    val doneEpochDay: Long
)
