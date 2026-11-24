const persianDigits = ["۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹"];

export function toPersianDigits(value: string | number): string {
  return String(value).replace(
    /[0-9]/g,
    (digit) => persianDigits[Number(digit)],
  );
}

export function toEnglishDigits(value: string): string {
  return value
    .replace(/[۰-۹]/g, (digit) => String("۰۱۲۳۴۵۶۷۸۹".indexOf(digit)))
    .replace(/[٠-٩]/g, (digit) => String("٠١٢٣٤٥٦٧٨٩".indexOf(digit)))
    .replace(/[,٬\s]/g, "")
    .replace(/٫/g, ".");
}

export function formatNumber(value: number, decimals = 2): string {
  if (!Number.isFinite(value)) return "۰";
  return new Intl.NumberFormat("fa-IR", {
    maximumFractionDigits: decimals,
    minimumFractionDigits: 0,
  }).format(value);
}

export function formatMoney(value: number): string {
  return formatNumber(Math.round(value), 0);
}

export function formatInput(value: number | "", grouped = false): string {
  if (value === "") return "";
  if (grouped) return formatNumber(value, 2);
  return toPersianDigits(String(value)).replace(".", "٫");
}

export function todayISO(): string {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

export function formatDate(value: string): string {
  if (!value) return "انتخاب تاریخ";
  const date = new Date(`${value}T12:00:00`);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("fa-IR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(date);
}

export function formatSavedTime(value: string): string {
  return new Intl.DateTimeFormat("fa-IR", {
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}
