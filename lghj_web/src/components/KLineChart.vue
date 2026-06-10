<template>
    <div ref="chartRef" class="k-line-chart"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
    data: {
        type: Object,
        required: true
    },
    height: {
        type: String,
        default: '400px'
    }
})

const chartRef = ref(null)
let chartInstance = null

const initChart = () => {
    if (!chartRef.value) return

    chartInstance = echarts.init(chartRef.value)

    const option = {
        backgroundColor: '#fff',
        animation: false,
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross'
            }
        },
        grid: {
            left: '10%',
            right: '10%',
            bottom: '15%'
        },
        xAxis: {
            type: 'category',
            data: props.data.dates,
            scale: true,
            boundaryGap: false,
            axisLine: { onZero: false },
            splitLine: { show: false },
            splitNumber: 20,
            min: 'dataMin',
            max: 'dataMax'
        },
        yAxis: {
            scale: true,
            splitArea: {
                show: true
            }
        },
        dataZoom: [
            {
                type: 'inside',
                start: 50,
                end: 100
            },
            {
                show: true,
                type: 'slider',
                top: '90%',
                start: 50,
                end: 100
            }
        ],
        series: [
            {
                name: '日K',
                type: 'candlestick',
                data: props.data.values,
                itemStyle: {
                    color: '#D32F2F', // Up (Red)
                    color0: '#2E7D32', // Down (Green)
                    borderColor: '#D32F2F',
                    borderColor0: '#2E7D32'
                }
            },
            // MA Lines
            {
                name: 'MA5',
                type: 'line',
                data: calculateMA(5, props.data),
                smooth: true,
                lineStyle: { opacity: 0.5 }
            },
            {
                name: 'MA10',
                type: 'line',
                data: calculateMA(10, props.data),
                smooth: true,
                lineStyle: { opacity: 0.5 }
            }
        ]
    }

    // If prediction data exists
    if (props.data.prediction) {
        option.series.push({
            name: '预测趋势',
            type: 'line',
            data: props.data.prediction, // Should align with dates extended into future
            smooth: true,
            lineStyle: {
                type: 'dashed',
                color: '#FF9800',
                width: 2
            }
        })
    }

    chartInstance.setOption(option)
}

function calculateMA(dayCount, data) {
    var result = []
    for (var i = 0, len = data.values.length; i < len; i++) {
        if (i < dayCount) {
            result.push('-')
            continue
        }
        var sum = 0
        for (var j = 0; j < dayCount; j++) {
            sum += +data.values[i - j][1] // close price
        }
        result.push((sum / dayCount).toFixed(2))
    }
    return result
}

watch(() => props.data, () => {
    if (chartInstance) {
        chartInstance.dispose()
    }
    initChart()
}, { deep: true })

onMounted(() => {
    initChart()
    window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    if (chartInstance) {
        chartInstance.dispose()
    }
})

const handleResize = () => {
    chartInstance && chartInstance.resize()
}
</script>

<style scoped>
.k-line-chart {
    width: 100%;
    height: v-bind(height);
}
</style>