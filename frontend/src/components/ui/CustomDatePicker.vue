<template>
  <div
    class="custom-date-picker"
    ref="pickerRef"
    :class="[
      sizeClass,
      variantClass,
      { 'is-open': isOpen, 'is-disabled': disabled, 'is-range': isRange }
    ]"
    :style="containerStyle"
  >
    <!-- 触发器 -->
    <div
      class="cdp-trigger"
      :class="{ 'is-disabled': disabled }"
      @click="toggleOpen"
    >
      <i class="iconfont icon-calendar cdp-trigger-icon"/>

      <!-- 单日期模式 -->
      <template v-if="!isRange">
        <span class="cdp-label" :class="{ 'is-placeholder': !displaySingle }">
          {{ displaySingle || placeholder }}
        </span>
      </template>

      <!-- 范围模式 -->
      <template v-else>
        <span class="cdp-label" :class="{ 'is-placeholder': !displayStart }">
          {{ displayStart || startPlaceholder }}
        </span>
        <span class="cdp-separator">{{ rangeSeparator }}</span>
        <i class="iconfont icon-calendar cdp-trigger-icon cdp-trigger-icon--end"/>
        <span class="cdp-label" :class="{ 'is-placeholder': !displayEnd }">
          {{ displayEnd || endPlaceholder }}
        </span>
      </template>

      <i
        v-if="clearable && hasValue"
        class="iconfont icon-close cdp-clear"
        @click.stop="handleClear"
      />
      <i v-else class="iconfont icon-chevron-down cdp-arrow"/>
    </div>

    <!-- 下拉面板 -->
    <transition name="dropdown">
      <div v-show="isOpen" class="cdp-dropdown">
        <!-- 单日期面板 -->
        <div v-if="!isRange" class="cdp-calendar">
          <!-- 日期视图 -->
          <template v-if="panelViewMode === 'date'">
            <div class="cdp-header">
              <button type="button" class="cdp-header-btn" @click="prevMonth">
                <i class="iconfont icon-calendar-arrow-left"/>
              </button>
              <span class="cdp-header-title">
                <span class="cdp-header-year" @click.stop="switchToYearView">{{ viewYear }}年</span>
                <span class="cdp-header-divider"> · </span>
                <span class="cdp-header-month" @click.stop="switchToMonthView">{{ viewMonth + 1 }}月</span>
              </span>
              <button type="button" class="cdp-header-btn" @click="nextMonth">
                <i class="iconfont icon-calendar-arrow-right"/>
              </button>
            </div>

            <div class="cdp-weekdays">
              <span v-for="w in weekDays" :key="w" class="cdp-weekday">{{ w }}</span>
            </div>

            <div class="cdp-days">
              <span
                v-for="(cell, idx) in calendarCells"
                :key="idx"
                class="cdp-day"
                :class="cellClasses(cell)"
                @click="selectSingle(cell)"
              >
                {{ cell.day }}
              </span>
            </div>

            <div class="cdp-footer">
              <span class="cdp-today-btn" @click="selectToday">
                <i class="iconfont icon-calendar-today"/> 今天
              </span>
            </div>
          </template>

          <!-- 月份视图 -->
          <template v-else-if="panelViewMode === 'month'">
            <div class="cdp-header">
              <button type="button" class="cdp-header-btn" @click="prevYear">
                <i class="iconfont icon-calendar-arrow-left"/>
              </button>
              <span class="cdp-header-title">
                <span class="cdp-header-year" @click.stop="switchToYearView">{{ viewYear }}年</span>
              </span>
              <button type="button" class="cdp-header-btn" @click="nextYear">
                <i class="iconfont icon-calendar-arrow-right"/>
              </button>
            </div>
            <div class="cdp-months">
              <span
                v-for="m in 12"
                :key="m"
                class="cdp-month"
                :class="monthClasses(m)"
                @click="selectMonth(m - 1)"
              >
                {{ m }}月
              </span>
            </div>
            <div class="cdp-footer">
              <span class="cdp-today-btn" @click="switchToDateView">
                <i class="iconfont icon-calendar-arrow-left"/> 返回
              </span>
            </div>
          </template>

          <!-- 年份视图 -->
          <template v-else-if="panelViewMode === 'year'">
            <div class="cdp-header">
              <button type="button" class="cdp-header-btn" @click="prevYearPage">
                <i class="iconfont icon-calendar-arrow-left"/>
              </button>
              <span class="cdp-header-title">{{ yearRangeStart }} - {{ yearRangeEnd }}</span>
              <button type="button" class="cdp-header-btn" @click="nextYearPage">
                <i class="iconfont icon-calendar-arrow-right"/>
              </button>
            </div>
            <div class="cdp-years">
              <span
                v-for="y in yearRangeList"
                :key="y"
                class="cdp-year"
                :class="yearClasses(y)"
                @click="selectYear(y)"
              >
                {{ y }}
              </span>
            </div>
            <div class="cdp-footer">
              <span class="cdp-today-btn" @click="switchToDateView">
                <i class="iconfont icon-calendar-arrow-left"/> 返回
              </span>
            </div>
          </template>
        </div>

        <!-- 范围面板（双月并排） -->
        <div v-else class="cdp-calendar cdp-calendar--range">
          <!-- 日期视图 -->
          <template v-if="panelViewMode === 'date'">
            <div class="cdp-range-panels">
              <!-- 左侧面板 -->
              <div class="cdp-range-panel">
                <div class="cdp-header">
                  <button type="button" class="cdp-header-btn" @click="prevMonth">
                    <i class="iconfont icon-calendar-arrow-left"/>
                  </button>
                  <span class="cdp-header-title">
                    <span class="cdp-header-year" @click.stop="switchToYearView">{{ viewYear }}年</span>
                    <span class="cdp-header-divider"> · </span>
                    <span class="cdp-header-month" @click.stop="switchToMonthView">{{ viewMonth + 1 }}月</span>
                  </span>
                  <span class="cdp-header-placeholder"/>
                </div>
                <div class="cdp-weekdays">
                  <span v-for="w in weekDays" :key="w" class="cdp-weekday">{{ w }}</span>
                </div>
                <div class="cdp-days">
                  <span
                    v-for="(cell, idx) in leftCells"
                    :key="idx"
                    class="cdp-day"
                    :class="rangeCellClasses(cell)"
                    @click="selectRange(cell)"
                    @mouseenter="handleRangeHover(cell)"
                  >
                    {{ cell.day }}
                  </span>
                </div>
              </div>

              <!-- 右侧面板 -->
              <div class="cdp-range-panel">
                <div class="cdp-header">
                  <span class="cdp-header-placeholder"/>
                  <span class="cdp-header-title">
                    <span class="cdp-header-year" @click.stop="switchToYearView">{{ rightViewYear }}年</span>
                    <span class="cdp-header-divider"> · </span>
                    <span class="cdp-header-month" @click.stop="switchToMonthView">{{ rightViewMonth + 1 }}月</span>
                  </span>
                  <button type="button" class="cdp-header-btn" @click="nextMonth">
                    <i class="iconfont icon-calendar-arrow-right"/>
                  </button>
                </div>
                <div class="cdp-weekdays">
                  <span v-for="w in weekDays" :key="w" class="cdp-weekday">{{ w }}</span>
                </div>
                <div class="cdp-days">
                  <span
                    v-for="(cell, idx) in rightCells"
                    :key="idx"
                    class="cdp-day"
                    :class="rangeCellClasses(cell)"
                    @click="selectRange(cell)"
                    @mouseenter="handleRangeHover(cell)"
                  >
                    {{ cell.day }}
                  </span>
                </div>
              </div>
            </div>

            <div class="cdp-footer cdp-footer--range">
              <span class="cdp-today-btn" @click="selectTodayRange">
                <i class="iconfont icon-calendar-today"/> 今天
              </span>
              <span class="cdp-today-btn" @click="selectRecent7Days">
                <i class="iconfont icon-calendar-week"/> 近7天
              </span>
              <span class="cdp-today-btn" @click="selectRecent30Days">
                <i class="iconfont icon-calendar-month"/> 近30天
              </span>
            </div>
          </template>

          <!-- 月份视图（范围模式共用） -->
          <template v-else-if="panelViewMode === 'month'">
            <div class="cdp-header">
              <button type="button" class="cdp-header-btn" @click="prevYear">
                <i class="iconfont icon-calendar-arrow-left"/>
              </button>
              <span class="cdp-header-title">
                <span class="cdp-header-year" @click.stop="switchToYearView">{{ viewYear }}年</span>
              </span>
              <button type="button" class="cdp-header-btn" @click="nextYear">
                <i class="iconfont icon-calendar-arrow-right"/>
              </button>
            </div>
            <div class="cdp-months">
              <span
                v-for="m in 12"
                :key="m"
                class="cdp-month"
                :class="monthClasses(m)"
                @click="selectMonth(m - 1)"
              >
                {{ m }}月
              </span>
            </div>
            <div class="cdp-footer">
              <span class="cdp-today-btn" @click="switchToDateView">
                <i class="iconfont icon-calendar-arrow-left"/> 返回
              </span>
            </div>
          </template>

          <!-- 年份视图（范围模式共用） -->
          <template v-else-if="panelViewMode === 'year'">
            <div class="cdp-header">
              <button type="button" class="cdp-header-btn" @click="prevYearPage">
                <i class="iconfont icon-calendar-arrow-left"/>
              </button>
              <span class="cdp-header-title">{{ yearRangeStart }} - {{ yearRangeEnd }}</span>
              <button type="button" class="cdp-header-btn" @click="nextYearPage">
                <i class="iconfont icon-calendar-arrow-right"/>
              </button>
            </div>
            <div class="cdp-years">
              <span
                v-for="y in yearRangeList"
                :key="y"
                class="cdp-year"
                :class="yearClasses(y)"
                @click="selectYear(y)"
              >
                {{ y }}
              </span>
            </div>
            <div class="cdp-footer">
              <span class="cdp-today-btn" @click="switchToDateView">
                <i class="iconfont icon-calendar-arrow-left"/> 返回
              </span>
            </div>
          </template>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Array, Date, null],
    default: null
  },
  type: {
    type: String,
    default: 'date',
    validator: (val) => ['date', 'daterange'].includes(val)
  },
  placeholder: {
    type: String,
    default: '请选择日期'
  },
  startPlaceholder: {
    type: String,
    default: '开始日期'
  },
  endPlaceholder: {
    type: String,
    default: '结束日期'
  },
  rangeSeparator: {
    type: String,
    default: '至'
  },
  valueFormat: {
    type: String,
    default: 'YYYY-MM-DD'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  clearable: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'large', 'small'].includes(val)
  },
  variant: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'search'].includes(val)
  },
  width: {
    type: [String, Number],
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

// ---------- 基础状态 ----------
const pickerRef = ref(null)
const isOpen = ref(false)
const isRange = computed(() => props.type === 'daterange')

// 当前视图年月（单日期 / 范围左侧面板）
const viewDate = ref(new Date())
const viewYear = computed(() => viewDate.value.getFullYear())
const viewMonth = computed(() => viewDate.value.getMonth())

// 范围右侧面板 = 左侧面板的下一个月
const rightViewDate = computed(() => {
  const d = new Date(viewDate.value)
  d.setMonth(d.getMonth() + 1)
  return d
})
const rightViewYear = computed(() => rightViewDate.value.getFullYear())
const rightViewMonth = computed(() => rightViewDate.value.getMonth())

// 范围选择状态
const rangeStart = ref(null) // 已确认的开始
const rangeEnd = ref(null) // 已确认的结束
const hoverDate = ref(null) // 悬停日期（用于预览范围）
const selecting = ref(false) // 是否正在选择（已选开始，等待结束）

// 面板视图模式：date | month | year
const panelViewMode = ref('date')
// 年份视图的中心锚点
const yearViewAnchor = ref(new Date().getFullYear())

const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const sizeClass = computed(() => props.size !== 'default' ? `cdp-size--${props.size}` : '')
const variantClass = computed(() => props.variant !== 'default' ? `cdp-variant--${props.variant}` : '')

const containerStyle = computed(() => {
  const style = {}
  if (props.width) style.width = props.width
  return style
})

// ---------- 值处理 ----------

function formatDate(date) {
  if (!date || isNaN(date.getTime())) return ''
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function parseDate(str) {
  if (!str) return null
  const d = new Date(str)
  if (isNaN(d.getTime())) return null
  return d
}

// 单日期显示值
const displaySingle = computed(() => {
  if (!props.modelValue) return ''
  if (props.modelValue instanceof Date) return formatDate(props.modelValue)
  return String(props.modelValue)
})

// 范围显示值
const displayStart = computed(() => {
  if (!props.modelValue || !Array.isArray(props.modelValue)) return ''
  return props.modelValue[0] || ''
})
const displayEnd = computed(() => {
  if (!props.modelValue || !Array.isArray(props.modelValue)) return ''
  return props.modelValue[1] || ''
})

const hasValue = computed(() => {
  if (isRange.value) {
    return Array.isArray(props.modelValue) && (props.modelValue[0] || props.modelValue[1])
  }
  return !!props.modelValue
})

// 同步外部值到内部状态
watch(() => props.modelValue, (val) => {
  if (isRange.value) {
    if (Array.isArray(val)) {
      rangeStart.value = parseDate(val[0])
      rangeEnd.value = parseDate(val[1])
    } else {
      rangeStart.value = null
      rangeEnd.value = null
    }
  }
}, { immediate: true })

// ---------- 日历计算 ----------

function getMonthCells(year, month) {
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const startWeekday = firstDay.getDay() // 0 = 周日
  const daysInMonth = lastDay.getDate()

  const cells = []

  // 上月填充
  const prevLastDay = new Date(year, month, 0).getDate()
  for (let i = startWeekday - 1; i >= 0; i--) {
    const d = prevLastDay - i
    cells.push({
      day: d,
      date: new Date(year, month - 1, d),
      currentMonth: false
    })
  }

  // 当月
  for (let d = 1; d <= daysInMonth; d++) {
    cells.push({
      day: d,
      date: new Date(year, month, d),
      currentMonth: true
    })
  }

  // 下月填充
  const remainder = cells.length % 7
  const nextFill = remainder === 0 ? 0 : 7 - remainder
  for (let d = 1; d <= nextFill; d++) {
    cells.push({
      day: d,
      date: new Date(year, month + 1, d),
      currentMonth: false
    })
  }

  return cells
}

const calendarCells = computed(() => getMonthCells(viewYear.value, viewMonth.value))
const leftCells = computed(() => getMonthCells(viewYear.value, viewMonth.value))
const rightCells = computed(() => getMonthCells(rightViewYear.value, rightViewMonth.value))

// ---------- 日期比较 ----------

function isSameDay(d1, d2) {
  if (!d1 || !d2) return false
  return d1.getFullYear() === d2.getFullYear() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getDate() === d2.getDate()
}

function isBefore(d1, d2) {
  const a = new Date(d1.getFullYear(), d1.getMonth(), d1.getDate())
  const b = new Date(d2.getFullYear(), d2.getMonth(), d2.getDate())
  return a.getTime() < b.getTime()
}

function isBetween(target, start, end) {
  if (!start || !end) return false
  const t = new Date(target.getFullYear(), target.getMonth(), target.getDate())
  const s = new Date(start.getFullYear(), start.getMonth(), start.getDate())
  const e = new Date(end.getFullYear(), end.getMonth(), end.getDate())
  return t.getTime() > s.getTime() && t.getTime() < e.getTime()
}

// ---------- 类名计算 ----------

const today = new Date()

function cellClasses(cell) {
  const classes = []
  if (!cell.currentMonth) classes.push('is-other-month')
  if (isSameDay(cell.date, today)) classes.push('is-today')

  const selected = parseDate(props.modelValue)
  if (selected && isSameDay(cell.date, selected)) classes.push('is-selected')

  return classes
}

function rangeCellClasses(cell) {
  const classes = []
  if (!cell.currentMonth) classes.push('is-other-month')
  if (isSameDay(cell.date, today)) classes.push('is-today')

  const start = rangeStart.value
  const end = rangeEnd.value
  const hover = hoverDate.value

  if (selecting.value && start) {
    if (!hover) {
      // 仅选中一个锚点，未 hover 其他日期：显示全圆角单选样式
      if (isSameDay(cell.date, start)) classes.push('is-selected')
    } else {
      // 已有 hover，进入范围预览模式
      const previewStart = isBefore(start, hover) ? start : hover
      const previewEnd = isBefore(start, hover) ? hover : start

      if (isSameDay(previewStart, previewEnd) && isSameDay(cell.date, hover)) classes.push('is-selected')
      else if (isSameDay(cell.date, previewStart)) classes.push('is-start')
      else if (isSameDay(cell.date, previewEnd)) classes.push('is-end-preview')
      else if (isBetween(cell.date, previewStart, previewEnd)) {
        classes.push('is-in-range')
      }
    }
  } else {
    // 已确认的范围
    if (start && isSameDay(cell.date, start)) classes.push('is-start')
    if (end && isSameDay(cell.date, end)) classes.push('is-end')
    if (start && end && isBetween(cell.date, start, end)) classes.push('is-in-range')
  }

  return classes
}

// ---------- 交互 ----------

function toggleOpen() {
  if (props.disabled) return
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    // 打开时，将视图定位到选中的日期附近
    panelViewMode.value = 'date'
    if (isRange.value) {
      const anchor = rangeStart.value || new Date()
      viewDate.value = new Date(anchor.getFullYear(), anchor.getMonth(), 1)
    } else {
      const anchor = parseDate(props.modelValue) || new Date()
      viewDate.value = new Date(anchor.getFullYear(), anchor.getMonth(), 1)
    }
  }
}

function prevMonth() {
  const d = new Date(viewDate.value)
  d.setMonth(d.getMonth() - 1)
  viewDate.value = d
}

function nextMonth() {
  const d = new Date(viewDate.value)
  d.setMonth(d.getMonth() + 1)
  viewDate.value = d
}

function selectSingle(cell) {
  if (!cell.currentMonth) {
    viewDate.value = new Date(cell.date.getFullYear(), cell.date.getMonth(), 1)
  }
  const formatted = formatDate(cell.date)
  emit('update:modelValue', formatted)
  emit('change', formatted)
  isOpen.value = false
}

function selectToday() {
  const d = new Date()
  const formatted = formatDate(d)
  emit('update:modelValue', formatted)
  emit('change', formatted)
  viewDate.value = new Date(d.getFullYear(), d.getMonth(), 1)
  isOpen.value = false
}

// ---------- 范围选择 ----------

function selectRange(cell) {
  if (!cell.currentMonth) {
    viewDate.value = new Date(cell.date.getFullYear(), cell.date.getMonth(), 1)
  }

  if (!selecting.value) {
    // 第一次点击：设置开始
    rangeStart.value = cell.date
    rangeEnd.value = null
    selecting.value = true
    hoverDate.value = null
    emit('update:modelValue', [formatDate(cell.date), ''])
  } else {
    // 第二次点击：设置结束
    let start = rangeStart.value
    let end = cell.date
    if (isBefore(end, start)) {
      [start, end] = [end, start]
    }
    rangeStart.value = start
    rangeEnd.value = end
    selecting.value = false
    hoverDate.value = null

    const startStr = formatDate(start)
    const endStr = formatDate(end)
    emit('update:modelValue', [startStr, endStr])
    emit('change', [startStr, endStr])
    isOpen.value = false
  }
}

function handleRangeHover(cell) {
  if (selecting.value) {
    hoverDate.value = cell.date
  }
}

function selectTodayRange() {
  const today = new Date()
  rangeStart.value = today
  rangeEnd.value = today
  selecting.value = false
  const str = formatDate(today)
  emit('update:modelValue', [str, str])
  emit('change', [str, str])
  isOpen.value = false
}

function selectRecent7Days() {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 6)
  rangeStart.value = start
  rangeEnd.value = end
  selecting.value = false
  const startStr = formatDate(start)
  const endStr = formatDate(end)
  emit('update:modelValue', [startStr, endStr])
  emit('change', [startStr, endStr])
  isOpen.value = false
}

