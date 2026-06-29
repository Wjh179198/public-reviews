import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types'
import { ElMessage } from 'element-plus'

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})

// 请求拦截器 - 注入 token
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一错误处理
// 后端 code 约定: 1=成功, 0=失败
instance.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse
    if (res.code !== 1) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return response
  },
  (error) => {
    // 优先提取后端返回的错误消息
    const serverMsg = error.response?.data?.msg
    if (serverMsg) {
      ElMessage.error(serverMsg)
      return Promise.reject(new Error(serverMsg))
    }
    // 网络超时或无法连接
    if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
      return Promise.reject(error)
    }
    if (!error.response) {
      ElMessage.error('网络连接失败，请检查后端服务是否启动')
      console.error('[API] 网络错误:', error.message, '\n请确认后端服务运行在 http://localhost:8080')
      return Promise.reject(error)
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export async function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.get<ApiResponse<T>>(url, { params, ...config })
  return response.data.data
}

export async function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.post<ApiResponse<T>>(url, data, config)
  return response.data.data
}

export async function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.put<ApiResponse<T>>(url, data, config)
  return response.data.data
}

export async function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.delete<ApiResponse<T>>(url, config)
  return response.data.data
}

export default instance
