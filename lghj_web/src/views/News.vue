<template>
    <div class="news-page">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span class="card-title">财经资讯</span>
                    <div class="header-actions">
                        <el-select v-model="symbolInput" placeholder="搜索股票/代码" remote :remote-method="handleSearchStock"
                            :loading="searchLoading" class="search-input" filterable clearable
                            style="width: 250px; margin-right: 10px;" @change="fetchNews">
                            <el-option v-for="item in searchResults" :key="item.symbol"
                                :label="item.name + ' (' + item.symbol + ')'" :value="item.symbol" />
                        </el-select>
                        <el-button type="primary" @click="fetchNews">搜索</el-button>
                    </div>
                </div>
            </template>

            <div v-loading="loading">
                <el-empty v-if="newsList.length === 0" description="暂无资讯" />
                <ul v-else class="news-list">
                    <li v-for="news in newsList" :key="news.url" class="news-item" @click="openNews(news.url)">
                        <div class="news-content">
                            <h3 class="news-title">{{ news.title }}</h3>
                            <div class="news-meta">
                                <span class="news-source">{{ news.source || '未知来源' }}</span>
                                <span class="news-time">{{ news.time }}</span>
                            </div>
                        </div>
                        <el-icon>
                            <ArrowRight />
                        </el-icon>
                    </li>
                </ul>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRealtimeNews, searchStock } from '../api/stock'
import { ArrowRight } from '@element-plus/icons-vue'

const symbolInput = ref('600519')
const newsList = ref([])
const loading = ref(false)
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

const fetchNews = async () => {
    if (!symbolInput.value) return
    loading.value = true
    try {
        const res = await getRealtimeNews(symbolInput.value, 20)
        newsList.value = res.data
    } catch (error) {
        console.error(error)
    } finally {
        loading.value = false
    }
}

const openNews = (url) => {
    if (url) window.open(url, '_blank')
}

onMounted(() => {
    fetchNews()
})
</script>

<style scoped>
.news-page {
    padding: 20px 0;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.news-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.news-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background-color 0.3s;
}

.news-item:hover {
    background-color: #fafafa;
}

.news-item:last-child {
    border-bottom: none;
}

.news-title {
    margin: 0 0 10px 0;
    font-size: 16px;
    color: #333;
}

.news-meta {
    font-size: 12px;
    color: #999;
}

.news-source {
    margin-right: 15px;
    background-color: #f0f2f5;
    padding: 2px 6px;
    border-radius: 4px;
}
</style>