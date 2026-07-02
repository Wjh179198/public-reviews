<template>
  <div class="blog-card card-wrapper">
    <div class="blog-author">
      <UserAvatar
        :user-id="blog.userId"
        :image="blog.userImage || ''"
        :size="44"
      />
      <div class="author-info">
        <span class="author-name">{{ blog.userName }}</span>
        <span class="blog-time">{{ formatTime(blog.createTime) }}</span>
      </div>
    </div>

    <h3 class="blog-title">{{ blog.title }}</h3>
    <p class="blog-content">{{ blog.content }}</p>

    <div v-if="images.length" class="blog-images">
      <el-image
        v-for="(img, idx) in images"
        :key="idx"
        :src="img"
        :preview-src-list="images"
        :initial-index="idx"
        fit="cover"
        class="blog-thumb"
      />
    </div>

    <div v-if="blog.shopName" class="blog-shop-tag">
      <el-tag size="small" type="warning" effect="plain">
        <el-icon :size="12"><Shop /></el-icon>
        {{ blog.shopName }}
      </el-tag>
    </div>

    <div class="blog-footer">
      <span
        class="like-btn"
        :class="{ liked: isLiked }"
        @click="$emit('like', blog.id)"
      >
        <el-icon><CaretTop /></el-icon>
        {{ blog.likes }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Blog } from '@/types'
import { formatTime, parseImages } from '@/utils'
import { CaretTop } from '@element-plus/icons-vue'
import UserAvatar from './UserAvatar.vue'

const props = defineProps<{
  blog: Blog
  isLiked?: boolean
}>()

defineEmits<{
  like: [id: number]
}>()

const images = computed(() => parseImages(props.blog.images))
</script>

<style scoped>
.blog-card {
  margin-bottom: 16px;
}

.blog-author {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
  cursor: pointer;
}

.author-name:hover {
  color: #409eff;
}

.blog-time {
  font-size: 12px;
  color: #c0c4cc;
}

.blog-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 8px;
}

.blog-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.blog-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.blog-thumb {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
}

.blog-shop-tag {
  margin-bottom: 10px;
}

.blog-footer {
  display: flex;
}

.like-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 16px;
  background: #f5f7fa;
  transition: all 0.2s;
  user-select: none;
}

.like-btn:hover {
  color: #409eff;
  background: #ecf5ff;
}

.like-btn.liked {
  color: #fff;
  background: #409eff;
}
</style>
