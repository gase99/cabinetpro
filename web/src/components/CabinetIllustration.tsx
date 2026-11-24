import type { SectionKey } from "../types";

export function CabinetIllustration({ section }: { section: SectionKey }) {
  return (
    <svg
      className={`cabinet-illustration cabinet-${section}`}
      width="64"
      height="52"
      viewBox="0 0 64 52"
      fill="none"
      aria-hidden="true"
    >
      {section === "ground" && (
        <>
          <path
            d="M8 24H56V47H8z"
            fill="#eaf0f4"
            stroke="currentColor"
            strokeWidth="1.7"
          />
          <path
            d="M5 20H59V25H5z"
            fill="#cfa96a"
            stroke="currentColor"
            strokeWidth="1.5"
          />
          <path
            d="M24 25V47M40 25V47M10 47V50M54 47V50"
            stroke="currentColor"
            strokeWidth="1.5"
          />
          <path
            d="M19 30V35M35 30V35M51 30V35"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
          />
          <path
            d="M27 20V15C27 12 30 11 32 11C34 11 37 12 37 15V20M32 11V8"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
          />
        </>
      )}
      {section === "wall" && (
        <>
          <path
            d="M8 8H56V33H8z"
            fill="#eaf0f4"
            stroke="currentColor"
            strokeWidth="1.7"
          />
          <path
            d="M24 8V33M40 8V33M8 33H56"
            stroke="currentColor"
            strokeWidth="1.5"
          />
          <path
            d="M19 26V21M35 26V21M51 26V21"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
          />
          <path
            d="M13 5H51M13 36H51"
            stroke="#cfa96a"
            strokeWidth="2"
            strokeLinecap="round"
          />
          <path
            d="M18 44H46"
            stroke="#bdc8d3"
            strokeWidth="1.5"
            strokeLinecap="round"
          />
        </>
      )}
      {section === "tall" && (
        <>
          <path
            d="M20 3H44V48H20z"
            fill="#eaf0f4"
            stroke="currentColor"
            strokeWidth="1.7"
          />
          <path
            d="M20 22H44M20 43H44M22 48V50M42 48V50"
            stroke="currentColor"
            strokeWidth="1.5"
          />
          <path
            d="M38 12V17M38 29V35"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
          />
          <path
            d="M24 6H40"
            stroke="#cfa96a"
            strokeWidth="2"
            strokeLinecap="round"
          />
        </>
      )}
      {section === "island" && (
        <>
          <path
            d="M9 23H55V43H9z"
            fill="#eaf0f4"
            stroke="currentColor"
            strokeWidth="1.7"
          />
          <path
            d="M4 18H60V24H4z"
            fill="#cfa96a"
            stroke="currentColor"
            strokeWidth="1.5"
          />
          <path
            d="M24 24V43M40 24V43M11 43V48M53 43V48"
            stroke="currentColor"
            strokeWidth="1.5"
          />
          <path
            d="M17 29H21M33 29H37M46 29H50"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
          />
          <path
            d="M20 18V12H29V18M24 12V8"
            stroke="currentColor"
            strokeWidth="1.5"
          />
        </>
      )}
    </svg>
  );
}
