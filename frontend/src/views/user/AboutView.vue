<template>
  <div class="about-container">
    <!-- 顶部系统信息卡片 -->
    <div class="about-hero-card">
      <div class="about-logo" @click="goToUserIndex">
        <div class="logo-icon">
          <Logo/>
        </div>
      </div>
      <h1 class="about-title">{{ systemName || '冰箱管理系统' }}</h1>
      <p class="about-version">当前版本：{{ systemVersion || 'v1.0.0' }}</p>
      <p class="about-slogan">
        {{ slogan || '智能管理冰箱食材，让新鲜触手可及' }}
      </p>
    </div>

    <!-- 系统介绍 -->
    <div class="about-section" v-if="systemDescription">
      <h2 class="section-title">
        <i class="iconfont icon-script-text"/>
        系统介绍
      </h2>
      <div class="description-card" v-html="systemDescription"/>
    </div>

    <!-- 功能特性区域 -->
    <div class="about-section">
      <h2 class="section-title">
        <i class="iconfont icon-bookmark"/>
        核心功能
      </h2>
      <div class="feature-grid">
        <div class="feature-card" v-for="(feature, index) in features" :key="index">
          <div class="feature-icon">
            <i :class="['iconfont', feature.icon]"/>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.description }}</p>
        </div>
      </div>
    </div>

    <!-- 更新日志 -->
    <div class="about-section">
      <h2 class="section-title">
        <i class="iconfont icon-calendar-check"/>
        最近更新
      </h2>
      <el-scrollbar height="500px" class="update-card" view-style="padding: 24px 32px;">
        <el-timeline>
          <el-timeline-item
              v-for="(update, index) in updates"
              :key="update.version"
              :timestamp="update.date"
              placement="top"
              :type="index === 0 && !update.isMajor ? 'primary' : ''"
              :class="{ 'major-update': update.isMajor }"
          >
            <div class="update-item-content" :class="{ 'major-update-item': update.isMajor }">
              <div class="timeline-version">
                {{ update.version }}
                <span v-if="update.isMajor" class="major-badge">
                  <i class="iconfont icon-star"/>
                  重大更新
                </span>
              </div>
              <div v-if="update.summary" class="update-summary">{{ update.summary }}</div>
              <ul class="update-list">
                <li v-for="(item, idx) in update.changes" :key="idx">{{ item }}</li>
              </ul>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-scrollbar>
    </div>

    <!-- 技术支持与版权 -->
    <div class="about-section">
      <h2 class="section-title">
        <i class="iconfont icon-info-box"/>
        关于我们
      </h2>
      <div class="info-card">
        <div class="info-row" v-for="(item, index) in about" :key="index">
          <span class="info-label">
            <i :class="['iconfont', item.icon]"/>
            {{ item.label }}
          </span>
          <span class="info-value">
            <template v-if="item.type === 'url'">
              <a :href="item.value" target="_blank" rel="noopener noreferrer" class="info-link">{{ item.value }}</a>
            </template>
            <template v-else-if="item.type === 'email'">
              <a :href="'mailto:' + item.value" class="info-link">{{ item.value }}</a>
            </template>
            <template v-else>{{ item.value }}</template>
          </span>
        </div>
      </div>
    </div>
    <AboutTour ref="tourRef"/>
  </div>
</template>

