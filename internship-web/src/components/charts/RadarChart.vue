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
import { RadarChart as EChartsRadarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'

// 注册ECharts组件
use([
  CanvasRenderer,
  EChartsRadarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent
])

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  indicators: {
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
  if (!props.data || props.data.length === 0 || !props.indicators || props.indicators.length === 0) {
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
      trigger: 'item'
    },
    radar: {
      indicator: props.indicators.map(indicator => ({
        name: indicator.name,
        max: indicator.max || 100
      })),
      center: ['50%', '55%'],
      radius: '65%',
      axisName: {
        color: '#606266',
        fontSize: 12
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(64, 158, 255, 0.05)', 'rgba(64, 158, 255, 0.1)']
        }
      },
      splitLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      },
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      }
    },
    series: [
      {
        name: props.title || '数据',
        type: 'radar',
        data: props.data.map(item => ({
          value: item.values,
          name: item.name,
          areaStyle: {
            color: 'rgba(64, 158, 255, 0.2)'
          },
          lineStyle: {
            color: '#409EFF',
            width: 2
          },
          itemStyle: {
            color: '#409EFF'
          },
          label: {
            show: true,
            color: '#606266',
            fontSize: 12
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

