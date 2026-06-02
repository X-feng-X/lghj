import { useState } from "react";
import { Icon } from "@iconify/react";
import { MetricCard } from "./components/MetricCard";
import { Sparkline } from "./components/Sparkline";
import {
  account,
  advisorChat,
  advisorMessage,
  advisorTasks,
  communityPosts,
  deals,
  marketIndexes,
  marketNews,
  orders,
  positions,
  tickerItems,
  watchlist,
} from "./data/mock";

const formatMoney = (value: number) =>
  new Intl.NumberFormat("zh-CN", { style: "currency", currency: "CNY", maximumFractionDigits: 0 }).format(value);

type Page = "home" | "trade" | "community" | "advisor" | "news";

const icon = (name: string) => <Icon icon={name} aria-hidden="true" />;

export default function App() {
  const [page, setPage] = useState<Page>("home");

  const openPage = (nextPage: Page) => {
    setPage(nextPage);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  return (
    <main className="app-shell">
      <div className="soft-noise" />
      <Topbar page={page} onNavigate={openPage} />
      <Ticker />
      {page === "home" && <HomePage onNavigate={openPage} />}
      {page === "trade" && <TradePage onNavigate={openPage} />}
      {page === "community" && <CommunityPage onNavigate={openPage} />}
      {page === "advisor" && <AdvisorPage onNavigate={openPage} />}
      {page === "news" && <NewsPage onNavigate={openPage} />}
    </main>
  );
}

function Topbar({ page, onNavigate }: { page: Page; onNavigate: (page: Page) => void }) {
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

      <div className="market-status">
        <span className="pulse-dot" />
        模拟盘在线
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
            <button type="button" onClick={() => onNavigate("trade")}>
              {icon("solar:wallet-money-bold-duotone")}
              进入模拟交易
            </button>
            <button className="ghost" type="button" onClick={() => onNavigate("advisor")}>
              {icon("solar:chat-round-money-bold-duotone")}
              问 AI 顾问
            </button>
          </div>
        </div>

        <div className="hero-board">
          <div className="risk-score">
            <span>{account.riskScore}</span>
            <small>风险分</small>
          </div>
          <div>
            <strong>{formatMoney(account.totalAsset)}</strong>
            <p>总资产</p>
          </div>
          <div className="hero-note">
            <b>{advisorMessage.title}</b>
            <p>{advisorMessage.risk}</p>
          </div>
        </div>
      </section>

      <section className="section-block snap-block">
        <SectionTitle eyebrow="Scroll 01" title="市场先落地" action="看资讯" onClick={() => onNavigate("news")} />
        <div className="index-grid">
          {marketIndexes.map((item, index) => (
            <article className="index-card" key={item.code}>
              <div>
                <strong>{item.name}</strong>
                <span>{item.code}</span>
              </div>
              <Sparkline points={[12 + index, 18, 14 + index, 24, 22 + index, 31, 28 + index]} color={item.trend === "up" ? "#265d3f" : "#cf6b50"} />
              <b className={item.trend}>{item.change}</b>
            </article>
          ))}
        </div>
      </section>

      <section className="section-block snap-block">
        <SectionTitle eyebrow="Scroll 02" title="四个入口，分开看" />
        <div className="feature-grid">
          <FeatureCard
            iconName="solar:wallet-money-bold-duotone"
            title="模拟交易"
            text="订单、持仓、成交。"
            buttonText="打开交易页"
            onClick={() => onNavigate("trade")}
          />
          <FeatureCard
            iconName="solar:users-group-rounded-bold-duotone"
            title="社区"
            text="热帖和观点。"
            buttonText="打开社区页"
            onClick={() => onNavigate("community")}
          />
          <FeatureCard
            iconName="solar:brain-bold-duotone"
            title="AI 顾问"
            text="先挑错，再建议。"
            buttonText="打开顾问页"
            onClick={() => onNavigate("advisor")}
          />
          <FeatureCard
            iconName="solar:document-text-bold-duotone"
            title="资讯"
            text="市场碎片汇总。"
            buttonText="打开资讯页"
            onClick={() => onNavigate("news")}
          />
        </div>
      </section>

      <section className="section-block snap-block">
        <SectionTitle eyebrow="Scroll 03" title="账户摘要" action="去交易页" onClick={() => onNavigate("trade")} />
        <div className="metric-grid">
          <MetricCard label="总资产" value={formatMoney(account.totalAsset)} hint="模拟账户净值" icon={icon("solar:wallet-money-bold-duotone")} tone="cyan" />
          <MetricCard
            label="可用现金"
            value={formatMoney(account.availableCash)}
            hint={`冻结 ${formatMoney(account.frozenCash)}`}
            icon={icon("solar:cash-out-bold-duotone")}
            tone="green"
          />
          <MetricCard label="风险分" value={`${account.riskScore}/100`} hint={account.riskLabel} icon={icon("solar:shield-warning-bold-duotone")} tone="amber" />
        </div>
      </section>
    </div>
  );
}

