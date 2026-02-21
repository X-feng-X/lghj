<template>
  <div class="market-page">
    <el-card>
      <div v-if="!currentSymbol" class="market-placeholder">
        <el-icon :size="60" color="#D32F2F">
          <TrendCharts />
        </el-icon>
        <h2>行情中心</h2>
        <p>请使用顶部搜索栏查找股票，查看详细行情与预测。</p>
        <el-button type="primary" @click="$router.push('/prediction')">去预测</el-button>
      </div>

      <div v-else class="market-detail">
        <div class="detail-header">
          <h2>{{ currentSymbol }} 行情详情</h2>
          <div class="header-actions">
            <el-button type="primary" link
              @click="$router.push({ path: '/prediction', query: { symbol: currentSymbol } })">
              查看AI预测 >
            </el-button>
          </div>
        </div>

        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="k-line-tabs">
          <el-tab-pane label="日K线" name="D"></el-tab-pane>
          <el-tab-pane label="周K线" name="W"></el-tab-pane>
          <el-tab-pane label="月K线" name="M"></el-tab-pane>
        </el-tabs>

        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>

        <div v-else-if="chartData.dates.length > 0" class="chart-wrapper">
          <k-line-chart :data="chartData" height="500px" />
        </div>

        <el-empty v-else description="暂无该股票历史数据" />

        <div class="news-section">
          <div class="section-title">
            <h3>相关资讯</h3>
            <el-button link type="primary" @click="loadNews">刷新</el-button>
          </div>
          <div v-if="newsLoading" class="news-loading">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="newsList.length > 0" class="news-list">
            <div v-for="(item, index) in newsList" :key="index" class="news-item">
              <div class="news-content">
                <a :href="item.url" target="_blank" class="news-title">{{ item.title }}</a>
                <span class="news-date">{{ item.date }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无相关资讯" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { TrendCharts } from '@element-plus/icons-vue'
import KLineChart from '../components/KLineChart.vue'
import { getStockHistory, getRealtimeNews } from '../api/stock'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const currentSymbol = ref('')
const activeTab = ref('D')
const loading = ref(false)
const chartData = ref({ dates: [], values: [] })

const newsLoading = ref(false)
const newsList = ref([])

const loadStockData = async (symbol, period = 'D') => {
  if (!symbol) return

  loading.value = true
  try {
    const res = await getStockHistory(symbol, period)
    if (res.code === 200 && Array.isArray(res.data)) {
      processChartData(res.data)
    } else {
      // Fallback mock data if API fails or returns empty (for demo purposes)
      console.warn('History fetch failed or empty, using mock', res)
      generateMockData()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取行情数据失败')
    generateMockData()
  } finally {
    loading.value = false
  }
}

const loadNews = async () => {
  if (!currentSymbol.value) return
  newsLoading.value = true
  try {
    const res = await getRealtimeNews(currentSymbol.value)
    if (res.code === 200) {
      newsList.value = res.data
    } else {
      // Mock news if API fails
      generateMockNews()
    }
  } catch (error) {
    console.error("Failed to fetch news", error)
    generateMockNews()
  } finally {
    newsLoading.value = false
  }
}

const generateMockNews = () => {
  newsList.value = [
    { title: `${currentSymbol.value} 近期表现强劲，主力资金大幅流入`, date: '2026-02-20 10:30', url: '#' },
    { title: `行业利好频出，${currentSymbol.value} 有望突破前期高点`, date: '2026-02-19 14:20', url: '#' },
    { title: `${currentSymbol.value} 发布最新财报，业绩超预期`, date: '2026-02-18 09:15', url: '#' }
  ]
}

const processChartData = (data) => {
  const dates = []
  const values = []

  // Sort by date just in case
  data.sort((a, b) => new Date(a.date) - new Date(b.date))

  data.forEach(item => {
    dates.push(item.date)
    values.push([item.open, item.close, item.low, item.high])
  })

  chartData.value = {
    dates,
    values
  }
}

const generateMockData = () => {
  const dates = []
  const values = []
  const now = new Date()
  let baseValue = 100

  for (let i = 0; i < 60; i++) {
    const date = new Date(now.getTime() - (60 - i) * 24 * 3600 * 1000)
    const dateStr = [date.getFullYear(), (date.getMonth() + 1).toString().padStart(2, '0'), date.getDate().toString().padStart(2, '0')].join('-')
    dates.push(dateStr)

    const open = baseValue + Math.random() * 5 - 2.5
    const close = baseValue + Math.random() * 5 - 2.5
    const low = Math.min(open, close) - Math.random() * 2
    const high = Math.max(open, close) + Math.random() * 2

    values.push([open, close, low, high])
    baseValue = close
  }

  chartData.value = { dates, values }
}

const handleTabChange = (tab) => {
  loadStockData(currentSymbol.value, tab)
}

onMounted(() => {
  if (route.query.symbol) {
    currentSymbol.value = route.query.symbol
    loadStockData(currentSymbol.value, activeTab.value)
    loadNews()
  }
})

watch(() => route.query.symbol, (newSymbol) => {
  if (newSymbol) {
    currentSymbol.value = newSymbol
    loadStockData(newSymbol, activeTab.value)
    loadNews()
  } else {
    currentSymbol.value = ''
    chartData.value = { dates: [], values: [] }
    newsList.value = []
  }
})
</script>

<style scoped>
.market-page {
  padding: 20px 0;
}

.market-placeholder {
  text-align: center;
  padding: 60px 0;
}

.market-placeholder h2 {
  margin: 20px 0 10px;
  color: #333;
}

.market-placeholder p {
  color: #666;
  margin-bottom: 30px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h2 {
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.k-line-tabs {
  margin-bottom: 20px;
}

.chart-wrapper {
  margin-top: 20px;
  margin-bottom: 40px;
}

.loading-container {
  padding: 40px 0;
}

.news-section {
  margin-top: 30px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-title h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
  border-left: 4px solid var(--el-color-primary);
  padding-left: 10px;
}

.news-item {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.news-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.news-title {
  color: #333;
  text-decoration: none;
  font-size: 14px;
  flex: 1;
  margin-right: 20px;
}

.news-title:hover {
  color: var(--el-color-primary);
}

.news-date {
  color: #999;
  font-size: 12px;
  white-space: nowrap;
}
</style>
