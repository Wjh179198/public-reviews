import { get, post, put } from './index'
import type { User, UserSimple, PageResult, RechargeParams, Blog } from '@/types'

// 获取当前用户信息
export function getUserInfo() {
  return get<User>('/user/info')
}

// 更新用户信息
export function updateUserInfo(data: Partial<User>) {
  return put('/user/update', data)
}

// 获取用户详情（查看其他用户）
export function getUserDetail(userId: number) {
  return get<UserSimple>(`/user/${userId}`)
}

// 关注/取消关注用户
export function followUser(userId: number) {
  return post(`/user/follow/${userId}`)
}

// 获取粉丝列表
export function getFans(userId: number, page: number, pageSize: number) {
  return get<PageResult<UserSimple>>(`/user/${userId}/fans`, { page, pageSize })
}

// 获取关注列表
export function getFollowings(userId: number, page: number, pageSize: number) {
  return get<PageResult<UserSimple>>(`/user/${userId}/followings`, { page, pageSize })
}

// 获取共同关注
export function getCommonFollows(userId: number) {
  return get<UserSimple[]>(`/user/${userId}/common-follows`)
}

// 获取用户发表的博客
export function getUserBlogs(userId: number, page: number, pageSize: number) {
  return get<PageResult<Blog>>(`/blog/user/${userId}`, { page, pageSize })
}

// 余额充值
export function recharge(params: RechargeParams) {
  return post('/user/recharge', params)
}
