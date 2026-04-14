<template>
  <div class="page-content">
    <!-- 头部 -->
    <div class="header" style="background: linear-gradient(135deg, #4cd964 0%, #2ecc71 100%); padding: 40px 20px 60px; border-radius: 0 0 40px 40px;">
      <div>
        <h2 style="color: white; font-size: 28px; margin-bottom: 8px;">健康记录</h2>
        <p style="color: rgba(255,255,255,0.9); font-size: 14px;">查看毛孩子的完整健康档案</p>
      </div>
    </div>

    <!-- 宠物筛选 -->
    <div style="padding: 0 20px; margin-top: -20px; overflow-x: auto;">
      <div style="display: flex; gap: 12px; padding-bottom: 8px;">
        <div 
          v-for="pet in petList" 
          :key="pet.id"
          :style="{
            flexShrink: 0,
            background: selectedPetId === pet.id ? '#f5a623' : 'white',
            color: selectedPetId === pet.id ? 'white' : '#666',
            padding: '10px 24px',
            borderRadius: '30px',
            cursor: 'pointer',
            boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
            fontWeight: selectedPetId === pet.id ? 'bold' : 'normal'
          }"
          @click="selectPet(pet.id)"
        >
          {{ pet.name }}
        </div>
      </div>
    </div>

    <!-- 记录分类Tab -->
    <div style="padding: 20px 20px 0 20px;">
      <div style="display: flex; gap: 8px; border-bottom: 1px solid #f0f0f0;">
        <div 
          v-for="tab in recordTabs" 
          :key="tab.key"
          :style="{
            padding: '10px 16px',
            cursor: 'pointer',
            color: activeRecordTab === tab.key ? '#4cd964' : '#999',
            borderBottom: activeRecordTab === tab.key ? '2px solid #4cd964' : 'none',
            fontWeight: activeRecordTab === tab.key ? 'bold' : 'normal'
          }"
          @click="activeRecordTab = tab.key; loadRecords()"
        >
          {{ tab.label }}
        </div>
      </div>
    </div>

    <!-- 快捷添加提示（仅自填类型显示） -->
    <div v-if="activeRecordTab === 'owner'" style="padding: 12px 20px;">
      <div style="background: #e8f8e8; border-radius: 12px; padding: 12px; display: flex; align-items: center; justify-content: space-between;">
        <div>
          <span style="font-size: 14px;">📝 记录日常健康数据</span>
          <p style="font-size: 12px; color: #666; margin-top: 4px;">如体重变化、在家驱虫等</p>
        </div>
        <el-button type="success" size="small" @click="quickAdd">+ 添加记录</el-button>
      </div>
    </div>

    <!-- 就诊记录列表 -->
    <div style="padding: 0 20px 20px;" v-loading="loading">
      <div v-if="activeRecordTab === 'hospital'">
        <div v-for="record in hospitalRecords" :key="record.id" 
             style="background: white; border-radius: 16px; margin-bottom: 12px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.03);">
          <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
            <div>
              <span :style="{ background: getRecordBg(record.type), padding: '2px 10px', borderRadius: '20px', fontSize: '11px', color: getRecordColor(record.type) }">
                {{ getTypeName(record.type) }}
              </span>
              <span style="margin-left: 8px; font-size: 12px; color: #999;">医院记录</span>
            </div>
            <span style="color: #999; font-size: 12px;">{{ record.createTime }}</span>
          </div>
          <!-- 显示宠物名 -->
          <div style="margin-bottom: 8px;">
            <span style="background: #f0f0f0; padding: 2px 8px; border-radius: 12px; font-size: 11px; color: #666;">
              🐾 {{ record.petName }}
            </span>
          </div>
          <div style="font-weight: bold; margin-bottom: 4px;">{{ record.title }}</div>
          <p style="color: #666; font-size: 13px; line-height: 1.5;">{{ record.content }}</p>
          <div style="margin-top: 12px; display: flex; gap: 16px;">
            <div style="color: #999; font-size: 12px;">
              <el-icon><User /></el-icon>
              <span style="margin-left: 4px;">医生：{{ record.doctorName }}</span>
            </div>
            <div style="color: #999; font-size: 12px;">
              <el-icon><OfficeBuilding /></el-icon>
              <span style="margin-left: 4px;">{{ record.hospital || '宠爱宠物医院' }}</span>
            </div>
          </div>
        </div>
        <div v-if="hospitalRecords.length === 0" style="text-align: center; padding: 40px;">
          <div style="font-size: 48px; margin-bottom: 12px;">🏥</div>
          <p style="color: #999;">暂无就诊记录</p>
          <p style="color: #ccc; font-size: 12px;">带宠物就诊后，医生会录入记录</p>
        </div>
      </div>

      <!-- 自填记录列表 -->
      <div v-if="activeRecordTab === 'owner'">
        <div v-for="record in ownerRecords" :key="record.id" 
             style="background: white; border-radius: 16px; margin-bottom: 12px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.03);">
          <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
            <div>
              <span :style="{ background: '#f0f0f0', padding: '2px 10px', borderRadius: '20px', fontSize: '11px', color: '#666' }">
                {{ getOwnerTypeName(record.type) }}
              </span>
              <span style="margin-left: 8px; font-size: 12px; color: #999;">我的记录</span>
            </div>
            <span style="color: #999; font-size: 12px;">{{ record.createTime }}</span>
          </div>
          <!-- 显示宠物名 -->
          <div style="margin-bottom: 8px;">
            <span style="background: #f0f0f0; padding: 2px 8px; border-radius: 12px; font-size: 11px; color: #666;">
              🐾 {{ record.petName }}
            </span>
          </div>
          <div style="font-weight: bold; margin-bottom: 4px;">{{ record.title }}</div>
          <p style="color: #666; font-size: 13px; line-height: 1.5;">{{ record.content }}</p>
          <div v-if="record.recordDate" style="margin-top: 8px; color: #999; font-size: 12px;">
            记录日期：{{ record.recordDate }}
          </div>
          <div style="margin-top: 12px; text-align: right;" @click.stop>
            <el-button type="danger" size="small" plain @click="deleteRecord(record)">删除</el-button>
          </div>
        </div>
        <div v-if="ownerRecords.length === 0" style="text-align: center; padding: 40px;">
          <div style="font-size: 48px; margin-bottom: 12px;">📝</div>
          <p style="color: #999;">暂无自填记录</p>
          <el-button type="primary" plain size="small" @click="quickAdd">添加第一条记录</el-button>
        </div>
      </div>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <router-link to="/pet" class="nav-item">
        <el-icon><Avatar /></el-icon>
        <span>宠物</span>
      </router-link>
      <router-link to="/health" class="nav-item active">
        <el-icon><Notebook /></el-icon>
        <span>健康</span>
      </router-link>
      <router-link to="/reserve" class="nav-item">
        <el-icon><Calendar /></el-icon>
        <span>预约</span>
      </router-link>
      <router-link to="/consult" class="nav-item">
        <el-icon><ChatDotRound /></el-icon>
        <span>咨询</span>
      </router-link>
      <router-link to="/profile" class="nav-item">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </router-link>
    </div>

    <!-- 添加自填记录弹窗 -->
    <el-dialog v-model="dialogVisible" title="添加健康记录" width="90%" style="border-radius: 20px; max-width: 400px;">
      <el-form :model="recordForm" label-width="80px" size="small">
        <el-form-item label="选择宠物">
          <el-select v-model="recordForm.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option v-for="pet in petList" :key="pet.id" :label="pet.name" :value="pet.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录类型">
          <el-select v-model="recordForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="体重记录" value="weight" />
            <el-option label="驱虫记录" value="deworming" />
            <el-option label="日常观察" value="daily" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="recordForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="recordForm.content" :rows="3" placeholder="请详细描述" />
        </el-form-item>
        <el-form-item label="记录日期">
          <el-date-picker v-model="recordForm.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Avatar, Notebook, Calendar, ChatDotRound, User, OfficeBuilding } from '@element-plus/icons-vue'
