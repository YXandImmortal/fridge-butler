<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="物品分类"
        description="为食材建立分类体系，让冰箱管理更有条理，查找也更高效。"
    />
    <el-tour-step
        target=".page-header"
        title="创建分类"
        description="点击「新建分类」即可创建自定义分类，如冷冻食品、调味品等，按需灵活设置。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".category-section"
        title="我的分类"
        description="自定义分类支持编辑和删除，用于归类您自己的食材，让管理更个性化。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".category-section--system"
        title="系统默认"
        description="系统预置了常用分类，点击标题可展开查看，作为您自定义分类的参考。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        title="分类完毕"
        description="合理的分类让食材管理事半功倍，添加物品时记得选择对应分类哦。"
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
  tourStore.completeScene(TOUR_SCENES.ITEM_CATEGORY)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.ITEM_CATEGORY)
}

defineExpose({
  start
})
</script>
