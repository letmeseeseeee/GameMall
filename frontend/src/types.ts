export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  page: number;
  size: number;
}

export interface UserSession {
  token: string;
  userId: number;
  username: string;
  role: "ADMIN" | "USER";
}

export interface Category {
  id: number;
  name: string;
  code: string;
  description: string;
  sortOrder: number;
  gameCount: number;
}

export interface GameTag {
  id: number;
  name: string;
  groupName: string;
}

export interface GameSummary {
  id: number;
  categoryId: number;
  categoryName: string;
  title: string;
  developer: string;
  price: number;
  stock: number;
  soldCount: number;
  coverUrl?: string;
  tags: GameTag[];
}

export interface Game extends GameSummary {
  publisher?: string;
  description?: string;
  status?: number;
  releaseDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CartLine {
  id: number;
  gameId: number;
  title: string;
  coverUrl?: string;
  price: number;
  stock: number;
  quantity: number;
  subtotal: number;
}

export interface Order {
  id: number;
  orderNo: string;
  totalAmount: number;
  status: number;
  paymentStatus: number;
  paidAt?: string;
  expireAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  id: number;
  orderId: number;
  gameId: number;
  gameTitle: string;
  price: number;
  quantity: number;
  subtotal: number;
}

export interface OrderDetail {
  order: Order;
  items: OrderItem[];
}

export interface GamePayload {
  categoryId: number;
  title: string;
  developer: string;
  publisher: string;
  price: number;
  stock: number;
  coverUrl: string;
  description: string;
  releaseDate: string | null;
  tagIds: number[];
}
