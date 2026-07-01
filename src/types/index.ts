// ==================== 通用类型 ====================
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

export interface PageParams {
  page: number
  pageSize: number
}

export interface PageResult<T> {
  total: number
  records: T[]
  pages: number
  pageSize: number
}

// ==================== 用户 ====================
export interface User {
  id: number
  name: string
  phone: string
  image: string
  password?: string
  fansCounts: number
  followerCounts: number
  money: number
  address: string
  status: 0 | 1 | 2 // 0=普通用户 1=商户 2=被封禁
  shopId: number | null
  createTime: string
  updateTime: string
}

export interface UserSimple {
  id: number
  name: string
  image: string
  address: string
  fansCounts: number
  followerCounts: number
  status: number
}

// ==================== 店铺类型 ====================
export interface ShopType {
  id: number
  name: string
}

// ==================== 店铺 ====================
export interface Shop {
  id: number
  typeId: number
  typeName?: string
  name: string
  images: string
  address: string
  price: number
  comments: number
  sold: number
  score: number
  createTime: string
  updateTime: string
}

export interface ShopQuery {
  typeId?: number
  scoreRange?: 'below2' | '2to4' | '4to5' | 'noComment'
  page: number
  pageSize: number
}

// ==================== 评价 ====================
export interface Comment {
  id: number
  shopId: number
  userId: number
  userName?: string
  userImage?: string
  content: string
  score: number
  likes: number
  images: string
  isLiked?: boolean
  createTime: string
}

export interface CommentQuery {
  shopId: number
  filter?: 'all' | 'good' | 'mid' | 'bad' // 全部(按点赞)/好评(4-5)/中评(2-3)/差评(<=2)
  page: number
  pageSize: number
}

// ==================== 博客 ====================
export interface Blog {
  id: number
  userId: number
  userName?: string
  userImage?: string
  shopId: number
  shopName?: string
  likes: number
  title: string
  content: string
  images: string
  isLiked?: boolean
  createTime: string
  updateTime: string
}

export interface BlogQuery {
  type: 'recommend' | 'following'
  page: number
  pageSize: number
}

// ==================== 关注 ====================
export interface Follow {
  id: number
  userId: number
  followUserId: number
  createTime: string
}

// ==================== 优惠卷 ====================
export interface Voucher {
  id: number
  shopId: number
  value: number
  price: number // 优惠卷购买价格
  beginTime: string
  endTime: string
  stock: number
  createTime: string
  updateTime: string
}

export interface VoucherOrder {
  id: number
  userId: number
  voucherId: number
  voucherValue?: number
  status: 0 | 1 | 2 // 0=未使用 1=已使用 2=已过期
  orderTime: string
}

// ==================== 店铺订单 ====================
export interface ShopOrder {
  id: number
  shopId: number
  shopName?: string
  userId: number
  price: number
  status: 1 | 2 // 1=支付成功 2=退款
  voucherId?: number
  voucherValue?: number
  orderTime: string
}

// ==================== 管理员 ====================
export interface Admin {
  id: number
  name: string
  createTime: string
  updateTime: string
}

// ==================== 认证 ====================
export interface LoginByPasswordParams {
  phone: string
  password: string
}

export interface LoginByCodeParams {
  phone: string
  code: string
}

export interface RegisterParams {
  phone: string
  code: string
  password: string
  confirmPassword: string
}

export interface LoginResult {
  token: string
  user: User
}

export interface AdminLoginParams {
  name: string
  password: string
}

export interface AdminLoginResult {
  token: string
  admin: Admin
}

// ==================== 搜索 ====================
export type SearchType = 'shop' | 'user'

export interface SearchResult {
  type: SearchType
  shops?: Shop[]
  users?: UserSimple[]
}

// ==================== 充值 ====================
export interface RechargeParams {
  amount: number
}

// ==================== 营业额统计 ====================
export interface DailyRevenue {
  date: string
  amount: number
}

// ==================== 商家注册 ====================
export interface RegisterShopParams {
  typeId: number
  name: string
  address: string
  price: number
  images: string
}

// ==================== 发布优惠卷 ====================
export interface CreateVoucherParams {
  price: number
  value: number
  beginTime: string
  endTime: string
  stock: number
}
