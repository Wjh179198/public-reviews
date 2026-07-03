<template>
  <div class="page-container">
    <h2 class="page-title">我的店铺</h2>

    <!-- 未注册商家 -->
    <div v-if="!userStore.isMerchant" class="card-wrapper no-shop">
      <el-result icon="info" title="您未注册为商户" sub-title="注册成为商家，管理您的店铺">
        <template #extra>
          <el-button type="primary" @click="$router.push('/register-shop')">
            立即注册
          </el-button>
        </template>
      </el-result>
    </div>

    <!-- 已注册商家 -->
    <template v-else>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ===== 店铺信息 ===== -->
        <el-tab-pane label="店铺信息" name="info">
          <!-- 店铺基本信息 -->
          <div v-if="shop" class="card-wrapper">
            <div class="shop-header">
              <div class="shop-images" v-if="shop.images">
                <el-image
                  v-for="(img, idx) in parseImages(shop.images)"
                  :key="idx"
                  :src="img"
                  :preview-src-list="parseImages(shop.images)"
                  :initial-index="idx"
                  fit="cover"
                  class="shop-img"
                />
              </div>
              <div class="shop-info-main">
                <h3 class="shop-name">{{ shop.name }}</h3>
                <p class="shop-type">
                  <el-tag size="small" type="warning">{{ shop.typeName }}</el-tag>
                </p>
                <p class="shop-addr">{{ shop.address }}</p>
                <div class="shop-stats">
                  <span>人均 ¥{{ shop.price?.toFixed(2) }}</span>
                  <span class="stat-divider">|</span>
                  <span><StarRating :score="shop.score" :size="14" /> {{ shop.score?.toFixed(1) }}</span>
                  <span class="stat-divider">|</span>
                  <span>{{ shop.comments }} 条评价</span>
                  <span class="stat-divider">|</span>
                  <span>已售 {{ shop.sold }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 用户评价 -->
          <div class="card-wrapper">
            <h3 class="section-title">用户评价 ({{ commentTotal }})</h3>
            <div class="review-filters">
              <el-radio-group v-model="commentFilter" @change="fetchComments">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="good">好评</el-radio-button>
                <el-radio-button label="mid">中评</el-radio-button>
                <el-radio-button label="bad">差评</el-radio-button>
              </el-radio-group>
            </div>
            <div v-loading="commentLoading" class="review-list">
              <div v-for="c in comments" :key="c.id" class="review-item">
                <div class="review-header">
                  <UserAvatar :user-id="c.userId" :image="c.userImage || ''" :size="40" />
                  <div class="review-user-info">
                    <span class="review-username">{{ c.userName }}</span>
                    <StarRating :score="c.score" :size="12" />
                  </div>
                  <span class="review-time">{{ formatTime(c.createTime) }}</span>
                </div>
                <div class="review-body">{{ c.content }}</div>
                <div v-if="c.images" class="review-images">
                  <el-image
                    v-for="(img, idx) in parseImages(c.images)"
                    :key="idx"
                    :src="img"
                    :preview-src-list="parseImages(c.images)"
                    :initial-index="idx"
                    fit="cover"
                    class="review-thumb"
                  />
                </div>
                <div class="review-footer">
                  <span class="like-btn" :class="{ 'liked': likedCommentIds.has(c.id) }" @click="handleLikeComment(c)">
                    <el-icon><CaretTop /></el-icon>
                    {{ c.likes }}
                  </span>
                </div>
              </div>
              <el-empty v-if="!commentLoading && !comments.length" description="暂无评价" />
            </div>
            <div v-if="commentTotal > 10" class="pagination-area">
              <el-pagination
                v-model:current-page="commentPage"
                :page-size="10"
                :total="commentTotal"
                background
                layout="prev, pager, next"
                @change="fetchComments"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- ===== 营业额统计 ===== -->
        <el-tab-pane label="营业额统计" name="revenue">
          <div class="card-wrapper">
            <h3 class="section-title">近7天营业额</h3>
            <div ref="chartRef" class="chart-container" />
          </div>
        </el-tab-pane>

        <!-- ===== 优惠卷 ===== -->
        <el-tab-pane label="优惠卷" name="vouchers">
          <div class="card-wrapper">
            <h3 class="section-title">发布优惠卷</h3>
            <el-form label-position="top" size="default" style="max-width: 600px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="价格 (元)">
                    <el-input-number v-model="voucherForm.price" :min="0" :precision="2" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="优惠金额 (元)">
                    <el-input-number v-model="voucherForm.value" :min="1" :precision="2" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="开始时间">
                    <el-date-picker v-model="voucherForm.beginTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="结束时间">
                    <el-date-picker v-model="voucherForm.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="库存">
                <el-input-number v-model="voucherForm.stock" :min="1" :max="99999" style="width: 200px" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="pubLoading" @click="handlePublishVoucher">发布优惠卷</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="card-wrapper">
            <h3 class="section-title">我的优惠卷</h3>
            <div v-if="manageVouchers.length" class="voucher-list">
              <VoucherCard v-for="v in manageVouchers" :key="v.id" :voucher="v" />
            </div>
            <el-empty v-else description="暂无优惠卷" :image-size="60" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { CaretTop } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getShopDetail, getShopRevenue } from '@/api/shop'
