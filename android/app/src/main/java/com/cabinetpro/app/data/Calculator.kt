package com.cabinetpro.app.data

import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

private fun nonNegative(v: Double): Double = max(0.0, if (v.isFinite()) v else 0.0)
private fun precise(v: Double): Double = round((v + Double.MIN_VALUE) * 1e10) / 1e10

/**
 * کسرها فقط از طول زمینی کم می‌شوند.
 * ماژول‌های ویژه (دو پله، کافی بار، کمد) در صورت فعال‌شدن،
 * در جمع هزینه‌ها و مبلغ نهایی گنجانده می‌شوند.
 */
fun calculateCabinetPrice(form: CabinetForm): CalculationResult {
    val deductionsTotal = precise(form.deductions.values.sumOf { nonNegative(it) })
    val groundNet = precise(max(0.0, nonNegative(form.lengths[SectionKey.GROUND] ?: 0.0) - deductionsTotal))

    val effective = mapOf(
        SectionKey.GROUND to precise(groundNet * nonNegative(form.coefficients[SectionKey.GROUND] ?: 0.0)),
        SectionKey.WALL to precise(nonNegative(form.lengths[SectionKey.WALL] ?: 0.0) * nonNegative(form.coefficients[SectionKey.WALL] ?: 0.0)),
        SectionKey.TALL to precise(nonNegative(form.lengths[SectionKey.TALL] ?: 0.0) * nonNegative(form.coefficients[SectionKey.TALL] ?: 0.0)),
        SectionKey.ISLAND to precise(nonNegative(form.lengths[SectionKey.ISLAND] ?: 0.0) * nonNegative(form.coefficients[SectionKey.ISLAND] ?: 0.0))
    )
    val effectiveTotal = precise(effective.values.sum())

    val cabinetCost = effectiveTotal * nonNegative(form.basePrice)
    val countertopCost = nonNegative(form.countertopLength) * nonNegative(form.countertopRate)
    val accessoriesCost = form.accessories.sumOf { nonNegative(it.quantity) * nonNegative(it.unitPrice) }
    val installationCost = if (form.installationMode == CostMode.PERCENT)
        (cabinetCost * min(100.0, nonNegative(form.installationValue))) / 100.0
    else nonNegative(form.installationValue)
    val transportCost = nonNegative(form.transportCost)

    // ---- ماژول ۱: کابینت دو پله ----
    val doubleTierMain = precise(
        nonNegative(form.doubleTier.tier1Length) * nonNegative(form.doubleTier.tier1Price) +
            nonNegative(form.doubleTier.tier2Length) * nonNegative(form.doubleTier.tier2Price)
    )
    val doubleTierDetail = ModuleCost(doubleTierMain, 0.0, doubleTierMain)

    // ---- ماژول ۲: کافی بار ----
    val coffeeBarMainPart = precise(nonNegative(form.coffeeBar.length) * nonNegative(form.coffeeBar.pricePerMeter))
    val coffeeBarAccessories = precise(nonNegative(form.coffeeBar.countertopLength) * nonNegative(form.coffeeBar.countertopRate))
    val coffeeBarDetail = ModuleCost(
        main = precise(nonNegative(form.coffeeBar.basePrice) + coffeeBarMainPart),
        accessories = coffeeBarAccessories,
        total = precise(nonNegative(form.coffeeBar.basePrice) + coffeeBarMainPart + coffeeBarAccessories)
    )

    // ---- ماژول ۳: کمد (مدروار) ----
    val wardrobeModel = WARDROBE_MODELS.find { it.id == form.wardrobe.modelId } ?: WARDROBE_MODELS.first()
    val wardrobeMain = precise(
        nonNegative(form.wardrobe.width) * nonNegative(form.wardrobe.unitPrice) * wardrobeModel.coefficient
    )
    val wardrobeAccessories = precise(
        form.wardrobe.drawers.toDouble() * nonNegative(form.wardrobe.drawerPrice) +
            form.wardrobe.shelves.toDouble() * nonNegative(form.wardrobe.shelfPrice)
    )
    val wardrobeDetail = ModuleCost(
        main = wardrobeMain,
        accessories = wardrobeAccessories,
        total = precise(wardrobeMain + wardrobeAccessories)
    )

    // ---- جمع ماژول‌های فعال ----
    val moduleCost = precise(
        (if (form.doubleTierEnabled) doubleTierDetail.total else 0.0) +
            (if (form.coffeeBarEnabled) coffeeBarDetail.total else 0.0) +
            (if (form.wardrobeEnabled) wardrobeDetail.total else 0.0)
    )

    val subtotal = cabinetCost + countertopCost + accessoriesCost + installationCost + transportCost + moduleCost

    val discountRaw = if (form.discountMode == CostMode.PERCENT)
        (subtotal * min(100.0, nonNegative(form.discountValue))) / 100.0
    else nonNegative(form.discountValue)
    val discountAmount = min(subtotal, discountRaw)
    // مالیات پس از کسر تخفیف
    val taxAmount = ((subtotal - discountAmount) * min(100.0, nonNegative(form.taxPercent))) / 100.0
    val total = subtotal - discountAmount + taxAmount

    return CalculationResult(
        effectiveMeters = effective,
        effectiveTotal = effectiveTotal,
        cabinetCost = cabinetCost,
        countertopCost = countertopCost,
        accessoriesCost = accessoriesCost,
        installationCost = installationCost,
        transportCost = transportCost,
        discountAmount = discountAmount,
        taxAmount = taxAmount,
        total = total,
        subtotal = subtotal,
        groundNet = groundNet,
        deductionsTotal = deductionsTotal,
        doubleTierDetail = doubleTierDetail,
        coffeeBarDetail = coffeeBarDetail,
        wardrobeDetail = wardrobeDetail,
        doubleTierCost = doubleTierDetail.total,
        coffeeBarCost = coffeeBarDetail.total,
        wardrobeCost = wardrobeDetail.total,
        wardrobeModelName = wardrobeModel.name,
        wardrobeFormula = wardrobeModel.formula,
        moduleCost = moduleCost,
        includeDoubleTier = form.doubleTierEnabled,
        includeCoffeeBar = form.coffeeBarEnabled,
        includeWardrobe = form.wardrobeEnabled
    )
}

