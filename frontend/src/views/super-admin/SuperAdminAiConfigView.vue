<template>
  <div class="admin-ai-container">
    <div class="page-header">
      <div class="page-header-left">
        <i class="iconfont icon-deepseek page-header-icon" />
        <h1 class="page-title">AI 配置</h1>
      </div>
      <p class="page-subtitle">统一管理全局模型、API 密钥与调用用量</p>
    </div>

    <div class="config-card" v-loading="configLoading">
      <div class="card-header">
        <div class="card-title-wrapper">
          <i class="iconfont icon-sliders card-title-icon" />
          <div>
            <h3 class="card-title">全局模型配置</h3>
            <p class="card-subtitle">保存后应用于聊天、推荐、容量估算和定时任务等所有 AI 功能</p>
          </div>
        </div>
      </div>

      <el-form :model="configForm" label-position="top" class="config-form" @submit.prevent>
        <div class="form-grid">
          <el-form-item label="模型厂商" required>
            <CustomSelect
              v-model="configForm.provider"
              :options="providerOptions"
              :full-width="true"
              placeholder="请选择模型厂商"
              @change="handleProviderChange"
            />
          </el-form-item>

          <el-form-item label="模型 ID" required>
            <CustomSelect
              v-model="configForm.model"
              :options="availableModels"
              :full-width="true"
              filterable
              allow-create
              filter-placeholder="输入模型 ID 搜索或创建"
              placeholder="选择预设模型或输入模型 ID"
            />
            <p class="field-hint">自定义模型需支持 Chat Completions、JSON Object 和流式输出</p>
          </el-form-item>
        </div>

        <el-form-item label="接口地址">
          <CustomInput :model-value="currentProvider?.baseUrl || ''" disabled icon="icon-deepseek" />
        </el-form-item>

        <el-form-item label="API 密钥" :required="apiKeyRequired">
          <CustomInput
            v-model="configForm.apiKey"
            type="password"
            showPassword
            autocomplete="new-password"
            :maxlength="512"
            icon="icon-key"
            :placeholder="apiKeyPlaceholder"
          />
          <div class="key-status">
            <span v-if="providerChanged" class="key-status-warning">
              切换厂商后必须填写新厂商的 API 密钥
            </span>
            <span v-else-if="configForm.hasApiKey" class="key-status-ready">
              当前密钥：{{ configForm.apiKeyMasked || '已配置' }}，留空表示保持不变
            </span>
            <span v-else class="key-status-warning">尚未配置可用的 API 密钥</span>
          </div>
        </el-form-item>

        <div class="security-note">
          <i class="iconfont icon-alert" />
          <span>API 密钥使用 AES-GCM 加密存储，接口不会回传完整密钥。更换密钥只需重新填写并保存。</span>
        </div>

        <div class="form-footer">
          <CustomButton type="primary" :loading="saving" @click="handleSave">
            <i class="iconfont icon-save" />
            保存并应用
          </CustomButton>
        </div>
      </el-form>
    </div>

    <div class="usage-section">
      <div class="usage-header">
        <div class="card-title-wrapper">
          <i class="iconfont icon-chart card-title-icon" />
          <div>
            <h3 class="card-title">AI 请求用量</h3>
            <p class="card-subtitle">按实际发往模型服务的请求统计，包含失败与重试</p>
          </div>
        </div>
        <CustomSegmented
          v-model="usageDays"
          :options="usageDayOptions"
          size="small"
          @change="fetchUsage"
        />
      </div>

      <div class="stats-row">
        <StatCard :value="usage.requestTotal" label="累计请求" icon-class="icon-deepseek" suffix=" 次" />
        <StatCard :value="usage.requestToday" label="今日请求" icon-class="icon-chart" type="success" suffix=" 次" />
        <StatCard :value="usage.successTotal" label="累计成功" icon-class="icon-check" type="success" suffix=" 次" />
        <StatCard :value="usage.failureTotal" label="累计失败" icon-class="icon-close" type="danger" suffix=" 次" />
      </div>

      <div class="chart-card" v-loading="usageLoading">
        <div class="chart-header">
          <h3 class="chart-title">近 {{ usageDays }} 天请求趋势</h3>
          <CustomButton plain size="small" :loading="usageLoading" @click="fetchUsage">
            刷新
          </CustomButton>
        </div>
        <v-chart
          v-if="usage.trend.length > 0"
          class="chart-body"
          :option="trendChartOption"
          autoresize
        />
        <el-empty v-else-if="!usageLoading" description="暂无请求统计数据" class="chart-empty" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { LegacyGridContainLabel } from 'echarts/features'
