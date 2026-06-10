export type ApiResult<T = unknown> = {
  code: number;
  msg: string;
  data: T;
};

export type LoginVO = {
  token: string;
  id: number;
  username: string;
  userType?: number;
  identityDesc?: string;
  state?: number;
};

export type RegisterPayload = {
  username: string;
  password: string;
  nickName?: string;
  email?: string;
  phone?: string;
  sex?: number;
};

export type SimAccountDTO = {
  id?: number;
  totalCash?: number;
  availableCash?: number;
  frozenCash?: number;
  totalAsset?: number;
};

export type PositionDTO = {
  id?: number;
  symbol: string;
  totalQuantity?: number;
  availableQuantity?: number;
  frozenQuantity?: number;
  costPrice?: number;
  profitLoss?: number;
};

export type TradeOrderDTO = {
  id?: number;
  orderNo?: string;
  symbol: string;
  direction: 1 | 2;
  price: number;
  quantity: number;
  tradedQuantity?: number;
  status?: number;
  createTime?: string;
};

export type TradeDealDTO = {
  id?: number;
  symbol: string;
  direction?: 1 | 2;
  price?: number;
  quantity?: number;
  createTime?: string;
};

export type BlogDTO = {
  id?: number;
  userId?: number;
  name?: string;
  title: string;
  context: string;
  stockId?: string;
  images?: string;
  liked?: number;
  comments?: number;
  isLike?: boolean;
  createTime?: string;
};

export type BlogCommentDTO = {
  id: number;
  user?: {
    id?: number;
    nickname?: string;
    avatar?: string;
  };
  parentId?: number;
  content: string;
  liked?: number;
  isLiked?: number;
  createTime?: string;
  children?: BlogCommentDTO[];
};

export type PageResult<T> = {
  total?: number;
  records?: T[];
  list?: T[];
};

export type StockFollowDTO = {
  stockId?: number;
  symbol: string;
  name?: string;
  price?: number;
  changePercent?: number;
  volume?: number;
};

export type StockDocDTO = {
  id?: number;
  symbol: string;
  name?: string;
  industry?: string;
  marketType?: string;
};

export type RealtimeQuoteDTO = {
  code: string;
  name?: string;
  price?: number;
  prevClose?: number;
  open?: number;
  volume?: number;
  change?: number;
  changePercent?: number;
};

export type StockNewsDTO = {
  keyword?: string;
  title: string;
  content?: string;
  publishTime?: string;
  source?: string;
  url?: string;
};

export type AgentResponse<T = unknown> = {
  code: string;
  info: string;
  data: T;
};

export type AdvisorChatPayload = {
  agentId: string;
  userId: string;
  sessionId?: string;
  message: string;
};

export type AdvisorChatDTO = {
  content: string;
};

export type AdvisorSessionDTO = {
  sessionId: string;
};

export class ApiError extends Error {
  status?: number;

