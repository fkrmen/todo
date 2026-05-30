<template>
  <div class="todo-page">
    <section class="page-hero">
      <div class="hero-copy">
        <span class="hero-badge">{{ isLoggedIn ? '已登录工作台' : '访客浏览模式' }}</span>
        <h1>今天要做什么</h1>
        <p>
          {{ isLoggedIn ? '把重要任务放进列表，按优先级和截止时间推进' : '首页可以先浏览，点击添加任务时再进入登录页' }}
        </p>
      </div>
      <div class="hero-progress">
        <el-progress
          type="circle"
          :percentage="completionRate"
          :width="96"
          :stroke-width="8"
          :color="progressColor"
        />
        <span class="progress-caption">{{ progressCaption }}</span>
      </div>
    </section>

    <section class="stats-row">
      <div class="stat-card total">
        <div class="stat-icon"><el-icon><List /></el-icon></div>
        <div class="stat-info">
          <span class="stat-number">{{ todos.length }}</span>
          <span class="stat-label">全部任务</span>
        </div>
      </div>
      <div class="stat-card pending">
        <div class="stat-icon"><el-icon><Clock /></el-icon></div>
        <div class="stat-info">
          <span class="stat-number">{{ undoneCount }}</span>
          <span class="stat-label">待完成</span>
        </div>
      </div>
      <div class="stat-card done">
        <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
        <div class="stat-info">
          <span class="stat-number">{{ doneCount }}</span>
          <span class="stat-label">已完成</span>
        </div>
      </div>
      <div class="stat-card accent">
        <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
        <div class="stat-info">
          <span class="stat-number">{{ completionRate }}%</span>
          <span class="stat-label">完成率</span>
        </div>
      </div>
    </section>

    <section class="add-section">
      <div class="section-head">
        <div>
          <h2>快速新增</h2>
          <p>输入标题后回车即可创建，也可以补充优先级和截止时间</p>
        </div>
      </div>

      <el-input
        v-model="newTodo.title"
        placeholder="添加一条新任务，按 Enter 可以快速创建"
        size="large"
        clearable
        class="add-input"
        @keyup.enter="addTodo"
      >
        <template #prefix>
          <el-icon><Plus /></el-icon>
        </template>
      </el-input>

      <div class="add-options">
        <el-select v-model="newTodo.level" class="level-select" size="large">
          <el-option label="低优先级" value="LOW" />
          <el-option label="中优先级" value="MEDIUM" />
          <el-option label="高优先级" value="HIGH" />
        </el-select>
        <el-date-picker
          v-model="newTodo.deadline"
          type="datetime"
          placeholder="截止时间"
          format="MM/DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
          :shortcuts="dateShortcuts"
          :disabled-date="disabledPastDate"
          clearable
          class="date-picker"
          popper-class="date-picker-popper"
        />
        <el-button type="primary" class="add-btn" :disabled="!newTodo.title.trim()" @click="addTodo">
          <el-icon><Plus /></el-icon>
          添加任务
        </el-button>
      </div>
    </section>

    <section class="filter-section">
      <div class="filter-tabs">
        <button
          v-for="tab in filterTabs"
          :key="tab.value"
          type="button"
          class="filter-tab"
          :class="{ active: activeFilter === tab.value }"
          @click="activeFilter = tab.value"
        >
          <span>{{ tab.label }}</span>
          <span class="tab-count">{{ tab.count }}</span>
        </button>
      </div>
    </section>

    <section v-if="displayTodos.length === 0" class="empty-state">
      <div class="empty-illustration">
        <el-icon :size="52"><FolderOpened /></el-icon>
      </div>
      <p class="empty-title">{{ emptyTitle }}</p>
      <p class="empty-desc">{{ emptyDesc }}</p>
    </section>

    <section v-else class="todo-grid">
      <article
        v-for="todo in displayTodos"
        :key="todo.id"
        class="todo-card"
        :class="[levelClass(todo.level), { completed: todo.done === 1 }]"
      >
        <div class="todo-card-left">
          <button type="button" class="check-wrapper" @click="toggleDone(todo)">
            <span class="custom-check" :class="{ checked: todo.done === 1 }">
              <el-icon v-if="todo.done === 1"><Check /></el-icon>
            </span>
          </button>
        </div>

        <div class="todo-card-body">
          <div class="todo-card-title" :class="{ done: todo.done === 1 }">
            {{ todo.title }}
          </div>
          <div class="todo-card-meta">
            <span class="level-badge" :class="levelClass(todo.level)">
              {{ levelLabel(todo.level) }}
            </span>
            <span v-if="todo.deadline" class="deadline-badge" :class="{ overdue: isOverdue(todo) }">
              <el-icon :size="14"><Clock /></el-icon>
              {{ formatDate(todo.deadline) }}
            </span>
            <span class="created-at">创建于 {{ formatDate(todo.createdAt) }}</span>
          </div>
        </div>

        <div class="todo-card-actions">
          <el-button circle size="small" class="action-btn edit-btn" @click="editTodo(todo)">
            <el-icon><Edit /></el-icon>
          </el-button>
          <el-button circle size="small" class="action-btn delete-btn" @click="deleteTodo(todo.id)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </article>
    </section>

    <el-dialog v-model="editDialogVisible" title="编辑任务" width="460px" class="edit-dialog">
      <el-form :model="editForm" label-width="0">
        <el-form-item>
          <el-input v-model="editForm.title" placeholder="任务标题" size="large" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="editForm.level" placeholder="优先级" size="large" style="width: 100%">
            <el-option label="低优先级" value="LOW" />
            <el-option label="中优先级" value="MEDIUM" />
            <el-option label="高优先级" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="editForm.deadline"
            type="datetime"
            placeholder="截止时间"
            format="MM/DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :shortcuts="dateShortcuts"
            :disabled-date="disabledPastDate"
            style="width: 100%"
            size="large"
            clearable
            popper-class="date-picker-popper"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" size="large" @click="saveEdit">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { isLoggedIn } = useAuth()

