<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="冰箱详情"
        description="在这里查看和编辑单个冰箱的完整信息，灵活配置以符合您的实际需求。"
    />
    <el-tour-step
        target=".back-bar"
        title="快速导航"
        description="点击「返回列表」回到冰箱一览，或使用「切换冰箱」直接跳转至其他冰箱。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".detail-header"
        title="基本信息"
        description="头部展示了冰箱名称、描述和物品数量，右侧还能一键进入物品管理。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".detail-form"
        title="信息编辑"
        description="在这里修改冰箱名称、类型、容量、地址等属性，实时保存让您的管理更精准。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".detail-actions"
        title="保存与删除"
        description="编辑完成后点击「保存信息」生效；如不再需要该冰箱，可使用「删除冰箱」移除。"
        placement="left"
        :show-arrow="false"
    />
    <el-tour-step
        title="配置完成"
        description="冰箱信息设置完毕，接下来可以进入「物品管理」开始添加食材了。"
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
  tourStore.completeScene(TOUR_SCENES.FRIDGE_DETAIL)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.FRIDGE_DETAIL)
}

defineExpose({
  start
})
</script>