<script setup>
import {computed, ref, watch, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {useSystemStore} from '@/stores/system.js'
import Logo from '@/components/brand/Logo.vue'
import AboutTour from '@/components/tour/AboutTour.vue'
import {useTourStore, TOUR_SCENES} from '@/stores/tour.js'
import {getPublicConfig} from '@/api/system.js'

const router = useRouter()
const systemStore = useSystemStore()
const {systemName, systemVersion, slogan, features, updates, about} = systemStore

const systemDescription = ref('')

const fetchPublicConfig = async () => {
  try {
    const res = await getPublicConfig()
    if (res.code === 200 && res.data) {
      systemDescription.value = res.data.systemDescription || ''
    }
  } catch (error) {
    console.error('获取公开配置失败:', error)
  }
}

onMounted(() => {
  fetchPublicConfig()
})

const goToUserIndex = () => {
  router.push('/user/index')
}

// 当前年份
const currentYear = computed(() => new Date().getFullYear())

const tourRef = ref(null)
const tourStore = useTourStore()

watch(() => tourStore.pendingStartScene, (scene) => {
  if (scene === TOUR_SCENES.ABOUT) {
    tourRef.value?.start()
  }
})
</script>

<style scoped lang="scss">
.about-container {
  max-width: 960px;
  margin: 0 auto;
  animation: fade-in-up 0.6s ease-out;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* 顶部系统信息卡片 */
.about-hero-card {
  text-align: center;
  padding: 48px 40px;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.about-hero-card:hover {
  box-shadow: 0 12px 60px var(--glass-lavender-25);
  transform: translateY(-2px);
}

.about-logo {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.about-logo:hover {
  transform: scale(1.05);
}

.logo-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.about-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.about-version {
  font-size: 14px;
  color: var(--primary-color);
  font-weight: 500;
  margin-bottom: 12px;
  display: inline-block;
  padding: 4px 16px;
  background: var(--primary-light);
  border-radius: 20px;
}

.about-slogan {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
}

/* 通用区块样式 */
.about-section {
  animation: fade-in-up 0.6s ease-out;
  animation-fill-mode: both;
}

.about-section:nth-child(2) {
  animation-delay: 0.1s;
}

.about-section:nth-child(3) {
  animation-delay: 0.2s;
}

.about-section:nth-child(4) {
  animation-delay: 0.3s;
}

.about-section:nth-child(5) {
  animation-delay: 0.4s;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title .iconfont {
  font-size: 22px;
  color: var(--primary-color);
}

/* 功能特性网格 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.feature-card {
  padding: 28px;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.feature-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-4px);
  border-color: var(--primary-30);
}

.feature-icon {
  width: 48px;
  height: 48px;
  background: var(--primary-light);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.feature-icon .iconfont {
  font-size: 24px;
  color: var(--primary-color);
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.feature-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

/* 更新日志 */
.update-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);

}

.timeline-version {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 8px;
}

.update-summary {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 10px;
  line-height: 1.5;
}

/* Timeline 样式适配主题 */
.update-card :deep(.el-timeline) {
  padding-left: 4px;
}

.update-card :deep(.el-timeline-item__tail) {
  border-left-color: var(--primary-20);
}

.update-card :deep(.el-timeline-item__node--normal) {
  background-color: var(--primary-color);
}

.update-card :deep(.el-timeline-item__timestamp) {
  color: var(--text-tertiary);
  font-size: 13px;
}

.update-list {
  margin: 0;
  padding-left: 20px;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.8;
}

.update-list li {
  position: relative;
}

.update-list li::marker {
  color: var(--primary-color);
}

/* 重大更新时间线样式 */
.major-update :deep(.el-timeline-item__node--normal),
.major-update :deep(.el-timeline-item__node--primary) {
  background-color: var(--badge-gold);
}

.major-update :deep(.el-timeline-item__tail) {
  border-left-color: var(--badge-gold-shadow);
}

/* 重大更新内容卡片 */
.update-item-content {
  transition: all 0.3s ease;
}

.major-update-item {
  background: linear-gradient(135deg, var(--badge-gold-bg) 0%, var(--glass-bg) 60%);
  border: 1px solid var(--badge-gold);
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 4px 16px var(--badge-gold-shadow);
}

.major-update-item .timeline-version {
  color: var(--badge-gold-text);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.major-update-item .update-summary {
  color: var(--badge-gold-text);
}

.major-update-item .update-list li::marker {
  color: var(--badge-gold);
}

/* 重大更新徽章 */
.major-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  background: linear-gradient(135deg, var(--badge-gold) 0%, var(--badge-gold-hover) 100%);
  color: var(--badge-gold-text-dark);
  font-size: 11px;
  font-weight: 600;
  border-radius: 10px;
  box-shadow: 0 2px 8px var(--badge-gold-shadow);
}

.major-badge .iconfont {
  font-size: 12px;
}

/* 系统介绍描述卡片 */
.description-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  padding: 24px 32px;
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.description-card :deep(p) {
  margin: 0 0 12px;
}

.description-card :deep(p:last-child) {
  margin-bottom: 0;
}

.description-card :deep(ul),
.description-card :deep(ol) {
  padding-left: 20px;
  margin: 12px 0;
}

.description-card :deep(li) {
  margin-bottom: 6px;
}

.description-card :deep(a) {
  color: var(--primary-color);
  text-decoration: none;
}

.description-card :deep(a:hover) {
  text-decoration: underline;
}

/* 关于我们信息卡片 */
.info-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  padding: 24px 32px;
}

.info-row {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px solid var(--divider-color);
}

.info-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-row:first-child {
  padding-top: 0;
}

.info-label {
  width: 100px;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-label .iconfont {
  font-size: 16px;
  color: var(--primary-color);
}

.info-value {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
  word-break: break-all;
}

.info-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  padding-bottom: 2px;
}

.info-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--primary-color), var(--success-color));
  transition: width 0.3s ease;
  border-radius: 1px;
}

.info-link:hover {
  color: var(--primary-dark);
}

.info-link:hover::after {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .about-hero-card {
    padding: 36px 24px;
  }

  .about-title {
    font-size: 26px;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .info-row {
    flex-direction: column;
    gap: 6px;
  }

  .info-label {
    width: auto;
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .about-container {
    gap: 24px;
  }

  .about-hero-card {
    padding: 28px 20px;
  }

  .about-title {
    font-size: 22px;
  }

  .feature-card,
  .update-card,
  .info-card {
    padding: 20px;
  }
}

/* 动画定义 */
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
</style>
