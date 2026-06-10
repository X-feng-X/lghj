import request from '../utils/request'

// 创建模拟账户
export function createAccount() {
    return request({
        url: '/user/account/create',
        method: 'post'
    })
}

// 获取账户信息
export function getAccountInfo() {
    return request({
        url: '/user/account/query_info',
        method: 'get'
    })
}

// 获取持仓列表
export function getPositions() {
    return request({
        url: '/user/account/query_positions',
        method: 'get'
    })
}

// 获取特定股票持仓
export function getPosition(symbol) {
    return request({
        url: '/user/account/query_position',
        method: 'get',
        params: { symbol }
    })
}

// 创建委托单(下单)
export function placeOrder(params) {
    return request({
        url: '/user/trade/order',
        method: 'post',
        params // symbol, direction, price, quantity
    })
}

// 撤销委托单
export function cancelOrder(orderId) {
    return request({
        url: '/user/trade/cancel',
        method: 'post',
        params: { orderId }
    })
}

// 获取用户委托单列表
export function getOrders() {
    return request({
        url: '/user/trade/query_orders',
        method: 'get'
    })
}

// 获取用户成交记录列表
export function getDeals() {
    return request({
        url: '/user/trade/query_deals',
        method: 'get'
    })
}
