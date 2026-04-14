<template>
  <div class="add-pet-page">
    <div class="page-nav">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <el-card>
      <template #header>
        <span>添加宠物</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="add-pet-form"
      >
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入宠物名称" />
        </el-form-item>

        <el-form-item label="品种" prop="breed">
          <el-input v-model="form.breed" placeholder="请输入品种" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="male">公</el-radio>
            <el-radio value="female">母</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            placeholder="选择出生日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="form.weight" :min="0" :precision="1" :step="0.5" />
        </el-form-item>

        <el-form-item label="芯片号" prop="chipNumber">
          <el-input v-model="form.chipNumber" placeholder="请输入芯片号（选填）" />
        </el-form-item>

        <el-form-item label="绝育状态" prop="neutered">
          <el-radio-group v-model="form.neutered">
            <el-radio :value="true">已绝育</el-radio>
            <el-radio :value="false">未绝育</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="宠物头像">
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

        <el-form-item label="描述" prop="description">
          <el-input
            type="textarea"
            v-model="form.description"
            placeholder="请输入宠物描述、特点、过敏史等"
            :rows="4"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">添加宠物</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addPet } from '@/api/owner/owner'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const avatarFile = ref(null)

const form = reactive({
  name: '',
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
    { required: true, message: '请输入宠物名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在1-20个字符', trigger: 'blur' }
  ],
  breed: [
    { required: true, message: '请输入品种', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  birthday: [
    { required: true, message: '请选择出生日期', trigger: 'change' }
  ],
  weight: [
    { required: true, message: '请输入体重', trigger: 'blur' }
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
          ElMessage.success('添加成功')
          router.push('/owner/pet')
        }
      } catch (error) {
        ElMessage.error('添加失败，请稍后重试')
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