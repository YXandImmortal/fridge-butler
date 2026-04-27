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
            <CustomButton type="link" @click="handleBack">
              <i class="iconfont icon-arrow-left" />
              返回列表
            </CustomButton>
            <CustomButton type="link" @click="openSelectFridgeDialog">
              <i class="iconfont icon-switch" />
              切换冰箱
            </CustomButton>
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-wrapper">
            <el-skeleton :rows="6" animated />
          </div>

          <!-- 详情内容 -->
          <div v-else-if="fridgeForm" class="detail-card">
            <div class="detail-header">
              <div class="detail-icon">
                <i class="iconfont icon-fridge-line" />
              </div>
              <div class="detail-info">
                <h2 class="detail-name">{{ fridgeForm.fridgeName }}</h2>
                <p class="detail-desc">{{ fridgeForm.remark || '暂无描述' }}</p>
              </div>
              <div class="item-management-wrapper">
                <CustomButton class="item-management">
                  <div class="item-management-inner">
                    <i class="iconfont icon-inbox-full" />
                    <span>物品管理</span>
                  </div>
                </CustomButton>
              </div>
              <div class="detail-actions">
                <CustomButton type="primary" :loading="saving" @click="handleSave">
                  保存信息
                </CustomButton>
                <CustomButton type="danger" @click="handleDelete">
                  删除冰箱
                </CustomButton>
              </div>
            </div>

            <el-divider/>

            <el-form :model="fridgeForm" label-width="100px" class="detail-form">

              <!-- 可编辑字段 -->
              <el-form-item label="冰箱名称" required>
                <EnhancedInput v-model="fridgeForm.fridgeName" placeholder="请输入冰箱名称" maxlength="30" show-word-limit />
              </el-form-item>

              <el-form-item label="默认冰箱" required>
                <el-switch v-model="fridgeForm.isDefault" active-text="是" inactive-text="否" />
              </el-form-item>

              <el-form-item label="冰箱地址">
                <EnhancedInput v-model="fridgeForm.fridgeAddress" placeholder="请输入冰箱地址" maxlength="200" show-word-limit />
              </el-form-item>

              <el-form-item label="总容量">
                <el-input-number v-model="fridgeForm.totalCapacity" :min="0" :precision="0" placeholder="请输入总容量（L）" style="width: 100%;" />
              </el-form-item>

              <el-form-item label="状态">
                <el-switch v-model="fridgeForm.status" :active-value="true" :inactive-value="false" active-text="启用" inactive-text="停用" />
              </el-form-item>

              <el-form-item label="备注">
                <EnhancedInput v-model="fridgeForm.remark" type="textarea" :rows="3" placeholder="请输入备注" maxlength="200" show-word-limit />
              </el-form-item>

              <!-- 不可编辑字段 -->
              <el-form-item label="物品数量">
                <EnhancedInput :model-value="fridgeForm.itemCount + ' 件'" disabled />
              </el-form-item>
              <el-form-item label="创建时间">
                <EnhancedInput :model-value="formatDateTime(fridgeForm.createTime)" disabled />
              </el-form-item>
              <el-form-item label="更新时间">
                <EnhancedInput :model-value="formatDateTime(fridgeForm.updateTime)" disabled />
              </el-form-item>
            </el-form>
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
      :message="`确定要删除冰箱「${fridgeForm?.fridgeName || ''}」吗？删除后可在后台恢复。`"
      confirm-text="确定删除"
      cancel-text="取消"
      @confirm="confirmDelete"
    />

    <!-- 选择冰箱对话框 -->
    <ConfirmDialog
      v-model:visible="showSelectFridgeDialog"
      v-model:select-value="selectedFridgeId"
      title="选择冰箱"
      message="请选择一个冰箱："
      confirm-text="确定"
      cancel-text="取消"
      type="select"
      :persistent="true"
      :show-close="false"
      width="420px"
      :options="fridgeList"
      option-label="fridgeName"
      option-value="id"
      select-placeholder="请选择冰箱"
      :select-loading="fridgeListLoading"
      @confirm="handleSelectFridgeConfirm"
      @cancel="handleSelectFridgeCancel"
    />
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import CopyrightFooter from '@/components/CopyrightFooter.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import showMessage from '@/utils/message'
import { useUserStore } from '@/stores/user'
import { getFridgeDetail, deleteFridge, listMyFridges, updateFridge } from '@/api/fridge'
import CustomButton from "@/components/CustomButton.vue";
import EnhancedInput from "@/components/EnhancedInput.vue";

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { logout } = userStore

