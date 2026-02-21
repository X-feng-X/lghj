<template>
    <div class="simulation-page">
        <el-card class="mb-4">
            <template #header>
                <div class="card-header">
                    <span class="card-title">我的模拟账户</span>
                    <el-button type="primary" size="small" @click="refreshAccount">刷新</el-button>
                </div>
            </template>
            <div v-if="account" class="account-summary">
                <div class="summary-item">
                    <div class="label">总资产</div>
                    <div class="value text-up">{{ account.totalAsset }}</div>
                </div>
                <div class="summary-item">
                    <div class="label">可用资金</div>
                    <div class="value">{{ account.availableCash }}</div>
                </div>
                <div class="summary-item">
                    <div class="label">冻结资金</div>
                    <div class="value">{{ account.frozenCash }}</div>
                </div>
            </div>
            <div v-else class="empty-account">
                <el-button type="primary" @click="handleCreateAccount">开通模拟账户</el-button>
            </div>
        </el-card>

        <el-row :gutter="20" v-if="account">
            <el-col :span="8">
                <el-card class="trade-card">
                    <el-tabs v-model="activeTab">
                        <el-tab-pane label="买入" name="buy">
                            <el-form :model="tradeForm" label-width="80px">
                                <el-form-item label="股票代码">
                                    <el-select v-model="tradeForm.symbol" placeholder="搜索股票/代码" remote :remote-method="handleSearchStock"
                                        :loading="searchLoading" filterable clearable style="width: 100%;" @change="fetchStockPrice">
                                        <el-option v-for="item in searchResults" :key="item.symbol"
                                            :label="item.name + ' (' + item.symbol + ')'" :value="item.symbol" />
                                    </el-select>
                                </el-form-item>
                                <el-form-item label="当前价格">
                                    <span class="current-price">{{ currentPrice || '--' }}</span>
                                </el-form-item>
                                <el-form-item label="买入价格">
                                    <el-input-number v-model="tradeForm.price" :precision="2" :step="0.01"
                                        style="width: 100%" />
                                </el-form-item>
                                <el-form-item label="买入数量">
                                    <el-input-number v-model="tradeForm.quantity" :step="100" :min="100"
                                        style="width: 100%" />
                                </el-form-item>
                                <el-button type="danger" class="trade-btn" @click="handleTrade(1)">买入</el-button>
                            </el-form>
                        </el-tab-pane>
                        <el-tab-pane label="卖出" name="sell">
                            <el-form :model="tradeForm" label-width="80px">
                                <el-form-item label="股票代码">
                                    <el-select v-model="tradeForm.symbol" placeholder="搜索股票/代码" remote :remote-method="handleSearchStock"
                                        :loading="searchLoading" filterable clearable style="width: 100%;" @change="fetchStockPrice">
                                        <el-option v-for="item in searchResults" :key="item.symbol"
                                            :label="item.name + ' (' + item.symbol + ')'" :value="item.symbol" />
                                    </el-select>
                                </el-form-item>
                                <el-form-item label="当前价格">
                                    <span class="current-price">{{ currentPrice || '--' }}</span>
                                </el-form-item>
                                <el-form-item label="卖出价格">
                                    <el-input-number v-model="tradeForm.price" :precision="2" :step="0.01"
                                        style="width: 100%" />
                                </el-form-item>
                                <el-form-item label="卖出数量">
                                    <el-input-number v-model="tradeForm.quantity" :step="100" :min="100"
                                        style="width: 100%" />
                                </el-form-item>
                                <el-button type="success" class="trade-btn" @click="handleTrade(2)">卖出</el-button>
                            </el-form>
                        </el-tab-pane>
                    </el-tabs>
                </el-card>
            </el-col>

            <el-col :span="16">
                <el-card title="我的持仓">
                    <el-table :data="positions" style="width: 100%" height="300">
                        <el-table-column prop="symbol" label="代码" width="100" />
                        <el-table-column prop="quantity" label="持仓数量" />
                        <el-table-column prop="avgPrice" label="成本价" />
                        <el-table-column prop="marketValue" label="市值" />
                        <el-table-column prop="profit" label="盈亏">
                            <template #default="scope">
                                <span
                                    :class="scope.row.profit > 0 ? 'text-up' : scope.row.profit < 0 ? 'text-down' : ''">
                                    {{ scope.row.profit }}
                                </span>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>
            </el-col>
        </el-row>

        <el-card class="mt-4" v-if="account">
            <el-tabs>
                <el-tab-pane label="当日委托">
                    <el-table :data="orders" style="width: 100%">
                        <el-table-column prop="createTime" label="时间" width="160" />
                        <el-table-column prop="symbol" label="代码" width="100" />
                        <el-table-column prop="direction" label="方向" width="80">
                            <template #default="scope">
                                <span :class="scope.row.direction === 1 ? 'text-up' : 'text-down'">
                                    {{ scope.row.direction === 1 ? '买入' : '卖出' }}
                                </span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="price" label="委托价" />
                        <el-table-column prop="quantity" label="委托量" />
                        <el-table-column prop="filledQuantity" label="成交量" />
                        <el-table-column label="状态" width="100">
                            <template #default="scope">
                                <el-tag :type="getOrderStatusType(scope.row.status)">
                                    {{ getOrderStatusText(scope.row.status) }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作">
                            <template #default="scope">
                                <el-button v-if="scope.row.status === 1 || scope.row.status === 2" size="small"
                                    type="text" @click="handleCancel(scope.row.id)">撤单</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-tab-pane>
                <el-tab-pane label="当日成交">
                    <el-table :data="deals" style="width: 100%">
                        <el-table-column prop="dealTime" label="成交时间" width="160" />
                        <el-table-column prop="symbol" label="代码" width="100" />
                        <el-table-column prop="direction" label="方向" width="80">
                            <template #default="scope">
                                <span :class="scope.row.direction === 1 ? 'text-up' : 'text-down'">
                                    {{ scope.row.direction === 1 ? '买入' : '卖出' }}
                                </span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="price" label="成交价" />
                        <el-table-column prop="quantity" label="成交量" />
                    </el-table>
                </el-tab-pane>
            </el-tabs>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
    createAccount, getAccountInfo, getPositions,
    placeOrder, cancelOrder, getOrders, getDeals
} from '../api/trade'
import { searchStock, getStockHistory } from '../api/stock'
import { ElMessage, ElMessageBox } from 'element-plus'

