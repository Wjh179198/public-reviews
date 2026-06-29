<template>
  <div class="page-container">
    <div class="card-wrapper" style="max-width: 800px">
      <h2 class="page-title">发表博客</h2>

      <el-form label-position="top" size="large">
        <el-form-item label="关联商家（必选）" required>
          <el-select
            v-model="selectedShopId"
            placeholder="搜索并选择商家"
            filterable
            remote
            :remote-method="searchShops"
            :loading="shopLoading"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="s in shopOptions"
              :key="s.id"
              :label="s.name"
              :value="s.id"
            >
              <span>{{ s.name }}</span>
              <span style="color: #909399; font-size: 12px; margin-left: 8px">
                {{ s.typeName }} | {{ s.address }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="标题" required>
          <el-input
            v-model="title"
            placeholder="请输入博客标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="正文" required>
          <el-input
            v-model="content"
            type="textarea"
            :rows="8"
            placeholder="分享你的打卡体验..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="上传图片">
          <ImageUploader v-model="images" :limit="9" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="publishing" @click="handlePublish">
            发表
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createBlog } from '@/api/blog'
import { getShopList } from '@/api/shop'
import type { Shop } from '@/types'
import ImageUploader from '@/components/ImageUploader.vue'

const router = useRouter()

const selectedShopId = ref<number | null>(null)
const shopOptions = ref<Shop[]>([])
const shopLoading = ref(false)

const title = ref('')
const content = ref('')
const images = ref<string[]>([])
const publishing = ref(false)

async function searchShops(query: string) {
  if (!query) {
    shopOptions.value = []
    return
  }
  shopLoading.value = true
  try {
    const result = await getShopList({ page: 1, pageSize: 20 })
    shopOptions.value = result.list.filter((s) =>
      s.name.toLowerCase().includes(query.toLowerCase())
    )
  } catch { /* ignore */ } finally {
    shopLoading.value = false
  }
}

async function handlePublish() {
  if (!selectedShopId.value) {
    ElMessage.warning('请选择关联商家')
    return
  }
  if (!title.value.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!content.value.trim()) {
    ElMessage.warning('请输入正文')
    return
  }
  publishing.value = true
  try {
    const formData = new FormData()
    formData.append('shopId', String(selectedShopId.value))
    formData.append('title', title.value)
    formData.append('content', content.value)
    formData.append('images', images.value.join(','))
    await createBlog(formData)
    ElMessage.success('发表成功')
    router.push('/blog')
  } catch { /* ignore */ } finally {
    publishing.value = false
  }
}
</script>

<style scoped>
.page-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 20px;
}
</style>