fun validateForm(form: CabinetForm): Map<String, String> {
    val errors = mutableMapOf<String, String>()
    fun check(key: String, v: Double) {
        if (v < 0) errors[key] = "مقدار نمی‌تواند منفی باشد."
        if (!v.isFinite()) errors[key] = "لطفاً یک عدد معتبر وارد کنید."
    }
    check("basePrice", form.basePrice)
    form.lengths.forEach { (k, v) -> check("lengths.${k.name}", v) }
    form.coefficients.forEach { (k, v) -> check("coefficients.${k.name}", v) }
    form.deductions.forEach { (k, v) -> check("deductions.${k.name}", v) }
    check("countertopLength", form.countertopLength)
    check("countertopRate", form.countertopRate)
    check("installationValue", form.installationValue)
    check("transportCost", form.transportCost)
    check("discountValue", form.discountValue)
    check("taxPercent", form.taxPercent)

    // ماژول دو پله
    check("doubleTier.tier1Length", form.doubleTier.tier1Length)
    check("doubleTier.tier1Price", form.doubleTier.tier1Price)
    check("doubleTier.tier2Length", form.doubleTier.tier2Length)
    check("doubleTier.tier2Price", form.doubleTier.tier2Price)
    if (form.doubleTierEnabled &&
        form.doubleTier.tier1Length <= 0 && form.doubleTier.tier2Length <= 0
    )
        errors["doubleTier"] = "برای محاسبه دو پله، حداقل طول یک پله را وارد کنید."

    // ماژول کافی بار
    check("coffeeBar.basePrice", form.coffeeBar.basePrice)
    check("coffeeBar.length", form.coffeeBar.length)
    check("coffeeBar.pricePerMeter", form.coffeeBar.pricePerMeter)
    check("coffeeBar.countertopLength", form.coffeeBar.countertopLength)
    check("coffeeBar.countertopRate", form.coffeeBar.countertopRate)
    if (form.coffeeBarEnabled && form.coffeeBar.basePrice <= 0 && form.coffeeBar.length <= 0)
        errors["coffeeBar"] = "برای محاسبه کافی بار، قیمت پایه یا طول را وارد کنید."

    // ماژول کمد
    check("wardrobe.width", form.wardrobe.width)
    check("wardrobe.unitPrice", form.wardrobe.unitPrice)
    check("wardrobe.drawerPrice", form.wardrobe.drawerPrice)
    check("wardrobe.shelfPrice", form.wardrobe.shelfPrice)
    if (form.wardrobeEnabled && form.wardrobe.width <= 0)
        errors["wardrobe"] = "برای محاسبه کمد، عرض کمد را وارد کنید."
    if (form.wardrobe.drawers < 0) errors["wardrobe.drawers"] = "تعداد کشوی نمی‌تواند منفی باشد."
    if (form.wardrobe.shelves < 0) errors["wardrobe.shelves"] = "تعداد قفسه نمی‌تواند منفی باشد."

    if (form.installationMode == CostMode.PERCENT && form.installationValue > 100)
        errors["installationValue"] = "درصد باید بین ۰ تا ۱۰۰ باشد."
    if (form.discountMode == CostMode.PERCENT && form.discountValue > 100)
        errors["discountValue"] = "درصد باید بین ۰ تا ۱۰۰ باشد."
    if (form.taxPercent > 100) errors["taxPercent"] = "درصد باید بین ۰ تا ۱۰۰ باشد."

    val deductions = precise(form.deductions.values.sum())
    if (deductions > precise(form.lengths[SectionKey.GROUND] ?: 0.0))
        errors["deductions"] = "مجموع کسرها نمی‌تواند از طول کابینت زمینی بیشتر باشد."
    if (form.lengths.values.none { it > 0 })
        errors["lengths"] = "برای محاسبه، حداقل طول یک بخش را وارد کنید."

    form.accessories.forEach { item ->
        if (item.quantity < 0) errors["accessory.${item.id}.quantity"] = "تعداد نمی‌تواند منفی باشد."
        if (item.unitPrice < 0) errors["accessory.${item.id}.unitPrice"] = "قیمت نمی‌تواند منفی باشد."
    }
    if (form.discountMode == CostMode.FIXED && form.discountValue > calculateCabinetPrice(form).subtotal)
        errors["discountValue"] = "تخفیف نمی‌تواند بیشتر از جمع هزینه‌ها باشد."
    return errors
}
