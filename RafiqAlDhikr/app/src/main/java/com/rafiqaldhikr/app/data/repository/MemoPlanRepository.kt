package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.core.dua.DuaLibrary
import com.rafiqaldhikr.app.core.memorization.MemoPlanMath
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.MemoPlanEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemoPlanItemEntity
import kotlinx.coroutines.flow.Flow

/** A text to be put into a plan. */
data class PlanSeed(val title: String, val text: String, val source: String)

class MemoPlanRepository(database: AppDatabase, private val tools: PersonalToolsRepository) {
    private val dao = database.memoPlanDao()

    fun observePlans(profileId: Long): Flow<List<MemoPlanEntity>> = dao.observePlans(profileId)
    fun observePlan(id: Long): Flow<MemoPlanEntity?> = dao.observePlan(id)
    fun observeItems(planId: Long): Flow<List<MemoPlanItemEntity>> = dao.observeItems(planId)
    fun observeAllItems(profileId: Long): Flow<List<MemoPlanItemEntity>> = dao.observeAllItems(profileId)

    suspend fun createPlan(
        profileId: Long, title: String, sourceId: String, groupTitle: String,
        seeds: List<PlanSeed>, today: Long, totalDays: Int, reviewMode: Int, reviewCount: Int
    ): Long {
        val planId = dao.insertPlan(
            MemoPlanEntity(
                profileId = profileId, title = title.trim(), sourceId = sourceId, groupTitle = groupTitle,
                startEpochDay = today, totalDays = totalDays.coerceAtLeast(1), reviewMode = reviewMode,
                reviewCount = reviewCount.coerceAtLeast(1), createdAtMillis = System.currentTimeMillis()
            )
        )
        dao.insertItems(
            seeds.mapIndexed { i, s ->
                MemoPlanItemEntity(
                    planId = planId, position = i, itemKey = DuaLibrary.itemKey(s.text),
                    title = s.title, text = s.text, source = s.source, doneEpochDay = -1
                )
            }
        )
        return planId
    }

    /** Marks an item learned; with automatic review it also joins the spaced-repetition list. */
    suspend fun markDone(plan: MemoPlanEntity, item: MemoPlanItemEntity, today: Long) {
        dao.setDone(item.id, today)
        if (plan.reviewMode == MemoPlanMath.REVIEW_AUTO) {
            tools.addMemo(plan.profileId, item.title, item.text, item.source, today + 1)
        }
    }

    suspend fun undo(item: MemoPlanItemEntity) = dao.setDone(item.id, -1)

    suspend fun deletePlan(id: Long) = dao.deletePlan(id)
}