  constructor(message: string, status?: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

const getToken = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("lghj_token") ||
  localStorage.getItem("userToken") ||
  "";

async function request<T>(path: string, options: RequestInit = {}) {
  const token = getToken();
  const headers = new Headers(options.headers);

  if (token) {
    headers.set("token", token);
  }

  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(path, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    throw new ApiError("需要先登录。请先登录或把 token 存到 localStorage.token。", 401);
  }

  if (!response.ok) {
    throw new ApiError(`接口请求失败：${response.status}`, response.status);
  }

  const result = (await response.json()) as ApiResult<T>;
  if (result.code !== 200) {
    throw new ApiError(result.msg || "接口返回失败");
  }

  return result.data;
}

async function requestAgent<T>(path: string, options: RequestInit = {}) {
  const headers = new Headers(options.headers);

  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(path, {
    ...options,
    headers,
  });

  if (!response.ok) {
    throw new ApiError(`AI 服务请求失败：${response.status}`, response.status);
  }

  const result = (await response.json()) as AgentResponse<T>;
  if (result.code !== "0000") {
    throw new ApiError(result.info || "AI 服务返回失败");
  }

  return result.data;
}

const params = (values: Record<string, string | number | boolean | undefined>) => {
  const search = new URLSearchParams();
  Object.entries(values).forEach(([key, value]) => {
    if (value !== undefined && value !== "") search.set(key, String(value));
  });
  return search.toString();
};

export const api = {
  login: (payload: { username: string; password: string }) =>
    request<LoginVO>("/api/login", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  register: (payload: RegisterPayload) =>
    request("/api/register", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  createAccount: () => request<SimAccountDTO>("/api/user/account/create", { method: "POST" }),
  getAccount: () => request<SimAccountDTO>("/api/user/account/query_info"),
  getPositions: () => request<PositionDTO[]>("/api/user/account/query_positions"),
  createOrder: (payload: { symbol: string; direction: 1 | 2; price: number; quantity: number }) =>
    request<TradeOrderDTO>(`/api/user/trade/order?${params(payload)}`, { method: "POST" }),
  cancelOrder: (orderId: number) => request(`/api/user/trade/cancel?${params({ orderId })}`, { method: "POST" }),
  getOrders: () => request<TradeOrderDTO[]>("/api/user/trade/query_orders"),
  getDeals: () => request<TradeDealDTO[]>("/api/user/trade/query_deals"),
  getHotBlogs: () => request<BlogDTO[]>("/api/user/blog/query/hot?current=1&size=20"),
  createBlog: (payload: Pick<BlogDTO, "title" | "context" | "stockId">) =>
    request("/api/user/blog", {
      method: "POST",
      body: JSON.stringify({ ...payload, liked: 0, comments: 0, status: 1 }),
    }),
  likeBlog: (id: number) => request(`/api/user/blog/like/${id}`, { method: "PUT" }),
  followUser: (id: number, isFollow: boolean) => request(`/api/user/follow/${id}/${isFollow}`, { method: "PUT" }),
  getComments: (blogId: number) =>
    request<PageResult<BlogCommentDTO> | BlogCommentDTO[]>(`/api/user/blog/comments/list?${params({ blogId, pageNum: 1, pageSize: 20 })}`),
  addComment: (payload: { blogId: number; parentId?: number; content: string }) =>
    request("/api/user/blog/comments/add", {
      method: "POST",
      body: JSON.stringify({ parentId: 0, ...payload }),
    }),
  likeComment: (commentId: number) => request(`/api/user/blog/comments/like/${commentId}`, { method: "POST" }),
  deleteComment: (commentId: number) => request(`/api/user/blog/comments/delete/${commentId}`, { method: "DELETE" }),
  getOptionalStocks: () => request<StockFollowDTO[]>("/api/user/optional/list"),
  addOptionalStock: (symbol: string) => request(`/api/user/optional/add?${params({ symbol })}`, { method: "POST" }),
  removeOptionalStock: (symbol: string) => request(`/api/user/optional/remove?${params({ symbol })}`, { method: "POST" }),
  searchStocks: (keyword: string) => request<StockDocDTO[]>(`/api/user/stock/search?${params({ keyword })}`),
  getRealtimeQuote: (payload: { market: string; code: string }) =>
    request<RealtimeQuoteDTO>(`/api/user/realtime/quote?${params(payload)}`),
  getMinuteData: (payload: { market: string; code: string }) =>
    request<Array<Record<string, unknown>>>(`/api/user/realtime/minute?${params(payload)}`),
  getStockNews: (symbol: string, recentN = 10) =>
    request<StockNewsDTO[]>(`/api/user/realtime/news?${params({ symbol, recentN })}`),
  chatAdvisor: (payload: AdvisorChatPayload) =>
    requestAgent<AdvisorChatDTO>("/api/v1/chat", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  createAdvisorSession: (payload: { agentId: string; userId: string }) =>
    requestAgent<AdvisorSessionDTO>("/api/v1/create_session", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
};
