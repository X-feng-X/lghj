export type MarketIndex = {
  name: string;
  code: string;
  value: string;
  change: string;
  trend: "up" | "down";
};

export type WatchStock = {
  symbol: string;
  name: string;
  price: string;
  change: string;
  heat: number;
};

export const marketIndexes: MarketIndex[] = [
  { name: "上证指数", code: "SH000001", value: "3,214.86", change: "+0.82%", trend: "up" },
  { name: "深证成指", code: "SZ399001", value: "10,842.31", change: "+1.14%", trend: "up" },
  { name: "创业板指", code: "SZ399006", value: "2,126.48", change: "-0.27%", trend: "down" },
  { name: "科创50", code: "SH000688", value: "932.18", change: "+0.36%", trend: "up" },
];

export const watchlist: WatchStock[] = [
  { symbol: "600519", name: "贵州茅台", price: "1,485.30", change: "+0.64%", heat: 86 },
  { symbol: "000001", name: "平安银行", price: "11.28", change: "-0.18%", heat: 42 },
  { symbol: "600000", name: "浦发银行", price: "8.41", change: "+0.21%", heat: 51 },
  { symbol: "300750", name: "宁德时代", price: "196.70", change: "+1.92%", heat: 73 },
];

export const tickerItems = [
  ...marketIndexes.map((item) => ({ id: item.code, name: item.name, quote: item.value, change: item.change })),
  ...watchlist.map((item) => ({ id: item.symbol, name: item.name, quote: item.price, change: item.change })),
];

export const account = {
  totalAsset: 200000,
  cash: 51470,
  availableCash: 51370,
  frozenCash: 100,
  riskScore: 71,
  riskLabel: "仓位偏集中",
};

export const positions = [
  { symbol: "600519", name: "贵州茅台", quantity: 1, cost: 1485.3, weight: 100, risk: "单票过重" },
];

export const orders = [
  { symbol: "000001", name: "平安银行", side: "买入", price: 1.0, quantity: 0, status: "待处理" },
  { symbol: "600000", name: "浦发银行", side: "买入", price: 1.0, quantity: 0, status: "待处理" },
  { symbol: "600519", name: "贵州茅台", side: "买入", price: 500, quantity: 1, status: "已撤销" },
];

export const deals = [
  { symbol: "600519", name: "贵州茅台", side: "买入", price: 1485.3, quantity: 1, time: "09:42:16" },
];

export const advisorMessage = {
  title: "先别急着冲",
  risk: "账户只抱一只票。买单还在排队。先把刹车装上。",
  suggestions: ["先看两张 1 元挂单。", "单票别超过 50%。", "给 600519 画止损线。"],
};

export const advisorChat = [
  {
    role: "user",
    text: "我现在最大问题是啥？",
  },
  {
    role: "assistant",
    text: "不是赚少了。是仓位太单薄。只靠一只票硬扛。",
  },
  {
    role: "assistant",
    text: "先撤异常单。再设止损。最后再谈加仓。",
  },
];

export const advisorTasks = [
  { label: "交易画像", value: "集中 + 爱买", level: 86 },
  { label: "风控优先", value: "先查挂单", level: 74 },
  { label: "组合建议", value: "补两类资产", level: 62 },
];

export const marketNews = ["银行板块放量。", "白酒午后缩量。", "高股息又热了。", "AI 提醒先看仓位。"];

export const communityPosts = [
  { id: 1, author: "量化老王", title: "别把模拟盘当许愿池", context: "先看仓位，再看收益。", heat: 128 },
  { id: 2, author: "趋势猎手", title: "白酒反弹别上头", context: "缩量反弹要轻一点。", heat: 96 },
  { id: 3, author: "风控笔记", title: "1 元挂单很刺眼", context: "测试单也要及时撤。", heat: 84 },
];
