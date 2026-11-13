/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export default function App() {
  return (
    <main className="min-h-screen bg-[#FAF8F3] text-[#1B231F] px-6 py-12 font-sans" dir="rtl">
      <div className="mx-auto max-w-3xl rounded-2xl border border-[#D0C9B9] bg-[#F3EFE6] p-8 shadow-sm">
        <div className="inline-block rounded-lg bg-[#D6EDE3] px-3 py-1 text-sm font-medium text-[#1B5E4A]">
          مشروع أندرويد أصلي (Native Android Project)
        </div>
        <h1 className="mt-4 text-3xl font-bold text-[#1B5E4A]">رفيقك في الذكر</h1>
        <p className="mt-3 text-base leading-7 text-[#525E58]">
          تم إنشاء المشروع الكامل لتطبيق الأندرويد الأصلي في المجلد <code className="rounded bg-white/70 px-2 py-0.5 font-mono text-sm">RafiqAlDhikr/</code> باستخدام:
          Kotlin، و Jetpack Compose، و Material 3، و Room Database، و Preferences DataStore، و WorkManager / AlarmManager.
        </p>
        <div className="mt-6 rounded-xl bg-white/70 p-5 text-sm leading-7 text-[#1B231F]">
          <p className="font-semibold text-[#1B5E4A]">بنية المشروع:</p>
          <ul className="mt-2 list-disc space-y-1 pr-5">
            <li><code>RafiqAlDhikr/app/src/main/java/com/rafiqaldhikr/app/</code> — الكود المصدري الكامل (الشاشات، ViewModels، المستودعات، Room، الإشعارات).</li>
            <li><code>RafiqAlDhikr/app/src/test/</code> — اختبارات الوحدة (JUnit 4).</li>
            <li><code>RafiqAlDhikr/app/src/androidTest/</code> — اختبارات قاعدة البيانات وواجهة RTL.</li>
            <li><code>RafiqAlDhikr/README.md</code> — تعليمات البناء والتثبيت عبر Android Studio أو Gradle.</li>
          </ul>
        </div>
      </div>
    </main>
  );
}
