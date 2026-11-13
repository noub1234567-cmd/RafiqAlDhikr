package com.rafiqaldhikr.app.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiqaldhikr.app.core.model.ThemeMode
import com.rafiqaldhikr.app.data.backup.BackupValidationResult
import com.rafiqaldhikr.app.data.backup.RafiqBackupFile
import com.rafiqaldhikr.app.data.local.datastore.AppSettingsSnapshot
import com.rafiqaldhikr.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettingsSnapshot = AppSettingsSnapshot(),
    val statusMessage: String? = null,
    val pendingRestoreBackup: RafiqBackupFile? = null
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val messageFlow = MutableStateFlow<String?>(null)
    private val pendingBackupFlow = MutableStateFlow<RafiqBackupFile?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        container.preferences.settingsFlow,
        messageFlow,
        pendingBackupFlow
    ) { settings, msg, pending ->
        SettingsUiState(settings, msg, pending)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { container.preferences.setThemeMode(mode) }
    }

    fun setHijriOffset(offset: Int) {
        viewModelScope.launch { container.preferences.setHijriOffsetDays(offset) }
    }

    fun setTasbeehHaptic(enabled: Boolean) {
        viewModelScope.launch { container.preferences.setTasbeehHapticEnabled(enabled) }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            val result = container.backupManager.exportToUri(uri)
            messageFlow.value = if (result.isSuccess) {
                "تم تصدير النسخة الاحتياطية بنجاح شاملةً جميع الملفات الشخصية."
            } else {
                "تعذّر حفظ النسخة الاحتياطية: ${result.exceptionOrNull()?.message ?: ""}"
            }
        }
    }

    fun inspectBackupForRestore(uri: Uri) {
        viewModelScope.launch {
            when (val res = container.backupManager.readAndValidateFromUri(uri)) {
                is BackupValidationResult.Valid -> pendingBackupFlow.value = res.backup
                is BackupValidationResult.Invalid -> messageFlow.value = res.reasonAr
            }
        }
    }

    fun confirmRestore() {
        val backup = pendingBackupFlow.value ?: return
        pendingBackupFlow.value = null
        viewModelScope.launch {
            val res = container.backupManager.restoreValidatedBackup(backup)
            messageFlow.value = if (res.isSuccess) {
                "تمت استعادة البيانات وإعادة جدولة التذكيرات بنجاح."
            } else {
                "تعذّر استعادة النسخة الاحتياطية: ${res.exceptionOrNull()?.message ?: ""}"
            }
        }
    }

    fun dismissRestoreConfirmation() {
        pendingBackupFlow.value = null
    }

    fun clearMessage() {
        messageFlow.value = null
    }
}
