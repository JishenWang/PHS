<template>
  <div class="create-reserve-page">
    <div class="page-nav">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <el-card>
      <template #header>
        <span>新建预约</span>
      </template>

      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 30px">
        <el-step title="选择宠物" />
        <el-step title="选择服务" />
        <el-step title="选择医生/时间" />
        <el-step title="确认提交" />
      </el-steps>

      <!-- 步骤内容（复用 index.vue 中的逻辑） -->
      <div v-show="currentStep === 0">
        <el-form-item label="选择宠物" prop="petId" required>
          <el-select v-model="form.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option
              v-for="pet in petList"
              :key="pet.id"
              :label="pet.name"
              :value="pet.id"
            />
          </el-select>
        </el-form-item>
      </div>

      <!-- 后续步骤与 index.vue 相同，可复用 -->
      
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/owner/reserve')
}
</script>