import request from '../utils/request'

// 搜索股票
export function searchStock(keyword) {
    return request({
        url: '/user/stock/search',
        method: 'get',
        params: { keyword }
    })
}

// 获取股票实时资讯
export function getRealtimeNews(symbol, recentN = 10) {
    return request({
        url: '/user/realtime/news',
        method: 'get',
        params: { symbol, recentN }
    })
}

// 获取股票分时数据 (Port 8001)
export function getRealtimeMinute(symbol) {
    return request({
        url: `/ml/minute/${symbol}`,
        method: 'get'
    })
}

// 预测未来30天股价 (Port 8001)
export function getPrediction(symbol) {
    return request({
        url: `/ml/predict/${symbol}`,
        method: 'get'
    })
}

// 获取股票历史数据 (Port 8002)
export function getStockHistory(symbol, period = 'D') {
    return request({
        url: '/user/stock/data',
        method: 'get',
        params: { symbol, period }
    })
}

// 导出预测数据Excel (Main Service)
export function getPredictionExcel(symbol) {
    return request({
        url: `/user/prediction/${symbol}/get_excel`,
        method: 'get',
        responseType: 'blob'
    })
}

// 自选股 - 添加
export function addWatchlist(symbol) {
    return request({
        url: '/user/optional/add',
        method: 'post',
        params: { symbol }
    })
}

// 自选股 - 删除
export function removeWatchlist(symbol) {
    return request({
        url: '/user/optional/remove',
        method: 'post',
        params: { symbol }
    })
}

// 自选股 - 列表
export function getWatchlist() {
    return request({
        url: '/user/optional/list',
        method: 'get'
    })
}
