package com.rafiqaldhikr.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rafiqaldhikr.app.data.local.db.dao.AdhkarDao
import com.rafiqaldhikr.app.data.local.db.dao.DuaDao
import com.rafiqaldhikr.app.data.local.db.dao.FastingDao
import com.rafiqaldhikr.app.data.local.db.dao.MemorizationDao
import com.rafiqaldhikr.app.data.local.db.dao.KhatmaDao
import com.rafiqaldhikr.app.data.local.db.dao.PlannerDao
import com.rafiqaldhikr.app.data.local.db.dao.PrayerLogDao
import com.rafiqaldhikr.app.data.local.db.dao.ProfileDao
import com.rafiqaldhikr.app.data.local.db.dao.QuranDao
import com.rafiqaldhikr.app.data.local.db.dao.SeasonalDao
import com.rafiqaldhikr.app.data.local.db.dao.TasbeehDao
import com.rafiqaldhikr.app.data.local.db.dao.WorshipDao
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarProgressEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarReminderEntity
import com.rafiqaldhikr.app.data.local.db.entities.DuaEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationPlanEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationSessionEntity
import com.rafiqaldhikr.app.data.local.db.entities.KhatmaEntity
import com.rafiqaldhikr.app.data.local.db.entities.PlannerReportEntity
import com.rafiqaldhikr.app.data.local.db.entities.PlannerSlotEntity
import com.rafiqaldhikr.app.data.local.db.entities.PlannerWeekEntity
import com.rafiqaldhikr.app.data.local.db.entities.PrayerLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.QuranGoalEntity
import com.rafiqaldhikr.app.data.local.db.entities.QuranReadingLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.SeasonalSelectionEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehTypeEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity

@Database(
    entities = [
        UserProfileEntity::class,
        WorshipActivityEntity::class,
        ActivityLogEntity::class,
        AdhkarEntryEntity::class,
        AdhkarProgressEntity::class,
        AdhkarReminderEntity::class,
        QuranGoalEntity::class,
        QuranReadingLogEntity::class,
        MemorizationPlanEntity::class,
        MemorizationSessionEntity::class,
        FastingDayEntity::class,
        TasbeehTypeEntity::class,
        TasbeehLogEntity::class,
        DuaEntryEntity::class,
        SeasonalSelectionEntity::class,
        PrayerLogEntity::class,
        KhatmaEntity::class,
        PlannerWeekEntity::class,
        PlannerSlotEntity::class,
        PlannerReportEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun worshipDao(): WorshipDao
    abstract fun adhkarDao(): AdhkarDao
    abstract fun quranDao(): QuranDao
    abstract fun memorizationDao(): MemorizationDao
    abstract fun fastingDao(): FastingDao
    abstract fun tasbeehDao(): TasbeehDao
    abstract fun duaDao(): DuaDao
    abstract fun seasonalDao(): SeasonalDao
    abstract fun prayerLogDao(): PrayerLogDao
    abstract fun khatmaDao(): KhatmaDao
    abstract fun plannerDao(): PlannerDao

    companion object {
        const val DATABASE_NAME = "rafiq_al_dhikr.db"

        /** Version 2 adds the prayer tracker table; existing data is untouched. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(PRAYER_LOGS_CREATE_SQL)
                db.execSQL(PRAYER_LOGS_INDEX_SQL)
            }
        }

        /** Version 3 adds the khatma table; existing data is untouched. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(KHATMAS_CREATE_SQL)
                db.execSQL(KHATMAS_INDEX_SQL)
            }
        }

        /** Version 4 adds the weekly planner tables; existing data is untouched. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(PLANNER_WEEKS_CREATE_SQL)
                db.execSQL(PLANNER_WEEKS_INDEX_SQL)
                db.execSQL(PLANNER_SLOTS_CREATE_SQL)
                db.execSQL(PLANNER_SLOTS_INDEX_SQL)
                db.execSQL(PLANNER_REPORTS_CREATE_SQL)
                db.execSQL(PLANNER_REPORTS_INDEX_SQL)
            }
        }

        const val PLANNER_WEEKS_CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS `planner_weeks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`profileId` INTEGER NOT NULL, `weekStartEpochDay` INTEGER NOT NULL, `rowCount` INTEGER NOT NULL, " +
                "`autoRepeat` INTEGER NOT NULL, FOREIGN KEY(`profileId`) REFERENCES `user_profiles`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"

        const val PLANNER_WEEKS_INDEX_SQL =
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_planner_weeks_profileId_weekStartEpochDay` " +
                "ON `planner_weeks` (`profileId`, `weekStartEpochDay`)"

        const val PLANNER_SLOTS_CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS `planner_slots` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`weekId` INTEGER NOT NULL, `rowIndex` INTEGER NOT NULL, `dayIndex` INTEGER NOT NULL, " +
                "`title` TEXT NOT NULL, `type` TEXT NOT NULL, `timeMinutes` INTEGER NOT NULL, " +
                "`groupId` INTEGER NOT NULL, `doneAtMillis` INTEGER NOT NULL, " +
                "FOREIGN KEY(`weekId`) REFERENCES `planner_weeks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"

        const val PLANNER_SLOTS_INDEX_SQL =
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_planner_slots_weekId_rowIndex_dayIndex` " +
                "ON `planner_slots` (`weekId`, `rowIndex`, `dayIndex`)"

        const val PLANNER_REPORTS_CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS `planner_reports` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`profileId` INTEGER NOT NULL, `kind` TEXT NOT NULL, `startEpochDay` INTEGER NOT NULL, " +
                "`endEpochDay` INTEGER NOT NULL, `summaryJson` TEXT NOT NULL, `createdAtMillis` INTEGER NOT NULL, " +
                "FOREIGN KEY(`profileId`) REFERENCES `user_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"

        const val PLANNER_REPORTS_INDEX_SQL =
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_planner_reports_profileId_kind_startEpochDay` " +
                "ON `planner_reports` (`profileId`, `kind`, `startEpochDay`)"

        const val KHATMAS_CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS `khatmas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`profileId` INTEGER NOT NULL, `title` TEXT NOT NULL, `startEpochDay` INTEGER NOT NULL, " +
                "`targetEpochDay` INTEGER NOT NULL, `readAyahs` INTEGER NOT NULL, `finishedEpochDay` INTEGER NOT NULL, " +
                "FOREIGN KEY(`profileId`) REFERENCES `user_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"

        const val KHATMAS_INDEX_SQL =
            "CREATE INDEX IF NOT EXISTS `index_khatmas_profileId` ON `khatmas` (`profileId`)"

        const val PRAYER_LOGS_CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS `prayer_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`profileId` INTEGER NOT NULL, `dateEpochDay` INTEGER NOT NULL, `itemKey` TEXT NOT NULL, " +
                "`completedAtMillis` INTEGER NOT NULL, FOREIGN KEY(`profileId`) REFERENCES `user_profiles`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"

        const val PRAYER_LOGS_INDEX_SQL =
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_prayer_logs_profileId_dateEpochDay_itemKey` " +
                "ON `prayer_logs` (`profileId`, `dateEpochDay`, `itemKey`)"

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
        }
    }
}
