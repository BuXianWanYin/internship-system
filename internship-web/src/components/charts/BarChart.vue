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
import { BarChart as EChartsBarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'

// 注册ECharts组件
use([
  CanvasRenderer,
  EChartsBarChart,
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

  const names = props.data.map(item => item.name)
  const values = props.data.map(item => item.value)
  const colors = props.data.map(item => item.color || '#409EFF')

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
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const param = params[0]
        return `${param.name}<br/>${param.seriesName}: ${param.value}`
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
        fontSize: 12,
        rotate: names.length > 8 ? 45 : 0,
        interval: 0
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
        type: 'bar',
        data: values.map((value, index) => ({
          value: value,
          itemStyle: {
            color: colors[index] || '#409EFF'
          }
        })),
        barWidth: '60%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0]
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

