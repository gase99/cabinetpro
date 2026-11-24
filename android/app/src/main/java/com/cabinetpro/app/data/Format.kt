package com.cabinetpro.app.data

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

private val persianDigits = charArrayOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')

fun toPersianDigits(value: String): String =
    value.map { c -> if (c in '0'..'9') persianDigits[c - '0'] else c }.joinToString("")

fun toEnglishDigits(value: String): String {
    val fa = "۰۱۲۳۴۵۶۷۸۹"
    val ar = "٠١٢٣٤٥٦٧٨٩"
    return value.map { c ->
        when {
            c in fa -> fa.indexOf(c).toString()
            c in ar -> ar.indexOf(c).toString()
            c == '٬' || c == ',' || c == ' ' -> ""
            c == '٫' -> "."
            else -> c.toString()
        }
    }.joinToString("")
}

fun parseDoubleInput(raw: String): Double? {
    val cleaned = toEnglishDigits(raw.trim()).replace("٫", ".")
    if (cleaned.isEmpty()) return null
    return cleaned.toDoubleOrNull()
}

/** fa-IR style grouping with ٬ and decimal ٫ — matches web formatNumber */
fun formatNumber(value: Double, decimals: Int = 2): String {
    if (!value.isFinite()) return "۰"
    val symbols = DecimalFormatSymbols(Locale("fa", "IR")).apply {
        groupingSeparator = '٬'
        decimalSeparator = '٫'
        zeroDigit = '۰'
        // Use latin digits then convert — simpler & consistent
        digit = '۰'
    }
    val pattern = if (decimals == 0) "#,##0" else "#,##0.##"
    val df = DecimalFormat(pattern, symbols)
    df.maximumFractionDigits = decimals
    df.minimumFractionDigits = 0
    df.isGroupingUsed = true
    // DecimalFormat with fa symbols may still emit latin digits on some devices; force Persian:
    val latin = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).apply {
        maximumFractionDigits = decimals; minimumFractionDigits = 0; isGroupingUsed = true
    }
    val grouped = latin.format(value).replace(",", "٬").replace(".", "٫")
    return toPersianDigits(grouped)
}

fun formatMoney(value: Double): String = formatNumber(value.roundToLong().toDouble(), 0)

