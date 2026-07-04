import { get, post } from './index'
import type { ShopOrder, PageResult } from '@/types'

// 获取用户订单
export function getUserOrders(
  userId: number,
  status?: number,
  page?: number,
  pageSize?: number
) {
  return get<PageResult<ShopOrder>>(`/order/user/${userId}`, {
    status,
    page,
    pageSize,
  })
}

// 创建订单（预订店铺）
export function createOrder(data: {
  shopId: number
  voucherId?: number
}) {
  return post<ShopOrder>('/order/create', data)
}

// 退款
export function refundOrder(orderId: number) {
  return post(`/order/refund/${orderId}`)
}
