import {
  Calculator,
  Check,
  Copy,
  Info,
  Lightbulb,
  Printer,
  ReceiptText,
  Ruler,
  ShieldCheck,
  Sparkles,
  ArrowLeft,
} from "lucide-react";
import type { CabinetForm, CalculationResult, FieldErrors } from "../types";
import { SECTIONS } from "../data";
import { formatDate, formatMoney, formatNumber } from "../utils/format";
import { numeric } from "../utils/calculatePrice";

interface Props {
  form: CabinetForm;
  result: CalculationResult;
  errors: FieldErrors;
  onCalculate: () => void;
  onCopy: () => void;
  onPrint: () => void;
  onHelp: () => void;
  calculationTick: number;
}

export function ResultSummary({
  form,
  result,
  errors,
  onCalculate,
  onCopy,
  onPrint,
  onHelp,
  calculationTick,
}: Props) {
  const maxMeter = Math.max(
    ...SECTIONS.map((section) => result.effectiveMeters[section.key]),
    1,
  );
  const moduleRows = [
    ...(form.doubleTierEnabled && result.doubleTierCost > 0
      ? [
          {
            label: "کابینت دو پله",
            value: result.doubleTierCost,
            primary: false,
            discount: false,
          },
        ]
      : []),
    ...(form.coffeeBarEnabled && result.coffeeBarCost > 0
      ? [
          {
            label: "کافی بار (کافه‌بار)",
            value: result.coffeeBarCost,
            primary: false,
            discount: false,
          },
        ]
      : []),
    ...(form.wardrobeEnabled && result.wardrobeCost > 0
      ? [
          {
            label: `کمد — ${result.wardrobeModelName}`,
            value: result.wardrobeCost,
            primary: false,
            discount: false,
          },
        ]
      : []),
  ];
  const rows = [
    { label: "هزینه کابینت", value: result.cabinetCost, primary: true },
    { label: "صفحه کابینت (کانتر)", value: result.countertopCost },
    { label: "یراق‌آلات و تجهیزات", value: result.accessoriesCost },
    ...moduleRows,
    { label: "هزینه نصب", value: result.installationCost },
    { label: "هزینه حمل", value: result.transportCost },
    { label: "تخفیف", value: result.discountAmount, discount: true },
    { label: "مالیات / ارزش افزوده", value: result.taxAmount },
  ];
  return (
    <aside className="summary-column" id="invoice-summary">
      <div className="print-only print-header">
        <h1>محاسبه‌گر قیمت کابینت</h1>
        <p>پیش‌فاکتور برآورد هزینه کابینت آشپزخانه</p>
        <p>
          پروژه: {form.name || "پروژه بدون نام"} — تاریخ:{" "}
          {formatDate(form.date)}
        </p>
      </div>
      <section
        className={`summary-card ${calculationTick ? "calculated" : ""}`}
        key={`summary-${calculationTick}`}
      >
        <div className="summary-heading">
          <div className="summary-heading-title">
            <span>
              <ReceiptText size={22} />
            </span>
            <div>
              <h2>خلاصه فاکتور</h2>
              <p>برآورد شفاف، تا آخرین تومان</p>
            </div>
          </div>
          <span className="live-badge">
            <i /> زنده
          </span>
        </div>
        <div className="summary-body">
          <div className="summary-label">
            <span>متراژ مؤثر کابینت‌ها</span>
            <button
              className="icon-button"
              aria-label="راهنمای متراژ مؤثر"
              onClick={onHelp}
            >
              <Info size={15} />
            </button>
          </div>
          <div className="meter-breakdown">
            {SECTIONS.map((section) => (
              <div className="meter-row" key={section.key}>
                <span className="meter-name">
                  <i style={{ background: section.color }} />
                  {section.label}
                </span>
                <div className="meter-track">
                  <span
                    style={{
                      width: `${(result.effectiveMeters[section.key] / maxMeter) * 100}%`,
                      background: section.color,
                    }}
                  />
                </div>
                <span className="meter-value">
                  {formatNumber(result.effectiveMeters[section.key])}{" "}
                  <small>متر</small>
                </span>
              </div>
            ))}
          </div>
          <div className="effective-total">
            <span>
              <Ruler size={17} />
              متراژ مؤثر کل
            </span>
            <strong>
              {formatNumber(result.effectiveMeters.total)} <small>متر</small>
            </strong>
          </div>
          <div className="cost-breakdown">
            {rows.map((row) => (
              <div
                className={`cost-row ${row.primary ? "primary-cost" : ""} ${row.discount ? "discount-row" : ""}`}
                key={row.label}
              >
                <span>{row.label}</span>
                <span className="cost-amount">
                  <b>
                    {row.discount && row.value > 0 ? "− " : ""}
                    {formatMoney(row.value)}
                  </b>
                  <small>تومان</small>
                </span>
              </div>
            ))}
          </div>
          {result.moduleCost > 0 && (
            <div className="summary-note">
              <Sparkles size={14} />
              <span>
                محاسبات ویژه (دو پله، کافی بار، کمد) در جمع هزینه‌ها گنجانده
                شده‌اند.
              </span>
            </div>
          )}
          {errors.deductions && (
            <div className="summary-warning">
              <Info size={16} />
              <span>{errors.deductions}</span>
            </div>
          )}
          {errors.lengths && (
            <div className="summary-warning">
              <Info size={16} />
              <span>{errors.lengths}</span>
            </div>
          )}
          {numeric(form.basePrice) === 0 && (
            <div className="summary-warning">
              <Info size={16} />
              <span>قیمت پایه صفر است؛ هزینه کابینت صفر محاسبه می‌شود.</span>
            </div>
          )}
          <div className="invoice-total">
            <div>
              <span>مبلغ نهایی قابل پرداخت</span>
              <Sparkles size={17} />
            </div>
            <p>
              <strong>{formatMoney(result.total)}</strong>
              <span>تومان</span>
            </p>
            <span className="total-note">
              {result.taxAmount > 0
                ? "شامل مالیات و پس از کسر تخفیف"
                : "با احتساب همه هزینه‌ها و تخفیف‌ها"}
            </span>
          </div>
          <div className="summary-actions no-print">
            <button
              className="primary-button calculate-button"
              onClick={onCalculate}
            >
              <Calculator size={18} />
              محاسبه قیمت نهایی
              <Check size={17} />
            </button>
            <div className="summary-secondary">
              <button className="secondary-button" onClick={onPrint}>
                <Printer size={17} /> چاپ / PDF
              </button>
              <button className="secondary-button" onClick={onCopy}>
                <Copy size={16} /> کپی فاکتور
              </button>
            </div>
          </div>
          <div className="live-note no-print">
            <span />
            محاسبات با تغییر ورودی‌ها، زنده به‌روز می‌شوند.
          </div>
        </div>
      </section>
      <div className="tip-card no-print">
        <span className="tip-icon">
          <Lightbulb size={21} />
        </span>
        <div>
          <h3>یک نکته برای برآورد دقیق‌تر</h3>
          <p>
            طول یخچال، گاز و ماشین لباسشویی را از بخش «کسر طول‌ها» کم کنید تا
            متراژ واقعی کابینت محاسبه شود.
          </p>
          <button onClick={onHelp}>
            راهنمای محاسبه <ArrowLeft size={14} />
          </button>
        </div>
      </div>
      <div className="privacy-note no-print">
        <ShieldCheck size={15} />
        <span>اطلاعات پروژه فقط در مرورگر شما ذخیره می‌شود.</span>
      </div>
    </aside>
  );
}
