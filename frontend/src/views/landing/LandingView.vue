<template>
  <div class="landing-page">
    <!-- 导航栏 -->
    <nav class="landing-navbar" :class="{ 'is-scrolled': isScrolled }">
      <div class="navbar-container">
        <div class="navbar-brand" @click="scrollToTop">
          <div class="brand-logo">
            <Logo/>
          </div>
          <span class="brand-name text-gradient-primary">{{ systemName || '冰箱管家' }}</span>
        </div>
        <div class="navbar-actions">
          <ThemeToggle/>
          <CustomButton
              class="login-btn"
              type="primary"
              @click="goToLogin"
          >
            <i class="iconfont icon-login"/> 登录
          </CustomButton>
          <CustomButton
              class="register-btn"
              @click="goToRegister"
          >
            <i class="iconfont icon-user-plus"/> 注册
          </CustomButton>
        </div>
      </div>
    </nav>

    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-bg-decoration">
        <div class="hero-blob blob-1"></div>
        <div class="hero-blob blob-2"></div>
        <div class="hero-grid"></div>
      </div>
      <div class="hero-container">
        <div class="hero-content">
          <div class="hero-badge">
            <span class="badge-dot"></span>
            <span>Release 版本正式发布</span>
          </div>
          <h1 class="hero-title">
            智能冰箱管理<br/>
            <span class="text-gradient-primary">从这里开始</span>
          </h1>
          <p class="hero-subtitle">
            一站式智能冰箱食材管理平台，支持多冰箱管理、食材追踪、过期预警、
            数据可视化分析，让您的厨房生活更加高效便捷。
          </p>
          <div class="hero-actions">
            <CustomButton
                class="hero-btn-primary"
                type="primary"
                size="large"
                round
                @click="goToRegister"
            >
              免费开始使用
              <i class="iconfont icon-forwardburger" style="margin-left: 4px;"/>
            </CustomButton>
            <CustomButton
                class="hero-btn-secondary"
                size="large"
                round
                plain
                @click="scrollToFeatures"
            >
              了解更多
            </CustomButton>
          </div>
        </div>
        <div class="hero-visual">
          <div class="hero-card-stack">
            <div class="hero-card card-back"></div>
            <div class="hero-card card-mid"></div>
            <div class="hero-card card-front">
              <div class="card-front-content">
                <div class="card-icon">
                  <i class="iconfont icon-fridge-line"/>
                </div>
                <div class="card-text">
                  <div class="card-label">当前冰箱</div>
                  <div class="card-value">3 台</div>
                </div>
              </div>
              <div class="card-items">
                <div class="mini-item" style="--delay: 0s; transition: all 0.3s ease"><i class="iconfont icon-item"/> 食材 128</div>
                <div class="mini-item" style="--delay: 0.1s; transition: all 0.3s ease"><i class="iconfont icon-label"/> 分类 12</div>
                <div class="mini-item" style="--delay: 0.2s; transition: all 0.3s ease"><i class="iconfont icon-alert"/> 待处理 5</div>
                <div class="mini-item" style="--delay: 0.3s; transition: all 0.3s ease"><i class="iconfont icon-chart"/> 近 7 天存取记录</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="hero-scroll-hint" @click="scrollToFeatures">
        <div class="scroll-mouse">
          <div class="scroll-wheel"></div>
        </div>
        <span>向下滚动</span>
      </div>
    </section>

    <!-- 功能特色区 -->
    <section id="features" class="features-section" ref="featuresSection">
      <div class="section-container">
        <div class="section-header">
          <span class="section-tag">核心功能</span>
          <h2 class="section-title">全方位食材管理方案</h2>
          <p class="section-desc">
            从食材入库到过期提醒，从单台冰箱到多设备管理，覆盖您厨房管理的每一个场景
          </p>
        </div>
        <div class="features-grid">
          <div
              v-for="(feature, index) in features"
              :key="feature.title"
              class="feature-card"
              :class="{ 'is-visible': visibleSections.features }"
              :style="{ '--delay': `${index * 0.1}s` }"
          >
            <div class="feature-icon-wrapper" :style="{ background: feature.gradient }">
              <i :class="['iconfont', feature.icon]"/>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 核心优势区 -->
    <section class="advantages-section" ref="advantagesSection">
      <div class="section-container">
        <div class="section-header">
          <span class="section-tag">技术优势</span>
          <h2 class="section-title">为什么选择我们</h2>
          <p class="section-desc">
            采用现代化技术架构，为用户提供流畅、安全、智能的使用体验
          </p>
        </div>
        <div class="advantages-list">
          <div
              v-for="(adv, index) in advantages"
              :key="adv.title"
              class="advantage-item"
              :class="{ 'is-visible': visibleSections.advantages }"
              :style="{ '--delay': `${index * 0.15}s` }"
          >
            <div class="advantage-num">0{{ index + 1 }}</div>
            <div class="advantage-content">
              <h3 class="advantage-title">{{ adv.title }}</h3>
              <p class="advantage-desc">{{ adv.desc }}</p>
            </div>
            <div class="advantage-icon">
              <i :class="['iconfont', adv.icon]"/>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据统计区 -->
    <section class="stats-section" ref="statsSection">
      <div class="stats-bg"></div>
      <div class="section-container">
        <div class="stats-grid">
          <div
              v-for="(stat, index) in stats"
              :key="stat.label"
              class="stat-item"
              :class="{ 'is-visible': visibleSections.stats }"
              :style="{ '--delay': `${index * 0.1}s` }"
          >
            <div class="stat-value">
              <span class="stat-number">{{ stat.displayValue }}</span>
              <span class="stat-suffix">{{ stat.suffix }}</span>
            </div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA 区域 -->
    <section class="cta-section">
      <div class="cta-card">
        <div class="cta-content">
          <h2 class="cta-title">准备好管理您的冰箱了吗？</h2>
          <p class="cta-desc">
            立即注册，开启智能食材管理之旅。完全免费，无需信用卡。
          </p>
          <div class="cta-actions">
            <CustomButton
                type="primary"
                size="large"
                round
                @click="goToRegister"
            >
              立即注册
            </CustomButton>
            <CustomButton
                size="large"
                round
                plain
                @click="goToLogin"
            >
              已有账号？登录
            </CustomButton>
          </div>
        </div>
        <div class="cta-decoration">
          <div class="cta-circle c1"></div>
          <div class="cta-circle c2"></div>
          <div class="cta-circle c3"></div>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="landing-footer">
      <div class="footer-content">
        <div class="footer-brand">
          <div class="footer-logo">
            <Logo/>
          </div>
          <span class="footer-name">{{ systemName || '冰箱管家' }}</span>
          <p class="footer-slogan">智能食材管理，让生活更简单</p>
        </div>
        <div class="footer-links">
          <div class="footer-col">
            <h4>产品</h4>
            <a @click="scrollToFeatures">功能特色</a>
            <a @click="goToLogin">登录系统</a>
            <a @click="goToRegister">注册账号</a>
          </div>
          <div class="footer-col">
            <h4>关于</h4>
            <a href="https://github.com/YXandImmortal" target="_blank" rel="noopener noreferrer">开发者主页</a>
            <a href="https://hyperos.mi.com/font" target="_blank" rel="noopener noreferrer">MiSans 字体</a>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© 2026 {{ systemName || '冰箱管理系统' }} by YXandImmortal</span>
        <span class="footer-version">版本 {{ systemVersion || '1.0.0' }}</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted, reactive} from 'vue'
