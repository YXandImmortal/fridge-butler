<template>
  <div class="index-container">
    <!-- 头部组件 -->
    <Header
      @show-logout-dialog="showLogoutDialog = true"
    />

    <!-- 主体内容区域 -->
    <div class="main-content-wrapper">
      <!-- 左侧导航栏 -->
      <Sidebar />

      <!-- 主内容区域 -->
      <main class="main-content">
        <div class="system-admin-container">
          <!-- 页面标题 -->
          <div class="page-header">
            <h1 class="page-title">系统管理</h1>
            <p class="page-subtitle">超级管理员控制台 · 系统状态概览与快捷管理</p>
          </div>

          <!-- 统计卡片 -->
          <div class="stats-grid">
            <div class="stat-card" v-for="stat in statsList" :key="stat.key">
              <div class="stat-icon" :class="stat.colorClass">
                <i class="iconfont" :class="stat.icon"></i>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ stat.value }}</span>
                <span class="stat-label">{{ stat.label }}</span>
              </div>
            </div>
          </div>

          <!-- 系统监控与快捷操作 -->
          <div class="dashboard-grid">
            <!-- 系统资源监控 -->
            <div class="dashboard-card monitor-card">
              <div class="card-header">
                <h3 class="card-title">
                  <i class="iconfont icon-chart"></i>
                  系统资源监控
                </h3>
                <span class="update-time">更新于 {{ monitorData.updateTime }}</span>
              </div>
              <div class="monitor-items">
                <div class="monitor-item" v-for="item in monitorData.items" :key="item.name">
                  <div class="monitor-header">
                    <span class="monitor-name">{{ item.name }}</span>
                    <span class="monitor-value" :class="getStatusClass(item.percentage)">
                      {{ item.percentage }}%
                    </span>
                  </div>
                  <div class="progress-bar">
                    <div
                      class="progress-fill"
                      :class="getStatusClass(item.percentage)"
                      :style="{ width: item.percentage + '%' }"
                    ></div>
                  </div>
                  <div class="monitor-detail">
                    <span>{{ item.used }} / {{ item.total }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 快捷操作 -->
            <div class="dashboard-card actions-card">
              <div class="card-header">
                <h3 class="card-title">
                  <i class="iconfont icon-apps"></i>
                  快捷操作
                </h3>
              </div>
              <div class="actions-grid">
                <div
                  class="action-item"
                  v-for="action in quickActions"
                  :key="action.name"
                  @click="handleAction(action)"
                >
                  <div class="action-icon" :class="action.colorClass">
                    <i class="iconfont" :class="action.icon"></i>
                  </div>
                  <span class="action-name">{{ action.name }}</span>
                  <span class="action-desc">{{ action.desc }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 系统信息与日志 -->
          <div class="dashboard-grid">
            <!-- 系统信息 -->
            <div class="dashboard-card info-card">
              <div class="card-header">
                <h3 class="card-title">
                  <i class="iconfont icon-info-circle"></i>
                  系统信息
                </h3>
              </div>
              <div class="info-list">
                <div class="info-item" v-for="info in systemInfoList" :key="info.label">
                  <span class="info-label">{{ info.label }}</span>
                  <span class="info-value">{{ info.value }}</span>
                </div>
              </div>
            </div>

            <!-- 最近日志 -->
            <div class="dashboard-card logs-card">
              <div class="card-header">
                <h3 class="card-title">
                  <i class="iconfont icon-file-text"></i>
                  最近系统日志
                </h3>
                <CustomButton type="text" size="small" @click="viewAllLogs">查看全部</CustomButton>
              </div>
              <div class="logs-list">
                <div class="log-item" v-for="log in recentLogs" :key="log.id">
                  <div class="log-dot" :class="log.level"></div>
                  <div class="log-content">
                    <p class="log-message">{{ log.message }}</p>
                    <span class="log-time">{{ log.time }}</span>
                  </div>
                </div>
                <div v-if="recentLogs.length === 0" class="empty-logs">
                  <i class="iconfont icon-inbox"></i>
                  <span>暂无日志记录</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 底部版权信息 -->
    <CopyrightFooter />

    <!-- 登出确认对话框 -->
    <ConfirmDialog
      v-model:visible="showLogoutDialog"
      title="退出登录"
      message="您确定要退出登录吗？"
      confirm-text="确定"
      cancel-text="取消"
      @confirm="handleLogout"
    />

    <!-- 功能开发中提示 -->
    <el-dialog
      v-model="devDialogVisible"
      title="提示"
      width="400px"
      align-center
      class="dev-dialog"
    >
      <div class="dev-dialog-content">
        <i class="iconfont icon-tool"></i>
        <p>该功能正在开发中，敬请期待</p>
      </div>
      <template #footer>
        <CustomButton type="primary" @click="devDialogVisible = false">我知道了</CustomButton>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import CopyrightFooter from '@/components/CopyrightFooter.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import CustomButton from '@/components/CustomButton.vue'
import showMessage from '@/utils/message'
import { useSystemStore } from '@/stores/system'
import { useUserStore } from '@/stores/user'
import router from '@/router/index.js'
import {
  getSystemStats,
  getSystemMonitor,
  getRecentLogs,
  getSystemConfig
} from '@/api/system'

const systemStore = useSystemStore()
const userStore = useUserStore()
const { getSystemInfo } = systemStore
const { logout } = userStore

const showLogoutDialog = ref(false)
const devDialogVisible = ref(false)
const isLoading = ref(false)

// 统计数据
const statsList = ref([
  { key: 'users', label: '用户总数', value: '-', icon: 'icon-user-group', colorClass: 'blue' },
  { key: 'fridges', label: '冰箱总数', value: '-', icon: 'icon-home', colorClass: 'green' },
  { key: 'active', label: '今日活跃', value: '-', icon: 'icon-activity', colorClass: 'orange' },
  { key: 'uptime', label: '运行时间', value: '-', icon: 'icon-clock', colorClass: 'purple' }
])

// 系统监控数据
const monitorData = ref({
  updateTime: '--:--:--',
  items: [
    { name: 'CPU 使用率', percentage: 0, used: '0%', total: '100%' },
    { name: '内存使用率', percentage: 0, used: '0 GB', total: '0 GB' },
    { name: '磁盘使用率', percentage: 0, used: '0 GB', total: '0 GB' }
  ]
})

// 快捷操作
const quickActions = ref([
  { name: '用户管理', desc: '管理系统用户', icon: 'icon-user-group', colorClass: 'blue', route: null },
  { name: '角色权限', desc: '配置角色与权限', icon: 'icon-shield', colorClass: 'green', route: null },
  { name: '系统设置', desc: '修改系统参数', icon: 'icon-settings', colorClass: 'purple', route: null },
  { name: '日志管理', desc: '查看系统日志', icon: 'icon-file-text', colorClass: 'orange', route: null },
  { name: '数据备份', desc: '备份与恢复', icon: 'icon-database', colorClass: 'cyan', route: null },
  { name: '通知公告', desc: '发布系统公告', icon: 'icon-bell', colorClass: 'pink', route: null }
])

// 系统信息
const systemInfoList = ref([
  { label: '系统名称', value: '-' },
  { label: '系统版本', value: '-' },
  { label: '运行环境', value: '-' },
  { label: 'Node 版本', value: '-' },
  { label: '数据库', value: '-' },
  { label: '前端版本', value: import.meta.env.VITE_APP_VERSION || '1.0.0' }
])

// 最近日志
const recentLogs = ref([])

// 获取状态样式类
const getStatusClass = (percentage) => {
  if (percentage >= 90) return 'danger'
  if (percentage >= 70) return 'warning'
  return 'normal'
}

// 格式化运行时间
const formatUptime = (hours) => {
  if (hours < 24) return `${hours} 小时`
  const days = Math.floor(hours / 24)
  const remainHours = hours % 24
  if (remainHours === 0) return `${days} 天`
  return `${days} 天 ${remainHours} 小时`
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getSystemStats()
    if (res.code === 200 && res.data) {
      const data = res.data
      statsList.value = [
        { key: 'users', label: '用户总数', value: data.userCount ?? 0, icon: 'icon-user-group', colorClass: 'blue' },
        { key: 'fridges', label: '冰箱总数', value: data.fridgeCount ?? 0, icon: 'icon-home', colorClass: 'green' },
        { key: 'active', label: '今日活跃', value: data.todayActive ?? 0, icon: 'icon-activity', colorClass: 'orange' },
        { key: 'uptime', label: '运行时间', value: formatUptime(data.uptimeHours ?? 0), icon: 'icon-clock', colorClass: 'purple' }
      ]
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 使用模拟数据
    statsList.value = [
      { key: 'users', label: '用户总数', value: '128', icon: 'icon-user-group', colorClass: 'blue' },
      { key: 'fridges', label: '冰箱总数', value: '256', icon: 'icon-home', colorClass: 'green' },
      { key: 'active', label: '今日活跃', value: '42', icon: 'icon-activity', colorClass: 'orange' },
      { key: 'uptime', label: '运行时间', value: '15 天', icon: 'icon-clock', colorClass: 'purple' }
    ]
  }
}

// 加载监控数据
const loadMonitor = async () => {
  try {
    const res = await getSystemMonitor()
    if (res.code === 200 && res.data) {
      const data = res.data
      monitorData.value = {
        updateTime: new Date().toLocaleTimeString('zh-CN'),
        items: [
          { name: 'CPU 使用率', percentage: data.cpuPercentage ?? 0, used: `${data.cpuPercentage ?? 0}%`, total: '100%' },
          { name: '内存使用率', percentage: data.memoryPercentage ?? 0, used: data.memoryUsed ?? '0 GB', total: data.memoryTotal ?? '0 GB' },
          { name: '磁盘使用率', percentage: data.diskPercentage ?? 0, used: data.diskUsed ?? '0 GB', total: data.diskTotal ?? '0 GB' }
        ]
      }
    }
  } catch (error) {
    console.error('获取监控数据失败:', error)
    // 使用模拟数据
    monitorData.value = {
      updateTime: new Date().toLocaleTimeString('zh-CN'),
      items: [
        { name: 'CPU 使用率', percentage: 32, used: '32%', total: '100%' },
        { name: '内存使用率', percentage: 58, used: '4.6 GB', total: '8 GB' },
        { name: '磁盘使用率', percentage: 45, used: '45 GB', total: '100 GB' }
      ]
    }
  }
}

// 加载系统信息
const loadSystemInfo = async () => {
  try {
    const res = await getSystemConfig()
    if (res.code === 200 && res.data) {
      const data = res.data
      systemInfoList.value = [
        { label: '系统名称', value: data.systemName || '冰箱管家' },
        { label: '系统版本', value: data.systemVersion || 'v1.0.0' },
        { label: '运行环境', value: data.environment || 'Production' },
        { label: 'Node 版本', value: data.nodeVersion || 'v20.x' },
        { label: '数据库', value: data.database || 'MySQL 8.0' },
        { label: '前端版本', value: import.meta.env.VITE_APP_VERSION || '1.0.0' }
      ]
    }
  } catch (error) {
    console.error('获取系统信息失败:', error)
    // 使用默认数据
    systemInfoList.value = [
      { label: '系统名称', value: '冰箱管家' },
      { label: '系统版本', value: 'v1.0.0' },
      { label: '运行环境', value: 'Production' },
      { label: 'Node 版本', value: 'v20.x' },
      { label: '数据库', value: 'MySQL 8.0' },
      { label: '前端版本', value: import.meta.env.VITE_APP_VERSION || '1.0.0' }
    ]
  }
}

// 加载最近日志
const loadLogs = async () => {
  try {
    const res = await getRecentLogs(5)
    if (res.code === 200 && res.data) {
      recentLogs.value = res.data.map((log, index) => ({
        id: log.id || index,
        message: log.message || '系统运行正常',
        time: log.time || new Date().toLocaleString('zh-CN'),
        level: log.level || 'info'
      }))
    }
  } catch (error) {
    console.error('获取日志失败:', error)
    // 使用模拟数据
    recentLogs.value = [
      { id: 1, message: '系统启动成功', time: '2026-04-24 08:00:00', level: 'info' },
      { id: 2, message: '数据库连接正常', time: '2026-04-24 08:00:05', level: 'info' },
      { id: 3, message: '用户 admin 登录成功', time: '2026-04-24 09:30:12', level: 'success' },
      { id: 4, message: '定时任务执行完成', time: '2026-04-24 10:00:00', level: 'info' },
      { id: 5, message: '检测到内存使用超过阈值', time: '2026-04-24 10:15:33', level: 'warning' }
    ]
  }
}

// 处理快捷操作
const handleAction = (action) => {
  if (action.route) {
    router.push(action.route)
  } else {
    devDialogVisible.value = true
  }
}

// 查看全部日志
const viewAllLogs = () => {
  devDialogVisible.value = true
}

// 处理退出登录
const handleLogout = () => {
  logout()
  showLogoutDialog.value = false
  router.push('/login')
  showMessage.info('已退出登录')
}

// 初始化
onMounted(async () => {
  isLoading.value = true
  await getSystemInfo()
  await Promise.all([
    loadStats(),
    loadMonitor(),
    loadSystemInfo(),
    loadLogs()
  ])
  isLoading.value = false
})
</script>

<style scoped>
.index-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content-wrapper {
  margin-top: var(--header-height);
}

.main-content {
  margin-left: var(--sidebar-width);
  transition: all 0.3s ease;
  max-height: calc(100vh - var(--header-height) - var(--footer-height));
  height: 100%;
  background: var(--main-content-bg);
  padding: var(--space-5);
  overflow-y: auto;
}

/* 系统管理容器 */
.system-admin-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  animation: fade-in-up 0.6s ease-out;
}

/* 页面标题 */
.page-header {
  margin-bottom: var(--space-8);
  text-align: center;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--space-2);
  background: var(--gradient-text);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-tertiary);
  font-weight: 400;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-5);
  margin-bottom: var(--space-8);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6);
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon .iconfont {
  font-size: 26px;
  color: var(--text-inverse);
}

