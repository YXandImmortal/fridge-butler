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
        <div class="fridge-list-container">
          <!-- 页面标题栏 -->
          <div class="page-header">
            <h2 class="page-title">我的冰箱</h2>
            <CustomButton type="primary" @click="handleCreate" class="create-btn">
              新建冰箱
            </CustomButton>
          </div>

          <!-- 搜索栏 -->
          <SearchBar
            v-model="searchForm.keyword"
            placeholder="请输入搜索内容"
            @search="handleSearch"
            @clear="handleSearch"
            class="search-bar"
          >
            <SortControl
              v-model:field="searchForm.sortField"
              v-model:order="searchForm.sortOrder"
              :field-options="sortFieldOptions"
              @change="handleSearch"
            />
          </SearchBar>

          <!-- 冰箱列表 -->
          <div v-loading="loading" class="fridge-list-wrapper">
            <!-- 空状态 -->
            <el-empty
              v-if="!loading && fridgeList.length === 0"
              description="暂无冰箱，快去创建一个吧"
            >
              <CustomButton type="primary" @click="handleCreate">立即创建</CustomButton>
            </el-empty>

            <!-- 卡片列表 -->
            <div v-else class="fridge-grid">
              <div
                v-for="fridge in fridgeList"
                :key="fridge.id"
                class="fridge-card"
                @click="handleViewDetail(fridge.id)"
              >
                <div class="card-header">
                  <div class="fridge-icon">
                    <i class="iconfont icon-fridge-line" />
                  </div>
                  <div class="fridge-basic-info">
                    <h3 class="fridge-name">{{ fridge.fridgeName }}</h3>
                    <p class="fridge-desc">{{ fridge.remark || '暂无描述' }}</p>
                  </div>
                </div>

                <div class="fridge-meta">
                  <span v-if="fridge.itemCount !== undefined" class="meta-item">
                    <i class="iconfont icon-item" />
                    {{ fridge.itemCount }} 件物品
                  </span>
                </div>
                <div class="fridge-meta">
                  <span class="meta-item">
                    <i class="iconfont icon-calendar" />
                    {{ formatDate(fridge.createTime) }}
                  </span>
                  <span v-if="fridge.totalCapacity === null" class="meta-item">
                    未设置容量
                  </span>
                  <span v-else class="meta-item">
                    容量 {{fridge.totalCapacity}} L
                  </span>
                </div>
              </div>
            </div>
          </div>
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
      :message="`确定要删除冰箱「${selectedFridge?.fridgeName || ''}」吗？删除后可在后台恢复。`"
      confirm-text="确定删除"
      cancel-text="取消"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import CopyrightFooter from '@/components/CopyrightFooter.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import showMessage from '@/utils/message'
import { useUserStore } from '@/stores/user'
import {listMyFridges, deleteFridge, searchFridges} from '@/api/fridge'
import CustomButton from "@/components/CustomButton.vue";
import SearchBar from "@/components/SearchBar.vue";
import SortControl from "@/components/SortControl.vue";

const router = useRouter()
const userStore = useUserStore()
const { logout } = userStore

// 加载状态
const loading = ref(false)

// 冰箱列表
const fridgeList = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  sortField: 'createTime',
  sortOrder: 'asc'
})

// 排序选项
const sortFieldOptions = [
  { label: '创建日期', value: 'createTime' },
  { label: '冰箱名称', value: 'name' },
  { label: '容量大小', value: 'totalCapacity' }
]

const sortOrderOptions = [
  { label: '升序', value: 'asc' },
  { label: '降序', value: 'desc' }
]

// 对话框控制
const showLogoutDialog = ref(false)
const showDeleteDialog = ref(false)
const selectedFridge = ref(null)

// 获取冰箱列表
const fetchFridgeList = async () => {
  loading.value = true
  try {
    const res = await listMyFridges()
    if (res.code === 200 && Array.isArray(res.data)) {
      fridgeList.value = res.data
    } else {
      fridgeList.value = []
    }
  } catch (error) {
    console.error('获取冰箱列表失败:', error)
    showMessage.error('获取冰箱列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = async () => {
  try {
    const params = {
      keyword: searchForm.keyword,
      sortField: searchForm.sortField,
      sortOrder: searchForm.sortOrder
    }
    const res = await searchFridges(params)
    if (res.code === 200 && Array.isArray(res.data)) {
      fridgeList.value = res.data
    } else {
      fridgeList.value = []
    }
  } catch (error) {
    console.error('查询冰箱列表失败:', error)
    showMessage.error('查询冰箱列表失败')
  }
}

// 查看详情
const handleViewDetail = (id) => {
  router.push({
    name: 'fridge-detail',
    params: {
      id: id
    }
  })
}

// 创建冰箱
const handleCreate = () => {
  router.push({
    name: 'fridge-create',
  })
}

// 删除确认
const handleDelete = (fridge) => {
  selectedFridge.value = fridge
  showDeleteDialog.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!selectedFridge.value) return
  try {
    const res = await deleteFridge(selectedFridge.value.id)
    if (res.code === 200) {
      showMessage.success('删除成功')
      await fetchFridgeList()
    } else {
      showMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除冰箱失败:', error)
    showMessage.error('删除失败')
  } finally {
    showDeleteDialog.value = false
    selectedFridge.value = null
  }
}

// 处理退出登录
const handleLogout = () => {
  logout()
  showLogoutDialog.value = false
  router.push('/login')
  showMessage.info('已退出登录')
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  fetchFridgeList()
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
  flex: 1;
}

.main-content {
  margin-left: var(--sidebar-width);
  transition: all 0.3s ease;
  min-height: calc(100vh - var(--header-height) - var(--footer-height));
  background: var(--main-content-bg);
  padding: var(--space-5);
}

.fridge-list-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.create-btn {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 200;
}

.create-btn :deep(.iconfont) {
  margin-right: 6px;
  font-size: 14px;
}

.search-bar {
  margin-bottom: 24px;
  max-width: 700px;
}

.fridge-list-wrapper {
  min-height: 400px;
  padding-bottom: var(--space-6);
}

.fridge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 20px;
}

.fridge-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--gray-40);
  cursor: pointer;
  transition: all 0.3s ease;
}

.fridge-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
}

.fridge-icon {
  min-width: 53px;
  min-height: 53px;
  background: var(--primary-light);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fridge-icon .iconfont {
  font-size: 24px;
  color: var(--primary-color);
}

.fridge-actions {
  display: flex;
  gap: 8px;
}

.fridge-basic-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fridge-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fridge-desc {
  font-size: 14px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-height: 1.5em;
}

.fridge-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: flex-start;
}

.meta-item {
  font-size: 13px;
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-item .iconfont {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: var(--sidebar-width-md);
    padding: var(--space-4);
  }

  .page-title {
    font-size: 22px;
  }

  .fridge-grid {
    grid-template-columns: 1fr;
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .main-content {
    margin-left: 0;
    padding: var(--space-3);
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .search-bar {
    max-width: 100%;
  }

  .search-bar :deep(.search-bar-wrapper) {
    flex-direction: column;
    align-items: stretch;
  }

  .search-bar :deep(.search-input-group) {
    width: 100%;
  }
}
</style>
