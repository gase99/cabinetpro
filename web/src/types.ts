export type SectionKey = "ground" | "wall" | "tall" | "island";
export type NumericValue = number | "";
export type CostMode = "fixed" | "percent";
export type DeductionKey = "fridge" | "stove" | "washer" | "other";

export interface Accessory {
  id: string;
  name: string;
  quantity: NumericValue;
  unitPrice: NumericValue;
}

/** محاسبه کابینت دو پله: هر پله به صورت جداگانه طول × قیمت هر متر */
export interface DoubleTierData {
  tier1Length: NumericValue;
  tier1Price: NumericValue;
  tier2Length: NumericValue;
  tier2Price: NumericValue;
}

/** محاسبه کافی بار: قیمت پایه + طول × نرخ + صفحه کانتر */
export interface CoffeeBarData {
  basePrice: NumericValue;
  length: NumericValue;
  pricePerMeter: NumericValue;
  countertopLength: NumericValue;
  countertopRate: NumericValue;
}

/** مدل کمد با ضریب مخصوص و فرمول محاسبه آن */
export interface WardrobeModel {
  id: string;
  name: string;
  note: string;
  coefficient: number;
  formula: string;
}

/** محاسبه قیمت کمد (مدروار): عرض × قیمت هر متر × ضریب مدل + اجناس */
export interface WardrobeData {
  modelId: string;
  width: NumericValue;
  unitPrice: NumericValue;
  drawers: NumericValue;
  drawerPrice: NumericValue;
  shelves: NumericValue;
  shelfPrice: NumericValue;
}

/** جمع‌وجور هزینه یک ماژول محاسباتی */
export interface ModuleCost {
  main: number;
  accessories: number;
  total: number;
}

export interface CabinetForm {
  name: string;
  date: string;
  basePrice: NumericValue;
  coefficients: Record<SectionKey, NumericValue>;
  lengths: Record<SectionKey, NumericValue>;
  deductions: Record<DeductionKey, NumericValue>;
  countertopLength: NumericValue;
  countertopRate: NumericValue;
  accessories: Accessory[];
  installationMode: CostMode;
  installationValue: NumericValue;
  transportCost: NumericValue;
  discountMode: CostMode;
  discountValue: NumericValue;
  taxPercent: NumericValue;
  doubleTierEnabled: boolean;
  doubleTier: DoubleTierData;
  coffeeBarEnabled: boolean;
  coffeeBar: CoffeeBarData;
  wardrobeEnabled: boolean;
  wardrobe: WardrobeData;
}

export interface CalculationResult {
  effectiveMeters: Record<SectionKey, number> & { total: number };
  cabinetCost: number;
  countertopCost: number;
  accessoriesCost: number;
  installationCost: number;
  transportCost: number;
  discountAmount: number;
  taxAmount: number;
  total: number;
  subtotal: number;
  groundNet: number;
  deductionsTotal: number;
  doubleTierDetail: ModuleCost;
  coffeeBarDetail: ModuleCost;
  wardrobeDetail: ModuleCost;
  doubleTierCost: number;
  coffeeBarCost: number;
  wardrobeCost: number;
  wardrobeModelName: string;
  wardrobeFormula: string;
  moduleCost: number;
  includeDoubleTier: boolean;
  includeCoffeeBar: boolean;
  includeWardrobe: boolean;
}

export interface Draft {
  id: string;
  name: string;
  savedAt: string;
  form: CabinetForm;
}

export type FieldErrors = Record<string, string>;
