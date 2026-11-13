package com.rafiqaldhikr.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.DuaEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehTypeEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import com.rafiqaldhikr.app.notifications.MissedActivityEvaluator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun profilesAreStrictlyIsolatedAndCascadeDelete() = runTest {
        val p1 = db.profileDao().insert(UserProfileEntity(name = "الأول", isDefault = true))
        val p2 = db.profileDao().insert(UserProfileEntity(name = "الثاني", isDefault = false))

        db.duaDao().insert(DuaEntryEntity(profileId = p1, title = "دعاء ١", body = "نص ١"))
        db.duaDao().insert(DuaEntryEntity(profileId = p2, title = "دعاء ٢", body = "نص ٢"))

        assertEquals(1, db.duaDao().observeDuas(p1).first().size)
        assertEquals(1, db.duaDao().observeDuas(p2).first().size)

        val profile1 = db.profileDao().getById(p1)!!
        db.profileDao().delete(profile1)

        assertEquals(0, db.duaDao().observeDuas(p1).first().size)
        assertEquals(1, db.duaDao().observeDuas(p2).first().size)
    }

    @Test
    fun tasbeehAndReschedulePersistenceWorkAsExpected() = runTest {
        val p1 = db.profileDao().insert(UserProfileEntity(name = "مستخدم", isDefault = true))
        val typeId = db.tasbeehDao().insertType(
            TasbeehTypeEntity(profileId = p1, title = "الاستغفار", phraseArabic = "أستغفر الله")
        )
        db.tasbeehDao().upsertLog(
            TasbeehLogEntity(profileId = p1, tasbeehTypeId = typeId, dateEpochDay = 20100L, count = 33)
        )
        assertEquals(33, db.tasbeehDao().getLogForDay(p1, typeId, 20100L)?.count)

        val actId = db.worshipDao().insertActivity(
            WorshipActivityEntity(profileId = p1, title = "صلاة الضحى")
        )
        val logId = db.worshipDao().insertLog(
            com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity(
                profileId = p1,
                activityId = actId,
                activityTitleSnapshot = "صلاة الضحى",
                categorySnapshot = com.rafiqaldhikr.app.core.model.ActivityCategory.PRAYER_SUNNAH,
                dateEpochDay = 20100L,
                scheduledAtMillis = 1000L,
                status = ActivityStatus.MISSED
            )
        )
        val log = db.worshipDao().getLogById(logId)!!
        val rescheduled = MissedActivityEvaluator.rescheduleWithoutCompleting(log, 5000L)
        db.worshipDao().updateLog(rescheduled)

        val reloaded = db.worshipDao().getLogById(logId)!!
        assertEquals(ActivityStatus.RESCHEDULED, reloaded.status)
        assertNull(reloaded.completedAtMillis)
    }
}
