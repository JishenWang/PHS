<template>
  <el-form :inline="true" :model="modelValue" class="search-form">
    <slot></slot>
    <el-form-item>
      <el-button type="primary" @click="$emit('search')">
        <el-icon><Search /></el-icon>搜索
      </el-button>
      <el-button @click="$emit('reset')">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { Search } from '@element-plus/icons-vue'

defineProps(['modelValue'])
defineEmits(['search', 'reset'])
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
</style>