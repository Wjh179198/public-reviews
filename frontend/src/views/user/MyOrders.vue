<template>
  <div class="page-container">
    <h2 class="page-title">我的订单</h2>

    <el-tabs v-model="activeTab" @tab-change="fetchOrders">
      <el-tab-pane label="支付成功" name="1" />
      <el-tab-pane label="已退款" name="2" />
    </el-tabs>

    <div v-loading="loading" class="order-list">
      <template v-if="orders.length">
        <div v-for="order in orders" :key="order.id" class="order-card card-wrapper">
          <div class="order-header">
            <span class="order-shop">{{ order.shopName }}</span>
            <el-tag :type="order.status === 1 ? 'success' : 'info'">
              {{ order.status === 1 ? '支付成功' : '已退款' }}
            </el-tag>
          </div>
          <div class="order-body">
            <p>交易金额：<span class="price">¥{{ order.price?.toFixed(2) }}</span></p>
            <p v-if="order.voucherValue">
              优惠卷抵扣：<span class="discount">-¥{{ order.voucherValue?.toFixed(2) }}</span>
            </p>
            <p class="order-time">下单时间：{{ formatTime(order.orderTime) }}</p>
          </div>
          <div v-if="order.status === 1" class="order-footer">
            <el-button
              type="warning"
              size="small"
              @click="handleRefund(order.id)"
            >
              申请退款
            </el-button>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无订单" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserOrders, refundOrder } from '@/api/order'
import { formatTime } from '@/utils'
import type { ShopOrder } from '@/types'

const userStore = useUserStore()

const activeTab = ref('1')
const orders = ref<ShopOrder[]>([])
const loading = ref(false)

async function fetchOrders() {
  loading.value = true
  try {
    const result = await getUserOrders(
      userStore.userInfo!.id,
      Number(activeTab.value),
      1,
      100
    )
    orders.value = result.records
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

async function handleRefund(orderId: number) {
  try {
    await ElMessageBox.confirm('确定要申请退款吗？', '退款确认', { type: 'warning' })
    await refundOrder(orderId)
    ElMessage.success('退款成功')
    await fetchOrders()
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.page-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.order-list {
  min-height: 300px;
}

.order-card {
  margin-bottom: 12px;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.order-shop {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.order-body p {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.order-body .price {
  color: #f56c6c;
  font-weight: bold;
}

.order-body .discount {
  color: #67c23a;
}

.order-time {
  font-size: 12px;
  color: #c0c4cc;
}

.order-footer {
  margin-top: 12px;
  text-align: right;
}
</style>
