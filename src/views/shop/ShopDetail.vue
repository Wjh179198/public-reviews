<template>
  <div class="page-container">
    <!-- 商家头部 -->
    <div class="shop-header card-wrapper">
      <div class="shop-header-top">
        <div class="shop-cover">
          <img :src="coverImage" :alt="shop?.name" />
        </div>
        <div class="shop-header-info">
          <h2>{{ shop?.name }}</h2>
          <div class="info-tags">
            <el-tag>{{ shop?.typeName }}</el-tag>
            <StarRating :score="shop?.score || 0" :size="18" />
            <span class="info-sold">已售 {{ shop?.sold || 0 }}</span>
          </div>
          <div class="info-detail">
            <p><el-icon><Location /></el-icon> {{ shop?.address }}</p>
            <p class="shop-price-tag"><el-icon><Money /></el-icon> 人均 ¥{{ shop?.price }}</p>
          </div>
          <!-- 预订 -->
          <div class="booking-area">
            <el-button type="primary" @click="showBookDialog = true">
              立即预订
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="tab-section card-wrapper">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 商家信息 -->
        <el-tab-pane label="商家信息" name="info">
          <div class="info-content">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="商家名称">{{ shop?.name }}</el-descriptions-item>
              <el-descriptions-item label="商家类型">{{ shop?.typeName }}</el-descriptions-item>
              <el-descriptions-item label="地址">{{ shop?.address }}</el-descriptions-item>
              <el-descriptions-item label="人均价格">¥{{ shop?.price }}</el-descriptions-item>
              <el-descriptions-item label="评分">
                <StarRating :score="shop?.score || 0" :size="14" />
              </el-descriptions-item>
              <el-descriptions-item label="销量">{{ shop?.sold }}</el-descriptions-item>
            </el-descriptions>

            <!-- 优惠卷 -->
            <div class="voucher-section">
              <h3 class="section-title">优惠卷</h3>
              <div v-if="vouchers.length" class="voucher-list">
                <VoucherCard
                  v-for="v in vouchers"
                  :key="v.id"
                  :voucher="v"
                  :purchased="purchasedVoucherIds.includes(v.id)"
                  @grab="handleGrabVoucher"
                />
              </div>
              <el-empty v-else description="暂无优惠卷" :image-size="60" />
            </div>
          </div>
        </el-tab-pane>

        <!-- 用户评价 -->
        <el-tab-pane :label="`用户评价 (${commentTotal})`" name="reviews">
          <div class="review-filters">
            <el-radio-group v-model="commentFilter" @change="fetchComments">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="good">好评</el-radio-button>
              <el-radio-button label="mid">中评</el-radio-button>
              <el-radio-button label="bad">差评</el-radio-button>
            </el-radio-group>
          </div>

          <div v-loading="commentLoading" class="review-list">
            <div
              v-for="c in comments"
              :key="c.id"
              class="review-item"
            >
              <div class="review-header">
                <UserAvatar
                  :user-id="c.userId"
                  :image="c.userImage || ''"
                  :size="40"
                />
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
                <span
                  class="like-btn"
                  :class="{ 'liked': likedCommentIds.has(c.id) }"
                  @click="handleLikeComment(c)"
                >
                  <el-icon><CaretTop /></el-icon>
                  {{ c.likes }}
                </span>
              </div>
            </div>
            <el-empty v-if="!commentLoading && !comments.length" description="暂无评价" />
          </div>

          <div v-if="commentTotal > 0" class="pagination-area">
            <el-pagination
              v-model:current-page="commentPage"
              :page-size="10"
              :total="commentTotal"
              background
              layout="prev, pager, next"
              @change="fetchComments"
            />
          </div>
        </el-tab-pane>

        <!-- 写评价 -->
        <el-tab-pane label="写评价" name="write">
          <div class="write-review-area">
            <el-form label-position="top" size="large">
              <el-form-item label="评分">
                <StarRating v-model="reviewScore" :interactive="true" :size="24" />
              </el-form-item>
              <el-form-item label="评价内容">
                <el-input
                  v-model="reviewContent"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入您的评价..."
                />
              </el-form-item>
              <el-form-item label="上传图片">
                <ImageUploader v-model="reviewImages" :limit="6" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="submitting" @click="handleSubmitReview">
                  发表评价
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 预订弹窗 -->
    <el-dialog v-model="showBookDialog" title="确认预订" width="420px">
      <div class="book-dialog">
        <p class="book-shop-name">{{ shop?.name }}</p>
        <p>单价：<span class="price">¥{{ shop?.price }}/人</span></p>
        <div v-if="myVouchers.length" class="book-voucher-select">
          <p>选择优惠卷（可选）：</p>
          <el-select v-model="selectedVoucherId" placeholder="不使用优惠卷" clearable>
            <el-option
              v-for="v in myVouchers"
              :key="v.id"
              :label="`¥${v.voucherValue} 优惠卷`"
              :value="v.id"
            />
          </el-select>
        </div>
        <p class="book-total">
          实付：<span class="price">¥{{ finalPrice }}</span>
        </p>
      </div>
      <template #footer>
        <el-button @click="showBookDialog = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="userStore.userInfo?.money! < finalPrice"
          :loading="booking"
          @click="handleBook"
        >
          确认预订
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Shop, Voucher, Comment, VoucherOrder } from '@/types'
import { getShopDetail } from '@/api/shop'
import { getShopVouchers, grabVoucher, getUserVouchers } from '@/api/voucher'
import { getComments, createComment, likeComment, checkCommentLikeStatus } from '@/api/comment'
import { createOrder } from '@/api/order'
import { useUserStore } from '@/stores/user'
import { formatTime, parseImages } from '@/utils'
import { CaretTop } from '@element-plus/icons-vue'
import StarRating from '@/components/StarRating.vue'
import VoucherCard from '@/components/VoucherCard.vue'
import ImageUploader from '@/components/ImageUploader.vue'
import UserAvatar from '@/components/UserAvatar.vue'