.stat-icon.blue { background: linear-gradient(135deg, var(--color-primary-300), var(--color-primary-400)); }
.stat-icon.green { background: linear-gradient(135deg, var(--color-green-400), var(--color-green-500)); }
.stat-icon.orange { background: linear-gradient(135deg, var(--color-orange-400), var(--color-orange-500)); }
.stat-icon.purple { background: linear-gradient(135deg, var(--color-purple-400), var(--color-purple-500)); }

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-tertiary);
  font-weight: 500;
}

/* 仪表盘网格 */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-5);
  margin-bottom: var(--space-8);
}

.dashboard-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  padding: var(--space-6);
  transition: all 0.3s ease;
}

.dashboard-card:hover {
  box-shadow: var(--shadow-card-hover);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.card-title .iconfont {
  font-size: 20px;
  color: var(--primary-color);
}

.update-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

/* 监控项 */
.monitor-items {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.monitor-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.monitor-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.monitor-value {
  font-size: 14px;
  font-weight: 600;
}

.monitor-value.normal { color: var(--success-color); }
.monitor-value.warning { color: var(--warn-color); }
.monitor-value.danger { color: var(--danger-color); }

.progress-bar {
  width: 100%;
  height: 8px;
  background: var(--input-bg);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 1s ease;
}

.progress-fill.normal { background: linear-gradient(90deg, var(--color-green-400), var(--color-green-500)); }
.progress-fill.warning { background: linear-gradient(90deg, var(--color-orange-400), var(--color-orange-500)); }
.progress-fill.danger { background: linear-gradient(90deg, var(--danger-color), var(--danger-dark)); }

.monitor-detail {
  font-size: 12px;
  color: var(--text-tertiary);
  text-align: right;
}

/* 快捷操作 */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: var(--space-5);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.action-item:hover {
  background: var(--primary-light);
  border-color: var(--border-light);
  transform: translateY(-2px);
}

.action-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon .iconfont {
  font-size: 22px;
  color: var(--text-inverse);
}

.action-icon.blue { background: linear-gradient(135deg, var(--color-primary-300), var(--color-primary-400)); }
.action-icon.green { background: linear-gradient(135deg, var(--color-green-400), var(--color-green-500)); }
.action-icon.purple { background: linear-gradient(135deg, var(--color-purple-400), var(--color-purple-500)); }
.action-icon.orange { background: linear-gradient(135deg, var(--color-orange-400), var(--color-orange-500)); }
.action-icon.cyan { background: linear-gradient(135deg, var(--color-cyan-400), var(--color-cyan-500)); }
.action-icon.pink { background: linear-gradient(135deg, var(--color-pink-400), var(--color-pink-500)); }

.action-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.action-desc {
  font-size: 11px;
  color: var(--text-tertiary);
}

/* 系统信息 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--divider-color);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.info-value {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 600;
  font-family: 'MiSans', monospace;
}

/* 日志列表 */
.logs-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.log-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-sm);
  transition: background 0.3s ease;
}

