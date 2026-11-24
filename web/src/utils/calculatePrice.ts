import type {
  CabinetForm,
  CalculationResult,
  FieldErrors,
  NumericValue,
  ModuleCost,
} from "../types";
import { WARDROBE_MODELS } from "../data";

export const numeric = (value: NumericValue | undefined): number => {
  const result = Number(value ?? 0);
  return Number.isFinite(result) ? result : 0;
};

const nonNegative = (value: NumericValue | undefined): number =>
  Math.max(0, numeric(value));
const precise = (value: number): number =>
  Math.round((value + Number.EPSILON) * 1e10) / 1e10;

// تابع خالص: کسرها فقط از طول زمینی کم می‌شوند.
export function calculateCabinetPrice(form: CabinetForm): CalculationResult {
  const deductionsTotal = precise(
    Object.values(form.deductions).reduce<number>(
      (sum, value) => sum + nonNegative(value),
      0,
    ),
  );
  const groundNet = precise(
    Math.max(0, nonNegative(form.lengths.ground) - deductionsTotal),
  );
  const effectiveMeters = {
    ground: precise(groundNet * nonNegative(form.coefficients.ground)),
    wall: precise(
      nonNegative(form.lengths.wall) * nonNegative(form.coefficients.wall),
    ),
    tall: precise(
      nonNegative(form.lengths.tall) * nonNegative(form.coefficients.tall),
    ),
    island: precise(
      nonNegative(form.lengths.island) * nonNegative(form.coefficients.island),
    ),
    total: 0,
  };
  effectiveMeters.total = precise(
    effectiveMeters.ground +
      effectiveMeters.wall +
      effectiveMeters.tall +
      effectiveMeters.island,
  );

  const cabinetCost = effectiveMeters.total * nonNegative(form.basePrice);
  const countertopCost =
    nonNegative(form.countertopLength) * nonNegative(form.countertopRate);
  const accessoriesCost = form.accessories.reduce(
    (sum, item) =>
      sum + nonNegative(item.quantity) * nonNegative(item.unitPrice),
    0,
  );
  const installationCost =
    form.installationMode === "percent"
      ? (cabinetCost * Math.min(100, nonNegative(form.installationValue))) / 100
      : nonNegative(form.installationValue);
  const transportCost = nonNegative(form.transportCost);

  // ---- ماژول ۱: کابینت دو پله ----
  const doubleTierMain = precise(
    nonNegative(form.doubleTier.tier1Length) *
      nonNegative(form.doubleTier.tier1Price) +
      nonNegative(form.doubleTier.tier2Length) *
      nonNegative(form.doubleTier.tier2Price),
  );
  const doubleTierDetail: ModuleCost = {
    main: doubleTierMain,
    accessories: 0,
    total: doubleTierMain,
  };

  // ---- ماژول ۲: کافی بار ----
  const coffeeBarMainPart = precise(
    nonNegative(form.coffeeBar.length) * nonNegative(form.coffeeBar.pricePerMeter),
  );
  const coffeeBarAccessories = precise(
    nonNegative(form.coffeeBar.countertopLength) *
      nonNegative(form.coffeeBar.countertopRate),
  );
  const coffeeBarTotal = precise(
    nonNegative(form.coffeeBar.basePrice) + coffeeBarMainPart + coffeeBarAccessories,
  );
  const coffeeBarDetail: ModuleCost = {
    main: precise(nonNegative(form.coffeeBar.basePrice) + coffeeBarMainPart),
    accessories: coffeeBarAccessories,
    total: coffeeBarTotal,
  };

  // ---- ماژول ۳: کمد (مدروار) ----
  const wardrobeModel =
    WARDROBE_MODELS.find((model) => model.id === form.wardrobe.modelId) ??
    WARDROBE_MODELS[0];
  const wardrobeMain = precise(
    nonNegative(form.wardrobe.width) *
      nonNegative(form.wardrobe.unitPrice) *
      wardrobeModel.coefficient,
  );
  const wardrobeAccessories = precise(
    nonNegative(form.wardrobe.drawers) * nonNegative(form.wardrobe.drawerPrice) +
      nonNegative(form.wardrobe.shelves) * nonNegative(form.wardrobe.shelfPrice),
  );
  const wardrobeDetail: ModuleCost = {
    main: wardrobeMain,
    accessories: wardrobeAccessories,
    total: precise(wardrobeMain + wardrobeAccessories),
  };

  // ---- جمع ماژول‌های فعال ----
  const moduleCost = precise(
    (form.doubleTierEnabled ? doubleTierDetail.total : 0) +
      (form.coffeeBarEnabled ? coffeeBarDetail.total : 0) +
      (form.wardrobeEnabled ? wardrobeDetail.total : 0),
  );

  const subtotal =
    cabinetCost +
    countertopCost +
    accessoriesCost +
    installationCost +
    transportCost +
    moduleCost;
  const discountRaw =
    form.discountMode === "percent"
      ? (subtotal * Math.min(100, nonNegative(form.discountValue))) / 100
      : nonNegative(form.discountValue);
  const discountAmount = Math.min(subtotal, discountRaw);
  // مالیات پس از کسر تخفیف، روی جمع هزینه‌ها محاسبه می‌شود.
  const taxAmount =
    ((subtotal - discountAmount) *
      Math.min(100, nonNegative(form.taxPercent))) /
    100;
  const total = subtotal - discountAmount + taxAmount;

  return {
    effectiveMeters,
    cabinetCost,
    countertopCost,
    accessoriesCost,
    installationCost,
    transportCost,
    discountAmount,
    taxAmount,
    total,
    subtotal,
    groundNet,
    deductionsTotal,
    doubleTierDetail,
    coffeeBarDetail,
    wardrobeDetail,
    doubleTierCost: doubleTierDetail.total,
    coffeeBarCost: coffeeBarDetail.total,
    wardrobeCost: wardrobeDetail.total,
    wardrobeModelName: wardrobeModel.name,
    wardrobeFormula: wardrobeModel.formula,
    moduleCost,
    includeDoubleTier: form.doubleTierEnabled,
    includeCoffeeBar: form.coffeeBarEnabled,
    includeWardrobe: form.wardrobeEnabled,
  };
}

