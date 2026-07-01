<template>
  <div class="page-container">
    <!-- 用户信息头部 -->
    <div v-if="user" class="card-wrapper user-header">
      <div class="user-header-main">
        <UserAvatar
          :user-id="user.id"
          :image="user.image"
          :size="80"
          :clickable="false"
        />
        <div class="user-detail-info">
          <h2>{{ user.name }}</h2>
          <p class="user-address">
            <el-icon><Location /></el-icon>
            {{ user.address || '未知地区' }}
          </p>
          <div class="user-stats-detail">
            <span><strong>{{ user.fansCounts }}</strong> 粉丝</span>
            <span><strong>{{ user.followerCounts }}</strong> 关注</span>
          </div>
        </div>
      </div>
      <div class="user-header-actions">
        <el-button
          :type="isFollowing ? 'default' : 'primary'"
          @click="handleToggleFollow"
        >
          {{ isFollowing ? '已关注' : '+ 关注' }}
        </el-button>
      </div>
    </div>

    <!-- 他的博客 -->
    <div class="card-wrapper">
      <h3 class="section-title">TA 的博客</h3>
      <div v-loading="blogLoading">
        <template v-if="blogs.length">
          <BlogCard
            v-for="blog in blogs"
            :key="blog.id"
            :blog="blog"
            @like="handleLikeBlog"
          />
        </template>
        <el-empty v-else description="暂无博客" :image-size="60" />
      </div>
      <div v-if="blogTotal > 10" class="pagination-area">
        <el-pagination
          v-model:current-page="blogPage"
          :page-size="10"
          :total="blogTotal"
          background
          layout="prev, pager, next"
          @change="fetchBlogs"
        />
      </div>
    </div>

    <!-- 共同关注 -->
    <div class="card-wrapper">
      <h3 class="section-title">共同关注</h3>
      <div v-loading="commonLoading">
        <div v-if="commonFollows.length" class="common-follow-list">
          <div
            v-for="u in commonFollows"
            :key="u.id"
            class="common-follow-item"
            @click="$router.push(`/user/${u.id}`)"
          >
            <UserAvatar :user-id="u.id" :image="u.image" :size="40" />
            <span>{{ u.name }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无共同关注" :image-size="40" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserDetail, followUser, checkFollowStatus, getCommonFollows, getUserBlogs } from '@/api/user'
import { likeBlog } from '@/api/blog'
import { formatTime } from '@/utils'
import type { UserSimple, Blog } from '@/types'
import UserAvatar from '@/components/UserAvatar.vue'
import BlogCard from '@/components/BlogCard.vue'

const route = useRoute()
const userStore = useUserStore()

const userId = computed(() => Number(route.params.id))

const user = ref<UserSimple | null>(null)
const isFollowing = ref(false)

const blogs = ref<Blog[]>([])
const blogPage = ref(1)
const blogTotal = ref(0)
const blogLoading = ref(false)

const commonFollows = ref<UserSimple[]>([])
const commonLoading = ref(false)

async function fetchUser() {
  try {
    user.value = await getUserDetail(userId.value)
  } catch { /* ignore */ }
}

async function fetchFollowStatus() {
  try {
    isFollowing.value = await checkFollowStatus(userId.value)
  } catch { /* ignore */ }
}

async function fetchBlogs() {
  blogLoading.value = true
  try {
    const result = await getUserBlogs(userId.value, blogPage.value, 10)
    blogs.value = result.list
    blogTotal.value = result.total
  } catch { /* ignore */ } finally {
    blogLoading.value = false
  }
}

async function fetchCommonFollows() {
  commonLoading.value = true
  try {
    commonFollows.value = await getCommonFollows(userId.value)
  } catch { /* ignore */ } finally {
    commonLoading.value = false
  }
}

async function handleToggleFollow() {
  try {
    await followUser(userId.value)
    isFollowing.value = !isFollowing.value
    if (user.value) {
      if (isFollowing.value) {
        user.value.fansCounts++
      } else {
        user.value.fansCounts--
      }
    }
    ElMessage.success(isFollowing.value ? '关注成功' : '已取消关注')
  } catch { /* ignore */ }
}

async function handleLikeBlog(blogId: number) {
  try {
    await likeBlog(blogId)
    const blog = blogs.value.find((b) => b.id === blogId)
    if (blog) {
      if (blog.isLiked) {
        blog.likes--
        blog.isLiked = false
      } else {
        blog.likes++
        blog.isLiked = true
      }
    }
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchUser()
  fetchFollowStatus()
  fetchBlogs()
  fetchCommonFollows()
})
</script>

<style scoped>
.user-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.user-header-main {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-detail-info h2 {
  font-size: 22px;
  color: #303133;
  margin-bottom: 4px;
}

.user-address {
  font-size: 14px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}

.user-stats-detail {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #606266;
}

.user-stats-detail strong {
  color: #303133;
}

.section-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
}

.common-follow-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.common-follow-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
  font-size: 14px;
}

.common-follow-item:hover {
  background: #f0f2f5;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
