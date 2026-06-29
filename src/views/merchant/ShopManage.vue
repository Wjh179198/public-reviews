<template>
  <div class="page-container">
    <h2 class="page-title">店铺管理</h2>

    <!-- 未注册商家 -->
    <div v-if="!userStore.isMerchant" class="card-wrapper no-shop">
      <el-result icon="info" title="您未注册为商家" sub-title="注册成为商家，管理您的店铺">
        <template #extra>
          <el-button type="primary" @click="$router.push('/register-shop')">
            立即注册
          </el-button>
        </template>
      </el-result>
    </div>

    <!-- 已注册商家 -->
    <template v-else>
      <!-- 营业额图表 -->
      <div class="card-wrapper">
        <h3 class="section-title">近7天营业额</h3>
        <div ref="chartRef" class="chart-container" />
      </div>

      <!-- 发布优惠卷 -->
      <div class="card-wrapper">
        <h3 class="section-title">发布优惠卷</h3>
        <el-form label-position="top" size="default" style="max-width: 600px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="价格 (元)">
                <el-input-number
                  v-model="voucherForm.price"
                  :min="0"
                  :precision="2"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="优惠金额 (元)">
                <el-input-number
                  v-model="voucherForm.value"
                  :min="1"
                  :precision="2"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="开始时间">
                <el-date-picker
                  v-model="voucherForm.beginTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束时间">
                <el-date-picker
                  v-model="voucherForm.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="库存">
            <el-input-number
              v-model="voucherForm.stock"
              :min="1"
              :max="99999"
              style="width: 200px"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="pubLoading" @click="handlePublishVoucher">
              发布优惠卷
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 已有优惠卷 -->
      <div class="card-wrapper">
        <h3 class="section-title">我的优惠卷</h3>
        <div v-if="manageVouchers.length" class="voucher-list">
          <VoucherCard
            v-for="v in manageVouchers"
            :key="v.id"
            :voucher="v"
          />
        </div>
        <el-empty v-else description="暂无优惠卷" :image-size="60" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'
import { getShopRevenue } from '@/api/shop'
import { createVoucher, getManageVouchers } from '@/api/voucher'
import type { DailyRevenue, Voucher } from '@/types'
import VoucherCard from '@/components/VoucherCard.vue'

const userStore = useUserStore()

// 图表
const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

// 优惠卷
const manageVouchers = ref<Voucher[]>([])
const pubLoading = ref(false)

const voucherForm = reactive({
  price: 0,
  value: 0,
  beginTime: '',
  endTime: '',
  stock: 100,
})

async function fetchRevenue() {
  if (!userStore.userInfo?.shopId) return
  try {
    const data: DailyRevenue[] = await getShopRevenue(userStore.userInfo.shopId)
    await nextTick()
    renderChart(data)
  } catch { /* ignore */ }
}

function renderChart(data: DailyRevenue[]) {
  if (!chartRef.value) return
  if (chartInstance) {
    chartInstance.dispose()
  }
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.date),
    },
    yAxis: {
      type: 'value',
      name: '营业额 (元)',
    },
    series: [
      {
        name: '营业额',
        type: 'bar',
        data: data.map((d) => d.amount),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#79bbff' },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
      },
    ],
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
  })
}

async function fetchVouchers() {
  if (!userStore.userInfo?.shopId) return
  try {
    manageVouchers.value = await getManageVouchers(userStore.userInfo.shopId)
  } catch { /* ignore */ }
}

async function handlePublishVoucher() {
  if (!voucherForm.price || !voucherForm.value) {
    ElMessage.warning('请填写价格和优惠金额')
    return
  }
  if (!voucherForm.beginTime || !voucherForm.endTime) {
    ElMessage.warning('请选择有效期')
    return
  }
  if (new Date(voucherForm.endTime) <= new Date(voucherForm.beginTime)) {
    ElMessage.warning('结束时间必须大于开始时间')
    return
  }
  pubLoading.value = true
  try {
    await createVoucher({
      price: voucherForm.price,
      value: voucherForm.value,
      beginTime: voucherForm.beginTime,
      endTime: voucherForm.endTime,
      stock: voucherForm.stock,
    })
    ElMessage.success('优惠卷发布成功')
    voucherForm.price = 0
    voucherForm.value = 0
    voucherForm.beginTime = ''
    voucherForm.endTime = ''
    voucherForm.stock = 100
    await fetchVouchers()
  } catch { /* ignore */ } finally {
    pubLoading.value = false
  }
}

onMounted(() => {
  if (userStore.isMerchant) {
    fetchRevenue()
    fetchVouchers()
  }
})
</script>

<style scoped>
.page-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 16px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.no-shop {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.voucher-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