import {useRouter} from 'vue-router'
import {useSystemStore} from '@/stores/system'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import Logo from '@/components/brand/Logo.vue'

const router = useRouter()
const systemStore = useSystemStore()
const {systemName, systemVersion, getSystemInfo} = systemStore

// 滚动状态
const isScrolled = ref(false)
const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

// 可见性状态（用于滚动动画）
const visibleSections = reactive({
  features: false,
  advantages: false,
  stats: false
})

const featuresSection = ref(null)
const advantagesSection = ref(null)
const statsSection = ref(null)

let observer = null

onMounted(async () => {
  await getSystemInfo()
  window.addEventListener('scroll', handleScroll, {passive: true})
  handleScroll()

  // 初始化 Intersection Observer
  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        if (entry.target === featuresSection.value) visibleSections.features = true
        if (entry.target === advantagesSection.value) visibleSections.advantages = true
        if (entry.target === statsSection.value) {
          visibleSections.stats = true
          startCountUp()
        }
      }
    })
  }, {threshold: 0.15})

  if (featuresSection.value) observer.observe(featuresSection.value)
  if (advantagesSection.value) observer.observe(advantagesSection.value)
  if (statsSection.value) observer.observe(statsSection.value)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  if (observer) observer.disconnect()
})

// 数字滚动动画
const stats = reactive([
  {value: 99.9, suffix: '%', label: '系统稳定性', displayValue: '0'},
  {value: 3, suffix: '秒', label: '平均响应时间', displayValue: '0'},
  {value: 24, suffix: '/7', label: '全天候服务', displayValue: '0'},
  {value: 100, suffix: '+', label: '支持食材种类', displayValue: '0'}
])

