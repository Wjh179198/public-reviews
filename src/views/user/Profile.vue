<template>
  <div class="page-container">
    <h2 class="page-title">我的</h2>

    <!-- 个人信息 -->
    <div class="card-wrapper profile-header">
      <UserAvatar
        :user-id="user.id"
        :image="user.image"
        :size="72"
        :clickable="false"
      />
      <div class="profile-info">
        <h3>{{ user.name }}</h3>
        <p>{{ user.address || '未设置地区' }}</p>
        <div class="profile-stats">
          <span class="stat-item" @click="showFans = true">
            <strong>{{ user.fansCounts }}</strong> 粉丝
          </span>
          <span class="stat-item" @click="showFollows = true">
            <strong>{{ user.followerCounts }}</strong> 关注
          </span>
        </div>
      </div>
      <div class="profile-actions">
        <el-button @click="$router.push('/profile/edit')">编辑资料</el-button>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <!-- 余额 & 订单 -->
    <div class="card-wrapper">
      <div class="money-row">
        <span class="money-label">我的余额：</span>
        <span class="money-amount">¥{{ user.money?.toFixed(2) }}</span>
        <el-button type="primary" size="small" @click="$router.push('/profile/recharge')">
          充值
        </el-button>
      </div>
      <div class="action-row">
        <el-button @click="$router.push('/profile/orders')">我的订单</el-button>
      </div>
    </div>

    <!-- 我的博客 -->
    <div class="card-wrapper">
      <h3 class="section-title">我的博客</h3>
      <div v-if="myBlogs.length" class="blog-mini-list">
        <div v-for="blog in myBlogs" :key="blog.id" class="blog-mini-item">
          <div class="blog-mini-info">
            <h4 class="blog-mini-title" @click="$router.push(`/shops/${blog.shopId}`)">{{ blog.title }}</h4>
            <span class="blog-mini-time">{{ formatTime(blog.createTime) }}</span>
            <span class="blog-mini-likes">👍 {{ blog.likes }}</span>
          </div>
          <el-button
            type="danger"
            size="small"
            text
            @click="handleDeleteBlog(blog.id)"
          >
            删除
          </el-button>
        </div>
      </div>
      <el-empty v-else description="暂无博客" :image-size="60" />
    </div>

    <!-- 粉丝弹窗 -->
    <el-dialog v-model="showFans" title="我的粉丝" width="400px">
      <div v-loading="fansLoading">
        <div v-for="f in fans" :key="f.id" class="user-list-item" @click="goUser(f.id)">
          <UserAvatar :user-id="f.id" :image="f.image" :size="40" />
          <span>{{ f.name }}</span>
        </div>
        <el-empty v-if="!fansLoading && !fans.length" description="暂无粉丝" :image-size="40" />
      </div>
    </el-dialog>

    <!-- 关注弹窗 -->
    <el-dialog v-model="showFollows" title="我的关注" width="400px">
      <div v-loading="followsLoading">
        <div v-for="f in follows" :key="f.id" class="user-list-item" @click="goUser(f.id)">
          <UserAvatar :user-id="f.id" :image="f.image" :size="40" />
          <span>{{ f.name }}</span>
        </div>
        <el-empty v-if="!followsLoading && !follows.length" description="暂无关注" :image-size="40" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { deleteBlog } from '@/api/blog'
import { getFans, getFollowings, getUserBlogs } from '@/api/user'
import { formatTime } from '@/utils'
import type { Blog, UserSimple } from '@/types'
import UserAvatar from '@/components/UserAvatar.vue'

const router = useRouter()
const userStore = useUserStore()

const user = computed(() => userStore.userInfo!)

const myBlogs = ref<Blog[]>([])
const fans = ref<UserSimple[]>([])
const follows = ref<UserSimple[]>([])
const showFans = ref(false)
const showFollows = ref(false)
const fansLoading = ref(false)
const followsLoading = ref(false)

async function fetchBlogs() {
  if (!user.value) return
  try {
    const result = await getUserBlogs(user.value.id, 1, 100)
    myBlogs.value = result.records
  } catch { /* ignore */ }
}

async function fetchFans() {
  fansLoading.value = true
  try {
    const result = await getFans(user.value.id, 1, 100)
    fans.value = result.records
  } catch { /* ignore */ } finally {
    fansLoading.value = false
  }
}

async function fetchFollows() {
  followsLoading.value = true
  try {
    const result = await getFollowings(user.value.id, 1, 100)
    follows.value = result.records
  } catch { /* ignore */ } finally {
    followsLoading.value = false
  }
}

async function handleDeleteBlog(blogId: number) {
  try {
    await ElMessageBox.confirm('确定要删除这篇博客吗？', '确认', {
      type: 'warning',
    })
    await deleteBlog(blogId)
    ElMessage.success('删除成功')
    myBlogs.value = myBlogs.value.filter((b) => b.id !== blogId)
  } catch { /* ignore */ }
}

function goUser(userId: number) {
  showFans.value = false
  showFollows.value = false
  router.push(`/user/${userId}`)
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchBlogs()
})

// watch dialogs
import { watch } from 'vue'
watch(showFans, (val) => {
  if (val && fans.value.length === 0) fetchFans()
})
watch(showFollows, (val) => {
  if (val && follows.value.length === 0) fetchFollows()
})
</script>

<style scoped>
.page-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-info {
  flex: 1;
}

.profile-info h3 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 4px;
}

.profile-info p {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.profile-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  font-size: 14px;
  color: #606266;
  cursor: pointer;
}

.stat-item strong {
  color: #303133;
}

.stat-item:hover {
  color: #409eff;
}

.profile-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.money-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.money-label {
  font-size: 15px;
  color: #606266;
}

.money-amount {
  font-size: 22px;
  color: #f56c6c;
  font-weight: bold;
}

.action-row {
  display: flex;
  gap: 12px;
}

.section-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
}

.blog-mini-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.blog-mini-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.blog-mini-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.blog-mini-info h4 {
  font-size: 14px;
  color: #303133;
  margin: 0;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.blog-mini-title {
  cursor: pointer;
}

.blog-mini-title:hover {
  color: #409eff;
}

.blog-mini-time {
  font-size: 12px;
  color: #c0c4cc;
}

.blog-mini-likes {
  font-size: 12px;
  color: #909399;
}

.user-list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.user-list-item:hover {
  background: #f5f7fa;
}
</style>
