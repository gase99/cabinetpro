package com.cabinetpro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cabinetpro.app.data.ACCESSORY_SUGGESTIONS
import androidx.compose.foundation.layout.*
import com.cabinetpro.app.data.Accessory
import com.cabinetpro.app.data.CabinetForm
import com.cabinetpro.app.data.CostMode
import com.cabinetpro.app.data.DEDUCTIONS
import com.cabinetpro.app.data.DeductionKey
import com.cabinetpro.app.data.SECTIONS
import com.cabinetpro.app.data.SectionKey
import com.cabinetpro.app.data.buildInvoiceText
import com.cabinetpro.app.data.calculateCabinetPrice
import com.cabinetpro.app.data.MODULE_FORMULAS
import com.cabinetpro.app.data.ModuleKey
import com.cabinetpro.app.data.formatMoney
import com.cabinetpro.app.data.formatNumber
import com.cabinetpro.app.data.parseDoubleInput
import com.cabinetpro.app.data.toPersianDigits
import com.cabinetpro.app.data.validateForm
import com.cabinetpro.app.data.WARDROBE_MODELS
import kotlin.math.roundToInt
import com.cabinetpro.app.ui.components.CabinetIllustration
import com.cabinetpro.app.ui.components.RingLightCard
import com.cabinetpro.app.ui.components.RingLightTotalCard
import com.cabinetpro.app.ui.theme.Gold
import com.cabinetpro.app.ui.theme.GoldLight
import com.cabinetpro.app.ui.theme.Muted
import com.cabinetpro.app.ui.theme.Navy
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    form: CabinetForm,
    onFormChange: (CabinetForm) -> Unit,
    onSaveDraft: () -> Unit,
    onShowDrafts: () -> Unit,
    onShowHelp: () -> Unit,
    onReset: () -> Unit,
    onShareInvoice: (String) -> Unit
) {
    val result = remember(form) { calculateCabinetPrice(form) }
    val errors = remember(form) { validateForm(form) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    fun notify(msg: String) { scope.launch { snackbar.showSnackbar(msg) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("محاسبه‌گر قیمت کابینت", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("برآورد دقیق، تصمیم مطمئن", fontSize = 10.sp, color = GoldLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy, titleContentColor = Color.White),
                actions = {
                    IconButton(onClick = onShowDrafts) { Icon(Icons.Default.FolderOpen, contentDescription = "پیش‌نویس‌ها", tint = Color.White) }
                    IconButton(onClick = onShowHelp) { Icon(Icons.Default.Help, contentDescription = "راهنما", tint = Color.White) }
                }
            )
        },
        bottomBar = {
            // Sticky total bar with ring light
            RingLightTotalCard(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("مبلغ نهایی", color = GoldLight, fontSize = 11.sp)
                        Text(
                            "${formatMoney(result.total)} تومان",
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }
                    Button(
                        onClick = {
                            if (errors.isNotEmpty()) notify(errors.values.first())
                            else notify("محاسبه انجام شد؛ فاکتور شما آماده است.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
                    ) { Text("محاسبه قیمت نهایی") }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Toolbar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("محاسبه‌گر  ›  پروژه جدید", fontSize = 11.sp, color = Muted)
                    Text("جزئیات محاسبه", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Navy)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onReset) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("ریست فرم")
                    }
                    Button(onClick = onSaveDraft, colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("ذخیره پیش‌نویس")
                    }
                }
            }

            // ۱ — اطلاعات پروژه
            RingLightCard(modifier = Modifier.fillMaxWidth(), phaseOffset = 0f) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(step = "۱", title = "اطلاعات پروژه", subtitle = "شروع یک برآورد دقیق، با مشخصات پروژه شما")
                    OutlinedTextField(
                        value = form.name,
                        onValueChange = { onFormChange(form.copy(name = it)) },
                        label = { Text("نام مشتری / نام پروژه") },
                        placeholder = { Text("مثلاً: آشپزخانه آقای احمدی") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = form.date,
                        onValueChange = { onFormChange(form.copy(date = it)) },
                        label = { Text("تاریخ (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // ۲ — قیمت پایه و ضرایب
            RingLightCard(modifier = Modifier.fillMaxWidth(), phaseOffset = 60f) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(step = "۲", title = "قیمت پایه و ضرایب", subtitle = "قیمت هر متر کابینت زمینی، مبنای محاسبه است")
                    NumericField(
                        label = "قیمت هر متر کابینت پایه (تومان)",
                        value = form.basePrice,
                        onValueChange = { onFormChange(form.copy(basePrice = it)) },
                        error = errors["basePrice"]
                    )
                    if (form.basePrice == 0.0) {
                        WarningRow("قیمت پایه صفر است؛ محاسبه انجام می‌شود اما هزینه کابینت صفر خواهد بود.")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SECTIONS.forEach { s ->
                            Box(Modifier.weight(1f)) {
                                NumericField(
                                    label = "ضریب ${s.short}",
                                    value = form.coefficients[s.key] ?: 0.0,
                                    onValueChange = { v -> onFormChange(form.copy(coefficients = form.coefficients + (s.key to v))) },
                                    error = errors["coefficients.${s.key.name}"]
                                )
                            }
                        }
                    }
                }
            }

            // ۳ — طول بخش‌ها
            RingLightCard(modifier = Modifier.fillMaxWidth(), phaseOffset = 120f) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(step = "۳", title = "طول بخش‌های کابینت", subtitle = "طول هر بخش را به متر وارد کنید  •  واحد: متر")
                    SECTIONS.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { s ->
                                RingLightCard(
                                    modifier = Modifier.weight(1f),
                                    cornerRadius = 12.dp, borderWidth = 2.dp,
                                    phaseOffset = (s.key.ordinal * 45f),
                                    durationMs = 4000
                                ) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            CabinetIllustration(section = s.key, modifier = Modifier.size(width = 52.dp, height = 42.dp))
                                            Text("× ${formatNumber(form.coefficients[s.key] ?: 0.0)}", fontSize = 10.sp, color = Muted,
                                                modifier = Modifier.background(Color(0xFFEDF1F5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Text(s.label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Navy)
                                        Text(s.description, fontSize = 10.sp, color = Muted)
                                        NumericField(
                                            label = "طول (متر)",
                                            value = form.lengths[s.key] ?: 0.0,
                                            onValueChange = { v -> onFormChange(form.copy(lengths = form.lengths + (s.key to v))) },
                                            error = errors["lengths.${s.key.name}"]
                                        )
                                        Text(
                                            "متراژ مؤثر: ${formatNumber(result.effectiveMeters[s.key] ?: 0.0)} متر",
                                            fontSize = 10.sp, color = Navy, fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                    errors["lengths"]?.let { Text(it, color = Color(0xFFB3261E), fontSize = 11.sp) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("متراژ مؤثر = طول بخش × ضریب همان بخش", fontSize = 10.sp, color = Muted)
                        Text("جمع مؤثر: ${formatNumber(result.effectiveTotal)} متر", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Navy)
                    }
                }
            }

            // کسر طول‌ها
            var deductionsOpen by remember { mutableStateOf(result.deductionsTotal > 0 || errors.containsKey("deductions")) }
            RingLightCard(modifier = Modifier.fillMaxWidth(), phaseOffset = 180f) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle(step = "−", title = "کسر طول‌ها", subtitle = "یخچال، گاز و سایر مواردِ غیرکابینتی")
                        TextButton(onClick = { deductionsOpen = !deductionsOpen }) {
                            Text(if (deductionsOpen) "بستن" else "افزودن کسر")
                        }
                    }
                    if (deductionsOpen) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DEDUCTIONS.forEach { d ->
                                Box(Modifier.weight(1f)) {
                                    NumericField(
                                        label = d.label,
                                        value = form.deductions[d.key] ?: 0.0,
                                        onValueChange = { v -> onFormChange(form.copy(deductions = form.deductions + (d.key to v))) },
                                        error = errors["deductions.${d.key.name}"]
                                    )
                                }
                            }
                        }
                        if (errors["deductions"] != null) {
                            Text(errors["deductions"]!!, color = Color(0xFFB3261E), fontSize = 11.sp)
                        } else {
                            Text(
                                "این مقدارها فقط از طول کابینت زمینی کسر می‌شوند. طول خالص زمینی: ${formatNumber(result.groundNet)} متر",
                                fontSize = 10.sp, color = Muted
                            )
                        }
                    }
                }
            }

            // ۴ — صفحه کابینت
            RingLightCard(modifier = Modifier.fillMaxWidth(), phaseOffset = 240f) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(step = "۴", title = "صفحه کابینت (کانتر)", subtitle = "هزینه صفحه، جدا از متراژ کابینت محاسبه می‌شود")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "طول صفحه (متر)", value = form.countertopLength,
                                onValueChange = { onFormChange(form.copy(countertopLength = it)) }, error = errors["countertopLength"])
                        }
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "قیمت هر متر صفحه (تومان)", value = form.countertopRate,
                                onValueChange = { onFormChange(form.copy(countertopRate = it)) }, error = errors["countertopRate"])
                        }
                    }
                }
            }

            // ۵ — یراق‌آلات
            RingLightCard(modifier = Modifier.fillMaxWidth(), phaseOffset = 300f) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle(step = "۵", title = "یراق‌آلات و تجهیزات ثابت", subtitle = "لولا، دستگیره، ریل و سایر تجهیزات")
                        OutlinedButton(onClick = {
                            onFormChange(form.copy(accessories = form.accessories + Accessory(id = UUID.randomUUID().toString(), quantity = 1.0)))
                        }) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)); Text(" افزودن آیتم") }
                    }
                    if (form.accessories.isEmpty()) {
                        Text("هنوز تجهیزاتی اضافه نشده است. آیتم‌های مورد نیاز پروژه را به‌صورت جداگانه اضافه کنید.", fontSize = 12.sp, color = Muted)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(ACCESSORY_SUGGESTIONS.take(3)) { s ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        onFormChange(form.copy(accessories = form.accessories + Accessory(
                                            id = UUID.randomUUID().toString(), name = s.name, quantity = 1.0, unitPrice = s.price)))
                                    },
                                    label = { Text("+ ${s.name}") }
                                )
                            }
                        }
                    } else {
                        form.accessories.forEach { item ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(Modifier.weight(1.4f)) {
                                        OutlinedTextField(
                                            value = item.name,
                                            onValueChange = { v ->
                                                val suggestion = ACCESSORY_SUGGESTIONS.find { it.name == v }
                                                onFormChange(form.copy(accessories = form.accessories.map {
                                                    if (it.id == item.id) it.copy(name = v, unitPrice = suggestion?.price ?: it.unitPrice) else it
                                                }))
                                            },
                                            label = { Text("نام تجهیز") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                                        )
                                    }
                                    Box(Modifier.weight(0.7f)) {
                                        NumericField(label = "تعداد", value = item.quantity,
                                            onValueChange = { v -> onFormChange(form.copy(accessories = form.accessories.map { if (it.id == item.id) it.copy(quantity = v) else it })) },
                                            error = errors["accessory.${item.id}.quantity"])
                                    }
                                    Box(Modifier.weight(1f)) {
                                        NumericField(label = "قیمت واحد", value = item.unitPrice,
                                            onValueChange = { v -> onFormChange(form.copy(accessories = form.accessories.map { if (it.id == item.id) it.copy(unitPrice = v) else it })) },
                                            error = errors["accessory.${item.id}.unitPrice"])
                                    }
                                    IconButton(onClick = {
                                        onFormChange(form.copy(accessories = form.accessories.filter { it.id != item.id }))
                                    }) { Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFB3261E)) }
                                }
                                Text(
                                    "جمع: ${formatMoney(item.quantity * item.unitPrice)} تومان",
                                    fontSize = 11.sp, color = Navy, fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("جمع یراق‌آلات و تجهیزات", fontWeight = FontWeight.Bold, color = Navy)
                            Text("${formatMoney(result.accessoriesCost)} تومان", fontWeight = FontWeight.Bold, color = Navy)
                        }
                    }
                }
            }

            // ۶ — هزینه‌های جانبی
            RingLightCard(modifier = Modifier.fillMaxWidth(), phaseOffset = 30f) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionTitle(step = "۶", title = "هزینه‌های جانبی و تعدیلات", subtitle = "نصب، حمل، تخفیف و مالیات را اضافه کنید")
                    // نصب
                    Text("هزینه نصب", fontWeight = FontWeight.SemiBold, color = Navy)
                    ModeToggle(selected = form.installationMode, onChange = { onFormChange(form.copy(installationMode = it, installationValue = 0.0)) })
                    NumericField(
                        label = if (form.installationMode == CostMode.PERCENT) "درصد نصب (٪)" else "مبلغ نصب (تومان)",
                        value = form.installationValue,
                        onValueChange = { onFormChange(form.copy(installationValue = it)) },
                        error = errors["installationValue"]
                    )
                    NumericField(label = "هزینه حمل (تومان)", value = form.transportCost,
                        onValueChange = { onFormChange(form.copy(transportCost = it)) }, error = errors["transportCost"])
                    Text("تخفیف", fontWeight = FontWeight.SemiBold, color = Navy)
                    ModeToggle(selected = form.discountMode, onChange = { onFormChange(form.copy(discountMode = it, discountValue = 0.0)) })
                    NumericField(
                        label = if (form.discountMode == CostMode.PERCENT) "درصد تخفیف (٪)" else "مبلغ تخفیف (تومان)",
                        value = form.discountValue,
                        onValueChange = { onFormChange(form.copy(discountValue = it)) },
                        error = errors["discountValue"]
                    )
                    NumericField(label = "مالیات (٪)", value = form.taxPercent,
                        onValueChange = { onFormChange(form.copy(taxPercent = it)) }, error = errors["taxPercent"])
                }
            }

            // ۷ — محاسبه کابینت دو پله
            RingLightCard(modifier = Modifier.fillMaxWidth(), phaseOffset = 210f) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(step = "۷", title = "محاسبه کابینت دو پله", subtitle = "هر پله به صورت جداگانه: طول × قیمت هر متر")
                    ModuleToggle(enabled = form.doubleTierEnabled, onToggle = { v -> onFormChange(form.copy(doubleTierEnabled = v)) })
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("پله اول", fontWeight = FontWeight.SemiBold, color = Navy, fontSize = 12.sp)
                                NumericField(label = "طول (متر)", value = form.doubleTier.tier1Length,
                                    onValueChange = { v -> onFormChange(form.copy(doubleTier = form.doubleTier.copy(tier1Length = v))) },
                                    error = errors["doubleTier.tier1Length"])
                                NumericField(label = "قیمت هر متر (تومان)", value = form.doubleTier.tier1Price,
                                    onValueChange = { v -> onFormChange(form.copy(doubleTier = form.doubleTier.copy(tier1Price = v))) },
                                    error = errors["doubleTier.tier1Price"])
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("پله دوم", fontWeight = FontWeight.SemiBold, color = Navy, fontSize = 12.sp)
                                NumericField(label = "طول (متر)", value = form.doubleTier.tier2Length,
                                    onValueChange = { v -> onFormChange(form.copy(doubleTier = form.doubleTier.copy(tier2Length = v))) },
                                    error = errors["doubleTier.tier2Length"])
                                NumericField(label = "قیمت هر متر (تومان)", value = form.doubleTier.tier2Price,
                                    onValueChange = { v -> onFormChange(form.copy(doubleTier = form.doubleTier.copy(tier2Price = v))) },
                                    error = errors["doubleTier.tier2Price"])
                            }
                        }
                    }
                    Text("فرمول: " + MODULE_FORMULAS[ModuleKey.DOUBLE_TIER], fontSize = 10.sp, color = Muted)
                    errors["doubleTier"]?.let { WarningRow(it) }
                    if (form.doubleTierEnabled) {
                        Text(
                            "جمع کابینت دو پله: ${formatMoney(result.doubleTierCost)} تومان",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Navy
                        )
                    }
                }
            }

            // ۸ — محاسبه کافی بار
            RingLightCard(modifier = Modifier.fillMaxWidth(), phaseOffset = 270f) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(step = "۸", title = "محاسبه کافی بار (کافه‌بار)", subtitle = "قیمت پایه + طول کابینت + صفحه کانتر")
                    ModuleToggle(enabled = form.coffeeBarEnabled, onToggle = { v -> onFormChange(form.copy(coffeeBarEnabled = v)) })
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "قیمت پایه (تومان)", value = form.coffeeBar.basePrice,
                                onValueChange = { v -> onFormChange(form.copy(coffeeBar = form.coffeeBar.copy(basePrice = v))) },
                                error = errors["coffeeBar.basePrice"])
                        }
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "طول کابینت (متر)", value = form.coffeeBar.length,
                                onValueChange = { v -> onFormChange(form.copy(coffeeBar = form.coffeeBar.copy(length = v))) },
                                error = errors["coffeeBar.length"])
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "قیمت هر متر کابینت (تومان)", value = form.coffeeBar.pricePerMeter,
                                onValueChange = { v -> onFormChange(form.copy(coffeeBar = form.coffeeBar.copy(pricePerMeter = v))) },
                                error = errors["coffeeBar.pricePerMeter"])
                        }
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "طول صفحه کانتر (متر)", value = form.coffeeBar.countertopLength,
                                onValueChange = { v -> onFormChange(form.copy(coffeeBar = form.coffeeBar.copy(countertopLength = v))) },
                                error = errors["coffeeBar.countertopLength"])
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "قیمت هر متر صفحه (تومان)", value = form.coffeeBar.countertopRate,
                                onValueChange = { v -> onFormChange(form.copy(coffeeBar = form.coffeeBar.copy(countertopRate = v))) },
                                error = errors["coffeeBar.countertopRate"])
                        }
                    }
                    Text("فرمول: " + MODULE_FORMULAS[ModuleKey.COFFEE_BAR], fontSize = 10.sp, color = Muted)
                    errors["coffeeBar"]?.let { WarningRow(it) }
                    if (form.coffeeBarEnabled) {
                        Text(
                            "جمع کافی بار: ${formatMoney(result.coffeeBarCost)} تومان",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Navy
                        )
                    }
                }
            }

            // ۹ — محاسبه قیمت کمد (مدروار)
            var modelMenuOpen by remember { mutableStateOf(false) }
            val selectedModel = remember(form.wardrobe.modelId) {
                WARDROBE_MODELS.find { it.id == form.wardrobe.modelId } ?: WARDROBE_MODELS.first()
            }
            RingLightCard(modifier = Modifier.fillMaxWidth(), phaseOffset = 330f) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(step = "۹", title = "محاسبه قیمت کمد (مدروار)", subtitle = "انتخاب مدل از منوی کمد؛ فرمول مخصوص هر مدل")
                    ModuleToggle(enabled = form.wardrobeEnabled, onToggle = { v -> onFormChange(form.copy(wardrobeEnabled = v)) })
                    ExposedDropdownMenuBox(expanded = modelMenuOpen) {
                        OutlinedTextField(
                            value = selectedModel.name,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            label = { Text("مدل کمد") },
                            singleLine = true
                        )
                        DropdownMenu(expanded = modelMenuOpen, onDismissRequest = { modelMenuOpen = false }) {
                            WARDROBE_MODELS.forEach { m ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(m.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                            Text(m.note, fontSize = 10.sp, color = Muted)
                                        }
                                    },
                                    onClick = {
                                        onFormChange(form.copy(wardrobe = form.wardrobe.copy(modelId = m.id)))
                                        modelMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF4F6F9), RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("فرمول این مدل", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Navy)
                            Text(selectedModel.formula, fontSize = 11.sp, color = Navy)
                            Text(selectedModel.note, fontSize = 10.sp, color = Muted)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "عرض / طول کمد (متر)", value = form.wardrobe.width,
                                onValueChange = { v -> onFormChange(form.copy(wardrobe = form.wardrobe.copy(width = v))) },
                                error = errors["wardrobe.width"])
                        }
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "قیمت هر متر کمد (تومان)", value = form.wardrobe.unitPrice,
                                onValueChange = { v -> onFormChange(form.copy(wardrobe = form.wardrobe.copy(unitPrice = v))) },
                                error = errors["wardrobe.unitPrice"])
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "تعداد کشوی (عدد)", value = form.wardrobe.drawers.toDouble(),
                                onValueChange = { v -> onFormChange(form.copy(wardrobe = form.wardrobe.copy(drawers = v.roundToInt()))) },
                                error = errors["wardrobe.drawers"])
                        }
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "قیمت هر کشو (تومان)", value = form.wardrobe.drawerPrice,
                                onValueChange = { v -> onFormChange(form.copy(wardrobe = form.wardrobe.copy(drawerPrice = v))) },
                                error = errors["wardrobe.drawerPrice"])
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "تعداد قفسه (عدد)", value = form.wardrobe.shelves.toDouble(),
                                onValueChange = { v -> onFormChange(form.copy(wardrobe = form.wardrobe.copy(shelves = v.roundToInt()))) },
                                error = errors["wardrobe.shelves"])
                        }
                        Box(Modifier.weight(1f)) {
                            NumericField(label = "قیمت هر قفسه (تومان)", value = form.wardrobe.shelfPrice,
                                onValueChange = { v -> onFormChange(form.copy(wardrobe = form.wardrobe.copy(shelfPrice = v))) },
                                error = errors["wardrobe.shelfPrice"])
                        }
                    }
                    Text("فرمول: " + MODULE_FORMULAS[ModuleKey.WARDROBE], fontSize = 10.sp, color = Muted)
                    errors["wardrobe"]?.let { WarningRow(it) }
                    if (form.wardrobeEnabled) {
                        Text(
                            "جمع کمد (${result.wardrobeModelName}): ${formatMoney(result.wardrobeCost)} تومان",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Navy
                        )
                    }
                }
            }

            // خلاصه فاکتور
            RingLightCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, borderWidth = 3.dp, phaseOffset = 90f) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("خلاصه فاکتور", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Navy)
                        Box(
                            modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("● زنده", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) }
                    }
                    Text("متراژ مؤثر کابینت‌ها", fontSize = 12.sp, color = Muted)
                    SECTIONS.forEach { s ->
                        val v = result.effectiveMeters[s.key] ?: 0.0
                        val maxMeter = maxOf(1.0, result.effectiveMeters.values.maxOrNull() ?: 1.0)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(10.dp).background(Color(s.colorHex), RoundedCornerShape(2.dp)))
                            Text(s.label, fontSize = 11.sp, color = Navy, modifier = Modifier.width(90.dp))
                            Box(Modifier.weight(1f).height(8.dp).background(Color(0xFFEDF1F5), RoundedCornerShape(4.dp))) {
                                Box(Modifier.fillMaxHeight().fillMaxWidth((v / maxMeter).toFloat().coerceIn(0f, 1f))
                                    .background(Color(s.colorHex), RoundedCornerShape(4.dp)))
                            }
                            Text("${formatNumber(v)} متر", fontSize = 11.sp, color = Navy)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("متراژ مؤثر کل", fontWeight = FontWeight.Bold, color = Navy)
                        Text("${formatNumber(result.effectiveTotal)} متر", fontWeight = FontWeight.Bold, color = Navy)
                    }
                    CostRow("هزینه کابینت", result.cabinetCost, primary = true)
                    CostRow("هزینه صفحه کابینت", result.countertopCost)
                    CostRow("جمع یراق‌آلات", result.accessoriesCost)
                    if (form.doubleTierEnabled && result.doubleTierCost > 0)
                        CostRow("کابینت دو پله", result.doubleTierCost)
                    if (form.coffeeBarEnabled && result.coffeeBarCost > 0)
                        CostRow("کافی بار (کافه‌بار)", result.coffeeBarCost)
                    if (form.wardrobeEnabled && result.wardrobeCost > 0)
                        CostRow("کمد — ${result.wardrobeModelName}", result.wardrobeCost)
                    CostRow("هزینه نصب", result.installationCost)
                    CostRow("هزینه حمل", result.transportCost)
                    CostRow("جمع هزینه‌ها", result.subtotal)
                    CostRow("تخفیف", result.discountAmount, discount = true)
                    CostRow("مالیات", result.taxAmount)
                    errors["deductions"]?.let { WarningRow(it) }
                    errors["lengths"]?.let { WarningRow(it) }
                    if (form.basePrice == 0.0) WarningRow("قیمت پایه صفر است؛ هزینه کابینت صفر محاسبه می‌شود.")
                }
            }

            // مبلغ نهایی
            RingLightTotalCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("مبلغ نهایی قابل پرداخت", color = GoldLight, fontSize = 12.sp)
                    Text("${formatMoney(result.total)} تومان", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(
                        if (result.taxAmount > 0) "شامل مالیات و پس از کسر تخفیف" else "با احتساب همه هزینه‌ها و تخفیف‌ها",
                        color = Color(0xFFB8C6D7), fontSize = 10.sp
                    )
                }
            }

            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (errors.isNotEmpty()) notify(errors.values.first())
                        else notify("محاسبه انجام شد؛ فاکتور شما آماده است.")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) { Icon(Icons.Default.Check, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("محاسبه قیمت نهایی") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onShareInvoice(buildInvoiceText(form, result)) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Print, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("چاپ / اشتراک‌گذاری")
                }
                OutlinedButton(onClick = {
                    clipboard.setText(AnnotatedString(buildInvoiceText(form, result)))
                    notify("خلاصه فاکتور در حافظه کپی شد.")
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("کپی فاکتور")
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("یک برآورد خوب، اولین قدم برای ساخت یک آشپزخانه خوب است.  •  اطلاعات فقط روی دستگاه شما ذخیره می‌شود.", fontSize = 10.sp, color = Muted, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionTitle(step: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier.size(32.dp).background(Color(0xFFFAF3E7), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) { Text(step, fontWeight = FontWeight.Bold, color = Gold, fontSize = 14.sp) }
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy)
            Text(subtitle, fontSize = 10.sp, color = Muted)
        }
    }
}

@Composable
private fun NumericField(label: String, value: Double, onValueChange: (Double) -> Unit, error: String? = null) {
    // Show Persian digits; parse back on edit
    var text by remember(value) { mutableStateOf(if (value == 0.0) "" else toPersianDigits(value.toString())) }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        if (!focused) text = if (value == 0.0) "" else toPersianDigits(value.toString())
    }
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { raw ->
                text = raw
                val parsed = parseDoubleInput(raw)
                onValueChange(parsed ?: 0.0)
            },
            label = { Text(label, fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            supportingText = { if (error != null) Text(error, color = Color(0xFFB3261E), fontSize = 10.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }
}

@Composable
private fun ModeToggle(selected: CostMode, onChange: (CostMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selected == CostMode.FIXED, onClick = { onChange(CostMode.FIXED) }, label = { Text("مبلغ ثابت") })
        FilterChip(selected = selected == CostMode.PERCENT, onClick = { onChange(CostMode.PERCENT) }, label = { Text("درصدی") })
    }
}

/** تکی‌سنج «در فاکتور» برای ماژول‌های محاسبه ویژه */
@Composable
private fun ModuleToggle(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    FilterChip(
        selected = enabled,
        onClick = { onToggle(!enabled) },
        label = { Text(if (enabled) "✅ در فاکتور" else "افزودن به فاکتور") }
    )
}

@Composable
private fun CostRow(label: String, value: Double, primary: Boolean = false, discount: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = if (primary) Navy else Muted, fontWeight = if (primary) FontWeight.SemiBold else FontWeight.Normal)
        Text(
            "${if (discount && value > 0) "− " else ""}${formatMoney(value)} تومان",
            fontSize = 12.sp, color = if (discount) Color(0xFF2E7D32) else Navy,
            fontWeight = if (primary) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun WarningRow(msg: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB58B48), modifier = Modifier.size(16.dp))
        Text(msg, fontSize = 11.sp, color = Color(0xFF6D5A2E))
    }
}
