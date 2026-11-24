import type { CabinetForm, Draft } from "../types";
import { createDefaultForm } from "../data";

const CURRENT_KEY = "cabinet-calculator-current-v1";
const DRAFTS_KEY = "cabinet-calculator-drafts-v1";

export function readCurrent(): CabinetForm {
  try {
    const raw = localStorage.getItem(CURRENT_KEY);
    if (raw) return mergeForm(JSON.parse(raw));
  } catch {
    /* در صورت خراب بودن داده، فرم پیش‌فرض بازگردانده می‌شود. */
  }
  return createDefaultForm();
}

export function mergeForm(value: Partial<CabinetForm>): CabinetForm {
  const defaults = createDefaultForm();
  return {
    ...defaults,
    ...value,
    coefficients: { ...defaults.coefficients, ...value.coefficients },
    lengths: { ...defaults.lengths, ...value.lengths },
    deductions: { ...defaults.deductions, ...value.deductions },
    accessories: Array.isArray(value.accessories) ? value.accessories : [],
  };
}

export function saveCurrent(form: CabinetForm): boolean {
  try {
    localStorage.setItem(CURRENT_KEY, JSON.stringify(form));
    return true;
  } catch {
    return false;
  }
}

export function readDrafts(): Draft[] {
  try {
    const value = JSON.parse(localStorage.getItem(DRAFTS_KEY) || "[]");
    return Array.isArray(value)
      ? value
          .filter((item) => item && typeof item.id === "string" && item.form)
          .map((item) => ({ ...item, form: mergeForm(item.form) }))
      : [];
  } catch {
    return [];
  }
}

export function saveDrafts(drafts: Draft[]): boolean {
  try {
    localStorage.setItem(DRAFTS_KEY, JSON.stringify(drafts));
    return true;
  } catch {
    return false;
  }
}
