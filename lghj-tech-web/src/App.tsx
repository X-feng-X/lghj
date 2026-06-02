import { FormEvent, useEffect, useMemo, useState } from "react";
import { Icon } from "@iconify/react";
import {
  api,
  ApiError,
  BlogCommentDTO,
  BlogDTO,
  LoginVO,
  PositionDTO,
  SimAccountDTO,
  StockFollowDTO,
  TradeDealDTO,
  TradeOrderDTO,
} from "./api";
import { MetricCard } from "./components/MetricCard";
import { Sparkline } from "./components/Sparkline";
import {
  account as mockAccount,
  advisorChat,
  advisorMessage,
  advisorTasks,
  communityPosts,
  deals as mockDeals,
  marketIndexes,
  marketNews,
  orders as mockOrders,
  positions as mockPositions,
  tickerItems,
  watchlist,
} from "./data/mock";

const formatMoney = (value: number) =>
  new Intl.NumberFormat("zh-CN", { style: "currency", currency: "CNY", maximumFractionDigits: 0 }).format(value || 0);

type Page = "home" | "auth" | "trade" | "community" | "advisor" | "news";
type Notice = { type: "success" | "error" | "info"; text: string };
type OrderRow = { left: string; meta: string; middle: string; right: string; id?: number; canCancel?: boolean };

const icon = (name: string) => <Icon icon={name} aria-hidden="true" />;
const toNumber = (value: unknown, fallback = 0) => Number(value ?? fallback) || fallback;
const orderStatus = (status?: number) => {
  const map: Record<number, string> = { 1: "待成交", 2: "部分成交", 3: "已成交", 4: "已撤销" };
  return status ? map[status] || `状态 ${status}` : "待处理";
};
const directionLabel = (direction?: number) => (direction === 2 ? "卖出" : "买入");
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