let countUpStarted = false
const startCountUp = () => {
  if (countUpStarted) return
  countUpStarted = true
  stats.forEach(stat => {
    const duration = 1500
    const steps = 60
    const increment = stat.value / steps
    let current = 0
    let step = 0
    const timer = setInterval(() => {
      step++
      current = Math.min(increment * step, stat.value)
      if (stat.value % 1 !== 0) {
        stat.displayValue = current.toFixed(1)
      } else {
        stat.displayValue = Math.floor(current).toString()
      }
      if (step >= steps) {
        stat.displayValue = stat.value % 1 !== 0 ? stat.value.toFixed(1) : stat.value.toString()
        clearInterval(timer)
      }
    }, duration / steps)
  })
}

// 功能数据
const features = [
  {
    icon: 'icon-fridge-line',
    title: '多冰箱管理',
    desc: '支持同时管理多台冰箱设备，独立追踪每台冰箱的存储状况，满足家庭与办公多场景需求。',
    gradient: 'linear-gradient(135deg, var(--primary-color), var(--success-color))'
  },
  {
    icon: 'icon-item',
    title: '食材追踪',
    desc: '精确记录每件食材的入库时间、保质期与存储位置，智能计算剩余有效期，让管理井井有条。',
    gradient: 'linear-gradient(135deg, var(--color-emerald), var(--color-cyan-500))'
  },
  {
    icon: 'icon-charts-pie',
    title: '数据中心',
    desc: '可视化图表展示食材消耗趋势、分类占比与过期分析，帮助您科学规划采购与饮食。',
    gradient: 'linear-gradient(135deg, var(--color-purple-500), var(--color-pink-500))'
  },
  {
    icon: 'icon-label-alt-multiple',
    title: '智能分类',
    desc: '灵活的分类与单位管理体系，支持自定义食材类别、计量单位，适配各类食材特性。',
    gradient: 'linear-gradient(135deg, var(--color-orange-500), var(--warn-color))'
  },
  {
    icon: 'icon-notification',
    title: '消息通知',
    desc: '食材临期自动预警，重要变动实时推送，确保您不会错过任何需要关注的食材状态。',
    gradient: 'linear-gradient(135deg, var(--danger-color), var(--color-pink-400))'
  },
  {
    icon: 'icon-layout',
    title: '响应式设计',
    desc: '完美适配桌面、平板与手机等多种设备，无论身在何处，都能随时掌握冰箱动态。',
    gradient: 'linear-gradient(135deg, var(--color-cyan-500), var(--primary-color))'
  }
]

// 优势数据
const advantages = [
  {
    icon: 'icon-chart',
    title: '现代化技术架构',
    desc: '基于 Vue 3 + Element Plus 构建，配合 Pinia 状态管理，提供流畅的交互体验与稳定的性能表现。'
  },
  {
    icon: 'icon-lock',
    title: '安全的数据保护',
    desc: '采用行业标准的身份认证与权限控制机制，区分普通用户与超级管理员，保障数据安全。'
  },
  {
    icon: 'icon-reload',
    title: '持续迭代更新',
    desc: 'Release 版本标志着系统进入稳定阶段，我们将持续听取用户反馈，不断优化功能与体验。'
  },
  {
    icon: 'icon-device-phone',
    title: '跨平台兼容',
    desc: '基于现代浏览器技术，无需安装客户端，打开网页即可使用，支持各类主流浏览器。'
  }
]

// 导航方法
const goToLogin = () => router.push('/login')
const goToRegister = () => router.push('/register')
const scrollToTop = () => window.scrollTo({top: 0, behavior: 'smooth'})
const scrollToFeatures = () => {
  document.getElementById('features')?.scrollIntoView({behavior: 'smooth'})
}
</script>