import { getPetList } from '@/api/owner/owner'

const loading = ref(false)
const submitting = ref(false)
const petList = ref([])
const selectedPetId = ref(null)
const activeRecordTab = ref('hospital')
const dialogVisible = ref(false)

const recordTabs = ref([
  { key: 'hospital', label: '🏥 就诊记录' },
  { key: 'owner', label: '📝 自填记录' }
])

const hospitalRecords = ref([])
const ownerRecords = ref([])

const recordForm = ref({
  petId: '',
  type: 'weight',
  title: '',
  content: '',
  recordDate: ''
})

const getRecordBg = (type) => {
  const bg = { vaccine: '#e8f8e8', deworming: '#fff3e8', exam: '#e8f0ff', treatment: '#ffe8e8' }
  return bg[type] || '#f0f0f0'
}

const getRecordColor = (type) => {
  const colors = { vaccine: '#4cd964', deworming: '#f5a623', exam: '#409eff', treatment: '#ff6b6b' }
  return colors[type] || '#666'
}

const getTypeName = (type) => {
  const names = { vaccine: '疫苗', deworming: '驱虫', exam: '体检', treatment: '诊疗' }
  return names[type] || type
}

const getOwnerTypeName = (type) => {
  const names = { weight: '体重', deworming: '驱虫', daily: '日常观察', other: '其他' }
  return names[type] || type
}

