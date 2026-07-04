import { get, post } from './index'
import type {
  Shop,
  ShopType,
  PageResult,
  ShopQuery,
  RegisterShopParams,
  DailyRevenue,
} from '@/types'

// 获取店铺类型列表
export function getShopTypes() {
  return get<ShopType[]>('/shop/types')
}

// 获取店铺列表
export function getShopList(params: ShopQuery) {
  return get<PageResult<Shop>>('/shop/list', params)
}

// 获取店铺详情
export function getShopDetail(id: number) {
  return get<Shop>(`/shop/${id}`)
}

// 注册成为商家
export function registerShop(params: RegisterShopParams) {
  return post('/shop/register', params)
}

// 获取商家近7天营业额
export function getShopRevenue(shopId: number) {
  return get<DailyRevenue[]>(`/shop/${shopId}/revenue`)
}
