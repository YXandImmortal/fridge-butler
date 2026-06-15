<template>
  <div class="admin-user-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <i class="iconfont icon-users page-header-icon"/>
        <h1 class="page-title">用户管理</h1>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <SearchBar
        v-model="searchForm.keyword"
        placeholder="搜索用户名/手机号"
        @search="handleSearch"
        @clear="handleReset"
      >
        <SortControl
            v-model:field="sortField"
            v-model:order="sortOrder"
            :field-options="sortFieldOptions"
            @change="handleSortChange"
        />
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
      <el-empty v-if="!loading && userList.length === 0" description="暂无用户数据" />
      <template v-else>
        <el-table max-height="520px" :data="userList" style="width: 100%">
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="email" label="邮箱" min-width="160" />
          <el-table-column prop="roleName" label="角色" width="120" />
          <el-table-column prop="status" label="账号状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === true ? 'success' : 'danger'" size="small">
                {{ row.status === true ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isActivated" label="激活状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.isActivated === true ? 'success' : 'warning'" size="small">
                {{ row.isActivated === true ? '已激活' : '未激活' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" min-width="160" />
          <el-table-column prop="lastLoginTime" label="最后登录" min-width="160">
            <template #default="{ row }">
              {{ row.lastLoginTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <CustomButton type="link" color="primary" size="small" @click="handleViewDetail(row)">
                查看详情
              </CustomButton>
              <template v-if="row.id !== currentUserId">
                <CustomButton type="link" color="warning" size="small" @click="handleResetPassword(row)">
                  重置密码
                </CustomButton>
                <CustomButton
                  type="link"
                  :color="row.status === true ? 'danger' : 'success'"
                  size="small"
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === true ? '禁用' : '启用' }}
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

    <!-- 用户详情对话框 -->
    <UserDetailDialog
      v-model:visible="detailVisible"
      :user="selectedUser"
    />

    <!-- 重置密码确认 -->
    <ConfirmDialog
      v-model:visible="resetPwdConfirmVisible"
      title="重置密码"
      :message="`确定要重置用户「${selectedUser?.username || ''}」的密码吗？重置后将生成随机密码。`"
      confirm-text="确定重置"
      cancel-text="取消"
      @confirm="confirmResetPassword"
      width="400px"
    />

    <!-- 禁用/启用确认 -->
    <ConfirmDialog
      v-model:visible="toggleStatusVisible"
      :title="toggleActionText"
      :message="toggleConfirmMessage"
      confirm-text="确定"
      cancel-text="取消"
      @confirm="confirmToggleStatus"
      width="400px"
    />

    <!-- 重置密码结果 -->
    <ResetPasswordResultDialog
      v-model:visible="resetPwdResultVisible"
      :username="selectedUser?.username"
      :password="newPassword"
      @copy="handleCopyPassword"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useUserStore } from '@/stores/user.js'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import ResetPasswordResultDialog from '@/components/ui/ResetPasswordResultDialog.vue'
import SearchBar from '@/components/form/SearchBar.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import UserDetailDialog from '@/components/super-admin/UserDetailDialog.vue'
import SortControl from '@/components/form/SortControl.vue'
import { getUserList, updateUserStatus, resetUserPassword, getUserDetail } from '@/api/admin.js'
import showMessage from '@/utils/message.js'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userId)

// ==================== 搜索与分页 ====================
const loading = ref(false)
const searchForm = reactive({
  keyword: '',
  status: null
})

const statusOptions = [
  { label: '正常', value: true },
  { label: '禁用', value: false }
]

const sortField = ref('createTime')
const sortOrder = ref('desc')

const sortFieldOptions = [
  { label: '注册时间', value: 'createTime' },
  { label: '最后登录', value: 'lastLoginTime' }
]

// 状态下拉变化自动搜索
watch(() => searchForm.status, () => {
  handleSearch()
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const userList = ref([])

// ==================== 对话框控制 ====================
const detailVisible = ref(false)
const resetPwdConfirmVisible = ref(false)
const toggleStatusVisible = ref(false)
const resetPwdResultVisible = ref(false)

const selectedUser = ref(null)
const newPassword = ref('')

const toggleActionText = computed(() => {
  if (!selectedUser.value) return ''
  return selectedUser.value.status === true ? '禁用用户' : '启用用户'
})

const toggleConfirmMessage = computed(() => {
  if (!selectedUser.value) return ''
  const action = selectedUser.value.status === true ? '禁用' : '启用'
  return `确定要${action}用户「${selectedUser.value.username}」吗？`
})

// ==================== 数据获取 ====================
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.status !== null && searchForm.status !== undefined) {
      params.status = searchForm.status
    }
    if (sortField.value) {
      params.sortField = sortField.value
      params.sortOrder = sortOrder.value
    }

    const res = await getUserList(params)
    if (res.code === 200 && res.data) {
      const data = res.data
      if (Array.isArray(data.list)) {
        userList.value = data.list
        pagination.total = data.total || 0
      } else if (Array.isArray(data)) {
        userList.value = data
        pagination.total = data.length
      } else {
        userList.value = []
        pagination.total = 0
      }
    } else {
      userList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    showMessage.error('获取用户列表失败')
    userList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// ==================== 搜索与分页事件 ====================
const handleSearch = () => {
  pagination.page = 1
  fetchUserList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  sortField.value = 'createTime'
  sortOrder.value = 'desc'
  pagination.page = 1
  fetchUserList()
}

const handleSortChange = () => {
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchUserList()
}

const handlePageChange = (page) => {
  pagination.page = page
  fetchUserList()
}

// ==================== 查看详情 ====================
const handleViewDetail = async (row) => {
  selectedUser.value = row
  // 如果后端有详情接口，可以在这里调用获取更完整的信息
  try {
    const res = await getUserDetail(row.id)
    if (res.code === 200 && res.data) {
      selectedUser.value = { ...row, ...res.data }
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
  }
  detailVisible.value = true
}

// ==================== 重置密码 ====================
const handleResetPassword = (row) => {
  selectedUser.value = row
  resetPwdConfirmVisible.value = true
}

const confirmResetPassword = async () => {
  if (!selectedUser.value) return
  try {
    const res = await resetUserPassword(selectedUser.value.id)
    if (res.code === 200) {
      newPassword.value = res.data.newPassword
      resetPwdResultVisible.value = true
      showMessage.success('密码重置成功')
    } else {
      showMessage.error(res.message || '重置失败')
    }
  } catch (error) {
    console.error('重置密码失败:', error)
    showMessage.error('重置密码失败')
  } finally {
    resetPwdConfirmVisible.value = false
  }
}

const handleCopyPassword = async () => {
  if (!newPassword.value) return
  try {
    await navigator.clipboard.writeText(newPassword.value)
    showMessage.success('密码已复制到剪贴板')
  } catch {
    showMessage.error('复制失败，请手动复制')
  }
}

// ==================== 禁用/启用 ====================
const handleToggleStatus = (row) => {
  selectedUser.value = row
  toggleStatusVisible.value = true
}

const confirmToggleStatus = async () => {
  if (!selectedUser.value) return
  try {
    const newStatus = selectedUser.value.status !== true
    const res = await updateUserStatus(selectedUser.value.id, newStatus)
    if (res.code === 200) {
      showMessage.success(newStatus === true ? '用户已启用' : '用户已禁用')
      // 更新本地数据
      const target = userList.value.find(u => u.id === selectedUser.value.id)
      if (target) {
        target.status = newStatus
      }
    } else {
      showMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('更新用户状态失败:', error)
    showMessage.error('操作失败')
  } finally {
    toggleStatusVisible.value = false
  }
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped lang="scss">
.admin-user-container {
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

  .search-bar-wrapper {
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
