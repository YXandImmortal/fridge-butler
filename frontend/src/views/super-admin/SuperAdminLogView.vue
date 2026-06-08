<template>
  <div class="admin-log-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <i class="iconfont icon-script-text page-header-icon"/>
        <h1 class="page-title">操作日志</h1>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <SearchBar
        v-model="searchForm.keyword"
        placeholder="搜索用户名/URI"
        @search="handleSearch"
        @clear="handleReset"
      >
        <CustomSelect
          v-model="searchForm.method"
          placeholder="全部方法"
          clearable
          :options="methodOptions"
          @change="handleSearch"
        />
        <CustomSelect
          v-model="searchForm.statusCode"
          placeholder="全部状态码"
          clearable
          :options="statusCodeOptions"
          @change="handleSearch"
        />
        <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
            class="log-date-picker"
            style="--el-border-radius-base: var(--radius-md); height: 40px; width: 200px;"
        />
        <CustomButton @click="handleReset" type="search-reset">
          重置
        </CustomButton>
      </SearchBar>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper" v-loading="loading">
      <el-empty v-if="!loading && logList.length === 0" description="暂无日志数据" />
      <template v-else>
        <el-table max-height="520px" :data="logList" style="width: 100%" @row-click="handleRowClick">
          <el-table-column prop="username" label="用户" min-width="100" />
          <el-table-column prop="method" label="方法" width="90">
            <template #default="{ row }">
              <el-tag :type="methodTagType(row.method)" size="small" class="method-tag">
                {{ row.method }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="uri" label="请求路径" min-width="180" />
          <el-table-column prop="ip" label="IP" min-width="120" />
          <el-table-column prop="statusCode" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.statusCode)" size="small">
                {{ row.statusCode }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="durationMs" label="耗时" width="90">
            <template #default="{ row }">
              {{ row.durationMs }}ms
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="160" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <CustomButton type="link" color="primary" size="small" @click.stop="handleViewDetail(row)">
                查看详情
              </CustomButton>
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

    <!-- 日志详情对话框 -->
    <LogDetailDialog v-model:visible="detailVisible" :log="selectedLog" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import SearchBar from '@/components/form/SearchBar.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import LogDetailDialog from '@/components/super-admin/LogDetailDialog.vue'
import { getLogList } from '@/api/admin.js'
import showMessage from '@/utils/message.js'

// ==================== 搜索与分页 ====================
const loading = ref(false)
const searchForm = reactive({
  keyword: '',
  method: null,
  dateRange: [],
  statusCode: null
})

const methodOptions = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'PATCH', value: 'PATCH' }
]

const statusCodeOptions = [
  { label: '200', value: 200 },
  { label: '400', value: 400 },
  { label: '500', value: 500 }
]

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const logList = ref([])

// ==================== 详情对话框 ====================
const detailVisible = ref(false)
const selectedLog = ref(null)

// ==================== 标签类型 ====================
const methodTagType = (method) => {
  const map = {
    GET: 'primary',
    POST: 'success',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return map[method] || 'info'
}

const statusTagType = (code) => {
  if (code >= 200 && code < 300) return 'success'
  if (code >= 400 && code < 500) return 'warning'
  if (code >= 500) return 'danger'
  return 'info'
}

// ==================== 数据获取 ====================
const fetchLogList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.method) params.method = searchForm.method
    if (searchForm.statusCode !== null && searchForm.statusCode !== undefined) {
      params.statusCode = searchForm.statusCode
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const res = await getLogList(params)
    if (res.code === 200 && res.data) {
      const data = res.data
      if (Array.isArray(data.list)) {
        logList.value = data.list
        pagination.total = data.total || 0
      } else if (Array.isArray(data)) {
        logList.value = data
        pagination.total = data.length
      } else {
        logList.value = []
        pagination.total = 0
      }
    } else {
      logList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取日志列表失败:', error)
    showMessage.error('获取日志列表失败')
    logList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// ==================== 搜索与分页事件 ====================
const handleSearch = () => {
  pagination.page = 1
  fetchLogList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.method = null
  searchForm.dateRange = []
  searchForm.statusCode = null
  pagination.page = 1
  fetchLogList()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchLogList()
}

const handlePageChange = (page) => {
  pagination.page = page
  fetchLogList()
}

// ==================== 查看详情 ====================
const handleViewDetail = (row) => {
  selectedLog.value = row
  detailVisible.value = true
}

const handleRowClick = (row) => {
  selectedLog.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchLogList()
})
</script>

<style scoped lang="scss">
.admin-log-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.page-header {
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
}

.search-section {
  margin-bottom: var(--space-6);
}

/* 日期选择器在 SearchBar 插槽中的对齐 */
:deep(.log-date-picker) {
  .el-input__wrapper {
    height: 40px;
  }
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

.method-tag {
  font-weight: 600;
  min-width: 52px;
  text-align: center;
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
