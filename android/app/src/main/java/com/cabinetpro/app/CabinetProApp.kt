package com.cabinetpro.app

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.cabinetpro.app.data.CabinetForm
import com.cabinetpro.app.data.CabinetStorage
import com.cabinetpro.app.data.Draft
import com.cabinetpro.app.ui.screens.CalculatorScreen
import com.cabinetpro.app.ui.screens.DraftsDialog
import com.cabinetpro.app.ui.screens.HelpDialog
import com.cabinetpro.app.ui.theme.CabinetProTheme
import java.util.UUID

enum class DialogKind { NONE, DRAFTS, HELP, RESET }

@Composable
fun CabinetProApp() {
    val context = LocalContext.current
    val storage = remember { CabinetStorage(context) }

    var form by remember { mutableStateOf(storage.readCurrent()) }
    var drafts by remember { mutableStateOf(storage.readDrafts()) }
    var draftId by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf(DialogKind.NONE) }

    // Auto-save current form (debounced equivalent of web 650ms timer)
    LaunchedEffect(form) {
        storage.saveCurrent(form)
    }

    CabinetProTheme {
        Box(Modifier) {
            CalculatorScreen(
                form = form,
                onFormChange = { form = it },
                onSaveDraft = {
                    val id = draftId ?: UUID.randomUUID().toString()
                    val draft = Draft(
                        id = id,
                        name = form.name.trim().ifBlank { "پروژه بدون نام" },
                        savedAt = java.time.Instant.now().toString(),
                        form = form
                    )
                    val next = (listOf(draft) + drafts.filter { it.id != id }).take(30)
                    if (storage.saveDrafts(next)) {
                        drafts = next
                        draftId = id
                    }
                },
                onShowDrafts = { dialog = DialogKind.DRAFTS },
                onShowHelp = { dialog = DialogKind.HELP },
                onReset = { dialog = DialogKind.RESET },
                onShareInvoice = { text ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                        putExtra(Intent.EXTRA_SUBJECT, "خلاصه فاکتور کابینت")
                    }
                    context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فاکتور"))
                }
            )

            when (dialog) {
                DialogKind.DRAFTS -> DraftsDialog(
                    drafts = drafts,
                    onDismiss = { dialog = DialogKind.NONE },
                    onLoad = { draft ->
                        form = draft.form
                        draftId = draft.id
                        dialog = DialogKind.NONE
                    },
                    onDelete = { id ->
                        val next = drafts.filter { it.id != id }
                        if (storage.saveDrafts(next)) {
                            drafts = next
                            if (draftId == id) draftId = null
                        }
                    },
                    onSaveCurrent = {
                        val id = draftId ?: UUID.randomUUID().toString()
                        val draft = Draft(id, form.name.trim().ifBlank { "پروژه بدون نام" }, java.time.Instant.now().toString(), form)
                        val next = (listOf(draft) + drafts.filter { it.id != id }).take(30)
                        if (storage.saveDrafts(next)) { drafts = next; draftId = id }
                    }
                )
                DialogKind.HELP -> HelpDialog(
                    onDismiss = { dialog = DialogKind.NONE },
                    onLoadSample = {
                        form = CabinetForm()
                        draftId = null
                        dialog = DialogKind.NONE
                    }
                )
                DialogKind.RESET -> androidx.compose.material3.AlertDialog(
                    onDismissRequest = { dialog = DialogKind.NONE },
                    title = { androidx.compose.material3.Text("شروع یک پروژه جدید؟") },
                    text = { androidx.compose.material3.Text("اطلاعات فرم پاک می‌شود و ضرایب به مقدار پیش‌فرض برمی‌گردند. پیش‌نویس‌های ذخیره‌شده حذف نمی‌شوند.") },
                    confirmButton = {
                        androidx.compose.material3.Button(onClick = {
                            form = form.empty()
                            draftId = null
                            dialog = DialogKind.NONE
                        }) { androidx.compose.material3.Text("ریست فرم و شروع جدید") }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { dialog = DialogKind.NONE }) {
                            androidx.compose.material3.Text("انصراف")
                        }
                    }
                )
                DialogKind.NONE -> Unit
            }
        }
    }
}
