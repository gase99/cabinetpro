package com.cabinetpro.app.data

val SECTIONS = listOf(
    SectionInfo(SectionKey.GROUND, "کابینت زمینی", "زمینی", "کابینت زیر صفحه", 0xFF233B57),
    SectionInfo(SectionKey.WALL, "کابینت دیواری", "دیواری", "کابینت هوایی", 0xFFC89B58),
    SectionInfo(SectionKey.TALL, "کابینت ایستاده", "ایستاده", "کابینت کمدی", 0xFF72968A),
    SectionInfo(SectionKey.ISLAND, "جزیره / اپن", "جزیره / اپن", "کابینت جزیره‌ای", 0xFFA9B5C3)
)

val DEDUCTIONS = listOf(
    DeductionInfo(DeductionKey.FRIDGE, "یخچال"),
    DeductionInfo(DeductionKey.STOVE, "گاز"),
    DeductionInfo(DeductionKey.WASHER, "ماشین لباسشویی"),
    DeductionInfo(DeductionKey.OTHER, "سایر موارد")
)

val ACCESSORY_SUGGESTIONS = listOf(
    AccessorySuggestion("لولا آرام‌بند", 85_000.0),
    AccessorySuggestion("ریل تاندم‌ باکس", 850_000.0),
    AccessorySuggestion("دستگیره کابینت", 120_000.0),
    AccessorySuggestion("جک کابینت", 450_000.0),
    AccessorySuggestion("سبد سوپرمارکتی", 1_900_000.0),
    AccessorySuggestion("سبد زیر سینک", 650_000.0),
    AccessorySuggestion("سینک استیل", 4_200_000.0),
    AccessorySuggestion("شیرآلات آشپزخانه", 2_800_000.0),
    AccessorySuggestion("چراغ سنسوردار", 350_000.0),
    AccessorySuggestion("پایه کابینت", 45_000.0),
    AccessorySuggestion("مگنت کابینت", 35_000.0),
    AccessorySuggestion("سطل زباله توکار", 1_600_000.0)
)

/**
 * مدل‌های کمد (مدروار) موجود در بازار ایران.
 * هر مدل ضریب مخصوص به خود دارد و فرمول محاسبه آن به‌صورت خودکار نمایش داده می‌شود.
 */
val WARDROBE_MODELS = listOf(
    WardrobeModel(
        "komod_akband",
        "کمد آکبند (KD)",
        "آماده‌ساز؛ نصب سریع و جابه‌جایی آسان",
        1.00,
        ""
    ),
    WardrobeModel(
        "komod_do_dar_malan",
        "کمد دو درب ملانی",
        "دو درب ملانی؛ فضای استاندارد آپارتمانی",
        1.10,
        ""
    ),
    WardrobeModel(
        "komod_se_dar_malan",
        "کمد سه درب ملانی",
        "سه درب ملانی؛ فضای ذخیره‌سازی بیشتر",
        1.15,
        ""
    ),
    WardrobeModel(
        "komod_dar_shoyi_shishe",
        "کمد درب شویی شیشه‌ای",
        "درب شویی با شیشه سکوریت",
        1.25,
        ""
    ),
    WardrobeModel(
        "komod_dar_shoyi_pto",
        "کمد درب شویی پتو",
        "درب شویی با نما پتو",
        1.28,
        ""
    ),
    WardrobeModel(
        "komod_panjere",
        "کمد پنجره‌ای",
        "پنجره‌ای رو می‌نشین؛ فضای رویت بالا",
        1.05,
        ""
    ),
    WardrobeModel(
        "komod_fantazi_kshoyi",
        "کمد فانتزی کشویی",
        "فانتزی با کشوی‌های پیچیده",
        1.35,
        ""
    ),
    WardrobeModel(
        "komod_moder",
        "کمد مدرن (هویز ملانی)",
        "هویز ملانی؛ پایه لوله‌ای مدرن",
        1.30,
        ""
    ),
    WardrobeModel(
        "komod_do_pale",
        "کمد دو پله",
        "دو پله؛ استاندارد مسکونی",
        1.20,
        ""
    ),
    WardrobeModel(
        "komod_lemak",
        "کمد برند لمک (آکبند)",
        "برند لمک؛ کیفیت بالا و ساخت یک‌تکه",
        1.12,
        ""
    )
).map { model ->
    model.copy(
        formula = "هزینه = عرض کمد × قیمت هر متر × ${toPersianDigits(model.coefficient.toString())} + اجناس (کشوی، قفسه)"
    )
}

/** توضیح و فرمول هر ماژول محاسباتی ویژه */
val MODULE_FORMULAS = mapOf(
    ModuleKey.DOUBLE_TIER to "هزینه = طول پله ۱ × قیمت هر متر پله ۱ + طول پله ۲ × قیمت هر متر پله ۲",
    ModuleKey.COFFEE_BAR to "هزینه = قیمت پایه + طول × قیمت هر متر + طول صفحه × نرخ هر متر صفحه",
    ModuleKey.WARDROBE to "هزینه = عرض کمد × قیمت هر متر × ضریب مدل + اجناس (کشوی، قفسه)"
)
