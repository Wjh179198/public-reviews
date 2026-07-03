<template>
  <div class="voucher-card" :class="{ disabled: isDisabled }">
    <div class="voucher-left">
      <div class="voucher-value">
        <span class="voucher-symbol">¥</span>
        <span class="voucher-amount">{{ voucher.value }}</span>
      </div>
      <div class="voucher-label">优惠卷</div>
    </div>
    <div class="voucher-right">
      <div class="voucher-info">
        <p>价格：<span class="price">¥{{ voucher.price }}</span></p>
        <p>有效期：{{ formatDate(voucher.beginTime) }} ~ {{ formatDate(voucher.endTime) }}</p>
        <p v-if="showStock">库存：{{ voucher.stock }}张</p>
        <p v-if="isDisabled" class="disabled-msg">{{ disabledMsg }}</p>
      </div>
      <el-button
        type="danger"
        size="small"
        :disabled="isDisabled"
        @click="$emit('grab', voucher)"
      >
        立即抢购
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import type { Voucher } from '@/types'
import { formatDate } from '@/utils'
import { useUserStore } from '@/stores/user'
import { checkVoucherStock } from '@/api/voucher'

const props = defineProps<{
  voucher: Voucher
  purchased?: boolean
  showStock?: boolean
}>()

defineEmits<{
  grab: [voucher: Voucher]
}>()

const userStore = useUserStore()

const now = new Date()
const hasStock = ref(true)

onMounted(async () => {
  try {
    hasStock.value = await checkVoucherStock(props.voucher.id)
  } catch { /* ignore */ }
})

const isExpired = computed(() => new Date(props.voucher.endTime) < now)
const notStarted = computed(() => new Date(props.voucher.beginTime) > now)
const noMoney = computed(() => (userStore.userInfo?.money || 0) < props.voucher.price)

const isDisabled = computed(
  () => isExpired.value || notStarted.value || !hasStock.value || noMoney.value || !!props.purchased
)

const disabledMsg = computed(() => {
  if (props.purchased) return '已购买'
  if (notStarted.value) return '活动未开始'
  if (isExpired.value) return '已过期'
  if (!hasStock.value) return '已抢光'
  if (noMoney.value) return '余额不足'
  return ''
})
</script>

<style scoped>
.voucher-card {
  display: flex;
  border: 1px solid #f56c6c;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.voucher-card.disabled {
  border-color: #dcdfe6;
  opacity: 0.7;
}

.voucher-left {
  width: 100px;
  background: #f56c6c;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 0;
}

.disabled .voucher-left {
  background: #c0c4cc;
}

.voucher-symbol {
  font-size: 14px;
}

.voucher-amount {
  font-size: 28px;
  font-weight: bold;
}

.voucher-label {
  font-size: 12px;
  margin-top: 4px;
}

.voucher-right {
  flex: 1;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.voucher-info {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}

.voucher-info .price {
  color: #f56c6c;
  font-weight: bold;
}

.disabled-msg {
  color: #f56c6c;
  font-size: 12px;
}
</style>
