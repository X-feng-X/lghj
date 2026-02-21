<template>
  <div class="prediction-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">AI 智能预测</span>
          <div class="header-actions">
            <el-select v-model="symbolInput" placeholder="搜索股票/代码" remote :remote-method="handleSearchStock"
                :loading="searchLoading" class="search-input" filterable clearable style="width: 250px; margin-right: 10px;">
                <el-option v-for="item in searchResults" :key="item.symbol"
                    :label="item.name + ' (' + item.symbol + ')'" :value="item.symbol" />
            </el-select>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button type="success" @click="handleExport" :disabled="!predictionData">导出 Excel</el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <div v-else-if="predictionData" class="prediction-content">
        <div class="stock-info">
          <h3>{{ predictionData.symbol }} 预测结果</h3>
          <el-tag type="warning">未来30天趋势预测</el-tag>
        </div>

        <div class="chart-container">
          <k-line-chart :data="chartData" height="500px" />
        </div>

        <div class="prediction-table">
          <h4>详细预测数据</h4>
          <el-table :data="predictionData.predictions" style="width: 100%" height="300">
            <el-table-column prop="date" label="日期" />
            <el-table-column prop="price" label="预测价格">
              <template #default="scope">
                {{ Number(scope.row.price).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <el-empty v-else description="请输入股票代码开始预测" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import KLineChart from '../components/KLineChart.vue'
import { getPrediction, getPredictionExcel, getStockHistory, searchStock } from '../api/stock'
import { ElMessage } from 'element-plus'

const route = useRoute()
const symbolInput = ref('')
const loading = ref(false)
const predictionData = ref(null)
const chartData = ref({ dates: [], values: [], prediction: [] })
const searchLoading = ref(false)
const searchResults = ref([])

const handleSearchStock = async (query) => {
    if (query !== '') {
        searchLoading.value = true
        try {
            const res = await searchStock(query)
            searchResults.value = res.data
        } catch (error) {
            console.error(error)
        } finally {
            searchLoading.value = false
        }
    } else {
        searchResults.value = []
    }
}

const handleSearch = async () => {
  if (!symbolInput.value) return

  loading.value = true
  try {
    // Parallel fetch
    const [predRes, histRes] = await Promise.allSettled([
      getPrediction(symbolInput.value),
      getStockHistory(symbolInput.value, 'D')
    ])

    let history = []
    if (histRes.status === 'fulfilled' && Array.isArray(histRes.value.data) && histRes.value.data.length > 0) {
      history = histRes.value.data
    } else {
      console.warn('History fetch failed or empty, using mock', histRes)
      history = generateHistory().raw
    }

    if (predRes.status === 'fulfilled') {
      predictionData.value = predRes.value.data
      updateChart(predRes.value.data, history)
    } else {
      ElMessage.error('预测数据获取失败')
      predictionData.value = null
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('查询出错')
  } finally {
    loading.value = false
  }
}

const handleExport = async () => {
  if (!symbolInput.value) return
  try {
    const blob = await getPredictionExcel(symbolInput.value)
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `${symbolInput.value}_prediction.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    console.error(error)
    ElMessage.error('导出失败')
  }
}

// Mock Data Generator for History (Fallback)
const generateHistory = () => {
  const dates = []
  const values = []
  const raw = []
  let baseValue = 100 // Default base

  // Try to use first prediction price as base if available
  if (predictionData.value && predictionData.value.predictions.length > 0) {
    baseValue = predictionData.value.predictions[0].price
  }

  const now = new Date()

  for (let i = 0; i < 60; i++) {
    const date = new Date(now.getTime() - (60 - i) * 24 * 3600 * 1000)
    const dateStr = [date.getFullYear(), (date.getMonth() + 1).toString().padStart(2, '0'), date.getDate().toString().padStart(2, '0')].join('-')
    dates.push(dateStr)

    // Random walk around baseValue
    const open = baseValue + Math.random() * 5 - 2.5
    const close = baseValue + Math.random() * 5 - 2.5
    const low = Math.min(open, close) - Math.random() * 2
    const high = Math.max(open, close) + Math.random() * 2

    values.push([open, close, low, high])
    raw.push({ date: dateStr, open, close, low, high })
  }

  return { dates, values, raw }
}

const updateChart = (predData, historyData) => {
  if (!predData || !predData.predictions) return

  const dates = []
  const values = []

  // Process history
  historyData.forEach(item => {
    dates.push(item.date)
    values.push([item.open, item.close, item.low, item.high])
  })

  const preds = predData.predictions

  const newDates = [...dates]
  const predictionValues = new Array(dates.length).fill('-')

  preds.forEach(p => {
    newDates.push(p.date)
    predictionValues.push(p.price)
  })

  chartData.value = {
    dates: newDates,
    values: values,
    prediction: predictionValues
  }
}

onMounted(() => {
  if (route.query.symbol) {
    symbolInput.value = route.query.symbol
    handleSearch()
  }
})
</script>

<style scoped>
.prediction-page {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.stock-info {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.prediction-table {
  margin-top: 20px;
}
</style>