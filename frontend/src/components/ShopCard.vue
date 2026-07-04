<template>
  <div class="shop-card" @click="$router.push(`/shops/${shop.id}`)">
    <div class="shop-image">
      <img :src="coverImage" :alt="shop.name" />
    </div>
    <div class="shop-info">
      <h4 class="shop-name">{{ shop.name }}</h4>
      <div class="shop-tags">
        <el-tag size="small" type="info">{{ shop.typeName }}</el-tag>
      </div>
      <div class="shop-meta">
        <StarRating :score="shop.score" :size="14" />
        <span class="shop-comments">{{ shop.comments }}条评价</span>
      </div>
      <div class="shop-bottom">
        <span class="shop-address">
          <el-icon :size="14"><Location /></el-icon>
          {{ shop.address }}
        </span>
        <span class="shop-price">
          <span class="price-symbol">¥</span>{{ shop.price }}/人
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Shop } from '@/types'
import { parseImages } from '@/utils'
import { Location } from '@element-plus/icons-vue'
import StarRating from './StarRating.vue'

const props = defineProps<{
  shop: Shop
}>()

const coverImage = computed(() => {
  const imgs = parseImages(props.shop.images)
  return imgs[0] || 'https://via.placeholder.com/280x180?text=暂无图片'
})
</script>

<style scoped>
.shop-card {
  display: flex;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.3s, transform 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.shop-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.shop-image {
  width: 200px;
  min-height: 140px;
  flex-shrink: 0;
}

.shop-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-info {
  flex: 1;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.shop-name {
  font-size: 18px;
  color: #303133;
  margin: 0;
}

.shop-tags {
  display: flex;
  gap: 6px;
}

.shop-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.shop-comments {
  font-size: 12px;
  color: #909399;
}

.shop-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.shop-address {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 2px;
}

.shop-price {
  font-size: 14px;
  color: #f56c6c;
}

.price-symbol {
  font-size: 12px;
}
</style>
