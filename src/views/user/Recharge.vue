<template>
  <div class="page-container">
    <div class="card-wrapper" style="max-width: 500px">
      <h3 class="recharge-title">余额充值</h3>
      <p class="current-balance">
        当前余额：<span class="amount">¥{{ userStore.userInfo?.money?.toFixed(2) }}</span>
      </p>

      <div class="amount-grid">
        <el-button
          v-for="amt in fixedAmounts"
          :key="amt"
          :type="selectedAmount === amt ? 'primary' : 'default'"
          size="large"
          class="amount-btn"
          @click="selectedAmount = amt"
        >
          ¥{{ amt }}
        </el-button>
      </div>

      <div class="custom-row">
        <span class="custom-label">自定义金额：</span>
        <el-input-number
          v-model="customAmount"
          :min="1"
          :max="10000"
          :step="10"
          style="width: 200px"
        />
        <el-button @click="selectedAmount = customAmount">确认</el-button>
      </div>

      <div class="selected-display" v-if="selectedAmount">
        <p>充值金额：<span class="amount">¥{{ selectedAmount }}</span></p>
        <el-button
          type="primary"
          size="large"
          :loading="recharging"
          @click="handleRecharge"
          style="width: 100%"
        >
          确认充值
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { recharge } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const fixedAmounts = [10, 20, 50, 100]
const selectedAmount = ref<number | null>(null)
const customAmount = ref(100)
const recharging = ref(false)

async function handleRecharge() {
  if (!selectedAmount.value) return
  recharging.value = true
  try {
    await recharge({ amount: selectedAmount.value })
    ElMessage.success(`充值 ¥${selectedAmount.value} 成功！`)
    // 刷新用户信息
    await userStore.fetchUser()
    router.back()
  } catch { /* ignore */ } finally {
    recharging.value = false
  }
}
</script>

<style scoped>
.recharge-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 8px;
}

.current-balance {
  font-size: 15px;
  color: #606266;
  margin-bottom: 24px;
}

.current-balance .amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.amount-grid {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.amount-btn {
  width: 100px;
  height: 48px;
  font-size: 18px;
}

.custom-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.custom-label {
  font-size: 14px;
  color: #606266;
}

.selected-display {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.selected-display p {
  font-size: 16px;
  margin-bottom: 16px;
}

.selected-display .amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 24px;
}
</style>
