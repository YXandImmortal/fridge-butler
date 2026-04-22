<template>
  <div class="index-container">
    <!-- 头部组件 -->
    <Header @show-logout-dialog="showLogoutDialog = true" />

    <!-- 主体内容区域 -->
    <div class="main-content-wrapper">
      <!-- 左侧导航栏 -->
      <Sidebar />

      <!-- 主内容区域 -->
      <main class="main-content">
        <div class="fridge-detail-container">
          <!-- 返回按钮 -->
          <div class="back-bar">
            <el-button link @click="handleBack">
              <i class="iconfont icon-arrow-left" />
              返回列表
            </el-button>
            <el-button link type="primary" @click="openSelectFridgeDialog">
              <i class="iconfont icon-switch" />
              切换冰箱
            </el-button>
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-wrapper">
            <el-skeleton :rows="6" animated />
          </div>

          <!-- 详情内容 -->
          <div v-else-if="fridge" class="detail-card">
            <div class="detail-header">
              <div class="detail-icon">
                <i class="iconfont icon-refrigerator" />
              </div>
              <div class="detail-info">
                <h2 class="detail-name">{{ fridge.name }}</h2>
                <p class="detail-desc">{{ fridge.description || '暂无描述' }}</p>
              </div>
            </div>

            <el-divider />

            <div class="detail-body">
              <div class="info-row">
                <span class="info-label">冰箱ID</span>
                <span class="info-value">{{ fridge.id }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">创建时间</span>
                <span class="info-value">{{ formatDateTime(fridge.createTime) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">更新时间</span>
                <span class="info-value">{{ formatDateTime(fridge.updateTime) }}</span>
              </div>
              <div v-if="fridge.itemCount !== undefined" class="info-row">
                <span class="info-label">物品数量</span>
                <span class="info-value">{{ fridge.itemCount }} 件</span>
              </div>
            </div>

            <el-divider />

            <div class="detail-actions">
              <el-button type="danger" @click="handleDelete">
                <i class="iconfont icon-delete" />
                删除冰箱
              </el-button>
            </div>
          </div>

          <!-- 未找到 -->
          <el-empty v-else description="冰箱不存在或已被删除" />
        </div>
      </main>
    </div>

    <!-- 底部版权信息 -->
    <CopyrightFooter />

    <!-- 登出确认对话框 -->
    <ConfirmDialog
      v-model:visible="showLogoutDialog"
      title="退出登录"
      message="您确定要退出登录吗？"
      confirm-text="确定"
      cancel-text="取消"
      @confirm="handleLogout"
    />

    <!-- 删除确认对话框 -->
    <ConfirmDialog
      v-model:visible="showDeleteDialog"
      title="删除冰箱"
      :message="`确定要删除冰箱「${fridge?.name || ''}」吗？删除后可在后台恢复。`"
      confirm-text="确定删除"
      cancel-text="取消"
      @confirm="confirmDelete"
    />

    <!-- 选择冰箱对话框 -->
    <ConfirmDialog
      v-model:visible="showSelectFridgeDialog"
      title="选择冰箱"
      message="请选择一个冰箱查看详情："
      confirm-text="确定"
      cancel-text="取消"
      :persistent="true"
      :show-close="false"
      width="420px"
      @confirm="handleSelectFridgeConfirm"
      @cancel="handleSelectFridgeCancel"
    >
      <div class="select-fridge-content">
        <el-select
          v-model="selectedFridgeId"
          placeholder="请选择冰箱"
          clearable
          :loading="fridgeListLoading"
          style="width: 100%"
        >
          <el-option
            v-for="item in fridgeList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </div>
    </ConfirmDialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import CopyrightFooter from '@/components/CopyrightFooter.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import showMessage from '@/utils/message'
import { useUserStore } from '@/stores/user'
import { getFridgeDetail, deleteFridge, listMyFridges } from '@/api/fridge'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { logout } = userStore

// 冰箱数据
const fridge = ref(null)
const loading = ref(false)

// 对话框
const showLogoutDialog = ref(false)
const showDeleteDialog = ref(false)
const showSelectFridgeDialog = ref(false)

// 冰箱选择列表
const fridgeList = ref([])
const selectedFridgeId = ref(null)
const fridgeListLoading = ref(false)

// 打开选择冰箱对话框
const openSelectFridgeDialog = async () => {
  fridgeListLoading.value = true
  try {
    const res = await listMyFridges()
    if (res.code === 200 && res.data) {
      fridgeList.value = res.data.list || res.data || []
    } else {
      fridgeList.value = []
    }
  } catch (error) {
    console.error('获取冰箱列表失败:', error)
    fridgeList.value = []
  } finally {
    fridgeListLoading.value = false
  }
  selectedFridgeId.value = null
  showSelectFridgeDialog.value = true
}

// 确认选择冰箱
const handleSelectFridgeConfirm = () => {
  if (selectedFridgeId.value) {
    showSelectFridgeDialog.value = false
    router.push(`/fridge/${selectedFridgeId.value}`)
  } else {
    showMessage.warning('请选择一个冰箱')
  }
}

// 取消选择冰箱
const handleSelectFridgeCancel = () => {
  showSelectFridgeDialog.value = false
  showMessage.error('冰箱ID不能为空')
}

// 获取冰箱详情
const fetchFridgeDetail = async () => {
  const id = route.params.id
  if (!id) {
    await openSelectFridgeDialog()
    return
  }

  loading.value = true
  try {
    const res = await getFridgeDetail(id)
    if (res.code === 200 && res.data) {
      fridge.value = res.data
    } else {
      showMessage.error(res.message || '获取冰箱详情失败')
      fridge.value = null
    }
  } catch (error) {
    console.error('获取冰箱详情失败:', error)
    showMessage.error('获取冰箱详情失败')
    fridge.value = null
  } finally {
    loading.value = false
  }
}

// 返回列表
const handleBack = () => {
  router.push('/fridge/list')
}

// 删除确认
const handleDelete = () => {
  showDeleteDialog.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!fridge.value) return
  try {
    const res = await deleteFridge(fridge.value.id)
    if (res.code === 200) {
      showMessage.success('删除成功')
      router.push('/fridge/list')
    } else {
      showMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除冰箱失败:', error)
    showMessage.error('删除失败')
  } finally {
    showDeleteDialog.value = false
  }
}

// 处理退出登录
const handleLogout = () => {
  logout()
  showLogoutDialog.value = false
  router.push('/login')
  showMessage.info('已退出登录')
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchFridgeDetail()
})
</script>

<style scoped>
.index-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content-wrapper {
  margin-top: var(--header-height);
}

.main-content {
  margin-left: var(--sidebar-width);
  transition: all 0.3s ease;
  min-height: calc(100vh - var(--header-height) - var(--footer-height));
  background: var(--main-content-bg);
  padding: var(--space-5);
}

.fridge-detail-container {
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.back-bar {
  margin-bottom: 20px;
}

.back-bar :deep(.el-button) {
  font-size: 14px;
  color: var(--text-secondary);
}

.back-bar :deep(.iconfont) {
  margin-right: 4px;
  font-size: 12px;
}

.loading-wrapper {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.detail-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.detail-icon {
  width: 64px;
  height: 64px;
  background: var(--primary-light);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.detail-icon .iconfont {
  font-size: 32px;
  color: var(--primary-color);
}

.detail-info {
  flex: 1;
  min-width: 0;
}

.detail-name {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-body {
  padding: 8px 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: var(--text-primary);
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.detail-actions :deep(.el-button) {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 600;
}

.detail-actions :deep(.el-button--danger) {
  background: var(--danger-color);
  border: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: var(--sidebar-width-md);
    padding: var(--space-4);
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .detail-name {
    font-size: 20px;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}

.select-fridge-content {
  padding: 8px 0;
}

.select-fridge-tip {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .main-content {
    margin-left: 0;
    padding: var(--space-3);
  }

  .detail-card {
    padding: 20px;
  }
}
</style>
