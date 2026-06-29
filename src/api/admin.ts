import { get, post, put } from './index'
import type {
  User,
  AdminLoginParams,
  AdminLoginResult,
  PageResult,
} from '@/types'

// 管理员登录
export function adminLogin(params: AdminLoginParams) {
  return post<AdminLoginResult>('/admin/login', params)
}

// 查询用户列表
export function getUsers(params: {
  id?: number
  name?: string
  status?: number
  page: number
  pageSize: number
}) {
  return get<PageResult<User>>('/admin/users', params)
}

// 获取用户详情
export function getUserDetailById(userId: number) {
  return get<User>(`/admin/user/${userId}`)
}

// 封禁用户
export function banUser(userId: number) {
  return post(`/admin/user/ban/${userId}`)
}

// 解封用户
export function unbanUser(userId: number) {
  return post(`/admin/user/unban/${userId}`)
}

// 编辑管理员信息
export function updateAdminInfo(data: { name?: string; password?: string }) {
  return put('/admin/update', data)
}

// 获取管理员信息
export function getAdminInfo() {
  return get('/admin/info')
}
