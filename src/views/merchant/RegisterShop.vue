<template>
  <div class="page-container">
    <div class="card-wrapper" style="max-width: 600px">
      <h3 class="page-title">成为商家</h3>

      <el-form label-position="top" size="large">
        <el-form-item label="商家类型" required>
          <el-select v-model="form.typeId" placeholder="请选择商家类型" style="width: 100%">
            <el-option
              v-for="t in shopTypes"
              :key="t.id"
              :label="t.name"
              :value="t.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商家名称" required>
          <el-input
            v-model="form.name"
            placeholder="请输入商家名称"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="地点" required>
          <el-input
            v-model="form.address"
            placeholder="请输入商家地址"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="单价 (元/人)" required>
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="商家图片" required>
          <ImageUploader v-model="form.images" :limit="6" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交注册
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getShopTypes, registerShop } from '@/api/shop'
import { useUserStore } from '@/stores/user'
import type { ShopType } from '@/types'
import ImageUploader from '@/components/ImageUploader.vue'

const router = useRouter()
const userStore = useUserStore()

const shopTypes = ref<ShopType[]>([])

const form = reactive({
  typeId: null as number | null,
  name: '',
  address: '',
  price: 0,
  images: [] as string[],
})

const submitting = ref(false)

onMounted(async () => {
  if (userStore.isMerchant) {
    ElMessage.warning('您已经是商家了')
    router.push('/shop-manage')
    return
  }
  try {
    shopTypes.value = await getShopTypes()
  } catch { /* ignore */ }
})

async function handleSubmit() {
  if (!form.typeId) {
    ElMessage.warning('请选择商家类型')
    return
  }
  if (!form.name.trim()) {
    ElMessage.warning('请输入商家名称')
    return
  }
  if (!form.address.trim()) {
    ElMessage.warning('请输入商家地址')
    return
  }
  if (form.images.length === 0) {
    ElMessage.warning('请上传商家图片')
    return
  }

  submitting.value = true
  try {
    await registerShop({
      typeId: form.typeId,
      name: form.name,
      address: form.address,
      price: form.price,
      images: form.images.join(','),
    })
    ElMessage.success('注册成功！')
    // 刷新用户信息
    await userStore.fetchUser()
    router.push('/shop-manage')
  } catch { /* ignore */ } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
}
</style>
