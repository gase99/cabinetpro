import type {
  CabinetForm,
  DeductionKey,
  SectionKey,
  WardrobeModel,
} from "./types";
import { todayISO, toPersianDigits } from "./utils/format";

export const SECTIONS: {
  key: SectionKey;
  label: string;
  short: string;
  description: string;
  color: string;
}[] = [
  {
    key: "ground",
    label: "کابینت زمینی",
    short: "زمینی",
    description: "کابینت زیر صفحه",
    color: "#233b57",
  },
  {
    key: "wall",
    label: "کابینت دیواری",
    short: "دیواری",
    description: "کابینت هوایی",
    color: "#c89b58",
  },
  {
    key: "tall",
    label: "کابینت ایستاده",
    short: "ایستاده",
    description: "کابینت کمدی",
    color: "#72968a",
  },
  {
    key: "island",
    label: "جزیره / اپن",
    short: "جزیره / اپن",
    description: "کابینت جزیره‌ای",
    color: "#a9b5c3",
  },
];

export const DEDUCTIONS: { key: DeductionKey; label: string }[] = [
  { key: "fridge", label: "یخچال" },
  { key: "stove", label: "گاز" },
  { key: "washer", label: "ماشین لباسشویی" },
  { key: "other", label: "سایر موارد" },
];

export const ACCESSORY_SUGGESTIONS = [
  { name: "لولا آرام‌بند", price: 85000 },
  { name: "ریل تاندم‌ باکس", price: 850000 },
  { name: "دستگیره کابینت", price: 120000 },
  { name: "جک کابینت", price: 450000 },
  { name: "سبد سوپرمارکتی", price: 1900000 },
  { name: "سبد زیر سینک", price: 650000 },
  { name: "سینک استیل", price: 4200000 },
  { name: "شیرآلات آشپزخانه", price: 2800000 },
  { name: "چراغ سنسوردار", price: 350000 },
  { name: "پایه کابینت", price: 45000 },
  { name: "مگنت کابینت", price: 35000 },
  { name: "سطل زباله توکار", price: 1600000 },
];

/**
 * مدل‌های کمد (مدروار) موجود در بازار ایران.
 * هر مدل ضریب مخصوص به خود دارد و فرمول محاسبه آن به‌صورت خودکار نمایش داده می‌شود.
 */
export const WARDROBE_MODELS: WardrobeModel[] = [
  {
    id: "komod_akband",
    name: "کمد آکبند (KD)",
    note: "آماده‌ساز؛ نصب سریع و جابه‌جایی آسان",
    coefficient: 1.0,
    formula: "",
  },
  {
    id: "komod_do_dar_malan",
    name: "کمد دو درب ملانی",
    note: "دو درب ملانی؛ فضای استاندارد آپارتمانی",
    coefficient: 1.1,
    formula: "",
  },
  {
    id: "komod_se_dar_malan",
    name: "کمد سه درب ملانی",
    note: "سه درب ملانی؛ فضای ذخیره‌سازی بیشتر",
    coefficient: 1.15,
    formula: "",
  },
  {
    id: "komod_dar_shoyi_shishe",
    name: "کمد درب شویی شیشه‌ای",
    note: "درب شویی با شیشه سکوریت",
    coefficient: 1.25,
    formula: "",
  },
  {
    id: "komod_dar_shoyi_pto",
    name: "کمد درب شویی پتو",
    note: "درب شویی با نما پتو",
    coefficient: 1.28,
    formula: "",
  },
  {
    id: "komod_panjere",
    name: "کمد پنجره‌ای",
    note: "پنجره‌ای رو می‌نشین؛ فضای رویت بالا",
    coefficient: 1.05,
    formula: "",
  },
  {
    id: "komod_fantazi_kshoyi",
    name: "کمد فانتزی کشویی",
    note: "فانتزی با کشوی‌های پیچیده",
    coefficient: 1.35,
    formula: "",
  },
  {
    id: "komod_moder",
    name: "کمد مدرن (هویز ملانی)",
    note: "هویز ملانی؛ پایه لوله‌ای مدرن",
    coefficient: 1.3,
    formula: "",
  },
  {
    id: "komod_do_pale",
    name: "کمد دو پله",
    note: "دو پله؛ استاندارد مسکونی",
    coefficient: 1.2,
    formula: "",
  },
  {
    id: "komod_lemak",
    name: "کمد برند لمک (آکبند)",
    note: "برند لمک؛ کیفیت بالا و ساخت یک‌تکه",
    coefficient: 1.12,
    formula: "",
  },
].map((model) => ({
  ...model,
  formula: `هزینه = عرض کمد × قیمت هر متر × ${toPersianDigits(
    model.coefficient.toString(),
  )} + اجناس (کشوی، قفسه)`,
}));

/** توضیح و فرمول هر ماژول محاسباتی ویژه */
export const MODULE_FORMULAS = {
  doubleTier:
    "هزینه = طول پله ۱ × قیمت هر متر پله ۱ + طول پله ۲ × قیمت هر متر پله ۲",
  coffeeBar:
    "هزینه = قیمت پایه + طول × قیمت هر متر + طول صفحه × نرخ هر متر صفحه",
  wardrobe:
    "هزینه = عرض کمد × قیمت هر متر × ضریب مدل + اجناس (کشوی، قفسه)",
} as const;

export function createDefaultForm(): CabinetForm {
  return {
    name: "",
    date: todayISO(),
    basePrice: 6500000,
    coefficients: { ground: 1, wall: 0.6, tall: 2, island: 1 },
    lengths: { ground: 3, wall: 3, tall: 1, island: 0 },
    deductions: { fridge: 0, stove: 0, washer: 0, other: 0 },
    countertopLength: 0,
    countertopRate: 0,
    accessories: [],
    installationMode: "fixed",
    installationValue: 0,
    transportCost: 0,
    discountMode: "fixed",
    discountValue: 0,
    taxPercent: 0,
    doubleTierEnabled: false,
    doubleTier: { tier1Length: 0, tier1Price: 0, tier2Length: 0, tier2Price: 0 },
    coffeeBarEnabled: false,
    coffeeBar: {
      basePrice: 0,
      length: 0,
      pricePerMeter: 0,
      countertopLength: 0,
      countertopRate: 0,
    },
    wardrobeEnabled: false,
    wardrobe: {
      modelId: "komod_akband",
      width: 0,
      unitPrice: 0,
      drawers: 0,
      drawerPrice: 0,
      shelves: 0,
      shelfPrice: 0,
    },
  };
}

export function createEmptyForm(): CabinetForm {
  return {
    ...createDefaultForm(),
    name: "",
    basePrice: 0,
    lengths: { ground: 0, wall: 0, tall: 0, island: 0 },
  };
}
