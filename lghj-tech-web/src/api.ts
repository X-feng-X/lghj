export type ApiResult<T = unknown> = {
  code: number;
  msg: string;
  data: T;
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
    throw new ApiError("需要先登录。请把登录 token 存到 localStorage.token。", 401);
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

const params = (values: Record<string, string | number | boolean>) => {
  const search = new URLSearchParams();
  Object.entries(values).forEach(([key, value]) => search.set(key, String(value)));
  return search.toString();
};

export const api = {
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
};
