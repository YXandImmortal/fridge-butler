<!-- src/App.vue -->
<template>
  <!-- 路由出口：所有页面都在这里渲染（登录页/首页/注册页） -->
  <router-view />
</template>

<script setup>
// 无需编写任何代码，空的 setup 即可
</script>

<style>
/* 全局样式（可选，比如重置默认样式） */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  --page-bg: #f5f5f5;
}
</style>