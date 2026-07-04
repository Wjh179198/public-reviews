<template>
  <div class="page-container">
    <h2 class="page-title">网友打卡</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="推荐" name="recommend" />
      <el-tab-pane label="我的关注" name="following" />
    </el-tabs>

    <div v-loading="loading" class="blog-list">
      <template v-if="blogs.length">
        <BlogCard
          v-for="blog in blogs"
          :key="blog.id"
          :blog="blog"
          :is-liked="likedBlogIds.has(blog.id)"
          @like="handleLike"
        />
      </template>
      <el-empty v-else description="暂无博客" />
    </div>

    <div v-if="total > 0" class="pagination-area">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        background
        layout="prev, pager, next"
        @change="fetchBlogs"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { Blog } from '@/types'
import { getBlogs, likeBlog, checkBlogLikeStatus } from '@/api/blog'
import BlogCard from '@/components/BlogCard.vue'

const activeTab = ref<'recommend' | 'following'>('recommend')
const blogs = ref<Blog[]>([])
const likedBlogIds = ref<Set<number>>(new Set())
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchBlogs() {
  loading.value = true
  try {
    const result = await getBlogs({
      type: activeTab.value,
      page: page.value,
      pageSize: pageSize.value,
    })
    blogs.value = result.records
    total.value = result.total
    // 批量检查点赞状态
    await fetchLikeStatuses()
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

async function fetchLikeStatuses() {
  const ids = blogs.value.map(b => b.id)
  if (ids.length === 0) return
  const results = await Promise.allSettled(
    ids.map(id => checkBlogLikeStatus(id))
  )
  const set = new Set<number>()
  results.forEach((r, i) => {
    if (r.status === 'fulfilled' && r.value) {
      set.add(ids[i])
    }
  })
  likedBlogIds.value = set
}

async function handleLike(blogId: number) {
  try {
    await likeBlog(blogId)
    const blog = blogs.value.find((b) => b.id === blogId)
    if (!blog) return
    const newSet = new Set(likedBlogIds.value)
    if (newSet.has(blogId)) {
      blog.likes--
      newSet.delete(blogId)
    } else {
      blog.likes++
      newSet.add(blogId)
    }
    likedBlogIds.value = newSet
  } catch { /* ignore */ }
}

function handleTabChange() {
  page.value = 1
  fetchBlogs()
}

onMounted(() => {
  fetchBlogs()
})
</script>

<style scoped>
.page-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.blog-list {
  min-height: 300px;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
