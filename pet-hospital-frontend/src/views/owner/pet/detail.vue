<template>
  <div class="page-content">
    <!-- 头部 -->
    <div class="header" style="background: linear-gradient(135deg, #f5a623 0%, #ff6b6b 100%); padding: 30px 20px; border-radius: 0 0 30px 30px;">
      <div style="display: flex; align-items: center; gap: 16px;">
        <el-button circle :icon="ArrowLeft" style="background: rgba(255,255,255,0.2); border: none; color: white;" @click="goBack" />
        <div>
          <h2 style="color: white; font-size: 24px;">{{ petDetail.name }}</h2>
          <p style="color: rgba(255,255,255,0.9); font-size: 14px;">{{ petDetail.breed }}</p>
        </div>
      </div>
    </div>

    <!-- 宠物信息卡片 -->
    <div style="padding: 20px;">
      <div style="background: white; border-radius: 20px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
        <div style="display: flex; gap: 20px; align-items: center;">
          <el-avatar :size="70" :src="petDetail.avatar">
            <el-icon size="40"><Avatar /></el-icon>
          </el-avatar>
          <div style="flex: 1;">
            <div style="display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 8px;">
              <span :style="{ background: petDetail.gender === 'male' ? '#e8f4ff' : '#ffe8e8', padding: '4px 12px', borderRadius: '20px', fontSize: '12px', color: petDetail.gender === 'male' ? '#409eff' : '#f56c6c' }">
                {{ petDetail.gender === 'male' ? '男孩子' : '女孩子' }}
              </span>
              <span style="background: #f0f0f0; padding: 4px 12px; border-radius: 20px; font-size: 12px;">{{ petDetail.age }}岁</span>
              <span style="background: #f0f0f0; padding: 4px 12px; border-radius: 20px; font-size: 12px;">{{ petDetail.weight }}kg</span>
            </div>
            <div style="color: #999; font-size: 13px;">生日：{{ petDetail.birthday }}</div>
            <div style="color: #999; font-size: 13px;">芯片号：{{ petDetail.chipNumber || '未录入' }}</div>
          </div>
        </div>
        <div style="margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0;">
          <div style="color: #666; font-size: 13px;">{{ petDetail.description || '暂无描述' }}</div>
        </div>
      </div>

      <!-- 健康记录标题 + 添加按钮 -->
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3 style="font-size: 18px;">健康记录</h3>
        <el-button type="primary" size="small" :icon="Plus" @click="addRecord">添加记录</el-button>
      </div>

      <!-- 健康记录列表 -->
      <div v-loading="loading">
        <div v-for="record in recordList" :key="record.id" 
             style="background: white; border-radius: 16px; margin-bottom: 12px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.03);">
          <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
            <span :style="{ color: getRecordColor(record.type), fontWeight: 'bold', fontSize: '13px' }">{{ getTypeName(record.type) }}</span>
            <span style="color: #999; font-size: 12px;">{{ record.createTime }}</span>
          </div>
          <div style="font-weight: bold; margin-bottom: 4px;">{{ record.title }}</div>
          <p style="color: #666; font-size: 13px; line-height: 1.5;">{{ record.content }}</p>
          <div style="margin-top: 8px; color: #999; font-size: 12px;">医生：{{ record.doctorName || '系统记录' }}</div>
        </div>

        <div v-if="!loading && recordList.length === 0" style="text-align: center; padding: 40px;">
          <div style="font-size: 48px; margin-bottom: 12px;">📝</div>
          <p style="color: #999;">暂无健康记录</p>
          <el-button type="primary" plain size="small" style="margin-top: 12px;" @click="addRecord">添加第一条记录</el-button>
        </div>
      </div>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <router-link to="/pet" class="nav-item">
        <el-icon><Avatar /></el-icon>
        <span>宠物</span>
      </router-link>
      <router-link to="/health" class="nav-item">
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

    <!-- 添加记录弹窗 -->
    <el-dialog v-model="dialogVisible" title="添加健康记录" width="90%" style="border-radius: 20px; max-width: 400px;">
      <el-form :model="recordForm" label-width="70px" size="small">
        <el-form-item label="类型">
          <el-select v-model="recordForm.type" placeholder="选择类型" style="width: 100%">
            <el-option label="疫苗" value="vaccine" />
            <el-option label="驱虫" value="deworming" />
            <el-option label="体检" value="exam" />
            <el-option label="诊疗" value="treatment" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="recordForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="recordForm.content" :rows="3" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="日期">
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Avatar, Notebook, Calendar, ChatDotRound, User } from '@element-plus/icons-vue'
import { getPetDetail } from '@/api/owner/owner'
import { getHealthRecords, addHealthRecord } from '@/api/owner/owner'

const route = useRoute()
const router = useRouter()
const petId = route.params.id

const loading = ref(false)
const submitting = ref(false)
const petDetail = ref({})
const recordList = ref([])
const dialogVisible = ref(false)

const recordForm = ref({
  type: 'vaccine',
  title: '',
  content: '',
  recordDate: ''
})

const getRecordColor = (type) => {
  const colors = { vaccine: '#4cd964', deworming: '#f5a623', exam: '#409eff', treatment: '#ff6b6b' }
  return colors[type] || '#999'
}

const getTypeName = (type) => {
  const names = { vaccine: '疫苗', deworming: '驱虫', exam: '体检', treatment: '诊疗' }
  return names[type] || type
}

const goBack = () => {
  router.push('/pet')
}

const loadPetDetail = async () => {
  try {
    const res = await getPetDetail(petId)
    if (res.code === 200) {
      petDetail.value = res.data
    } else {
      // 模拟数据
      petDetail.value = {
        id: petId,
        name: '旺财',
        breed: '金毛',
        gender: 'male',
        age: 3,
        weight: 25,
        birthday: '2020-01-01',
        chipNumber: '123456789',
        description: '性格温顺，喜欢玩耍，对鸡肉过敏'
      }
    }
  } catch (error) {
    // 模拟数据
    petDetail.value = {
      id: petId,
      name: '旺财',
      breed: '金毛',
      gender: 'male',
      age: 3,
      weight: 25,
      birthday: '2020-01-01',
      chipNumber: '123456789',
      description: '性格温顺，喜欢玩耍，对鸡肉过敏'
    }
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getHealthRecords({ petId: petId, page: 1, pageSize: 50 })
    if (res.code === 200) {
      recordList.value = res.data.records || []
    }
  } catch {
    // 模拟数据
    recordList.value = [
      { id: 1, type: 'vaccine', title: '狂犬疫苗', content: '接种狂犬疫苗，一切正常', doctorName: '张医生', createTime: '2024-01-15' },
      { id: 2, type: 'deworming', title: '体内驱虫', content: '服用驱虫药，无不良反应', doctorName: '李医生', createTime: '2024-01-05' }
    ]
  } finally {
    loading.value = false
  }
}

const addRecord = () => {
  recordForm.value = {
    type: 'vaccine',
    title: '',
    content: '',
    recordDate: ''
  }
  dialogVisible.value = true
}

const submitRecord = async () => {
  if (!recordForm.value.title || !recordForm.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  submitting.value = true
  try {
    await addHealthRecord({
      petId: petId,
      ...recordForm.value
    })
    ElMessage.success('添加成功')
    dialogVisible.value = false
    loadRecords()
  } catch {
    ElMessage.success('添加成功（模拟）')
    dialogVisible.value = false
    loadRecords()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadPetDetail()
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
  color: #f5a623;
}

.nav-item .el-icon {
  font-size: 22px;
}
</style>