import VChart from 'vue-echarts'
import CustomButton from '@/components/ui/CustomButton.vue'
import CustomInput from '@/components/ui/CustomInput.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import CustomSegmented from '@/components/ui/CustomSegmented.vue'
import StatCard from '@/components/data-center/StatCard.vue'
import { getAiConfig, getAiProviders, getAiUsage, updateAiConfig } from '@/api/admin.js'
import { getChartThemeColors } from '@/utils/data-analysis.js'
import { useThemeStore } from '@/stores/theme.js'
import showMessage from '@/utils/message.js'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent, LegacyGridContainLabel])

const themeStore = useThemeStore()
const configLoading = ref(false)
const usageLoading = ref(false)
const saving = ref(false)
const providers = ref([])
const savedProvider = ref('DEEPSEEK')
const usageDays = ref(7)
const usage = ref({ requestTotal: 0, requestToday: 0, successTotal: 0, failureTotal: 0, trend: [] })
const configForm = reactive({
  provider: 'DEEPSEEK',
  model: '',
  apiKey: '',
  hasApiKey: false,
  apiKeyMasked: ''
})

const usageDayOptions = [
  { label: '近 7 天', value: 7 },
  { label: '近 30 天', value: 30 }
]

const providerOptions = computed(() => providers.value.map(item => ({
  label: item.providerName,
  value: item.provider
})))

const currentProvider = computed(() => providers.value.find(item => item.provider === configForm.provider))
const providerChanged = computed(() => configForm.provider !== savedProvider.value)
const apiKeyRequired = computed(() => providerChanged.value || !configForm.hasApiKey)
const apiKeyPlaceholder = computed(() => {
  if (apiKeyRequired.value) return '请输入 API 密钥'
  return '留空则保持当前密钥不变'
})
const availableModels = computed(() => {
  const models = [...(currentProvider.value?.models || [])]
  if (configForm.model && !models.includes(configForm.model)) models.push(configForm.model)
  return models.map(model => ({ label: model, value: model }))
})

const trendChartOption = computed(() => {
  const colors = getChartThemeColors(themeStore.theme === 'dark')
  const dates = usage.value.trend.map(item => item.date)
  const seriesConfig = [
    { name: '总请求', field: 'totalCount', color: '#64B5F6' },
    { name: '成功', field: 'successCount', color: '#81C784' },
    { name: '失败', field: 'failureCount', color: '#E57373' }
  ]
  return {
    color: seriesConfig.map(item => item.color),
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: { color: colors.textColor }
    },
    legend: {
      data: seriesConfig.map(item => item.name),
      top: '2%',
      textStyle: { color: colors.subTextColor, fontSize: 12 }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '18%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { lineStyle: { color: colors.axisLineColor } },
      axisLabel: {
        color: colors.subTextColor,
        fontSize: 11,
        formatter: value => typeof value === 'string' ? value.slice(5).replace('-', '/') : value
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: colors.splitLineColor, type: 'dashed' } },
      axisLabel: { color: colors.subTextColor }
    },
    series: seriesConfig.map(item => ({
      name: item.name,
      type: 'line',
      smooth: true,
      showSymbol: false,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { width: 2.5, color: item.color },
      itemStyle: { color: item.color },
      data: usage.value.trend.map(row => row[item.field] ?? 0)
    }))
  }
})

const applyConfig = data => {
  configForm.provider = data.provider
  configForm.model = data.model || ''
  configForm.hasApiKey = Boolean(data.hasApiKey)
  configForm.apiKeyMasked = data.apiKeyMasked || ''
  configForm.apiKey = ''
  savedProvider.value = data.provider
}

