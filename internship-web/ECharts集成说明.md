# ECharts 图表库集成说明

## 安装依赖

在项目根目录下运行以下命令安装 ECharts 和 vue-echarts：

```bash
npm install echarts vue-echarts
```

或者使用 yarn：

```bash
yarn add echarts vue-echarts
```

## 已创建的文件

### 图表组件

1. **PieChart.vue** - 饼图组件
   - 位置：`src/components/charts/PieChart.vue`
   - 用途：显示比例分布数据（如实习进度、岗位类型分布等）

2. **BarChart.vue** - 柱状图组件
   - 位置：`src/components/charts/BarChart.vue`
   - 用途：显示数量对比数据（如各学校/专业人数对比、分数分布等）

3. **LineChart.vue** - 折线图组件
   - 位置：`src/components/charts/LineChart.vue`
   - 用途：显示趋势数据（如实习时长分布等）

4. **RadarChart.vue** - 雷达图组件
   - 位置：`src/components/charts/RadarChart.vue`
   - 用途：显示多维度评价数据（如学生个人评价分数）

### 页面更新

1. **Dashboard.vue** - 仪表盘页面
   - 位置：`src/views/Dashboard.vue`
   - 已集成所有图表组件
   - 已连接后端API
   - 根据不同角色显示不同的图表

## 使用说明

### 图表组件使用示例

```vue
<template>
  <PieChart
    :data="pieData"
    :title="'实习进度统计'"
    :height="300"
  />
</template>

<script setup>
import PieChart from '@/components/charts/PieChart.vue'

const pieData = [
  { name: '待开始', value: 30, color: '#C0C4CC' },
  { name: '进行中', value: 320, color: '#409EFF' },
  { name: '已完成', value: 150, color: '#67C23A' }
]
</script>
```

### 数据格式

#### 饼图数据格式
```javascript
[
  {
    name: '名称',
    value: 数值,
    color: '#409EFF' // 可选，默认使用主题色
  }
]
```

#### 柱状图数据格式
```javascript
[
  {
    name: '名称',
    value: 数值,
    color: '#409EFF' // 可选
  }
]
```

#### 折线图数据格式
```javascript
[
  {
    name: '名称', // 或 month: '2024-01'
    value: 数值, // 或 averageDays: 85.5
  }
]
```

#### 雷达图数据格式
```javascript
// indicators: 指标列表
[
  {
    name: '指标名称',
    max: 100 // 最大值
  }
]

// data: 数据列表
[
  {
    name: '数据名称',
    values: [85, 90, 88, 82, 87] // 对应每个指标的值
  }
]
```

## 配色方案

所有图表使用统一的柔和配色方案：

- **主色调**：#409EFF（柔和蓝色）
- **成功色**：#67C23A（浅绿）
- **警告色**：#F7BA2A（浅橙）
- **信息色**：#909399（灰色）
- **待处理**：#C0C4CC（浅灰）
- **辅助色**：#79BBFF, #A0CFFF, #C6E2FF, #95D475, #FF7875

## 注意事项

1. 所有图表组件都支持响应式，会自动适应容器大小
2. 图表数据为空时会显示"暂无数据"提示
3. 图表高度可通过 `height` 属性自定义
4. 所有图表都使用 Canvas 渲染，性能较好

## 后续优化

1. 可以添加图表数据缓存，减少API调用
2. 可以添加图表导出功能（导出为图片）
3. 可以添加图表数据刷新按钮
4. 可以优化大数据量图表的渲染性能

