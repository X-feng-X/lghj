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
  riskLabel: "集中持仓",
};

export const positions = [
  { symbol: "600519", name: "贵州茅台", quantity: 1, cost: 1485.3, weight: 100, risk: "高集中" },
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
  title: "AI 交易画像结论",
  risk: "全部持仓集中在贵州茅台，近期委托仍偏买入，缺少卖出纪律和分散缓冲。",
  suggestions: [
    "先核查 000001、600000 的异常挂单，确认是否为测试单。",
    "将单一持仓占比目标降至 50% 以下，补充弱相关资产观察池。",
    "为 600519 设置止盈、止损观察线，形成交易闭环。",
  ],
};

export const advisorChat = [
  {
    role: "user",
    text: "根据我的模拟交易记录，当前账户最大的问题是什么？",
  },
  {
    role: "assistant",
    text: "你的账户目前不是收益率问题，而是交易结构问题：持仓过度集中，挂单以买入为主，缺少退出规则。建议先处理异常挂单，再为贵州茅台设置观察区间和减仓条件。",
  },
  {
    role: "assistant",
    text: "这不是保本预测，也不是买卖承诺。后续需要关注成交纪律、单票仓位上限、止损触发条件和市场风格切换。",
  },
];

export const advisorTasks = [
  { label: "交易画像", value: "高集中 + 买入偏好", level: 86 },
  { label: "风控优先级", value: "先处理异常委托", level: 74 },
  { label: "组合建议", value: "补充弱相关标的池", level: 62 },
];

export const marketNews = [
  "北向资金盘中净流入扩大，消费与银行板块活跃。",
  "多家券商上调高股息资产配置权重，防御风格升温。",
  "AI 投顾提示：高集中度账户需优先关注仓位纪律。",
];

export const communityPosts = [
  { author: "量化研究员", title: "高股息策略是否还适合震荡市？", heat: 128 },
  { author: "趋势猎手", title: "白酒板块缩量反弹，等待确认信号", heat: 96 },
  { author: "风控笔记", title: "模拟盘最容易忽视的三类订单风险", heat: 84 },
];