export function validateForm(form: CabinetForm): FieldErrors {
  const errors: FieldErrors = {};
  const checkNonNegative = (key: string, value: NumericValue) => {
    if (numeric(value) < 0) errors[key] = "مقدار نمی‌تواند منفی باشد.";
    if (!Number.isFinite(numeric(value)))
      errors[key] = "لطفاً یک عدد معتبر وارد کنید.";
  };
  checkNonNegative("basePrice", form.basePrice);
  Object.entries(form.lengths).forEach(([key, value]) =>
    checkNonNegative(`lengths.${key}`, value),
  );
  Object.entries(form.coefficients).forEach(([key, value]) =>
    checkNonNegative(`coefficients.${key}`, value),
  );
  Object.entries(form.deductions).forEach(([key, value]) =>
    checkNonNegative(`deductions.${key}`, value),
  );
  [
    "countertopLength",
    "countertopRate",
    "installationValue",
    "transportCost",
    "discountValue",
    "taxPercent",
  ].forEach((key) => {
    checkNonNegative(key, form[key as keyof CabinetForm] as NumericValue);
  });

  // ماژول کابینت دو پله
  const moduleFields: [string, (form: CabinetForm) => NumericValue][] = [
    ["doubleTier.tier1Length", (f) => f.doubleTier.tier1Length],
    ["doubleTier.tier1Price", (f) => f.doubleTier.tier1Price],
    ["doubleTier.tier2Length", (f) => f.doubleTier.tier2Length],
    ["doubleTier.tier2Price", (f) => f.doubleTier.tier2Price],
    ["coffeeBar.basePrice", (f) => f.coffeeBar.basePrice],
    ["coffeeBar.length", (f) => f.coffeeBar.length],
    ["coffeeBar.pricePerMeter", (f) => f.coffeeBar.pricePerMeter],
    ["coffeeBar.countertopLength", (f) => f.coffeeBar.countertopLength],
    ["coffeeBar.countertopRate", (f) => f.coffeeBar.countertopRate],
    ["wardrobe.width", (f) => f.wardrobe.width],
    ["wardrobe.unitPrice", (f) => f.wardrobe.unitPrice],
    ["wardrobe.drawerPrice", (f) => f.wardrobe.drawerPrice],
    ["wardrobe.shelfPrice", (f) => f.wardrobe.shelfPrice],
  ];
  moduleFields.forEach(([key, get]) => checkNonNegative(key, get(form)));

  if (
    form.doubleTierEnabled &&
    nonNegative(form.doubleTier.tier1Length) <= 0 &&
    nonNegative(form.doubleTier.tier2Length) <= 0
  )
    errors.doubleTier =
      "برای محاسبه دو پله، حداقل طول یک پله را وارد کنید.";

  if (
    form.coffeeBarEnabled &&
    nonNegative(form.coffeeBar.basePrice) <= 0 &&
    nonNegative(form.coffeeBar.length) <= 0
  )
    errors.coffeeBar = "برای محاسبه کافی بار، قیمت پایه یا طول را وارد کنید.";

  if (form.wardrobeEnabled && nonNegative(form.wardrobe.width) <= 0)
    errors.wardrobe = "برای محاسبه کمد، عرض کمد را وارد کنید.";
  if (nonNegative(form.wardrobe.drawers) < 0)
    errors.wardrobeDrawers = "تعداد کشوی نمی‌تواند منفی باشد.";
  if (nonNegative(form.wardrobe.shelves) < 0)
    errors.wardrobeShelves = "تعداد قفسه نمی‌تواند منفی باشد.";
  if (
    form.installationMode === "percent" &&
    numeric(form.installationValue) > 100
  )
    errors.installationValue = "درصد باید بین ۰ تا ۱۰۰ باشد.";
  if (form.discountMode === "percent" && numeric(form.discountValue) > 100)
    errors.discountValue = "درصد باید بین ۰ تا ۱۰۰ باشد.";
  if (numeric(form.taxPercent) > 100)
    errors.taxPercent = "درصد باید بین ۰ تا ۱۰۰ باشد.";
  const deductions = precise(
    Object.values(form.deductions).reduce<number>(
      (sum, value) => sum + numeric(value),
      0,
    ),
  );
  if (deductions > precise(numeric(form.lengths.ground)))
    errors.deductions = "مجموع کسرها نمی‌تواند از طول کابینت زمینی بیشتر باشد.";
  if (!Object.values(form.lengths).some((value) => numeric(value) > 0))
    errors.lengths = "برای محاسبه، حداقل طول یک بخش را وارد کنید.";
  form.accessories.forEach((item) => {
    if (numeric(item.quantity) < 0)
      errors[`accessory.${item.id}.quantity`] = "تعداد نمی‌تواند منفی باشد.";
    if (numeric(item.unitPrice) < 0)
      errors[`accessory.${item.id}.unitPrice`] = "قیمت نمی‌تواند منفی باشد.";
  });
  if (
    form.discountMode === "fixed" &&
    numeric(form.discountValue) > calculateCabinetPrice(form).subtotal
  ) {
    errors.discountValue = "تخفیف نمی‌تواند بیشتر از جمع هزینه‌ها باشد.";
  }
  return errors;
}