const todos = ref([])
const activeFilter = ref('all')
const editDialogVisible = ref(false)

const newTodo = reactive({
  title: '',
  level: 'MEDIUM',
  deadline: null
})

const editForm = reactive({
  id: null,
  title: '',
  level: 'MEDIUM',
  deadline: null
})

const dateShortcuts = [
  {
    text: '今天',
    value: () => {
      const date = new Date()
      date.setHours(23, 59, 0, 0)
      return date
    }
  },
  {
    text: '明天',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 1)
      date.setHours(23, 59, 0, 0)
      return date
    }
  },
  {
    text: '三天后',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 3)
      date.setHours(23, 59, 0, 0)
      return date
    }
  },
  {
    text: '一周后',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 7)
      date.setHours(23, 59, 0, 0)
      return date
    }
  }
]

function disabledPastDate(time) {
  return time.getTime() < Date.now() - 60 * 1000
}

const undoneCount = computed(() => todos.value.filter(item => item.done === 0).length)
const doneCount = computed(() => todos.value.filter(item => item.done === 1).length)

const completionRate = computed(() => {
  if (todos.value.length === 0) {
    return 0
  }
  return Math.round((doneCount.value / todos.value.length) * 100)
})

const progressColor = computed(() => {
  if (completionRate.value >= 80) {
    return '#67c23a'
  }
  if (completionRate.value >= 40) {
    return '#e6a23c'
  }
  return '#409eff'
})

const progressCaption = computed(() => {
  if (todos.value.length === 0) {
    return isLoggedIn.value ? '从第一条任务开始' : '先浏览，再决定是否登录'
  }
  return `${doneCount.value} / ${todos.value.length} 已完成`
})

const filterTabs = computed(() => [
  { label: '全部', value: 'all', count: todos.value.length },
  { label: '待完成', value: 'undone', count: undoneCount.value },
  { label: '已完成', value: 'done', count: doneCount.value }
])

const emptyTitle = computed(() => {
  if (!isLoggedIn.value) {
    return '首页可以先看，不必先登录'
  }
  if (activeFilter.value === 'done') {
    return '还没有已完成任务'
  }
  if (activeFilter.value === 'undone') {
    return '当前没有待完成任务'
  }
  return '还没有任何任务'
})

const emptyDesc = computed(() => {
  if (!isLoggedIn.value) {
    return '点击上方“添加任务”时才会跳转到登录页。'
  }
  if (activeFilter.value === 'done') {
    return '完成后的任务会收在这里，方便回顾。'
  }
  return '从上方输入框开始创建第一条任务。'
})

const displayTodos = computed(() => {
  let result = [...todos.value]

  if (activeFilter.value === 'undone') {
    result = result.filter(item => item.done === 0)
  } else if (activeFilter.value === 'done') {
    result = result.filter(item => item.done === 1)
  }

  result.sort((a, b) => {
    if (a.done !== b.done) {
      return a.done - b.done
    }

    const aDeadline = a.deadline ? new Date(a.deadline).getTime() : Number.MAX_SAFE_INTEGER
    const bDeadline = b.deadline ? new Date(b.deadline).getTime() : Number.MAX_SAFE_INTEGER
    return aDeadline - bDeadline
  })

  return result
})

function levelLabel(level) {
  return {
    LOW: '低优先级',
    MEDIUM: '中优先级',
    HIGH: '高优先级'
  }[level] || level
}

