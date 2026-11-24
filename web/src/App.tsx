import { useCallback, useEffect, useMemo, useState } from "react";
import {
  ArrowLeft,
  BookOpen,
  Calculator,
  Check,
  CheckCircle2,
  ChevronLeft,
  FileText,
  FolderOpen,
  HelpCircle,
  LockKeyhole,
  Plus,
  RotateCcw,
  Save,
  Search,
  ShieldCheck,
  Sparkles,
  Trash2,
  X,
  Zap,
} from "lucide-react";
import { CalculatorForm } from "./components/CalculatorForm";
import { ResultSummary } from "./components/ResultSummary";
import { Modal } from "./components/UI";
import { createDefaultForm, createEmptyForm } from "./data";
import type { Draft } from "./types";
import { calculateCabinetPrice, validateForm } from "./utils/calculatePrice";
import { formatMoney, formatSavedTime } from "./utils/format";
import { buildInvoiceText, copyText } from "./utils/invoice";
import {
  readCurrent,
  readDrafts,
  saveCurrent,
  saveDrafts,
} from "./utils/storage";

export default function App() {
  const [form, setForm] = useState(readCurrent);
  const [drafts, setDrafts] = useState(readDrafts);
  const [draftId, setDraftId] = useState<string | null>(null);
  const [modal, setModal] = useState<"help" | "drafts" | "reset" | null>(null);
  const [toast, setToast] = useState<{
    message: string;
    type: "success" | "error";
  } | null>(null);
  const [autoSaved, setAutoSaved] = useState(true);
  const [calculationTick, setCalculationTick] = useState(0);
  const [draftSearch, setDraftSearch] = useState("");
  const result = useMemo(() => calculateCabinetPrice(form), [form]);
  const errors = useMemo(() => validateForm(form), [form]);
  const closeModal = useCallback(() => setModal(null), []);

  useEffect(() => {
    setAutoSaved(false);
    const timer = window.setTimeout(() => setAutoSaved(saveCurrent(form)), 650);
    return () => window.clearTimeout(timer);
  }, [form]);
  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 4200);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const notify = (message: string, type: "success" | "error" = "success") =>
    setToast({ message, type });
  const saveDraft = () => {
    const id = draftId || crypto.randomUUID();
    const draft: Draft = {
      id,
      name: form.name.trim() || "پروژه بدون نام",
      savedAt: new Date().toISOString(),
      form: structuredClone(form),
    };
    const next = [draft, ...drafts.filter((item) => item.id !== id)].slice(
      0,
      30,
    );
    if (saveDrafts(next)) {
      setDrafts(next);
      setDraftId(id);
      notify("پیش‌نویس پروژه با موفقیت ذخیره شد.");
    } else notify("ذخیره انجام نشد؛ فضای ذخیره مرورگر را بررسی کنید.", "error");
  };
  const calculate = () => {
    if (Object.keys(errors).length) {
      notify(Object.values(errors)[0], "error");
      const field = Object.keys(errors)[0];
      document
        .getElementById(field === "lengths" ? "lengths.ground" : field)
        ?.scrollIntoView({ behavior: "smooth", block: "center" });
      return;
    }
    setCalculationTick((value) => value + 1);
    notify("محاسبه انجام شد؛ فاکتور شما آماده است.");
  };
  const copy = async () => {
    try {
      await copyText(buildInvoiceText(form, result));
      notify("خلاصه فاکتور در حافظه کپی شد.");
    } catch {
      notify(
        "دسترسی به حافظه ممکن نشد؛ از مرورگر دیگری استفاده کنید.",
        "error",
      );
    }
  };
  const print = () => {
    if (Object.keys(errors).length) {
      notify("ابتدا خطاهای فرم را اصلاح کنید.", "error");
      return;
    }
    window.print();
  };
  const reset = () => {
    setForm(createEmptyForm());
    setDraftId(null);
    setModal(null);
    notify("فرم ریست شد. آماده شروع پروژه جدید هستید.");
  };
  const loadDraft = (draft: Draft) => {
    setForm(draft.form);
    setDraftId(draft.id);
    setModal(null);
    notify("پیش‌نویس پروژه بارگذاری شد.");
  };
  const deleteDraft = (id: string) => {
    const next = drafts.filter((draft) => draft.id !== id);
    if (saveDrafts(next)) {
      setDrafts(next);
      if (draftId === id) setDraftId(null);
      notify("پیش‌نویس حذف شد.");
    } else notify("حذف پیش‌نویس انجام نشد.", "error");
  };
  const filteredDrafts = drafts.filter((draft) =>
    draft.name.toLowerCase().includes(draftSearch.toLowerCase()),
  );

  return (
    <div className="app-shell" dir="rtl">
      <header className="app-header no-print">
        <div className="header-inner">
          <a
            className="brand"
            href="#"
            aria-label="محاسبه‌گر قیمت کابینت"
            onClick={(event) => {
              event.preventDefault();
              window.scrollTo({ top: 0, behavior: "smooth" });
            }}
          >
            <span className="brand-mark">
              <svg
                width="29"
                height="29"
                viewBox="0 0 32 32"
                fill="none"
                aria-hidden="true"
              >
                <path
                  d="M5 15H27V27H5zM3 12H29V16H3zM13 16V27M21 16V27M8 27V29M24 27V29M8 4H24V9H8zM16 4V9"
                  stroke="currentColor"
                  strokeWidth="1.6"
                  strokeLinejoin="round"
                />
                <path
                  d="M10 20V23M18 20V23M25 20V23"
                  stroke="currentColor"
                  strokeWidth="1.6"
                  strokeLinecap="round"
                />
              </svg>
            </span>
            <span>
              <strong>محاسبه‌گر قیمت کابینت</strong>
              <small>برآورد دقیق، تصمیم مطمئن</small>
            </span>
          </a>
          <nav className="header-nav" aria-label="منوی اصلی">
            <button
              className="nav-item active"
              onClick={() => {
                setModal(null);
                window.scrollTo({ top: 0, behavior: "smooth" });
              }}
            >
              <Calculator size={17} />
              محاسبه‌گر
            </button>
            <button className="nav-item" onClick={() => setModal("drafts")}>
              <FolderOpen size={17} />
              پیش‌نویس‌های من
              {drafts.length > 0 && (
                <span className="nav-count">{drafts.length}</span>
              )}
            </button>
            <button className="nav-item" onClick={() => setModal("help")}>
              <BookOpen size={17} />
              راهنمای محاسبه
            </button>
          </nav>
          <div className="header-actions">
            <span className="autosave-status">
              <i className={autoSaved ? "" : "saving"} />
              {autoSaved ? "ذخیره خودکار فعال" : "در حال ذخیره…"}
            </span>
            <button
              className="new-project-button"
              onClick={() => setModal("reset")}
            >
              <Plus size={17} />
              <span>پروژه جدید</span>
            </button>
            <button
              className="icon-button mobile-help"
              aria-label="پیش‌نویس‌های من"
              onClick={() => setModal("drafts")}
            >
              <FolderOpen size={20} />
            </button>
            <button
              className="icon-button mobile-help"
              aria-label="راهنما"
              onClick={() => setModal("help")}
            >
              <HelpCircle size={22} />
            </button>
          </div>
        </div>
      </header>

      <main className="main-container">
        <section className="hero no-print">
          <img
            src="https://images.pexels.com/photos/35021550/pexels-photo-35021550.jpeg?auto=compress&cs=tinysrgb&fit=crop&h=627&w=1200"
            alt="آشپزخانه مدرن با کابینت‌های چوبی و صفحه سنگی"
          />
          <div className="hero-overlay" />
          <div className="hero-content">
            <div className="hero-eyebrow">
              <span />
              از ایده تا برآورد دقیق
            </div>
            <h1>آشپزخانه رویایی‌تان، با یک برآورد دقیق</h1>
            <p>
              از متراژ کابینت تا آخرین جزئیات؛ هزینه پروژه را شفاف محاسبه کنید.
            </p>
            <div className="hero-features">
              <span>
                <Zap size={14} />
                محاسبه لحظه‌ای
              </span>
              <i />
              <span>
                <ShieldCheck size={14} />
                ذخیره امن در مرورگر
              </span>
              <i />
              <span>
                <FileText size={14} />
                آماده چاپ و PDF
              </span>
              <i />
              <span>
                <Sparkles size={14} />
                دو پله، کافی بار و کمد
              </span>
            </div>
          </div>
          <div className="hero-decoration">
            <span>دقیق</span>
            <strong>
              تا آخرین
              <br />
              جزئیات.
            </strong>
          </div>
        </section>

        <div className="workspace-toolbar no-print">
          <div>
            <div className="breadcrumb">
              <span>محاسبه‌گر</span>
              <ChevronLeft size={13} />
              <strong>پروژه جدید</strong>
            </div>
            <h2>
              جزئیات محاسبه <span>همه‌چیز برای یک برآورد دقیق</span>
            </h2>
          </div>
          <div className="toolbar-actions">
            <button className="reset-button" onClick={() => setModal("reset")}>
              <RotateCcw size={16} />
              ریست فرم
            </button>
            <button className="save-button" onClick={saveDraft}>
              <Save size={17} />
              ذخیره پیش‌نویس
            </button>
          </div>
        </div>

        <div className="workspace">
          <div className="no-print">
            <CalculatorForm
              form={form}
              setForm={setForm}
              result={result}
              errors={errors}
            />
          </div>
          <ResultSummary
            form={form}
            result={result}
            errors={errors}
            onCalculate={calculate}
            onCopy={copy}
            onPrint={print}
            onHelp={() => setModal("help")}
            calculationTick={calculationTick}
          />
        </div>
        <div className="bottom-note no-print">
          <Sparkles size={17} />
          <span>یک برآورد خوب، اولین قدم برای ساخت یک آشپزخانه خوب است.</span>
        </div>
        <footer className="footer no-print">
          <div>
            <span className="footer-brand">محاسبه‌گر قیمت کابینت</span>
            <span>نسخه ۱٫۱</span>
          </div>
          <p>
            <LockKeyhole size={13} />
            بدون نیاز به ثبت‌نام؛ محاسبات فقط روی دستگاه شما
          </p>
          <button onClick={() => setModal("help")}>
            راهنما و فرمول محاسبه <ArrowLeft size={13} />
          </button>
        </footer>
      </main>

      <div className="mobile-summary-bar no-print">
        <div>
          <small>مبلغ نهایی</small>
          <strong>
            {formatMoney(result.total)} <span>تومان</span>
          </strong>
        </div>
        <button
          onClick={() =>
            document
              .getElementById("invoice-summary")
              ?.scrollIntoView({ behavior: "smooth", block: "start" })
          }
        >
          مشاهده فاکتور <ArrowLeft size={17} />
        </button>
      </div>

      {toast && (
        <div className={`toast ${toast.type}`} role="status">
          <CheckCircle2 size={20} />
          <span>{toast.message}</span>
          <button aria-label="بستن پیام" onClick={() => setToast(null)}>
            <X size={16} />
          </button>
        </div>
      )}

      {modal === "help" && (
        <Modal title="راهنمای محاسبه قیمت کابینت" onClose={closeModal} wide>
          <div className="help-intro">
            <span className="help-icon">
              <Calculator size={27} />
            </span>
            <p>
              با سه قدم ساده، هزینه واقعی پروژه را برآورد کنید. تمام محاسبات
              زنده و روی دستگاه شما انجام می‌شود.
            </p>
          </div>
          <div className="help-steps">
            <div>
              <span>۱</span>
              <h3>قیمت پایه و ضرایب</h3>
              <p>
                قیمت هر متر کابینت زمینی را وارد کنید. ضرایب پیش‌فرض: زمینی ۱،
                دیواری ۰٫۶، ایستاده ۲ و جزیره ۱ هستند.
              </p>
            </div>
            <div>
              <span>۲</span>
              <h3>طول‌ها و کسرها</h3>
              <p>
                طول هر بخش را به متر وارد کنید. کسرهای یخچال، گاز و سایر موارد
                فقط از طول زمینی کم می‌شوند.
              </p>
            </div>
            <div>
              <span>۳</span>
              <h3>هزینه‌ها و فاکتور</h3>
              <p>
                صفحه، یراق‌آلات، نصب و حمل را اضافه کنید. تخفیف از جمع هزینه‌ها
                کسر و سپس مالیات محاسبه می‌شود.
              </p>
            </div>
            <div>
              <span>۴</span>
              <h3>محاسبات ویژه</h3>
              <p>
                کابینت دو پله و کافی بار را با فرمول‌های مخصوص محاسبه کنید. در
                ماژول کمد، مدل را از منوی مدل‌های موجود در بازار ایران انتخاب
                کنید؛ هر مدل ضریب و فرمول مخصوص به خود را دارد و در صورت
                فعال‌شدن، در فاکتور نهایی گنجانده می‌شود.
              </p>
            </div>
          </div>
          <div className="formula-box">
            <strong>فرمول محاسبات ویژه</strong>
            <p>کابینت دو پله: طول پله ۱ × قیمت پله ۱ + طول پله ۲ × قیمت پله ۲</p>
            <p>کافی بار: قیمت پایه + طول × نرخ + طول صفحه × نرخ صفحه</p>
            <p>کمد: عرض × قیمت هر متر × ضریب مدل + اجناس (کشوی، قفسه)</p>
          </div>
          <div className="formula-box">
            <strong>فرمول اصلی محاسبه</strong>
            <p>متراژ مؤثر = طول بخش × ضریب بخش</p>
            <p>هزینه کابینت = مجموع متراژ مؤثر × قیمت پایه</p>
          </div>
          <div className="sample-box">
            <strong>نمونه تست پذیرش</strong>
            <p>
              قیمت پایه ۶٬۵۰۰٬۰۰۰ تومان؛ زمینی ۳ متر، دیواری ۳ متر، ایستاده ۱
              متر.
            </p>
            <p>
              متراژ مؤثر: ۳ + ۱٫۸ + ۲ = <b>۶٫۸ متر</b>
            </p>
            <p>
              هزینه کابینت: <b>۴۴٬۲۰۰٬۰۰۰ تومان</b>
            </p>
          </div>
          <div className="modal-actions">
            <button
              className="primary-button"
              onClick={() => {
                setForm(createDefaultForm());
                setDraftId(null);
                setModal(null);
                notify("داده‌های نمونه بارگذاری شد.");
              }}
            >
              <Sparkles size={17} />
              بارگذاری نمونه محاسبه
            </button>
            <button className="secondary-button" onClick={closeModal}>
              متوجه شدم <Check size={16} />
            </button>
          </div>
        </Modal>
      )}

      {modal === "drafts" && (
        <Modal title="پیش‌نویس‌های من" onClose={closeModal}>
          <p className="modal-description">
            پروژه‌های ذخیره‌شده در این مرورگر. می‌توانید هر زمان آن‌ها را باز
            کنید.
          </p>
          <div className="draft-search">
            <Search size={18} />
            <input
              placeholder="جستجو در پروژه‌ها…"
              value={draftSearch}
              onChange={(event) => setDraftSearch(event.target.value)}
              aria-label="جستجو در پیش‌نویس‌ها"
            />
          </div>
          {filteredDrafts.length ? (
            <div className="draft-list">
              {filteredDrafts.map((draft) => (
                <div className="draft-item" key={draft.id}>
                  <span className="draft-icon">
                    <FolderOpen size={23} />
                  </span>
                  <div>
                    <h3>{draft.name}</h3>
                    <p>
                      {formatSavedTime(draft.savedAt)} •{" "}
                      {formatMoney(calculateCabinetPrice(draft.form).total)}{" "}
                      تومان
                    </p>
                  </div>
                  <button
                    className="small-button"
                    onClick={() => loadDraft(draft)}
                  >
                    باز کردن <ArrowLeft size={14} />
                  </button>
                  <button
                    className="icon-button delete-accessory"
                    onClick={() => deleteDraft(draft.id)}
                    aria-label={`حذف ${draft.name}`}
                  >
                    <Trash2 size={17} />
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <div className="draft-empty">
              <FolderOpen size={42} />
              <h3>
                {draftSearch
                  ? "پروژه‌ای یافت نشد"
                  : "هنوز پیش‌نویسی ذخیره نشده است"}
              </h3>
              <p>
                {draftSearch
                  ? "عبارت دیگری را جستجو کنید."
                  : "پروژه فعلی را ذخیره کنید تا بعدها به آن برگردید."}
              </p>
            </div>
          )}
          <button className="primary-button" onClick={saveDraft}>
            <Save size={17} />
            ذخیره پروژه فعلی
          </button>
        </Modal>
      )}

      {modal === "reset" && (
        <Modal title="شروع یک پروژه جدید؟" onClose={closeModal}>
          <div className="reset-modal-icon">
            <RotateCcw size={29} />
          </div>
          <p className="reset-description">
            اطلاعات فرم پاک می‌شود و ضرایب به مقدار پیش‌فرض برمی‌گردند.
            پیش‌نویس‌های ذخیره‌شده شما حذف نمی‌شوند.
          </p>
          <div className="modal-actions">
            <button className="primary-button" onClick={reset}>
              <RotateCcw size={17} />
              ریست فرم و شروع جدید
            </button>
            <button className="secondary-button" onClick={closeModal}>
              انصراف
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}
