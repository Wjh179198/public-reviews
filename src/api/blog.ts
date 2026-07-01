import { get, post, del } from './index'
import type { Blog, PageResult, BlogQuery } from '@/types'

// 获取博客列表
export function getBlogs(params: BlogQuery) {
  return get<PageResult<Blog>>('/blog/list', params)
}

// 发表博客
export function createBlog(data: FormData) {
  return post('/blog/create', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 删除博客
export function deleteBlog(blogId: number) {
  return del(`/blog/${blogId}`)
}

// 博客点赞/取消点赞
export function likeBlog(blogId: number) {
  return post(`/blog/${blogId}/like`)
}

// 检查是否已点赞某篇博客
export function checkBlogLikeStatus(blogId: number) {
  return get<boolean>(`/blog/like/check/${blogId}`)
}

// 获取用户发表的博客
export function getUserBlogs(userId: number, page: number, pageSize: number) {
  return get<PageResult<Blog>>(`/blog/user/${userId}`, { page, pageSize })
}
