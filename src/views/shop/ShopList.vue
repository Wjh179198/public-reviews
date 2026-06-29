<template>
  <div class="page-container">
    <h2 class="page-title">商家展示</h2>

    <!-- 筛选区域 -->
    <div class="filter-section card-wrapper">
      <div class="filter-row">
        <span class="filter-label">商家类型：</span>
        <div class="filter-options">
          <el-tag
            v-for="t in shopTypes"
            :key="t.id"
            :type="selectedTypeId === t.id ? 'primary' : 'info'"
            :effect="selectedTypeId === t.id ? 'dark' : 'plain'"
            class="filter-tag"
            @click="selectType(t.id)"
          >
            {{ t.name }}
          </el-tag>
          <el-tag
            v-if="selectedTypeId !== null"
            type="warning"
            effect="plain"
            class="filter-tag"
            @click="selectedTypeId = null"
          >
            清除
          </el-tag>
        </div>
      </div>

      <div class="filter-row">
        <span class="filter-label">评分区间：</span>
        <div class="filter-options">
          <el-radio-group v-model="selectedScoreRange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="below2">2星及以下</el-radio-button>
            <el-radio-button label="2to4">2-4星</el-radio-button>
            <el-radio-button label="4to5">4-5星</el-radio-button>
            <el-radio-button label="noComment">未点评</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <!-- 商家列表 -->
    <div v-loading="loading" class="shop-grid">
      <template v-if="shopList.length">
        <ShopCard v-for="shop in shopList" :key="shop.id" :shop="shop" />
      </template>
      <el-empty v-else description="暂无商家" />
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-area">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        background
        layout="total, prev, pager, next, sizes"
        @change="fetchShops"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import type { ShopType, Shop } from '@/types'
import { getShopTypes, getShopList } from '@/api/shop'
import ShopCard from '@/components/ShopCard.vue'

const shopTypes = ref<ShopType[]>([])
const selectedTypeId = ref<number | null>(null)
const selectedScoreRange = ref('')

const shopList = ref<Shop[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

function selectType(typeId: number) {
  selectedTypeId.value = selectedTypeId.value === typeId ? null : typeId
}

async function fetchShops() {
  loading.value = true
  try {
    const result = await getShopList({
      typeId: selectedTypeId.value || undefined,
      scoreRange: (selectedScoreRange.value || undefined) as any,
      page: page.value,
      pageSize: pageSize.value,
    })
    shopList.value = result.list
    total.value = result.total
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

watch([selectedTypeId, selectedScoreRange], () => {
  page.value = 1
  fetchShops()
})

onMounted(async () => {
  try {
    shopTypes.value = await getShopTypes()
  } catch {
    // ignore
  }
  fetchShops()
})
</script>

<style scoped>
.page-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  width: 80px;
  flex-shrink: 0;
  line-height: 28px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.filter-options {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  cursor: pointer;
}

.shop-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 300px;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
