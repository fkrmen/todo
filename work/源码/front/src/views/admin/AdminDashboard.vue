<template>
  <div class="admin-page">
    <section class="admin-hero">
      <div>
        <span class="hero-badge">后台总览</span>
        <h1>集中管理用户和系统公告</h1>
        <p>By Dormitory-111</p>
      </div>
      <div class="hero-metrics">
        <div class="metric-card">
          <span class="metric-label">用户总数</span>
          <strong>{{ users.length }}</strong>
        </div>
        <div class="metric-card">
          <span class="metric-label">公告总数</span>
          <strong>{{ notices.length }}</strong>
        </div>
      </div>
    </section>

    <section class="tab-header">
      <button type="button" class="tab-pill" :class="{ active: activeTab === 'users' }" @click="activeTab = 'users'">
        <el-icon><User /></el-icon>
        用户管理
        <span class="tab-badge">{{ users.length }}</span>
      </button>
      <button type="button" class="tab-pill" :class="{ active: activeTab === 'notices' }" @click="activeTab = 'notices'">
        <el-icon><Bell /></el-icon>
        公告管理
        <span class="tab-badge">{{ notices.length }}</span>
      </button>
    </section>

    <section v-show="activeTab === 'users'" class="tab-content">
      <div class="section-card">
        <div class="section-header">
          <div>
            <h2>用户列表</h2>
            <p>搜索用户名，管理账号状态、密码和登录会话。</p>
          </div>
          <el-input
            v-model="userSearch"
            placeholder="搜索用户名"
            :prefix-icon="Search"
            clearable
            size="large"
            class="user-search"
          />
        </div>

        <el-table :data="filteredUsers" stripe class="custom-table">
          <el-table-column prop="id" label="ID" width="68" align="center" />
          <el-table-column prop="username" label="用户名" min-width="150">
            <template #default="{ row }">
              <span class="user-cell">
                <el-avatar :size="30" :icon="UserFilled" />
                {{ row.username }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" min-width="220">
            <template #default="{ row }">
              <span class="email-cell">{{ row.email || '未设置' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small" effect="light" round>
                {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <span class="status-pill" :class="row.status === 1 ? 'active' : 'disabled'">
                <span class="status-dot"></span>
                {{ row.status === 1 ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" width="176">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="290" fixed="right">
            <template #default="{ row }">
              <div class="action-group">
                <el-button
                  size="small"
                  :type="row.status === 1 ? 'warning' : 'success'"
                  class="op-btn"
                  :disabled="row.role === 'ADMIN'"
                  @click="toggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button
                  size="small"
                  type="primary"
                  class="op-btn"
                  :disabled="row.role === 'ADMIN'"
                  @click="resetPwd(row)"
                >
                  重置密码
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  class="op-btn"
                  :disabled="row.role === 'ADMIN'"
                  @click="forceLogout(row)"
                >
                  强制下线
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <section v-show="activeTab === 'notices'" class="tab-content">
      <div class="section-card">
        <div class="section-header">
          <div>
            <h2>公告列表</h2>
            <p>发布系统消息，更新后会在用户侧展示。</p>
          </div>
          <el-button type="primary" class="notice-add-btn" @click="showCreateNotice">
            <el-icon><Plus /></el-icon>
            发布公告
          </el-button>
        </div>

        <el-table v-if="notices.length > 0" :data="notices" stripe class="custom-table">
          <el-table-column prop="id" label="ID" width="68" align="center" />
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column prop="content" label="内容" min-width="320" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="发布时间" width="176">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-group">
                <el-button type="primary" size="small" class="op-btn" @click="showEditNotice(row)">编辑</el-button>
                <el-button type="danger" size="small" class="op-btn" @click="deleteNotice(row.id)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div v-else class="empty-notice">
          <el-icon :size="48" color="#dcdfe6"><Document /></el-icon>
          <p>暂无公告，点击上方按钮即可发布。</p>
        </div>
      </div>
    </section>

    <el-dialog
      v-model="noticeDialogVisible"
      :title="noticeEditMode === 'create' ? '发布公告' : '编辑公告'"
      width="520px"
      class="notice-dialog"
    >
      <el-form :model="noticeForm" label-position="top">
        <el-form-item label="公告标题">
          <el-input v-model="noticeForm.title" placeholder="请输入标题" size="large" />
        </el-form-item>
        <el-form-item label="公告内容">
          <el-input v-model="noticeForm.content" type="textarea" :rows="6" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="noticeDialogVisible = false">取消</el-button>
        <el-button type="primary" size="large" @click="saveNotice">
          {{ noticeEditMode === 'create' ? '发布' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, UserFilled } from '@element-plus/icons-vue'
import request from '../../api/request'

const activeTab = ref('users')
const userSearch = ref('')
const users = ref([])
const notices = ref([])
const noticeDialogVisible = ref(false)
const noticeEditMode = ref('create')

const noticeForm = reactive({
  id: null,
  title: '',
  content: ''
})

const filteredUsers = computed(() => {
  const keyword = userSearch.value.trim().toLowerCase()
  if (!keyword) {
    return users.value
  }
  return users.value.filter(user => (user.username || '').toLowerCase().includes(keyword))
})

async function fetchUsers() {
  const res = await request.get('/admin/users')
  if (res.code === 200) {
    users.value = res.data || []
  }
}

async function fetchNotices() {
  const res = await request.get('/notice/all')
  if (res.code === 200) {
    notices.value = res.data || []
  }
}

function toggleStatus(row) {
  const actionText = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${actionText}用户“${row.username}”吗？`, '提示', {
    confirmButtonText: actionText,
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const res = await request.put(`/admin/users/${row.id}/toggle-status`)
      if (res.code === 200) {
        ElMessage.success('操作成功')
        fetchUsers()
      }
    })
    .catch(() => {})
}

function resetPwd(row) {
  ElMessageBox.confirm(`确定将用户“${row.username}”的密码重置为 123456 吗？`, '提示', {
    confirmButtonText: '重置',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const res = await request.put(`/admin/users/${row.id}/reset-password`)
      if (res.code === 200) {
        ElMessage.success(res.data || '密码已重置')
      }
    })
    .catch(() => {})
}

function forceLogout(row) {
  ElMessageBox.confirm(`确定强制用户“${row.username}”下线吗？`, '提示', {
    confirmButtonText: '强制下线',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const res = await request.put(`/admin/users/${row.id}/force-logout`)
      if (res.code === 200) {
        ElMessage.success('已强制下线')
      }
    })
    .catch(() => {})
}

function showCreateNotice() {
  noticeEditMode.value = 'create'
  noticeForm.id = null
  noticeForm.title = ''
  noticeForm.content = ''
  noticeDialogVisible.value = true
}

function showEditNotice(row) {
  noticeEditMode.value = 'edit'
  noticeForm.id = row.id
  noticeForm.title = row.title
  noticeForm.content = row.content
  noticeDialogVisible.value = true
}

async function saveNotice() {
  if (!noticeForm.title.trim() || !noticeForm.content.trim()) {
    ElMessage.warning('请填写完整的公告信息')
    return
  }

  const payload = {
    title: noticeForm.title.trim(),
    content: noticeForm.content.trim()
  }

  const res =
    noticeEditMode.value === 'create'
      ? await request.post('/notice/create', payload)
      : await request.put(`/notice/update/${noticeForm.id}`, payload)

  if (res.code === 200) {
    ElMessage.success(noticeEditMode.value === 'create' ? '公告发布成功' : '公告更新成功')
    noticeDialogVisible.value = false
    fetchNotices()
  }
}

function deleteNotice(id) {
  ElMessageBox.confirm('确定删除这条公告吗？', '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const res = await request.delete(`/notice/delete/${id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchNotices()
      }
    })
    .catch(() => {})
}

function formatDate(value) {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  const pad = num => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

onMounted(() => {
  fetchUsers()
  fetchNotices()
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.admin-hero,
.section-card,
.tab-header {
  border: 1px solid rgba(64, 158, 255, 0.08);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 36px rgba(27, 39, 53, 0.06);
}

.admin-hero {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 30px;
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.76)),
    linear-gradient(120deg, rgba(255, 214, 126, 0.12), rgba(64, 158, 255, 0.12));
}

.hero-badge {
  display: inline-flex;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #705400;
  background: rgba(255, 214, 126, 0.24);
}

.admin-hero h1 {
  margin: 12px 0 10px;
  font-size: clamp(28px, 4vw, 36px);
  line-height: 1.14;
  color: var(--text-primary);
}

.admin-hero p {
  max-width: 560px;
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.hero-metrics {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.metric-card {
  min-width: 138px;
  padding: 18px 18px 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(64, 158, 255, 0.08);
}

.metric-label {
  display: block;
  font-size: 13px;
  color: var(--text-tertiary);
}

.metric-card strong {
  display: block;
  margin-top: 12px;
  font-size: 34px;
  color: var(--text-primary);
}

.tab-header {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  border-radius: 24px;
  width: fit-content;
}

.tab-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.24s ease;
}

.tab-pill:hover,
.tab-pill.active {
  color: var(--primary);
  background: rgba(64, 158, 255, 0.1);
}

.tab-badge {
  min-width: 24px;
  height: 24px;
  padding: 0 7px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: rgba(64, 158, 255, 0.12);
  font-size: 12px;
  font-weight: 700;
}

.tab-pill.active .tab-badge {
  color: #fff;
  background: var(--primary);
}

.tab-content {
  animation: fade-in 0.24s ease;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-card {
  padding: 24px;
  border-radius: 24px;
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0 0 6px;
  font-size: 18px;
  color: var(--text-primary);
}

.section-header p {
  font-size: 13px;
  color: var(--text-tertiary);
}

.user-search {
  width: 240px;
}

.user-search :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 14px;
  background: #fbfcfe;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.08) inset;
}

.custom-table :deep(.el-table__header th) {
  background: #f8fbff;
  color: var(--text-secondary);
  font-weight: 700;
  border-bottom: 1px solid rgba(64, 158, 255, 0.08);
}

.custom-table :deep(.el-table__row:hover td) {
  background: #f7fbff;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}

.email-cell {
  color: var(--text-tertiary);
  font-size: 13px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status-pill.active {
  color: #5ba831;
  background: rgba(103, 194, 58, 0.14);
}

.status-pill.disabled {
  color: var(--text-tertiary);
  background: rgba(144, 147, 153, 0.14);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.action-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.op-btn {
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.notice-add-btn {
  height: 42px;
  padding: 0 18px;
  border-radius: 14px;
  font-weight: 700;
}

.empty-notice {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  color: var(--text-tertiary);
}

.empty-notice p {
  margin-top: 12px;
  font-size: 14px;
}

@media (max-width: 900px) {
  .admin-hero {
    flex-direction: column;
  }

  .hero-metrics {
    width: 100%;
  }

  .metric-card {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
  }

  .user-search,
  .notice-add-btn {
    width: 100%;
  }

  .tab-header {
    width: 100%;
  }
}
</style>
