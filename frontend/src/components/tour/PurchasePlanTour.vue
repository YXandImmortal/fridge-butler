<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <!-- 创建态指引 -->
    <template v-if="mode === 'creator'">
      <el-tour-step
          title="采购助手"
          description="在这里创建智能采购计划，AI 会结合冰箱数据为您的日常补货或特别场景推荐采购清单。"
      />
      <el-tour-step
          target=".page-header"
          title="创建采购计划"
          description="采购助手提供手动创建、日常 AI 推荐和特别场景生成三种方式，灵活满足不同需求。"
          placement="bottom"
          :show-arrow="false"
      />
      <el-tour-step
          target=".fridge-select-wrapper"
          title="选择目标冰箱"
          description="先选择要为哪台冰箱采购，后续生成的计划会自动关联到该冰箱。"
          placement="bottom"
          :show-arrow="false"
      />
      <el-tour-step
          target=".custom-segmented"
          title="切换创建方式"
          description="「新建计划」可基于模板手动创建；「日常采购」让 AI 根据日常消耗智能推荐；「特别采购」适合派对、聚餐等特别场景。"
          placement="bottom"
          :show-arrow="false"
      />
      <el-tour-step
          target=".tab-content"
          title="填写与生成"
          description="根据所选模式填写需求，点击「新建采购计划」或「AI助手智能创建」，即可生成采购清单并进入预览确认。"
          placement="top"
          :show-arrow="false"
      />
      <el-tour-step
          title="开始采购"
          description="确认计划后保存，您可以在「采购任务」中查看待办，或在「核对入库」中完成结算。祝您采购愉快！"
      />
    </template>

    <!-- 预览态指引 -->
    <template v-else>
      <el-tour-step
          title="计划预览"
          description="这是采购计划的预览确认页面，您可以在这里核对、补充或修改计划内容。"
      />
      <el-tour-step
          target=".cork-board"
          title="软木板预览区"
          description="所有计划信息以便签形式展示在软木板上，直观且易于编辑。"
          placement="top"
          :show-arrow="false"
      />
      <el-tour-step
          target=".cork-board-header"
          title="计划信息"
          description="便签顶部展示计划名称、所属冰箱和当前日期，点击名称即可修改。"
          placement="bottom"
          :show-arrow="false"
      />
      <el-tour-step
          target=".sticky-notes-grid"
          title="物品清单"
          description="每个物品对应一张便签，支持修改名称、数量、单位类型、单位和分类；点击「+ 添加物品」可继续补充。"
          placement="top"
          :show-arrow="false"
      />
      <el-tour-step
          target=".cork-board-actions"
          title="操作按钮"
          description="「返回」回到创建页；「保存为模板」将当前计划存为模板复用；「确定计划」生成待采购任务。"
          placement="top"
          :show-arrow="false"
      />
      <el-tour-step
          title="确认计划"
          description="核对无误后点击「确定计划」，即可在「采购任务」中看到该计划并继续后续采购流程。"
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
    default: 'creator',
    validator: (value) => ['creator', 'preview'].includes(value)
  }
})

const visible = ref(false)
const tourStore = useTourStore()

function start() {
  visible.value = true
}

function onFinish() {
  tourStore.completeScene(TOUR_SCENES.PURCHASE_PLAN)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.PURCHASE_PLAN)
}

defineExpose({
  start
})
</script>
