import { Plus, Trash2, Wrench } from "lucide-react";
import type { Accessory, FieldErrors } from "../types";
import { ACCESSORY_SUGGESTIONS } from "../data";
import { formatMoney } from "../utils/format";
import { numeric } from "../utils/calculatePrice";
import { NumericField, SectionHeader } from "./UI";

interface Props {
  accessories: Accessory[];
  onChange: (accessories: Accessory[]) => void;
  errors: FieldErrors;
}

export function AccessoryList({ accessories, onChange, errors }: Props) {
  const add = (name = "", price = 0) =>
    onChange([
      ...accessories,
      { id: crypto.randomUUID(), name, quantity: 1, unitPrice: price },
    ]);
  const update = (id: string, key: keyof Accessory, value: string | number) =>
    onChange(
      accessories.map((item) =>
        item.id === id ? { ...item, [key]: value } : item,
      ),
    );
  return (
    <section className="panel accessories-panel">
      <SectionHeader
        step="۵"
        title="یراق‌آلات و تجهیزات ثابت"
        subtitle="لولا، دستگیره، ریل و سایر تجهیزات"
        action={
          <button className="small-button gold-outline" onClick={() => add()}>
            <Plus size={16} /> افزودن آیتم
          </button>
        }
      />
      {accessories.length === 0 ? (
        <div className="accessories-empty">
          <span className="empty-icon">
            <Wrench size={24} />
          </span>
          <div>
            <h3>هنوز تجهیزاتی اضافه نشده است</h3>
            <p>آیتم‌های مورد نیاز پروژه را به‌صورت جداگانه اضافه کنید.</p>
          </div>
          <button className="text-button" onClick={() => add()}>
            افزودن اولین آیتم <Plus size={15} />
          </button>
        </div>
      ) : (
        <div className="accessories-table">
          <div className="accessory-labels">
            <span>نام تجهیز</span>
            <span>تعداد</span>
            <span>قیمت واحد (تومان)</span>
            <span>جمع</span>
            <span />
          </div>
          {accessories.map((item) => (
            <div className="accessory-row" key={item.id}>
              <div className="field">
                <input
                  className="accessory-name"
                  list="accessory-suggestions"
                  aria-label="نام تجهیز"
                  placeholder="نام تجهیز را وارد کنید"
                  value={item.name}
                  onChange={(event) => {
                    const suggestion = ACCESSORY_SUGGESTIONS.find(
                      (option) => option.name === event.target.value,
                    );
                    onChange(
                      accessories.map((row) =>
                        row.id === item.id
                          ? {
                              ...row,
                              name: event.target.value,
                              unitPrice: suggestion?.price ?? row.unitPrice,
                            }
                          : row,
                      ),
                    );
                  }}
                />
              </div>
              <NumericField
                ariaLabel="تعداد تجهیز"
                value={item.quantity}
                onChange={(value) => update(item.id, "quantity", value)}
                error={errors[`accessory.${item.id}.quantity`]}
              />
              <NumericField
                ariaLabel="قیمت واحد تجهیز"
                value={item.unitPrice}
                onChange={(value) => update(item.id, "unitPrice", value)}
                grouped
                error={errors[`accessory.${item.id}.unitPrice`]}
              />
              <span className="accessory-total">
                {formatMoney(numeric(item.quantity) * numeric(item.unitPrice))}
              </span>
              <button
                className="delete-accessory icon-button"
                aria-label={`حذف ${item.name || "تجهیز"}`}
                onClick={() =>
                  onChange(accessories.filter((row) => row.id !== item.id))
                }
              >
                <Trash2 size={17} />
              </button>
            </div>
          ))}
          <datalist id="accessory-suggestions">
            {ACCESSORY_SUGGESTIONS.map((item) => (
              <option key={item.name} value={item.name} />
            ))}
          </datalist>
          <div className="accessory-sum">
            <span>جمع یراق‌آلات و تجهیزات</span>
            <strong>
              {formatMoney(
                accessories.reduce(
                  (sum, item) =>
                    sum + numeric(item.quantity) * numeric(item.unitPrice),
                  0,
                ),
              )}{" "}
              <small>تومان</small>
            </strong>
          </div>
        </div>
      )}
      {accessories.length === 0 && (
        <div className="suggestions">
          <span>افزودن سریع:</span>
          {ACCESSORY_SUGGESTIONS.slice(0, 3).map((item) => (
            <button key={item.name} onClick={() => add(item.name, item.price)}>
              <Plus size={12} />
              {item.name}
            </button>
          ))}
        </div>
      )}
    </section>
  );
}
