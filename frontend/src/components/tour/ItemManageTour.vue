<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="物品管理"
        description="这里是冰箱的「内部仓库」，您可以查看、添加、编辑和取出食材。"
    />
    <el-tour-step
        target=".back-bar"
        title="快速导航"
        description="点击「返回冰箱详情」回到详情页，或使用「切换冰箱」直接管理其他冰箱。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".capacity-card"
        title="容量监控"
        description="顶部进度条实时展示冰箱已用容量，帮助您合理规划存储空间。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".header-actions"
        title="物品操作"
        description="支持「AI 帮我添加」智能录入、「添加物品」手动创建，以及「批量取出」快速操作。"
        placement="left"
        :show-arrow="false"
    />
    <el-tour-step
        target=".search-section"
        title="搜索与筛选"
        description="通过关键词、分类、单位类型、具体单位等多维度筛选，并支持排序，快速定位食材。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".item-table"
        title="物品列表"
        description="表格展示了物品名称、数量、生产日期、保质期、新鲜度、入库时间等信息，操作列支持取出、编辑和删除。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        title="管理有序"
        description="合理记录生产日期和保质期，系统会自动计算新鲜度，让食材管理更科学。"
    />
  </el-tour>
</template>

<script setup>
import {ref} from 'vue'
import {useTourStore, TOUR_SCENES} from '@/stores/tour'

const visible = ref(false)
const tourStore = useTourStore()

function start() {
  visible.value = true
}

function onFinish() {
  tourStore.completeScene(TOUR_SCENES.ITEM_MANAGE)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.ITEM_MANAGE)
}

defineExpose({
  start
})
</script>
