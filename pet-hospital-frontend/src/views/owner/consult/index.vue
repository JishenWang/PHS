<template>
  <div class="page-content">
    <!-- 头部 -->
    <div class="header" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 20px 60px; border-radius: 0 0 40px 40px;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <h2 style="color: white; font-size: 28px; margin-bottom: 8px;">在线咨询</h2>
          <p style="color: rgba(255,255,255,0.9); font-size: 14px;">专业医生在线解答</p>
        </div>
        <el-button circle :icon="Plus" style="background: rgba(255,255,255,0.2); border: none; color: white; width: 44px; height: 44px;" @click="handleCreate" />
      </div>
    </div>

    <!-- 咨询列表 -->
    <div style="padding: 20px;" v-loading="loading">
      <div v-for="item in consultList" :key="item.id" 
           style="background: white; border-radius: 20px; margin-bottom: 15px; padding: 16px; box-shadow: 0 2px 10px rgba(0,0,0,0.03); cursor: pointer;"
           @click="viewDetail(item.id)">
        <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
          <span style="color: #999; font-size: 12px;">{{ item.createTime }}</span>
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-icon><ChatLineSquare /></el-icon>
            <span style="color: #999; font-size: 12px;">{{ item.replyCount || 0 }}条回复</span>
          </div>
        </div>
        
        <div style="font-weight: bold; font-size: 16px; margin-bottom: 8px;">{{ item.title }}</div>
        <p style="color: #666; font-size: 13px; line-height: 1.5; margin-bottom: 12px;">{{ item.content }}</p>
        
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-avatar :size="28" style="background: #667eea;">
              <el-icon size="16"><User /></el-icon>
            </el-avatar>
            <span style="font-size: 12px; color: #666;">{{ item.doctorName || '等待接诊' }}</span>
          </div>
          <el-icon color="#999"><ArrowRight /></el-icon>
        </div>
      </div>

      <div v-if="!loading && consultList.length === 0" style="text-align: center; padding: 60px 20px;">
        <div style="font-size: 60px; margin-bottom: 16px;">💬</div>
        <p style="color: #999;">暂无咨询记录</p>
        <el-button type="primary" plain style="margin-top: 16px;" @click="handleCreate">发起咨询</el-button>
      </div>
    </div>

    

    <!-- 发起咨询弹窗 -->
    <el-dialog v-model="dialogVisible" title="发起咨询" width="90%" style="border-radius: 20px; max-width: 400px;">
      <el-form :model="form" label-width="70px" size="small">
        <el-form-item label="选择宠物">
          <el-select v-model="form.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option v-for="pet in petList" :key="pet.id" :label="pet.name" :value="pet.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择医生">
          <el-select v-model="form.doctorId" placeholder="请选择医生" style="width: 100%">
            <el-option v-for="doc in doctorList" :key="doc.id" :label="doc.name" :value="doc.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input type="textarea" v-model="form.content" :rows="4" placeholder="请详细描述您的问题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">提交咨询</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Avatar, Notebook, Calendar, ChatDotRound, User, ChatLineSquare, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const consultList = ref([])
const dialogVisible = ref(false)

const petList = ref([
  { id: 1, name: '旺财' },
  { id: 2, name: '咪咪' }
])

const doctorList = ref([
  { id: 1, name: '张医生' },
  { id: 2, name: '李医生' }
])

const form = ref({
  petId: '',
  doctorId: '',
  title: '',
  content: ''
})

// 加载咨询列表
const loadConsultList = () => {
  loading.value = true
  setTimeout(() => {
    consultList.value = [
      { id: 1, title: '狗狗不吃东西', content: '我家狗狗最近两天都不怎么吃东西，精神也不太好...', doctorName: '张医生', replyCount: 2, createTime: '2024-01-15 10:30', petName: '旺财' },
      { id: 2, title: '猫咪打喷嚏', content: '猫咪最近总是打喷嚏，是不是感冒了？', doctorName: '李医生', replyCount: 3, createTime: '2024-01-10 14:20', petName: '咪咪' },
      { id: 3, title: '皮肤红疹', content: '狗狗身上出现红疹，很痒一直抓', doctorName: '张医生', replyCount: 1, createTime: '2024-01-08 09:15', petName: '旺财' }
    ]
    loading.value = false
  }, 300)
}

const handleCreate = () => {
  form.value = { petId: '', doctorId: '', title: '', content: '' }
  dialogVisible.value = true
}

const submitForm = () => {
  if (!form.value.petId) {
    ElMessage.warning('请选择宠物')
    return
  }
  if (!form.value.doctorId) {
    ElMessage.warning('请选择医生')
    return
  }
  if (!form.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.value.content) {
    ElMessage.warning('请输入问题描述')
    return
  }
  
  submitting.value = true
  setTimeout(() => {
    ElMessage.success('咨询提交成功，医生会尽快回复')
    dialogVisible.value = false
    loadConsultList()
    submitting.value = false
  }, 500)
}

const viewDetail = (id) => {
  router.push(`/consult/${id}`)
}

onMounted(() => {
  loadConsultList()
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
  color: #667eea;
}

.nav-item .el-icon {
  font-size: 22px;
}
</style>