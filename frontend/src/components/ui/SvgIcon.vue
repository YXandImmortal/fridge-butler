<template>
  <svg
      class="svg-icon"
      :class="iconClass"
      :style="iconStyle"
      aria-hidden="true"
  >
    <use :xlink:href="symbolId"/>
  </svg>
</template>

<script setup>
import {computed} from 'vue'

const props = defineProps({
  name: {
    type: String,
    required: true
  },
  prefix: {
    type: String,
    default: 'icon'
  },
  size: {
    type: [String, Number],
    default: '1em'
  },
  color: {
    type: String,
    default: ''
  }
})

const symbolId = computed(() => `#${props.prefix}-${props.name}`)

const iconClass = computed(() => {
  return `svg-icon--${props.name}`
})

const iconStyle = computed(() => {
  const style = {}
  if (props.size) {
    const sizeValue = typeof props.size === 'number' ? `${props.size}px` : props.size
    style.width = sizeValue
    style.height = sizeValue
  }
  if (props.color) {
    style.color = props.color
  }
  return style
})
</script>

<style scoped lang="scss">
.svg-icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
  transition: transform 0.3s ease;
}
</style>
