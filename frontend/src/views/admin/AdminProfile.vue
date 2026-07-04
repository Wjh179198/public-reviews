<template>
  <div class="page-container">
    <div class="card-wrapper" style="max-width: 500px">
      <h3 class="edit-title">编辑个人信息</h3>

      <el-form label-position="top" size="large">
        <el-form-item label="用户名">
          <el-input v-model="form.name" placeholder="请输入用户名" maxlength="20" />
        </el-form-item>

        <el-form-item label="新密码（留空则不修改）">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认新密码">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import { updateAdminInfo } from '@/api/admin'

const adminStore = useAdminStore()

const form = reactive({
  name: '',
  password: '',
  confirmPassword: '',
})

const saving = ref(false)

onMounted(() => {
  form.name = adminStore.adminInfo?.name || ''
})

async function handleSave() {
  if (form.password && form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (!form.name.trim()) {
    ElMessage.warning('用户名不能为空')
    return
  }

  saving.value = true
  try {
    const payload: { name?: string; password?: string } = { name: form.name }
    if (form.password) {
      payload.password = form.password
    }
    await updateAdminInfo(payload)
    ElMessage.success('修改成功')
    form.password = ''
    form.confirmPassword = ''
    await adminStore.fetchAdmin()
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
</style>
