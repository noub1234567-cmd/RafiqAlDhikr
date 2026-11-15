package com.rafiqaldhikr.app.ui.screens.settings

import android.app.Activity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.security.AppLock
import com.rafiqaldhikr.app.ui.components.RafiqCard

@Composable
fun AppLockCard() {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(AppLock.isEnabled(context)) }

    RafiqCard {
        Text("قفل التطبيق", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "يطلب بصمتك أو نمط/رمز فتح هاتفك عند فتح التطبيق، ويخفي محتواه في قائمة التطبيقات الأخيرة. " +
                "لا يخزّن التطبيق أي كلمة سر. يتطلب أن يكون للهاتف قفل شاشة.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تفعيل القفل", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = enabled,
                onCheckedChange = { on ->
                    if (on && !AppLock.isDeviceSecure(context)) {
                        Toast.makeText(
                            context,
                            "فعّل قفل الشاشة في إعدادات هاتفك أولاً (بصمة أو نمط أو رمز).",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        AppLock.setEnabled(context, on)
                        enabled = on
                        (context as? Activity)?.window?.let { w ->
                            if (on) w.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                            else w.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                    }
                }
            )
        }
    }
}