function selectRecent30Days() {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 29)
  rangeStart.value = start
  rangeEnd.value = end
  selecting.value = false
  const startStr = formatDate(start)
  const endStr = formatDate(end)
  emit('update:modelValue', [startStr, endStr])
  emit('change', [startStr, endStr])
  isOpen.value = false
}

function handleClear() {
  if (isRange.value) {
    rangeStart.value = null
    rangeEnd.value = null
    selecting.value = false
    hoverDate.value = null
    emit('update:modelValue', ['', ''])
    emit('change', ['', ''])
  } else {
    emit('update:modelValue', '')
    emit('change', '')
  }
}

// ---------- 视图切换（年月快速选择） ----------

const yearRangeStart = computed(() => {
  return yearViewAnchor.value - 6
})

const yearRangeEnd = computed(() => yearRangeStart.value + 11)

const yearRangeList = computed(() => {
  const list = []
  for (let y = yearRangeStart.value; y <= yearRangeEnd.value; y++) {
    list.push(y)
  }
  return list
})

function switchToYearView() {
  yearViewAnchor.value = viewYear.value
  panelViewMode.value = 'year'
}

function switchToMonthView() {
  panelViewMode.value = 'month'
}

function switchToDateView() {
  panelViewMode.value = 'date'
}

function selectYear(year) {
  viewDate.value = new Date(year, viewMonth.value, 1)
  panelViewMode.value = 'month'
}

