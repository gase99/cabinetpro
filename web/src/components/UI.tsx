import { useEffect, useId, useRef, useState, type ReactNode } from "react";
import { CalendarDays, ChevronDown, Info, X } from "lucide-react";
import type { CostMode, NumericValue } from "../types";
import {
  formatDate,
  formatInput,
  toEnglishDigits,
  toPersianDigits,
} from "../utils/format";

interface NumericFieldProps {
  label?: string;
  value: NumericValue;
  onChange: (value: NumericValue) => void;
  suffix?: string;
  error?: string;
  hint?: string;
  grouped?: boolean;
  placeholder?: string;
  id?: string;
  ariaLabel?: string;
  className?: string;
}

export function NumericField({
  label,
  value,
  onChange,
  suffix,
  error,
  hint,
  grouped = false,
  placeholder = "۰",
  id,
  ariaLabel,
  className = "",
}: NumericFieldProps) {
  const generatedId = useId();
  const fieldId = id || generatedId;
  const [draft, setDraft] = useState(formatInput(value, grouped));
  const [localError, setLocalError] = useState("");
  useEffect(() => {
    const normalized = toEnglishDigits(draft);
    if (
      (value === "" && normalized !== "") ||
      (value !== "" && Number(normalized) !== value)
    ) {
      setDraft(formatInput(value, grouped));
    }
  }, [value, grouped]); // مقدار بیرونی بدون حذف اعشار در حال تایپ همگام می‌شود.

  const handleChange = (raw: string) => {
    const normalized = toEnglishDigits(raw).replace(/٫/g, ".");
    if (normalized.startsWith("-")) {
      setLocalError("مقادیر منفی مجاز نیستند.");
      return;
    }
    if (!/^\d*\.?\d*$/.test(normalized)) {
      setLocalError("لطفاً فقط عدد وارد کنید.");
      return;
    }
    if (normalized.length > 15) return;
    setLocalError("");
    if (grouped && normalized && !normalized.endsWith(".")) {
      const [integer, decimal] = normalized.split(".");
      const groupedInteger = integer
        ? Number(integer).toLocaleString("fa-IR")
        : "";
      setDraft(
        decimal !== undefined
          ? `${groupedInteger}٫${toPersianDigits(decimal)}`
          : groupedInteger,
      );
    } else setDraft(toPersianDigits(normalized).replace(/\./g, "٫"));
    onChange(normalized === "" || normalized === "." ? "" : Number(normalized));
  };

  return (
    <div
      className={`field ${className} ${error || localError ? "field-invalid" : ""}`}
    >
      {label && <label htmlFor={fieldId}>{label}</label>}
      <div className="input-wrap">
        <input
          id={fieldId}
          type="text"
          inputMode="decimal"
          dir="ltr"
          autoComplete="off"
          value={draft}
          placeholder={toPersianDigits(placeholder)}
          aria-label={ariaLabel || label}
          aria-invalid={Boolean(error || localError)}
          aria-describedby={
            error || localError ? `${fieldId}-error` : undefined
          }
          onChange={(event) => handleChange(event.target.value)}
          onKeyDown={(event) => {
            if (["-", "e", "E", "+"].includes(event.key)) {
              event.preventDefault();
              if (event.key === "-") setLocalError("مقادیر منفی مجاز نیستند.");
            }
          }}
        />
        {suffix && <span className="field-suffix">{suffix}</span>}
      </div>
      {(error || localError) && (
        <p id={`${fieldId}-error`} className="field-error">
          {error || localError}
        </p>
      )}
      {hint && !error && !localError && <p className="field-hint">{hint}</p>}
    </div>
  );
}

export function DateField({
  value,
  onChange,
}: {
  value: string;
  onChange: (value: string) => void;
}) {
  const ref = useRef<HTMLInputElement>(null);
  return (
    <div className="field date-field">
      <label htmlFor="project-date">تاریخ محاسبه</label>
      <div
        className="date-wrap"
        onClick={() => {
          try {
            ref.current?.showPicker();
          } catch {
            ref.current?.focus();
          }
        }}
      >
        <CalendarDays size={17} className="date-icon" />
        <span>{formatDate(value)}</span>
        <ChevronDown size={15} />
        <input
          ref={ref}
          id="project-date"
          type="date"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          aria-label="تاریخ محاسبه"
        />
      </div>
    </div>
  );
}

export function SectionHeader({
  step,
  title,
  subtitle,
  action,
  icon,
}: {
  step?: string;
  title: string;
  subtitle?: string;
  action?: ReactNode;
  icon?: ReactNode;
}) {
  return (
    <div className="section-heading">
      <div className="section-heading-main">
        {step ? (
          <span className="step-number">{step}</span>
        ) : (
          icon && <span className="section-icon">{icon}</span>
        )}
        <div>
          <h2>{title}</h2>
          {subtitle && <p>{subtitle}</p>}
        </div>
      </div>
      {action && <div className="section-heading-action">{action}</div>}
    </div>
  );
}

export function ModeToggle({
  value,
  onChange,
  label,
}: {
  value: CostMode;
  onChange: (value: CostMode) => void;
  label: string;
}) {
  return (
    <div className="mode-toggle" role="group" aria-label={label}>
      <button
        type="button"
        className={value === "fixed" ? "selected" : ""}
        onClick={() => onChange("fixed")}
        aria-pressed={value === "fixed"}
      >
        مبلغ ثابت
      </button>
      <button
        type="button"
        className={value === "percent" ? "selected" : ""}
        onClick={() => onChange("percent")}
        aria-pressed={value === "percent"}
      >
        درصدی
      </button>
    </div>
  );
}

export function InfoNote({ children }: { children: ReactNode }) {
  return (
    <div className="info-note">
      <Info size={16} />
      <span>{children}</span>
    </div>
  );
}

export function Modal({
  title,
  children,
  onClose,
  wide = false,
}: {
  title: string;
  children: ReactNode;
  onClose: () => void;
  wide?: boolean;
}) {
  const ref = useRef<HTMLDivElement>(null);
  const titleId = useId();
  useEffect(() => {
    const previous = document.activeElement as HTMLElement | null;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    ref.current?.focus();
    const keydown = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
      if (event.key === "Tab") {
        const items = ref.current?.querySelectorAll<HTMLElement>(
          'button, input, select, textarea, [tabindex="0"]',
        );
        if (!items?.length) return;
        const first = items[0];
        const last = items[items.length - 1];
        if (
          event.shiftKey &&
          (document.activeElement === first ||
            document.activeElement === ref.current)
        ) {
          event.preventDefault();
          last.focus();
        }
        if (!event.shiftKey && document.activeElement === last) {
          event.preventDefault();
          first.focus();
        }
      }
    };
    document.addEventListener("keydown", keydown);
    return () => {
      document.body.style.overflow = previousOverflow;
      document.removeEventListener("keydown", keydown);
      previous?.focus();
    };
  }, [onClose]);
  return (
    <div
      className="modal-backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <div
        className={`modal ${wide ? "modal-wide" : ""}`}
        ref={ref}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <div className="modal-heading">
          <h2 id={titleId}>{title}</h2>
          <button className="icon-button" onClick={onClose} aria-label="بستن">
            <X size={21} />
          </button>
        </div>
        <div className="modal-body">{children}</div>
      </div>
    </div>
  );
}
