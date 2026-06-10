<template>
    <div class="user-manage">
        <el-card>
            <div class="filter-container">
                <el-input v-model="queryParams.username" placeholder="用户名" style="width: 200px;" class="filter-item"
                    @keyup.enter="handleFilter" />
                <el-input v-model="queryParams.phone" placeholder="手机号" style="width: 200px;" class="filter-item"
                    @keyup.enter="handleFilter" />
                <el-button type="primary" @click="handleFilter">搜索</el-button>
                <el-button type="primary" @click="handleCreate">添加用户</el-button>
            </div>

            <el-table :data="list" v-loading="listLoading" style="width: 100%; margin-top: 20px;" border>
                <el-table-column prop="id" label="ID" width="80" align="center" />
                <el-table-column prop="username" label="用户名" align="center" />
                <el-table-column prop="name" label="姓名" align="center" />
                <el-table-column prop="phone" label="手机号" align="center" />
                <el-table-column prop="sex" label="性别" align="center">
                    <template #default="{ row }">
                        {{ row.sex === '1' ? '男' : '女' }}
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="注册时间" width="160" align="center" />
                <el-table-column label="操作" width="200" align="center">
                    <template #default="{ row }">
                        <el-button type="primary" size="small" @click="handleUpdate(row)">编辑</el-button>
                        <el-button :type="row.status === 1 ? 'danger' : 'success'" size="small"
                            @click="handleStatusChange(row)">
                            {{ row.status === 1 ? '禁用' : '启用' }}
                        </el-button>
                        <el-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(row)">
                            <template #reference>
                                <el-button type="danger" size="small">删除</el-button>
                            </template>
                        </el-popconfirm>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-container">
                <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
                    :page-sizes="[10, 20, 30, 50]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" />
            </div>
        </el-card>

        <!-- Dialog for Create/Update -->
        <el-dialog :title="dialogStatus === 'create' ? '添加用户' : '编辑用户'" v-model="dialogFormVisible">
            <el-form ref="dataForm" :model="temp" :rules="rules" label-width="100px">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="temp.username" />
                </el-form-item>
                <el-form-item label="姓名" prop="name">
                    <el-input v-model="temp.name" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                    <el-input v-model="temp.phone" />
                </el-form-item>
                <el-form-item label="性别" prop="sex">
                    <el-select v-model="temp.sex" placeholder="请选择">
                        <el-option label="男" value="1" />
                        <el-option label="女" value="0" />
                    </el-select>
                </el-form-item>
                <el-form-item label="身份证号" prop="idNumber">
                    <el-input v-model="temp.idNumber" />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogFormVisible = false">取消</el-button>
                    <el-button type="primary"
                        @click="dialogStatus === 'create' ? createData() : updateData()">确认</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserPage, addUser, updateUser, deleteUser, changeUserStatus } from '../../api/admin'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const listLoading = ref(true)
const queryParams = reactive({
    current: 1,
    size: 10,
    username: undefined,
    phone: undefined
})

const dialogFormVisible = ref(false)
const dialogStatus = ref('')
const temp = reactive({
    id: undefined,
    username: '',
    name: '',
    phone: '',
    sex: '1',
    idNumber: ''
})

const rules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const getList = async () => {
    listLoading.value = true
    try {
        const res = await getUserPage(queryParams)
        list.value = res.data.list
        total.value = res.data.total
    } catch (e) {
        console.error(e)
    } finally {
        listLoading.value = false
    }
}

const handleFilter = () => {
    queryParams.current = 1
    getList()
}

const handleSizeChange = (val) => {
    queryParams.size = val
    getList()
}

const handleCurrentChange = (val) => {
    queryParams.current = val
    getList()
}

const resetTemp = () => {
    temp.id = undefined
    temp.username = ''
    temp.name = ''
    temp.phone = ''
    temp.sex = '1'
    temp.idNumber = ''
}

const handleCreate = () => {
    resetTemp()
    dialogStatus.value = 'create'
    dialogFormVisible.value = true
}

const createData = async () => {
    try {
        await addUser(temp)
        dialogFormVisible.value = false
        ElMessage.success('添加成功')
        getList()
    } catch (e) {
        console.error(e)
    }
}

const handleUpdate = (row) => {
    temp.id = row.id
    temp.username = row.username
    temp.name = row.name
    temp.phone = row.phone
    temp.sex = row.sex
    temp.idNumber = row.idNumber
    dialogStatus.value = 'update'
    dialogFormVisible.value = true
}

const updateData = async () => {
    try {
        await updateUser(temp)
        dialogFormVisible.value = false
        ElMessage.success('更新成功')
        getList()
    } catch (e) {
        console.error(e)
    }
}

const handleStatusChange = async (row) => {
    const newStatus = row.status === 1 ? 0 : 1
    try {
        await changeUserStatus(row.id, newStatus)
        ElMessage.success('状态更新成功')
        getList()
    } catch (e) {
        console.error(e)
    }
}

const handleDelete = async (row) => {
    try {
        await deleteUser(row.id)
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