function levelClass(level) {
  return `level-${String(level || 'medium').toLowerCase()}`
}

function isOverdue(todo) {
  if (!todo.deadline || todo.done === 1) {
    return false
  }
  return new Date(todo.deadline).getTime() < Date.now()
}

function formatDate(value) {
  if (!value) {
    return ''
  }

  const date = new Date(value)
  const pad = num => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function fetchTodos() {
  if (!isLoggedIn.value) {
    todos.value = []
    return
  }

  const res = await request.get('/todo/list')
  if (res.code === 200) {
    todos.value = res.data || []
  }
}

async function addTodo() {
  if (!newTodo.title.trim()) {
    return
  }

  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  const res = await request.post('/todo/create', {
    title: newTodo.title.trim(),
    level: newTodo.level,
    deadline: newTodo.deadline || null
  })

  if (res.code === 200) {
    ElMessage.success('任务添加成功')
    newTodo.title = ''
    newTodo.deadline = null
    newTodo.level = 'MEDIUM'
    fetchTodos()
  }
}

async function toggleDone(todo) {
  if (!isLoggedIn.value) {
    return
  }

  const res = await request.put(`/todo/toggle/${todo.id}`)
  if (res.code === 200) {
    fetchTodos()
  }
}

function editTodo(todo) {
  if (!isLoggedIn.value) {
    return
  }

  editForm.id = todo.id
  editForm.title = todo.title
  editForm.level = todo.level
  editForm.deadline = todo.deadline
  editDialogVisible.value = true
}

async function saveEdit() {
  if (!isLoggedIn.value) {
    return
  }

  if (!editForm.title.trim()) {
    ElMessage.warning('任务标题不能为空')
    return
  }

  const res = await request.put(`/todo/update/${editForm.id}`, {
    title: editForm.title.trim(),
    level: editForm.level,
    deadline: editForm.deadline || null
  })

  if (res.code === 200) {
    ElMessage.success('任务更新成功')
    editDialogVisible.value = false
    fetchTodos()
  }
}

function deleteTodo(id) {
  if (!isLoggedIn.value) {
    return
  }

  ElMessageBox.confirm('确定删除这条任务吗？', '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const res = await request.delete(`/todo/delete/${id}`)
      if (res.code === 200) {
        ElMessage.success('任务删除成功')
        fetchTodos()
      }
    })
    .catch(() => {})
}

watch(isLoggedIn, value => {
  if (value) {
    fetchTodos()
  } else {
    todos.value = []
    editDialogVisible.value = false
  }
})

onMounted(fetchTodos)
</script>

<style scoped>
.todo-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-hero,
.stats-row,
.add-section,
.filter-section,
.empty-state,
.todo-card {
  border: 1px solid rgba(64, 158, 255, 0.08);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 18px 36px rgba(27, 39, 53, 0.06);
}

.page-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 30px;
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.74)),
    linear-gradient(120deg, rgba(255, 214, 126, 0.12), rgba(64, 158, 255, 0.12));
}

.hero-copy h1 {
  margin: 12px 0 10px;
  font-size: clamp(28px, 4vw, 36px);
  line-height: 1.14;
  color: var(--text-primary);
}

.hero-copy p {
  max-width: 520px;
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #705400;
  background: rgba(255, 214, 126, 0.24);
}

.hero-progress {
  min-width: 132px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.progress-caption {
  font-size: 13px;
  color: var(--text-tertiary);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0;
  overflow: hidden;
  border-radius: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 22px 20px;
  background: transparent;
}

.stat-card + .stat-card {
  border-left: 1px solid rgba(64, 158, 255, 0.08);
}

.stat-card.accent {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.08), rgba(64, 158, 255, 0.02));
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  font-size: 22px;
}

.stat-card.total .stat-icon {
  background: rgba(64, 158, 255, 0.12);
  color: var(--primary);
}

.stat-card.pending .stat-icon {
  background: rgba(230, 162, 60, 0.14);
  color: #d28a11;
}

.stat-card.done .stat-icon {
  background: rgba(103, 194, 58, 0.16);
  color: #5ba831;
}

.stat-card.accent .stat-icon {
  background: rgba(64, 158, 255, 0.1);
  color: var(--primary);
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-number {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--text-tertiary);
}

.add-section,
.filter-section,
.empty-state {
  padding: 24px;
  border-radius: 24px;
}

.section-head {
  margin-bottom: 16px;
}

.section-head h2 {
  margin: 0 0 6px;
  font-size: 18px;
  color: var(--text-primary);
}

.section-head p {
  font-size: 13px;
  color: var(--text-tertiary);
}

.add-input {
  margin-bottom: 12px;
}

