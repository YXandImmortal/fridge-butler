<template>
  <div class="attach-container">
    <!-- 已引用标签栏 -->
    <transition name="el-zoom-in-center">
      <transition-group
        v-if="modelValue.length > 0"
        name="el-zoom-in-center"
        tag="div"
        class="attachments-bar"
      >
        <span
          v-for="att in modelValue"
          :key="att.type + '-' + att.id"
          class="attach-tag"
        >
          <i class="iconfont" :class="att.type === 'fridge' ? 'icon-fridge-line' : 'icon-inbox'" />
          {{ att.name }}
          <i class="iconfont icon-close attach-tag-close" @click="removeAttachment(att)" />
        </span>
      </transition-group>
    </transition>

    <!-- 附件按钮 -->
    <el-popover
      v-model:visible="popoverVisible"
      placement="top-end"
      :width="280"
      trigger="click"
      popper-class="attach-popover"
      popper-style="padding: 0;"
    >
      <template #reference>
        <button
          class="attach-btn"
          :class="{ 'attach-btn-active': modelValue.length > 0 }"
          title="添加附件"
        >
          <i class="iconfont icon-attachment" />
          <span v-if="modelValue.length > 0" class="attach-badge">{{ modelValue.length }}</span>
        </button>
      </template>

      <el-scrollbar
        max-height="320px"
        class="attach-popover-content"
        view-style="padding: var(--space-3);"
      >
        <div class="attach-section">
          <div class="attach-section-title">
            <i class="iconfont icon-fridge-line" />
            <span>选择冰箱</span>
          </div>
          <div v-if="fridgeList.length === 0" class="attach-empty">暂无冰箱</div>
          <div v-else class="attach-option-list">
            <button
              v-for="fridge in fridgeList"
              :key="fridge.id"
              class="attach-option"
              :class="{ 'attach-option-selected': isAttached('fridge', fridge.id) }"
              @click="toggleAttachment('fridge', fridge)"
            >
              <span class="attach-option-name">{{ fridge.fridgeName }}</span>
              <i v-if="isAttached('fridge', fridge.id)" class="iconfont icon-check" />
            </button>
          </div>
        </div>

        <div class="attach-divider" />

        <div class="attach-section">
          <div class="attach-section-title">
            <i class="iconfont icon-inbox" />
            <span>选择物品</span>
          </div>
          <div v-if="itemList.length === 0" class="attach-empty">暂无物品</div>
          <div v-else class="attach-option-list">
            <button
              v-for="item in itemList.slice(0, 20)"
              :key="item.id"
              class="attach-option"
              :class="{ 'attach-option-selected': isAttached('item', item.id) }"
              @click="toggleAttachment('item', item)"
            >
              <span class="attach-option-name">{{ item.itemName }}</span>
              <span class="attach-option-meta">{{ item.fridgeName }}</span>
              <i v-if="isAttached('item', item.id)" class="iconfont icon-check" />
            </button>
          </div>
          <div v-if="itemList.length > 20" class="attach-more-tip">仅展示前 20 个物品</div>
        </div>
      </el-scrollbar>
    </el-popover>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  fridgeList: {
    type: Array,
    default: () => []
  },
  itemList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const popoverVisible = ref(false)

function isAttached(type, id) {
  return props.modelValue.some(a => a.type === type && a.id === id)
}

function toggleAttachment(type, data) {
  const existingIndex = props.modelValue.findIndex(a => a.type === type && a.id === data.id)
  const next = [...props.modelValue]

  if (existingIndex !== -1) {
    next.splice(existingIndex, 1)
    emit('update:modelValue', next)
    return
  }

  if (type === 'fridge') {
    next.push({
      type: 'fridge',
      id: data.id,
      name: data.fridgeName
    })
  } else if (type === 'item') {
    next.push({
      type: 'item',
      id: data.id,
      name: data.itemName,
      fridgeId: data.fridgeId || null,
      fridgeName: data.fridgeName || null
    })
  }
  emit('update:modelValue', next)
}

function removeAttachment(att) {
  const idx = props.modelValue.findIndex(a => a.type === att.type && a.id === att.id)
  if (idx !== -1) {
    const next = [...props.modelValue]
    next.splice(idx, 1)
    emit('update:modelValue', next)
  }
}
</script>

<style scoped lang="scss">
.attach-container {
  margin-left: auto;
  display: flex;
  flex-direction: row;
  gap: var(--space-3);
  align-items: center;
  justify-content: center;
}

/* 附件按钮 */
.attach-btn {
  position: relative;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  background: var(--input-bg);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover {
    background: var(--primary-light);
    color: var(--primary-dark);
  }

  &.attach-btn-active {
    background: var(--primary-light);
    color: var(--primary-color);
  }

  .iconfont {
    font-size: 18px;
  }
}

.attach-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: var(--danger-color);
  color: white;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 已引用标签栏 */
.attachments-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
}

.attach-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 12px;
  background: var(--primary-light);
  color: var(--primary-dark);
  font-size: 12px;
  font-weight: 500;
  border: 1px solid var(--primary-30);
  transition: all 0.2s ease;

  .iconfont {
    font-size: 12px;
  }

  .attach-tag-close {
    cursor: pointer;
    opacity: 0.7;
    transition: opacity 0.2s ease;

    &:hover {
      opacity: 1;
    }
  }
}

/* Popover 内容 */
:deep(.attach-popover) {
  .el-popover__title {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: var(--space-3);
  }
}

.attach-section {
  margin-bottom: var(--space-3);

  &:last-child {
    margin-bottom: 0;
  }
}

.attach-section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: var(--space-2);

  .iconfont {
    font-size: 14px;
    color: var(--primary-color);
  }
}

.attach-empty {
  font-size: 12px;
  color: var(--text-tertiary);
  padding: var(--space-2) 0;
  text-align: center;
}

.attach-option-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.attach-option {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  background: transparent;
  color: var(--text-primary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  width: 100%;

  &:hover {
    background: var(--primary-10);
    border-color: var(--primary-30);
  }

  &.attach-option-selected {
    background: var(--primary-light);
    border-color: var(--primary-color);
    color: var(--primary-dark);
    font-weight: 500;
  }

  .icon-check {
    margin-left: auto;
    font-size: 14px;
    color: var(--primary-color);
  }
}

.attach-option-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attach-option-meta {
  font-size: 11px;
  color: var(--text-tertiary);
  flex-shrink: 0;
}

.attach-divider {
  height: 1px;
  background: var(--border-color);
  margin: var(--space-3) 0;
}

.attach-more-tip {
  font-size: 11px;
  color: var(--text-tertiary);
  text-align: center;
  padding-top: var(--space-1);
}

/* Popover 滚动条 */
:deep(.attach-popover-content::-webkit-scrollbar) {
  width: 4px;
}

:deep(.attach-popover-content::-webkit-scrollbar-track) {
  background: transparent;
}

:deep(.attach-popover-content::-webkit-scrollbar-thumb) {
  background: var(--primary-20);
  border-radius: 2px;
}

/* 响应式 */
@media (max-width: 768px) {
  .attachments-bar {
    padding: var(--space-2) var(--space-4) 0;
  }

  .attach-btn {
    width: 28px;
    height: 28px;

    .iconfont {
      font-size: 16px;
    }
  }
}
</style>
