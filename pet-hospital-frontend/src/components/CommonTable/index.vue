<template>
  <div class="common-table">
    <el-table
      :data="data"
      v-loading="loading"
      border
      stripe
      style="width: 100%"
    >
      <slot></slot>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination" v-if="showPagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  total: {
    type: Number,
    default: 0
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  page: {
    type: Number,
    default: 1
  },
  size: {
    type: Number,
    default: 10
  }
})

const emit = defineEmits(['update:page', 'update:size', 'pagination-change'])

const currentPage = ref(props.page)
const pageSize = ref(props.size)

watch(() => props.page, (val) => {
  currentPage.value = val
})

watch(() => props.size, (val) => {
  pageSize.value = val
})

const handleSizeChange = (val) => {
  emit('update:size', val)
  emit('pagination-change', { page: currentPage.value, size: val })
}

const handleCurrentChange = (val) => {
  emit('update:page', val)
  emit('pagination-change', { page: val, size: pageSize.value })
}
</script>

<style scoped>
.common-table {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>