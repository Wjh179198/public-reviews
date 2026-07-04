import { post } from './index'

// 上传图片
export function uploadImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return post<string>('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
