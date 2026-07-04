<template>
  <div class="auth-wrapper">
    <div class="auth-card">
      <h2 class="auth-title">大众点评</h2>
      <p class="auth-subtitle">欢迎回来</p>

      <el-tabs v-model="loginMode" class="auth-tabs">
        <el-tab-pane label="验证码登录" name="code" />
        <el-tab-pane label="密码登录" name="password" />
      </el-tabs>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item v-if="loginMode === 'password'" label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item v-else label="验证码" prop="code">
          <div class="code-row">
            <el-input
              v-model="form.code"
              placeholder="请输入验证码"
              maxlength="6"
            />
            <el-button
              :disabled="codeCountdown > 0 || !isValidPhone(form.phone)"
              @click="handleSendCode"
              class="code-btn"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { isValidPhone } from '@/utils'

const router = useRouter()
const userStore = useUserStore()

const loginMode = ref<'code' | 'password'>('code')
const loading = ref(false)
const codeCountdown = ref(0)
const formRef = ref<FormInstance>()

const form = reactive({
  phone: '',
  password: '',
  code: '',
})

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: (_rule, value, cb) => {
        if (!isValidPhone(value)) {
          cb(new Error('请输入正确的手机号'))
        } else {
          cb()
        }
      }, trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ],
}

let countdownTimer: ReturnType<typeof setInterval> | null = null

function startCountdown() {
  codeCountdown.value = 60
  countdownTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0 && countdownTimer) {
      clearInterval(countdownTimer)
    }
  }, 1000)
}

async function handleSendCode() {
  if (!isValidPhone(form.phone)) {
    return
  }
  try {
    await userStore.sendCodeAction(form.phone)
    startCountdown()
  } catch {
    // 错误已在拦截器中处理
  }
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (loginMode.value === 'password') {
      await userStore.loginWithPassword({
        phone: form.phone,
        password: form.password,
      })
    } else {
      await userStore.loginWithCode({
        phone: form.phone,
        code: form.code,
      })
    }
    router.push('/shops')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrapper {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.auth-title {
  text-align: center;
  font-size: 28px;
  color: #303133;
  margin-bottom: 4px;
}

.auth-subtitle {
  text-align: center;
  color: #909399;
  margin-bottom: 20px;
}

.auth-tabs {
  margin-bottom: 10px;
}
.auth-tabs :deep(.el-tabs__header) {
  margin-bottom: 10px;
}
.auth-tabs :deep(.el-tabs__nav) {
  width: 100%;
  display: flex;
}
.auth-tabs :deep(.el-tabs__item) {
  flex: 1;
  text-align: center;
}

.code-row {
  display: flex;
  gap: 12px;
}

.code-row .el-input {
  flex: 1;
}

.code-btn {
  width: 120px;
  white-space: nowrap;
}

.submit-btn {
  width: 100%;
}

.auth-footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.auth-footer .link {
  color: #409eff;
  margin-left: 4px;
}
</style>
