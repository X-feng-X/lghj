import { FormEvent, ReactNode, useEffect, useMemo, useState } from "react";
import { Icon } from "@iconify/react";
import {
  api,
  ApiError,
  BlogCommentDTO,
  BlogDTO,
  LoginVO,
  PositionDTO,
  RealtimeQuoteDTO,
  SimAccountDTO,
  StockDocDTO,
  StockFollowDTO,
  StockNewsDTO,
  TradeDealDTO,
  TradeOrderDTO,
} from "./api";
import { Sparkline } from "./components/Sparkline";
import { tickerItems } from "./data/mock";

type Page = "home" | "market" | "advisor" | "optional" | "community" | "news" | "trade" | "auth";
type Notice = { type: "success" | "error" | "info"; text: string };
type OrderRow = { left: string; meta: string; middle: string; right: string; id?: number; canCancel?: boolean };
type ChatMessage = { role: "user" | "assistant"; text: string };

const protectedPages = new Set<Page>(["advisor", "optional", "community", "news", "trade"]);
const defaultStocks = [
  { market: "sh", code: "600519", label: "贵州茅台" },
  { market: "sz", code: "000001", label: "平安银行" },
  { market: "sh", code: "601318", label: "中国平安" },
];

const icon = (name: string) => <Icon icon={name} aria-hidden="true" />;
const toNumber = (value: unknown, fallback = 0) => Number(value ?? fallback) || fallback;
const formatMoney = (value: number, digits = 0) =>
  new Intl.NumberFormat("zh-CN", { style: "currency", currency: "CNY", maximumFractionDigits: digits }).format(value || 0);
const formatPercent = (value?: number) => `${toNumber(value).toFixed(2)}%`;
const inferMarket = (code: string) => (/^(5|6|9)/.test(code.trim()) ? "sh" : "sz");

const getStoredUser = (): LoginVO | null => {
  const raw = localStorage.getItem("lghj_user");
  if (!raw) return null;
  try {
    return JSON.parse(raw) as LoginVO;
  } catch {
    return null;
  }
};

function getErrorText(error: unknown) {
  if (error instanceof ApiError) return error.message;
  if (error instanceof Error) return error.message;
  return "操作失败，请稍后再试。";
}

const directionLabel = (direction?: number) => (direction === 2 ? "卖出" : "买入");
const orderStatus = (status?: number) => {
  const map: Record<number, string> = { 1: "待成交", 2: "部分成交", 3: "已成交", 4: "已撤销" };
  return status ? map[status] || `状态 ${status}` : "待处理";
};

