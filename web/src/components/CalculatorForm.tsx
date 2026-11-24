import { useState } from "react";
import {
  ChevronDown,
  CircleHelp,
  Info,
  Minus,
  Ruler,
  SlidersHorizontal,
  Sparkles,
} from "lucide-react";
import type {
  CabinetForm,
  CalculationResult,
  FieldErrors,
  NumericValue,
} from "../types";
import { DEDUCTIONS, MODULE_FORMULAS, SECTIONS, WARDROBE_MODELS } from "../data";
import { formatMoney, formatNumber } from "../utils/format";
import { numeric } from "../utils/calculatePrice";
import { CabinetIllustration } from "./CabinetIllustration";
import {
  DateField,
  InfoNote,
  ModeToggle,
  NumericField,
  SectionHeader,
} from "./UI";
import { AccessoryList } from "./AccessoryList";

interface Props {
  form: CabinetForm;
  setForm: React.Dispatch<React.SetStateAction<CabinetForm>>;
  result: CalculationResult;
  errors: FieldErrors;
}

export function CalculatorForm({ form, setForm, result, errors }: Props) {
  const [deductionsOpen, setDeductionsOpen] = useState(
    result.deductionsTotal > 0 || Boolean(errors.deductions),
  );
  const update = <K extends keyof CabinetForm>(key: K, value: CabinetForm[K]) =>
    setForm((old) => ({ ...old, [key]: value }));
  const updateNested = (
    group: "coefficients" | "lengths" | "deductions",
    key: string,
    value: NumericValue,
  ) => {
    setForm((old) => ({ ...old, [group]: { ...old[group], [key]: value } }));
  };
  const showDeductions = deductionsOpen;

  return (
    <div className="form-column">
      <section className="panel project-panel">
        <SectionHeader
          step="۱"
          title="اطلاعات پروژه"
          subtitle="شروع یک برآورد دقیق، با مشخصات پروژه شما"
          action={<span className="muted-badge">اختیاری</span>}
        />
        <div className="project-fields">
          <div className="field">
            <label htmlFor="project-name">نام مشتری / نام پروژه</label>
            <div className="input-wrap">
              <input
                id="project-name"
                type="text"
                value={form.name}
                maxLength={100}
                onChange={(event) => update("name", event.target.value)}
                placeholder="مثلاً: آشپزخانه آقای احمدی"
              />
            </div>
          </div>
          <DateField
            value={form.date}
            onChange={(value) => update("date", value)}
          />
        </div>
      </section>

      <section className="panel pricing-panel">
        <SectionHeader
          step="۲"
          title="قیمت پایه و ضرایب"
          subtitle="قیمت هر متر کابینت زمینی، مبنای محاسبه است"
          action={
            <span className="editable-badge">
              <SlidersHorizontal size={13} /> قابل ویرایش
            </span>
          }
        />
        <div className="pricing-controls">
          <div className="base-price-row">
            <NumericField
              id="basePrice"
              label="قیمت هر متر کابینت پایه"
              value={form.basePrice}
              onChange={(value) => update("basePrice", value)}
              suffix="تومان"
              grouped
              error={errors.basePrice}
            />
          </div>
          <div className="coefficients-grid">
            {SECTIONS.map((section) => (
              <NumericField
                key={section.key}
                id={`coefficients.${section.key}`}
                label={`ضریب ${section.short}`}
                value={form.coefficients[section.key]}
                onChange={(value) =>
                  updateNested("coefficients", section.key, value)
                }
                suffix="×"
                error={errors[`coefficients.${section.key}`]}
              />
            ))}
          </div>
        </div>
        {numeric(form.basePrice) === 0 && (
          <div className="inline-warning">
            <Info size={15} /> قیمت پایه صفر است؛ محاسبه انجام می‌شود اما هزینه
            کابینت صفر خواهد بود.
          </div>
        )}
      </section>

      <section className="panel lengths-panel">
        <SectionHeader
          step="۳"
          title="طول بخش‌های کابینت"
          subtitle="طول هر بخش را به متر وارد کنید"
          action={
            <span className="unit-badge">
              <Ruler size={14} /> واحد: متر
            </span>
          }
        />
        <div className="lengths-grid">
          {SECTIONS.map((section) => (
            <div className="length-card" key={section.key}>
              <div className="length-card-top">
                <CabinetIllustration section={section.key} />
                <span className="length-coefficient">
                  × {formatNumber(numeric(form.coefficients[section.key]))}
                </span>
              </div>
              <h3>{section.label}</h3>
              <p>{section.description}</p>
              <NumericField
                id={`lengths.${section.key}`}
                ariaLabel={`طول ${section.label}`}
                value={form.lengths[section.key]}
                onChange={(value) =>
                  updateNested("lengths", section.key, value)
                }
                suffix="متر"
                error={errors[`lengths.${section.key}`]}
              />
              <div className="effective-caption">
                متراژ مؤثر:{" "}
                <strong>
                  {formatNumber(result.effectiveMeters[section.key])}
                </strong>{" "}
                متر
              </div>
            </div>
          ))}
        </div>
        {errors.lengths && (
          <p className="field-error lengths-error">{errors.lengths}</p>
        )}
        <div className="length-footer">
          <CircleHelp size={15} />
          <span>متراژ مؤثر = طول بخش × ضریب همان بخش</span>
          <span className="length-total">
            جمع مؤثر:{" "}
            <strong>{formatNumber(result.effectiveMeters.total)} متر</strong>
          </span>
        </div>
      </section>

      <section
        className={`panel deductions-panel ${showDeductions ? "expanded" : ""}`}
      >
        <button
          className="collapsible-heading"
          onClick={() => setDeductionsOpen(!deductionsOpen)}
          aria-expanded={showDeductions}
          aria-controls="deductions-content"
        >
          <span className="section-heading-main">
            <span className="section-icon">
              <Minus size={20} />
            </span>
            <span>
              <span className="collapsible-title">
                کسر طول‌ها <span className="muted-badge">اختیاری</span>
              </span>
              <span className="collapsible-subtitle">
                یخچال، گاز و سایر مواردِ غیرکابینتی
              </span>
            </span>
          </span>
          <span className="collapse-action">
            <span>{showDeductions ? "بستن" : "افزودن کسر"}</span>
            <ChevronDown
              size={18}
              className={showDeductions ? "rotated" : ""}
            />
          </span>
        </button>
        {showDeductions && (
          <div id="deductions-content" className="deductions-content">
            <div className="four-fields">
              {DEDUCTIONS.map((item) => (
                <NumericField
                  key={item.key}
                  label={item.label}
                  value={form.deductions[item.key]}
                  onChange={(value) =>
                    updateNested("deductions", item.key, value)
                  }
                  suffix="متر"
                  error={errors[`deductions.${item.key}`]}
                />
              ))}
            </div>
            {errors.deductions ? (
              <p className="field-error">{errors.deductions}</p>
            ) : (
              <InfoNote>
                این مقدارها فقط از طول کابینت زمینی کسر می‌شوند. طول خالص زمینی:{" "}
                <strong>{formatNumber(result.groundNet)} متر</strong>
              </InfoNote>
            )}
          </div>
        )}
      </section>

      <section className="panel countertop-panel">
        <SectionHeader
          step="۴"
          title="صفحه کابینت (کانتر)"
          subtitle="هزینه صفحه، جدا از متراژ کابینت محاسبه می‌شود"
          action={<span className="muted-badge">اختیاری</span>}
        />
        <div className="two-fields">
          <NumericField
            label="طول صفحه کابینت"
            value={form.countertopLength}
            onChange={(value) => update("countertopLength", value)}
            suffix="متر"
            error={errors.countertopLength}
          />
          <NumericField
            label="قیمت هر متر صفحه کابینت"
            value={form.countertopRate}
            onChange={(value) => update("countertopRate", value)}
            suffix="تومان"
            grouped
            error={errors.countertopRate}
          />
        </div>
      </section>

      <AccessoryList
        accessories={form.accessories}
        onChange={(value) => update("accessories", value)}
        errors={errors}
      />

      <section className="panel extra-panel">
        <SectionHeader
          step="۶"
          title="هزینه‌های جانبی و تعدیلات"
          subtitle="نصب، حمل، تخفیف و مالیات را اضافه کنید"
          action={<Sparkles size={19} className="gold-icon" />}
        />
        <div className="extras-grid">
          <div className="extra-item">
            <div className="extra-label">
              <label>هزینه نصب</label>
              <ModeToggle
                label="نوع هزینه نصب"
                value={form.installationMode}
                onChange={(value) => {
                  setForm((old) => ({
                    ...old,
                    installationMode: value,
                    installationValue: 0,
                  }));
                }}
              />
            </div>
            <NumericField
              ariaLabel="هزینه نصب"
              value={form.installationValue}
              onChange={(value) => update("installationValue", value)}
              suffix={form.installationMode === "percent" ? "٪" : "تومان"}
              grouped={form.installationMode === "fixed"}
              error={errors.installationValue}
              hint={
                form.installationMode === "percent"
                  ? "درصدی از هزینه کابینت"
                  : "یک مبلغ ثابت برای نصب"
              }
            />
          </div>
          <div className="extra-item">
            <div className="extra-label">
              <label>هزینه حمل</label>
              <span className="muted-badge">مبلغ ثابت</span>
            </div>
            <NumericField
              ariaLabel="هزینه حمل"
              value={form.transportCost}
              onChange={(value) => update("transportCost", value)}
              suffix="تومان"
              grouped
              error={errors.transportCost}
              hint="هزینه حمل و انتقال به محل پروژه"
            />
          </div>
          <div className="extra-item">
            <div className="extra-label">
              <label>تخفیف</label>
              <ModeToggle
                label="نوع تخفیف"
                value={form.discountMode}
                onChange={(value) => {
                  setForm((old) => ({
                    ...old,
                    discountMode: value,
                    discountValue: 0,
                  }));
                }}
              />
            </div>
            <NumericField
              ariaLabel="تخفیف"
              value={form.discountValue}
              onChange={(value) => update("discountValue", value)}
              suffix={form.discountMode === "percent" ? "٪" : "تومان"}
              grouped={form.discountMode === "fixed"}
              error={errors.discountValue}
              hint="از جمع هزینه‌ها کسر می‌شود"
            />
          </div>
          <div className="extra-item">
            <div className="extra-label">
              <label>مالیات / ارزش افزوده</label>
              <span className="muted-badge">اختیاری</span>
            </div>
            <NumericField
              ariaLabel="درصد مالیات"
              value={form.taxPercent}
              onChange={(value) => update("taxPercent", value)}
              suffix="٪"
              error={errors.taxPercent}
              hint="پس از کسر تخفیف محاسبه می‌شود"
            />
          </div>
        </div>
        <InfoNote>
          همه مبالغ به تومان هستند. تخفیف از جمع هزینه‌ها کسر و سپس مالیات
          محاسبه می‌شود.
        </InfoNote>
      </section>

      <section className="panel module-panel">
        <SectionHeader
          step="۷"
          title="محاسبه کابینت دو پله"
          subtitle="هر پله به صورت جداگانه: طول × قیمت هر متر"
          action={<span className="muted-badge">ماژول ویژه</span>}
        />
        <div className="module-toolbar">
          <ModuleToggle
            enabled={form.doubleTierEnabled}
            onChange={(value) => update("doubleTierEnabled", value)}
          />
        </div>
        <div className="two-fields">
          <div className="module-tier">
            <h3 className="module-subtitle">پله اول</h3>
            <NumericField
              label="طول پله اول (متر)"
              value={form.doubleTier.tier1Length}
              onChange={(value) =>
                setForm((old) => ({
                  ...old,
                  doubleTier: { ...old.doubleTier, tier1Length: value },
                }))
              }
              suffix="متر"
              error={errors["doubleTier.tier1Length"]}
            />
            <NumericField
              label="قیمت هر متر پله اول"
              value={form.doubleTier.tier1Price}
              onChange={(value) =>
                setForm((old) => ({
                  ...old,
                  doubleTier: { ...old.doubleTier, tier1Price: value },
                }))
              }
              suffix="تومان"
              grouped
              error={errors["doubleTier.tier1Price"]}
            />
          </div>
          <div className="module-tier">
            <h3 className="module-subtitle">پله دوم</h3>
            <NumericField
              label="طول پله دوم (متر)"
              value={form.doubleTier.tier2Length}
              onChange={(value) =>
                setForm((old) => ({
                  ...old,
                  doubleTier: { ...old.doubleTier, tier2Length: value },
                }))
              }
              suffix="متر"
              error={errors["doubleTier.tier2Length"]}
            />
            <NumericField
              label="قیمت هر متر پله دوم"
              value={form.doubleTier.tier2Price}
              onChange={(value) =>
                setForm((old) => ({
                  ...old,
                  doubleTier: { ...old.doubleTier, tier2Price: value },
                }))
              }
              suffix="تومان"
              grouped
              error={errors["doubleTier.tier2Price"]}
            />
          </div>
        </div>
        <InfoNote>
          <strong>فرمول: </strong>
          {MODULE_FORMULAS.doubleTier}
        </InfoNote>
        {errors.doubleTier && <p className="field-error">{errors.doubleTier}</p>}
        {form.doubleTierEnabled && (
          <div className="module-result">
            <span>جمع کابینت دو پله</span>
            <strong>
              {formatMoney(result.doubleTierCost)} <small>تومان</small>
            </strong>
          </div>
        )}
      </section>

      <section className="panel module-panel">
        <SectionHeader
          step="۸"
          title="محاسبه کافی بار (کافه‌بار)"
          subtitle="قیمت پایه + طول کابینت + صفحه کانتر"
          action={<span className="muted-badge">ماژول ویژه</span>}
        />
        <div className="module-toolbar">
          <ModuleToggle
            enabled={form.coffeeBarEnabled}
            onChange={(value) => update("coffeeBarEnabled", value)}
          />
        </div>
        <div className="two-fields">
          <NumericField
            label="قیمت پایه (تومان)"
            value={form.coffeeBar.basePrice}
            onChange={(value) =>
              setForm((old) => ({
                ...old,
                coffeeBar: { ...old.coffeeBar, basePrice: value },
              }))
            }
            suffix="تومان"
            grouped
            error={errors["coffeeBar.basePrice"]}
          />
          <NumericField
            label="طول کابینت (متر)"
            value={form.coffeeBar.length}
            onChange={(value) =>
              setForm((old) => ({
                ...old,
                coffeeBar: { ...old.coffeeBar, length: value },
              }))
            }
            suffix="متر"
            error={errors["coffeeBar.length"]}
          />
        </div>
        <div className="two-fields">
          <NumericField
            label="قیمت هر متر کابینت"
            value={form.coffeeBar.pricePerMeter}
            onChange={(value) =>
              setForm((old) => ({
                ...old,
                coffeeBar: { ...old.coffeeBar, pricePerMeter: value },
              }))
            }
            suffix="تومان"
            grouped
            error={errors["coffeeBar.pricePerMeter"]}
          />
          <NumericField
            label="طول صفحه کانتر (متر)"
            value={form.coffeeBar.countertopLength}
            onChange={(value) =>
              setForm((old) => ({
                ...old,
                coffeeBar: { ...old.coffeeBar, countertopLength: value },
              }))
            }
            suffix="متر"
            error={errors["coffeeBar.countertopLength"]}
          />
        </div>
        <div className="two-fields">
          <NumericField
            label="قیمت هر متر صفحه کانتر"
            value={form.coffeeBar.countertopRate}
            onChange={(value) =>
              setForm((old) => ({
                ...old,
                coffeeBar: { ...old.coffeeBar, countertopRate: value },
              }))
            }
            suffix="تومان"
            grouped
            error={errors["coffeeBar.countertopRate"]}
          />
        </div>
        <InfoNote>
          <strong>فرمول: </strong>
          {MODULE_FORMULAS.coffeeBar}
        </InfoNote>
        {errors.coffeeBar && <p className="field-error">{errors.coffeeBar}</p>}
        {form.coffeeBarEnabled && (
          <div className="module-result">
            <span>جمع کافی بار</span>
            <strong>
              {formatMoney(result.coffeeBarCost)} <small>تومان</small>
            </strong>
          </div>
        )}
      </section>

      <section className="panel module-panel">
        <SectionHeader
          step="۹"
          title="محاسبه قیمت کمد (مدروار)"
          subtitle="انتخاب مدل از منوی کمد؛ فرمول مخصوص هر مدل"
          action={<span className="muted-badge">ماژول ویژه</span>}
        />
        <div className="module-toolbar">
          <ModuleToggle
            enabled={form.wardrobeEnabled}
            onChange={(value) => update("wardrobeEnabled", value)}
          />
        </div>
        <div className="field">
          <label htmlFor="wardrobe-model">مدل کمد</label>
          <div className="select-wrap">
            <select
              id="wardrobe-model"
              className="model-select"
              value={form.wardrobe.modelId}
              onChange={(event) =>
                setForm((old) => ({
                  ...old,
                  wardrobe: { ...old.wardrobe, modelId: event.target.value },
                }))
              }
            >
              {WARDROBE_MODELS.map((model) => (
                <option key={model.id} value={model.id}>
                  {model.name}
                </option>
              ))}
            </select>
          </div>
        </div>
        {(() => {
          const model =
            WARDROBE_MODELS.find((m) => m.id === form.wardrobe.modelId) ??
            WARDROBE_MODELS[0];
          return (
            <div className="formula-box">
              <strong>فرمول این مدل</strong>
              <p>{model.formula}</p>
              <p className="model-note">{model.note}</p>
            </div>
          );
        })()}
        <div className="two-fields">
          <NumericField
            label="عرض / طول کمد (متر)"
            value={form.wardrobe.width}
            onChange={(value) =>
              setForm((old) => ({
                ...old,
                wardrobe: { ...old.wardrobe, width: value },
              }))
            }
            suffix="متر"
            error={errors["wardrobe.width"]}
          />
          <NumericField
            label="قیمت هر متر کمد"
            value={form.wardrobe.unitPrice}
            onChange={(value) =>
              setForm((old) => ({
                ...old,
                wardrobe: { ...old.wardrobe, unitPrice: value },
              }))
            }
            suffix="تومان"
            grouped
            error={errors["wardrobe.unitPrice"]}
          />
        </div>
        <div className="two-fields">
          <NumericField
            label="تعداد کشوی"
            value={form.wardrobe.drawers}
            onChange={(value) =>
              setForm((old) => ({
                ...old,
                wardrobe: { ...old.wardrobe, drawers: value },
              }))
            }
            suffix="عدد"
            error={errors.wardrobeDrawers}
          />
          <NumericField
            label="قیمت هر کشو"
            value={form.wardrobe.drawerPrice}
            onChange={(value) =>
              setForm((old) => ({
                ...old,
                wardrobe: { ...old.wardrobe, drawerPrice: value },
              }))
            }
            suffix="تومان"
            grouped
            error={errors["wardrobe.drawerPrice"]}
          />
        </div>
        <div className="two-fields">
          <NumericField
            label="تعداد قفسه"
            value={form.wardrobe.shelves}
            onChange={(value) =>
              setForm((old) => ({
                ...old,
                wardrobe: { ...old.wardrobe, shelves: value },
              }))
            }
            suffix="عدد"
            error={errors.wardrobeShelves}
          />
          <NumericField
            label="قیمت هر قفسه"
            value={form.wardrobe.shelfPrice}
            onChange={(value) =>
              setForm((old) => ({
                ...old,
                wardrobe: { ...old.wardrobe, shelfPrice: value },
              }))
            }
            suffix="تومان"
            grouped
            error={errors["wardrobe.shelfPrice"]}
          />
        </div>
        <InfoNote>
          <strong>فرمول: </strong>
          {MODULE_FORMULAS.wardrobe}
        </InfoNote>
        {errors.wardrobe && <p className="field-error">{errors.wardrobe}</p>}
        {form.wardrobeEnabled && (
          <div className="module-result">
            <span>جمع کمد ({result.wardrobeModelName})</span>
            <strong>
              {formatMoney(result.wardrobeCost)} <small>تومان</small>
            </strong>
          </div>
        )}
      </section>
    </div>
  );
}

function ModuleToggle({
  enabled,
  onChange,
}: {
  enabled: boolean;
  onChange: (value: boolean) => void;
}) {
  return (
    <button
      type="button"
      className={`module-toggle ${enabled ? "selected" : ""}`}
      onClick={() => onChange(!enabled)}
      aria-pressed={enabled}
    >
      {enabled ? "✅ در فاکتور" : "افزودن به فاکتور"}
    </button>
  );
}
