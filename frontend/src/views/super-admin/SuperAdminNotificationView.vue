<template>
  <div class="admin-notification-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <i class="iconfont icon-megaphone page-header-icon"/>
        <h1 class="page-title">重要通知管理</h1>
      </div>
    </div>

    <!-- 模板列表 -->
    <div class="table-card" v-loading="tableLoading">
      <h3 class="section-title">
        <i class="iconfont icon-list section-icon"/>
        通知模板列表
      </h3>

      <el-empty v-if="!tableLoading && templateList.length === 0" description="暂无通知模板" />
      <template v-else>
        <el-table max-height="520px" :data="templateList" style="width: 100%">
          <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span :title="row.title">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column label="内容摘要" min-width="200">
            <template #default="{ row }">
              {{ row.content ? row.content.slice(0, 30) + (row.content.length > 30 ? '...' : '') : '' }}
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="90">
            <template #default="{ row }">
              <el-tag :type="getPriorityType(row.priority)" size="small">
                {{ getPriorityLabel(row.priority) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                {{ row.status === 'ACTIVE' ? '活跃' : '已关闭' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="broadcastCount" label="广播次数" width="100">
            <template #default="{ row }">
              {{ row.broadcastCount > 0 ? row.broadcastCount : '未广播' }}
            </template>
          </el-table-column>
          <el-table-column prop="broadcastTime" label="最近广播" width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.broadcastTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 'ACTIVE'">
                <CustomButton
                  type="link"
                  size="small"
                  @click="handleBroadcastTemplate(row)"
                  color="primary"
                >
                  广播
                </CustomButton>
                <CustomButton
                  type="link"
                  plain
                  size="small"
                  color="danger"
                  @click="handleCloseTemplate(row)"
                >
                  关闭
                </CustomButton>
              </template>
              <span v-else class="disabled-text">—</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            style="--el-border-radius-base: var(--radius-md);"
          />
        </div>
      </template>
    </div>

    <!-- 发布表单 -->
    <div class="publish-card" v-loading="broadcasting">
      <el-form
        ref="formRef"
        :model="noticeForm"
        label-position="top"
        class="publish-form"
      >
        <div class="form-section">
          <h3 class="section-title">
            <i class="iconfont icon-edit section-icon"/>
            新建并广播通知
          </h3>

          <el-form-item
            label="通知标题"
            :rules="[{ required: true, message: '请输入通知标题', trigger: 'blur' }]"
          >
            <CustomInput
              v-model="noticeForm.title"
              placeholder="请输入通知标题，建议简短有力"
              :maxlength="100"
              showWordLimit
            />
          </el-form-item>

          <el-form-item
            label="通知正文（支持 Markdown 格式）"
            :rules="[{ required: true, message: '请输入通知正文', trigger: 'blur' }]"
          >
            <MdEditor
              v-model="noticeForm.content"
              language="zh-CN"
              :theme="themeStore.theme"
              :toolbars="editorToolbars"
              :preview="true"
              :footers="[]"
              placeholder="在此输入 Markdown 格式的通知内容..."
            />
          </el-form-item>
        </div>

        <div class="form-section tips-section">
          <h3 class="section-title">
            <i class="iconfont icon-info-box section-icon"/>
            发布须知
          </h3>
          <ul class="tips-list">
            <li>通知发布后，将<strong>立即推送给所有普通用户</strong>（不包括 SuperAdmin）</li>
            <li>每位用户会收到独立的通知记录，支持各自标记已读</li>
            <li>支持 Markdown 语法：标题、列表、链接、代码块、表格等</li>
            <li>当前版本<strong>不支持撤回</strong>，请确认内容后再发布</li>
            <li>已关闭的模板新注册用户将不再收到，但已收到用户不受影响</li>
          </ul>
        </div>

        <div class="form-footer">
          <CustomButton
            type="primary"
            size="large"
            :loading="broadcasting"
            :disabled="!isFormValid"
            @click="handleBroadcast"
          >
            <i class="iconfont icon-mail-arrow-right" style="margin-right: 8px;"/>
            发布通知
          </CustomButton>
        </div>
      </el-form>
    </div>

    <!-- 广播确认 -->
    <ConfirmDialog
      v-model:visible="broadcastConfirmVisible"
      title="广播通知"
      message="确定向全体普通用户广播该通知？"
      confirm-text="确定广播"
      cancel-text="取消"
      @confirm="confirmBroadcastTemplate"
      width="340px"
    />

    <!-- 关闭确认 -->
    <ConfirmDialog
      v-model:visible="closeConfirmVisible"
      title="关闭通知"
      message="关闭后新注册用户将不再收到此通知，已收到用户不受影响。确定关闭？"
      confirm-text="确定关闭"
      cancel-text="取消"
      @confirm="confirmCloseTemplate"
      width="440px"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import CustomButton from '@/components/ui/CustomButton.vue'
import CustomInput from '@/components/ui/CustomInput.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import { useThemeStore } from '@/stores/theme.js'
import {
  broadcastImportantNotice,
  getImportantNoticeList,
  broadcastImportantNoticeById,
  closeImportantNotice
} from '@/api/notification.js'
import showMessage from '@/utils/message.js'

// ==================== 模板列表 ====================
const tableLoading = ref(false)
const templateList = ref([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ==================== 广播/关闭确认对话框 ====================
const broadcastConfirmVisible = ref(false)
const closeConfirmVisible = ref(false)
const selectedTemplate = ref(null)

// ==================== 原有表单数据 ====================
const formRef = ref(null)
const broadcasting = ref(false)
const themeStore = useThemeStore()

const noticeForm = reactive({
  title: '',
  content: ''
})

const editorToolbars = [
  'bold',
  'italic',
  'strikeThrough',
  'title',
  'sub',
  'sup',
  'quote',
  'unorderedList',
  'orderedList',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  'preview',
  'fullscreen'
]

const isFormValid = computed(() => {
  return noticeForm.title.trim().length > 0 && noticeForm.content.trim().length > 0
})

// ==================== 格式化函数 ====================
const formatDateTime = (dateStr) => {
  if (!dateStr) return '—'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).replace(/\//g, '-')
}

const getPriorityLabel = (priority) => {
  const map = { 0: '普通', 1: '警告', 2: '紧急' }
  return map[priority] ?? '普通'
}

const getPriorityType = (priority) => {
  const map = { 0: 'info', 1: 'warning', 2: 'danger' }
  return map[priority] ?? 'info'
}

// ==================== 数据获取 ====================
const fetchTemplateList = async () => {
  tableLoading.value = true
  try {
    const res = await getImportantNoticeList(pagination.page, pagination.size)
    if (res.code === 200 && res.data) {
      const data = res.data
      if (Array.isArray(data.list)) {
        templateList.value = data.list
        pagination.total = data.total || 0
      } else {
        templateList.value = []
        pagination.total = 0
      }
    } else {
      templateList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取通知模板列表失败:', error)
    templateList.value = []
    pagination.total = 0
  } finally {
    tableLoading.value = false
  }
}

// ==================== 广播操作 ====================
const handleBroadcastTemplate = (row) => {
  selectedTemplate.value = row
  broadcastConfirmVisible.value = true
}

const confirmBroadcastTemplate = async () => {
  if (!selectedTemplate.value) return
  try {
    const res = await broadcastImportantNoticeById(selectedTemplate.value.id)
    if (res.code === 200) {
      showMessage.success('广播成功')
      broadcastConfirmVisible.value = false
      fetchTemplateList()
    } else {
      showMessage.error(res.message || '广播失败')
    }
  } catch (error) {
    console.error('广播通知失败:', error)
    const status = error.response?.status
    const message = error.response?.data?.message || ''
    if (status === 429) {
      showMessage.warning(message || '该通知广播过于频繁，请10分钟后再试')
    } else if (status === 404) {
      showMessage.error('通知模板不存在，请刷新后重试')
    } else if (status === 400) {
      if (message.includes('已关闭')) {
        showMessage.error('该通知已关闭，无法广播')
      } else {
        showMessage.error(message || '广播失败')
      }
    }
  }
}

// ==================== 关闭操作 ====================
const handleCloseTemplate = (row) => {
  selectedTemplate.value = row
  closeConfirmVisible.value = true
}

const confirmCloseTemplate = async () => {
  if (!selectedTemplate.value) return
  try {
    const res = await closeImportantNotice(selectedTemplate.value.id)
    if (res.code === 200) {
      showMessage.success('已关闭')
      closeConfirmVisible.value = false
      fetchTemplateList()
    } else {
      showMessage.error(res.message || '关闭失败')
    }
  } catch (error) {
    console.error('关闭通知失败:', error)
    const status = error.response?.status
    const message = error.response?.data?.message || ''
    if (status === 404) {
      showMessage.error('通知模板不存在，请刷新后重试')
    } else {
      showMessage.error(message || '关闭失败')
    }
  }
}

// ==================== 分页 ====================
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchTemplateList()
}

const handlePageChange = (page) => {
  pagination.page = page
  fetchTemplateList()
}

// ==================== 新建并广播 ====================
const handleBroadcast = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  broadcasting.value = true
  try {
    const res = await broadcastImportantNotice({
      title: noticeForm.title.trim(),
      content: noticeForm.content.trim()
    })
    if (res.code === 200) {
      showMessage.success('重要通知已发布，所有普通用户将收到弹窗提醒')
      noticeForm.title = ''
      noticeForm.content = ''
      formRef.value?.resetFields()
      fetchTemplateList()
    } else {
      showMessage.error(res.message || '发布失败')
    }
  } catch (error) {
    console.error('发布重要通知失败:', error)
    const message = error.response?.data?.message || ''
    if (error.response?.status === 400 && message.includes('重复')) {
      showMessage.error('相同标题的通知在5分钟内已广播过')
    } else {
      showMessage.error('发布失败，请稍后重试')
    }
  } finally {
    broadcasting.value = false
  }
}

onMounted(() => {
  fetchTemplateList()
})
</script>

<style scoped lang="scss">
.admin-notification-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.page-header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.page-header-icon {
  font-size: 28px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.table-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  margin-bottom: var(--space-6);
}

.publish-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.form-section {
  margin-bottom: var(--space-6);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-5);
}

.section-icon {
  font-size: 18px;
  color: var(--primary-color);
}

.tips-section {
  background: var(--primary-10);
  border-radius: var(--radius-md);
  padding: var(--space-5);
  border: 1px dashed var(--primary-30);
}

.tips-list {
  margin: 0;
  padding-left: var(--space-5);
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.8;

  li {
    margin-bottom: var(--space-2);

    &:last-child {
      margin-bottom: 0;
    }

    strong {
      color: var(--danger-color);
      font-weight: 600;
    }
  }
}

.form-footer {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
  padding-top: var(--space-5);
  border-top: 1px solid var(--border-color);
}

.pagination-wrapper {
  margin-top: var(--space-6);
  display: flex;
  justify-content: flex-end;
}

.disabled-text {
  color: var(--text-tertiary);
  font-size: 14px;
}

// MdEditor 浅色模式主题适配
:deep(.md-editor) {
  --md-color: var(--text-primary);
  --md-hover-color: var(--primary-color);
  --md-bk-color: var(--card-bg);
  --md-bk-color-outstand: var(--card-bg);
  --md-bk-hover-color: var(--primary-10);
  --md-border-color: var(--border-color);
  --md-border-hover-color: var(--primary-color);
  --md-border-active-color: var(--primary-color);
  --md-modal-mask: rgba(0, 0, 0, 0.45);
  --md-scrollbar-bg-color: var(--primary-10);
  --md-scrollbar-thumb-color: var(--primary-30);
  --md-scrollbar-thumb-hover-color: var(--primary-40);
  --md-scrollbar-thumb-active-color: var(--primary-50);

  border-radius: var(--radius-md);
  border: none;
  box-shadow: 0 0 0 2px var(--gray-40);
  background-color: transparent;

  .md-editor-toolbar-wrapper {
    border: none;
  }

  .md-editor-toolbar {
    background-color: var(--card-bg);
  }

  .md-editor-toolbar-item {
    color: var(--text-secondary);

    &:hover {
      color: var(--primary-color);
      background-color: var(--primary-10);
    }
  }

  .md-editor-toolbar-item.md-editor-toolbar-active {
    background-color: var(--primary-20);
  }

  .md-editor-toolbar-item-name {
    color: var(--text-secondary);
  }

  .md-editor-input-wrapper {

    textarea,
    .md-editor-input {
      background-color: var(--input-bg) !important;
      color: var(--text-primary) !important;
    }
  }

  .md-editor-preview-wrapper {
    background-color: var(--card-bg);
    border-left: 1px solid var(--border-color);
  }

  .md-editor-preview {
    color: var(--text-primary);

    p, li, h1, h2, h3, h4, h5, h6 {
      color: var(--text-primary);
    }
  }

  .md-editor-content {
    background-color: var(--input-bg);
  }
}

// MdEditor 深色模式主题适配
.dark :deep(.md-editor) {
  --md-color: var(--text-primary);
  --md-hover-color: var(--text-primary);
  --md-bk-color: var(--input-bg);
  --md-bk-color-outstand: var(--card-bg);
  --md-bk-hover-color: rgba(255, 255, 255, 0.05);
  --md-border-color: var(--border-color);
  --md-border-hover-color: var(--primary-color);
  --md-border-active-color: var(--primary-color);
  --md-modal-mask: rgba(0, 0, 0, 0.65);
  --md-scrollbar-bg-color: var(--primary-10);
  --md-scrollbar-thumb-color: var(--primary-30);
  --md-scrollbar-thumb-hover-color: var(--primary-40);
  --md-scrollbar-thumb-active-color: var(--primary-50);

  background-color: var(--md-bk-color);

  .md-editor-toolbar {
    background-color: var(--card-bg);
    border-bottom-color: var(--border-color);
  }

  .md-editor-toolbar-item {
    color: var(--text-secondary);

    &:hover {
      color: var(--primary-color);
      background-color: rgba(255, 255, 255, 0.05);
    }
  }

  .md-editor-toolbar-item-name {
    color: var(--text-secondary);
  }

  .md-editor-input-wrapper {
    background-color: var(--input-bg);

    textarea,
    .md-editor-input {
      background-color: var(--input-bg) !important;
      color: var(--text-primary) !important;
      caret-color: var(--primary-color);
    }
  }

  .md-editor-preview-wrapper {
    background-color: var(--card-bg);
    border-left-color: var(--border-color);
  }

  .md-editor-preview {
    color: var(--text-primary);

    p, li, h1, h2, h3, h4, h5, h6 {
      color: var(--text-primary);
    }

    code {
      background: rgba(255, 255, 255, 0.08);
      color: var(--primary-color);
    }

    pre {
      background: rgba(0, 0, 0, 0.3);
    }
  }

  .md-editor-content {
    background-color: var(--input-bg);
  }
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
  }

  .table-card,
  .publish-card {
    padding: var(--space-4);
  }

  :deep(.md-editor) {
    .md-editor-toolbar {
      flex-wrap: wrap;
    }
  }
}
</style>
