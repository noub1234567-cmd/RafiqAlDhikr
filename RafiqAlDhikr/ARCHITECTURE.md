# البنية المعمارية وخطة التنفيذ — رفيقك في الذكر

## 1. البنية المعمارية (Clean Layered Architecture)
- **طبقة الواجهة (`ui/`)**: Jetpack Compose + Material 3 + Navigation Compose + StateFlow + ViewModels عبر `AppViewModelFactory`.
- **طبقة الأعمال والتنبيهات (`core/`, `notifications/`)**:
  - `HijriCalendarHelper`: تحويل هجري/ميلادي قابل للتعديل (`-2..+2`) واكتشاف مواسم الصيام.
  - `MissedActivityEvaluator`: منطق نقي لاحتساب المواعيد، رصد الفائت، ومنع الإكمال التلقائي عند إعادة الجدولة.
  - `ReminderScheduler` + `ReminderReceiver` + `NotificationActionReceiver` + `BootAndUpdateReceiver` + `MissedActivityWorker`.
- **طبقة البيانات (`data/`)**:
  - `AppDatabase` (Room v1 مع تصدير المخطط `schemas/` ودعم `Migration`).
  - `UserPreferencesDataStore` (Preferences DataStore للإعدادات العامة وتخصيص الرئيسية لكل ملف).
  - `BackupCodec` و `BackupManager` للتصدير والاستيراد المحلي مع التحقق من التوقيع (SHA-256) والمعاملات الآمنة (`withTransaction`).

## 2. مخطط قاعدة البيانات (Room Schema)
- `user_profiles` (الجدول الأب، وبحذفه تُحذف سجلات الملف المرتبطة عبر `CASCADE`).
- `worship_activities` و `activity_logs` (العبادات اليومية/الأسبوعية/المخصصة وسجل الإنجاز والفوات وإعادة الجدولة).
- `adhkar_entries` و `adhkar_progress` و `adhkar_reminders`.
- `quran_goals` و `quran_reading_logs`.
- `memorization_plans` و `memorization_sessions`.
- `fasting_days`.
- `tasbeeh_types` و `tasbeeh_logs`.
- `dua_entries`.
- `seasonal_selections`.

## 3. قيود أندرويد في الصلاحيات والجدولة
- يتطلب أندرويد 13+ إذن `POST_NOTIFICATIONS` وقت التشغيل.
- يتطلب أندرويد 12+ التحقق من `AlarmManager.canScheduleExactAlarms()` مع بديل `setAndAllowWhileIdle` وإرشاد واضح لفتح الإعدادات.
- تفرض بعض واجهات الشركات المصنّعة قيود بطارية في الخلفية، لذلك يوفّر التطبيق صفحة صلاحيات واضحة دون الادعاء بضمان التنبيه تحت كل القيود.
