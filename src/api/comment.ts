import { get, post, del } from './index'
import type { Comment, PageResult, CommentQuery } from '@/types'

// 获取评价列表
export function getComments(params: CommentQuery) {
  return get<PageResult<Comment>>('/comment/list', params)
}

// 发表评价
export function createComment(data: FormData) {
  return post('/comment/create', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 评价点赞/取消点赞
export function likeComment(commentId: number) {
  return post(`/comment/${commentId}/like`)
}