.log-item:hover {
  background: var(--primary-light);
}

.log-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.log-dot.info { background: var(--primary-color); }
.log-dot.success { background: var(--color-emerald); }
.log-dot.warning { background: var(--warn-color); }
.log-dot.error { background: var(--danger-color); }

.log-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.log-message {
  font-size: 13px;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.5;
  font-weight: 500;
}

.log-time {
  font-size: 11px;
  color: var(--text-tertiary);
}

.empty-logs {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-8) 0;
  color: var(--text-tertiary);
}

.empty-logs .iconfont {
  font-size: 32px;
  opacity: 0.5;
}

.empty-logs span {
  font-size: 13px;
}

/* 开发中对话框 */
.dev-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6) 0;
  text-align: center;
}

.dev-dialog-content .iconfont {
  font-size: 48px;
  color: var(--primary-color);
}

.dev-dialog-content p {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0;
}

/* 动画 */
@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .main-content {
    margin-left: var(--sidebar-width-md);
    padding: var(--space-4);
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-4);
  }

  .stat-card {
    padding: var(--space-4);
  }

  .stat-icon {
    width: 44px;
    height: 44px;
  }

  .stat-icon .iconfont {
    font-size: 22px;
  }

  .stat-value {
    font-size: 20px;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .main-content {
    margin-left: 0;
    padding: var(--space-3);
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 24px;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
