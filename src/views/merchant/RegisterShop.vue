<template>
  <div class="page-container">
    <div class="card-wrapper" style="max-width: 600px">
      <h3 class="page-title">注册商户</h3>

      <el-form label-position="top" size="large">
        <el-form-item label="商家类型" required>
          <el-select v-model="form.typeId" placeholder="请选择商家类型" style="width: 100%">
            <el-option
              v-for="t in shopTypes"
              :key="t.id"
              :label="t.name"
              :value="t.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商家名称" required>
          <el-input
            v-model="form.name"
            placeholder="请输入商家名称"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="所在地区" required>
          <el-cascader
            v-model="selectedRegion"
            :options="chinaRegions"
            :props="{ value: 'value', label: 'label', children: 'children' }"
            placeholder="请选择省/市/区"
            clearable
            filterable
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="详细地址" required>
          <el-input
            v-model="detailAddress"
            placeholder="街道门牌号、楼层等详细信息"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="单价 (元/人)" required>
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="商家图片" required>
          <ImageUploader v-model="form.images" :limit="6" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交注册
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getShopTypes, registerShop } from '@/api/shop'
import { useUserStore } from '@/stores/user'
import type { ShopType } from '@/types'
import ImageUploader from '@/components/ImageUploader.vue'
import chinaRegions from '@/data/china-regions'

const router = useRouter()
const userStore = useUserStore()

const shopTypes = ref<ShopType[]>([])

const selectedRegion = ref<string[]>([])
const detailAddress = ref('')

const form = reactive({
  typeId: null as number | null,
  name: '',
  address: '',
  price: 0,
  images: [] as string[],
})

const submitting = ref(false)

// 将级联选择的 value 数组转为显示用 label 字符串
function buildRegionLabel(path: string[]): string {
  if (!path || path.length === 0) return ''
  const province = chinaRegions.find((r) => r.value === path[0])
  const provinceLabel = province?.label || path[0]
  let result = provinceLabel
  if (path.length >= 2 && province?.children) {
    const city = province.children.find((c) => c.value === path[1])
    result += ' ' + (city?.label || path[1])
  }
  if (path.length >= 3 && province?.children) {
    const city = province.children.find((c) => c.value === path[1])
    if (city?.children) {
      const district = city.children.find((d) => d.value === path[2])
      result += ' ' + (district?.label || path[2])
    }
  }
  return result
}

onMounted(async () => {
  if (userStore.isMerchant) {
    ElMessage.warning('您已经是商家了')
    router.push('/shop-manage')
    return
  }
  try {
    shopTypes.value = await getShopTypes()
  } catch { /* ignore */ }
})

async function handleSubmit() {
  if (!form.typeId) {
    ElMessage.warning('请选择商家类型')
    return
  }
  if (!form.name.trim()) {
    ElMessage.warning('请输入商家名称')
    return
  }
  if (!selectedRegion.value || selectedRegion.value.length === 0) {
    ElMessage.warning('请选择所在地区')
    return
  }
  if (!detailAddress.value.trim()) {
    ElMessage.warning('请填写详细地址')
    return
  }
  if (form.images.length === 0) {
    ElMessage.warning('请上传商家图片')
    return
  }

  // 拼接：省/市/区 + 详细地址，如 "广东省 广州市 天河区 体育西路123号"
  const regionLabel = buildRegionLabel(selectedRegion.value)
  form.address = detailAddress.value.trim()
    ? regionLabel + ' ' + detailAddress.value.trim()
    : regionLabel

  submitting.value = true
  try {
    await registerShop({
      typeId: form.typeId,
      name: form.name,
      address: form.address,
      price: form.price,
      images: form.images.join(','),
    })
    ElMessage.success('注册成功！')
    await userStore.fetchUser()
    router.push('/shop-manage')
  } catch { /* ignore */ } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
}
</style>