.add-input :deep(.el-input__wrapper),
.level-select :deep(.el-input__wrapper),
.date-picker :deep(.el-input__wrapper) {
  min-height: 46px;
  border-radius: 14px;
  background: #fbfcfe;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.08) inset;
}

.add-input :deep(.el-input__wrapper.is-focus),
.level-select :deep(.el-input__wrapper.is-focus),
.date-picker :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.16) inset !important;
}

.add-options {
  display: flex;
  align-items: center;
  gap: 12px;
}

.level-select {
  width: 150px;
}

.date-picker {
  flex: 1;
}

.add-btn {
  height: 46px;
  padding: 0 22px;
  border-radius: 14px;
  font-weight: 700;
}

.filter-section {
  padding: 12px;
}

.filter-tabs {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  background: transparent;
  cursor: pointer;
  transition: all 0.25s;
}

.filter-tab:hover,
.filter-tab.active {
  color: var(--primary);
  background: rgba(64, 158, 255, 0.1);
}

.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  padding: 0 7px;
  border-radius: 999px;
  font-size: 12px;
  color: var(--text-tertiary);
  background: rgba(64, 158, 255, 0.08);
}

.filter-tab.active .tab-count {
  color: #fff;
  background: var(--primary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 56px 24px;
}

.empty-illustration {
  width: 82px;
  height: 82px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 26px;
  color: var(--primary);
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.12), rgba(255, 214, 126, 0.16));
}

.empty-title {
  margin: 18px 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.empty-desc {
  max-width: 360px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-tertiary);
}

.todo-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 20px;
  border-left: 4px solid transparent;
  transition: transform 0.24s, box-shadow 0.24s;
}

.todo-card:hover {
  transform: translateY(-2px);
}

.todo-card.level-low {
  border-left-color: #67c23a;
}

.todo-card.level-medium {
  border-left-color: #e6a23c;
}

.todo-card.level-high {
  border-left-color: #f56c6c;
}

.todo-card.completed {
  opacity: 0.72;
}

.check-wrapper {
  border: none;
  background: transparent;
  cursor: pointer;
}

.custom-check {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--border);
  border-radius: 50%;
  color: #fff;
  font-size: 14px;
  transition: all 0.24s;
}

.custom-check:hover {
  border-color: var(--primary);
}

.custom-check.checked {
  background: #67c23a;
  border-color: #67c23a;
}

.todo-card-body {
  flex: 1;
  min-width: 0;
}

.todo-card-title {
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  word-break: break-word;
}

.todo-card-title.done {
  color: var(--text-disabled);
  text-decoration: line-through;
}

.todo-card-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.level-badge,
.deadline-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.level-badge.level-low {
  background: rgba(103, 194, 58, 0.12);
  color: #54992f;
}

.level-badge.level-medium {
  background: rgba(230, 162, 60, 0.14);
  color: #cc8616;
}

.level-badge.level-high {
  background: rgba(245, 108, 108, 0.14);
  color: #db5252;
}

.deadline-badge {
  color: var(--text-tertiary);
  background: rgba(64, 158, 255, 0.08);
}

.deadline-badge.overdue {
  color: #d95050;
  background: rgba(245, 108, 108, 0.12);
}

.created-at {
  font-size: 12px;
  color: var(--text-disabled);
}

.todo-card-actions {
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.24s;
}

.todo-card:hover .todo-card-actions {
  opacity: 1;
}

.action-btn {
  width: 34px !important;
  height: 34px !important;
  border: none !important;
}

.edit-btn {
  color: var(--primary) !important;
  background: rgba(64, 158, 255, 0.12) !important;
}

.delete-btn {
  color: var(--danger) !important;
  background: rgba(245, 108, 108, 0.12) !important;
}

@media (max-width: 768px) {
  .page-hero {
    flex-direction: column;
    align-items: flex-start;
    padding: 24px;
  }

  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-card + .stat-card {
    border-left: none;
  }

  .stat-card:nth-child(n + 3) {
    border-top: 1px solid rgba(64, 158, 255, 0.08);
  }

  .add-options {
    flex-direction: column;
  }

  .level-select,
  .date-picker,
  .add-btn {
    width: 100%;
  }

  .todo-card {
    align-items: flex-start;
  }

  .todo-card-actions {
    opacity: 1;
  }
}
</style>

<style>
.date-picker-popper {
  border: none !important;
  border-radius: 14px !important;
  box-shadow: 0 10px 26px rgba(27, 39, 53, 0.12) !important;
}

.date-picker-popper .el-picker-panel__shortcut:hover {
  color: var(--primary);
}

.date-picker-popper .el-date-table td.today span {
  color: var(--primary);
  font-weight: 600;
}

.date-picker-popper .el-date-table td.current:not(.disabled) span {
  background-color: var(--primary) !important;
}
</style>
