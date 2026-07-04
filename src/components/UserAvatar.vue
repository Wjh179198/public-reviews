<template>
  <div class="user-avatar" :class="{ clickable: clickable }" @click="handleClick">
    <el-avatar :size="size" :src="image || undefined">
      <el-icon :size="Number(size) / 2"><UserFilled /></el-icon>
    </el-avatar>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  userId?: number
  image?: string
  size?: number | string
  clickable?: boolean
}>(), {
  size: 40,
  clickable: true,
})

const router = useRouter()

function handleClick() {
  if (props.clickable && props.userId) {
    router.push(`/user/${props.userId}`)
  }
}
</script>

<style scoped>
.user-avatar {
  display: inline-block;
}

.user-avatar.clickable {
  cursor: pointer;
}

.user-avatar.clickable:hover {
  opacity: 0.85;
}
</style>
