<template>
  <div class="page-container">
    <div class="search-header card-wrapper">
      <h2 class="search-title">
        搜索"<span class="keyword">{{ keyword }}</span>"
        <el-tag>{{ searchType === 'shop' ? '商家' : '用户' }}</el-tag>
      </h2>
    </div>

    <!-- 商家结果 -->
    <div v-if="searchType === 'shop'" v-loading="loading">
      <div v-if="shops.length" class="shop-grid">
        <ShopCard v-for="shop in shops" :key="shop.id" :shop="shop" />
      </div>
      <el-empty v-else description="未找到相关商家" />
    </div>

    <!-- 用户结果 -->
    <div v-else v-loading="loading">
      <div v-if="users.length" class="user-grid">
        <div
          v-for="user in users"
          :key="user.id"
          class="user-card card-wrapper"
          @click="$router.push(`/user/${user.id}`)"
        >
          <UserAvatar :user-id="user.id" :image="user.image" :size="56" />
          <div class="user-card-info">
            <span class="user-card-name">{{ user.name }}</span>
            <span class="user-card-addr">{{ user.address || '未知地区' }}</span>
            <div class="user-card-stats">
              <span>粉丝 {{ user.fansCounts }}</span>
              <span>关注 {{ user.followerCounts }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="未找到相关用户" />
    </div>

    <div class="search-back">
      <el-button @click="$router.back()">返回</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import type { Shop, UserSimple, SearchType } from '@/types'
import { get, post } from '@/api'
import ShopCard from '@/components/ShopCard.vue'
import UserAvatar from '@/components/UserAvatar.vue'

const route = useRoute()

const searchType = ref<SearchType>((route.query.type as SearchType) || 'shop')
const keyword = ref((route.query.keyword as string) || '')

const shops = ref<Shop[]>([])
const users = ref<UserSimple[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    if (searchType.value === 'shop') {
      shops.value = await get<Shop[]>('/shop/search', {
        keyword: keyword.value,
      })
    } else {
      users.value = await get<UserSimple[]>('/user/search', {
        keyword: keyword.value,
      })
    }
  } catch { /* ignore */ } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.search-title {
  font-size: 20px;
  color: #303133;
}

.keyword {
  color: #409eff;
}

.shop-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.user-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.user-card-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-card-name {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.user-card-addr {
  font-size: 13px;
  color: #909399;
}

.user-card-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #c0c4cc;
}

.search-back {
  margin-top: 20px;
  text-align: center;
}
</style>
