package com.cabinetpro.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cabinetpro.app.data.Draft
import com.cabinetpro.app.data.calculateCabinetPrice
import com.cabinetpro.app.data.formatMoney
import com.cabinetpro.app.ui.components.RingLightCard
import com.cabinetpro.app.ui.theme.Muted
import com.cabinetpro.app.ui.theme.Navy

@Composable
fun DraftsDialog(
    drafts: List<Draft>,
    onDismiss: () -> Unit,
    onLoad: (Draft) -> Unit,
    onDelete: (String) -> Unit,
    onSaveCurrent: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(drafts, query) {
        drafts.filter { it.name.contains(query, ignoreCase = true) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("پیش‌نویس‌های من", fontWeight = FontWeight.Bold, color = Navy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("پروژه‌های ذخیره‌شده در این دستگاه. می‌توانید هر زمان آن‌ها را باز کنید.", fontSize = 12.sp, color = Muted)
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("جستجو در پروژه‌ها…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                if (filtered.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(42.dp), tint = Muted)
                        Text(
                            if (query.isNotBlank()) "پروژه‌ای یافت نشد" else "هنوز پیش‌نویسی ذخیره نشده است",
                            fontWeight = FontWeight.Bold, color = Navy
                        )
                        Text(
                            if (query.isNotBlank()) "عبارت دیگری را جستجو کنید." else "پروژه فعلی را ذخیره کنید تا بعدها به آن برگردید.",
                            fontSize = 11.sp, color = Muted
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filtered) { draft ->
                            RingLightCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp, borderWidth = 2.dp, phaseOffset = 45f) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Navy)
                                    Column(Modifier.weight(1f)) {
                                        Text(draft.name, fontWeight = FontWeight.SemiBold, color = Navy, fontSize = 13.sp)
                                        Text(
                                            "${draft.savedAt}  •  ${formatMoney(calculateCabinetPrice(draft.form).total)} تومان",
                                            fontSize = 10.sp, color = Muted
                                        )
                                    }
                                    TextButton(onClick = { onLoad(draft) }) { Text("باز کردن") }
                                    IconButton(onClick = { onDelete(draft.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف ${draft.name}", tint = Color(0xFFB3261E))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSaveCurrent) { Text("ذخیره پروژه فعلی") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("بستن") } }
    )
}