function selectMonth(month) {
  viewDate.value = new Date(viewYear.value, month, 1)
  panelViewMode.value = 'date'
}

function prevYearPage() {
  yearViewAnchor.value -= 12
}

function nextYearPage() {
  yearViewAnchor.value += 12
}

function prevYear() {
  const d = new Date(viewDate.value)
  d.setFullYear(d.getFullYear() - 1)
  viewDate.value = d
}

function nextYear() {
  const d = new Date(viewDate.value)
  d.setFullYear(d.getFullYear() + 1)
  viewDate.value = d
}

function monthClasses(m) {
  const classes = []
  if (m - 1 === viewMonth.value) classes.push('is-current')
  return classes
}

function yearClasses(y) {
  const classes = []
  if (y === viewYear.value) classes.push('is-current')
  return classes
}

// ---------- 点击外部关闭 ----------

function handleClickOutside(event) {
  // 如果目标元素已被移除（视图切换导致的 DOM 重建），忽略
  if (!document.contains(event.target)) return
  if (pickerRef.value && !pickerRef.value.contains(event.target)) {
    isOpen.value = false
    // 如果范围选择未完成，取消选择
    if (selecting.value) {
      selecting.value = false
      hoverDate.value = null
      if (!rangeEnd.value) {
        rangeStart.value = null
        emit('update:modelValue', ['', ''])
      }
    }
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped lang="scss">
/* =========================================================
 * CustomDatePicker — 项目风格日期选择组件
 * 替代 el-date-picker，完全可控的样式与交互
 * ========================================================= */

.custom-date-picker {
  position: relative;
  display: inline-flex;
  width: 180px;
  font-family: var(--el-font-family);
  vertical-align: middle;
  height: 38px;
}

.custom-date-picker.is-range {
  min-width: 300px;
}

/* ---------- 触发器 ---------- */
.cdp-trigger {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 0 var(--space-3);
  background-color: var(--card-bg);
  box-shadow: var(--shadow-input);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s ease;
  gap: var(--space-2);
}

.cdp-trigger:hover:not(.is-disabled) {
  box-shadow: var(--shadow-input-hover);
}

/* ---------- 搜索变体 ---------- */
.custom-date-picker.cdp-variant--search .cdp-trigger {
  box-shadow: 0 0 0 1px var(--gray-40) inset;
  border-color: var(--border-color);
}

.custom-date-picker.cdp-variant--search .cdp-trigger:hover:not(.is-disabled) {
  box-shadow: 0 0 0 1px var(--gray-40) inset;
}

.custom-date-picker.cdp-variant--search.is-open .cdp-trigger {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
  border-color: var(--primary-color);
}

.custom-date-picker.cdp-variant--search.is-open .cdp-trigger:hover:not(.is-disabled) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.custom-date-picker.is-open .cdp-trigger {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-input-focus);
}

.cdp-trigger.is-disabled {
  cursor: not-allowed;
  opacity: 0.65;
  background-color: var(--disabled-bg);
  border: var(--disabled-border);
  box-shadow: none;
}

.cdp-trigger-icon {
  font-size: 16px;
  color: var(--text-tertiary);
  flex-shrink: 0;
}

.cdp-trigger-icon--end {
  font-size: 14px;
  opacity: 0.7;
  margin-left: 2px;
}

.cdp-label {
  flex: 1;
  color: var(--text-primary);
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cdp-label.is-placeholder {
  color: var(--text-tertiary);
}

.cdp-separator {
  color: var(--text-tertiary);
  font-size: 13px;
  padding: 0 var(--space-1);
  flex-shrink: 0;
}

.cdp-arrow {
  font-size: 14px;
  color: var(--text-tertiary);
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.custom-date-picker.is-open .cdp-arrow {
  transform: rotate(180deg);
  color: var(--primary-color);
}

.cdp-clear {
  font-size: 12px;
  color: var(--text-tertiary);
  border-radius: 50%;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.cdp-clear:hover {
  background: var(--primary-light);
  color: var(--primary-dark);
}

/* ---------- 下拉面板 ---------- */
.cdp-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  z-index: 100;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  padding: 12px;
}

/* ---------- 日历 ---------- */
.cdp-calendar {
  width: 280px;
}

.cdp-calendar--range {
  width: auto;
  min-width: 280px;
}

.cdp-range-panels {
  display: flex;
  gap: 16px;
}

.cdp-range-panel {
  width: 280px;
}

/* ---------- 头部 ---------- */
.cdp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  padding: 0 4px;
}

.cdp-header-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.cdp-header-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 0;
}

.cdp-header-btn:hover {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.cdp-header-placeholder {
  width: 28px;
}

.cdp-header-year,
.cdp-header-month {
  cursor: pointer;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  transition: all 0.2s ease;
  user-select: none;
}

.cdp-header-year:hover,
.cdp-header-month:hover {
  color: var(--primary-color);
  background: var(--primary-light);
}

.cdp-header-divider {
  color: var(--text-tertiary);
  margin: 0 2px;
  pointer-events: none;
}

/* ---------- 星期 ---------- */
.cdp-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 4px;
}

.cdp-weekday {
  text-align: center;
  font-size: 12px;
  color: var(--text-tertiary);
  padding: 6px 0;
}

/* ---------- 日期网格 ---------- */
.cdp-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.cdp-day {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  font-size: 13px;
  color: var(--text-primary);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.15s ease;
  user-select: none;
}

.cdp-day:hover {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.cdp-day.is-other-month {
  color: var(--text-tertiary);
  opacity: 0.6;
}

.cdp-day.is-today {
  font-weight: 600;
  color: var(--primary-color);
}

.cdp-day.is-selected {
  background: var(--primary-color);
  color: var(--text-inverse);
  font-weight: 500;
}

/* 范围选择样式 */
.cdp-day.is-start,
.cdp-day.is-end,
.cdp-day.is-end-preview {
  background: var(--primary-color);
  color: var(--text-inverse);
  font-weight: 500;
}

.cdp-day.is-in-range {
  background: var(--primary-light);
  color: var(--primary-dark);
  border-radius: 0;
}

.cdp-day.is-start {
  border-radius: var(--radius-sm) 0 0 var(--radius-sm);
}

.cdp-day.is-end,
.cdp-day.is-end-preview {
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
}

.cdp-day.is-start.is-end,
.cdp-day.is-start.is-end-preview {
  border-radius: var(--radius-sm);
}

/* ---------- 底部 ---------- */
.cdp-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border-color);
}

