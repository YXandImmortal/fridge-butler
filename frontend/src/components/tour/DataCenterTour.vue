<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="数据中心"
        description="这里是您的冰箱数据驾驶舱，通过可视化图表洞察食材管理的全貌。"
    />
    <el-tour-step
        target=".stats-row"
        title="核心指标"
        description="冰箱数量、物品总件数、容量利用率和临期预警四大指标，全局状态一屏掌握。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".charts-row:nth-of-type(1)"
        title="分布与占比"
        description="左图看各冰箱的物品分布，右图看食材分类占比，了解库存结构。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".charts-row:nth-of-type(2)"
        title="新鲜度与趋势"
        description="左图展示食材新鲜度分布，右图呈现近 30 天出入库趋势，健康与动态兼顾。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".charts-row:nth-of-type(3)"
        title="保质期与容量"
        description="左图看保质期分布，右图看各冰箱容量利用率，科学规划不浪费。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".table-row"
        title="临期预警"
        description="底部清单罗列出即将过期和已过期物品，建议优先处理，减少食材浪费。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        title="数据驱动"
        description="善用数据中心，让每一次采购和消耗都有据可依，做聪明的食材管理者。"
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
  tourStore.completeScene(TOUR_SCENES.DATA_CENTER)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.DATA_CENTER)
}

defineExpose({
  start
})
</script>
