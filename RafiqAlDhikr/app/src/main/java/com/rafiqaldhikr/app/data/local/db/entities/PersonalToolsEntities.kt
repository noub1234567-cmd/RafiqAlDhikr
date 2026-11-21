package com.rafiqaldhikr.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** A starred dhikr or du'a. The text is copied, so it stays even if the library changes. */
@Serializable
@Entity(
    tableName = "dua_favorites",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId", "itemKey"], unique = true)]
)
data class DuaFavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val itemKey: String,
    val title: String,
    val text: String,
    val source: String,
    val createdAtMillis: Long
)

/** One night's self-accounting: three short private answers. */
@Serializable
@Entity(
    tableName = "night_reviews",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId", "dateEpochDay"], unique = true)]
)
data class NightReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val dateEpochDay: Long,
    val shortfall: String,
    val good: String,
    val forgiveness: String,
    val updatedAtMillis: Long
)

/** A du'a being memorized, with its spaced-repetition state ([stage] indexes ReviewScheduler.INTERVALS). */
@Serializable
@Entity(
    tableName = "dua_memos",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId", "itemKey"], unique = true)]
)
data class DuaMemoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val itemKey: String,
    val title: String,
    val text: String,
    val source: String,
    val stage: Int,
    val dueEpochDay: Long,
    val lastEpochDay: Long,
    val createdAtMillis: Long
)

/** A bookmarked spot in a library section; [locator] is the list key used to scroll back to it. */
@Serializable
@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId", "itemKey"], unique = true)]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val itemKey: String,
    val sectionId: String,
    val sectionTitle: String,
    val locator: String,
    val title: String,
    val text: String,
    val source: String,
    val createdAtMillis: Long
)
