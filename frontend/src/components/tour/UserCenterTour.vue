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
        description="在这里管理您的账号信息、头像和密码，让账户更具个性化。"
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
        description="查看和修改用户名、手机号等资料，注册时间和角色信息一目了然。"
        placement="right"
        :show-arrow="false"
    />
    <el-tour-step
        target=".profile-actions"
        title="快捷操作"
        description="保存修改、更改密码或退出登录，常用功能一键直达。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        title="资料管理完毕"
        description="以上就是个人中心的主要功能，保持信息更新有助于获得更好的服务体验。"
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
