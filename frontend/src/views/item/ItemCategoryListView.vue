<template>
  <div class="category-list-container">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <h2 class="page-title">物品分类一览</h2>
      <CustomButton type="primary" @click="handleCreate" class="create-btn">
        新建分类
      </CustomButton>
    </div>

    <!-- 分类列表 -->
    <div v-loading="loading" class="category-list-wrapper">
      <!-- 自定义分类 -->
      <div class="category-section">
        <h3 class="section-title">我的分类</h3>
        <el-empty
          v-if="!loading && customCategories.length === 0"
          description="您还没有创建自定义分类"
        >
          <CustomButton type="primary" @click="handleCreate">立即创建</CustomButton>
        </el-empty>
        <div v-else class="category-grid">
          <div
            v-for="category in customCategories"
            :key="category.id"
            class="category-card"
          >
            <div class="card-header">
              <div class="category-icon">
                <i class="iconfont icon-label" />
              </div>
              <div class="category-info">
                <h3 class="category-name">{{ category.categoryName }}</h3>
                <p class="category-type">自定义分类</p>
              </div>
            </div>

            <div class="card-actions">
              <CustomButton type="primary" size="small" @click.stop="handleEdit(category)">
                <i class="iconfont icon-edit" />
                编辑
              </CustomButton>
              <CustomButton type="danger" size="small" @click.stop="handleDelete(category)">
                <i class="iconfont icon-delete" />
                删除
              </CustomButton>
            </div>
          </div>
        </div>
      </div>

      <!-- 系统默认分类 -->
      <div class="category-section category-section--system" v-if="systemCategories.length > 0">
        <div class="section-header" @click="isSystemCollapsed = !isSystemCollapsed">
          <h3 class="section-title">系统默认分类</h3>
          <i class="iconfont icon-chevron-down toggle-icon" :class="{ 'is-collapsed': isSystemCollapsed }" />
        </div>
        <el-collapse-transition>
          <div v-show="!isSystemCollapsed" class="category-grid">
            <div
                v-for="category in systemCategories"
                :key="category.id"
                class="category-card category-card--system"
            >
              <div class="system-badge">
                <i class="iconfont icon-shield-fill" />
                系统默认
              </div>
              <div class="card-header">
                <div class="category-icon">
                  <i class="iconfont icon-label" />
                </div>
                <div class="category-info">
                  <h3 class="category-name">{{ category.categoryName }}</h3>
                  <p class="category-type">系统预设，不可编辑</p>
                </div>
              </div>
            </div>
          </div>
        </el-collapse-transition>
      </div>
    </div>

    <!-- 删除确认对话框 -->
    <ConfirmDialog
      v-model:visible="showDeleteDialog"
      title="删除分类"
      :message="`确定要删除分类「${selectedCategory?.categoryName || ''}」吗？删除后无法恢复，且会影响该分类下存在的物品。`"
      confirm-text="确定删除"
      cancel-text="取消"
      @confirm="confirmDelete"
      width="450px"
    />

    <!-- 编辑分类对话框 -->
    <InputDialog
      v-model:visible="showEditDialog"
      title="编辑分类名称"
      label="分类名称"
      placeholder="请输入分类名称"
      icon="icon-label"
      value-prop="categoryName"
      confirm-text="确认修改"
      :data="selectedCategory"
      :loading="editLoading"
      @submit="handleEditSubmit"
    />
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import InputDialog from '@/components/InputDialog.vue'
import showMessage from '@/utils/message'
import { listItemCategories, deleteItemCategory, updateItemCategory } from '@/api/item'
import CustomButton from '@/components/CustomButton.vue'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 分类列表
const categoryList = ref([])

// 系统默认分类折叠状态（默认折叠）
const isSystemCollapsed = ref(true)

// 自定义分类
const customCategories = computed(() =>
  categoryList.value
    .filter(c => !c.isSystemDefault)
    .sort((a, b) => a.id - b.id)
)

// 系统默认分类
const systemCategories = computed(() =>
  categoryList.value
    .filter(c => c.isSystemDefault)
    .sort((a, b) => a.id - b.id)
)

// 对话框控制
const showDeleteDialog = ref(false)
const showEditDialog = ref(false)
const selectedCategory = ref(null)
const editLoading = ref(false)

