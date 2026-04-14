<template>
  <div class="page-content">
    <!-- 头部 -->
    <div class="header" style="background: linear-gradient(135deg, #f5a623 0%, #ff6b6b 100%); padding: 40px 20px 60px; border-radius: 0 0 40px 40px;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <h2 style="color: white; font-size: 28px; margin-bottom: 8px;">我的宠物</h2>
          <p style="color: rgba(255,255,255,0.9); font-size: 14px;">管理您的毛孩子</p>
        </div>
        <el-button circle :icon="Plus" style="background: rgba(255,255,255,0.2); border: none; color: white; width: 44px; height: 44px;" @click="handleAdd" />
      </div>
    </div>

    <!-- 统计卡片 -->
    <div style="display: flex; gap: 15px; padding: 0 20px; margin-top: -30px;">
      <div style="flex: 1; background: white; border-radius: 20px; padding: 15px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.05);">
        <div style="font-size: 28px; font-weight: bold; color: #f5a623;">{{ petList.length }}</div>
        <div style="font-size: 12px; color: #999;">宠物数量</div>
      </div>
      <div style="flex: 1; background: white; border-radius: 20px; padding: 15px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.05);">
        <div style="font-size: 28px; font-weight: bold; color: #4cd964;">{{ vaccineCount }}</div>
        <div style="font-size: 12px; color: #999;">疫苗记录</div>
      </div>
      <div style="flex: 1; background: white; border-radius: 20px; padding: 15px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.05);">
        <div style="font-size: 28px; font-weight: bold; color: #ff6b6b;">{{ healthScore || 85 }}</div>
        <div style="font-size: 12px; color: #999;">健康评分</div>
      </div>
    </div>

    <!-- 宠物列表 -->
    <div style="padding: 20px;" v-loading="loading">
      <div v-for="pet in petList" :key="pet.id" 
           style="background: white; border-radius: 20px; margin-bottom: 15px; padding: 16px; display: flex; align-items: center; gap: 15px; box-shadow: 0 2px 10px rgba(0,0,0,0.03); cursor: pointer;"
           @click="viewDetail(pet.id)">
        <el-avatar :size="60" :src="pet.avatar" style="flex-shrink: 0;">
          <el-icon size="32"><Avatar /></el-icon>
        </el-avatar>
        <div style="flex: 1;">
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
            <h3 style="font-size: 18px;">{{ pet.name }}</h3>
            <span :style="{
              background: pet.gender === 'male' ? '#e8f4ff' : '#ffe8e8',
              color: pet.gender === 'male' ? '#409eff' : '#f56c6c',
              padding: '2px 8px',
              borderRadius: '20px',
              fontSize: '10px'
            }">{{ pet.gender === 'male' ? '男孩子' : '女孩子' }}</span>
          </div>
          <p style="color: #999; font-size: 13px;">{{ pet.breed }} · {{ pet.age }}岁 · {{ pet.weight }}kg</p>
        </div>
        <div style="display: flex; gap: 8px;" @click.stop>
          <el-button circle :icon="Edit" size="small" @click="editPet(pet)" />
          <el-button circle :icon="Delete" size="small" type="danger" @click="deletePet(pet)" />
        </div>
      </div>

      <div v-if="!loading && petList.length === 0" style="text-align: center; padding: 60px 20px;">
        <div style="font-size: 60px; margin-bottom: 16px;">🐾</div>
        <p style="color: #999; margin-bottom: 20px;">还没有宠物，点击右上角添加</p>
        <el-button type="primary" @click="handleAdd">添加第一只宠物</el-button>
      </div>
    </div>

  

    <!-- 添加/编辑宠物弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="85%" style="border-radius: 20px; max-width: 400px; margin: 0 auto;">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="70px" size="small">
        <el-form-item label="名字" prop="name" style="margin-bottom: 12px;">
          <el-input v-model="form.name" placeholder="给宠物起个名字" />
        </el-form-item>
        <el-form-item label="品种" prop="breed" style="margin-bottom: 12px;">
          <el-input v-model="form.breed" placeholder="例如：金毛" />
        </el-form-item>
        <el-form-item label="性别" prop="gender" style="margin-bottom: 12px;">
          <el-radio-group v-model="form.gender">
            <el-radio value="male">男孩子</el-radio>
            <el-radio value="female">女孩子</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日" prop="birthday" style="margin-bottom: 12px;">
          <el-date-picker v-model="form.birthday" type="date" placeholder="选择生日" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="体重" prop="weight" style="margin-bottom: 12px;">
          <el-input-number v-model="form.weight" :min="0" :precision="1" style="width: 100%;" /> kg
        </el-form-item>
        <el-form-item label="描述" prop="description" style="margin-bottom: 12px;">
          <el-input type="textarea" v-model="form.description" placeholder="说说宠物的性格、喜好等" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Avatar, Notebook, Calendar, ChatDotRound, User } from '@element-plus/icons-vue'
import { getPetList, addPet, updatePet, deletePet as deletePetApi } from '@/api/owner/owner'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const petList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加宠物')
const editingId = ref(null)
const formRef = ref(null)

const vaccineCount = ref(2)
const healthScore = ref(85)

const form = reactive({
  name: '',
  breed: '',
  gender: 'male',
  birthday: '',
  weight: 0,
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名字', trigger: 'blur' }],
  breed: [{ required: true, message: '请输入品种', trigger: 'blur' }],
  birthday: [{ required: true, message: '请选择生日', trigger: 'change' }]
}

const loadPetList = async () => {
  loading.value = true
  try {
    const res = await getPetList({ page: 1, pageSize: 100 })
    if (res.code === 200) {
      petList.value = res.data.records || []
    }
  } catch {
    // 模拟数据
    petList.value = [
      { id: 1, name: '旺财', breed: '金毛', gender: 'male', age: 3, weight: 25, birthday: '2020-01-01' },
      { id: 2, name: '咪咪', breed: '布偶猫', gender: 'female', age: 2, weight: 4.5, birthday: '2021-06-15' }
    ]
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加宠物'
  editingId.value = null
  Object.assign(form, { name: '', breed: '', gender: 'male', birthday: '', weight: 0, description: '' })
  dialogVisible.value = true
}

const editPet = (pet) => {
  dialogTitle.value = '编辑宠物'
  editingId.value = pet.id
  Object.assign(form, {
    name: pet.name,
    breed: pet.breed,
    gender: pet.gender,
    birthday: pet.birthday,
    weight: pet.weight,
    description: pet.description || ''
  })
  dialogVisible.value = true
}

const deletePet = (pet) => {
  ElMessageBox.confirm(`删除后无法恢复，确定删除"${pet.name}"吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await deletePetApi(pet.id)
      ElMessage.success('删除成功')
      loadPetList()
    } catch {
      ElMessage.error('删除失败')
    }
  })
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (editingId.value) {
          await updatePet(editingId.value, form)
          ElMessage.success('修改成功')
        } else {
          await addPet(form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadPetList()
      } catch {
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const viewDetail = (id) => {
  router.push(`/pet/${id}`)
}

onMounted(() => {
  loadPetList()
})
</script>

<style scoped>
.page-content {
  padding-bottom: 80px;
  min-height: 100vh;
  background: #f8f9fc;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: space-around;
  padding: 10px 20px 20px;
  box-shadow: 0 -2px 15px rgba(0,0,0,0.05);
  border-radius: 20px 20px 0 0;
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: #999;
  font-size: 12px;
  transition: all 0.3s;
}

.nav-item.active {
  color: #f5a623;
}

.nav-item .el-icon {
  font-size: 22px;
}
</style>