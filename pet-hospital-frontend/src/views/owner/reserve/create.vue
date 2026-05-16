<template>
  <div class="create-reserve-page">
    <div class="page-nav">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        {{ $t('reserveCreate.back') }}
      </el-button>
    </div>

    <el-card>
      <template #header>
        <span>{{ $t('reserveCreate.newAppointment') }}</span>
      </template>

      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 30px">
        <el-step :title="$t('reserveCreate.selectPet')" />
        <el-step :title="$t('reserveCreate.selectService')" />
        <el-step :title="$t('reserveCreate.selectDoctorTime')" />
        <el-step :title="$t('reserveCreate.confirmSubmit')" />
      </el-steps>

      <!-- 步骤内容（复用 index.vue 中的逻辑） -->
      <div v-show="currentStep === 0">
        <el-form-item :label="$t('reserveCreate.selectPet')" prop="petId" required>
          <el-select v-model="form.petId" :placeholder="$t('reserveCreate.pleaseSelectPet')" style="width: 100%">
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const router = useRouter()

const currentStep = ref(0)
const petList = ref([])
const form = reactive({
  petId: ''
})

const goBack = () => {
  router.push('/owner/reserve')
}
</script>
