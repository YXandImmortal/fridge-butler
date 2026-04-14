<template>
  <div class="smart-fridge-container" style="padding: 50px; text-align: center">
    <h1>智能冰箱管理系统</h1>

    <!-- Element Plus 按钮组件 -->
    <el-button type="primary" @click="showMessage">添加食材</el-button>
    <el-button type="success">查看库存</el-button>
    <el-button type="danger">删除食材</el-button>

    <!-- Element Plus 卡片组件 -->
    <el-card style="width: 300px; margin: 20px auto">
      食材：牛奶<br/>
      保质期：3天
    </el-card>
  </div>
</template>

<script setup>
// 自动导入，无需手动写 import！
import { ElMessage } from 'element-plus'

const showMessage = () => {
  ElMessage.success('食材添加成功！')
}
</script>

<style scoped>
h1 {
  color: #409eff;
}
</style>