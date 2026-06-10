<template>
  <div
    class="custom-input"
    :class="[
      sizeClass,
      variantClass,
      { 'is-focused': isFocused, 'is-disabled': disabled, 'is-error': error }
    ]"
    :style="containerStyle"
  >
    <!-- text / password 模式 -->
    <template v-if="!isTextarea">
      <div class="ci-wrapper">
        <!-- 前缀区域 -->
        <div v-if="icon || $slots.prefix" class="ci-prefix">
          <i v-if="icon" class="iconfont" :class="icon" />
          <slot name="prefix" />
        </div>

        <!-- 输入框 -->
        <input
          ref="inputRef"
          :type="inputType"
          :value="modelValue"
          :placeholder="placeholder"
          :disabled="disabled"
          :maxlength="maxlength"
          :autocomplete="autocomplete"
          class="ci-input"
          @input="handleInput"
          @focus="handleFocus"
          @blur="handleBlur"
          @keyup="handleKeyup"
          @keydown="handleKeydown"
        />

        <!-- 后缀区域 -->
        <div v-if="showClear || showPasswordToggle || $slots.suffix" class="ci-suffix">
          <i
            v-if="showClear"
            class="iconfont icon-close ci-icon-btn ci-clear-btn"
            @mousedown.prevent="handleClear"
          />
          <i
            v-if="showPasswordToggle"
            class="iconfont ci-icon-btn ci-password-btn"
            :class="passwordVisible ? 'icon-visible' : 'icon-hidden'"
            @mousedown.prevent="togglePassword"
          />
          <slot name="suffix" />
        </div>
      </div>
    </template>

    <!-- textarea 模式 -->
    <template v-else>
      <div class="ci-textarea-wrapper">
        <textarea
          ref="textareaRef"
          :value="modelValue"
          :placeholder="placeholder"
          :disabled="disabled"
          :maxlength="maxlength"
          :rows="rows"
          class="ci-textarea"
          @input="handleInput"
          @focus="handleFocus"
          @blur="handleBlur"
          @keyup="handleKeyup"
          @keydown="handleKeydown"
        />
        <div v-if="showWordLimit" class="ci-textarea-footer">
          <span class="ci-word-limit">{{ currentLength }}/{{ maxlength }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useFormItem } from 'element-plus'

const props = defineProps({
  modelValue: { default: '' },
  placeholder: { type: String, default: '' },
  icon: { type: String, default: '' },
  type: {
    type: String,
    default: 'text',
    validator: (val) => {
      const valid = ['text', 'password', 'textarea'].includes(val)
      if (!valid) {
        console.warn(
          `[CustomInput] prop "type" must be one of ['text', 'password', 'textarea'], got "${val}"`
        )
      }
      return valid
    }
  },
  showPassword: { type: Boolean, default: false },
  clearable: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  width: { type: [String, Number], default: '' },
  height: { type: [String, Number], default: '' },
  variant: {
    type: String,
    default: 'default',
    validator: (val) => {
      const valid = ['default', 'search'].includes(val)
      if (!valid) {
        console.warn(
          `[CustomInput] prop "variant" must be one of ['default', 'search'], got "${val}"`
        )
      }
      return valid
    }
  },
  maxlength: { type: Number, default: undefined },
  showWordLimit: { type: Boolean, default: false },
  rows: { type: Number, default: 2 },
  size: {
    type: String,
    default: 'default',
    validator: (val) => {
      const valid = ['default', 'large', 'small'].includes(val)
      if (!valid) {
        console.warn(
          `[CustomInput] prop "size" must be one of ['default', 'large', 'small'], got "${val}"`
        )
      }
      return valid
    }
  },
  error: { type: Boolean, default: false },
  validateEvent: { type: Boolean, default: true },
  autocomplete: { type: String, default: '' }
})

const emit = defineEmits([
  'update:modelValue',
  'focus',
  'blur',
  'change',
  'clear',
  'keyup',
  'keydown'
])

const { formItem: elFormItem } = useFormItem()

