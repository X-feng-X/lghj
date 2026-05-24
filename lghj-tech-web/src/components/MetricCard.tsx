import type { ReactNode } from "react";

type MetricCardProps = {
  label: string;
  value: string;
  hint: string;
  icon: ReactNode;
  tone?: "cyan" | "green" | "amber" | "rose";
};

export function MetricCard({ label, value, hint, icon, tone = "cyan" }: MetricCardProps) {
  return (
    <div className={`metric-card tone-${tone}`}>
      <div className="metric-icon">{icon}</div>
      <div>
        <span>{label}</span>
        <strong>{value}</strong>
        <small>{hint}</small>
      </div>
    </div>
  );
}
