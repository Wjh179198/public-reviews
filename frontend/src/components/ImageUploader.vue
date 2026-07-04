<template>
  <div class="image-uploader">
    <el-upload
      :action="uploadUrl"
      :headers="uploadHeaders"
      list-type="picture-card"
      :file-list="fileList"
      :on-success="handleSuccess"
      :on-remove="handleRemove"
      :on-preview="handlePreview"
      :before-upload="beforeUpload"
      :limit="limit"
      multiple
    >
      <el-icon><Plus /></el-icon>
    </el-upload>

    <el-dialog v-model="previewVisible" title="图片预览" width="600px">
      <img :src="previewUrl" style="width: 100%" alt="" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { UploadProps, UploadUserFile } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  modelValue?: string[]
  limit?: number
}>(), {
  modelValue: () => [],
  limit: 6,
})

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
}>()

const uploadUrl = computed(() => import.meta.env.VITE_API_BASE_URL + '/upload/image')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`,
}))

const previewVisible = ref(false)
const previewUrl = ref('')

const fileList = computed(() => {
  return props.modelValue.map((url, index) => ({
    uid: index,
    name: `image-${index}`,
    url,
    status: 'success',
  })) as UploadUserFile[]
})

function handleSuccess(response: any, _file: any) {
  // 后端返回图片 URL
  const url = typeof response === 'string' ? response : response.data
  if (url) {
    const newUrls = [...props.modelValue, url]
    emit('update:modelValue', newUrls)
  }
}

function handleRemove(_file: any) {
  const urls = props.modelValue.filter((_, i) => i !== _file.uid)
  emit('update:modelValue', urls)
}

function handlePreview(file: UploadUserFile) {
  previewUrl.value = file.url || ''
  previewVisible.value = true
}

function beforeUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}
</script>

<style scoped>
.image-uploader :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}
.image-uploader :deep(.el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>
