<template>
  <el-container class="admin-container">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="admin-aside">
      <div class="admin-logo">
        <span class="admin-logo-text">管理后台</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :router="true"
        class="admin-menu"
        background-color="#1f2d3d"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/profile">
          <el-icon><Setting /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧 -->
    <el-container>
      <el-header class="admin-header">
        <div class="header-spacer" />
        <div class="header-right">
          <span class="admin-name">{{ adminStore.adminInfo?.name }}</span>
          <el-button text type="danger" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()

const activeMenu = computed(() => route.path)

async function handleLogout() {
  await adminStore.logout()
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-container {
  height: 100vh;
}

.admin-aside {
  background-color: #1f2d3d;
  overflow-y: auto;
}

.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.admin-logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 4px;
}

.admin-menu {
  border-right: none;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  height: 60px;
  padding: 0 24px;
}

.header-spacer {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-name {
  font-size: 14px;
  color: #333;
}

.admin-main {
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}
</style>
