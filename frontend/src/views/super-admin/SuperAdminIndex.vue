<template>
  <div class="admin-index">
    <h1>管理员页面</h1>
    <p>只有超级管理员可以访问此页面</p>
  </div>
</template>

<script setup>
// 管理员页面逻辑
</script>

<style scoped>
.admin-index {
  padding: 20px;
  text-align: center;
}

h1 {
  color: var(--text-primary);
  margin-bottom: var(--space-5);
}

p {
  color: var(--text-secondary);
}
</style>