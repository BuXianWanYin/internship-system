<template>
  <v-chart
    class="chart"
    :option="chartOption"
    :style="{ width: '100%', height: height + 'px' }"
    autoresize
  />
</template>

<script setup>
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart as EChartsPieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'

// 注册ECharts组件
use([
  CanvasRenderer,
  EChartsPieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent
])

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: ''
  },
  height: {
    type: Number,
    default: 300
  }
})

const chartOption = computed(() => {
  if (!props.data || props.data.length === 0) {
    return {
      title: {
        text: props.title || '暂无数据',
        left: 'center',
        textStyle: {
          color: '#909399',
          fontSize: 14
        }
      }
    }
  }

  return {
    title: {
      text: props.title,
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 500,
        color: '#303133'
      },
      show: !!props.title
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle',
      textStyle: {
        color: '#606266',
        fontSize: 12
      }
    },
    series: [
      {
        name: props.title || '数据',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c} ({d}%)',
          fontSize: 12,
          color: '#606266'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true
        },
        data: props.data.map(item => ({
          name: item.name,
          value: item.value,
          itemStyle: {
            color: item.color || '#409EFF'
          }
        }))
      }
    ]
  }
})
</script>

<style scoped>
.chart {
  width: 100%;
}
</style>

