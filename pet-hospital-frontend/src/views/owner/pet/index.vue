<template>
  <div class="pet-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ $t('ownerPet.title') }}</h2>
        <p class="page-subtitle">{{ $t('ownerPet.subtitle') }}</p>
      </div>
      <el-button type="primary" size="large" @click="handleAdd" class="add-btn">
        <el-icon><Plus /></el-icon>
        {{ $t('ownerPet.addPet') }}
      </el-button>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon">🐾</div>
        <div class="stat-value">{{ petList.length }}</div>
        <div class="stat-label">{{ $t('ownerPet.totalPets') }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💉</div>
        <div class="stat-value">{{ vaccineCount }}</div>
        <div class="stat-label">{{ $t('ownerPet.vaccines') }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏥</div>
        <div class="stat-value">{{ healthScore || 85 }}</div>
        <div class="stat-label">{{ $t('ownerPet.healthScore') }}</div>
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
            <span><el-icon><Clock /></el-icon> {{ pet.age }}{{ $t('ownerPet.yearUnit') }}</span>
            <span>⚖️ {{ pet.weight }}{{ $t('ownerPet.weightUnit') }}</span>
          </div>
        </div>
        <div class="pet-actions" @click.stop>
          <el-button circle :icon="Edit" size="small" @click="editPet(pet)" />
          <el-button circle :icon="Delete" size="small" type="danger" @click="deletePet(pet)" />
        </div>
      </div>

      <div v-if="!loading && petList.length === 0" class="empty-state">
        <el-empty :description="$t('ownerPet.noPetsYet')">
          <el-button type="primary" @click="handleAdd">{{ $t('ownerPet.addFirstPet') }}</el-button>
        </el-empty>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" class="pet-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" size="small">
        <el-form-item :label="$t('common.name')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('ownerPet.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('ownerPet.type')" prop="type">
          <el-select v-model="form.type" :placeholder="$t('ownerPet.typePlaceholder')" style="width: 100%">
            <el-option :label="$t('adminPet.typeDog')" value="dog" />
            <el-option :label="$t('adminPet.typeCat')" value="cat" />
            <el-option :label="$t('adminPet.typeRabbit')" value="rabbit" />
            <el-option :label="$t('adminPet.typeOther')" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('ownerPet.breed')" prop="breed">
          <el-input v-model="form.breed" :placeholder="$t('ownerPet.breedPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('ownerPet.gender')" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="male">{{ $t('ownerPet.male') }}</el-radio>
            <el-radio value="female">{{ $t('ownerPet.female') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('ownerPet.birthday')" prop="birthday">
          <el-date-picker v-model="form.birthday" type="date" :placeholder="$t('ownerPet.birthdayPlaceholder')" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('ownerPet.weightKg')" prop="weight">
          <el-input-number v-model="form.weight" :min="0" :precision="1" /> {{ $t('ownerPet.weightUnit') }}
        </el-form-item>
        <el-form-item :label="$t('ownerPet.description')" prop="description">
          <el-input type="textarea" v-model="form.description" :placeholder="$t('ownerPet.descriptionPlaceholder')" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">{{ $t('common.save') }}</el-button>
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
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
const loading = ref(false)
const submitting = ref(false)
const petList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref(t('ownerPet.addPet'))
const editingId = ref(null)
const formRef = ref(null)

const vaccineCount = ref(0)
const healthScore = ref(85)

const form = reactive({
  name: '',
  type: '',
  breed: '',
  gender: 'male',
  birthday: '',
  weight: 0,
  description: ''
})

const rules = {
  name: [{ required: true, message: t('ownerPet.pleaseEnterPetName'), trigger: 'blur' }],
  type: [{ required: true, message: t('ownerPet.pleaseSelectType'), trigger: 'change' }],
  breed: [{ required: true, message: t('ownerPet.pleaseEnterBreed'), trigger: 'blur' }],
  birthday: [{ required: true, message: t('ownerPet.pleaseSelectBirthday'), trigger: 'change' }]
}

// calculate age
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
      // data in res.data.data
      const records = res.data?.data || res.data?.records || []
      // calculate age
      petList.value = records.map(pet => ({
        ...pet,
        age: calculateAge(pet.birthday)
      }))
      console.log('Loaded, pet count:', petList.value.length)
    } else {
      ElMessage.error(res.message || res.msg || t('ownerPet.loadFailed'))
    }
  } catch (error) {
    ElMessage.error(t('ownerPet.loadFailed'))
    console.error('Failed to load pet list:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = t('ownerPet.addPet')
  editingId.value = null
  Object.assign(form, { name: '', type: '', breed: '', gender: 'male', birthday: '', weight: 0, description: '' })
  dialogVisible.value = true
}

const editPet = (pet) => {
  dialogTitle.value = t('ownerPet.editPet')
  editingId.value = pet.id
  Object.assign(form, {
    name: pet.name,
    type: pet.type || pet.species || '',
    breed: pet.breed,
    gender: pet.gender,
    birthday: pet.birthday,
    weight: pet.weight,
    description: pet.description || ''
  })
  dialogVisible.value = true
}

const deletePet = (pet) => {
  ElMessageBox.confirm(t('ownerPet.deleteConfirm', { name: pet.name }), t('common.tip'), { type: 'warning' }).then(async () => {
    try {
      await deletePetApi(pet.id)
      ElMessage.success(t('common.deleteSuccess'))
      loadPetList()
    } catch {
      ElMessage.error(t('common.deleteFailed'))
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
          ElMessage.success(t('common.updateSuccess'))
        } else {
          await addPet(form)
          ElMessage.success(t('common.addSuccess'))
        }
        dialogVisible.value = false
        loadPetList()
      } catch {
        ElMessage.error(t('common.operationFailed'))
      } finally {
        submitting.value = false
      }
    }
  })
}

const viewDetail = (id) => {
  console.log('Pet clicked:', id)
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