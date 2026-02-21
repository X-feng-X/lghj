<template>
    <div class="dashboard-container">
        <!-- Index Cards -->
        <el-row :gutter="20" class="mb-4">
            <el-col :span="8" v-for="idx in indices" :key="idx.name">
                <el-card class="index-card" :class="{ 'is-up': idx.change > 0, 'is-down': idx.change < 0 }"
                    @click="handleIndexClick(idx)" style="cursor: pointer">
                    <div class="index-header">
                        <span class="index-name">{{ idx.name }}</span>
                        <span class="index-tag" :class="idx.change > 0 ? 'bg-up' : 'bg-down'">
                            {{ idx.change > 0 ? '+' : '' }}{{ idx.percent }}%
                        </span>
                    </div>
                    <div class="index-value" :class="idx.change > 0 ? 'text-up' : 'text-down'">
                        {{ idx.value }}
                    </div>
                    <div class="index-change" :class="idx.change > 0 ? 'text-up' : 'text-down'">
                        {{ idx.change > 0 ? '+' : '' }}{{ idx.change }}
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <el-row :gutter="20">
            <!-- Main Chart & Prediction -->
            <el-col :span="16">
                <el-card class="chart-card">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">{{ currentName }} ({{ currentSymbol }})</span>
                            <div class="header-actions">
                                <el-radio-group v-model="timeRange" size="small" @change="handleTimeRangeChange">
                                    <el-radio-button label="D">日k</el-radio-button>
                                    <el-radio-button label="W">周k</el-radio-button>
                                    <el-radio-button label="M">月k</el-radio-button>
                                </el-radio-group>
                            </div>
                        </div>
                    </template>

                    <div class="chart-container" style="height: 400px;">
                        <div v-if="loading" class="loading-state">
                            <el-skeleton :rows="10" animated />
                        </div>
                        <k-line-chart v-else :data="chartData" height="400px" />
                    </div>

                    <div class="prediction-info" v-if="predictionResult">
                        <el-alert :title="predictionResult" type="success" :closable="false" show-icon />
                    </div>
                </el-card>
            </el-col>

            <!-- Right Sidebar -->
            <el-col :span="8">
                <!-- Watchlist -->
                <el-card class="mb-4">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">自选股</span>
                            <el-button link @click="$router.push('/watchlist')">更多</el-button>
                        </div>
                    </template>
                    <el-table :data="watchlist" style="width: 100%" :show-header="false" empty-text="暂无自选股"
                        @row-click="handleWatchlistClick" class="watchlist-table">
                        <el-table-column prop="name" label="Name">
                            <template #default="scope">
                                {{ scope.row.name }} <span
                                    style="font-size: 12px; color: #999;">{{ scope.row.symbol }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="currentPrice" label="Price" align="right">
                            <template #default="scope">
                                <span :class="scope.row.changePercent > 0 ? 'text-up' : 'text-down'">
                                    {{ scope.row.currentPrice }}
                                </span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="changePercent" label="Percent" align="right" width="80">
                            <template #default="scope">
                                <span :class="scope.row.changePercent > 0 ? 'text-up' : 'text-down'">
                                    {{ scope.row.changePercent > 0 ? '+' : '' }}{{ scope.row.changePercent }}%
                                </span>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>

                <!-- News -->
                <el-card>
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">最新资讯</span>
                            <el-button link @click="$router.push('/news')">更多</el-button>
                        </div>
                    </template>
                    <ul class="news-list" v-loading="newsLoading">
                        <li v-for="news in latestNews" :key="news.url" class="news-item" @click="openNews(news.url)">
                            <span class="news-title" :title="news.title">{{ news.title }}</span>
                            <span class="news-time">{{ formatTime(news.time) }}</span>
                        </li>
                    </ul>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import KLineChart from '../components/KLineChart.vue'
import { getRealtimeNews, getPrediction, getWatchlist, getStockHistory } from '../api/stock'

const loading = ref(true)
const predictionLoading = ref(false)
const newsLoading = ref(false)
const timeRange = ref('D')
const currentSymbol = ref('sh000001') // Default Shanghai Index
const currentName = ref('上证指数')

const indices = ref([
    { name: '上证指数', value: '3,050.12', change: 12.45, percent: 0.41, symbol: 'sh000001', stockName: '上证指数' },
    { name: '深证成指', value: '9,850.33', change: -25.10, percent: -0.25, symbol: 'sz399001', stockName: '深证成指' },
    { name: '创业板指', value: '1,980.66', change: 5.88, percent: 0.30, symbol: 'sz399006', stockName: '创业板指' }
])

const watchlist = ref([])
const latestNews = ref([])
const predictionResult = ref('')

const chartData = ref({
    dates: [],
    values: [],
    prediction: null
})

const handleIndexClick = (idx) => {
    currentSymbol.value = idx.symbol
    currentName.value = idx.stockName
    fetchHistory()
    fetchNews()
}

const handleWatchlistClick = (row) => {
    currentSymbol.value = row.symbol
    currentName.value = row.name
    fetchHistory()
    fetchNews()
}

// Mock Data Generator as fallback
const generateData = () => {
    const dates = []
    const values = []
    let baseValue = 3000
    const now = new Date()
    const days = 365 * 10 // 10 years

    for (let i = 0; i < days; i++) {
        const date = new Date(now.getTime() - (days - i) * 24 * 3600 * 1000)
        const dateStr = [date.getFullYear(), (date.getMonth() + 1).toString().padStart(2, '0'), date.getDate().toString().padStart(2, '0')].join('-')
        dates.push(dateStr)

        const open = baseValue + Math.random() * 20 - 10
        const close = baseValue + Math.random() * 20 - 10
        const low = Math.min(open, close) - Math.random() * 10
        const high = Math.max(open, close) + Math.random() * 10

        values.push([open, close, low, high])
        baseValue = close
    }

    return { dates, values }
}

const fetchHistory = async () => {
    loading.value = true
    try {
        const res = await getStockHistory(currentSymbol.value, timeRange.value)
        // Assuming res.data is array of { date, open, close, low, high }
        if (Array.isArray(res.data) && res.data.length > 0) {
            const dates = []
            const values = []

            // Sort by date just in case
            res.data.sort((a, b) => new Date(a.date) - new Date(b.date))

            res.data.forEach(item => {
                dates.push(item.date) // Ensure date format is yyyy-MM-dd
                values.push([item.open, item.close, item.low, item.high])
            })
            chartData.value = { dates, values }
        } else {
            // Fallback if empty or wrong format
            const { dates, values } = generateData()
            chartData.value = { dates, values }
        }
    } catch (error) {
        console.error('Failed to fetch history, using mock data', error)
        const { dates, values } = generateData()
        chartData.value = { dates, values }
    } finally {
        loading.value = false
    }
}

const fetchPrediction = async () => {
    predictionLoading.value = true
    try {
        const res = await getPrediction(currentSymbol.value)
        if (res.data && res.data.predictions) {
            const preds = res.data.predictions

            const newDates = [...chartData.value.dates]
            const predictionValues = new Array(chartData.value.dates.length).fill('-')

            preds.forEach(p => {
                newDates.push(p.date)
                predictionValues.push(p.price)
            })

            chartData.value = {
                ...chartData.value,
                dates: newDates,
                prediction: predictionValues
            }

            if (preds.length > 0) {
                const first = preds[0].price
                const last = preds[preds.length - 1].price
                const trend = last > first ? '上涨' : '下跌'
                predictionResult.value = `AI 预测结果: 未来30天呈${trend}趋势。`
            }
        }
    } catch (error) {
        console.error(error)
    } finally {
        predictionLoading.value = false
    }
}

const fetchNews = async () => {
    newsLoading.value = true
    try {
        const res = await getRealtimeNews(currentSymbol.value)
        latestNews.value = res.data
    } catch (error) {
        console.error(error)
    } finally {
        newsLoading.value = false
    }
}

const fetchWatchlist = async () => {
    try {
        const res = await getWatchlist()
        if (res.data) {
            watchlist.value = res.data.slice(0, 5)
        }
    } catch (error) {
        console.error(error)
    }
}

const handleTimeRangeChange = () => {
    // Implement time range filtering if API supports it or filter local data
    fetchHistory()
}

const openNews = (url) => {
    if (url) window.open(url, '_blank')
}

const formatTime = (timeStr) => {
    if (!timeStr) return ''
    return timeStr.split(' ')[0]
}

onMounted(() => {
    fetchHistory()
    fetchNews()
    fetchWatchlist()
})
</script>

<style scoped>
/* Styles remain same */
.dashboard-container {}

.mb-4 {
    margin-bottom: 20px;
}

.index-card {
    text-align: center;
    transition: transform 0.3s;
}

.index-card:hover {
    transform: translateY(-2px);
}

.index-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.index-name {
    font-size: 16px;
    color: #666;
}

.index-tag {
    font-size: 12px;
    padding: 2px 6px;
    border-radius: 4px;
}

.index-value {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 5px;
}

.index-change {
    font-size: 14px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-title {
    font-size: 16px;
    font-weight: bold;
    border-left: 4px solid var(--el-color-primary);
    padding-left: 10px;
}

.prediction-info {
    margin-top: 20px;
}

.watchlist-table {
    cursor: pointer;
}

.watchlist-table :deep(.el-table__row:hover) {
    background-color: #f5f7fa;
}

.news-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.news-item {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
}

.news-item:last-child {
    border-bottom: none;
}

.news-item:hover .news-title {
    color: var(--el-color-primary);
}

.news-title {
    font-size: 14px;
    color: #333;
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-right: 10px;
}

.news-time {
    font-size: 12px;
    color: #999;
}
</style>