import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { getUserInfo as fetchUserInfo } from '@/api/user'
import { loginByPassword, loginByCode, register, sendCode } from '@/api/auth'
import type {
  LoginByPasswordParams,
  LoginByCodeParams,
  RegisterParams,
} from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(
    JSON.parse(localStorage.getItem('user') || 'null')
  )

  const isLoggedIn = computed(() => !!token.value)
  const isMerchant = computed(() => userInfo.value?.status === 1)
  const isBanned = computed(() => userInfo.value?.status === 2)

  async function loginWithPassword(params: LoginByPasswordParams) {
    const result = await loginByPassword(params)
    token.value = result.token
    userInfo.value = result.user
    localStorage.setItem('token', result.token)
    localStorage.setItem('user', JSON.stringify(result.user))
    return result
  }

  async function loginWithCode(params: LoginByCodeParams) {
    const result = await loginByCode(params)
    token.value = result.token
    userInfo.value = result.user
    localStorage.setItem('token', result.token)
    localStorage.setItem('user', JSON.stringify(result.user))
    return result
  }

  async function registerUser(params: RegisterParams) {
    return await register(params)
  }

  async function sendCodeAction(phone: string) {
    return await sendCode(phone)
  }

  async function fetchUser() {
    const user = await fetchUserInfo()
    userInfo.value = user
    localStorage.setItem('user', JSON.stringify(user))
    return user
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function updateUser(user: User) {
    userInfo.value = user
    localStorage.setItem('user', JSON.stringify(user))
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isMerchant,
    isBanned,
    loginWithPassword,
    loginWithCode,
    registerUser,
    sendCodeAction,
    fetchUser,
    logout,
    updateUser,
  }
})