<style scoped lang="scss">
/* =========================================================
 * 系统介绍页面样式
 * ========================================================= */

.landing-page {
  min-height: 100vh;
  background: var(--page-bg);
  color: var(--text-primary);
  font-family: 'MiSans', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  transition: background-color 0.3s ease, color 0.3s ease;
  overflow-x: hidden;
}

/* ---------------------------------------------------------
 * 导航栏
 * --------------------------------------------------------- */
.landing-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: var(--header-height);
  background: transparent;
  backdrop-filter: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;

  &.is-scrolled {
    background: var(--glass-bg);
    backdrop-filter: blur(12px);
    border-bottom-color: var(--border-light);
    box-shadow: var(--shadow-sm);
  }
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-6);
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  cursor: pointer;
  user-select: none;
}

.brand-logo {
  width: 36px;
  height: 36px;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: var(--space-6);
}

.login-btn,
.register-btn {
  font-weight: 500;
}

/* ---------------------------------------------------------
 * Hero 区域
 * --------------------------------------------------------- */
.hero-section {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: calc(var(--header-height) + var(--space-10)) var(--space-6) var(--space-10);
  overflow: hidden;
}

.hero-bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.hero-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: pulse 10s ease-in-out infinite;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: var(--primary-color);
  top: -10%;
  right: -5%;
  animation-delay: 0s;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: var(--success-color);
  bottom: -10%;
  left: -5%;
  animation-delay: -5s;
  opacity: 0.25;
}

.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
      linear-gradient(var(--border-color) 1px, transparent 1px),
      linear-gradient(90deg, var(--border-color) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: radial-gradient(ellipse at center, black 0%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse at center, black 0%, transparent 70%);
  opacity: 0.5;
}

.hero-container {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-10);
  align-items: center;
}

.hero-content {
  animation: fade-in-up 0.8s ease-out;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: 6px 14px;
  background: var(--primary-10);
  border: 1px solid var(--border-light);
  border-radius: 20px;
  font-size: 13px;
  color: var(--primary-color);
  font-weight: 500;
  margin-bottom: var(--space-5);
}

.badge-dot {
  width: 8px;
  height: 8px;
  background: var(--color-emerald);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

.hero-title {
  font-size: 52px;
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: -0.5px;
  margin-bottom: var(--space-5);
  color: var(--text-primary);
}

.hero-subtitle {
  font-size: 17px;
  line-height: 1.7;
  color: var(--text-secondary);
  margin-bottom: var(--space-8);
  max-width: 480px;
}

.hero-actions {
  display: flex;
  gap: var(--space-4);
}

.hero-btn-primary {
  font-weight: 600;
  padding: 0 32px;
  height: 48px;
  font-size: 16px;
  background: var(--gradient-btn);
  border: none;

  &:hover {
    opacity: 0.9;
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }
}

.hero-btn-secondary {
  font-weight: 600;
  padding: 0 32px;
  height: 48px;
  font-size: 16px;
}

/* Hero 视觉卡片堆 */
.hero-visual {
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fade-in-up 0.8s ease-out 0.2s both;
}

.hero-card-stack {
  position: relative;
  width: 320px;
  height: 380px;
}

.hero-card {
  position: absolute;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  transition: all 0.4s ease;
}

.card-back {
  width: 280px;
  height: 340px;
  background: linear-gradient(135deg, var(--primary-color), var(--success-color));
  top: 20px;
  left: 40px;
  opacity: 0.3;
  transform: rotate(6deg);
}

.card-mid {
  width: 290px;
  height: 350px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  top: 10px;
  left: 20px;
  opacity: 0.6;
  transform: rotate(3deg);
}

.card-front {
  width: 300px;
  height: 360px;
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--border-light);
  top: 0;
  left: 0;
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);

  &:hover {
    transform: translateY(-8px) rotate(-1deg);
    box-shadow: var(--shadow-card-hover);
  }
}

.card-front-content {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  background: var(--gradient-btn);
  display: flex;
  align-items: center;
  justify-content: center;

  i {
    font-size: 28px;
    color: white;
  }
}

.card-label {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-bottom: 2px;
}

