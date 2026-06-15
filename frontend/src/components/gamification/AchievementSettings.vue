<template>
  <div class="achievement-settings">
    <div v-if="loading" class="section-loading">
      <el-skeleton :rows="5" animated/>
    </div>
    <div v-else-if="!settings" class="empty-state">
      <i class="iconfont icon-settings"/>
      <span>暂无成就设置数据</span>
    </div>
    <div v-else class="settings-list">
      <div
          v-for="item in settingItems"
          :key="item.key"
          class="setting-item"
      >
        <div class="setting-info">
          <div class="setting-title">
            <i class="iconfont" :class="item.icon"/>
            {{ item.title }}
          </div>
          <div class="setting-desc">{{ item.desc }}</div>
        </div>
        <CustomSwitch
            :model-value="settings[item.key]"
            :active-value="true"
            :inactive-value="false"
            :loading="updatingKey === item.key"
            inline-prompt
            active-text="开"
            inactive-text="关"
            @change="(val) => handleToggle(item.key, val)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useGamificationStore} from '@/stores/gamification'
import CustomSwitch from '@/components/ui/CustomSwitch.vue'
import showMessage from '@/utils/message'

const gamificationStore = useGamificationStore()

const updatingKey = ref('')

const settings = computed(() => gamificationStore.settings)
const loading = computed(() => gamificationStore.isSettingsLoading)

const settingItems = [
  {
    key: 'panelHidden',
    title: '隐藏成就面板',
    desc: '开启后，个人中心顶部将不显示成就总览卡片',
    icon: 'icon-hidden'
  },
  {
    key: 'autoStreakProtect',
    title: '自动保护冰鲜连续天数',
    desc: '当月有保护次数时，若某天未登录将自动消耗一次保护机会维持连续天数',
    icon: 'icon-a-SheriffBadge'
  },
  {
    key: 'streakProtectNotify',
    title: '保护触发通知',
    desc: '自动保护冰鲜连续天数时，发送系统通知提醒',
    icon: 'icon-notification'
  }
]

onMounted(() => {
  if (!settings.value) {
    gamificationStore.fetchSettings()
  }
})

const handleToggle = async (key, val) => {
  if (updatingKey.value) return
  updatingKey.value = key
  try {
    const res = await gamificationStore.updateSettings({[key]: val})
    if (res?.code === 200) {
      showMessage.success('设置已保存')
    } else {
      showMessage.error(res?.message || '设置保存失败')
    }
  } catch (error) {
    showMessage.error('设置保存失败')
    console.error('更新成就设置失败:', error)
  } finally {
    updatingKey.value = ''
  }
}
</script>

<style scoped lang="scss">
.achievement-settings {
  min-height: 200px;
}

.section-loading {
  padding: var(--space-4) 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  padding: var(--space-12) 0;
  color: var(--text-tertiary);
  font-size: 14px;

  .iconfont {
    font-size: 48px;
    color: var(--text-secondary);
  }
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all 0.3s ease;

  &:hover {
    background: var(--input-bg);
    transform: translateY(-1px);
    box-shadow: var(--shadow-sm);
  }
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.setting-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);

  .iconfont {
    font-size: 18px;
    color: var(--primary-color);
  }
}

.setting-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

@media (max-width: 768px) {
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4);
  }
}
</style>
