<template>
  <div class="page-container">
    <div class="card-wrapper write-wrapper">
      <h3 class="write-title">写评价</h3>
      <p class="write-shop-name">商家：{{ shop?.name }}</p>

      <el-form label-position="top" size="large">
        <el-form-item label="评分" required>
          <StarRating v-model="reviewScore" :interactive="true" :size="28" />
        </el-form-item>
        <el-form-item label="评价内容" required>
          <el-input
            v-model="reviewContent"
            type="textarea"
            :rows="6"
            placeholder="分享您的真实体验..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="上传图片">
          <ImageUploader v-model="reviewImages" :limit="6" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            发表评价
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getShopDetail } from '@/api/shop'
import { createComment } from '@/api/comment'
import type { Shop } from '@/types'
import StarRating from '@/components/StarRating.vue'
import ImageUploader from '@/components/ImageUploader.vue'

const route = useRoute()
const router = useRouter()

const shopId = Number(route.params.id)
const shop = ref<Shop | null>(null)

const reviewScore = ref(0)
const reviewContent = ref('')
const reviewImages = ref<string[]>([])
const submitting = ref(false)

onMounted(async () => {
  try {
    shop.value = await getShopDetail(shopId)
  } catch { /* ignore */ }
})

async function handleSubmit() {
  if (!reviewScore.value) {
    ElMessage.warning('请选择评分')
    return
  }
  if (!reviewContent.value.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('shopId', String(shopId))
    formData.append('content', reviewContent.value)
    formData.append('score', String(reviewScore.value))
    formData.append('images', reviewImages.value.join(','))
    await createComment(formData)
    ElMessage.success('评价发表成功')
    router.push(`/shops/${shopId}`)
  } catch { /* ignore */ } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.write-wrapper {
  max-width: 700px;
}

.write-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 4px;
}

.write-shop-name {
  color: #909399;
  margin-bottom: 24px;
}
</style>