.card-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.card-items {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.mini-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--main-content-bg);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-secondary);
  animation: fade-in-up 0.5s ease-out var(--delay) both;

  i {
    font-size: 16px;
    color: var(--primary-color);
  }
}

/* 滚动提示 */
.hero-scroll-hint {
  position: absolute;
  bottom: 32px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--text-tertiary);
  font-size: 12px;
  cursor: pointer;
  animation: fade-in-up 1s ease-out 1s both;
  z-index: 2;

  &:hover {
    color: var(--primary-color);
  }
}

.scroll-mouse {
  width: 22px;
  height: 34px;
  border: 2px solid currentColor;
  border-radius: 11px;
  display: flex;
  justify-content: center;
  padding-top: 6px;
}

.scroll-wheel {
  width: 4px;
  height: 6px;
  background: currentColor;
  border-radius: 2px;
  animation: scroll-wheel 1.5s ease-in-out infinite;
}

@keyframes scroll-wheel {
  0%, 100% {
    transform: translateY(0);
    opacity: 1;
  }
  50% {
    transform: translateY(6px);
    opacity: 0.3;
  }
}

/* ---------------------------------------------------------
 * 通用区块样式
 * --------------------------------------------------------- */
.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--space-6);
}

.section-header {
  text-align: center;
  margin-bottom: var(--space-10);
}

.section-tag {
  display: inline-block;
  padding: 4px 14px;
  background: var(--primary-10);
  border: 1px solid var(--border-light);
  border-radius: 16px;
  font-size: 13px;
  color: var(--primary-color);
  font-weight: 500;
  margin-bottom: var(--space-4);
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--space-4);
  letter-spacing: -0.3px;
}

.section-desc {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.6;
  max-width: 560px;
  margin: 0 auto;
}

/* ---------------------------------------------------------
 * 功能特色区
 * --------------------------------------------------------- */
.features-section {
  padding: 100px 0;
  position: relative;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-6);
}

.feature-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-8);
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(24px);

  &.is-visible {
    animation: fade-in-up 0.6s ease-out var(--delay) both;
  }

  &:hover {
    transform: translateY(-6px);
    box-shadow: var(--shadow-card-hover);
    border-color: var(--border-light);
  }
}

.feature-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-5);

  i {
    font-size: 26px;
    color: white;
  }
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-3);
}

.feature-desc {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-secondary);
}

/* ---------------------------------------------------------
 * 优势区
 * --------------------------------------------------------- */
.advantages-section {
  padding: 100px 0;
  background: var(--main-content-bg);
  transition: background-color 0.3s ease;
}

.advantages-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  max-width: 800px;
  margin: 0 auto;
}

.advantage-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: var(--space-6);
  padding: var(--space-6) var(--space-8);
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateX(-20px);

  &.is-visible {
    animation: slide-in-right 0.6s ease-out var(--delay) both;
  }

  &:hover {
    border-color: var(--border-light);
    box-shadow: var(--shadow-md);
    transform: translateX(4px);
  }
}

.advantage-num {
  font-size: 28px;
  font-weight: 800;
  color: var(--primary-20);
  line-height: 1;
  min-width: 40px;
  text-align: center;
}

.advantage-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.advantage-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.advantage-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--primary-10);
  display: flex;
  align-items: center;
  justify-content: center;

  i {
    font-size: 22px;
    color: var(--primary-color);
  }
}

/* ---------------------------------------------------------
 * 数据统计区
 * --------------------------------------------------------- */
.stats-section {
  padding: 80px 0;
  position: relative;
  overflow: hidden;
}

.stats-bg {
  position: absolute;
  inset: 0;
  background: var(--gradient-page);
  opacity: 0.5;
}

.stats-grid {
  position: relative;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-6);
}

.stat-item {
  text-align: center;
  padding: var(--space-8);
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  opacity: 0;
  transform: scale(0.9);
  transition: all 0.3s ease;

  &.is-visible {
    animation: fade-in-up 0.5s ease-out var(--delay) both;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-md);
  }
}

.stat-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
  margin-bottom: var(--space-3);
}