export default function App() {
  const [page, setPage] = useState<Page>("home");
  const [user, setUser] = useState<LoginVO | null>(() => getStoredUser());
  const [loginHint, setLoginHint] = useState("登录后才能访问交易、社区、自选股和 AI 顾问。");

  const openPage = (nextPage: Page) => {
    if (protectedPages.has(nextPage) && !user) {
      setLoginHint("这个功能需要先登录。登录后自动回到系统。");
      setPage("auth");
      window.scrollTo({ top: 0, behavior: "smooth" });
      return;
    }
    setPage(nextPage);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleLogin = (nextUser: LoginVO) => {
    localStorage.setItem("token", nextUser.token);
    localStorage.setItem("lghj_user", JSON.stringify(nextUser));
    setUser(nextUser);
    setPage("home");
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("lghj_token");
    localStorage.removeItem("userToken");
    localStorage.removeItem("lghj_user");
    setUser(null);
    setPage("home");
  };

  return (
    <main className="terminal-shell">
      <div className="grid-field" />
      <Topbar page={page} user={user} onNavigate={openPage} onLogout={handleLogout} />
      {page !== "auth" && <Ticker />}
      {page === "home" && <HomePage onNavigate={openPage} />}
      {page === "market" && <MarketPage onNavigate={openPage} />}
      {page === "advisor" && <AdvisorPage onNavigate={openPage} user={user} />}
      {page === "optional" && <OptionalPage onNavigate={openPage} />}
      {page === "community" && <CommunityPage user={user} />}
      {page === "news" && <NewsPage onNavigate={openPage} />}
      {page === "trade" && <TradePage onNavigate={openPage} />}
      {page === "auth" && <AuthPage hint={loginHint} onLogin={handleLogin} />}
    </main>
  );
}

function Topbar({ page, user, onNavigate, onLogout }: { page: Page; user: LoginVO | null; onNavigate: (page: Page) => void; onLogout: () => void }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const navItems: Array<{ page: Page; label: string; protected?: boolean }> = [
    { page: "home", label: "首页" },
    { page: "market", label: "行情中心" },
    { page: "advisor", label: "AI 顾问", protected: true },
    { page: "optional", label: "自选股", protected: true },
    { page: "community", label: "股友社区", protected: true },
    { page: "news", label: "财经资讯", protected: true },
    { page: "trade", label: "模拟交易", protected: true },
  ];

  const confirmLogout = () => {
    if (window.confirm("确认退出登录？")) {
      setMenuOpen(false);
      onLogout();
    }
  };

  return (
    <header className="topbar">
      <button className="brand" type="button" onClick={() => onNavigate("home")}>
        <span className="brand-mark">{icon("solar:chart-square-bold-duotone")}</span>
        <strong>量股化金</strong>
      </button>
      <nav className="nav-tabs" aria-label="主导航">
        {navItems.map((item) => (
          <button className={page === item.page ? "active" : ""} key={item.page} type="button" onClick={() => onNavigate(item.page)}>
            {item.label}
            {item.protected && !user && <i />}
          </button>
        ))}
      </nav>
      <div className="top-actions">
        <StockSearchBox className="top-search" placeholder="搜索股票/代码" onSelect={(stock) => onNavigate("market")} />
        {user ? (
          <div className="user-menu">
            <button className="user-chip" type="button" onClick={() => setMenuOpen((value) => !value)}>
              {icon("solar:user-circle-bold-duotone")}
              {user.username}
              {icon("solar:alt-arrow-down-linear")}
            </button>
            {menuOpen && (
              <div className="user-popover">
                <span>已登录</span>
                <b>{user.username}</b>
                <button type="button" onClick={confirmLogout}>退出登录</button>
              </div>
            )}
          </div>
        ) : (
          <button className="login-chip" type="button" onClick={() => onNavigate("auth")}>登录</button>
        )}
      </div>
    </header>
  );
}

function StockSearchBox({
  value,
  placeholder = "输入股票/代码",
  className = "",
  onInput,
  onSelect,
}: {
  value?: string;
  placeholder?: string;
  className?: string;
  onInput?: (value: string) => void;
  onSelect?: (stock: StockDocDTO) => void;
}) {
  const [keyword, setKeyword] = useState(value || "");
  const [results, setResults] = useState<StockDocDTO[]>([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (value !== undefined) setKeyword(value);
  }, [value]);

  useEffect(() => {
    const text = keyword.trim();
    if (text.length < 2) {
      setResults([]);
      return;
    }
    const timer = window.setTimeout(async () => {
      setLoading(true);
      try {
        const next = await api.searchStocks(text);
        setResults((next || []).slice(0, 8));
        setOpen(true);
      } catch {
        setResults([]);
      } finally {
        setLoading(false);
      }
    }, 220);
    return () => window.clearTimeout(timer);
  }, [keyword]);

  const choose = (stock: StockDocDTO) => {
    setKeyword(stock.symbol);
    setOpen(false);
    onInput?.(stock.symbol);
    onSelect?.(stock);
  };

  return (
    <div className={`stock-search ${className}`}>
      <div className="search-box">
        {icon("solar:magnifer-bold")}
        <input
          value={keyword}
          placeholder={placeholder}
          onBlur={() => window.setTimeout(() => setOpen(false), 140)}
          onChange={(event) => {
            setKeyword(event.target.value);
            onInput?.(event.target.value);
            setOpen(true);
          }}
          onFocus={() => setOpen(true)}
        />
      </div>
      {open && (loading || results.length > 0) && (
        <div className="stock-dropdown">
          {loading && <span className="dropdown-empty">搜索中</span>}
          {!loading && results.map((stock) => (
            <button type="button" key={`${stock.symbol}-${stock.id || ""}`} onMouseDown={() => choose(stock)}>
              <strong>{stock.symbol}</strong>
              <span>{stock.name || "未命名"}</span>
              <em>{stock.industry || stock.marketType || "股票"}</em>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

function Ticker() {
  return (
    <section className="ticker">
      <div className="ticker-track">
        {tickerItems.concat(tickerItems).map((item, index) => (
          <span key={`${item.id}-${index}`} className={item.change.startsWith("+") ? "up" : "down"}>
            {item.name} {item.quote} {item.change}
          </span>
        ))}
      </div>
    </section>
  );
}

function HomePage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  const [quotes, setQuotes] = useState<RealtimeQuoteDTO[]>([]);
  const [selected, setSelected] = useState(defaultStocks[0]);
  const [minutePoints, setMinutePoints] = useState<number[]>([]);
  const [news, setNews] = useState<StockNewsDTO[]>([]);
  const [notice, setNotice] = useState<Notice>({ type: "info", text: "正在读取真实行情。" });

  const loadMarket = async (market: string, code: string) => {
    try {
      const [quote, minute, nextNews] = await Promise.all([
        api.getRealtimeQuote({ market, code }),
        api.getMinuteData({ market, code }).catch(() => []),
        api.getStockNews(code, 6).catch(() => []),
      ]);
      setSelected({ market, code, label: quote?.name || code });
      if (quote) {
        setQuotes((current) => {
          const rest = current.filter((item) => item.code !== quote.code);
          return [quote, ...rest].slice(0, 6);
        });
      }
      setMinutePoints(toSparkPoints(minute));
      setNews(nextNews || []);
      setNotice({ type: "success", text: `${quote?.name || code} 行情已更新。` });
      return quote;
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
      return null;
    }
  };

  useEffect(() => {
    Promise.all(defaultStocks.map((stock) => api.getRealtimeQuote({ market: stock.market, code: stock.code }).catch(() => null)))
      .then((items) => setQuotes(items.filter(Boolean) as RealtimeQuoteDTO[]));
    loadMarket(defaultStocks[0].market, defaultStocks[0].code);
  }, []);

  const chartPoints = minutePoints.length > 2 ? minutePoints : [22, 30, 25, 38, 34, 48, 39, 42, 36, 54, 49, 60, 58];
  const activeQuote = quotes.find((item) => item.code === selected.code) || quotes[0];

  return (
    <div className="screen-grid">
      <section className="index-strip">
        {quotes.length === 0 && <EmptyCard text="真实行情加载中。" />}
        {quotes.map((item) => (
          <article className="glass-card index-tile" key={item.code}>
            <span>{item.name || item.code}</span>
            <strong>{formatMoney(toNumber(item.price), 2)}</strong>
            <em className={toNumber(item.changePercent) >= 0 ? "up" : "down"}>{formatPercent(item.changePercent)}</em>
          </article>
        ))}
      </section>

      <section className="glass-card chart-panel">
        <PanelHeader title={activeQuote?.name || selected.label} action="看资讯" onAction={() => onNavigate("news")} />
        <NoticeBar notice={notice} />
        <div className="inline-form home-query">
          <StockSearchBox
            value={selected.code}
            onInput={(value) => setSelected({ ...selected, code: value })}
            onSelect={(stock) => loadMarket(inferMarket(stock.symbol), stock.symbol)}
          />
          <button type="button" onClick={() => loadMarket(inferMarket(selected.code), selected.code)}>查询</button>
        </div>
        <div className="chart-stage">
          <Sparkline points={chartPoints} color="#53d4ff" />
          <div className="candle-grid">
            {chartPoints.concat(chartPoints).slice(0, 46).map((point, index) => <i key={index} style={{ height: `${18 + (point % 76)}%` }} />)}
          </div>
          <div className="chart-tip">
            <b>{selected.code}</b>
            <span>最新 {formatMoney(toNumber(activeQuote?.price), 2)}</span>
            <span>涨跌 {formatPercent(activeQuote?.changePercent)}</span>
          </div>
        </div>
      </section>

      <aside className="side-stack">
        <section className="glass-card">
          <PanelHeader title="真实行情" action="交易" onAction={() => onNavigate("trade")} />
          {quotes.map((stock) => (
            <DataLine key={stock.code} title={stock.name || stock.code} meta={stock.code} value={formatPercent(stock.changePercent)} tone={toNumber(stock.changePercent) >= 0 ? "up" : "down"} />
          ))}
        </section>
        <section className="glass-card">
          <PanelHeader title="最新资讯" action="更多" onAction={() => onNavigate("news")} />
          {news.length === 0 && <p className="empty-text">暂无资讯。</p>}
          {news.map((item) => <NewsLine key={`${item.title}-${item.publishTime || ""}`} title={item.title} />)}
        </section>
        <button className="advisor-launch" type="button" onClick={() => onNavigate("advisor")}>
          {icon("solar:chat-round-like-bold-duotone")}
          找 AI 投资顾问
        </button>
      </aside>
    </div>
  );
}

function MarketPage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  return (
    <PageFrame title="行情中心" subtitle="查一只票。看真实数据。">
      <section className="market-layout">
        <div className="glass-card chart-panel big">
          <PanelHeader title="行情工作台" action="去交易" onAction={() => onNavigate("trade")} />
          <HomeMarketPanel />
        </div>
        <aside className="side-stack">
          <section className="glass-card">
            <PanelHeader title="下一步" />
            {["输入股票代码", "读取真实行情", "看新闻和分时", "再决定买卖"].map((item) => <DataLine key={item} title={item} meta="流程" value="可用" tone="up" />)}
          </section>
          <button className="advisor-launch" type="button" onClick={() => onNavigate("advisor")}>让 AI 解读行情</button>
        </aside>
      </section>
    </PageFrame>
  );
}

function HomeMarketPanel() {
  const [code, setCode] = useState("600519");
  const [quote, setQuote] = useState<RealtimeQuoteDTO | null>(null);
  const [points, setPoints] = useState<number[]>([]);
  const [notice, setNotice] = useState<Notice>({ type: "info", text: "输入股票，读取后端真实行情。" });

  const search = async (nextCode = code) => {
    try {
      const market = inferMarket(nextCode);
      const [nextQuote, minute] = await Promise.all([
        api.getRealtimeQuote({ market, code: nextCode }),
        api.getMinuteData({ market, code: nextCode }).catch(() => []),
      ]);
      setQuote(nextQuote);
      setPoints(toSparkPoints(minute));
      setNotice({ type: "success", text: `${nextQuote?.name || nextCode} 已更新。` });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    }
  };

  useEffect(() => {
    search("600519");
  }, []);

  return (
    <>
      <NoticeBar notice={notice} />
      <div className="inline-form home-query">
        <StockSearchBox value={code} onInput={setCode} onSelect={(stock) => search(stock.symbol)} />
        <button type="button" onClick={() => search()}>搜索</button>
      </div>
      <MetricTriplet values={[
        ["最新价", formatMoney(toNumber(quote?.price), 2)],
        ["涨跌额", formatMoney(toNumber(quote?.change), 2)],
        ["涨跌幅", formatPercent(quote?.changePercent)],
      ]} />
      <div className="chart-stage tall">
        <Sparkline points={points.length > 2 ? points : [18, 24, 21, 32, 29, 48, 36, 39, 31, 46, 42, 58, 51, 64]} color="#53d4ff" />
      </div>
    </>
  );
}

function AuthPage({ hint, onLogin }: { hint: string; onLogin: (user: LoginVO) => void }) {
  const [mode, setMode] = useState<"login" | "register">("login");
  const [notice, setNotice] = useState<Notice>({ type: "info", text: hint });
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ username: "zhangsan", password: "123456", nickName: "", email: "", phone: "" });

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      if (mode === "register") {
        await api.register({
          username: form.username.trim(),
          password: form.password,
          nickName: form.nickName.trim() || undefined,
          email: form.email.trim() || undefined,
          phone: form.phone.trim() || undefined,
        });
        setNotice({ type: "success", text: "注册成功。现在可以登录。" });
        setMode("login");
      } else {
        const nextUser = await api.login({ username: form.username.trim(), password: form.password });
        onLogin(nextUser);
      }
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="login-screen">
      <div className="login-copy">
        <div className="login-logo">{icon("solar:chart-square-bold-duotone")}<span>Stock Prediction</span></div>
        <h1>股市预测系统</h1>
        <p>先登录。再交易。</p>
        <p>没 token，就别硬闯。</p>
        <div className="red-module">智能决策系统<span>支持模拟交易和 AI 顾问</span></div>
      </div>
      <form className="login-card" onSubmit={submit}>
        <h2>{mode === "login" ? "登录 Stock Prediction" : "注册 Stock Prediction"}</h2>
        <NoticeBar notice={notice} />
        <div className="segmented">
          <button className={mode === "login" ? "active" : ""} type="button" onClick={() => setMode("login")}>登录</button>
          <button className={mode === "register" ? "active" : ""} type="button" onClick={() => setMode("register")}>注册</button>
        </div>
        <label>用户名<input value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} required /></label>
        <label>密码<input type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} required /></label>
        {mode === "register" && (
          <>
            <label>昵称<input value={form.nickName} onChange={(event) => setForm({ ...form, nickName: event.target.value })} /></label>
            <label>邮箱<input value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label>
            <label>手机<input value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} /></label>
          </>
        )}
        <button className="primary-btn" type="submit" disabled={loading}>{loading ? "处理中" : mode === "login" ? "登录" : "创建账号"}</button>
      </form>
    </section>
  );
}

