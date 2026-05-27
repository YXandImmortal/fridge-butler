<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="关于智鲜·引擎"
        description="这里记录了系统信息、功能亮点和更新动态，方便您全面了解产品。"
    />
    <el-tour-step
        target=".about-hero-card"
        title="系统概览"
        description="展示了当前系统版本和核心理念，点击 Logo 可快速回到首页。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".feature-grid"
        title="核心功能"
        description="带您一览系统的主要能力，包括 AI 管家、冰箱管理、数据分析等。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".update-card"
        title="最近更新"
        description="时间轴展示了版本迭代记录，重大更新会特别标注，方便您追踪新功能。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".info-card"
        title="技术支持"
        description="关于我们、联系方式和版权信息都在这里，遇到问题可随时查阅。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        title="探索更多"
        description="以上就是关于页面的全部内容，更多功能期待您在实际使用中发现！"
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
  tourStore.completeScene(TOUR_SCENES.ABOUT)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.ABOUT)
}

defineExpose({
  start
})
</script>