function TradePage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  return (
    <PageShell eyebrow="Simulation" title="模拟交易" subtitle="持仓、订单、成交，分开看。">
      <div className="metric-grid">
        <MetricCard label="总资产" value={formatMoney(account.totalAsset)} hint="模拟账户净值" icon={icon("solar:wallet-money-bold-duotone")} />
        <MetricCard label="可用现金" value={formatMoney(account.availableCash)} hint={`冻结 ${formatMoney(account.frozenCash)}`} icon={icon("solar:cash-out-bold-duotone")} />
        <MetricCard label="风险分" value={`${account.riskScore}/100`} hint={account.riskLabel} icon={icon("solar:shield-warning-bold-duotone")} />
      </div>

      <div className="two-column">
        <section className="panel tall-panel">
          <PanelTitle eyebrow="Portfolio" title="持仓" iconName="solar:pie-chart-2-bold-duotone" />
          <div className="asset-visual">
            <div className="risk-score compact">
              <span>{account.riskScore}</span>
              <small>Risk</small>
            </div>
            <div>
              <h2>仓位太集中</h2>
              <p>单票占比过高。</p>
              <p>先设退出线。</p>
            </div>
          </div>
          <DataRows
            rows={positions.map((position) => ({
              left: position.name,
              meta: position.symbol,
              middle: `${position.quantity} 股`,
              right: position.risk,
            }))}
          />
        </section>

        <section className="panel tall-panel">
          <PanelTitle eyebrow="Orders" title="订单流" iconName="solar:clipboard-list-bold-duotone" />
          <DataRows rows={orders.map((order) => ({ left: order.name, meta: order.side, middle: order.symbol, right: order.status }))} />
          <PanelTitle eyebrow="Deals" title="成交回放" iconName="solar:rewind-back-bold-duotone" />
          <DataRows rows={deals.map((deal) => ({ left: deal.name, meta: deal.time, middle: deal.side, right: formatMoney(deal.price) }))} />
        </section>
      </div>

      <button className="page-cta" type="button" onClick={() => onNavigate("advisor")}>
        {icon("solar:chat-round-money-bold-duotone")}
        让 AI 看一下风险
      </button>
    </PageShell>
  );
}

function CommunityPage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  return (
    <PageShell eyebrow="Community" title="社区" subtitle="少点口号，多看观点。">
      <div className="community-layout">
        <section className="panel">
          <PanelTitle eyebrow="Hot Posts" title="热帖" iconName="solar:users-group-rounded-bold-duotone" />
          {communityPosts.map((post) => (
            <article className="post-card" key={post.title}>
              <div>
                <strong>{post.title}</strong>
                <span>{post.author}</span>
              </div>
              <b>{post.heat}</b>
            </article>
          ))}
        </section>

        <section className="panel accent-panel">
          <PanelTitle eyebrow="Watch" title="社区提醒" iconName="solar:bell-bing-bold-duotone" />
          <h2>别只看热闹。</h2>
          <p>热帖能参考。</p>
          <p>仓位要自己扛。</p>
          <button type="button" onClick={() => onNavigate("trade")}>回交易页</button>
        </section>
      </div>
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
            <div className="profile-row" key={task.label}>
              <div>
                <strong>{task.label}</strong>
                <span>{task.value}</span>
              </div>
              <div className="heat-bar">
                <i style={{ width: `${task.level}%` }} />
              </div>
            </div>
          ))}
        </aside>

        <section className="panel chat-panel">
          <PanelTitle eyebrow="Conversation" title="顾问对话" iconName="solar:chat-round-like-bold-duotone" />
          <div className="chat-stream">
            {advisorChat.map((message, index) => (
              <div className={`chat-bubble ${message.role}`} key={`${message.role}-${index}`}>
                <span>{message.role === "user" ? "你" : "AI 顾问"}</span>
                <p>{message.text}</p>
              </div>
            ))}
          </div>
          <div className="chat-composer">
            <input value="下一步先看啥？" readOnly aria-label="顾问问题输入框" />
            <button type="button">{icon("solar:plain-2-bold-duotone")}发送</button>
          </div>
        </section>

        <aside className="panel">
          <PanelTitle eyebrow="Action Board" title="观察清单" iconName="solar:checklist-minimalistic-bold-duotone" />
          {advisorMessage.suggestions.map((suggestion, index) => (
            <div className="action-card" key={suggestion}>
              <b>{index + 1}</b>
              <p>{suggestion}</p>
            </div>
          ))}
          <button className="page-cta full" type="button" onClick={() => onNavigate("trade")}>去交易页处理</button>
        </aside>
      </div>
    </PageShell>
  );
}

