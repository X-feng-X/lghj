import {
  Activity,
  ArrowUpRight,
  Bot,
  BrainCircuit,
  ChartCandlestick,
  CircleDollarSign,
  Gauge,
  MessageSquareText,
  Radar,
  ShieldAlert,
  Sparkles,
  Wallet,
} from "lucide-react";
import { MetricCard } from "./components/MetricCard";
import { Sparkline } from "./components/Sparkline";
import {
  account,
  advisorMessage,
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

export default function App() {
  return (
    <main className="app-shell">
      <div className="grid-backdrop" />
      <div className="scan-backdrop" />

      <header className="topbar">
        <div className="brand">
          <div className="brand-mark">
            <ChartCandlestick size={24} />
          </div>
          <div>
            <p>LiangGu HuaJin</p>
            <h1>量股化金智能交易驾驶舱</h1>
          </div>
        </div>
        <div className="market-status">
          <span className="pulse-dot" />
          A 股模拟盘运行中
        </div>
        <button className="ai-entry" type="button">
          <Bot size={18} />
          AI 投资顾问
        </button>
      </header>

      <section className="ticker" aria-label="实时行情">
        <div className="ticker-track">
          {tickerItems.map((item, index) => (
            <span key={`${item.id}-${index}`} className={item.change.startsWith("+") ? "up" : "down"}>
              {item.name} {item.quote} {item.change}
            </span>
          ))}
        </div>
      </section>

      <section className="dashboard-grid">
        <aside className="panel market-panel">
          <div className="panel-heading">
            <div>
              <p>Market Pulse</p>
              <h2>行情热区</h2>
            </div>
            <Activity size={19} />
          </div>

          <div className="index-list">
            {marketIndexes.map((item, index) => (
              <div className="index-row" key={item.code}>
                <div>
                  <strong>{item.name}</strong>
                  <span>{item.code}</span>
                </div>
                <Sparkline
                  points={[12 + index, 18, 14 + index, 24, 22 + index, 31, 28 + index]}
                  color={item.trend === "up" ? "#48f5d2" : "#ff5b7f"}
                />
                <b className={item.trend}>{item.change}</b>
              </div>
            ))}
          </div>

          <div className="watchlist">
            <h3>自选股雷达</h3>
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
          </div>
        </aside>

        <section className="center-stack">
          <div className="metrics-grid">
            <MetricCard label="总资产" value={formatMoney(account.totalAsset)} hint="模拟账户净值" icon={<Wallet />} tone="cyan" />
            <MetricCard
              label="可用现金"
              value={formatMoney(account.availableCash)}
              hint={`冻结 ${formatMoney(account.frozenCash)}`}
              icon={<CircleDollarSign />}
              tone="green"
            />
            <MetricCard label="风险评分" value={`${account.riskScore}/100`} hint={account.riskLabel} icon={<Gauge />} tone="amber" />
          </div>

          <div className="panel account-panel">
            <div className="panel-heading">
              <div>
                <p>Portfolio Core</p>
                <h2>模拟交易总览</h2>
              </div>
              <ShieldAlert size={19} />
            </div>

            <div className="asset-visual">
              <div className="asset-ring">
                <span>{account.riskScore}</span>
                <small>Risk</small>
              </div>
              <div className="asset-copy">
                <h3>单一持仓占比 100%</h3>
                <p>系统识别到集中持仓与买入偏好，建议优先检查异常挂单并建立止盈止损观察线。</p>
              </div>
            </div>

            <div className="position-table">
              {positions.map((position) => (
                <div className="position-row" key={position.symbol}>
                  <div>
                    <strong>{position.name}</strong>
                    <span>{position.symbol}</span>
                  </div>
                  <span>{position.quantity} 股</span>
                  <span>{formatMoney(position.cost)}</span>
                  <b>{position.risk}</b>
                </div>
              ))}
            </div>
          </div>

          <div className="split-panels">
            <div className="panel compact-panel">
              <h2>订单流</h2>
              {orders.map((order) => (
                <div className="order-line" key={`${order.symbol}-${order.status}`}>
                  <span>{order.side}</span>
                  <strong>{order.name}</strong>
                  <em>{order.status}</em>
                </div>
              ))}
            </div>
            <div className="panel compact-panel">
              <h2>成交回放</h2>
              {deals.map((deal) => (
                <div className="deal-line" key={`${deal.symbol}-${deal.time}`}>
                  <span>{deal.time}</span>
                  <strong>
                    {deal.side} {deal.name}
                  </strong>
                  <em>{formatMoney(deal.price)}</em>
                </div>
              ))}
            </div>
          </div>
        </section>

        <aside className="panel advisor-panel">
          <div className="advisor-header">
            <div className="advisor-avatar">
              <BrainCircuit size={26} />
            </div>
            <div>
              <p>Supervisor Agent</p>
              <h2>智能投资顾问</h2>
            </div>
            <Sparkles className="spark-icon" size={18} />
          </div>

          <div className="advisor-message">
            <span>{advisorMessage.title}</span>
            <p>{advisorMessage.risk}</p>
          </div>

          <div className="suggestion-list">
            {advisorMessage.suggestions.map((suggestion, index) => (
              <div className="suggestion" key={suggestion}>
                <b>0{index + 1}</b>
                <p>{suggestion}</p>
              </div>
            ))}
          </div>

          <button className="ask-button" type="button">
            <MessageSquareText size={17} />
            进入顾问对话
            <ArrowUpRight size={16} />
          </button>
        </aside>
      </section>

      <section className="bottom-grid">
        <div className="panel community-panel">
          <div className="panel-heading">
            <div>
              <p>Community Signal</p>
              <h2>社区热帖</h2>
            </div>
            <MessageSquareText size={19} />
          </div>
          {communityPosts.map((post) => (
            <article className="post-card" key={post.title}>
              <span>{post.author}</span>
              <strong>{post.title}</strong>
              <em>{post.heat} 热度</em>
            </article>
          ))}
        </div>

        <div className="panel news-panel">
          <div className="panel-heading">
            <div>
              <p>Live Intelligence</p>
              <h2>实时资讯</h2>
            </div>
            <Radar size={19} />
          </div>
          {marketNews.map((news) => (
            <div className="news-line" key={news}>
              <span />
              <p>{news}</p>
            </div>
          ))}
        </div>

        <div className="panel risk-panel">
          <div className="panel-heading">
            <div>
              <p>Risk Matrix</p>
              <h2>风险雷达</h2>
            </div>
            <ShieldAlert size={19} />
          </div>
          <div className="radar-map">
            <i className="axis a" />
            <i className="axis b" />
            <i className="axis c" />
            <div className="radar-core" />
          </div>
          <div className="risk-tags">
            <span>集中持仓</span>
            <span>买入偏好</span>
            <span>异常挂单</span>
          </div>
        </div>
      </section>
    </main>
  );
}
