package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profiles ORDER BY isDefault DESC, createdAt ASC")
    fun observeAll(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles ORDER BY isDefault DESC, createdAt ASC")
    suspend fun getAll(): List<UserProfileEntity>

    @Query("SELECT * FROM user_profiles WHERE id = :profileId LIMIT 1")
    fun observeById(profileId: Long): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE id = :profileId LIMIT 1")
    suspend fun getById(profileId: Long): UserProfileEntity?

    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<UserProfileEntity>)

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Delete
    suspend fun delete(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles")
    suspend fun deleteAll()
}
