<template>
  <span class="star-rating" :class="{ 'star-interactive': interactive }">
    <template v-for="i in 5" :key="i">
      <el-icon
        v-if="interactive"
        :size="size"
        :class="['star', { 'star-active': i <= modelValue }]"
        @click="$emit('update:modelValue', i)"
      >
        <StarFilled v-if="i <= modelValue" />
        <Star v-else />
      </el-icon>
      <el-icon v-else :size="size" :class="['star', { 'star-active': i <= score }]">
        <StarFilled v-if="i <= Math.floor(score)" />
        <SemiSelect v-else-if="i === Math.ceil(score) && score % 1 !== 0" />
        <Star v-else />
      </el-icon>
    </template>
    <span v-if="showScore" class="score-text">{{ typeof score === 'number' ? score.toFixed(1) : score }}</span>
  </span>
</template>

<script setup lang="ts">
import { StarFilled, Star, SemiSelect } from '@element-plus/icons-vue'

withDefaults(defineProps<{
  score?: number
  modelValue?: number
  interactive?: boolean
  showScore?: boolean
  size?: number | string
}>(), {
  score: 0,
  modelValue: 0,
  interactive: false,
  showScore: true,
  size: 16,
})

defineEmits<{
  'update:modelValue': [value: number]
}>()
</script>

<style scoped>
.star-rating {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.star {
  color: #c0c4cc;
}

.star-active {
  color: #e6a23c;
}

.star-interactive .star {
  cursor: pointer;
  transition: color 0.2s;
}

.star-interactive .star:hover {
  color: #e6a23c;
}

.score-text {
  margin-left: 4px;
  font-size: 14px;
  color: #e6a23c;
  font-weight: bold;
}
</style>
