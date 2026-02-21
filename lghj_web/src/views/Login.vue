<template>
    <div class="login-container">
        <div class="login-content">
            <div class="brand-section">
                <h1 class="brand-title">量股化金</h1>
                <p class="brand-slogan">智能风控系统 · 股市预测专家</p>
                <div class="brand-desc">
                    <p>以科技赋能金融初心</p>
                    <p>创新金融科技，践行社会责任</p>
                    <p>致力于为客户提供更有温度的金融服务</p>
                </div>
                <div class="brand-card">
                    <h3>智能决策系统</h3>
                    <p>支持全生命周期的风险决策系统</p>
                </div>
            </div>

            <div class="login-form-card">
                <h2 class="form-title">登录 量股化金</h2>
                <div class="title-line"></div>

                <el-form :model="loginForm" class="login-form" @submit.prevent="handleLogin">
                    <el-form-item label="用户名或邮箱地址" prop="username">
                        <el-input v-model="loginForm.username" placeholder="请输入用户名" />
                    </el-form-item>

                    <el-form-item label="密码" prop="password">
                        <div class="password-label-row">
                            <!-- Label is handled by form-item, but forgot password link needs to be here -->
                        </div>
                        <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
                        <div class="forgot-password">忘记密码?</div>
                    </el-form-item>

                    <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">
                        登录
                    </el-button>
                </el-form>
            </div>
        </div>

        <div class="copyright">
            Copyright(C) BANK OF CHINA(BOC) All Rights Reserved (Ref)
        </div>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const loginForm = reactive({
    username: 'admin',
    password: ''
})

const handleLogin = async () => {
    if (!loginForm.username || !loginForm.password) {
        ElMessage.warning('请输入用户名和密码')
        return
    }

    loading.value = true
    try {
        const res = await login(loginForm)
        // res.data contains token, id, username, userType, identityDesc, state
        const { token, ...userInfo } = res.data
        userStore.setToken(token)
        userStore.setUserInfo(userInfo)

        ElMessage.success('登录成功')
        if (userInfo.userType === 3) {
            router.push('/admin/dashboard')
        } else {
            router.push('/dashboard')
        }
    } catch (error) {
        console.error(error)
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.login-container {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
    position: relative;
    overflow: hidden;
}

/* Background decoration - Red shape on the right */
.login-container::before {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 40%;
    height: 100%;
    background-color: #D32F2F;
    /* Primary Red */
    z-index: 0;
    clip-path: polygon(20% 0, 100% 0, 100% 100%, 0% 100%);
}

.login-content {
    display: flex;
    width: 1000px;
    max-width: 90%;
    z-index: 1;
    justify-content: space-between;
    align-items: center;
}

.brand-section {
    flex: 1;
    padding-right: 50px;
    color: #333;
}

.brand-title {
    font-size: 32px;
    font-weight: bold;
    margin-bottom: 10px;
    color: #D32F2F;
}

.brand-slogan {
    font-size: 24px;
    margin-bottom: 30px;
    color: #333;
}

.brand-desc p {
    margin: 5px 0;
    color: #666;
    font-size: 14px;
}

.brand-card {
    margin-top: 40px;
    background: linear-gradient(135deg, #ff5252 0%, #D32F2F 100%);
    color: white;
    padding: 20px;
    border-radius: 8px;
    width: 300px;
    box-shadow: 0 4px 15px rgba(211, 47, 47, 0.3);
}

.brand-card h3 {
    margin: 0 0 5px 0;
    font-size: 18px;
}

.brand-card p {
    margin: 0;
    font-size: 12px;
    opacity: 0.9;
}

.login-form-card {
    width: 400px;
    background: white;
    padding: 40px;
    border-radius: 4px;
    /* Slightly rounded, almost sharp like the ref */
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.form-title {
    margin: 0 0 10px 0;
    font-size: 22px;
    color: #333;
}

.title-line {
    width: 40px;
    height: 3px;
    background-color: #D32F2F;
    margin-bottom: 30px;
}

.login-form .el-form-item {
    margin-bottom: 25px;
    display: block;
    /* Make label block */
}

/* Force label to be top aligned */
:deep(.el-form-item__label) {
    display: block;
    text-align: left;
    line-height: 1.5;
    margin-bottom: 8px;
    color: #333;
}

.forgot-password {
    text-align: right;
    font-size: 12px;
    color: #666;
    margin-top: 5px;
    cursor: pointer;
}

.login-btn {
    width: 100%;
    height: 40px;
    font-size: 16px;
    margin-top: 20px;
    background-color: #D32F2F;
    border-color: #D32F2F;
}

.login-btn:hover {
    background-color: #b71c1c;
    border-color: #b71c1c;
}

.copyright {
    position: absolute;
    bottom: 20px;
    left: 50px;
    color: #999;
    font-size: 12px;
    z-index: 1;
}
</style>