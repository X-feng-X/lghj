<template>
    <div class="stock-manage">
        <el-card>
            <div class="filter-container">
                <el-input v-model="queryParams.keyword" placeholder="股票代码/名称" style="width: 200px;" class="filter-item"
                    @keyup.enter="handleFilter" />
                <el-button type="primary" @click="handleFilter">搜索</el-button>
                <el-button type="success" @click="handleImport" :loading="importLoading">导入A股基础信息</el-button>
                <el-button type="warning" @click="handleSync" :loading="syncLoading">同步ES数据</el-button>
                <el-button type="info" @click="handleBatchUpdate" :loading="batchUpdateLoading">批量更新</el-button>
            </div>

            <el-table :data="list" v-loading="listLoading" border style="width: 100%; margin-top: 20px;">
                <el-table-column prop="tsCode" label="TS代码" width="100" align="center" />
                <el-table-column prop="symbol" label="股票代码" width="100" align="center" />
                <el-table-column prop="name" label="股票名称" align="center" />
                <el-table-column prop="area" label="地域" align="center" />
                <el-table-column prop="industry" label="行业" align="center" />
                <el-table-column prop="market" label="市场" align="center" />
                <el-table-column prop="listDate" label="上市日期" align="center" />
                <el-table-column label="操作" width="150" align="center">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleUpdate(row)">编辑</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-container">
                <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
                    :page-sizes="[10, 20, 30, 50]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" />
            </div>
        </el-card>

        <!-- Dialog for Update -->
        <el-dialog title="编辑股票信息" v-model="dialogFormVisible">
            <el-form ref="dataForm" :model="temp" label-width="100px">
                <el-form-item label="股票代码">
                    <el-input v-model="temp.symbol" disabled />
                </el-form-item>
                <el-form-item label="股票名称">
                    <el-input v-model="temp.name" />
                </el-form-item>
                <el-form-item label="地域">
                    <el-input v-model="temp.area" />
                </el-form-item>
                <el-form-item label="行业">
                    <el-input v-model="temp.industry" />
                </el-form-item>
                <el-form-item label="市场">
                    <el-input v-model="temp.market" />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogFormVisible = false">取消</el-button>
                    <el-button type="primary" @click="updateData">确认</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStockPage, updateStock, importStockData, syncStockToEs, batchUpdateStock } from '../../api/admin'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const listLoading = ref(true)
const importLoading = ref(false)
const syncLoading = ref(false)
const batchUpdateLoading = ref(false)

const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    keyword: undefined
})

const dialogFormVisible = ref(false)
const temp = reactive({
    tsCode: '',
    symbol: '',
    name: '',
    area: '',
    industry: '',
    market: ''
})

const getList = async () => {
    listLoading.value = true
    try {
        const res = await getStockPage(queryParams)
        list.value = res.data.records
        total.value = res.data.total
    } catch (e) {
        console.error(e)
    } finally {
        listLoading.value = false
    }
}

const handleFilter = () => {
    queryParams.pageNum = 1
    getList()
}

const handleSizeChange = (val) => {
    queryParams.pageSize = val
    getList()
}

const handleCurrentChange = (val) => {
    queryParams.pageNum = val
    getList()
}

const handleImport = async () => {
    importLoading.value = true
    try {
        await importStockData()
        ElMessage.success('导入任务已提交')
    } catch (e) {
        console.error(e)
    } finally {
        importLoading.value = false
    }
}

const handleSync = async () => {
    syncLoading.value = true
    try {
        await syncStockToEs()
        ElMessage.success('同步任务已提交')
    } catch (e) {
        console.error(e)
    } finally {
        syncLoading.value = false
    }
}

const handleBatchUpdate = async () => {
    batchUpdateLoading.value = true
    try {
        await batchUpdateStock()
        ElMessage.success('批量更新任务已提交')
    } catch (e) {
        console.error(e)
    } finally {
        batchUpdateLoading.value = false
    }
}

const handleUpdate = (row) => {
    Object.assign(temp, row)
    dialogFormVisible.value = true
}

const updateData = async () => {
    try {
        await updateStock(temp)
        dialogFormVisible.value = false
        ElMessage.success('更新成功')
        getList()
    } catch (e) {
        console.error(e)
    }
}

onMounted(() => {
    getList()
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
