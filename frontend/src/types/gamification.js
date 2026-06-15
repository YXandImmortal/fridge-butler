/**
 * 成就与游戏化系统类型定义（JSDoc）
 * 本文件仅用于 IDE 提示与文档，运行时无实际导出行为
 */

/**
 * @typedef {Object} LevelIconVO
 * @property {string} [snowman] - 雪人图标类名
 * @property {string} [iceCream] - 冰淇淋图标类名
 * @property {string} [ice] - 冰块图标类名
 * @property {string} [snowflake] - 雪花图标类名
 */

/**
 * @typedef {Object} StreakInfoVO
 * @property {number} currentStreak - 当前冰鲜连续天数
 * @property {number} maxStreak - 历史最高冰鲜连续天数
 * @property {number} protectRemaining - 本月剩余保护次数
 * @property {number} protectTotal - 本月保护次数上限
 * @property {boolean} protectAutoEnabled - 是否开启自动保护
 * @property {boolean} meltWarning - 是否触发融化警告
 *
 * @typedef {Object} OverviewVO
 * @property {LevelInfoVO} level - 等级信息
 * @property {StreakInfoVO} streak - 冰鲜连续天数信息
 * @property {BadgeVO[]} badges - 徽章墙列表
 * @property {number} todayExp - 今日已获得 EXP
 * @property {number} todayExpLimit - 今日 EXP 上限
 * @property {number} freshnessScore - 实时保鲜评分（0-100，无有效食材时为 -1）
 * @property {string} scoreGrade - 评分等级 S/A/B/C/D，无数据时为 "-"
 * @property {FreshnessDimensionVO[]} [freshnessDimensions] - 四维评分明细，无数据时各维度 score 为 0
 * @property {HeatmapDayVO[]} [heatmap] - 热力图缩略数据
 * @property {AchievementSettingsVO} settings - 成就设置
 */

/**
 * @typedef {Object} BadgeVO
 * @property {string} code - 徽章唯一编码
 * @property {string} name - 徽章名称
 * @property {string} iconClass - iconfont 图标类名
 * @property {string} description - 徽章描述
 * @property {boolean} unlocked - 是否已解锁
 * @property {string} [unlockedAt] - 解锁时间（ISO 8601）
 * @property {number} expReward - 解锁奖励 EXP
 * @property {string} unlockConditionDesc - 解锁条件说明
 */

/**
 * @typedef {Object} ExpLogRecordVO
 * @property {number} id - 日志 ID
 * @property {string} actionType - 行为类型枚举值
 * @property {string} actionDesc - 行为描述
 * @property {number} expGained - 获得 EXP
 * @property {number} expBalance - 操作后 EXP 余额
 * @property {string} createdAt - 创建时间（ISO 8601）
 */

/**
 * @typedef {Object} ExpLogPageVO
 * @property {ExpLogRecordVO[]} records - 日志记录列表
 * @property {number} total - 总记录数
 * @property {number} page - 当前页码
 * @property {number} size - 每页大小
 * @property {number} pages - 总页数
 */

/**
 * @typedef {Object} FreshnessDimensionVO
 * @property {number} score - 单项得分
 * @property {string} label - 维度名称
 */

/**
 * @typedef {Object} LevelInfoVO
 * @property {number} currentLevel - 当前等级
 * @property {string} title - 当前称号
 * @property {number} currentExp - 当前等级 EXP
 * @property {number} requiredExp - 升级所需 EXP
 * @property {number} totalExp - 累计 EXP
 * @property {LevelIconVO} icons - 等级图标数量集合
 * @property {number} progressPercent - 进度百分比
 * @property {NextLevelInfoVO} [nextLevel] - 下一级预览信息
 */

/**
 * @typedef {Object} NextLevelInfoVO
 * @property {number} level - 下一等级
 * @property {string} title - 下一等级称号
 * @property {number} requiredExp - 升级所需 EXP
 * @property {LevelIconVO} icons - 下一等级图标数量集合
 */

/**
 * @typedef {Object} FreshnessScoreVO
 * @property {number} score - 总分（0-100，无有效食材时为 -1）
 * @property {string} grade - 等级 S/A/B/C/D，无数据时为 "-"
 * @property {FreshnessDimensionVO[]} dimensions - 四维评分明细，无数据时各维度 score 为 0
 * @property {string} [evaluateAt] - 评分时间（ISO 8601）
 */

/**
 * @typedef {Object} HeatmapDayVO
 * @property {string} date - 日期（yyyy-MM-dd）
 * @property {number} score - 当日保鲜评分（-1 表示无数据）
 * @property {string} grade - 当日等级 S/A/B/C/D，无数据时为 "-"
 * @property {boolean} hasExpired - 当日是否有过期食材
 */

/**
 * @typedef {Object} MonthlyReportVO
 * @property {string} yearMonth - 年月（yyyy-MM）
 * @property {number} avgScore - 月平均保鲜评分
 * @property {number} maxScore - 最高评分
 * @property {number} minScore - 最低评分
 * @property {number} expiredCount - 过期食材数量
 * @property {number} nearExpiredCount - 临期食材数量
 * @property {number} wastedAmount - 浪费金额估算
 * @property {number} ecoValue - 环保价值估算
 * @property {number} itemInCount - 入库次数
 * @property {number} itemOutCount - 取出次数
 * @property {BadgeVO[]} [newBadges] - 本月新获得徽章
 * @property {string} [levelChange] - 等级变化描述
 * @property {number} [maxStreak] - 本月最高连续天数
 * @property {boolean} viewed - 是否已查看
 */

/**
 * @typedef {Object} AchievementSettingsVO
 * @property {boolean} panelHidden - 是否隐藏成就面板
 * @property {boolean} autoStreakProtect - 是否自动保护冰鲜连续天数
 * @property {boolean} streakProtectNotify - 保护时是否发送通知
 */

/**
 * @typedef {Object} ApiResponse
 * @property {number} code - 业务状态码
 * @property {string} message - 提示信息
 * @property {OverviewVO|BadgeVO[]|ExpLogPageVO|FreshnessScoreVO|HeatmapDayVO[]|MonthlyReportVO|AchievementSettingsVO|null} data - 响应数据
 */

export {}
