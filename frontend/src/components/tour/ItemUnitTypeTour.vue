<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="单位分类"
        description="管理物品计量的单位体系，如重量、容量、数量等，让食材记录更规范。"
    />
    <el-tour-step
        target=".page-header"
        title="创建分类"
        description="点击「新建单位分类」建立新的计量维度，再为其添加具体单位。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".unit-type-section"
        title="我的单位"
        description="自定义单位分类可自由编辑，点击卡片还能展开管理其中的具体单位。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".unit-type-section--system"
        title="系统预设"
        description="系统内置了常用单位分类，供您直接使用或作为自定义的参考基准。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        title="计量规范"
        description="完善的单位体系让食材数量记录更准确，数据分析也更可靠。"
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
  tourStore.completeScene(TOUR_SCENES.ITEM_UNIT_TYPE)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.ITEM_UNIT_TYPE)
}

defineExpose({
  start
})
</script>
