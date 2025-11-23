<template>
  <div class="rich-text-editor">
    <div ref="editorRef" class="editor-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  },
  height: {
    type: String,
    default: '300px'
  }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
let quillInstance = null

onMounted(() => {
  if (editorRef.value) {
    quillInstance = new Quill(editorRef.value, {
      theme: 'snow',
      placeholder: props.placeholder,
      modules: {
        toolbar: [
          [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
          [{ 'font': [] }],
          [{ 'size': ['small', false, 'large', 'huge'] }],
          ['bold', 'italic', 'underline', 'strike'],
          [{ 'color': [] }, { 'background': [] }],
          [{ 'script': 'sub' }, { 'script': 'super' }],
          [{ 'list': 'ordered' }, { 'list': 'bullet' }],
          [{ 'indent': '-1' }, { 'indent': '+1' }],
          [{ 'align': [] }],
          ['blockquote', 'code-block'],
          ['link', 'image'],
          ['clean']
        ]
      }
    })
    
    // 设置高度
    if (props.height) {
      editorRef.value.style.height = props.height
    }
    
    // 设置初始内容
    if (props.modelValue) {
      quillInstance.root.innerHTML = props.modelValue
    }
    
    // 监听内容变化
    quillInstance.on('text-change', () => {
      const content = quillInstance.root.innerHTML
      emit('update:modelValue', content)
    })
  }
})

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (quillInstance && quillInstance.root.innerHTML !== newValue) {
    quillInstance.root.innerHTML = newValue || ''
  }
})

onBeforeUnmount(() => {
  if (quillInstance) {
    quillInstance = null
  }
})

// 获取编辑器内容
const getContent = () => {
  return quillInstance ? quillInstance.root.innerHTML : ''
}

// 设置编辑器内容
const setContent = (content) => {
  if (quillInstance) {
    quillInstance.root.innerHTML = content || ''
  }
}

// 清空编辑器
const clear = () => {
  if (quillInstance) {
    quillInstance.setText('')
  }
}

defineExpose({
  getContent,
  setContent,
  clear
})
</script>

<style scoped>
.rich-text-editor {
  width: 100%;
}

.editor-container {
  background: #fff;
}

:deep(.ql-container) {
  min-height: 200px;
  font-size: 14px;
}

:deep(.ql-editor) {
  min-height: 200px;
}

:deep(.ql-toolbar) {
  border-top: 1px solid #ccc;
  border-left: 1px solid #ccc;
  border-right: 1px solid #ccc;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
}

:deep(.ql-container) {
  border-bottom: 1px solid #ccc;
  border-left: 1px solid #ccc;
  border-right: 1px solid #ccc;
  border-top: none;
  border-radius: 0 0 4px 4px;
}
</style>

