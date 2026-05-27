<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="冰箱一览"
        description="这里是您所有冰箱的管理中心，支持查看、搜索、筛选和创建新冰箱。"
    />
    <el-tour-step
        target=".page-header"
        title="创建冰箱"
        description="点击「新建冰箱」手动创建，或使用「AI 帮我创建」让 AI 管家辅助您完成。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".search-bar"
        title="搜索与筛选"
        description="通过关键词搜索冰箱名称，或按类型、排序方式筛选，快速定位目标冰箱。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".fridge-card"
        title="冰箱卡片"
        description="每张卡片展示冰箱名称、物品数量、容量等关键信息，默认冰箱会有金色标识。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        title="管理冰箱"
        description="点击卡片即可进入冰箱详情页，编辑信息或管理内部物品。快去创建您的第一台冰箱吧！"
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
  tourStore.completeScene(TOUR_SCENES.FRIDGE_LIST)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.FRIDGE_LIST)
}

defineExpose({
  start
})
</script>
