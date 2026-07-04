/**
 * 格式化时间
 */
export function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 格式化日期（不含时间）
 */
export function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 获取评分文字描述
 */
export function getScoreLabel(score: number): string {
  if (score >= 4) return '好评'
  if (score >= 2) return '中评'
  return '差评'
}

/**
 * 校验手机号
 */
export function isValidPhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 将图片 URL 字符串转为数组（后端可能返回逗号分隔的字符串）
 */
export function parseImages(images: string): string[] {
  if (!images) return []
  return images.split(',').filter(Boolean)
}

/**
 * 将图片数组转为逗号分隔字符串
 */
export function joinImages(images: string[]): string {
  return images.filter(Boolean).join(',')
}
