import { get, post, del } from './index'
import type { Comment, PageResult, CommentQuery } from '@/types'

// 获取评价列表
export function getComments(params: CommentQuery) {
  return get<PageResult<Comment>>('/comment/list', params)
}

// 发表评价
export function createComment(data: { shopId: number; content: string; score: number; images: string }) {
  return post('/comment/create', data)
}

// 评价点赞/取消点赞
export function likeComment(commentId: number) {
  return post(`/comment/${commentId}/like`)
}

// 检查是否已点赞某条评价
export function checkCommentLikeStatus(commentId: number) {
  return get<boolean>(`/comment/like/check/${commentId}`)
}
