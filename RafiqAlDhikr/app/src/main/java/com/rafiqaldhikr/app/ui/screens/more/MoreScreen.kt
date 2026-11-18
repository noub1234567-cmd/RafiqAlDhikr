package com.rafiqaldhikr.app.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.navigation.NavRoute

private data class MoreMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MoreScreen(
    activeProfile: UserProfileEntity?,
    onOpenProfileSwitcher: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        MoreMenuItem("الصيام", "أيام الصيام والمناسبات والتتبع", Icons.Default.CalendarMonth, NavRoute.Fasting.route),
        MoreMenuItem("جدول العبادات الأسبوعي", "جدول بصفوفك وأيام الأسبوع مع منبّه لكل خانة وتقرير بما أنجزت", Icons.Default.CalendarMonth, NavRoute.Planner.route),
        MoreMenuItem("تقرير الأسبوع", "ملخص واحد للصلاة والأذكار والقرآن والصيام والجدول مقارنةً بالأسبوع الماضي", Icons.Default.BarChart, NavRoute.WeeklyReport.route),
        MoreMenuItem("السجل السنوي", "شبكة أيام سنتك لكل عبادة مع التحديات والأوسمة", Icons.Default.DateRange, NavRoute.YearLog.route),
        MoreMenuItem("رمضان", "الإمساكية وعدّ الإفطار والتراويح وختمة ٣٠ يوماً", Icons.Default.CheckCircle, NavRoute.Ramadan.route),
        MoreMenuItem("ختمة القرآن", "موضع القراءة بالسورة والآية والورد اليومي لإنهاء الختمة", Icons.Default.CheckCircle, NavRoute.Khatma.route),
        MoreMenuItem("متابعة الصلاة", "الفرائض والسنن الرواتب والوتر والضحى بنقرة واحدة", Icons.Default.CheckCircle, NavRoute.PrayerTracker.route),
        MoreMenuItem("اتجاه القبلة", "بوصلة تعتمد المدينة المختارة في الإعدادات", Icons.Default.Place, NavRoute.Qibla.route),
        MoreMenuItem("حفظ القرآن ومراجعته", "خطط الحفظ وجلسات المراجعة والتذكيرات المستقلة", Icons.Default.Book, NavRoute.Memorization.route),
        MoreMenuItem("المسبحة والاستغفار", "عدّاد إلكتروني وأذكار مخصصة وسجل يومي", Icons.Default.Fingerprint, NavRoute.Tasbeeh.route),
        MoreMenuItem("دفتر الأدعية الشخصي", "أدعيتك الخاصة والمفضلة والبحث المحلي", Icons.Default.VolunteerActivism, NavRoute.Dua.route),
        MoreMenuItem("سنن الجمعة", "قائمة بما تفعله يوم الجمعة تُعلَّم بنقرة", Icons.Default.Checklist, NavRoute.Jumua.route),
        MoreMenuItem("مفضلتي", "كل ما نجّمته من أذكار وأدعية في مكان واحد", Icons.Default.Star, NavRoute.Favorites.route),
        MoreMenuItem("حفظ الأدعية", "راجع أدعيتك المحفوظة في مواعيد متباعدة تثبّتها في الذاكرة", Icons.Default.Book, NavRoute.DuaMemo.route),
        MoreMenuItem("محاسبة النفس", "ثلاثة أسئلة قصيرة قبل النوم وسجل خاص على جهازك", Icons.Default.Checklist, NavRoute.NightReview.route),
        MoreMenuItem("العبادات والنوافل والأهداف", "الضحى، قيام الليل، الصدقة، الجمعة، والأهداف المخصصة", Icons.Default.Checklist, NavRoute.WorshipActs.route),
        MoreMenuItem("المواسم الإسلامية", "رمضان، عشر ذي الحجة، عرفة، عاشوراء، الأشهر الحرم", Icons.Default.EventAvailable, NavRoute.Seasons.route),
        MoreMenuItem("السجل (المكتمل والفائت)", "مراجعة العبادات وإعادة الجدولة دون إكمال تلقائي", Icons.Default.History, NavRoute.History.route),
        MoreMenuItem("الإحصائيات والمتابعة", "ملخص يومي وأسبوعي وشهري بتشجيع هادئ", Icons.Default.BarChart, NavRoute.Statistics.route),
        MoreMenuItem("إدارة الملفات الشخصية", "إنشاء وتبديل ملفات مستقلة على نفس الجهاز", Icons.Default.ManageAccounts, NavRoute.Profiles.route),
        MoreMenuItem("الإعدادات والنسخ الاحتياطي", "المظهر، التقويم الهجري، الإشعارات، التصدير والاستعادة", Icons.Default.Settings, NavRoute.Settings.route)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(
            title = "الأقسام والإعدادات",
            activeProfile = activeProfile,
            onProfileClick = onOpenProfileSwitcher
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
                RafiqCard(onClick = { onNavigate(item.route) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
