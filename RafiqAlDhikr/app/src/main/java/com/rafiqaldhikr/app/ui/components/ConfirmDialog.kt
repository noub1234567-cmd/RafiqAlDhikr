package com.rafiqaldhikr.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Holds one pending "are you sure?" question for a screen. */
@Stable
class ConfirmState {
    var message by mutableStateOf<String?>(null)
        private set
    private var action: (() -> Unit)? = null

    fun ask(message: String, onConfirm: () -> Unit) {
        action = onConfirm
        this.message = message
    }

    internal fun accept() {
        val a = action
        dismiss()
        a?.invoke()
    }

    internal fun dismiss() {
        message = null
        action = null
    }
}

@Composable
fun rememberConfirm(): ConfirmState = remember { ConfirmState() }

/** Draws the dialog while a question is pending. Place anywhere in the screen body. */
@Composable
fun ConfirmHost(state: ConfirmState) {
    val msg = state.message ?: return
    AlertDialog(
        onDismissRequest = { state.dismiss() },
        title = { Text("تأكيد") },
        text = { Text(msg) },
        confirmButton = { Button(onClick = { state.accept() }) { Text("نعم") } },
        dismissButton = { TextButton(onClick = { state.dismiss() }) { Text("إلغاء") } }
    )
}