.stat-number {
  font-size: 40px;
  font-weight: 800;
  background: var(--gradient-text);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.stat-suffix {
  font-size: 20px;
  font-weight: 600;
  color: var(--primary-color);
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

/* ---------------------------------------------------------
 * CTA 区域
 * --------------------------------------------------------- */
.cta-section {
  padding: 80px var(--space-6);
}

.cta-card {
  position: relative;
  max-width: 1000px;
  margin: 0 auto;
  background: linear-gradient(135deg, var(--primary-color), var(--color-primary-500));
  border-radius: var(--radius-lg);
  padding: 60px var(--space-8);
  overflow: hidden;
  text-align: center;
}

.cta-content {
  position: relative;
  z-index: 1;
}

.cta-title {
  font-size: 32px;
  font-weight: 700;
  color: white;
  margin-bottom: var(--space-4);
}

.cta-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: var(--space-8);
  max-width: 480px;
  margin-left: auto;
  margin-right: auto;
}

.cta-actions {
  display: flex;
  gap: var(--space-4);
  justify-content: center;

  :deep(.custom-button) {
    font-weight: 600;
    padding: 0 32px;
    height: 48px;
    font-size: 16px;
  }

  :deep(.custom-button--primary) {
    background: white;
    color: var(--color-primary-500);
    border-color: white;

    &:hover {
      background: rgba(255, 255, 255, 0.9);
      color: var(--color-primary-500);
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
    }
  }

  :deep(.custom-button--default.is-plain) {
    background: transparent;
    color: white;
    border-color: rgba(255, 255, 255, 0.5);

    &:hover {
      background: rgba(255, 255, 255, 0.1);
      border-color: white;
      color: white;
      box-shadow: none;
    }
  }
}

.cta-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.cta-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.c1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -80px;
}

.c2 {
  width: 200px;
  height: 200px;
  bottom: -60px;
  right: 60px;
}

.c3 {
  width: 120px;
  height: 120px;
  top: 40px;
  right: -30px;
}

/* ---------------------------------------------------------
 * 页脚
 * --------------------------------------------------------- */
.landing-footer {
  background: var(--main-content-bg);
  border-top: 1px solid var(--border-color);
  padding: var(--space-10) var(--space-6) var(--space-6);
  transition: background-color 0.3s ease;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-10);
  margin-bottom: var(--space-10);
}

.footer-brand {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.footer-logo {
  width: 40px;
  height: 40px;
}

.footer-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.footer-slogan {
  font-size: 14px;
  color: var(--text-secondary);
}

.footer-links {
  display: flex;
  gap: var(--space-10);
  justify-content: flex-end;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);

  h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: var(--space-2);
  }

  a {
    font-size: 14px;
    color: var(--text-secondary);
    cursor: pointer;
    transition: color 0.2s ease;
    text-decoration: none;

    &:hover {
      color: var(--primary-color);
    }
  }
}

.footer-bottom {
  max-width: 1200px;
  margin: 0 auto;
  padding-top: var(--space-6);
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--text-tertiary);
}

.footer-version {
  font-family: 'Courier New', monospace;
}

/* ---------------------------------------------------------
 * 响应式设计
 * --------------------------------------------------------- */
@media (max-width: 992px) {
  .hero-container {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .hero-subtitle {
    margin-left: auto;
    margin-right: auto;
  }

  .hero-actions {
    justify-content: center;
  }

  .hero-visual {
    display: none;
  }

  .hero-title {
    font-size: 40px;
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .advantage-item {
    grid-template-columns: auto 1fr;
  }

  .advantage-icon {
    display: none;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }

  .section-title {
    font-size: 28px;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }

  .footer-content {
    grid-template-columns: 1fr;
    gap: var(--space-8);
  }

  .footer-links {
    justify-content: flex-start;
  }

  .footer-bottom {
    flex-direction: column;
    gap: var(--space-2);
    text-align: center;
  }

  .cta-title {
    font-size: 24px;
  }

  .advantage-item {
    padding: var(--space-4) var(--space-5);
  }

  .advantage-num {
    font-size: 22px;
    min-width: 32px;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 28px;
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
  }

  .hero-btn-primary,
  .hero-btn-secondary {
    width: 100%;
    max-width: 280px;
  }

  .navbar-actions {
    .login-btn {
      display: none;
    }
  }

  .cta-actions {
    flex-direction: column;
    align-items: center;

    :deep(.el-button) {
      width: 100%;
      max-width: 280px;
    }
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-number {
    font-size: 32px;
  }
}
</style>
