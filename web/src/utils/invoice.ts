import type { CabinetForm, CalculationResult } from "../types";
import { SECTIONS } from "../data";
import { formatDate, formatMoney, formatNumber } from "./format";

export function buildInvoiceText(
  form: CabinetForm,
  result: CalculationResult,
): string {
  const lines = [
    "محاسبه‌گر قیمت کابینت",
    "خلاصه فاکتور",
    `پروژه: ${form.name || "پروژه بدون نام"}`,
    `تاریخ: ${formatDate(form.date)}`,
    "────────────────────",
    `قیمت هر متر کابینت پایه: ${formatMoney(Number(form.basePrice))} تومان`,
    ...SECTIONS.map(
      (section) =>
        `${section.label}: ${formatNumber(result.effectiveMeters[section.key])} متر (طول ${formatNumber(section.key === "ground" ? result.groundNet : Number(form.lengths[section.key]))} × ضریب ${formatNumber(Number(form.coefficients[section.key]))})`,
    ),
    `کسر طول زمینی: ${formatNumber(result.deductionsTotal)} متر`,
    `متراژ مؤثر کل: ${formatNumber(result.effectiveMeters.total)} متر`,
    "────────────────────",
    `هزینه کابینت: ${formatMoney(result.cabinetCost)} تومان`,
    `هزینه صفحه کابینت: ${formatMoney(result.countertopCost)} تومان`,
    `جمع یراق‌آلات: ${formatMoney(result.accessoriesCost)} تومان`,
    ...form.accessories.map(
      (item) =>
        `  ${item.name || "تجهیز"} × ${formatNumber(Number(item.quantity))}: ${formatMoney(Number(item.quantity) * Number(item.unitPrice))} تومان`,
    ),
    // ---- محاسبات ویژه (ماژول‌های فعال) ----
    ...(form.doubleTierEnabled && result.doubleTierCost > 0
      ? [
          "────────────────────",
          "【 محاسبات ویژه ۱ — کابینت دو پله 】",
          `پله اول: ${formatNumber(Number(form.doubleTier.tier1Length))} متر × ${formatMoney(Number(form.doubleTier.tier1Price))} تومان = ${formatMoney(Number(form.doubleTier.tier1Length) * Number(form.doubleTier.tier1Price))} تومان`,
          `پله دوم: ${formatNumber(Number(form.doubleTier.tier2Length))} متر × ${formatMoney(Number(form.doubleTier.tier2Price))} تومان = ${formatMoney(Number(form.doubleTier.tier2Length) * Number(form.doubleTier.tier2Price))} تومان`,
          `جمع کابینت دو پله: ${formatMoney(result.doubleTierCost)} تومان`,
        ]
      : []),
    ...(form.coffeeBarEnabled && result.coffeeBarCost > 0
      ? [
          "────────────────────",
          "【 محاسبات ویژه ۲ — کافی بار (کافه‌بار) 】",
          `قیمت پایه: ${formatMoney(Number(form.coffeeBar.basePrice))} تومان`,
          `کابینت: ${formatNumber(Number(form.coffeeBar.length))} متر × ${formatMoney(Number(form.coffeeBar.pricePerMeter))} تومان = ${formatMoney(Number(form.coffeeBar.length) * Number(form.coffeeBar.pricePerMeter))} تومان`,
          `صفحه کانتر: ${formatNumber(Number(form.coffeeBar.countertopLength))} متر × ${formatMoney(Number(form.coffeeBar.countertopRate))} تومان = ${formatMoney(Number(form.coffeeBar.countertopLength) * Number(form.coffeeBar.countertopRate))} تومان`,
          `جمع کافی بار: ${formatMoney(result.coffeeBarCost)} تومان`,
        ]
      : []),
    ...(form.wardrobeEnabled && result.wardrobeCost > 0
      ? [
          "────────────────────",
          "【 محاسبات ویژه ۳ — کمد (مدروار) 】",
          `مدل: ${result.wardrobeModelName}`,
          `فرمول: ${result.wardrobeFormula}`,
          `بخش اصلی: ${formatNumber(Number(form.wardrobe.width))} متر × ${formatMoney(Number(form.wardrobe.unitPrice))} تومان × ضریب = ${formatMoney(result.wardrobeDetail.main)} تومان`,
          `اجناس: کشوی ${formatNumber(Number(form.wardrobe.drawers))} × ${formatMoney(Number(form.wardrobe.drawerPrice))} تومان + قفسه ${formatNumber(Number(form.wardrobe.shelves))} × ${formatMoney(Number(form.wardrobe.shelfPrice))} تومان = ${formatMoney(result.wardrobeDetail.accessories)} تومان`,
          `جمع کمد: ${formatMoney(result.wardrobeCost)} تومان`,
        ]
      : []),
    `هزینه نصب: ${formatMoney(result.installationCost)} تومان`,
    `هزینه حمل: ${formatMoney(result.transportCost)} تومان`,
    `تخفیف: ${formatMoney(result.discountAmount)} تومان`,
    `مالیات: ${formatMoney(result.taxAmount)} تومان`,
    "────────────────────",
    `مبلغ نهایی قابل پرداخت: ${formatMoney(result.total)} تومان`,
    "این برآورد با محاسبه‌گر قیمت کابینت تهیه شده است.",
  ];
  return lines.join("\n");
}

export async function copyText(text: string): Promise<void> {
  if (navigator.clipboard?.writeText) {
    try {
      await navigator.clipboard.writeText(text);
      return;
    } catch {
      /* روش جایگزین برای مرورگرهای قدیمی */
    }
  }
  const textarea = document.createElement("textarea");
  textarea.value = text;
  textarea.style.position = "fixed";
  textarea.style.opacity = "0";
  document.body.appendChild(textarea);
  textarea.select();
  const copied = document.execCommand("copy");
  textarea.remove();
  if (!copied) throw new Error("Clipboard unavailable");
}
