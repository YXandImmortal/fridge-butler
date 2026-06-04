<template>
  <div class="admin-notification-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <i class="iconfont icon-megaphone page-header-icon"/>
        <h1 class="page-title">重要通知发布</h1>
      </div>
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
            通知内容编辑
          </h3>

          <el-form-item
            label="通知标题"
            :rules="[{ required: true, message: '请输入通知标题', trigger: 'blur' }]"
          >
            <EnhancedInput
              v-model="noticeForm.title"
              placeholder="请输入通知标题，建议简短有力"
              maxlength="100"
              show-word-limit
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
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import CustomButton from '@/components/ui/CustomButton.vue'
import EnhancedInput from '@/components/ui/EnhancedInput.vue'
import { useThemeStore } from '@/stores/theme.js'
import { broadcastImportantNotice } from '@/api/admin.js'
import showMessage from '@/utils/message.js'

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
    } else {
      showMessage.error(res.message || '发布失败')
    }
  } catch (error) {
    console.error('发布重要通知失败:', error)
    showMessage.error('发布失败，请稍后重试')
  } finally {
    broadcasting.value = false
  }
}
</script>

<style scoped lang="scss">
.admin-notification-container {
  max-width: 960px;
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