// 获取分类列表
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const res = await listItemCategories()
    if (res.code === 200 && Array.isArray(res.data)) {
      categoryList.value = res.data
    } else {
      categoryList.value = []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    showMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 创建分类
const handleCreate = () => {
  router.push({
    name: 'item-category-create'
  })
}

// 编辑分类
const handleEdit = (category) => {
  if (category.isSystemDefault) {
    showMessage.warning('系统默认分类不可编辑')
    return
  }
  selectedCategory.value = category
  showEditDialog.value = true
}

// 编辑提交
const handleEditSubmit = async ({ id, value }) => {
  editLoading.value = true
  try {
    const res = await updateItemCategory({
      id,
      categoryName: value
    })
    if (res.code === 200) {
      showMessage.success('修改成功')
      showEditDialog.value = false
      await fetchCategoryList()
    } else {
      showMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    console.error('修改分类失败:', error)
    showMessage.error('修改失败')
  } finally {
    editLoading.value = false
  }
}

// 删除确认
const handleDelete = (category) => {
  if (category.isSystemDefault) {
    showMessage.warning('系统默认分类不可删除')
    return
  }
  selectedCategory.value = category
  showDeleteDialog.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!selectedCategory.value) return
  try {
    const res = await deleteItemCategory(selectedCategory.value.id)
    if (res.code === 200) {
      showMessage.success('删除成功')
      await fetchCategoryList()
    } else {
      showMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除分类失败:', error)
    showMessage.error('删除失败')
  } finally {
    showDeleteDialog.value = false
    selectedCategory.value = null
  }
}

onMounted(() => {
  fetchCategoryList()
})
</script>

<style scoped lang="scss">
.category-list-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.create-btn {
  border-radius: var(--radius-md);
  padding: 10px var(--space-5);
  font-weight: 200;
}

.category-list-wrapper {
  min-height: 400px;
  padding-bottom: var(--space-6);
}

.category-section + .category-section {
  margin-top: var(--space-8);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-4);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  padding: var(--space-3) var(--space-4);
  margin: 0 calc(-1 * var(--space-4)) var(--space-4);
  border-radius: var(--radius-md);
  transition: background-color 0.2s ease;
}

.section-header:hover {
  background-color: var(--gray-30);
}

.section-header .section-title {
  margin: 0;
}

.toggle-icon {
  font-size: 18px;
  color: var(--text-secondary);
  transition: transform 0.3s ease;
}

.toggle-icon.is-collapsed {
  transform: rotate(0deg);
}

.toggle-icon:not(.is-collapsed) {
  transform: rotate(180deg);
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-5);
}

.category-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--gray-40);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 140px;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

.category-card--system {
  border: 2px solid var(--badge-silver);
  background: linear-gradient(135deg, var(--badge-silver-bg) 0%, var(--glass-bg) 60%);
  box-shadow: 0 4px 20px var(--badge-silver-shadow);
}

.category-card--system:hover {
  box-shadow: 0 8px 28px var(--badge-silver-shadow-hover);
  border-color: var(--badge-silver-hover);
}

.system-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: linear-gradient(135deg, var(--badge-silver) 0%, var(--badge-silver-hover) 100%);
  color: var(--badge-silver-text-dark);
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-bottom-left-radius: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 8px var(--badge-silver-shadow);
  z-index: 1;
}

.system-badge .iconfont {
  font-size: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.category-icon {
  min-width: 48px;
  min-height: 48px;
  background: var(--primary-light);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-icon .iconfont {
  font-size: 22px;
  color: var(--primary-color);
}

.category-card--system .category-icon {
  background: linear-gradient(135deg, var(--badge-silver-light) 0%, var(--badge-silver-accent) 100%);
}

.category-card--system .category-icon .iconfont {
  color: var(--badge-silver-icon);
}

.category-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow: hidden;
}

.category-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 0;
}

.category-card--system .category-name {
  color: var(--badge-silver-text);
}

.category-type {
  font-size: 13px;
  color: var(--text-tertiary);
  margin: 0;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: var(--space-2);
}

.card-actions .custom-button {
  flex: 1;
  justify-content: center;
}

.card-actions .custom-button .iconfont {
  margin-right: 4px;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
  }

  .category-grid {
    grid-template-columns: 1fr;
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