const route = useRoute()
const userStore = useUserStore()

const shopId = computed(() => Number(route.params.id))

// 商家信息
const shop = ref<Shop | null>(null)
const vouchers = ref<Voucher[]>([])
const purchasedVoucherIds = ref<number[]>([])

const coverImage = computed(() => {
  const imgs = parseImages(shop.value?.images || '')
  return imgs[0] || 'https://via.placeholder.com/600x300?text=暂无图片'
})

// 标签页
const activeTab = ref('info')

// 评价相关
const comments = ref<Comment[]>([])
const likedCommentIds = ref<Set<number>>(new Set())
const commentFilter = ref('all')
const commentPage = ref(1)
const commentTotal = ref(0)
const commentLoading = ref(false)

// 写评价
const reviewScore = ref(0)
const reviewContent = ref('')
const reviewImages = ref<string[]>([])
const submitting = ref(false)

// 预订
const showBookDialog = ref(false)
const myVouchers = ref<VoucherOrder[]>([])
const selectedVoucherId = ref<number | undefined>(undefined)
const booking = ref(false)

const finalPrice = computed(() => {
  let price = shop.value?.price || 0
  if (selectedVoucherId.value) {
    const v = myVouchers.value.find((vo) => vo.id === selectedVoucherId.value)
    if (v?.voucherValue) {
      price = Math.max(0, price - v.voucherValue)
    }
  }
  return price
})

// ===== 数据加载 =====
onMounted(async () => {
  await fetchShopDetail()
  await fetchVouchers()
  await fetchMyVouchers()
  await fetchComments()
})

async function fetchShopDetail() {
  try {
    shop.value = await getShopDetail(shopId.value)
  } catch { /* ignore */ }
}

async function fetchVouchers() {
  try {
    vouchers.value = await getShopVouchers(shopId.value)
  } catch { /* ignore */ }
}

async function fetchMyVouchers() {
  if (!userStore.userInfo) return
  try {
    const list = await getUserVouchers(userStore.userInfo.id)
    myVouchers.value = list.filter((v) => v.status === 0) // 未使用的
  } catch { /* ignore */ }
}

async function handleTabChange(tab: string) {
  if (tab === 'reviews') {
    commentPage.value = 1
    await fetchComments()
  } else if (tab === 'write') {
    // reset form
  }
}

