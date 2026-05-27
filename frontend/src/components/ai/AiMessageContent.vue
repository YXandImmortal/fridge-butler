<template>
  <div class="ai-message-markdown">
    <VueMarkdown :source="content" :options="markdownOptions"/>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import VueMarkdown from 'vue-markdown-render'
import hljs from 'highlight.js/lib/common'

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  isStreaming: {
    type: Boolean,
    default: false
  }
})

/**
 * markdown-it 配置
 * - html: false  禁止渲染原始 HTML，防止 XSS
 * - linkify: true 自动识别链接
 * - breaks: true  转换换行符
 * - highlight:    代码块语法高亮
 */
const markdownOptions = computed(() => ({
  html: false,
  linkify: true,
  breaks: true,
  highlight: (str, lang) => {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, {language: lang}).value
      } catch (__) {
        // fall through
      }
    }
    // 语言未知或高亮失败时自动检测
    try {
      return hljs.highlightAuto(str).value
    } catch (__) {
      return escapeHtml(str)
    }
  }
}))

function escapeHtml(text) {
  return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;')
}
</script>

<style scoped lang="scss">
.ai-message-markdown {
  /* 基础文本 */
  :deep(p) {
    margin: 0 0 0.6em;
    line-height: 1.7;
    color: inherit;

    &:last-child {
      margin-bottom: 0;
    }
  }

  /* 标题 */
  :deep(h1),
  :deep(h2),
  :deep(h3),
  :deep(h4),
  :deep(h5),
  :deep(h6) {
    margin: 0.8em 0 0.4em;
    font-weight: 600;
    line-height: 1.4;
    color: var(--text-primary);
  }

  :deep(h1) {
    font-size: 1.35em;
  }

  :deep(h2) {
    font-size: 1.2em;
  }

  :deep(h3) {
    font-size: 1.1em;
  }

  :deep(h4),
  :deep(h5),
  :deep(h6) {
    font-size: 1em;
  }

  /* 列表 */
  :deep(ul),
  :deep(ol) {
    margin: 0.4em 0;
    padding-left: 1.4em;
  }

  :deep(li) {
    margin: 0.25em 0;
    line-height: 1.7;
  }

  :deep(ul) {
    list-style-type: disc;
  }

  :deep(ol) {
    list-style-type: decimal;
  }

  /* 任务列表 */
  :deep(input[type="checkbox"]) {
    margin-right: 0.4em;
    vertical-align: middle;
    accent-color: var(--primary-color);
  }

  /* 引用块 */
  :deep(blockquote) {
    margin: 0.6em 0;
    padding: 0.4em 0.8em;
    border-left: 3px solid var(--primary-color);
    background: var(--primary-10);
    border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
    color: var(--text-secondary);

    p:last-child {
      margin-bottom: 0;
    }
  }

  /* 代码块 */
  :deep(pre) {
    margin: 0.6em 0;
    padding: 0.8em 1em;
    background: var(--input-bg);
    border-radius: var(--radius-sm);
    overflow-x: auto;
    border: 1px solid var(--border-color);

    code {
      display: block;
      font-family: 'JetBrains Mono', 'Fira Code', 'Cascadia Code', 'MiSans', monospace;
      font-size: 0.88em;
      line-height: 1.6;
      color: var(--text-primary);
      background: transparent;
      padding: 0;
      border-radius: 0;
    }
  }

  /* 行内代码 */
  :deep(:not(pre) > code) {
    font-family: 'JetBrains Mono', 'Fira Code', 'Cascadia Code', 'MiSans', monospace;
    font-size: 0.9em;
    padding: 0.15em 0.4em;
    background: var(--input-bg);
    border-radius: 4px;
    color: var(--primary-dark);
    border: 1px solid var(--border-color);
  }

  /* 表格 */
  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 0.6em 0;
    font-size: 0.95em;
  }

  :deep(th),
  :deep(td) {
    padding: 0.5em 0.8em;
    border: 1px solid var(--border-color);
    text-align: left;
  }

  :deep(th) {
    background: var(--primary-10);
    font-weight: 600;
    color: var(--text-primary);
  }

  :deep(tr:nth-child(even)) {
    background: var(--white-10);
  }

  /* 链接 */
  :deep(a) {
    color: var(--primary-color);
    text-decoration: none;
    border-bottom: 1px solid transparent;
    transition: border-color 0.2s ease;

    &:hover {
      border-bottom-color: var(--primary-color);
    }
  }

  /* 粗体、斜体 */
  :deep(strong) {
    font-weight: 700;
    color: var(--text-primary);
  }

  :deep(em) {
    font-style: italic;
  }

  /* 删除线 */
  :deep(del) {
    opacity: 0.6;
    text-decoration: line-through;
  }

  /* 分隔线 */
  :deep(hr) {
    border: none;
    border-top: 1px solid var(--divider-color);
    margin: 1em 0;
  }

  /* 图片 */
  :deep(img) {
    max-width: 100%;
    border-radius: var(--radius-sm);
    margin: 0.4em 0;
  }

  /* ========== Highlight.js 语法高亮配色 ========== */
  /* 使用 CSS 变量，自动适配亮/暗主题 */
  :deep(.hljs) {
    color: var(--text-primary);
    background: transparent;
  }

  :deep(.hljs-keyword),
  :deep(.hljs-selector-tag),
  :deep(.hljs-literal),
  :deep(.hljs-section),
  :deep(.hljs-link) {
    color: var(--color-pink-500);
  }

  :deep(.hljs-string),
  :deep(.hljs-title),
  :deep(.hljs-name),
  :deep(.hljs-type),
  :deep(.hljs-attribute),
  :deep(.hljs-symbol),
  :deep(.hljs-bullet),
  :deep(.hljs-built_in),
  :deep(.hljs-addition) {
    color: var(--color-emerald);
  }

  :deep(.hljs-number),
  :deep(.hljs-meta),
  :deep(.hljs-subst),
  :deep(.hljs-regexp) {
    color: var(--color-orange-500);
  }

  :deep(.hljs-comment),
  :deep(.hljs-quote),
  :deep(.hljs-deletion) {
    color: var(--text-tertiary);
    font-style: italic;
  }

  :deep(.hljs-variable),
  :deep(.hljs-template-variable),
  :deep(.hljs-tag),
  :deep(.hljs-name) {
    color: var(--color-cyan-500);
  }

  :deep(.hljs-params) {
    color: var(--text-secondary);
  }

  :deep(.hljs-property),
  :deep(.hljs-attr) {
    color: var(--color-purple-500);
  }

  :deep(.hljs-function) {
    color: var(--color-primary-500);
  }

  :deep(.hljs-operator),
  :deep(.hljs-punctuation) {
    color: var(--text-secondary);
  }
}
</style>