function TradePage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  const [accountInfo, setAccountInfo] = useState<SimAccountDTO | null>(null);
  const [positionList, setPositionList] = useState<PositionDTO[]>([]);
  const [orderList, setOrderList] = useState<TradeOrderDTO[]>([]);
  const [dealList, setDealList] = useState<TradeDealDTO[]>([]);
  const [notice, setNotice] = useState<Notice>({ type: "info", text: "这里连接后端模拟交易接口。" });
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ symbol: "600519", direction: "1", price: "1485.30", quantity: "100" });

  const refreshTrade = async () => {
    setLoading(true);
    try {
      const [nextAccount, nextPositions, nextOrders, nextDeals] = await Promise.all([api.getAccount(), api.getPositions(), api.getOrders(), api.getDeals()]);
      setAccountInfo(nextAccount);
      setPositionList(nextPositions || []);
      setOrderList(nextOrders || []);
      setDealList(nextDeals || []);
      setNotice({ type: "success", text: "交易数据已刷新。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshTrade();
  }, []);

  const summary = useMemo(() => ({
    totalAsset: toNumber(accountInfo?.totalAsset),
    availableCash: toNumber(accountInfo?.availableCash),
    frozenCash: toNumber(accountInfo?.frozenCash),
  }), [accountInfo]);

  const createAccount = async () => {
    setLoading(true);
    try {
      setAccountInfo(await api.createAccount());
      setNotice({ type: "success", text: "模拟账户已开通。" });
      await refreshTrade();
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  const submitOrder = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      await api.createOrder({ symbol: form.symbol.trim(), direction: Number(form.direction) as 1 | 2, price: Number(form.price), quantity: Number(form.quantity) });
      setNotice({ type: "success", text: `${form.direction === "1" ? "买入" : "卖出"}委托已提交。` });
      await refreshTrade();
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  const cancelOrder = async (orderId?: number) => {
    if (!orderId) return;
    setLoading(true);
    try {
      await api.cancelOrder(orderId);
      setNotice({ type: "success", text: "撤单成功。" });
      await refreshTrade();
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  const orders: OrderRow[] = orderList.map((order) => ({
    left: order.symbol,
    meta: directionLabel(order.direction),
    middle: `${order.quantity} 股`,
    right: orderStatus(order.status),
    id: order.id,
    canCancel: order.status === 1 || order.status === 2,
  }));

  return (
    <PageFrame title="模拟交易" subtitle="开户、买入、卖出、撤单，都在这一屏。">
      <NoticeBar notice={notice} />
      <section className="glass-card account-strip">
        <PanelHeader title="我的模拟账户" action="刷新" onAction={refreshTrade} />
        <MetricTriplet values={[["总资产", formatMoney(summary.totalAsset)], ["可用资金", formatMoney(summary.availableCash)], ["冻结资金", formatMoney(summary.frozenCash)]]} />
        <button className="small-action" type="button" onClick={createAccount} disabled={loading}>开户</button>
      </section>
      <section className="trade-grid">
        <form className="glass-card ticket-card" onSubmit={submitOrder}>
          <PanelHeader title="买卖股票" />
          <div className="segmented wide">
            <button className={form.direction === "1" ? "active" : ""} type="button" onClick={() => setForm({ ...form, direction: "1" })}>买入</button>
            <button className={form.direction === "2" ? "active" : ""} type="button" onClick={() => setForm({ ...form, direction: "2" })}>卖出</button>
          </div>
          <label>股票代码<StockSearchBox value={form.symbol} onInput={(symbol) => setForm({ ...form, symbol })} onSelect={(stock) => setForm({ ...form, symbol: stock.symbol })} /></label>
          <label>委托价格<input type="number" min="0" step="0.01" value={form.price} onChange={(event) => setForm({ ...form, price: event.target.value })} /></label>
          <label>数量<input type="number" min="1" step="1" value={form.quantity} onChange={(event) => setForm({ ...form, quantity: event.target.value })} /></label>
          <button className="primary-btn" type="submit" disabled={loading}>{form.direction === "1" ? "买入" : "卖出"}</button>
        </form>
        <section className="glass-card table-card">
          <PanelHeader title="持仓" />
          <Table headers={["代码", "持仓数量", "成本价", "盈亏"]} rows={positionList.map((p) => [p.symbol, String(p.totalQuantity ?? 0), formatMoney(toNumber(p.costPrice), 2), formatMoney(toNumber(p.profitLoss), 2)])} />
          {positionList.length === 0 && <p className="empty-text">暂无持仓。</p>}
        </section>
      </section>
      <section className="glass-card table-card">
        <PanelHeader title="当日委托" />
        <div className="order-list">
          {orders.map((row) => (
            <div className="order-row" key={`${row.left}-${row.meta}-${row.right}-${row.id || ""}`}>
              <span>{row.left}</span><b>{row.meta}</b><span>{row.middle}</span><em>{row.right}</em>
              {row.canCancel ? <button type="button" onClick={() => cancelOrder(row.id)} disabled={loading}>撤单</button> : <i />}
            </div>
          ))}
        </div>
        {orders.length === 0 && <p className="empty-text">暂无委托。</p>}
      </section>
      <section className="glass-card table-card">
        <PanelHeader title="当日成交" />
        <Table headers={["时间", "代码", "方向", "成交价", "数量"]} rows={dealList.map((d) => [d.createTime || "-", d.symbol, directionLabel(d.direction), formatMoney(toNumber(d.price), 2), String(d.quantity ?? "-")])} />
        {dealList.length === 0 && <p className="empty-text">暂无成交。</p>}
      </section>
      <button className="advisor-launch inline" type="button" onClick={() => onNavigate("advisor")}>让 AI 看一下风险</button>
    </PageFrame>
  );
}

function OptionalPage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  const [optionalStocks, setOptionalStocks] = useState<StockFollowDTO[]>([]);
  const [symbol, setSymbol] = useState("600519");
  const [notice, setNotice] = useState<Notice>({ type: "info", text: "自选股会同步后端。" });
  const [loading, setLoading] = useState(false);

  const refreshOptional = async () => {
    setLoading(true);
    try {
      setOptionalStocks(await api.getOptionalStocks() || []);
      setNotice({ type: "success", text: "自选股已刷新。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshOptional();
  }, []);

  const addOptional = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      await api.addOptionalStock(symbol.trim());
      await refreshOptional();
      setNotice({ type: "success", text: "已添加自选股。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  const removeOptional = async (nextSymbol: string) => {
    setLoading(true);
    try {
      await api.removeOptionalStock(nextSymbol);
      await refreshOptional();
      setNotice({ type: "success", text: "已删除。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageFrame title="我的自选股" subtitle="只放你真会看的票。">
      <NoticeBar notice={notice} />
      <section className="glass-card table-card">
        <form className="inline-form" onSubmit={addOptional}>
          <StockSearchBox value={symbol} onInput={setSymbol} onSelect={(stock) => setSymbol(stock.symbol)} />
          <button type="submit" disabled={loading}>添加自选</button>
          <button type="button" onClick={refreshOptional} disabled={loading}>刷新</button>
        </form>
        <Table
          headers={["代码", "名称", "当前价", "涨跌幅", "操作"]}
          rows={optionalStocks.map((stock) => [
            stock.symbol,
            stock.name || stock.symbol,
            formatMoney(toNumber(stock.price), 2),
            formatPercent(stock.changePercent),
            <button className="link-btn" type="button" onClick={() => removeOptional(stock.symbol)} disabled={loading}>删除</button>,
          ])}
        />
        {optionalStocks.length === 0 && <p className="empty-text">还没有自选股。</p>}
      </section>
      <button className="advisor-launch inline" type="button" onClick={() => onNavigate("advisor")}>预测自选股</button>
    </PageFrame>
  );
}

function CommunityPage({ user }: { user: LoginVO | null }) {
  const [blogs, setBlogs] = useState<BlogDTO[]>([]);
  const [comments, setComments] = useState<Record<number, BlogCommentDTO[]>>({});
  const [openBlogId, setOpenBlogId] = useState<number | null>(null);
  const [commentText, setCommentText] = useState("");
  const [notice, setNotice] = useState<Notice>({ type: "info", text: "可以发帖、点赞、评论、关注。" });
  const [loading, setLoading] = useState(false);
  const [followedUsers, setFollowedUsers] = useState<Record<number, boolean>>({});
  const [form, setForm] = useState({ title: "", stockId: "", context: "" });

  const normalizeComments = (data: unknown): BlogCommentDTO[] => {
    if (Array.isArray(data)) return data as BlogCommentDTO[];
    const page = data as { records?: BlogCommentDTO[]; list?: BlogCommentDTO[] };
    return page?.records || page?.list || [];
  };

  const refreshBlogs = async () => {
    setLoading(true);
    try {
      setBlogs(await api.getHotBlogs() || []);
      setNotice({ type: "success", text: "社区已刷新。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshBlogs();
  }, []);

  const submitBlog = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      await api.createBlog({ title: form.title.trim(), stockId: form.stockId.trim(), context: form.context.trim() });
      setForm({ title: "", stockId: "", context: "" });
      await refreshBlogs();
      setNotice({ type: "success", text: "帖子已发布。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  const loadComments = async (blogId?: number) => {
    if (!blogId) return;
    setOpenBlogId(openBlogId === blogId ? null : blogId);
    try {
      const data = await api.getComments(blogId);
      setComments((current) => ({ ...current, [blogId]: normalizeComments(data) }));
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    }
  };

  const likeBlog = async (id?: number) => {
    if (!id) return;
    const target = blogs.find((blog) => blog.id === id);
    const wasLiked = Boolean(target?.isLike);
    try {
      await api.likeBlog(id);
      setBlogs((current) => current.map((blog) => {
        if (blog.id !== id) return blog;
        const liked = Math.max(0, (blog.liked || 0) + (wasLiked ? -1 : 1));
        return { ...blog, liked, isLike: !wasLiked };
      }));
      setNotice({ type: "success", text: wasLiked ? "已取消点赞。" : "已点赞。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    }
  };

  const submitComment = async (blogId?: number) => {
    if (!blogId || !commentText.trim()) return;
    try {
      await api.addComment({ blogId, content: commentText.trim() });
      setCommentText("");
      const data = await api.getComments(blogId);
      setComments((current) => ({ ...current, [blogId]: normalizeComments(data) }));
      setNotice({ type: "success", text: "评论已发布。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    }
  };

  const likeComment = async (blogId: number, commentId: number) => {
    try {
      await api.likeComment(commentId);
      const data = await api.getComments(blogId);
      setComments((current) => ({ ...current, [blogId]: normalizeComments(data) }));
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    }
  };

  const deleteComment = async (blogId: number, commentId: number) => {
    try {
      await api.deleteComment(commentId);
      const data = await api.getComments(blogId);
      setComments((current) => ({ ...current, [blogId]: normalizeComments(data) }));
      setNotice({ type: "success", text: "评论已删除。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    }
  };

  const followUser = async (userId?: number) => {
    if (!userId) return;
    const next = !followedUsers[userId];
    try {
      await api.followUser(userId, next);
      setFollowedUsers((current) => ({ ...current, [userId]: next }));
      setNotice({ type: "success", text: next ? "已关注。" : "已取消关注。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    }
  };

  return (
    <PageFrame title="股友社区" subtitle="说人话。别装大师。">
      <NoticeBar notice={notice} />
      <section className="community-board">
        <form className="glass-card post-form" onSubmit={submitBlog}>
          <PanelHeader title="发布观点" />
          <input value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} placeholder="标题" required />
          <StockSearchBox value={form.stockId} placeholder="关联股票，可不填" onInput={(stockId) => setForm({ ...form, stockId })} onSelect={(stock) => setForm({ ...form, stockId: stock.symbol })} />
          <textarea value={form.context} onChange={(event) => setForm({ ...form, context: event.target.value })} placeholder="写清楚你的判断。" required />
          <button className="primary-btn" type="submit" disabled={loading}>发布观点</button>
        </form>
        <section className="glass-card post-list">
          <PanelHeader title="热门推荐" />
          {blogs.length === 0 && <p className="empty-text">暂无帖子。</p>}
          {blogs.map((post) => {
            const blogComments = post.id ? comments[post.id] || [] : [];
            return (
              <article className="post-card" key={post.id || post.title}>
                <h3>{post.title}</h3>
                <p>{post.context}</p>
                <small>{post.name || `用户 ${post.userId || ""}`}</small>
                <div className="inline-actions">
                  <button className={post.isLike ? "liked" : ""} type="button" onClick={() => likeBlog(post.id)} disabled={!post.id}>{icon("solar:heart-bold-duotone")}{post.liked || 0}</button>
                  <button type="button" onClick={() => loadComments(post.id)} disabled={!post.id}>{icon("solar:chat-round-dots-bold-duotone")}评论</button>
                  <button type="button" onClick={() => followUser(post.userId)} disabled={!post.userId || user?.id === post.userId}>{post.userId && followedUsers[post.userId] ? "取消关注" : "关注"}</button>
                </div>
                {openBlogId === post.id && (
                  <div className="comment-box">
                    <div className="inline-form compact">
                      <input value={commentText} onChange={(event) => setCommentText(event.target.value)} placeholder="写下你的评论" />
                      <button type="button" onClick={() => submitComment(post.id)}>发送</button>
                    </div>
                    {blogComments.length === 0 && <p className="empty-text">暂无评论</p>}
                    {blogComments.map((comment) => (
                      <div className="comment-line" key={comment.id}>
                        <div><b>{comment.user?.nickname || "匿名用户"}</b><p>{comment.content}</p></div>
                        <button type="button" onClick={() => likeComment(post.id!, comment.id)}>赞 {comment.liked || 0}</button>
                        <button type="button" onClick={() => deleteComment(post.id!, comment.id)}>删除</button>
                      </div>
                    ))}
                  </div>
                )}
              </article>
            );
          })}
        </section>
      </section>
    </PageFrame>
  );
}

function AdvisorPage({ onNavigate, user }: { onNavigate: (page: Page) => void; user: LoginVO | null }) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [question, setQuestion] = useState("");
  const [sessionId, setSessionId] = useState("");
  const [loading, setLoading] = useState(false);
  const [profile, setProfile] = useState<Array<{ label: string; value: string; level: number }>>([]);
  const [notice, setNotice] = useState<Notice | null>(null);
  const advisorUserId = user?.id ? String(user.id) : "web-guest";

  useEffect(() => {
    Promise.all([api.getAccount(), api.getPositions(), api.getOrders(), api.getDeals()])
      .then(([account, positions, orders, deals]) => {
        setProfile(buildTradeProfile(account, positions || [], orders || [], deals || []));
      })
      .catch((error) => setNotice({ type: "error", text: getErrorText(error) }));
  }, []);

  const submitQuestion = async (event: FormEvent) => {
    event.preventDefault();
    const text = question.trim();
    if (!text) return;
    setMessages((current) => [...current, { role: "user", text }]);
    setQuestion("");
    setLoading(true);

    try {
      let nextSessionId = sessionId;
      if (!nextSessionId) {
        const session = await api.createAdvisorSession({ agentId: "investment-advisor", userId: advisorUserId });
        nextSessionId = session.sessionId;
        setSessionId(nextSessionId);
      }
      const reply = await api.chatAdvisor({
        agentId: "investment-advisor",
        userId: advisorUserId,
        sessionId: nextSessionId,
        message: text,
      });
      setMessages((current) => [...current, { role: "assistant", text: reply.content || "AI 服务返回了空内容。" }]);
    } catch (error) {
      setMessages((current) => [...current, { role: "assistant", text: `AI 服务错误：${getErrorText(error)}` }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageFrame title="Agent 投资顾问" subtitle="先问数据。再问判断。">
      {notice && <NoticeBar notice={notice} />}
      <section className="advisor-grid compact">
        <aside className="glass-card profile-panel">
          <PanelHeader title="交易画像" />
          {profile.length === 0 && <p className="empty-text">暂无真实交易画像。</p>}
          {profile.map((task) => (
            <div className="profile-row" key={task.label}>
              <div><b>{task.label}</b><span>{task.value}</span></div>
              <div className="heat-bar"><i style={{ width: `${task.level}%` }} /></div>
            </div>
          ))}
          <button className="advisor-launch inline" type="button" onClick={() => onNavigate("trade")}>去交易页处理</button>
        </aside>
        <section className="glass-card chat-panel">
          <PanelHeader title="顾问对话" />
          <div className="chat-stream empty-ready">
            {messages.length === 0 && <p className="empty-text">对话框已就绪。直接问。</p>}
            {messages.map((message, index) => (
              <div className={`chat-bubble ${message.role}`} key={`${message.role}-${index}`}>
                <span>{message.role === "user" ? "你" : "AI 顾问"}</span>
                {message.role === "assistant" ? <MarkdownMessage text={message.text} /> : <p>{message.text}</p>}
              </div>
            ))}
          </div>
          <form className="chat-composer" onSubmit={submitQuestion}>
            <input value={question} onChange={(event) => setQuestion(event.target.value)} placeholder="输入你的问题，比如：600519 现在多少钱？" />
            <button type="submit" disabled={loading}>{loading ? "等待 AI" : "发送"}</button>
          </form>
        </section>
      </section>
    </PageFrame>
  );
}

function NewsPage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  const [symbol, setSymbol] = useState("600519");
  const [news, setNews] = useState<StockNewsDTO[]>([]);
  const [notice, setNotice] = useState<Notice>({ type: "info", text: "输入股票，读取真实资讯。" });
  const [loading, setLoading] = useState(false);

  const searchNews = async (nextSymbol = symbol) => {
    setLoading(true);
    try {
      const list = await api.getStockNews(nextSymbol.trim(), 12);
      setNews(list || []);
      setNotice({ type: "success", text: `已加载 ${nextSymbol} 的资讯。` });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    searchNews("600519");
  }, []);

  const openNews = (item: StockNewsDTO) => {
    if (!item.url) return;
    window.open(item.url, "_blank", "noopener,noreferrer");
  };

  return (
    <PageFrame title="财经资讯" subtitle="新闻很多。先筛，再看。">
      <NoticeBar notice={notice} />
      <section className="glass-card news-panel">
        <div className="inline-form">
          <StockSearchBox value={symbol} onInput={setSymbol} onSelect={(stock) => searchNews(stock.symbol)} />
          <button type="button" onClick={() => searchNews()} disabled={loading}>搜索</button>
        </div>
        {news.length === 0 && <p className="empty-text">暂无资讯。</p>}
        {news.map((item, index) => (
          <button className="news-item" type="button" key={`${item.title}-${index}`} disabled={!item.url} onClick={() => openNews(item)}>
            <strong>{item.title}</strong>
            <span>{item.source || item.publishTime || "来源未知"}</span>
            {item.url ? icon("solar:alt-arrow-right-linear") : icon("solar:lock-keyhole-minimalistic-linear")}
          </button>
        ))}
      </section>
      <button className="advisor-launch inline" type="button" onClick={() => onNavigate("advisor")}>让 AI 总结资讯</button>
    </PageFrame>
  );
}

function PageFrame({ title, subtitle, children }: { title: string; subtitle: string; children: ReactNode }) {
  return (
    <div className="page-frame">
      <section className="page-title">
        <span>Quant Console</span>
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </section>
      {children}
    </div>
  );
}

function PanelHeader({ title, action, onAction }: { title: string; action?: string; onAction?: () => void }) {
  return (
    <div className="panel-header">
      <h2>{title}</h2>
      {action && <button type="button" onClick={onAction}>{action}</button>}
    </div>
  );
}

function MetricTriplet({ values }: { values: Array<[string, string]> }) {
  return (
    <div className="metric-triplet">
      {values.map(([label, value]) => (
        <div key={label}><span>{label}</span><strong>{value}</strong></div>
      ))}
    </div>
  );
}

function Table({ headers, rows }: { headers: string[]; rows: Array<Array<ReactNode>> }) {
  return (
    <div className="data-table" style={{ "--cols": headers.length } as React.CSSProperties}>
      {headers.map((header) => <b key={header}>{header}</b>)}
      {rows.map((row, rowIndex) => row.map((cell, cellIndex) => <span key={`${rowIndex}-${cellIndex}`}>{cell}</span>))}
    </div>
  );
}

function DataLine({ title, meta, value, tone }: { title: string; meta: string; value: string; tone: "up" | "down" }) {
  return <div className="data-line"><div><strong>{title}</strong><span>{meta}</span></div><em className={tone}>{value}</em></div>;
}

function NewsLine({ title }: { title: string }) {
  return <div className="news-line"><span /> <p>{title}</p></div>;
}

function NoticeBar({ notice }: { notice: Notice }) {
  return <div className={`notice-bar ${notice.type}`}>{notice.text}</div>;
}

function EmptyCard({ text }: { text: string }) {
  return <article className="glass-card index-tile empty-card"><span>{text}</span></article>;
}

function toSparkPoints(rows: Array<Record<string, unknown>>) {
  return rows
    .map((row) => toNumber(row.price ?? row.close ?? row.value ?? row.current))
    .filter((value) => value > 0)
    .slice(-80);
}

function buildTradeProfile(account: SimAccountDTO, positions: PositionDTO[], orders: TradeOrderDTO[], deals: TradeDealDTO[]) {
  const totalAsset = toNumber(account?.totalAsset);
  const positionCost = positions.reduce((sum, item) => sum + toNumber(item.costPrice) * toNumber(item.totalQuantity), 0);
  const buyCount = orders.filter((item) => item.direction === 1).length;
  const sellCount = orders.filter((item) => item.direction === 2).length;
  const activeSymbols = new Set([...positions.map((item) => item.symbol), ...deals.map((item) => item.symbol)].filter(Boolean));

  return [
    { label: "持仓集中度", value: `${positions.length} 只股票`, level: Math.min(100, positions.length * 18) },
    { label: "仓位占比", value: totalAsset ? `${((positionCost / totalAsset) * 100).toFixed(1)}%` : "暂无资产", level: totalAsset ? Math.min(100, (positionCost / totalAsset) * 100) : 0 },
    { label: "交易活跃度", value: `${orders.length} 笔委托`, level: Math.min(100, orders.length * 12) },
    { label: "买卖倾向", value: `买 ${buyCount} / 卖 ${sellCount}`, level: orders.length ? Math.round((buyCount / orders.length) * 100) : 0 },
    { label: "覆盖标的", value: `${activeSymbols.size} 只`, level: Math.min(100, activeSymbols.size * 20) },
  ];
}

function renderInlineMarkdown(text: string) {
  const nodes: ReactNode[] = [];
  text.split(/(\*\*[^*]+\*\*)/g).forEach((part, index) => {
    if (!part) return;
    nodes.push(part.startsWith("**") && part.endsWith("**") ? <strong key={index}>{part.slice(2, -2)}</strong> : part);
  });
  return nodes;
}

function MarkdownMessage({ text }: { text: string }) {
  const blocks: ReactNode[] = [];
  let listItems: string[] = [];
  const flushList = () => {
    if (!listItems.length) return;
    blocks.push(<ul key={`list-${blocks.length}`}>{listItems.map((item, index) => <li key={index}>{renderInlineMarkdown(item)}</li>)}</ul>);
    listItems = [];
  };

  text.split(/\r?\n/).forEach((rawLine, index) => {
    const line = rawLine.trim();
    if (!line) {
      flushList();
      return;
    }
    if (/^-{3,}$/.test(line)) {
      flushList();
      blocks.push(<hr key={index} />);
      return;
    }
    const heading = /^(#{1,4})\s+(.+)$/.exec(line);
    if (heading) {
      flushList();
      blocks.push(<h3 key={index}>{renderInlineMarkdown(heading[2])}</h3>);
      return;
    }
    const list = /^[-*]\s+(.+)$/.exec(line) || /^\d+[.)]\s+(.+)$/.exec(line);
    if (list) {
      listItems.push(list[1]);
      return;
    }
    flushList();
    blocks.push(<p key={index}>{renderInlineMarkdown(line)}</p>);
  });
  flushList();
  return <div className="markdown-message">{blocks}</div>;
}