async function fetchComments() {
  commentLoading.value = true
  try {
    const result = await getComments({
      shopId: shopId.value,
      filter: commentFilter.value as any,
      page: commentPage.value,
      pageSize: 10,
    })
    // 防御：后端无数据时可能返回 null
    comments.value = result?.records || []
    commentTotal.value = result?.total || 0
    // 批量检查点赞状态
    const ids = comments.value.map(c => c.id)
    if (ids.length > 0) {
      const results = await Promise.allSettled(
        ids.map(id => checkCommentLikeStatus(id))
      )
      const set = new Set<number>()
      results.forEach((r, i) => {
        if (r.status === 'fulfilled' && r.value) set.add(ids[i])
      })
      likedCommentIds.value = set
    } else {
      likedCommentIds.value = new Set()
    }
  } catch { /* ignore */ } finally {
    commentLoading.value = false
  }
}

// ===== 优惠卷抢购 =====
async function handleGrabVoucher(voucher: Voucher) {
  try {
    await ElMessageBox.confirm(
      `确认以 ¥${voucher.price} 抢购 ¥${voucher.value} 优惠卷？`,
      '抢购优惠卷',
      { confirmButtonText: '确认抢购', cancelButtonText: '取消', type: 'warning' }
    )
    await grabVoucher(voucher.id)
    ElMessage.success('抢购成功！')
    purchasedVoucherIds.value.push(voucher.id)
    await fetchMyVouchers()
    await fetchVouchers()
  } catch {
    // 取消或其他错误
  }
}

// ===== 评价点赞 =====
async function handleLikeComment(comment: Comment) {
  try {
    await likeComment(comment.id)
    const newSet = new Set(likedCommentIds.value)
    if (newSet.has(comment.id)) {
      comment.likes--
      newSet.delete(comment.id)
    } else {
      comment.likes++
      newSet.add(comment.id)
    }
    likedCommentIds.value = newSet
  } catch { /* ignore */ }
}

// ===== 发表评价 =====
async function handleSubmitReview() {
  if (!reviewScore.value) {
    ElMessage.warning('请选择评分')
    return
  }
  if (!reviewContent.value.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  submitting.value = true
  try {
    await createComment({
      shopId: shopId.value,
      content: reviewContent.value,
      score: reviewScore.value,
      images: reviewImages.value.join(','),
    })
    ElMessage.success('评价发表成功')
    reviewScore.value = 0
    reviewContent.value = ''
    reviewImages.value = []
    activeTab.value = 'reviews'
    commentPage.value = 1
    await fetchComments()
  } catch { /* ignore */ } finally {
    submitting.value = false
  }
}

// ===== 预订 =====
async function handleBook() {
  if ((userStore.userInfo?.money || 0) < finalPrice.value) {
    ElMessage.error('余额不足')
    return
  }
  booking.value = true
  try {
    await createOrder({
      shopId: shopId.value,
      voucherId: selectedVoucherId.value || undefined,
    })
    ElMessage.success('预订成功')
    showBookDialog.value = false
    selectedVoucherId.value = undefined
    // 刷新用户信息（余额）
    await userStore.fetchUser()
  } catch { /* ignore */ } finally {
    booking.value = false
  }
}
</script>

<style scoped>
.shop-header {
  margin-bottom: 20px;
}

.shop-header-top {
  display: flex;
  gap: 24px;
}

.shop-cover {
  width: 400px;
  height: 220px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.shop-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shop-header-info h2 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.info-tags {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-sold {
  font-size: 13px;
  color: #909399;
}

.info-detail p {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.shop-price-tag {
  color: #f56c6c;
  font-weight: bold;
}

.booking-area {
  margin-top: auto;
}

/* 标签页 */
.tab-section {
  min-height: 400px;
}

/* 商家信息 */
.info-content {
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin: 24px 0 16px;
}

.voucher-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 评价 */
.review-filters {
  margin-bottom: 16px;
}

.review-list {
  min-height: 200px;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.review-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.review-username {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.review-time {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}

.review-body {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
}

.review-images {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.review-thumb {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
}

.review-footer {
  display: flex;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  cursor: pointer;
  user-select: none;
}

.like-btn:hover {
  color: #409eff;
}

.like-btn.liked {
  color: #409eff;
}

/* 写评价 */
.write-review-area {
  max-width: 600px;
}

/* 预订弹窗 */
.book-dialog p {
  margin-bottom: 12px;
  font-size: 14px;
}

.book-shop-name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.book-dialog .price {
  color: #f56c6c;
  font-weight: bold;
}

.book-voucher-select {
  margin: 12px 0;
}

.book-total {
  font-size: 16px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
