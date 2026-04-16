<template>
  <div class="pet-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的宠物</h2>
        <p class="page-subtitle">管理您的宠物档案，记录它们的成长</p>
      </div>
      <el-button type="primary" size="large" @click="handleAdd" class="add-btn">
        <el-icon><Plus /></el-icon>
        添加宠物
      </el-button>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon">🐾</div>
        <div class="stat-value">{{ petList.length }}</div>
        <div class="stat-label">宠物总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💉</div>
        <div class="stat-value">{{ vaccineCount }}</div>
        <div class="stat-label">疫苗记录</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏥</div>
        <div class="stat-value">{{ healthScore || 85 }}</div>
        <div class="stat-label">健康评分</div>
      </div>
    </div>

    <div class="pet-grid" v-loading="loading">
      <div v-for="pet in petList" :key="pet.id" class="pet-card" @click="viewDetail(pet.id)">
        <div class="pet-avatar">
          <div class="avatar-placeholder">
            <el-icon size="40"><Avatar /></el-icon>
          </div>
          <div class="pet-gender" :class="pet.gender === 'male' ? 'male' : 'female'">
            {{ pet.gender === 'male' ? '♂' : '♀' }}
          </div>
        </div>
        <div class="pet-info">
          <h3>{{ pet.name }}</h3>
          <p class="breed">{{ pet.breed }}</p>
          <div class="pet-meta">
            <span><el-icon><Clock /></el-icon> {{ pet.age }}岁</span>
            <span>⚖️ {{ pet.weight }}kg</span>
          </div>
        </div>
        <div class="pet-actions" @click.stop>
          <el-button circle :icon="Edit" size="small" @click="editPet(pet)" />
          <el-button circle :icon="Delete" size="small" type="danger" @click="deletePet(pet)" />
        </div>
      </div>

      <div v-if="!loading && petList.length === 0" class="empty-state">
        <el-empty description="暂无宠物">
          <el-button type="primary" @click="handleAdd">添加第一只宠物</el-button>
        </el-empty>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" class="pet-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" size="small">
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="form.name" placeholder="给宠物起个可爱的名字" />
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-input v-model="form.breed" placeholder="例如：金毛、布偶猫" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="male">🐕 男孩子</el-radio>
            <el-radio value="female">🐱 女孩子</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker v-model="form.birthday" type="date" placeholder="选择出生日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="form.weight" :min="0" :precision="1" /> kg
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description" placeholder="说说宠物的性格、喜好、过敏史等" :rows="3" />
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
import { Plus, Edit, Delete, Avatar, Clock } from '@element-plus/icons-vue'
import { getPetList, addPet, updatePet, deletePet as deletePetApi } from '@/api/owner/owner'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const petList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加宠物')
const editingId = ref(null)
const formRef = ref(null)

const vaccineCount = ref(0)
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
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  breed: [{ required: true, message: '请输入品种', trigger: 'blur' }],
  birthday: [{ required: true, message: '请选择出生日期', trigger: 'change' }]
}

// 计算年龄
const calculateAge = (birthday) => {
  if (!birthday) return 0
  const birthDate = new Date(birthday)
  const today = new Date()
  let age = today.getFullYear() - birthDate.getFullYear()
  const monthDiff = today.getMonth() - birthDate.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--
  }
  return age
}

const loadPetList = async () => {
  loading.value = true
  try {
    const res = await getPetList({ page: 1, pageSize: 100 })
    if (res.code === 200) {
      // 数据在 res.data.data 中
      const records = res.data?.data || res.data?.records || []
      // 计算年龄
      petList.value = records.map(pet => ({
        ...pet,
        age: calculateAge(pet.birthday)
      }))
      console.log('加载成功，宠物数量:', petList.value.length)
    } else {
      ElMessage.error(res.message || res.msg || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载失败，请检查网络')
    console.error('加载宠物列表失败:', error)
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
  ElMessageBox.confirm(`确定要删除"${pet.name}"吗？`, '提示', { type: 'warning' }).then(async () => {
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
  console.log('点击宠物:', id)
  router.push(`/owner/pet/${id}`)
}

onMounted(() => {
  loadPetList()
})
</script>

<style scoped lang="scss">
.pet-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 4px;
    }
    
    .page-subtitle {
      font-size: 14px;
      color: #64748b;
    }
    
    .add-btn {
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      border: none;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
      }
    }
  }
  
  .stats-row {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 28px;
    
    .stat-card {
      background: white;
      border-radius: 20px;
      padding: 20px;
      text-align: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      transition: transform 0.2s, box-shadow 0.2s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
      }
      
      .stat-icon {
        font-size: 32px;
        margin-bottom: 8px;
      }
      
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #3b82f6;
      }
      
      .stat-label {
        font-size: 13px;
        color: #64748b;
        margin-top: 4px;
      }
    }
  }
  
  .pet-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
    
    .pet-card {
      background: white;
      border-radius: 20px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      cursor: pointer;
      transition: all 0.3s;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
      }
      
      .pet-avatar {
        position: relative;
        flex-shrink: 0;
        
        .avatar-placeholder {
          width: 70px;
          height: 70px;
          background: #f0f4f8;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #94a3b8;
        }
        
        .pet-gender {
          position: absolute;
          bottom: 0;
          right: 0;
          width: 24px;
          height: 24px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 13px;
          font-weight: bold;
          color: white;
          
          &.male {
            background: #3b82f6;
          }
          
          &.female {
            background: #ef4444;
          }
        }
      }
      
      .pet-info {
        flex: 1;
        
        h3 {
          font-size: 18px;
          font-weight: 600;
          color: #1e293b;
          margin-bottom: 4px;
        }
        
        .breed {
          font-size: 13px;
          color: #64748b;
          margin-bottom: 8px;
        }
        
        .pet-meta {
          display: flex;
          gap: 16px;
          font-size: 12px;
          color: #94a3b8;
          
          span {
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }
      
      .pet-actions {
        display: flex;
        gap: 8px;
        flex-shrink: 0;
      }
    }
  }
  
  .empty-state {
    grid-column: 1 / -1;
    padding: 60px;
    text-align: center;
  }
}

.pet-dialog {
  :deep(.el-dialog) {
    border-radius: 20px;
  }
}
</style>