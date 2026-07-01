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
          <el-cascader
            v-model="selectedRegion"
            :options="chinaRegions"
            :props="{ value: 'value', label: 'label', children: 'children' }"
            placeholder="请选择省/市"
            clearable
            filterable
            style="width: 100%"
          />
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
import chinaRegions from '@/data/china-regions'
import { watch } from 'vue'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  name: '',
  phone: '',
  address: '',
  image: '',
  images: [] as string[],
})

// 级联选择器的值，如 ['广东', '深圳']
const selectedRegion = ref<string[]>([])

const saving = ref(false)

onMounted(() => {
  const u = userStore.userInfo
  if (u) {
    form.name = u.name
    form.phone = u.phone
    form.image = u.image || ''

    // 从存储的地址字符串还原为级联数组
    if (u.address) {
      const parts = u.address.split(/\s+/).filter(Boolean)
      if (parts.length >= 2) {
        // 检查省名是否匹配
        const province = chinaRegions.find(
          (r) => r.label === parts[0] || r.value === parts[0]
        )
        if (province) {
          selectedRegion.value = [province.value, parts[1] || parts[0]]
        }
      } else if (parts.length === 1) {
        selectedRegion.value = [parts[0]]
      }
    }
  }
})

// 级联选择变化时同步到 form.address
watch(selectedRegion, (val) => {
  if (val && val.length > 0) {
    // 找到对应的 label 显示名，存储为 "广东省 深圳市" 格式
    const province = chinaRegions.find((r) => r.value === val[0])
    const provinceLabel = province?.label || val[0]
    let address = provinceLabel
    if (val.length >= 2) {
      const city = province?.children?.find((c) => c.value === val[1])
      address += ' ' + (city?.label || val[1])
    }
    form.address = address
  } else {
    form.address = ''
  }
})

// 图片上传
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