const account = ref(null)
const positions = ref([])
const orders = ref([])
const deals = ref([])
const activeTab = ref('buy')
const currentPrice = ref(null)
const searchLoading = ref(false)
const searchResults = ref([])

const tradeForm = reactive({
    symbol: '',
    price: 0,
    quantity: 100
})

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

const fetchAccount = async () => {
    try {
        const res = await getAccountInfo()
        // Account info from backend might be wrapped or direct
        // Backend usually returns { code: 200, data: { ... } }
        // Let's inspect the structure if needed, but assuming standard Result<T>
        if (res.code === 200 && res.data) {
            account.value = res.data
            fetchPositions()
            fetchOrders()
            fetchDeals()
        } else {
            // Handle case where data might be null or structure is different
            console.warn('Account info fetch failed or empty', res)
            account.value = null
        }
    } catch (error) {
        console.error('Fetch account error', error)
    }
}

const handleCreateAccount = async () => {
    try {
        await createAccount()
        ElMessage.success('开户成功')
        fetchAccount()
    } catch (error) {
        console.error(error)
    }
}

const fetchPositions = async () => {
    try {
        const res = await getPositions()
        if (res.code === 200 && Array.isArray(res.data)) {
            positions.value = res.data
        } else {
             console.warn('Positions fetch empty or invalid', res)
             positions.value = []
        }
    } catch (e) {
        console.error(e)
        positions.value = []
    }
}

const fetchOrders = async () => {
    try {
        const res = await getOrders()
        if (res.code === 200 && Array.isArray(res.data)) {
            orders.value = res.data
        }
    } catch (e) {
        console.error(e)
    }
}

const fetchDeals = async () => {
    try {
        const res = await getDeals()
        if (res.code === 200 && Array.isArray(res.data)) {
            deals.value = res.data
        }
    } catch (e) {
        console.error(e)
    }
}

const refreshAccount = () => {
    fetchAccount()
}

const fetchStockPrice = async () => {
    if (!tradeForm.symbol) return
    try {
        // Use history API to get latest price as a workaround
        const res = await getStockHistory(tradeForm.symbol, 'D')
        if (Array.isArray(res.data) && res.data.length > 0) {
            // Sort by date desc to get latest
            const latest = res.data.sort((a, b) => new Date(b.date) - new Date(a.date))[0]
            if (latest) {
                currentPrice.value = latest.close
                tradeForm.price = latest.close // Auto fill price
            } else {
                currentPrice.value = '暂无报价'
            }
        } else {
            currentPrice.value = '暂无报价'
        }
    } catch (e) {
        console.error(e)
        currentPrice.value = '--'
    }
}

const handleTrade = async (direction) => {
    if (!account.value) return
    if (!tradeForm.symbol || !tradeForm.price || !tradeForm.quantity) {
        ElMessage.warning('请填写完整交易信息')
        return
    }

    try {
        await placeOrder({
            symbol: tradeForm.symbol,
            direction,
            price: tradeForm.price,
            quantity: tradeForm.quantity
        })
        ElMessage.success('委托提交成功')
        fetchOrders()
        refreshAccount() // Funds frozen
    } catch (error) {
        console.error(error)
    }
}

const getOrderStatusText = (status) => {
    const map = {
        1: '待定',
        2: '部分完成',
        3: '已完成',
        4: '已取消'
    }
    return map[status] || '未知'
}

const getOrderStatusType = (status) => {
    const map = {
        1: 'warning',
        2: 'primary',
        3: 'success',
        4: 'info'
    }
    return map[status] || ''
}

const handleCancel = (id) => {
    ElMessageBox.confirm('确定撤销该委托吗?', '提示').then(async () => {
        try {
            await cancelOrder(id)
            ElMessage.success('撤单成功')
            fetchOrders()
            refreshAccount()
        } catch (error) {
            console.error(error)
        }
    }).catch(() => { })
}

onMounted(() => {
    fetchAccount()
})
</script>

<style scoped>
.simulation-page {
    padding: 20px 0;
}

.mb-4 {
    margin-bottom: 20px;
}

.mt-4 {
    margin-top: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.account-summary {
    display: flex;
    justify-content: space-around;
    padding: 20px 0;
}

.summary-item {
    text-align: center;
}

.summary-item .label {
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
}

.summary-item .value {
    font-size: 24px;
    font-weight: bold;
}

.empty-account {
    text-align: center;
    padding: 40px 0;
}

.trade-btn {
    width: 100%;
}

.current-price {
    font-weight: bold;
    color: #f56c6c;
}
</style>