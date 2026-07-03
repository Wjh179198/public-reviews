<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="aside">
      <div class="logo" @click="$router.push('/shops')">
        <span class="logo-text">大众点评</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :router="true"
        class="side-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/shops">
          <el-icon><Shop /></el-icon>
          <span>商户展示</span>
        </el-menu-item>
        <el-menu-item index="/blog">
          <el-icon><Notebook /></el-icon>
          <span>网友打卡</span>
        </el-menu-item>
        <el-menu-item index="/blog/publish">
          <el-icon><Edit /></el-icon>
          <span>发表博客</span>
        </el-menu-item>
        <el-menu-item index="/register-shop">
          <el-icon><CirclePlus /></el-icon>
          <span>注册商户</span>
        </el-menu-item>
        <el-menu-item index="/shop-manage">
          <el-icon><Setting /></el-icon>
          <span>我的店铺</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>我的</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-select
            v-model="searchType"
            class="search-type"
            size="default"
          >
            <el-option label="搜商家" value="shop" />
            <el-option label="搜用户" value="user" />
          </el-select>
          <el-input
            v-model="keyword"
            placeholder="输入名称搜索..."
            class="search-input"
            size="default"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="user-badge">
              <el-avatar
                :size="36"
                :src="userStore.userInfo?.image || ''"
                :icon="UserFilled"
              />
              <span class="user-name">{{ userStore.userInfo?.name }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UserFilled } from '@element-plus/icons-vue'
import type { SearchType } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const searchType = ref<SearchType>('shop')
const keyword = ref('')

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/shops')) return '/shops'
  if (path.startsWith('/blog')) {
    if (path === '/blog/publish') return '/blog/publish'
    return '/blog'
  }
  return path
})

function handleSearch() {
  const kw = keyword.value.trim()
  if (!kw) return
  router.push({
    path: '/search',
    query: { type: searchType.value, keyword: kw },
  })
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.logo-text {
  color: #fff;
  font-size: 22px;
  font-weight: bold;
  letter-spacing: 2px;
}

.side-menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
  height: 60px;
  z-index: 1;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0;
  width: 480px;
}

.search-type {
  width: 110px;
}
.search-type :deep(.el-input__inner) {
  border-radius: 4px 0 0 4px;
  border-right: none;
}
.search-input :deep(.el-input__inner) {
  border-radius: 0 4px 4px 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-badge:hover {
  background: #f5f7fa;
}

.user-name {
  font-size: 14px;
  color: #333;
}

.main-content {
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  overflow-y: auto;
}
</style>
