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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Settings
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
        MoreMenuItem("حفظ القرآن ومراجعته", "خطط الحفظ وجلسات المراجعة والتذكيرات المستقلة", Icons.Default.Book, NavRoute.Memorization.route),
        MoreMenuItem("المسبحة والاستغفار", "عدّاد إلكتروني وأذكار مخصصة وسجل يومي", Icons.Default.Fingerprint, NavRoute.Tasbeeh.route),
        MoreMenuItem("دفتر الأدعية الشخصي", "أدعيتك الخاصة والمفضلة والبحث المحلي", Icons.Default.VolunteerActivism, NavRoute.Dua.route),
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
