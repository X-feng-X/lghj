<template>
    <div class="main-layout">
        <el-header class="app-header">
            <div class="header-content">
                <div class="logo">
                    <span class="logo-icon">📈</span>
                    <span class="logo-text">量股化金</span>
                </div>

                <el-menu :default-active="activeMenu" class="nav-menu" mode="horizontal" :ellipsis="false" router>
                    <el-menu-item index="/dashboard">首页</el-menu-item>
                    <el-menu-item index="/market">行情中心</el-menu-item>
                    <el-menu-item index="/prediction">智能预测</el-menu-item>
                    <el-menu-item index="/watchlist">自选股</el-menu-item>
                    <el-menu-item index="/community">股友社区</el-menu-item>
                    <el-menu-item index="/news">财经资讯</el-menu-item>
                    <el-menu-item index="/simulation">模拟交易</el-menu-item>
                </el-menu>

                <div class="header-right">
                    <el-select v-model="searchQuery" placeholder="搜索股票/代码" remote :remote-method="handleSearch"
                        :loading="searchLoading" class="search-input" filterable clearable @change="handleSelectStock">
                        <el-option v-for="item in searchResults" :key="item.symbol"
                            :label="item.name + ' (' + item.symbol + ')'" :value="item.symbol" />
                    </el-select>

                    <el-dropdown trigger="click" @command="handleCommand">
                        <div class="user-profile">
                            <el-avatar :size="32"
                                src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                            <span class="username">{{ userStore.userInfo.username || 'User' }}</span>
                        </div>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </div>
        </el-header>

        <el-main class="app-content">
            <router-view v-slot="{ Component, route: r }">
                <transition name="fade" mode="out-in">
                    <keep-alive include="Community,Simulation">
                        <component :is="Component" :key="r.fullPath" />
                    </keep-alive>
                </transition>
            </router-view>
        </el-main>

        <!-- Profile Drawer -->
        <el-drawer v-model="profileDrawerVisible" title="个人中心" direction="rtl" size="400px">
            <div class="profile-content">
                <div class="profile-header">
                    <el-avatar :size="80" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                    <h3 class="profile-name">{{ userStore.userInfo.username || 'User' }}</h3>
                    <p class="profile-bio">股市风云变幻，唯有策略长存。</p>
                </div>

                <el-divider />

                <div class="profile-stats">
                    <div class="stat-item">
                        <div class="stat-value">12</div>
                        <div class="stat-label">发布</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value">56</div>
                        <div class="stat-label">关注</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value">128</div>
                        <div class="stat-label">粉丝</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value">324</div>
                        <div class="stat-label">获赞</div>
                    </div>
                </div>

                <el-divider />

                <div class="profile-menu">
                    <el-menu default-active="1" class="el-menu-vertical-demo">
                        <el-menu-item index="1">
                            <el-icon>
                                <Document />
                            </el-icon>
                            <span>我的博客</span>
                        </el-menu-item>
                        <el-menu-item index="2">
                            <el-icon>
                                <Star />
                            </el-icon>
                            <span>我的收藏</span>
                        </el-menu-item>
                        <el-menu-item index="3">
                            <el-icon>
                                <Setting />
                            </el-icon>
                            <span>账号设置</span>
                        </el-menu-item>
                    </el-menu>
                </div>
            </div>
        </el-drawer>

        <el-footer class="app-footer">
            <p>Copyright © 2026 量股化金 - 智能股市预测系统</p>
        </el-footer>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Document, Star, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { logout } from '../api/auth'
import { searchStock } from '../api/stock'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const searchQuery = ref('')
const searchLoading = ref(false)
const searchResults = ref([])
const profileDrawerVisible = ref(false)

const activeMenu = computed(() => {
    // If path is /community, return /community
    if (route.path.startsWith('/community')) {
        return '/community'
    }
    // If path is /simulation, return /simulation
    if (route.path.startsWith('/simulation')) {
        return '/simulation'
    }
    // Default behavior
    return route.path
})

const handleSearch = async (query) => {
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

const handleSelectStock = (symbol) => {
    if (symbol) {
        // Navigate to prediction or market detail page
        // For now, let's navigate to Market page with query param as requested
        router.push({ path: '/market', query: { symbol } })
        searchQuery.value = '' // clear input
    }
}

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
    } else if (command === 'profile') {
        profileDrawerVisible.value = true
    }
}
</script>

<style scoped>
.main-layout {
    min-height: 100vh;
    background-color: var(--color-bg);
    display: flex;
    flex-direction: column;
}

.app-header {
    background-color: white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    height: 60px;
    position: sticky;
    top: 0;
    z-index: 1000;
    padding: 0;
}

.header-content {
    max-width: 1400px;
    /* Wide container */
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
}

.logo {
    display: flex;
    align-items: center;
    font-size: 20px;
    font-weight: bold;
    color: var(--el-color-primary);
    margin-right: 40px;
}

.logo-icon {
    margin-right: 8px;
    font-size: 24px;
}

.nav-menu {
    flex: 1;
    border-bottom: none !important;
    background: transparent;
}

.nav-menu .el-menu-item {
    font-size: 16px;
    color: #333;
    transition: all 0.3s;
}

.nav-menu .el-menu-item:hover {
    color: var(--el-color-primary);
    background-color: transparent !important;
}

.nav-menu .el-menu-item.is-active {
    color: var(--el-color-primary) !important;
    border-bottom: 2px solid var(--el-color-primary) !important;
    font-weight: 500;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 20px;
}

.search-input {
    width: 250px;
}

.user-profile {
    display: flex;
    align-items: center;
    cursor: pointer;
    gap: 8px;
}

.username {
    font-size: 14px;
    color: #666;
}

.app-content {
    flex: 1;
    width: 100%;
    max-width: 1400px;
    margin: 0 auto;
    padding: 20px;
    box-sizing: border-box;
}

.app-footer {
    background-color: #f0f2f5;
    text-align: center;
    color: #999;
    padding: 20px;
    font-size: 12px;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

.profile-content {
    padding: 20px;
    text-align: center;
}

.profile-header {
    margin-bottom: 20px;
}

.profile-name {
    margin: 10px 0 5px;
    font-size: 20px;
    color: #333;
}

.profile-bio {
    color: #999;
    font-size: 14px;
    margin: 0;
}

.profile-stats {
    display: flex;
    justify-content: space-around;
    padding: 10px 0;
}

.stat-item {
    text-align: center;
}

.stat-value {
    font-size: 18px;
    font-weight: bold;
    color: #333;
}

.stat-label {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
}

.profile-menu {
    text-align: left;
}
</style>