function NewsPage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  return (
    <PageShell eyebrow="Intelligence" title="市场资讯" subtitle="把碎片排整齐。">
      <div className="two-column">
        <section className="panel">
          <PanelTitle eyebrow="Live News" title="资讯流" iconName="solar:document-text-bold-duotone" />
          {marketNews.map((news) => (
            <div className="news-line" key={news}>
              <span />
              <p>{news}</p>
            </div>
          ))}
        </section>

        <section className="panel">
          <PanelTitle eyebrow="Watch List" title="自选雷达" iconName="solar:radar-2-bold-duotone" />
          {watchlist.map((stock) => (
            <div className="heat-row" key={stock.symbol}>
              <div>
                <strong>{stock.name}</strong>
                <span>{stock.symbol}</span>
              </div>
              <div className="heat-bar" aria-label={`${stock.name} 热度 ${stock.heat}`}>
                <i style={{ width: `${stock.heat}%` }} />
              </div>
              <em className={stock.change.startsWith("+") ? "up" : "down"}>{stock.change}</em>
            </div>
          ))}
        </section>
      </div>
      <button className="page-cta" type="button" onClick={() => onNavigate("advisor")}>让 AI 总结风险</button>
    </PageShell>
  );
}

function PageShell({ eyebrow, title, subtitle, children }: { eyebrow: string; title: string; subtitle: string; children: React.ReactNode }) {
  return (
    <div className="page-shell">
      <section className="page-hero">
        <span className="eyebrow">{eyebrow}</span>
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </section>
      {children}
    </div>
  );
}

function SectionTitle({ eyebrow, title, action, onClick }: { eyebrow: string; title: string; action?: string; onClick?: () => void }) {
  return (
    <div className="section-title">
      <div>
        <span className="eyebrow">{eyebrow}</span>
        <h2>{title}</h2>
      </div>
      {action && (
        <button type="button" onClick={onClick}>
          {action}
          {icon("solar:arrow-right-up-bold-duotone")}
        </button>
      )}
    </div>
  );
}

function FeatureCard({
  iconName,
  title,
  text,
  buttonText,
  onClick,
}: {
  iconName: string;
  title: string;
  text: string;
  buttonText: string;
  onClick: () => void;
}) {
  return (
    <article className="feature-card">
      <span>{icon(iconName)}</span>
      <h3>{title}</h3>
      <p>{text}</p>
      <button type="button" onClick={onClick}>{buttonText}</button>
    </article>
  );
}

function PanelTitle({ eyebrow, title, iconName }: { eyebrow: string; title: string; iconName: string }) {
  return (
    <div className="panel-heading">
      <div>
        <p>{eyebrow}</p>
        <h2>{title}</h2>
      </div>
      <Icon icon={iconName} aria-hidden="true" />
    </div>
  );
}

function DataRows({ rows }: { rows: Array<{ left: string; meta: string; middle: string; right: string }> }) {
  return (
    <div className="data-rows">
      {rows.map((row) => (
        <div className="data-row" key={`${row.left}-${row.meta}-${row.right}`}>
          <div>
            <strong>{row.left}</strong>
            <span>{row.meta}</span>
          </div>
          <span>{row.middle}</span>
          <b>{row.right}</b>
        </div>
      ))}
    </div>
  );
}
