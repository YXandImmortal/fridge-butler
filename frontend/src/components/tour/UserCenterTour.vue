<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="个人中心"
        description="在这里管理账号信息，还能查看你的成就、徽章和冰箱保鲜数据。"
    />
    <el-tour-step
        target=".profile-avatar"
        title="头像设置"
        description="点击头像即可选择系统预设头像，保存后立即生效。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".profile-form"
        title="基本信息"
        description="查看和修改用户名、手机号和邮箱。若未绑定邮箱，可点击右侧按钮进行绑定。"
        placement="right"
        :show-arrow="false"
    />
    <el-tour-step
        target=".profile-actions"
        title="快捷操作"
        description="保存修改、更改密码、绑定/修改邮箱或退出登录，常用功能一键直达。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".achievement-panel-wrapper"
        title="成就概览"
        description="这里展示你的等级、EXP、冰鲜连续天数、保鲜评分和最近徽章，点击右上角可折叠或进入设置。"
        placement="left"
        :show-arrow="false"
    />
    <el-tour-step
        target=".achievement-tabs-card"
        title="成就详情"
        description="徽章墙、EXP 日志、月度报告和成就设置都在这里切换查看，帮助你追踪冰箱管理成就。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        title="资料与成就管理"
        description="以上就是个人中心的主要功能，保持信息更新、积极管理冰箱，解锁更多成就吧！"
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
  tourStore.completeScene(TOUR_SCENES.USER_CENTER)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.USER_CENTER)
}

defineExpose({
  start
})
</script>