const inputRef = ref(null)
const textareaRef = ref(null)
const isFocused = ref(false)
const passwordVisible = ref(false)

const isTextarea = computed(() => props.type === 'textarea')

const inputType = computed(() => {
  if (props.type !== 'password') return props.type
  return passwordVisible.value ? 'text' : 'password'
})

const sizeClass = computed(() => `ci-size--${props.size}`)

const variantClass = computed(() => {
  if (isTextarea.value) return 'ci-variant--textarea'
  if (props.variant === 'search') return 'ci-variant--search'
  return 'ci-variant--default'
})

const containerStyle = computed(() => {
  const style = {}
  if (props.width) style.width = props.width
  if (props.height && !isTextarea.value) {
    style['--custom-input-height'] = props.height
  }
  return style
})

const showClear = computed(
  () => props.clearable && props.modelValue && !props.disabled && isFocused.value
)

const showPasswordToggle = computed(
  () => props.showPassword && props.type === 'password' && !props.disabled
)

const currentLength = computed(() => String(props.modelValue || '').length)

/* ---------- 事件处理 ---------- */

function handleInput(e) {
  const value = e.target.value
  if (value !== props.modelValue) {
    emit('update:modelValue', value)
    emit('change', value)
    if (props.validateEvent) {
      elFormItem?.validate?.('change').catch(() => {})
    }
  }
}

function handleFocus(e) {
  isFocused.value = true
  emit('focus', e)
}

function handleBlur(e) {
  isFocused.value = false
  emit('blur', e)
  if (props.validateEvent) {
    elFormItem?.validate?.('blur').catch(() => {})
  }
}

function handleClear() {
  emit('update:modelValue', '')
  emit('change', '')
  emit('clear')
  // 保持焦点在输入框内，提升交互体验
  nextTick(() => {
    inputRef.value?.focus()
  })
}

function togglePassword() {
  passwordVisible.value = !passwordVisible.value
}

function handleKeyup(e) {
  emit('keyup', e)
}

function handleKeydown(e) {
  emit('keydown', e)
}

/* ---------- 暴露方法 ---------- */

defineExpose({
  focus: () => {
    inputRef.value?.focus()
    textareaRef.value?.focus()
  },
  blur: () => {
    inputRef.value?.blur()
    textareaRef.value?.blur()
  },
  select: () => inputRef.value?.select(),
  input: inputRef,
  textarea: textareaRef
})
</script>

<style scoped lang="scss">
/* =========================================================
 * CustomInput — 项目风格输入组件
 * 替代 EnhancedInput/el-input，完全可控的样式与交互
 * ========================================================= */

/* ---------- 根容器（统一外观） ---------- */
.custom-input {
  display: inline-flex;
  align-items: stretch;
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-input);
  transition: all 0.3s ease;
  overflow: hidden;
  font-family: var(--el-font-family);
  vertical-align: middle;
  width: 100%;
}

.custom-input:hover:not(.is-disabled) {
  box-shadow: var(--shadow-input-hover);
  border-color: var(--border-color);
}

.custom-input.is-focused:not(.is-disabled) {
  box-shadow: var(--shadow-input-focus);
  border-color: var(--primary-color);
}

/* ---------- text / password 内部布局 ---------- */
.ci-wrapper {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.ci-input {
  flex: 1 1 auto;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-primary);
  font-family: inherit;
  padding: 0 var(--space-3);
  height: var(--custom-input-height, 36px);
  font-size: 14px;
  line-height: 1.5;

  &::placeholder {
    color: var(--text-tertiary);
    font-weight: 200;
  }

  &:disabled {
    cursor: not-allowed;
    color: var(--disabled-text);
  }

  /* 隐藏浏览器原生的密码显示按钮，避免与组件自定义按钮重叠 */
  &::-ms-reveal {
    display: none;
  }

  &::-webkit-textfield-decoration-container {
    display: none;
  }
}

