type SparklineProps = {
  points: number[];
  color?: string;
};

export function Sparkline({ points, color = "#48f5d2" }: SparklineProps) {
  const max = Math.max(...points);
  const min = Math.min(...points);
  const range = max - min || 1;
  const coords = points
    .map((point, index) => {
      const x = (index / (points.length - 1)) * 100;
      const y = 42 - ((point - min) / range) * 34;
      return `${x},${y}`;
    })
    .join(" ");

  return (
    <svg className="sparkline" viewBox="0 0 100 48" preserveAspectRatio="none" aria-hidden="true">
      <defs>
        <linearGradient id={`glow-${color.replace("#", "")}`} x1="0" x2="1">
          <stop offset="0%" stopColor={color} stopOpacity="0.1" />
          <stop offset="100%" stopColor={color} stopOpacity="0.9" />
        </linearGradient>
      </defs>
      <polyline points={coords} fill="none" stroke={`url(#glow-${color.replace("#", "")})`} strokeWidth="3" />
    </svg>
  );
}