import { createVoucher, getManageVouchers } from '@/api/voucher'
import { getComments, likeComment, checkCommentLikeStatus } from '@/api/comment'
import { formatTime, parseImages } from '@/utils'
import type { DailyRevenue, Voucher, Shop, Comment } from '@/types'
import VoucherCard from '@/components/VoucherCard.vue'
import StarRating from '@/components/StarRating.vue'
import UserAvatar from '@/components/UserAvatar.vue'

const userStore = useUserStore()

const activeTab = ref('info')

// 店铺信息
const shop = ref<Shop | null>(null)

// 图表
const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

// 评论
const comments = ref<Comment[]>([])
const likedCommentIds = ref<Set<number>>(new Set())
const commentFilter = ref('all')
const commentPage = ref(1)
const commentTotal = ref(0)
const commentLoading = ref(false)

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

async function fetchShopInfo() {
  if (!userStore.userInfo?.shopId) return
  try {
    shop.value = await getShopDetail(userStore.userInfo.shopId)
  } catch { /* ignore */ }
}

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
  if (chartInstance) { chartInstance.dispose() }
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map((d) => d.date) },
    yAxis: { type: 'value', name: '营业额 (元)' },
    series: [{
      name: '营业额', type: 'bar',
      data: data.map((d) => d.amount),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409eff' }, { offset: 1, color: '#79bbff' },
        ]),
        borderRadius: [4, 4, 0, 0],
      },
    }],
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  })
}

async function fetchComments() {
  if (!userStore.userInfo?.shopId) return
  commentLoading.value = true
  try {
    const result = await getComments({
      shopId: userStore.userInfo.shopId,
      filter: commentFilter.value as any,
      page: commentPage.value,
      pageSize: 10,
    })
    comments.value = result?.records || []
    commentTotal.value = result?.total || 0
    const ids = comments.value.map(c => c.id)
    if (ids.length > 0) {
      const results = await Promise.allSettled(ids.map(id => checkCommentLikeStatus(id)))
      const set = new Set<number>()
      results.forEach((r, i) => { if (r.status === 'fulfilled' && r.value) set.add(ids[i]) })
      likedCommentIds.value = set
    } else {
      likedCommentIds.value = new Set()
    }
  } catch { /* ignore */ } finally {
    commentLoading.value = false
  }
}

