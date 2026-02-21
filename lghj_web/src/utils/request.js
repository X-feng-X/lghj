import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 5000
})

// Request interceptor
service.interceptors.request.use(
    config => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers['token'] = userStore.token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// Response interceptor
service.interceptors.response.use(
    response => {
        // Check if response is blob
        if (response.config.responseType === 'blob') {
            return response.data
        }

        const res = response.data

        // Special handling for Python service (returns Array directly)
        if (Array.isArray(res)) {
            return { code: 200, data: res, msg: 'success' }
        }

        // Special handling for Prediction service (returns object without code field but with symbol and predictions)
        if (res && res.symbol && Array.isArray(res.predictions)) {
            return { code: 200, data: res, msg: 'success' }
        }

        // 200 means success (some APIs use 1 for success)
        if (res.code === 200 || res.code === 1) {
            return res
        } else {
            ElMessage.error(res.msg || 'Error')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    error => {
        ElMessage.error(error.message || 'Request Error')
        return Promise.reject(error)
    }
)

export default service
