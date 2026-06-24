<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <!-- 列表态指引 -->
    <template v-if="mode === 'list'">
      <el-tour-step
          title="核对入库"
          description="在这里查看采购方案的结算记录，并对待核对方案进行入库确认。"
      />
      <el-tour-step
          target=".page-header"
          title="结算中心"
          description="这里汇集了待核对、已完成和已取消的采购方案，方便您跟踪每一笔采购的后续状态。"
          placement="bottom"
          :show-arrow="false"
      />
      <el-tour-step
          target=".tab-segmented"
          title="状态筛选"
          description="通过 Tab 切换查看「待核对」「已完成」「已取消」的方案，快速定位目标记录。"
          placement="bottom"
          :show-arrow="false"
      />
      <el-tour-step
          target=".tab-content"
          title="方案列表"
          description="点击待核对方案进入结算流程；已完成的方案可查看详情；已取消的方案支持删除。"
          placement="top"
          :show-arrow="false"
      />
      <el-tour-step
          title="开始核对"
          description="选择待核对方案后，即可进入核对入库流程，确认实际采购结果并更新库存。"
      />
    </template>

    <!-- 核对流程态指引 -->
    <template v-else>
      <el-tour-step
          title="核对入库流程"
          description="请按步骤确认实际采购情况，并填写需要存入冰箱的物品信息。"
      />
      <el-tour-step
          target=".back-bar"
          title="返回列表"
          description="点击「返回列表」可随时回到方案列表，当前已填写的核对进度会自动保留。"
          placement="bottom"
          :show-arrow="false"
      />
      <el-tour-step
          target=".card-header"
          title="核对进度"
          description="顶部显示当前核对方案名称、目标冰箱，以及「核对采购物品 → 填写入库信息」两步进度。"
          placement="bottom"
          :show-arrow="false"
      />
      <el-tour-step
          target=".item-list"
          title="物品核对"
          description="第一步确认每件物品是否采购及实际数量；第二步填写已采购物品的生产日期、保质期、存放位置等入库信息。"
          placement="top"
          :show-arrow="false"
      />
      <el-tour-step
          target=".progress-section"
          title="进度统计"
          description="进度条实时展示确定入库、已采购不入库、跳过三类物品的数量分布，方便整体把控。"
          placement="top"
          :show-arrow="false"
      />
      <el-tour-step
          target=".card-actions"
          title="流程操作"
          description="第一步点击「下一步」进入入库信息填写；第二步点击「确认结算」完成整个核对流程。"
          placement="top"
          :show-arrow="false"
      />
      <el-tour-step
          title="完成结算"
          description="核对无误后确认结算，系统将自动更新冰箱库存，并生成对应的采购任务完成记录。"
      />
    </template>
  </el-tour>
</template>

<script setup>
import {ref} from 'vue'
import {useTourStore, TOUR_SCENES} from '@/stores/tour'

const props = defineProps({
  mode: {
    type: String,
    default: 'list',
    validator: (value) => ['list', 'form'].includes(value)
  }
})

const visible = ref(false)
const tourStore = useTourStore()

function start() {
  visible.value = true
}

function onFinish() {
  tourStore.completeScene(TOUR_SCENES.PURCHASE_CHECK_IN)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.PURCHASE_CHECK_IN)
}

defineExpose({
  start
})
</script>
