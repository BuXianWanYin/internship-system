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
import { LineChart as EChartsLineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'

// 注册ECharts组件
use([
  CanvasRenderer,
  EChartsLineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
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
  },
  xAxisName: {
    type: String,
    default: ''
  },
  yAxisName: {
    type: String,
    default: ''
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

  const names = props.data.map(item => item.name || item.month)
  const values = props.data.map(item => item.value || item.averageDays)

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
      trigger: 'axis',
      formatter: (params) => {
        const param = params[0]
        return `${param.name}<br/>${param.seriesName}: ${param.value}${props.yAxisName.includes('天') ? '天' : ''}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        color: '#606266',
        fontSize: 12
      },
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      },
      name: props.xAxisName,
      nameTextStyle: {
        color: '#606266',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#606266',
        fontSize: 12
      },
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        },
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#F0F2F5',
          type: 'dashed'
        }
      },
      name: props.yAxisName,
      nameTextStyle: {
        color: '#606266',
        fontSize: 12
      }
    },
    series: [
      {
        name: props.title || '数据',
        type: 'line',
        data: values,
        smooth: true,
        lineStyle: {
          color: '#409EFF',
          width: 3
        },
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(64, 158, 255, 0.3)'
              },
              {
                offset: 1,
                color: 'rgba(64, 158, 255, 0.1)'
              }
            ]
          }
        },
        label: {
          show: true,
          position: 'top',
          color: '#606266',
          fontSize: 12
        }
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

