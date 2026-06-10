<template>
    <div class="admin-layout">
        <el-container>
            <el-aside width="200px" class="aside">
                <div class="logo">
                    <span class="logo-icon">🛠️</span>
                    <span class="logo-text">后台管理</span>
                </div>
                <el-menu :default-active="activeMenu" class="el-menu-vertical" router background-color="#304156"
                    text-color="#bfcbd9" active-text-color="#409EFF">
                    <el-menu-item index="/admin/dashboard">
                        <el-icon>
                            <DataBoard />
                        </el-icon>
                        <span>仪表盘</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/user">
                        <el-icon>
                            <User />
                        </el-icon>
                        <span>用户管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/trade">
                        <el-icon>
                            <Money />
                        </el-icon>
                        <span>交易管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/stock">
                        <el-icon>
                            <TrendCharts />
                        </el-icon>
                        <span>股票管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/blog">
                        <el-icon>
                            <ChatDotSquare />
                        </el-icon>
                        <span>博客管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/comment">
                        <el-icon>
                            <Comment />
                        </el-icon>
                        <span>评论管理</span>
                    </el-menu-item>
                </el-menu>
            </el-aside>
            <el-container>
                <el-header class="header">
                    <div class="header-left">
                        <!-- Breadcrumb could go here -->
                    </div>
                    <div class="header-right">
                        <el-dropdown trigger="click" @command="handleCommand">
                            <span class="el-dropdown-link">
                                {{ userStore.userInfo.username || '管理员' }}
                                <el-icon class="el-icon--right"><arrow-down /></el-icon>
                            </span>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item command="home">返回前台</el-dropdown-item>
                                    <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </el-header>
                <el-main class="main">
                    <router-view />
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { logout } from '../api/auth'
import { ElMessage } from 'element-plus'
import {
    DataBoard,
    User,
    Money,
    TrendCharts,
    ChatDotSquare,
    Comment,
    ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleCommand = async (command) => {
    if (command === 'logout') {
        try {
            await logout()
        } catch (e) {
            console.error(e)
        }
        userStore.logout()
        router.push('/login')
        ElMessage.success('已退出登录')
    } else if (command === 'home') {
        router.push('/')
    }
}
</script>

<style scoped>
.admin-layout {
    height: 100vh;
    display: flex;
}

.aside {
    background-color: #304156;
    color: #fff;
    display: flex;
    flex-direction: column;
}

.logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    font-weight: bold;
    color: #fff;
    background-color: #2b3649;
}

.logo-icon {
    margin-right: 8px;
}

.el-menu-vertical {
    border-right: none;
    flex: 1;
}

.header {
    background-color: #fff;
    border-bottom: 1px solid #e6e6e6;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    height: 60px;
}

.header-right {
    cursor: pointer;
}

.main {
    background-color: #f0f2f5;
    padding: 20px;
}
</style>