async function handleLikeComment(comment: Comment) {
  try {
    await likeComment(comment.id)
    const newSet = new Set(likedCommentIds.value)
    if (newSet.has(comment.id)) { comment.likes--; newSet.delete(comment.id) }
    else { comment.likes++; newSet.add(comment.id) }
    likedCommentIds.value = newSet
  } catch { /* ignore */ }
}

async function fetchVouchers() {
  if (!userStore.userInfo?.shopId) return
  try {
    manageVouchers.value = await getManageVouchers(userStore.userInfo.shopId)
  } catch { /* ignore */ }
}

async function handlePublishVoucher() {
  if (!voucherForm.price || !voucherForm.value) { ElMessage.warning('请填写价格和优惠金额'); return }
  if (!voucherForm.beginTime || !voucherForm.endTime) { ElMessage.warning('请选择有效期'); return }
  if (new Date(voucherForm.endTime) <= new Date(voucherForm.beginTime)) { ElMessage.warning('结束时间必须大于开始时间'); return }
  pubLoading.value = true
  try {
    await createVoucher({
      price: voucherForm.price, value: voucherForm.value,
      beginTime: voucherForm.beginTime, endTime: voucherForm.endTime, stock: voucherForm.stock,
    })
    ElMessage.success('优惠卷发布成功')
    voucherForm.price = 0; voucherForm.value = 0
    voucherForm.beginTime = ''; voucherForm.endTime = ''; voucherForm.stock = 100
    await fetchVouchers()
  } catch { /* ignore */ } finally { pubLoading.value = false }
}

function handleTabChange(tab: string) {
  if (tab === 'revenue') { nextTick(() => renderChart([])); fetchRevenue() }
  else if (tab === 'info') { if (!shop.value) { fetchShopInfo(); fetchComments() } }
  else if (tab === 'vouchers') { if (!manageVouchers.value.length) fetchVouchers() }
}

onMounted(() => {
  if (userStore.isMerchant) {
    fetchShopInfo()
    fetchComments()
  }
})
</script>

<style scoped>
.page-title { font-size: 24px; color: #303133; margin-bottom: 20px; }
.section-title { font-size: 16px; color: #303133; margin-bottom: 16px; }
.chart-container { width: 100%; height: 350px; }
.no-shop { min-height: 300px; display: flex; align-items: center; justify-content: center; }

/* 店铺信息 */
.shop-header { display: flex; gap: 20px; }
.shop-images { display: flex; gap: 8px; flex-wrap: wrap; }
.shop-img { width: 160px; height: 120px; border-radius: 8px; }
.shop-info-main { flex: 1; }
.shop-name { font-size: 22px; color: #303133; margin: 0 0 8px 0; }
.shop-type { margin-bottom: 8px; }
.shop-addr { font-size: 14px; color: #909399; margin-bottom: 12px; }
.shop-stats { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #606266; }
.stat-divider { color: #dcdfe6; }

/* 评价 */
.review-filters { margin-bottom: 12px; }
.review-list { min-height: 100px; }
.review-item { padding: 14px 0; border-bottom: 1px solid #f0f0f0; }
.review-item:last-child { border-bottom: none; }
.review-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.review-user-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.review-username { font-size: 14px; color: #303133; font-weight: 500; }
.review-time { font-size: 12px; color: #c0c4cc; }
.review-body { font-size: 14px; color: #606266; line-height: 1.5; margin-bottom: 8px; padding-left: 50px; }
.review-images { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px; padding-left: 50px; }
.review-thumb { width: 80px; height: 80px; border-radius: 4px; }
.review-footer { display: flex; padding-left: 50px; }
.like-btn { display: inline-flex; align-items: center; gap: 4px; font-size: 13px; color: #909399; cursor: pointer; user-select: none; }
.like-btn:hover { color: #409eff; }
.like-btn.liked { color: #409eff; }
.pagination-area { display: flex; justify-content: center; margin-top: 12px; }

/* 优惠卷 */
.voucher-list { display: flex; flex-direction: column; gap: 12px; }
</style>