const loadPets = async () => {
  try {
    const res = await getPetList({ page: 1, pageSize: 100 })
    if (res.code === 200) {
      petList.value = res.data.records || []
    }
  } catch {
    petList.value = [
      { id: 1, name: '旺财' },
      { id: 2, name: '咪咪' }
    ]
  }
}

const loadRecords = () => {
  loading.value = true
  setTimeout(() => {
    // 就诊记录
    let hospitalData = [
      { id: 1, type: 'vaccine', title: '狂犬疫苗', content: '接种狂犬疫苗，一切正常', doctorName: '张医生', hospital: '宠爱宠物医院', petId: 1, petName: '旺财', createTime: '2024-01-15' },
      { id: 2, type: 'deworming', title: '体内驱虫', content: '服用驱虫药，无不良反应', doctorName: '李医生', hospital: '宠爱宠物医院', petId: 2, petName: '咪咪', createTime: '2024-01-10' },
      { id: 3, type: 'treatment', title: '皮肤病治疗', content: '诊断为真菌感染，开具药膏', doctorName: '张医生', hospital: '宠爱宠物医院', petId: 1, petName: '旺财', createTime: '2024-01-05' }
    ]
    
    // 自填记录
    let ownerData = [
      { id: 101, type: 'weight', title: '体重记录', content: '体重25.5kg，比上个月增加0.5kg', petId: 1, petName: '旺财', recordDate: '2024-01-20', createTime: '2024-01-20' },
      { id: 102, type: 'daily', title: '日常观察', content: '精神状态良好，食欲正常', petId: 2, petName: '咪咪', recordDate: '2024-01-18', createTime: '2024-01-18' }
    ]
    
    // 根据选中的宠物筛选
    if (selectedPetId.value) {
      hospitalData = hospitalData.filter(r => r.petId === selectedPetId.value)
      ownerData = ownerData.filter(r => r.petId === selectedPetId.value)
    }
    
    hospitalRecords.value = hospitalData
    ownerRecords.value = ownerData
    
    loading.value = false
  }, 300)
}

const selectPet = (petId) => {
  selectedPetId.value = petId
  loadRecords()
}

const quickAdd = () => {
  if (petList.value.length === 0) {
    ElMessage.warning('请先添加宠物')
    return
  }
  recordForm.value = {
    petId: selectedPetId.value || petList.value[0]?.id || '',
    type: 'weight',
    title: '',
    content: '',
    recordDate: ''
  }
  dialogVisible.value = true
}

const submitRecord = () => {
  if (!recordForm.value.petId) {
    ElMessage.warning('请选择宠物')
    return
  }
  if (!recordForm.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!recordForm.value.content) {
    ElMessage.warning('请输入内容')
    return
  }
  
  submitting.value = true
  setTimeout(() => {
    const pet = petList.value.find(p => p.id === recordForm.value.petId)
    
    const newRecord = {
      id: Date.now(),
      type: recordForm.value.type,
      title: recordForm.value.title,
      content: recordForm.value.content,
      recordDate: recordForm.value.recordDate || new Date().toLocaleDateString(),
      createTime: new Date().toLocaleDateString(),
      petId: pet?.id,
      petName: pet?.name
    }
    
    ownerRecords.value = [newRecord, ...ownerRecords.value]
    ElMessage.success('添加成功')
    dialogVisible.value = false
    submitting.value = false
  }, 300)
}

const deleteRecord = (record) => {
  ElMessageBox.confirm('确定删除这条记录吗？', '提示', { type: 'warning' }).then(() => {
    ownerRecords.value = ownerRecords.value.filter(r => r.id !== record.id)
    ElMessage.success('删除成功')
  })
}

onMounted(() => {
  loadPets()
  loadRecords()
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
  color: #4cd964;
}

.nav-item .el-icon {
  font-size: 22px;
}
</style>