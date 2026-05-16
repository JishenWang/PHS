<template>
  <el-empty :description="description" />
</template>

<script setup>
defineProps({
  description: {
    type: String,
    default: 'No Data'
  }
})
</script>
