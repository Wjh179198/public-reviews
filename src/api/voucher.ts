import { get, post } from './index'
import type { Voucher, VoucherOrder, CreateVoucherParams } from '@/types'

// 获取商家优惠卷列表
export function getShopVouchers(shopId: number) {
  return get<Voucher[]>(`/voucher/shop/${shopId}`)
}

// 抢购优惠卷
export function grabVoucher(voucherId: number) {
  return post(`/voucher/grab/${voucherId}`)
}

// 获取用户优惠卷
export function getUserVouchers(userId: number) {
  return get<VoucherOrder[]>(`/voucher/user/${userId}`)
}

// 发布优惠卷（商家）
export function createVoucher(data: CreateVoucherParams) {
  return post('/voucher/create', data)
}

// 商家优惠卷管理列表
export function getManageVouchers(shopId: number) {
  return get<Voucher[]>(`/voucher/manage/${shopId}`)
}
