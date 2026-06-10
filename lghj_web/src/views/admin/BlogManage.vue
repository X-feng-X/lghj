<template>
    <div class="blog-manage">
        <el-card>
            <div class="filter-container">
                <el-input v-model="queryParams.userId" placeholder="用户ID" style="width: 200px;" class="filter-item"
                    @keyup.enter="handleFilter" />
                <el-button type="primary" @click="handleFilter">搜索</el-button>
            </div>

            <el-table :data="list" v-loading="listLoading" border style="width: 100%; margin-top: 20px;">
                <el-table-column prop="id" label="博客ID" width="100" align="center" />
                <el-table-column prop="userId" label="用户ID" width="100" align="center" />
                <el-table-column prop="title" label="标题" align="center" />
                <el-table-column prop="content" label="内容" show-overflow-tooltip align="center" />
                <el-table-column prop="createTime" label="发布时间" width="160" align="center" />
                <el-table-column label="操作" width="150" align="center">
                    <template #default="{ row }">
                        <el-popconfirm title="确定删除该博客吗？" @confirm="handleDelete(row)">
                            <template #reference>
                                <el-button type="danger" size="small">删除</el-button>
                            </template>
                        </el-popconfirm>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-container">
                <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
                    :page-sizes="[10, 20, 30, 50]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBlogPage, deleteBlog } from '../../api/admin'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const listLoading = ref(true)
const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    userId: undefined
})

const getList = async () => {
    listLoading.value = true
    try {
        const res = await getBlogPage(queryParams)
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

const handleDelete = async (row) => {
    try {
        await deleteBlog(row.id)
        ElMessage.success('删除成功')
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
