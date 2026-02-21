<template>
    <div class="watchlist-page">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span class="card-title">我的自选股</span>
                    <el-button type="primary" @click="handleAdd">添加自选</el-button>
                </div>
            </template>

            <el-table :data="watchlist" style="width: 100%" v-loading="loading">
                <el-table-column prop="symbol" label="代码" width="120" />
                <el-table-column prop="name" label="名称" width="150" />
                <el-table-column prop="currentPrice" label="当前价" align="right">
                    <template #default="scope">
                        <span :class="scope.row.changePercent > 0 ? 'text-up' : 'text-down'">
                            {{ scope.row.currentPrice }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="changePercent" label="涨跌幅" align="right">
                    <template #default="scope">
                        <span :class="scope.row.changePercent > 0 ? 'text-up' : 'text-down'">
                            {{ scope.row.changePercent > 0 ? '+' : '' }}{{ scope.row.changePercent }}%
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="followTime" label="关注时间" width="180" />
                <el-table-column label="操作" width="150" align="center">
                    <template #default="scope">
                        <el-button link type="primary"
                            @click="$router.push({ path: '/prediction', query: { symbol: scope.row.symbol } })">预测</el-button>
                        <el-button link type="danger" @click="handleRemove(scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>
    </div>
    
    <!-- Add Watchlist Dialog -->
    <el-dialog v-model="showAddDialog" title="添加自选股" width="400px">
        <el-select v-model="addSymbol" placeholder="搜索股票/代码" remote :remote-method="handleSearchStock"
            :loading="searchLoading" class="search-input" filterable clearable style="width: 100%;">
            <el-option v-for="item in searchResults" :key="item.symbol"
                :label="item.name + ' (' + item.symbol + ')'" :value="item.symbol" />
        </el-select>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="showAddDialog = false">取消</el-button>
                <el-button type="primary" @click="confirmAdd" :disabled="!addSymbol">确定</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWatchlist, addWatchlist, removeWatchlist, searchStock } from '../api/stock'
import { ElMessage, ElMessageBox } from 'element-plus'

const watchlist = ref([])
const loading = ref(false)
const showAddDialog = ref(false)
const addSymbol = ref('')
const searchLoading = ref(false)
const searchResults = ref([])

const fetchWatchlist = async () => {
    loading.value = true
    try {
        const res = await getWatchlist()
        watchlist.value = res.data
    } catch (error) {
        console.error(error)
    } finally {
        loading.value = false
    }
}

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

const handleAdd = () => {
    addSymbol.value = ''
    searchResults.value = []
    showAddDialog.value = true
}

const confirmAdd = async () => {
    if (!addSymbol.value) return
    try {
        await addWatchlist(addSymbol.value)
        ElMessage.success('添加成功')
        showAddDialog.value = false
        fetchWatchlist()
    } catch (error) {
        console.error(error)
        ElMessage.error(error.message || '添加失败')
    }
}

const handleRemove = (row) => {
    ElMessageBox.confirm(`确定要删除 ${row.name} (${row.symbol}) 吗？`, '提示', {
        type: 'warning'
    }).then(async () => {
        try {
            await removeWatchlist(row.symbol)
            ElMessage.success('删除成功')
            fetchWatchlist()
        } catch (error) {
            console.error(error)
        }
    }).catch(() => { })
}

onMounted(() => {
    fetchWatchlist()
})
</script>

<style scoped>
.watchlist-page {
    padding: 20px 0;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
</style>