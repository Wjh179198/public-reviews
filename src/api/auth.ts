import { get, post } from './index'
import type {
  LoginByPasswordParams,
  LoginByCodeParams,
  RegisterParams,
  LoginResult,
} from '@/types'

// 密码登录
export function loginByPassword(params: LoginByPasswordParams) {
  return post<LoginResult>('/user/login/password', params)
}

// 验证码登录
export function loginByCode(params: LoginByCodeParams) {
  return post<LoginResult>('/user/login/code', params)
}

// 发送验证码
export function sendCode(phone: string) {
  return post('/user/send-code', { phone })
}

// 注册
export function register(params: RegisterParams) {
  return post('/user/register', params)
}
