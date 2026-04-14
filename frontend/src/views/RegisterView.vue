<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 class="register-title">用户注册</h2>
      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="80px"
          class="register-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码"></el-input>
        </el-form-item>
        <el-form-item class="register-btn-group">
          <el-button type="primary" @click="handleRegister">注册</el-button>
          <el-button @click="router.push('/login')">返回登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const registerFormRef = ref()
// 注册表单（需和后端RegisterRequest字段一致）
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

// 注册表单验证规则
const registerRules = ref({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 注册方法
const handleRegister = async () => {
  const valid = await registerFormRef.value.validate()
  if (!valid) return

  try {
    await request({
      url: '/auth/register/user',
      method: 'post',
      data: {
        username: registerForm.value.username,
        password: registerForm.value.password
        // 其他字段根据后端RegisterRequest补充
      }
    })
    ElMessage.success('注册成功！请登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error('注册失败：' + (error.message || '用户名已存在'))
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--page-bg);
}

.register-card {
  width: 400px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  color: var(--el-color-primary);
  margin-bottom: 20px;
}

.register-form {
  margin-top: 20px;
}

.register-btn-group {
  display: flex;
  justify-content: space-between;
}
</style>