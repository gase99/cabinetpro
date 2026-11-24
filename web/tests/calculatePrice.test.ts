import assert from "node:assert/strict";
import { test } from "node:test";
import { createDefaultForm, createEmptyForm } from "../src/data";
import {
  calculateCabinetPrice,
  validateForm,
} from "../src/utils/calculatePrice";

// داده‌های پذیرش کاربر: ۳ + ۱٫۸ + ۲ = ۶٫۸ متر.
test("acceptance: effective meters and cabinet cost", () => {
  const result = calculateCabinetPrice(createDefaultForm());
  assert.deepEqual(result.effectiveMeters, {
    ground: 3,
    wall: 1.8,
    tall: 2,
    island: 0,
    total: 6.8,
  });
  assert.equal(result.cabinetCost, 44200000);
  assert.equal(result.total, 44200000);
});

test("deductions reduce only ground length", () => {
  const form = createDefaultForm();
  form.deductions = { fridge: 0.6, stove: 0.6, washer: 0.3, other: 0.1 };
  const result = calculateCabinetPrice(form);
  assert.equal(result.deductionsTotal, 1.6);
  assert.equal(result.groundNet, 1.4);
  assert.equal(result.effectiveMeters.wall, 1.8);
  assert.equal(result.effectiveMeters.total, 5.2);
});

test("all coefficients are editable and island is included", () => {
  const form = createDefaultForm();
  form.coefficients = { ground: 1.5, wall: 0.8, tall: 1.2, island: 2 };
  form.lengths.island = 2;
  const result = calculateCabinetPrice(form);
  assert.equal(result.effectiveMeters.ground, 4.5);
  assert.equal(result.effectiveMeters.wall, 2.4);
  assert.equal(result.effectiveMeters.tall, 1.2);
  assert.equal(result.effectiveMeters.island, 4);
  assert.equal(result.effectiveMeters.total, 12.1);
});

test("countertop and accessory costs are separate from cabinet meters", () => {
  const form = createDefaultForm();
  form.countertopLength = 3;
  form.countertopRate = 1000000;
  form.accessories = [{ id: "a", name: "لولا", quantity: 4, unitPrice: 85000 }];
  const result = calculateCabinetPrice(form);
  assert.equal(result.countertopCost, 3000000);
  assert.equal(result.accessoriesCost, 340000);
  assert.equal(result.total, 47540000);
});

test("percentage installation is based only on cabinet cost", () => {
  const form = createDefaultForm();
  form.installationMode = "percent";
  form.installationValue = 10;
  form.countertopLength = 2;
  form.countertopRate = 1000000;
  const result = calculateCabinetPrice(form);
  assert.equal(result.installationCost, 4420000);
  assert.equal(result.total, 50620000);
});

test("discount is applied before tax", () => {
  const form = createDefaultForm();
  form.transportCost = 800000;
  form.discountMode = "percent";
  form.discountValue = 10;
  form.taxPercent = 10;
  const result = calculateCabinetPrice(form);
  assert.equal(result.subtotal, 45000000);
  assert.equal(result.discountAmount, 4500000);
  assert.equal(result.taxAmount, 4050000);
  assert.equal(result.total, 44550000);
});

test("fixed discount and tax", () => {
  const form = createDefaultForm();
  form.discountValue = 200000;
  form.taxPercent = 9;
  const result = calculateCabinetPrice(form);
  assert.equal(result.discountAmount, 200000);
  assert.equal(result.taxAmount, 3960000);
  assert.equal(result.total, 47960000);
});

test("empty lengths produce a useful validation message", () => {
  assert.ok(validateForm(createEmptyForm()).lengths);
  assert.equal(calculateCabinetPrice(createEmptyForm()).total, 0);
});

test("zero base price is allowed and still calculates", () => {
  const form = createDefaultForm();
  form.basePrice = 0;
  assert.equal(validateForm(form).basePrice, undefined);
  assert.equal(calculateCabinetPrice(form).effectiveMeters.total, 6.8);
  assert.equal(calculateCabinetPrice(form).total, 0);
});

test("negative numbers and excess deductions are rejected", () => {
  const form = createDefaultForm();
  form.lengths.ground = -1;
  form.basePrice = -100;
  form.deductions.fridge = 4;
  const errors = validateForm(form);
  assert.ok(errors["lengths.ground"]);
  assert.ok(errors.basePrice);
  assert.ok(errors.deductions);
  assert.equal(calculateCabinetPrice(form).groundNet, 0);
});