.cdp-footer--range {
  justify-content: space-around;
  gap: var(--space-2);
}

.cdp-today-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--primary-color);
  cursor: pointer;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  transition: all 0.2s ease;
  user-select: none;
}

.cdp-today-btn:hover {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.cdp-today-btn i {
  font-size: 14px;
}

/* ---------- 月份 / 年份 选择网格 ---------- */
.cdp-months,
.cdp-years {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding: 8px 4px;
}

.cdp-month,
.cdp-year {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  font-size: 14px;
  color: var(--text-primary);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.15s ease;
  user-select: none;
}

.cdp-month:hover,
.cdp-year:hover {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.cdp-month.is-current,
.cdp-year.is-current {
  background: var(--primary-color);
  color: var(--text-inverse);
  font-weight: 500;
}

/* ---------- 尺寸系统 ---------- */

/* default: 触发器高度 36px */
.cdp-size--default {
  height: 36px;
}

/* large */
.cdp-size--large {
  height: 42px;
  width: 200px;
}

.cdp-size--large.is-range {
  width: 300px;
}

.cdp-size--large .cdp-trigger {
  padding: 0 var(--space-4);
  font-size: 16px;
}

.cdp-size--large .cdp-trigger-icon {
  font-size: 18px;
}

.cdp-size--large .cdp-label {
  font-size: 16px;
}

/* small */
.cdp-size--small {
  height: 28px;
  width: 150px;
}

.cdp-size--small.is-range {
  width: 220px;
}

.cdp-size--small .cdp-trigger {
  padding: 0 var(--space-2);
}

.cdp-size--small .cdp-trigger-icon {
  font-size: 14px;
}

.cdp-size--small .cdp-label {
  font-size: 12px;
}

.cdp-size--small .cdp-arrow,
.cdp-size--small .cdp-clear {
  font-size: 11px;
}

/* ---------- 下拉动画 ---------- */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
