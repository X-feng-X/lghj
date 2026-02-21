<template>
    <div class="trade-manage">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
            <el-tab-pane label="委托单" name="order">
                <div class="filter-container">
                    <el-input v-model="orderQuery.symbol" placeholder="股票代码" style="width: 200px;" class="filter-item"
                        @keyup.enter="handleOrderFilter" />
                    <el-input v-model="orderQuery.userId" placeholder="用户ID" style="width: 200px;" class="filter-item"
                        @keyup.enter="handleOrderFilter" />
                    <el-button type="primary" @click="handleOrderFilter">搜索</el-button>
                </div>

                <el-table :data="orderList" v-loading="orderLoading" border style="width: 100%; margin-top: 20px;">
                    <el-table-column prop="id" label="订单ID" width="100" align="center" />
                    <el-table-column prop="userId" label="用户ID" width="100" align="center" />
                    <el-table-column prop="code" label="股票代码" align="center" />
                    <el-table-column prop="name" label="股票名称" align="center" />
                    <el-table-column prop="direction" label="方向" align="center">
                        <template #default="{ row }">
                            <el-tag :type="row.direction === 1 ? 'danger' : 'success'">
                                {{ row.direction === 1 ? '买入' : '卖出' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="type" label="类型" align="center">
                        <template #default="{ row }">
                            {{ row.type === 1 ? '限价' : '市价' }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="price" label="委托价格" align="center" />
                    <el-table-column prop="count" label="委托数量" align="center" />
                    <el-table-column prop="status" label="状态" align="center">
                        <template #default="{ row }">
                            <el-tag :type="statusType(row.status)">
                                {{ statusText(row.status) }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="委托时间" width="160" align="center" />
                </el-table>
                <div class="pagination-container">
                    <el-pagination v-model:current-page="orderQuery.pageNum" v-model:page-size="orderQuery.pageSize"
                        :page-sizes="[10, 20, 30, 50]" layout="total, sizes, prev, pager, next, jumper"
                        :total="orderTotal" @size-change="handleOrderSizeChange"
                        @current-change="handleOrderCurrentChange" />
                </div>
            </el-tab-pane>

            <el-tab-pane label="成交记录" name="deal">
                <div class="filter-container">
                    <el-input v-model="dealQuery.symbol" placeholder="股票代码" style="width: 200px;" class="filter-item"
                        @keyup.enter="handleDealFilter" />
                    <el-input v-model="dealQuery.userId" placeholder="用户ID" style="width: 200px;" class="filter-item"
                        @keyup.enter="handleDealFilter" />
                    <el-button type="primary" @click="handleDealFilter">搜索</el-button>
                </div>

                <el-table :data="dealList" v-loading="dealLoading" border style="width: 100%; margin-top: 20px;">
                    <el-table-column prop="id" label="成交ID" width="100" align="center" />
                    <el-table-column prop="userId" label="用户ID" width="100" align="center" />
                    <el-table-column prop="stockCode" label="股票代码" align="center" />
                    <el-table-column prop="stockName" label="股票名称" align="center" />
                    <el-table-column prop="direction" label="方向" align="center">
                        <template #default="{ row }">
                            <el-tag :type="row.direction === 1 ? 'danger' : 'success'">
                                {{ row.direction === 1 ? '买入' : '卖出' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="dealPrice" label="成交价格" align="center" />
                    <el-table-column prop="dealCount" label="成交数量" align="center" />
                    <el-table-column prop="createTime" label="成交时间" width="160" align="center" />
                </el-table>
                <div class="pagination-container">
                    <el-pagination v-model:current-page="dealQuery.pageNum" v-model:page-size="dealQuery.pageSize"
                        :page-sizes="[10, 20, 30, 50]" layout="total, sizes, prev, pager, next, jumper"
                        :total="dealTotal" @size-change="handleDealSizeChange"
                        @current-change="handleDealCurrentChange" />
                </div>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getOrderPage, getDealPage } from '../../api/admin'

const activeTab = ref('order')

// Order
const orderList = ref([])
const orderTotal = ref(0)
const orderLoading = ref(false)
const orderQuery = reactive({
    pageNum: 1,
    pageSize: 10,
    userId: undefined,
    symbol: undefined
})

// Deal
const dealList = ref([])
const dealTotal = ref(0)
const dealLoading = ref(false)
const dealQuery = reactive({
    pageNum: 1,
    pageSize: 10,
    userId: undefined,
    symbol: undefined
})

const statusText = (status) => {
    const map = {
        0: '未成交',
        1: '部分成交',
        2: '全部成交',
        3: '已撤单',
        4: '撤单中'
    }
    return map[status] || '未知'
}

const statusType = (status) => {
    const map = {
        0: 'info',
        1: 'warning',
        2: 'success',
        3: 'danger',
        4: 'warning'
    }
    return map[status] || 'info'
}

const getOrderList = async () => {
    orderLoading.value = true
    try {
        const res = await getOrderPage(orderQuery)
        orderList.value = res.data.records
        orderTotal.value = res.data.total
    } catch (e) {
        console.error(e)
    } finally {
        orderLoading.value = false
    }
}

const getDealList = async () => {
    dealLoading.value = true
    try {
        const res = await getDealPage(dealQuery)
        dealList.value = res.data.records
        dealTotal.value = res.data.total
    } catch (e) {
        console.error(e)
    } finally {
        dealLoading.value = false
    }
}

const handleTabClick = (tab) => {
    if (tab.props.name === 'order') {
        getOrderList()
    } else {
        getDealList()
    }
}

const handleOrderFilter = () => {
    orderQuery.pageNum = 1
    getOrderList()
}

const handleOrderSizeChange = (val) => {
    orderQuery.pageSize = val
    getOrderList()
}

const handleOrderCurrentChange = (val) => {
    orderQuery.pageNum = val
    getOrderList()
}

const handleDealFilter = () => {
    dealQuery.pageNum = 1
    getDealList()
}

const handleDealSizeChange = (val) => {
    dealQuery.pageSize = val
    getDealList()
}

const handleDealCurrentChange = (val) => {
    dealQuery.pageNum = val
    getDealList()
}

onMounted(() => {
    getOrderList()
})
</script>

<style scoped>
.filter-container {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>