/* ---------- textarea 内部布局 ---------- */
.ci-textarea-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.ci-textarea {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-primary);
  font-family: inherit;
  padding: var(--space-3);
  font-size: 14px;
  line-height: 1.5;
  resize: none;

  &::placeholder {
    color: var(--text-tertiary);
    font-weight: 200;
  }

  &:disabled {
    cursor: not-allowed;
    color: var(--disabled-text);
  }
}

.ci-textarea-footer {
  display: flex;
  justify-content: flex-end;
  padding: 0 var(--space-3) var(--space-1);
  flex-shrink: 0;
}

.ci-word-limit {
  font-size: 12px;
  color: var(--text-tertiary);
}

/* ---------- 搜索变体 ---------- */
.custom-input.ci-variant--search {
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  border-right: none;
  box-shadow: 0 0 0 1px var(--gray-40) inset;
}

.custom-input.ci-variant--search:hover:not(.is-disabled) {
  box-shadow: 0 0 0 1px var(--gray-40) inset;
}

.custom-input.ci-variant--search.is-focused:not(.is-disabled) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
  border-color: var(--primary-color);
}

/* ---------- 前缀 / 后缀 ---------- */
.ci-prefix {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  padding-left: var(--space-3);
  color: var(--text-tertiary);
  gap: var(--space-1);

  .iconfont {
    font-size: var(--space-5);
  }
}

.ci-suffix {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  padding-right: var(--space-3);
  gap: var(--space-1);
  color: var(--text-tertiary);
}

/* ---------- 图标按钮（清空 / 密码切换） ---------- */
.ci-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  color: var(--text-tertiary);
  user-select: none;

  &:hover {
    background: var(--primary-light);
    color: var(--primary-dark);
  }
}

/* ---------- 禁用状态 ---------- */
.custom-input.is-disabled {
  opacity: 0.65;
  cursor: not-allowed;
  background-color: var(--disabled-bg);
  border: var(--disabled-border);
  box-shadow: none;
}

.custom-input.is-disabled .ci-input,
.custom-input.is-disabled .ci-textarea {
  cursor: not-allowed;
}

/* ---------- 错误状态 ---------- */
.custom-input.is-error {
  border-color: var(--el-color-danger);
}

/* 默认输入框 focus 时的错误阴影 */
.custom-input.is-error.ci-variant--default.is-focused:not(.is-disabled) {
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.3), 0 0 0 3px rgba(245, 108, 108, 0.15);
  border-color: var(--el-color-danger);
}

/* 搜索变体错误（保持 inset 阴影风格） */
.custom-input.is-error.ci-variant--search {
  box-shadow: 0 0 0 1px var(--el-color-danger) inset;
  border-color: var(--el-color-danger);
}

.custom-input.is-error.ci-variant--search.is-focused:not(.is-disabled) {
  box-shadow: 0 0 0 1px var(--el-color-danger) inset, 0 4px 16px rgba(245, 108, 108, 0.2);
}

/* 文本域 focus 时的错误阴影 */
.custom-input.is-error.ci-variant--textarea.is-focused:not(.is-disabled) {
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.3), 0 0 0 3px rgba(245, 108, 108, 0.15);
  border-color: var(--el-color-danger);
}

/* ---------- 尺寸系统 ---------- */

/* default */
.ci-size--default .ci-input {
  height: var(--custom-input-height, 36px);
  font-size: 14px;
}
.ci-size--default .ci-textarea {
  font-size: 14px;
}

/* large */
.ci-size--large .ci-input {
  height: var(--custom-input-height, 40px);
  font-size: 16px;
}
.ci-size--large .ci-textarea {
  font-size: 16px;
}
.ci-size--large .ci-prefix .iconfont {
  font-size: 18px;
}

/* small */
.ci-size--small .ci-input {
  height: var(--custom-input-height, 26px);
  font-size: 12px;
}
.ci-size--small .ci-textarea {
  font-size: 12px;
}
.ci-size--small .ci-prefix .iconfont {
  font-size: 14px;
}
.ci-size--small .ci-icon-btn {
  width: 16px;
  height: 16px;
  font-size: 12px;
}
</style>