fun todayISO(): String {
    val c = Calendar.getInstance()
    return "%04d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

fun formatDateFA(iso: String): String {
    if (iso.isBlank()) return "انتخاب تاریخ"
    val parts = iso.split("-")
    if (parts.size != 3) return iso
    // Keep Gregorian display in Persian digits (matches web fa-IR locale formatting intent)
    return toPersianDigits("${parts[2]}/${parts[1]}/${parts[0]}")
}

fun buildInvoiceText(form: CabinetForm, result: CalculationResult): String {
    val lines = mutableListOf<String>()
    lines += "محاسبه‌گر قیمت کابینت"
    lines += "خلاصه فاکتور"
    lines += "پروژه: ${form.name.ifBlank { "پروژه بدون نام" }}"
    lines += "تاریخ: ${formatDateFA(form.date)}"
    lines += "────────────────────"
    lines += "قیمت هر متر کابینت پایه: ${formatMoney(form.basePrice)} تومان"
    SECTIONS.forEach { s ->
        val len = if (s.key == SectionKey.GROUND) result.groundNet else (form.lengths[s.key] ?: 0.0)
        lines += "${s.label}: ${formatNumber(result.effectiveMeters[s.key] ?: 0.0)} متر (طول ${formatNumber(len)} × ضریب ${formatNumber(form.coefficients[s.key] ?: 0.0)})"
    }
    lines += "کسر طول زمینی: ${formatNumber(result.deductionsTotal)} متر"
    lines += "متراژ مؤثر کل: ${formatNumber(result.effectiveTotal)} متر"
    lines += "────────────────────"
    lines += "هزینه کابینت: ${formatMoney(result.cabinetCost)} تومان"
    lines += "هزینه صفحه کابینت: ${formatMoney(result.countertopCost)} تومان"
    lines += "جمع یراق‌آلات: ${formatMoney(result.accessoriesCost)} تومان"
    form.accessories.forEach { item ->
        lines += "  ${item.name.ifBlank { "تجهیز" }} × ${formatNumber(item.quantity)}: ${formatMoney(item.quantity * item.unitPrice)} تومان"
    }
    // ---- محاسبات ویژه (ماژول‌های فعال) ----
    if (form.doubleTierEnabled && result.doubleTierCost > 0) {
        lines += "────────────────────"
        lines += "【 محاسبات ویژه ۱ — کابینت دو پله 】"
        lines += "پله اول: ${formatNumber(form.doubleTier.tier1Length)} متر × ${formatMoney(form.doubleTier.tier1Price)} تومان = ${formatMoney(form.doubleTier.tier1Length * form.doubleTier.tier1Price)} تومان"
        lines += "پله دوم: ${formatNumber(form.doubleTier.tier2Length)} متر × ${formatMoney(form.doubleTier.tier2Price)} تومان = ${formatMoney(form.doubleTier.tier2Length * form.doubleTier.tier2Price)} تومان"
        lines += "جمع کابینت دو پله: ${formatMoney(result.doubleTierCost)} تومان"
    }
    if (form.coffeeBarEnabled && result.coffeeBarCost > 0) {
        lines += "────────────────────"
        lines += "【 محاسبات ویژه ۲ — کافی بار (کافه‌بار) 】"
        lines += "قیمت پایه: ${formatMoney(form.coffeeBar.basePrice)} تومان"
        lines += "کابینت: ${formatNumber(form.coffeeBar.length)} متر × ${formatMoney(form.coffeeBar.pricePerMeter)} تومان = ${formatMoney(form.coffeeBar.length * form.coffeeBar.pricePerMeter)} تومان"
        lines += "صفحه کانتر: ${formatNumber(form.coffeeBar.countertopLength)} متر × ${formatMoney(form.coffeeBar.countertopRate)} تومان = ${formatMoney(form.coffeeBar.countertopLength * form.coffeeBar.countertopRate)} تومان"
        lines += "جمع کافی بار: ${formatMoney(result.coffeeBarCost)} تومان"
    }
    if (form.wardrobeEnabled && result.wardrobeCost > 0) {
        lines += "────────────────────"
        lines += "【 محاسبات ویژه ۳ — کمد (مدروار) 】"
        lines += "مدل: ${result.wardrobeModelName}"
        lines += "فرمول: ${result.wardrobeFormula}"
        lines += "بخش اصلی: ${formatNumber(form.wardrobe.width)} متر × ${formatMoney(form.wardrobe.unitPrice)} تومان × ضریب = ${formatMoney(result.wardrobeDetail.main)} تومان"
        lines += "اجناس: کشوی ${form.wardrobe.drawers} × ${formatMoney(form.wardrobe.drawerPrice)} تومان + قفسه ${form.wardrobe.shelves} × ${formatMoney(form.wardrobe.shelfPrice)} تومان = ${formatMoney(result.wardrobeDetail.accessories)} تومان"
        lines += "جمع کمد: ${formatMoney(result.wardrobeCost)} تومان"
    }
    lines += "────────────────────"
    lines += "هزینه نصب: ${formatMoney(result.installationCost)} تومان"
    lines += "هزینه حمل: ${formatMoney(result.transportCost)} تومان"
    lines += "تخفیف: ${formatMoney(result.discountAmount)} تومان"
    lines += "مالیات: ${formatMoney(result.taxAmount)} تومان"
    lines += "────────────────────"
    lines += "مبلغ نهایی قابل پرداخت: ${formatMoney(result.total)} تومان"
    lines += "این برآورد با محاسبه‌گر قیمت کابینت تهیه شده است."
    return lines.joinToString("\n")
}
