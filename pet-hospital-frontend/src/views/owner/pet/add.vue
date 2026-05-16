<template>
  <div class="add-pet-page">
    <div class="page-nav">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        {{ $t('ownerPetAdd.back') }}
      </el-button>
    </div>

    <el-card>
      <template #header>
        <span>{{ $t('ownerPetAdd.addPet') }}</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="add-pet-form"
      >
        <el-form-item :label="$t('ownerPetAdd.petName')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('ownerPetAdd.pleaseEnterPetName')" />
        </el-form-item>

        <el-form-item :label="$t('ownerPetAdd.type')" prop="type">
          <el-select v-model="form.type" :placeholder="$t('ownerPetAdd.typePlaceholder')" style="width: 100%">
            <el-option :label="$t('adminPet.typeDog')" value="dog" />
            <el-option :label="$t('adminPet.typeCat')" value="cat" />
            <el-option :label="$t('adminPet.typeRabbit')" value="rabbit" />
            <el-option :label="$t('adminPet.typeOther')" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('ownerPetAdd.breed')" prop="breed">
          <el-input v-model="form.breed" :placeholder="$t('ownerPetAdd.pleaseEnterBreed')" />
        </el-form-item>

        <el-form-item :label="$t('ownerPetAdd.gender')" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="male">{{ $t('ownerPetAdd.male') }}</el-radio>
            <el-radio value="female">{{ $t('ownerPetAdd.female') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item :label="$t('ownerPetAdd.birthday')" prop="birthday">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            :placeholder="$t('ownerPetAdd.selectBirthday')"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item :label="$t('ownerPetAdd.weightKg')" prop="weight">
          <el-input-number v-model="form.weight" :min="0" :precision="1" :step="0.5" />
        </el-form-item>

        <el-form-item :label="$t('ownerPetAdd.chipNumber')" prop="chipNumber">
          <el-input v-model="form.chipNumber" :placeholder="$t('ownerPetAdd.chipPlaceholder')" />
        </el-form-item>

        <el-form-item :label="$t('ownerPetAdd.neuteredStatus')" prop="neutered">
          <el-radio-group v-model="form.neutered">
            <el-radio :value="true">{{ $t('ownerPetAdd.neutered') }}</el-radio>
            <el-radio :value="false">{{ $t('ownerPetAdd.notNeutered') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item :label="$t('ownerPetAdd.petAvatar')">
          <el-upload
            action="#"
            :auto-upload="false"
            :on-change="handleAvatarChange"
            :limit="1"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item :label="$t('ownerPetAdd.description')" prop="description">
          <el-input
            type="textarea"
            v-model="form.description"
            :placeholder="$t('ownerPetAdd.descriptionPlaceholder')"
            :rows="4"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">{{ $t('ownerPetAdd.addPetBtn') }}</el-button>
          <el-button @click="goBack">{{ $t('ownerPetAdd.cancel') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { addPet } from '@/api/owner/owner'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const avatarFile = ref(null)

const form = reactive({
  name: '',
  type: '',
  breed: '',
  gender: 'male',
  birthday: '',
  weight: 0,
  chipNumber: '',
  neutered: false,
  description: '',
  avatar: ''
})

const rules = {
  name: [
    { required: true, message: t('ownerPetAdd.pleaseEnterPetName'), trigger: 'blur' },
    { min: 1, max: 20, message: t('ownerPetAdd.length1To20'), trigger: 'blur' }
  ],
  type: [
    { required: true, message: t('ownerPetAdd.pleaseSelectType'), trigger: 'change' }
  ],
  breed: [
    { required: true, message: t('ownerPetAdd.pleaseEnterBreed'), trigger: 'blur' }
  ],
  gender: [
    { required: true, message: t('ownerPetAdd.pleaseSelectGender'), trigger: 'change' }
  ],
  birthday: [
    { required: true, message: t('ownerPetAdd.pleaseSelectBirthday'), trigger: 'change' }
  ],
  weight: [
    { required: true, message: t('ownerPetAdd.pleaseEnterWeight'), trigger: 'blur' }
  ]
}

const goBack = () => {
  router.push('/owner/pet')
}

const handleAvatarChange = (file) => {
  avatarFile.value = file.raw
  // 实际项目中需要上传到服务器
  // const formData = new FormData()
  // formData.append('file', file.raw)
  // upload API...
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await addPet(form)
        if (res.code === 200) {
          ElMessage.success(t('ownerPetAdd.addedSuccessfully'))
          router.push('/owner/pet')
        }
      } catch (error) {
        ElMessage.error(t('ownerPetAdd.failedToAdd'))
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped>
.add-pet-page {
  max-width: 600px;
  margin: 0 auto;
}

.page-nav {
  margin-bottom: 16px;
}

.add-pet-form {
  margin-top: 20px;
}
</style>
