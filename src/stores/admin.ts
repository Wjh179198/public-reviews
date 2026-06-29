import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Admin } from '@/types'
import { adminLogin as doLogin, getAdminInfo } from '@/api/admin'
import type { AdminLoginParams } from '@/types'

export const useAdminStore = defineStore('admin', () => {
  const token = ref<string>(localStorage.getItem('adminToken') || '')
  const adminInfo = ref<Admin | null>(
    JSON.parse(localStorage.getItem('adminInfo') || 'null')
  )

  const isLoggedIn = computed(() => !!token.value)

  async function login(params: AdminLoginParams) {
    const result = await doLogin(params)
    token.value = result.token
    adminInfo.value = result.admin
    localStorage.setItem('adminToken', result.token)
    localStorage.setItem('adminInfo', JSON.stringify(result.admin))
    return result
  }

  async function fetchAdmin() {
    const admin = await getAdminInfo()
    adminInfo.value = admin
    localStorage.setItem('adminInfo', JSON.stringify(admin))
    return admin
  }

  function logout() {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminInfo')
  }

  return { token, adminInfo, isLoggedIn, login, fetchAdmin, logout }
})
