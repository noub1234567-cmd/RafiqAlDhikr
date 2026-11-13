package com.rafiqaldhikr.app.data.local.db

import androidx.room.TypeConverter
import com.rafiqaldhikr.app.core.model.ActivityCategory
import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.core.model.AdhkarCategory
import com.rafiqaldhikr.app.core.model.FastingOccasion
import com.rafiqaldhikr.app.core.model.GoalPeriod
import com.rafiqaldhikr.app.core.model.NotificationSoundMode
import com.rafiqaldhikr.app.core.model.QuranGoalType
import com.rafiqaldhikr.app.core.model.ScheduleType

class Converters {
    @TypeConverter fun fromAdhkarCategory(v: AdhkarCategory): String = v.name
    @TypeConverter fun toAdhkarCategory(v: String): AdhkarCategory = AdhkarCategory.valueOf(v)

    @TypeConverter fun fromActivityCategory(v: ActivityCategory): String = v.name
    @TypeConverter fun toActivityCategory(v: String): ActivityCategory = ActivityCategory.valueOf(v)

    @TypeConverter fun fromScheduleType(v: ScheduleType): String = v.name
    @TypeConverter fun toScheduleType(v: String): ScheduleType = ScheduleType.valueOf(v)

    @TypeConverter fun fromActivityStatus(v: ActivityStatus): String = v.name
    @TypeConverter fun toActivityStatus(v: String): ActivityStatus = ActivityStatus.valueOf(v)

    @TypeConverter fun fromGoalPeriod(v: GoalPeriod): String = v.name
    @TypeConverter fun toGoalPeriod(v: String): GoalPeriod = GoalPeriod.valueOf(v)

    @TypeConverter fun fromQuranGoalType(v: QuranGoalType): String = v.name
    @TypeConverter fun toQuranGoalType(v: String): QuranGoalType = QuranGoalType.valueOf(v)

    @TypeConverter fun fromFastingOccasion(v: FastingOccasion): String = v.name
    @TypeConverter fun toFastingOccasion(v: String): FastingOccasion = FastingOccasion.valueOf(v)

    @TypeConverter fun fromSoundMode(v: NotificationSoundMode): String = v.name
    @TypeConverter fun toSoundMode(v: String): NotificationSoundMode = NotificationSoundMode.valueOf(v)
}