const fetchConfig = async () => {
  configLoading.value = true
  try {
    const [providerRes, configRes] = await Promise.all([getAiProviders(), getAiConfig()])
    if (providerRes.code !== 200 || !Array.isArray(providerRes.data)) {
      throw new Error(providerRes.message || '获取厂商目录失败')
    }
    if (configRes.code !== 200 || !configRes.data) {
      throw new Error(configRes.message || '获取 AI 配置失败')
    }
    providers.value = providerRes.data
    applyConfig(configRes.data)
  } catch (error) {
    console.error('获取 AI 配置失败:', error)
    showMessage.error(error.message || '获取 AI 配置失败')
  } finally {
    configLoading.value = false
  }
}

const handleProviderChange = () => {
  configForm.model = currentProvider.value?.models?.[0] || ''
  configForm.apiKey = ''
}

const handleSave = async () => {
  const model = configForm.model.trim()
  if (!model) {
    showMessage.warning('请选择或输入模型 ID')
    return
  }
  if (apiKeyRequired.value && !configForm.apiKey.trim()) {
    showMessage.warning(providerChanged.value ? '切换厂商时必须填写新厂商的 API 密钥' : '请输入 API 密钥')
    return
  }

  saving.value = true
  try {
    const payload = { provider: configForm.provider, model }
    if (configForm.apiKey.trim()) payload.apiKey = configForm.apiKey.trim()
    const res = await updateAiConfig(payload)
    if (res.code !== 200 || !res.data) {
      showMessage.error(res.message || '保存 AI 配置失败')
      return
    }
    applyConfig(res.data)
    showMessage.success('AI 配置已保存并应用')
  } catch (error) {
    console.error('保存 AI 配置失败:', error)
  } finally {
    saving.value = false
  }
}

const fetchUsage = async () => {
  usageLoading.value = true
  try {
    const res = await getAiUsage(usageDays.value)
    if (res.code === 200 && res.data) {
      usage.value = {
        requestTotal: res.data.requestTotal ?? 0,
        requestToday: res.data.requestToday ?? 0,
        successTotal: res.data.successTotal ?? 0,
        failureTotal: res.data.failureTotal ?? 0,
        trend: Array.isArray(res.data.trend) ? res.data.trend : []
      }
    } else {
      showMessage.error(res.message || '获取 AI 请求用量失败')
    }
  } catch (error) {
    console.error('获取 AI 请求用量失败:', error)
  } finally {
    usageLoading.value = false
  }
}

onMounted(() => {
  fetchConfig()
  fetchUsage()
})
</script>

<style scoped lang="scss">
.admin-ai-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.page-header {
  margin-bottom: var(--space-6);
}

.page-header-left,
.card-title-wrapper,
.usage-header,
.chart-header {
  display: flex;
  align-items: center;
}

.page-header-left {
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.page-header-icon {
  font-size: 28px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.page-subtitle,
.card-subtitle {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.config-card,
.chart-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.card-header {
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-color);
}

.card-title-wrapper {
  gap: var(--space-3);
  min-width: 0;
}

.card-title-icon {
  font-size: 23px;
  color: var(--primary-color);
  flex-shrink: 0;
}

.card-title,
.chart-title {
  margin: 0 0 var(--space-1);
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-5);
}

.field-hint,
.key-status {
  margin: var(--space-2) 0 0;
  font-size: 12px;
  line-height: 1.6;
}

.field-hint {
  color: var(--text-tertiary);
}

.key-status-ready {
  color: var(--success-color);
}

.key-status-warning {
  color: var(--warn-color);
}

.security-note {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--primary-10);
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.6;

  .iconfont {
    color: var(--primary-color);
    margin-top: 2px;
  }
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-5);
  padding-top: var(--space-5);
  border-top: 1px solid var(--border-color);
}

.usage-section {
  margin-top: var(--space-6);
}

.usage-header {
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-5);
  margin-bottom: var(--space-5);
}

.chart-card {
  min-height: 340px;
}

.chart-header {
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.chart-body {
  width: 100%;
  height: 280px;
}

.chart-empty {
  height: 260px;
}

@media (max-width: 1100px) {
  .stats-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
  }

  .config-card,
  .chart-card {
    padding: var(--space-4);
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .stats-row {
    gap: var(--space-4);
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }

  .stats-row {
    grid-template-columns: 1fr;
  }

  .usage-header {
    align-items: flex-start;
    flex-direction: column;
  }
}

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
