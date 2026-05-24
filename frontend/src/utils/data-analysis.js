/**
 * 数据中心 — 数据聚合与统计工具函数
 */

/**
 * 计算物品新鲜度状态
 * 复用 ItemManageView.vue 中的逻辑
 * @param {Object} item - 物品对象
 * @returns {Object} { label, type, remainingDays, percent }
 */
export function getFreshnessStatus(item) {
  if (item.shelfLifeDays > 30) {
    return { label: '长保质期', type: 'info', remainingDays: null, percent: null }
  }

  if (!item.productionDate || !item.shelfLifeDays) {
    return { label: '-', type: 'info', remainingDays: null, percent: null }
  }

  const productionDate = new Date(item.productionDate)
  const now = new Date()
  const diffTime = now - productionDate
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))

  const remainingDays = item.shelfLifeDays - diffDays
  const R = (remainingDays / item.shelfLifeDays) * 100

  if (R <= 0) {
    return { label: '已过期', type: 'danger', remainingDays, percent: 0 }
  } else if (R < 20) {
    return { label: '临期', type: 'warning', remainingDays, percent: R }
  } else if (R < 50) {
    return { label: '一般', type: 'primary', remainingDays, percent: R }
  } else {
    return { label: '新鲜', type: 'success', remainingDays, percent: R }
  }
}

/**
 * 统计概览指标
 * @param {Array} fridges - 冰箱列表
 * @param {Array} items - 物品列表
 */
export function computeOverviewStats(fridges, items) {
  const fridgeCount = fridges.length
  const totalItems = items.length

  // 临期预警：临期 + 已过期
  let expiringCount = 0
  items.forEach(item => {
    const status = getFreshnessStatus(item)
    if (status.label === '临期' || status.label === '已过期') {
      expiringCount++
    }
  })

  // 容量利用率不再由前端计算，统一由后端 /fridge/capacity-stats 接口提供
  return {
    fridgeCount,
    totalItems,
    expiringCount,
    capacityRate: 0
  }
}

/**
 * 按冰箱聚合物品数量
 * @param {Array} fridges - 冰箱列表
 * @returns {Array} [{ name, value, itemCount, totalCapacity, status }]
 */
export function aggregateByFridge(fridges) {
  return fridges.map(f => ({
    name: f.fridgeName || '未命名',
    value: f.itemCount || 0,
    totalCapacity: f.totalCapacity, // 保留原始值，用于判断容量是否已设置
    status: f.status
  }))
}

/**
 * 按分类聚合物品数量
 * @param {Array} items - 物品列表
 * @returns {Array} [{ name, value }]
 */
export function aggregateByCategory(items) {
  const map = new Map()
  items.forEach(item => {
    const key = item.categoryName || '未分类'
    map.set(key, (map.get(key) || 0) + 1)
  })
  return Array.from(map.entries()).map(([name, value]) => ({ name, value }))
}

/**
 * 新鲜度状态分布统计
 * @param {Array} items - 物品列表
 * @returns {Array} [{ name, value, type }]
 */
export function aggregateFreshness(items) {
  const counts = { '新鲜': 0, '一般': 0, '临期': 0, '已过期': 0, '长保质期': 0, '-': 0 }
  items.forEach(item => {
    const status = getFreshnessStatus(item)
    counts[status.label] = (counts[status.label] || 0) + 1
  })

  return [
    { name: '新鲜', value: counts['新鲜'], type: 'success' },
    { name: '一般', value: counts['一般'], type: 'primary' },
    { name: '临期', value: counts['临期'], type: 'warning' },
    { name: '已过期', value: counts['已过期'], type: 'danger' },
    { name: '长保质期', value: counts['长保质期'], type: 'info' }
  ].filter(d => d.value > 0)
}

/**
 * 近30天入库趋势
 * @param {Array} items - 物品列表
 * @returns {Object} { dates: [], counts: [] }
 */
export function computeInboundTrend(items) {
  const now = new Date()
  const dates = []
  const counts = []

  for (let i = 29; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(d.getDate() - i)
    const dateStr = d.toISOString().split('T')[0]
    dates.push(dateStr)

    const dayCount = items.filter(item => {
      if (!item.storedDate) return false
      const stored = new Date(item.storedDate)
      return stored.toISOString().split('T')[0] === dateStr
    }).length

    counts.push(dayCount)
  }

  return { dates, counts }
}

/**
 * 保质期剩余天数分布
 * @param {Array} items - 物品列表
 * @returns {Array} [{ name, value }]
 */
export function aggregateShelfLifeDistribution(items) {
  const ranges = [
    { name: '已过期', min: -Infinity, max: 0 },
    { name: '1~3天', min: 1, max: 3 },
    { name: '4~7天', min: 4, max: 7 },
    { name: '8~15天', min: 8, max: 15 },
    { name: '16~30天', min: 16, max: 30 },
    { name: '>30天', min: 31, max: Infinity }
  ]

  const counts = ranges.map(r => ({ ...r, value: 0 }))

  items.forEach(item => {
    const status = getFreshnessStatus(item)
    if (status.remainingDays === null) {
      if (item.shelfLifeDays > 30) {
        const range = counts.find(r => r.name === '>30天')
        if (range) range.value++
      }
      return
    }

    for (const range of counts) {
      if (status.remainingDays >= range.min && status.remainingDays <= range.max) {
        range.value++
        break
      }
    }
  })

  return counts.map(r => ({ name: r.name, value: r.value }))
}

/**
 * 获取临期/过期物品列表（用于预警表格）
 * @param {Array} items - 物品列表
 * @param {Array} fridges - 冰箱列表（用于关联冰箱名称）
 * @returns {Array} 按剩余天数升序排列的物品
 */
export function getExpiringItems(items, fridges = []) {
  const fridgeMap = new Map(fridges.map(f => [f.id, f.fridgeName]))

  return items
    .map(item => {
      const status = getFreshnessStatus(item)
      return {
        ...item,
        fridgeName: fridgeMap.get(item.fridgeId) || '未知冰箱',
        freshnessLabel: status.label,
        freshnessType: status.type,
        remainingDays: status.remainingDays ?? Infinity
      }
    })
    .filter(item => item.freshnessLabel === '临期' || item.freshnessLabel === '已过期')
    .sort((a, b) => (a.remainingDays ?? Infinity) - (b.remainingDays ?? Infinity))
}

/**
 * ECharts 主题颜色配置
 * 根据当前 light/dark 主题返回对应颜色
 */
export function getChartThemeColors(isDark = false) {
  return {
    textColor: isDark ? '#e2e8f0' : '#2d3748',
    subTextColor: isDark ? '#a0aec0' : '#606266',
    axisLineColor: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)',
    splitLineColor: isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)',
    tooltipBg: isDark ? 'rgba(30,41,59,0.95)' : 'rgba(255,255,255,0.95)',
    tooltipBorder: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.06)',
    colors: ['#64B5F6', '#81D4FA', '#81C784', '#FFB74D', '#B39DDB', '#F48FB1', '#80DEEA', '#4FC3F7', '#FFA726', '#9575CD']
  }
}
