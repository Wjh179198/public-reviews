<template>
  <div class="page-container">
    <div class="card-wrapper" style="max-width: 500px">
      <h3 class="edit-title">编辑个人信息</h3>

      <el-form label-position="top" size="large">
        <el-form-item label="头像">
          <div class="avatar-row">
            <el-avatar :size="80" :src="form.image || undefined">
              <el-icon :size="40"><UserFilled /></el-icon>
            </el-avatar>
            <ImageUploader
              v-model="form.images"
              :limit="1"
            />
          </div>
          <span v-if="form.image" class="hint">
            当前头像链接：{{ form.image }}
          </span>
        </el-form-item>

        <el-form-item label="昵称">
          <el-input v-model="form.name" placeholder="请输入昵称" maxlength="20" />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" disabled />
        </el-form-item>

        <el-form-item label="地区">
          <el-input v-model="form.address" placeholder="请输入地区" maxlength="50" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { updateUserInfo } from '@/api/user'
import ImageUploader from '@/components/ImageUploader.vue'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  name: '',
  phone: '',
  address: '',
  image: '',
  images: [] as string[],
})

const saving = ref(false)

onMounted(() => {
  const u = userStore.userInfo
  if (u) {
    form.name = u.name
    form.phone = u.phone
    form.address = u.address || ''
    form.image = u.image || ''
  }
})

// watch image upload
import { watch } from 'vue'
watch(() => form.images, (val) => {
  if (val.length > 0) {
    form.image = val[val.length - 1]
  }
})

async function handleSave() {
  saving.value = true
  try {
    const updated = await updateUserInfo({
      name: form.name,
      address: form.address,
      image: form.image,
    })
    userStore.updateUser(updated)
    ElMessage.success('保存成功')
    router.back()
  } catch { /* ignore */ } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.edit-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hint {
  font-size: 12px;
  color: #909399;
  display: block;
  margin-top: 4px;
}
</style>