export default function App() {
  const [page, setPage] = useState<Page>("home");
  const [user, setUser] = useState<LoginVO | null>(() => getStoredUser());

  const openPage = (nextPage: Page) => {
    setPage(nextPage);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleLogin = (nextUser: LoginVO) => {
    localStorage.setItem("token", nextUser.token);
    localStorage.setItem("lghj_user", JSON.stringify(nextUser));
    setUser(nextUser);
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("lghj_token");
    localStorage.removeItem("userToken");
    localStorage.removeItem("lghj_user");
    setUser(null);
  };

  return (
    <main className="app-shell">
      <div className="soft-noise" />
      <Topbar page={page} user={user} onNavigate={openPage} onLogout={handleLogout} />
      <Ticker />
      {page === "home" && <HomePage onNavigate={openPage} />}
      {page === "auth" && <AuthPage onLogin={handleLogin} />}
      {page === "trade" && <TradePage onNavigate={openPage} />}
      {page === "community" && <CommunityPage onNavigate={openPage} user={user} />}
      {page === "advisor" && <AdvisorPage onNavigate={openPage} />}
      {page === "news" && <NewsPage onNavigate={openPage} />}
    </main>
  );
}

function Topbar({ page, user, onNavigate, onLogout }: { page: Page; user: LoginVO | null; onNavigate: (page: Page) => void; onLogout: () => void }) {
  const navItems: Array<{ page: Page; label: string }> = [
    { page: "home", label: "总览" },
    { page: "trade", label: "模拟交易" },
    { page: "community", label: "社区" },
    { page: "advisor", label: "AI 顾问" },
    { page: "news", label: "资讯" },
  ];

  return (
    <header className="topbar">
      <button className="brand" type="button" onClick={() => onNavigate("home")}>
        <span className="brand-mark">{icon("solar:chart-square-bold-duotone")}</span>
        <span>
          <small>量股化金</small>
          <strong>把交易讲清楚</strong>
        </span>
      </button>

      <nav className="nav-tabs" aria-label="主导航">
        {navItems.map((item) => (
          <button className={page === item.page ? "active" : ""} key={item.page} type="button" onClick={() => onNavigate(item.page)}>
            {item.label}
          </button>
        ))}
      </nav>

      <div className="auth-status">
        {user ? (
          <>
            <span>{user.username}</span>
            <button type="button" onClick={onLogout}>退出</button>
          </>
        ) : (
          <button type="button" onClick={() => onNavigate("auth")}>登录 / 注册</button>
        )}
      </div>
    </header>
  );
}

function Ticker() {
  return (
    <section className="ticker" aria-label="实时行情">
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
  return (
    <div className="page-flow">
      <section className="hero-section">
        <div className="hero-copy">
          <span className="eyebrow">AI Trading Console</span>
          <h1>看清仓位，再做决定。</h1>
          <p>行情很吵。账户要稳。</p>
          <div className="hero-actions">
            <button type="button" onClick={() => onNavigate("trade")}>{icon("solar:wallet-money-bold-duotone")}进入模拟交易</button>
            <button className="ghost" type="button" onClick={() => onNavigate("advisor")}>{icon("solar:chat-round-like-bold-duotone")}问 AI 顾问</button>
          </div>
        </div>

        <div className="hero-board">
          <div className="risk-score"><span>{mockAccount.riskScore}</span><small>风险分</small></div>
          <div><strong>{formatMoney(mockAccount.totalAsset)}</strong><p>总资产</p></div>
          <div className="hero-note"><b>{advisorMessage.title}</b><p>{advisorMessage.risk}</p></div>
        </div>
      </section>

      <section className="section-block snap-block">
        <SectionTitle eyebrow="Scroll 01" title="市场先落地" action="看资讯" onClick={() => onNavigate("news")} />
        <div className="index-grid">
          {marketIndexes.map((item, index) => (
            <article className="index-card" key={item.code}>
              <div><strong>{item.name}</strong><span>{item.code}</span></div>
              <Sparkline points={[12 + index, 18, 14 + index, 24, 22 + index, 31, 28 + index]} color={item.trend === "up" ? "#72f5a4" : "#ff8f70"} />
              <b className={item.trend}>{item.change}</b>
            </article>
          ))}
        </div>
      </section>

      <section className="section-block snap-block">
        <SectionTitle eyebrow="Scroll 02" title="四个入口，分开看" />
        <div className="feature-grid">
          <FeatureCard iconName="solar:wallet-money-bold-duotone" title="模拟交易" text="开户、买卖、撤单。" buttonText="打开交易页" onClick={() => onNavigate("trade")} />
          <FeatureCard iconName="solar:users-group-rounded-bold-duotone" title="社区" text="发帖、点赞、评论。" buttonText="打开社区页" onClick={() => onNavigate("community")} />
          <FeatureCard iconName="solar:chat-round-like-bold-duotone" title="AI 顾问" text="先挑错，再建议。" buttonText="打开顾问页" onClick={() => onNavigate("advisor")} />
          <FeatureCard iconName="solar:document-text-bold-duotone" title="资讯" text="自选股和市场消息。" buttonText="打开资讯页" onClick={() => onNavigate("news")} />
        </div>
      </section>

      <section className="section-block snap-block">
        <SectionTitle eyebrow="Scroll 03" title="账户摘要" action="去交易页" onClick={() => onNavigate("trade")} />
        <div className="metric-grid">
          <MetricCard label="总资产" value={formatMoney(mockAccount.totalAsset)} hint="模拟账户净值" icon={icon("solar:wallet-money-bold-duotone")} tone="cyan" />
          <MetricCard label="可用现金" value={formatMoney(mockAccount.availableCash)} hint={`冻结 ${formatMoney(mockAccount.frozenCash)}`} icon={icon("solar:cash-out-bold-duotone")} tone="green" />
          <MetricCard label="风险分" value={`${mockAccount.riskScore}/100`} hint={mockAccount.riskLabel} icon={icon("solar:shield-warning-bold-duotone")} tone="amber" />
        </div>
      </section>
    </div>
  );
}

function AuthPage({ onLogin }: { onLogin: (user: LoginVO) => void }) {
  const [mode, setMode] = useState<"login" | "register">("login");
  const [notice, setNotice] = useState<Notice>({ type: "info", text: "登录后会自动保存 token，用于交易、发帖、点赞和关注。" });
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
        setNotice({ type: "success", text: "登录成功，token 已保存。" });
      }
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageShell eyebrow="Account" title="登录 / 注册" subtitle="先拿到 token，再操作交易和社区。">
      <NoticeBar notice={notice} />
      <section className="panel auth-panel">
        <div className="segmented">
          <button className={mode === "login" ? "active" : ""} type="button" onClick={() => setMode("login")}>登录</button>
          <button className={mode === "register" ? "active" : ""} type="button" onClick={() => setMode("register")}>注册</button>
        </div>
        <form className="post-form auth-form" onSubmit={submit}>
          <label>用户名<input value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} required /></label>
          <label>密码<input type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} required /></label>
          {mode === "register" && (
            <>
              <label>昵称<input value={form.nickName} onChange={(event) => setForm({ ...form, nickName: event.target.value })} /></label>
              <label>邮箱<input value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label>
              <label>手机号<input value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} /></label>
            </>
          )}
          <button type="submit" disabled={loading}>{icon("solar:key-minimalistic-bold-duotone")}{mode === "login" ? "登录并保存 token" : "创建账号"}</button>
        </form>
      </section>
    </PageShell>
  );
}

