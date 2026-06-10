<template>
  <div class="admin-activation-key-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <i class="iconfont icon-key page-header-icon"/>
        <h1 class="page-title">密钥管理</h1>
      </div>
    </div>

    <!-- 生成操作区 -->
    <div class="operation-section">
      <div class="operation-form">
        <div class="generate-form">
          <div class="form-left">
            <div class="form-item">
              <span class="form-label">生成数量</span>
              <CustomInputNumber v-model="generateForm.count" :min="1" :max="100" size="large" :step="1" />
            </div>
            <div class="form-item form-item-remark">
              <span class="form-label">备注</span>
              <CustomInput v-model="generateForm.remark" placeholder="可选：备注信息" class="remark-input" />
            </div>
          </div>
          <CustomButton type="primary" :loading="generating" @click="handleGenerate" style="height: 42px">
            批量生成
          </CustomButton>
        </div>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="search-section">
      <SearchBar
        v-model="searchForm.keyword"
        placeholder="搜索密钥码/备注"
        @search="handleSearch"
        @clear="handleReset"
      >
        <CustomSelect
          v-model="searchForm.status"
          placeholder="全部状态"
          clearable
          :options="statusOptions"
          size="large"
          variant="search"
        />
        <CustomButton @click="handleReset" type="search-reset">
          重置
        </CustomButton>
      </SearchBar>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper" v-loading="loading">
      <el-empty v-if="!loading && keyList.length === 0" description="暂无密钥数据" />
      <template v-else>
        <el-table max-height="520px" :data="keyList" style="width: 100%">
          <el-table-column prop="keyCode" label="密钥码" min-width="180">
            <template #default="{ row }">
              <span class="keycode-text" @click="copyKeyCode(row.keyCode)">
                {{ row.keyCode }}
                <i class="iconfont icon-copy keycode-copy-icon" />
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">
                {{ statusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="boundUsername" label="绑定用户" min-width="120">
            <template #default="{ row }">
              {{ row.boundUsername || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="boundTime" label="绑定时间" min-width="160">
            <template #default="{ row }">
              {{ row.boundTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140">
            <template #default="{ row }">
              {{ row.remark || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <!-- UNUSED: 发放、收回、销毁 -->
              <template v-if="row.status === 'UNUSED'">
                <CustomButton
                  type="link"
                  color="primary"
                  size="small"
                  @click="handleIssue(row)"
                >
                  发放
                </CustomButton>
                <CustomButton
                  type="link"
                  color="warning"
                  size="small"
                  @click="handleRevoke(row)"
                >
                  收回
                </CustomButton>
                <CustomButton
                  type="link"
                  color="danger"
                  size="small"
                  @click="handleDestroy(row)"
                >
                  销毁
                </CustomButton>
              </template>
              <!-- ISSUED: 收回、销毁 -->
              <template v-else-if="row.status === 'ISSUED'">
                <CustomButton
                  type="link"
                  color="warning"
                  size="small"
                  @click="handleRevoke(row)"
                >
                  收回
                </CustomButton>
                <CustomButton
                  type="link"
                  color="danger"
                  size="small"
                  @click="handleDestroy(row)"
                >
                  销毁
                </CustomButton>
              </template>
              <!-- BOUND: 收回 -->
              <template v-else-if="row.status === 'BOUND'">
                <CustomButton
                  type="link"
                  color="warning"
                  size="small"
                  @click="handleRevoke(row)"
                >
                  收回
                </CustomButton>
              </template>
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

    <!-- 发放确认 -->
    <ConfirmDialog
      v-model:visible="issueConfirmVisible"
      title="发放密钥"
      :message="`确定要发放密钥「${selectedKey?.keyCode || ''}」吗？发放后该密钥可被用户绑定。`"
      confirm-text="确定发放"
      cancel-text="取消"
      @confirm="confirmIssue"
      width="400px"
    />

    <!-- 收回确认 -->
    <ConfirmDialog
      v-model:visible="revokeConfirmVisible"
      title="收回密钥"
      :message="`确定要收回密钥「${selectedKey?.keyCode || ''}」吗？收回后关联用户将取消激活。`"
      confirm-text="确定收回"
      cancel-text="取消"
      @confirm="confirmRevoke"
      width="400px"
    />

    <!-- 销毁确认 -->
    <ConfirmDialog
      v-model:visible="destroyConfirmVisible"
      title="销毁密钥"
      :message="`确定要销毁密钥「${selectedKey?.keyCode || ''}」吗？销毁后无法恢复。`"
      confirm-text="确定销毁"
      cancel-text="取消"
      @confirm="confirmDestroy"
      width="400px"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import SearchBar from '@/components/form/SearchBar.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import CustomInput from '@/components/ui/CustomInput.vue'
import CustomInputNumber from '@/components/ui/CustomInputNumber.vue'
import {
  getActivationKeyList,
  generateActivationKeys,
  issueActivationKey,
  revokeActivationKey,
  destroyActivationKey
} from '@/api/admin.js'
import showMessage from '@/utils/message.js'

// ==================== 状态 ====================
const loading = ref(false)
const generating = ref(false)
const issueConfirmVisible = ref(false)
const revokeConfirmVisible = ref(false)
const destroyConfirmVisible = ref(false)
const selectedKey = ref(null)

const generateForm = reactive({
  count: 1,
  remark: ''
})

const searchForm = reactive({
  keyword: '',
  status: null
})

const statusOptions = [
  { label: '未使用', value: 'UNUSED' },
  { label: '已发放', value: 'ISSUED' },
  { label: '已绑定', value: 'BOUND' },
  { label: '已收回', value: 'REVOKED' },
  { label: '已销毁', value: 'DESTROYED' }
]

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const keyList = ref([])

// ==================== 辅助函数 ====================
const statusTagType = (status) => {
  const map = {
    UNUSED: 'info',
    ISSUED: 'primary',
    BOUND: 'success',
    REVOKED: 'warning',
    DESTROYED: 'danger'
  }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = {
    UNUSED: '未使用',
    ISSUED: '已发放',
    BOUND: '已绑定',
    REVOKED: '已收回',
    DESTROYED: '已销毁'
  }
  return map[status] || status
}

// ==================== 数据获取 ====================
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.status) params.status = searchForm.status

    const res = await getActivationKeyList(params)
    if (res.code === 200 && res.data) {
      const data = res.data
      if (Array.isArray(data.list)) {
        keyList.value = data.list
        pagination.total = data.total || 0
      } else if (Array.isArray(data)) {
        keyList.value = data
        pagination.total = data.length
      } else {
        keyList.value = []
        pagination.total = 0
      }
    } else {
      keyList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取密钥列表失败:', error)
    showMessage.error('获取密钥列表失败')
    keyList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// ==================== 搜索与分页事件 ====================
const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.page = 1
  fetchList()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchList()
}

const handlePageChange = (page) => {
  pagination.page = page
  fetchList()
}

// 状态下拉变化自动搜索
watch(() => searchForm.status, () => {
  handleSearch()
})

// ==================== 生成密钥 ====================
const handleGenerate = async () => {
  if (generating.value) return
  if (!generateForm.count || generateForm.count < 1) {
    showMessage.warning('请输入生成数量')
    return
  }
  generating.value = true
  try {
    const res = await generateActivationKeys({
      count: generateForm.count,
      remark: generateForm.remark || undefined
    })
    if (res.code === 200) {
      showMessage.success(`成功生成 ${generateForm.count} 个密钥`)
      generateForm.remark = ''
      fetchList()
    } else {
      showMessage.error(res.message || '生成失败')
    }
  } catch (error) {
    console.error('生成密钥失败:', error)
    showMessage.error('生成密钥失败')
  } finally {
    generating.value = false
  }
}

// ==================== 发放密钥 ====================
const handleIssue = (row) => {
  selectedKey.value = row
  issueConfirmVisible.value = true
}

const confirmIssue = async () => {
  if (!selectedKey.value) return
  try {
    const res = await issueActivationKey(selectedKey.value.id)
    if (res.code === 200) {
      showMessage.success('密钥已发放')
      fetchList()
    } else {
      showMessage.error(res.message || '发放失败')
    }
  } catch (error) {
    console.error('发放密钥失败:', error)
    showMessage.error('发放密钥失败')
  } finally {
    issueConfirmVisible.value = false
  }
}

// ==================== 收回密钥 ====================
const handleRevoke = (row) => {
  selectedKey.value = row
  revokeConfirmVisible.value = true
}

const confirmRevoke = async () => {
  if (!selectedKey.value) return
  try {
    const res = await revokeActivationKey(selectedKey.value.id)
    if (res.code === 200) {
      showMessage.success('密钥已收回，关联用户已取消激活')
      fetchList()
    } else {
      showMessage.error(res.message || '收回失败')
    }
  } catch (error) {
    console.error('收回密钥失败:', error)
    showMessage.error('收回密钥失败')
  } finally {
    revokeConfirmVisible.value = false
  }
}

// ==================== 复制密钥码 ====================
const copyKeyCode = async (keyCode) => {
  if (!keyCode) return
  try {
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(keyCode)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = keyCode
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    showMessage.success('密钥码已复制')
  } catch {
    showMessage.error('复制失败，请手动复制')
  }
}

// ==================== 销毁密钥 ====================
const handleDestroy = (row) => {
  selectedKey.value = row
  destroyConfirmVisible.value = true
}

const confirmDestroy = async () => {
  if (!selectedKey.value) return
  try {
    const res = await destroyActivationKey(selectedKey.value.id)
    if (res.code === 200) {
      showMessage.success('密钥已销毁')
      fetchList()
    } else {
      showMessage.error(res.message || '销毁失败')
    }
  } catch (error) {
    console.error('销毁密钥失败:', error)
    showMessage.error('销毁密钥失败')
  } finally {
    destroyConfirmVisible.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.admin-activation-key-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.page-header {
  margin-bottom: var(--space-6);
}

.page-header-icon {
  font-size: 28px;
}

.page-header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.operation-section {
  margin-bottom: var(--space-6);
}

.operation-form {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-5) var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.generate-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-4);
  justify-content: space-between;
}

.form-left {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-4);
  flex: 1;
  min-width: 0;
}

.form-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.form-item-remark {
  flex: 1;
  min-width: 200px;
}

.remark-input {
  width: 100%;
}

.form-label {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
  white-space: nowrap;
}

.keycode-text {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--text-primary);
  transition: color 0.2s ease;
}

.keycode-text:hover {
  color: var(--primary-color);
}

.keycode-copy-icon {
  font-size: 13px;
  opacity: 0.5;
  transition: opacity 0.2s ease;
}

.keycode-text:hover .keycode-copy-icon {
  opacity: 1;
}

.search-section {
  margin-bottom: var(--space-6);
}

.table-wrapper {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  min-height: 400px;
}

.pagination-wrapper {
  margin-top: var(--space-6);
  display: flex;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
  }

  .operation-form {
    padding: var(--space-4);
  }

  .table-wrapper {
    padding: var(--space-4);
  }

  .pagination-wrapper {
    justify-content: center;
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
</style>
