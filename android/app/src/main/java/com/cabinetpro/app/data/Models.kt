package com.cabinetpro.app.data

import java.util.UUID

enum class SectionKey { GROUND, WALL, TALL, ISLAND }
enum class CostMode { FIXED, PERCENT }
enum class DeductionKey { FRIDGE, STOVE, WASHER, OTHER }
enum class ModuleKey { DOUBLE_TIER, COFFEE_BAR, WARDROBE }

data class Accessory(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0
)

/** کابینت دو پله: هر پله به صورت جداگانه طول × قیمت هر متر */
data class DoubleTierData(
    val tier1Length: Double = 0.0,
    val tier1Price: Double = 0.0,
    val tier2Length: Double = 0.0,
    val tier2Price: Double = 0.0
)

/** کافی بار: قیمت پایه + طول × نرخ + صفحه کانتر */
data class CoffeeBarData(
    val basePrice: Double = 0.0,
    val length: Double = 0.0,
    val pricePerMeter: Double = 0.0,
    val countertopLength: Double = 0.0,
    val countertopRate: Double = 0.0
)

/** مدل کمد با ضریب مخصوص و فرمول محاسبهٔ آن */
data class WardrobeModel(
    val id: String,
    val name: String,
    val note: String,
    val coefficient: Double,
    val formula: String
)

/** محاسبه قیمت کمد (مدروار): عرض × قیمت هر متر × ضریب مدل + اجناس */
data class WardrobeData(
    val modelId: String = WARDROBE_MODELS.first().id,
    val width: Double = 0.0,
    val unitPrice: Double = 0.0,
    val drawers: Int = 0,
    val drawerPrice: Double = 0.0,
    val shelves: Int = 0,
    val shelfPrice: Double = 0.0
)

/** جمع‌وجور هزینه یک ماژول محاسباتی */
data class ModuleCost(
    val main: Double,
    val accessories: Double,
    val total: Double
)

data class CabinetForm(
    val name: String = "",
    val date: String = todayISO(),
    val basePrice: Double = 6_500_000.0,
    val coefficients: Map<SectionKey, Double> = mapOf(
        SectionKey.GROUND to 1.0,
        SectionKey.WALL to 0.6,
        SectionKey.TALL to 2.0,
        SectionKey.ISLAND to 1.0
    ),
    val lengths: Map<SectionKey, Double> = mapOf(
        SectionKey.GROUND to 3.0,
        SectionKey.WALL to 3.0,
        SectionKey.TALL to 1.0,
        SectionKey.ISLAND to 0.0
    ),
    val deductions: Map<DeductionKey, Double> = mapOf(
        DeductionKey.FRIDGE to 0.0,
        DeductionKey.STOVE to 0.0,
        DeductionKey.WASHER to 0.0,
        DeductionKey.OTHER to 0.0
    ),
    val countertopLength: Double = 0.0,
    val countertopRate: Double = 0.0,
    val accessories: List<Accessory> = emptyList(),
    val installationMode: CostMode = CostMode.FIXED,
    val installationValue: Double = 0.0,
    val transportCost: Double = 0.0,
    val discountMode: CostMode = CostMode.FIXED,
    val discountValue: Double = 0.0,
    val taxPercent: Double = 0.0,
    val doubleTierEnabled: Boolean = false,
    val doubleTier: DoubleTierData = DoubleTierData(),
    val coffeeBarEnabled: Boolean = false,
    val coffeeBar: CoffeeBarData = CoffeeBarData(),
    val wardrobeEnabled: Boolean = false,
    val wardrobe: WardrobeData = WardrobeData()
) {
    fun empty(): CabinetForm = copy(
        name = "",
        basePrice = 0.0,
        lengths = mapOf(
            SectionKey.GROUND to 0.0, SectionKey.WALL to 0.0,
            SectionKey.TALL to 0.0, SectionKey.ISLAND to 0.0
        )
    )
}

data class CalculationResult(
    val effectiveMeters: Map<SectionKey, Double>,
    val effectiveTotal: Double,
    val cabinetCost: Double,
    val countertopCost: Double,
    val accessoriesCost: Double,
    val installationCost: Double,
    val transportCost: Double,
    val discountAmount: Double,
    val taxAmount: Double,
    val total: Double,
    val subtotal: Double,
    val groundNet: Double,
    val deductionsTotal: Double,
    val doubleTierDetail: ModuleCost,
    val coffeeBarDetail: ModuleCost,
    val wardrobeDetail: ModuleCost,
    val doubleTierCost: Double,
    val coffeeBarCost: Double,
    val wardrobeCost: Double,
    val wardrobeModelName: String,
    val wardrobeFormula: String,
    val moduleCost: Double,
    val includeDoubleTier: Boolean,
    val includeCoffeeBar: Boolean,
    val includeWardrobe: Boolean
)

data class Draft(
    val id: String,
    val name: String,
    val savedAt: String,
    val form: CabinetForm
)

data class SectionInfo(
    val key: SectionKey,
    val label: String,
    val short: String,
    val description: String,
    val colorHex: Long
)

data class DeductionInfo(val key: DeductionKey, val label: String)
data class AccessorySuggestion(val name: String, val price: Double)