// 冰箱数据
const fridge = ref(null)
const fridgeForm = ref(null)
const loading = ref(false)
const saving = ref(false)

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
      fridgeList.value = res.data || []
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
    console.log(selectedFridgeId.value)
    router.push({
      name: 'fridge-detail',
      params: { id: selectedFridgeId.value },
    })
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
      fridgeForm.value = { ...res.data }
    } else {
      showMessage.error(res.message || '获取冰箱详情失败')
      fridge.value = null
      fridgeForm.value = null
    }
  } catch (error) {
    console.error('获取冰箱详情失败:', error)
    showMessage.error('获取冰箱详情失败')
    fridge.value = null
    fridgeForm.value = null
  } finally {
    loading.value = false
  }
}

// 保存冰箱信息
const handleSave = async () => {
  if (!fridgeForm.value.fridgeName || fridgeForm.value.fridgeName.trim() === '') {
    showMessage.warning('冰箱名称不能为空')
    return
  }
  if (fridgeForm.value.isDefault === null || fridgeForm.value.isDefault === undefined) {
    showMessage.warning('请设置是否为默认冰箱')
    return
  }

  saving.value = true
  try {
    const res = await updateFridge({
      id: fridgeForm.value.id,
      fridgeName: fridgeForm.value.fridgeName,
      isDefault: fridgeForm.value.isDefault,
      fridgeAddress: fridgeForm.value.fridgeAddress,
      remark: fridgeForm.value.remark,
      totalCapacity: fridgeForm.value.totalCapacity,
      status: fridgeForm.value.status,
    })
    if (res.code === 200) {
      showMessage.success('保存成功')
      await fetchFridgeDetail()
    } else {
      showMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存冰箱信息失败:', error)
    showMessage.error('保存失败')
  } finally {
    saving.value = false
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

// 监听路由参数变化，切换冰箱时重新获取详情
watch(
  () => route.params.id,
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      fetchFridgeDetail()
    }
  }
)
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
  max-height: calc(100vh - var(--header-height) - var(--footer-height));
  min-height: calc(100vh - var(--header-height) - var(--footer-height));
  background: var(--main-content-bg);
  padding: var(--space-5);
  overflow-y: scroll;
}

.fridge-detail-container {
  max-width: 600px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

/* 验证错误时的 focus 样式 */
.el-form-item.is-error :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-danger) inset;
  border-color: var(--el-color-danger);
}

.back-bar {
  display: flex;
  margin-bottom: 20px;
}

.back-bar .custom-button {
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
  box-shadow: var(--shadow-sm);
}

.detail-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 32px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
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
  font-weight: 100;
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

.el-divider {
  margin: 16px 0;
}

.detail-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-form {
  align-self: center;
  max-width: 400px;
}

.detail-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}

.item-management-wrapper {
  align-self: stretch;
  display: flex;
  align-items: center;
}

.item-management {
  border-radius: 12px;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  height: 100%;
}

.item-management:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--gray-40);
}

.item-management :deep(.custom-button__content) {
  padding: 0;
  height: 100%;
}

.item-management-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 100%;

}

.item-management-inner .iconfont {
  font-size: 34px;
  font-weight: 200;
}

.item-management-inner span {
  font-size: 16px;
}

.detail-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.detail-actions .custom-button {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 200;
  margin: 0;
}

.detail-actions .custom-button--primary {
  background: var(--primary-color);
  border: none;
}

.detail-actions .custom-button--primary:hover:not(:disabled) {
  box-shadow: 0 6px 20px var(--primary-40);
}

.detail-actions .custom-button--danger {
  background: var(--danger-color);
  border: none;
}

.detail-actions .custom-button--danger:hover:not(:disabled) {
  box-shadow: 0 6px 20px var(--danger-40);
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

  .detail-form :deep(.el-form-item) {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-form :deep(.el-form-item__label) {
    width: 100% !important;
    text-align: left;
    margin-bottom: 4px;
  }

  .detail-form :deep(.el-form-item__content) {
    width: 100%;
    margin-left: 0 !important;
  }

  .detail-actions {
    flex-direction: column;
    align-items: stretch;
    margin-left: 0;
    width: 100%;
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