test("percentages must stay between zero and 100", () => {
  const form = createDefaultForm();
  form.installationMode = "percent";
  form.installationValue = 101;
  form.discountMode = "percent";
  form.discountValue = 101;
  form.taxPercent = 101;
  const errors = validateForm(form);
  assert.ok(errors.installationValue);
  assert.ok(errors.discountValue);
  assert.ok(errors.taxPercent);
});

test("discount cannot exceed subtotal", () => {
  const form = createDefaultForm();
  form.discountValue = 50000000;
  assert.ok(validateForm(form).discountValue);
  assert.equal(calculateCabinetPrice(form).total, 0);
});

test("decimal deduction equality is not a validation error", () => {
  const form = createDefaultForm();
  form.lengths.ground = 0.3;
  form.deductions.fridge = 0.1;
  form.deductions.stove = 0.2;
  assert.equal(validateForm(form).deductions, undefined);
});

test("calculation does not mutate input", () => {
  const form = createDefaultForm();
  const snapshot = JSON.stringify(form);
  calculateCabinetPrice(form);
  assert.equal(JSON.stringify(form), snapshot);
});

test("double tier module computes each tier separately", () => {
  const form = createDefaultForm();
  form.doubleTierEnabled = true;
  form.doubleTier = {
    tier1Length: 2,
    tier1Price: 8000000,
    tier2Length: 1.5,
    tier2Price: 6000000,
  };
  const result = calculateCabinetPrice(form);
  assert.equal(result.doubleTierCost, 25000000); // 2*8M + 1.5*6M
  assert.equal(result.moduleCost, 25000000);
  assert.equal(result.subtotal, 44200000 + 25000000);
  assert.equal(result.total, 69200000);
});

test("coffee bar module formula", () => {
  const form = createDefaultForm();
  form.coffeeBarEnabled = true;
  form.coffeeBar = {
    basePrice: 5000000,
    length: 2,
    pricePerMeter: 12000000,
    countertopLength: 2,
    countertopRate: 4000000,
  };
  const result = calculateCabinetPrice(form);
  assert.equal(result.coffeeBarCost, 37000000); // 5M + 2*12M + 2*4M
  assert.equal(result.coffeeBarDetail.main, 29000000);
  assert.equal(result.coffeeBarDetail.accessories, 8000000);
});

test("wardrobe module uses model coefficient and formula", () => {
  const form = createDefaultForm();
  form.wardrobeEnabled = true;
  form.wardrobe = {
    modelId: "komod_se_dar_malan",
    width: 2,
    unitPrice: 9000000,
    drawers: 2,
    drawerPrice: 1600000,
    shelves: 3,
    shelfPrice: 250000,
  };
  const result = calculateCabinetPrice(form);
  assert.equal(result.wardrobeDetail.main, 20700000); // 2 * 9M * 1.15
  assert.equal(result.wardrobeDetail.accessories, 3950000);
  assert.equal(result.wardrobeCost, 24650000);
  assert.equal(result.wardrobeModelName, "کمد سه درب ملانی");
  assert.ok(result.wardrobeFormula.includes("۱.۱۵"));
  assert.equal(result.moduleCost, 24650000);
});

test("inactive modules do not affect the invoice total", () => {
  const form = createDefaultForm();
  form.doubleTier = {
    tier1Length: 2,
    tier1Price: 8000000,
    tier2Length: 1.5,
    tier2Price: 6000000,
  };
  const result = calculateCabinetPrice(form);
  assert.equal(result.moduleCost, 0);
  assert.equal(result.doubleTierCost, 25000000); // محاسبه وجود دارد اما در جمع گنجانده نمی‌شود
  assert.equal(result.total, 44200000);
});

test("wardrobe enabled without width shows validation error", () => {
  const form = createDefaultForm();
  form.wardrobeEnabled = true;
  assert.ok(validateForm(form).wardrobe);
});

test("double tier enabled without any length shows validation error", () => {
  const form = createDefaultForm();
  form.doubleTierEnabled = true;
  assert.ok(validateForm(form).doubleTier);
});