function TradePage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  const [accountInfo, setAccountInfo] = useState<SimAccountDTO | null>(null);
  const [positionList, setPositionList] = useState<PositionDTO[]>([]);
  const [orderList, setOrderList] = useState<TradeOrderDTO[]>([]);
  const [dealList, setDealList] = useState<TradeDealDTO[]>([]);
  const [notice, setNotice] = useState<Notice>({ type: "info", text: "登录后可直接调用后端模拟交易接口。" });
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ symbol: "600519", direction: "1", price: "1485.3", quantity: "100" });

  const summary = useMemo(() => ({
    totalAsset: toNumber(accountInfo?.totalAsset, mockAccount.totalAsset),
    availableCash: toNumber(accountInfo?.availableCash, mockAccount.availableCash),
    frozenCash: toNumber(accountInfo?.frozenCash, mockAccount.frozenCash),
  }), [accountInfo]);

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

  const createAccount = async () => {
    setLoading(true);
    try {
      const nextAccount = await api.createAccount();
      setAccountInfo(nextAccount);
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

  const rows = positionList.length > 0
    ? positionList.map((position) => ({ left: position.symbol, meta: `可用 ${position.availableQuantity ?? 0}`, middle: `${position.totalQuantity ?? 0} 股`, right: formatMoney(toNumber(position.costPrice)) }))
    : mockPositions.map((position) => ({ left: position.name, meta: position.symbol, middle: `${position.quantity} 股`, right: position.risk }));

  const orderRows: OrderRow[] = orderList.length > 0
    ? orderList.map((order) => ({ left: order.symbol, meta: directionLabel(order.direction), middle: `${order.quantity} 股 / ${formatMoney(order.price)}`, right: orderStatus(order.status), id: order.id, canCancel: order.status === 1 || order.status === 2 }))
    : mockOrders.map((order) => ({ left: order.name, meta: order.side, middle: order.symbol, right: order.status }));

  const dealRows = dealList.length > 0
    ? dealList.map((deal) => ({ left: deal.symbol, meta: deal.createTime || directionLabel(deal.direction), middle: directionLabel(deal.direction), right: formatMoney(toNumber(deal.price)) }))
    : mockDeals.map((deal) => ({ left: deal.name, meta: deal.time, middle: deal.side, right: formatMoney(deal.price) }));

  return (
    <PageShell eyebrow="Simulation" title="模拟交易" subtitle="开户、买入、卖出、撤单，都在这里。">
      <NoticeBar notice={notice} />
      <div className="toolbar-row">
        <button className="page-cta" type="button" onClick={createAccount} disabled={loading}>{icon("solar:user-plus-rounded-bold-duotone")}开通模拟账户</button>
        <button className="page-cta subtle" type="button" onClick={refreshTrade} disabled={loading}>{icon("solar:refresh-bold-duotone")}刷新交易数据</button>
      </div>
      <div className="metric-grid">
        <MetricCard label="总资产" value={formatMoney(summary.totalAsset)} hint="后端账户总资产" icon={icon("solar:wallet-money-bold-duotone")} />
        <MetricCard label="可用现金" value={formatMoney(summary.availableCash)} hint={`冻结 ${formatMoney(summary.frozenCash)}`} icon={icon("solar:cash-out-bold-duotone")} />
        <MetricCard label="订单数" value={`${orderList.length || mockOrders.length}`} hint="当前委托记录" icon={icon("solar:clipboard-list-bold-duotone")} />
      </div>
      <div className="two-column trade-layout">
        <section className="panel">
          <PanelTitle eyebrow="Order Ticket" title="买卖股票" iconName="solar:cart-large-2-bold-duotone" />
          <form className="trade-form" onSubmit={submitOrder}>
            <label>股票代码<input value={form.symbol} onChange={(event) => setForm({ ...form, symbol: event.target.value })} placeholder="600519" /></label>
            <label>方向<select value={form.direction} onChange={(event) => setForm({ ...form, direction: event.target.value })}><option value="1">买入</option><option value="2">卖出</option></select></label>
            <label>委托价格<input type="number" min="0" step="0.01" value={form.price} onChange={(event) => setForm({ ...form, price: event.target.value })} /></label>
            <label>数量<input type="number" min="1" step="1" value={form.quantity} onChange={(event) => setForm({ ...form, quantity: event.target.value })} /></label>
            <button type="submit" disabled={loading}>{icon(form.direction === "1" ? "solar:round-arrow-up-bold-duotone" : "solar:round-arrow-down-bold-duotone")}提交委托</button>
          </form>
        </section>
        <section className="panel"><PanelTitle eyebrow="Portfolio" title="持仓" iconName="solar:pie-chart-2-bold-duotone" /><DataRows rows={rows} /></section>
      </div>
      <div className="two-column">
        <section className="panel">
          <PanelTitle eyebrow="Orders" title="订单流" iconName="solar:clipboard-list-bold-duotone" />
          <div className="data-rows">
            {orderRows.map((row) => (
              <div className="data-row action-row" key={`${row.left}-${row.meta}-${row.right}-${row.id || ""}`}>
                <div><strong>{row.left}</strong><span>{row.meta}</span></div>
                <span>{row.middle}</span>
                <b>{row.right}</b>
                {row.canCancel && <button type="button" onClick={() => cancelOrder(row.id)} disabled={loading}>撤单</button>}
              </div>
            ))}
          </div>
        </section>
        <section className="panel"><PanelTitle eyebrow="Deals" title="成交回放" iconName="solar:rewind-back-bold-duotone" /><DataRows rows={dealRows} /></section>
      </div>
      <button className="page-cta" type="button" onClick={() => onNavigate("advisor")}>{icon("solar:chat-round-like-bold-duotone")}让 AI 看一下风险</button>
    </PageShell>
  );
}

function CommunityPage({ onNavigate, user }: { onNavigate: (page: Page) => void; user: LoginVO | null }) {
  const [blogs, setBlogs] = useState<BlogDTO[]>([]);
  const [notice, setNotice] = useState<Notice>({ type: "info", text: "热门帖子可直接查看；发帖、点赞、评论和关注需要登录。" });
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ title: "", stockId: "", context: "" });
  const [openBlogId, setOpenBlogId] = useState<number | null>(null);
  const [commentText, setCommentText] = useState("");
  const [comments, setComments] = useState<Record<number, BlogCommentDTO[]>>({});
  const [followedUsers, setFollowedUsers] = useState<Record<number, boolean>>({});

  const fallbackBlogs: BlogDTO[] = communityPosts.map((post) => ({ id: post.id, title: post.title, context: post.context, name: post.author, liked: post.heat, isLike: false }));
  const visibleBlogs = blogs.length > 0 ? blogs : fallbackBlogs;

  const refreshBlogs = async () => {
    setLoading(true);
    try {
      const nextBlogs = await api.getHotBlogs();
      setBlogs(nextBlogs || []);
      setNotice({ type: "success", text: "社区热帖已刷新。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshBlogs();
  }, []);

  const normalizeComments = (data: Awaited<ReturnType<typeof api.getComments>>) => {
    if (Array.isArray(data)) return data;
    return data.records || data.list || [];
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

  const submitBlog = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      await api.createBlog({ title: form.title.trim(), stockId: form.stockId.trim(), context: form.context.trim() });
      setForm({ title: "", stockId: "", context: "" });
      setNotice({ type: "success", text: "帖子已发布。" });
      await refreshBlogs();
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  const likeBlog = async (id?: number) => {
    if (!id) return;
    setLoading(true);
    try {
      await api.likeBlog(id);
      setBlogs((current) => current.map((blog) => (blog.id === id ? { ...blog, liked: (blog.liked || 0) + 1, isLike: true } : blog)));
      setNotice({ type: "success", text: "已点赞。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  const submitComment = async (blogId?: number) => {
    if (!blogId || !commentText.trim()) return;
    setLoading(true);
    try {
      await api.addComment({ blogId, content: commentText.trim() });
      setCommentText("");
      const data = await api.getComments(blogId);
      setComments((current) => ({ ...current, [blogId]: normalizeComments(data) }));
      setNotice({ type: "success", text: "评论已发布。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
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
      setNotice({ type: "success", text: next ? "已关注用户。" : "已取消关注。" });
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    }
  };

  return (
    <PageShell eyebrow="Community" title="社区" subtitle="发帖、点赞、评论、关注。">
      <NoticeBar notice={notice} />
      <div className="community-layout">
        <section className="panel">
          <PanelTitle eyebrow="New Post" title="发布观点" iconName="solar:pen-new-square-bold-duotone" />
          <form className="post-form" onSubmit={submitBlog}>
            <label>标题<input value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} placeholder="比如：白酒反弹别上头" required /></label>
            <label>关联股票<input value={form.stockId} onChange={(event) => setForm({ ...form, stockId: event.target.value })} placeholder="600519，可不填" /></label>
            <label>正文<textarea value={form.context} onChange={(event) => setForm({ ...form, context: event.target.value })} placeholder="说清楚你的判断。" required /></label>
            <button type="submit" disabled={loading}>{icon("solar:plain-2-bold-duotone")}发布帖子</button>
          </form>
        </section>

        <section className="panel">
          <PanelTitle eyebrow="Hot Posts" title="热帖" iconName="solar:users-group-rounded-bold-duotone" />
          <div className="post-list">
            {visibleBlogs.map((post) => {
              const blogComments = post.id ? comments[post.id] || [] : [];
              return (
                <article className="post-card rich-post" key={post.id || post.title}>
                  <div>
                    <strong>{post.title}</strong>
                    <span>{post.name || `用户 ${post.userId || ""}`}</span>
                    <p>{post.context}</p>
                    <div className="inline-actions">
                      <button type="button" onClick={() => likeBlog(post.id)} disabled={loading || !post.id}>{icon(post.isLike ? "solar:heart-bold" : "solar:heart-bold-duotone")}{post.liked || 0}</button>
                      <button type="button" onClick={() => loadComments(post.id)} disabled={!post.id}>{icon("solar:chat-round-dots-bold-duotone")}评论</button>
                      <button type="button" onClick={() => followUser(post.userId)} disabled={!post.userId || user?.id === post.userId}>
                        {icon("solar:user-check-rounded-bold-duotone")}
                        {post.userId && followedUsers[post.userId] ? "取消关注" : "关注"}
                      </button>
                    </div>
                    {openBlogId === post.id && (
                      <div className="comment-box">
                        <div className="comment-composer">
                          <input value={commentText} onChange={(event) => setCommentText(event.target.value)} placeholder="写一句评论" />
                          <button type="button" onClick={() => submitComment(post.id)} disabled={loading}>发送</button>
                        </div>
                        {blogComments.map((comment) => (
                          <div className="comment-line" key={comment.id}>
                            <div><strong>{comment.user?.nickname || "匿名用户"}</strong><p>{comment.content}</p></div>
                            <button type="button" onClick={() => likeComment(post.id!, comment.id)}>{icon("solar:heart-bold-duotone")}{comment.liked || 0}</button>
                            <button type="button" onClick={() => deleteComment(post.id!, comment.id)}>删除</button>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </article>
              );
            })}
          </div>
        </section>
      </div>

      <section className="panel accent-panel">
        <PanelTitle eyebrow="Watch" title="社区提醒" iconName="solar:bell-bing-bold-duotone" />
        <h2>别只看热闹。</h2>
        <p>热帖能参考。</p>
        <p>仓位要自己扛。</p>
        <button type="button" onClick={() => onNavigate("trade")}>回交易页</button>
      </section>
    </PageShell>
  );
}

function AdvisorPage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  return (
    <PageShell eyebrow="Advisor" title="AI 顾问" subtitle="先找问题，再给动作。">
      <div className="advisor-layout">
        <aside className="panel">
          <PanelTitle eyebrow="Trading Profile" title="交易画像" iconName="solar:user-id-bold-duotone" />
          {advisorTasks.map((task) => (
            <div className="profile-row" key={task.label}><div><strong>{task.label}</strong><span>{task.value}</span></div><div className="heat-bar"><i style={{ width: `${task.level}%` }} /></div></div>
          ))}
        </aside>
        <section className="panel chat-panel">
          <PanelTitle eyebrow="Conversation" title="顾问对话" iconName="solar:chat-round-like-bold-duotone" />
          <div className="advisor-avatar-large">{icon("solar:chat-round-like-bold-duotone")}</div>
          <div className="chat-stream">
            {advisorChat.map((message, index) => (
              <div className={`chat-bubble ${message.role}`} key={`${message.role}-${index}`}><span>{message.role === "user" ? "你" : "AI 顾问"}</span><p>{message.text}</p></div>
            ))}
          </div>
          <div className="chat-composer"><input value="下一步先看啥？" readOnly aria-label="顾问问题输入框" /><button type="button">{icon("solar:plain-2-bold-duotone")}发送</button></div>
        </section>
        <aside className="panel">
          <PanelTitle eyebrow="Action Board" title="观察清单" iconName="solar:checklist-minimalistic-bold-duotone" />
          {advisorMessage.suggestions.map((suggestion, index) => (<div className="action-card" key={suggestion}><b>{index + 1}</b><p>{suggestion}</p></div>))}
          <button className="page-cta full" type="button" onClick={() => onNavigate("trade")}>去交易页处理</button>
        </aside>
      </div>
    </PageShell>
  );
}

function NewsPage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  const [optionalStocks, setOptionalStocks] = useState<StockFollowDTO[]>([]);
  const [symbol, setSymbol] = useState("600519");
  const [notice, setNotice] = useState<Notice>({ type: "info", text: "登录后可管理自选股。" });
  const [loading, setLoading] = useState(false);

  const refreshOptional = async () => {
    setLoading(true);
    try {
      const list = await api.getOptionalStocks();
      setOptionalStocks(list || []);
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
      setNotice({ type: "success", text: "已添加自选股。" });
      await refreshOptional();
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
      setNotice({ type: "success", text: "已删除自选股。" });
      await refreshOptional();
    } catch (error) {
      setNotice({ type: "error", text: getErrorText(error) });
    } finally {
      setLoading(false);
    }
  };

  const visibleWatchlist = optionalStocks.length > 0 ? optionalStocks : watchlist.map((stock) => ({ symbol: stock.symbol, name: stock.name, price: Number(stock.price.replace(/,/g, "")), changePercent: Number(stock.change.replace("%", "")) }));

  return (
    <PageShell eyebrow="Intelligence" title="市场资讯" subtitle="资讯流和自选股管理。">
      <NoticeBar notice={notice} />
      <div className="two-column">
        <section className="panel">
          <PanelTitle eyebrow="Live News" title="资讯流" iconName="solar:document-text-bold-duotone" />
          {marketNews.map((news) => (<div className="news-line" key={news}><span /><p>{news}</p></div>))}
        </section>
        <section className="panel">
          <PanelTitle eyebrow="Watch List" title="自选股管理" iconName="solar:radar-2-bold-duotone" />
          <form className="optional-form" onSubmit={addOptional}>
            <input value={symbol} onChange={(event) => setSymbol(event.target.value)} placeholder="输入股票代码" />
            <button type="submit" disabled={loading}>添加</button>
            <button type="button" onClick={refreshOptional} disabled={loading}>刷新</button>
          </form>
          {visibleWatchlist.map((stock) => (
            <div className="heat-row optional-row" key={stock.symbol}>
              <div><strong>{stock.name || stock.symbol}</strong><span>{stock.symbol}</span></div>
              <span>{formatMoney(toNumber(stock.price))}</span>
              <em>{toNumber(stock.changePercent).toFixed(2)}%</em>
              <button type="button" onClick={() => removeOptional(stock.symbol)} disabled={loading}>删除</button>
            </div>
          ))}
        </section>
      </div>
      <button className="page-cta" type="button" onClick={() => onNavigate("advisor")}>让 AI 总结风险</button>
    </PageShell>
  );
}

function PageShell({ eyebrow, title, subtitle, children }: { eyebrow: string; title: string; subtitle: string; children: React.ReactNode }) {
  return <div className="page-shell"><section className="page-hero"><span className="eyebrow">{eyebrow}</span><h1>{title}</h1><p>{subtitle}</p></section>{children}</div>;
}

function SectionTitle({ eyebrow, title, action, onClick }: { eyebrow: string; title: string; action?: string; onClick?: () => void }) {
  return <div className="section-title"><div><span className="eyebrow">{eyebrow}</span><h2>{title}</h2></div>{action && <button type="button" onClick={onClick}>{action}{icon("solar:arrow-right-up-bold-duotone")}</button>}</div>;
}

function FeatureCard({ iconName, title, text, buttonText, onClick }: { iconName: string; title: string; text: string; buttonText: string; onClick: () => void }) {
  return <article className="feature-card"><span>{icon(iconName)}</span><h3>{title}</h3><p>{text}</p><button type="button" onClick={onClick}>{buttonText}</button></article>;
}

function PanelTitle({ eyebrow, title, iconName }: { eyebrow: string; title: string; iconName: string }) {
  return <div className="panel-heading"><div><p>{eyebrow}</p><h2>{title}</h2></div><Icon icon={iconName} aria-hidden="true" /></div>;
}

function DataRows({ rows }: { rows: Array<{ left: string; meta: string; middle: string; right: string }> }) {
  return <div className="data-rows">{rows.map((row) => <div className="data-row" key={`${row.left}-${row.meta}-${row.right}`}><div><strong>{row.left}</strong><span>{row.meta}</span></div><span>{row.middle}</span><b>{row.right}</b></div>)}</div>;
}

function NoticeBar({ notice }: { notice: Notice }) {
  return <div className={`notice-bar ${notice.type}`}>{notice.text}</div>;
}
