<template>
  <div class="page-container">
    <h2 class="page-title">用户管理</h2>

    <!-- 搜索区域 -->
    <div class="card-wrapper">
      <el-form :inline="true" size="default">
        <el-form-item label="用户ID">
          <el-input
            v-model="query.id"
            placeholder="按ID精确查询"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input
            v-model="query.name"
            placeholder="模糊搜索"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="商户状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="普通用户" :value="0" />
            <el-option label="商户" :value="1" />
            <el-option label="已封禁" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户表格 -->
    <div class="card-wrapper">
      <el-table v-loading="loading" :data="users" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="70">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.image || undefined">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="昵称" width="150" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="success">普通用户</el-tag>
            <el-tag v-else-if="row.status === 1" type="warning">商户</el-tag>
            <el-tag v-else type="danger">已封禁</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="money" label="余额" width="100">
          <template #default="{ row }">¥{{ row.money?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="address" label="地区" width="120" />
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">详情</el-button>
            <el-button
              v-if="row.status !== 2"
              size="small"
              type="danger"
              @click="handleBan(row)"
            >
              封禁
            </el-button>
            <el-button
              v-else
              size="small"
              type="success"
              @click="handleUnban(row)"
            >
              解封
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-area">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          background
          layout="total, prev, pager, next, sizes"
          @change="fetchUsers"
        />
      </div>
    </div>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="500px">
      <div v-if="detailUser">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ detailUser.id }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailUser.name }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="detailUser.status === 0">普通用户</el-tag>
            <el-tag v-else-if="detailUser.status === 1">商户</el-tag>
            <el-tag v-else type="danger">已封禁</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="余额">¥{{ detailUser.money?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="粉丝数">{{ detailUser.fansCounts }}</el-descriptions-item>
          <el-descriptions-item label="关注数">{{ detailUser.followerCounts }}</el-descriptions-item>
          <el-descriptions-item label="地区">{{ detailUser.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="店铺ID">{{ detailUser.shopId || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { getUsers, getUserDetailById, banUser, unbanUser } from '@/api/admin'
import { formatTime } from '@/utils'
import type { User } from '@/types'

const loading = ref(false)
const users = ref<User[]>([])
const total = ref(0)

const query = reactive({
  id: undefined as number | undefined,
  name: '',
  status: undefined as number | undefined,
  page: 1,
  pageSize: 10,
})

const detailVisible = ref(false)
const detailUser = ref<User | null>(null)

async function fetchUsers() {
  loading.value = true
  try {
    const result = await getUsers({
      id: query.id || undefined,
      name: query.name || undefined,
      status: query.status !== undefined ? query.status : undefined,
      page: query.page,
      pageSize: query.pageSize,
    })
    users.value = result.list
    total.value = result.total
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchUsers()
}

function handleReset() {
  query.id = undefined
  query.name = ''
  query.status = undefined
  query.page = 1
  fetchUsers()
}

async function showDetail(row: User) {
  try {
    detailUser.value = await getUserDetailById(row.id)
    detailVisible.value = true
  } catch { /* ignore */ }
}

async function handleBan(row: User) {
  try {
    await ElMessageBox.confirm(`确定要封禁用户 "${row.name}" 吗？`, '封禁确认', {
      type: 'warning',
    })
    await banUser(row.id)
    ElMessage.success('封禁成功')
    row.status = 2
  } catch { /* ignore */ }
}

async function handleUnban(row: User) {
  try {
    await unbanUser(row.id)
    ElMessage.success('解封成功')
    row.status = 0
  } catch { /* ignore */ }
}

fetchUsers()
</script>

<style scoped>
.page-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 20px;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
