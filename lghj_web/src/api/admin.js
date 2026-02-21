import request from '../utils/request'

// --- 用户管理 ---

// 分页查询用户
export function getUserPage(params) {
    return request({
        url: '/admin/user',
        method: 'get',
        params
    })
}

// 新增用户
export function addUser(data) {
    return request({
        url: '/admin/user/add',
        method: 'post',
        data
    })
}

// 删除用户
export function deleteUser(id) {
    return request({
        url: `/admin/user/${id}`,
        method: 'delete'
    })
}

// 更新用户
export function updateUser(data) {
    return request({
        url: '/admin/user/update',
        method: 'put',
        data
    })
}

// 启用/禁用用户
export function changeUserStatus(id, status) {
    return request({
        url: `/admin/user/changeStatus/${id}`,
        method: 'post',
        params: { status }
    })
}

// --- 交易管理 ---

// 分页查询委托单
export function getOrderPage(params) {
    return request({
        url: '/admin/trade/order/page',
        method: 'get',
        params
    })
}

// 分页查询成交记录
export function getDealPage(params) {
    return request({
        url: '/admin/trade/deal/page',
        method: 'get',
        params
    })
}

// --- 博客管理 ---

// 分页查询博客
export function getBlogPage(params) {
    return request({
        url: '/admin/blog/page',
        method: 'get',
        params
    })
}

// 删除博客
export function deleteBlog(id) {
    return request({
        url: `/admin/blog/${id}`,
        method: 'delete'
    })
}

// --- 评论管理 ---

// 分页查询评论
export function getCommentPage(params) {
    return request({
        url: '/admin/blog/comments/page',
        method: 'get',
        params
    })
}

// 删除评论
export function deleteComment(id) {
    return request({
        url: `/admin/blog/comments/${id}`,
        method: 'delete'
    })
}

// --- 股票管理 ---

// 分页查询股票
export function getStockPage(params) {
    return request({
        url: '/admin/stock/page',
        method: 'get',
        params
    })
}

// 更新股票信息
export function updateStock(data) {
    return request({
        url: '/admin/stock/update',
        method: 'put',
        data
    })
}

// 导入股票数据
export function importStockData() {
    return request({
        url: '/admin/stock/import',
        method: 'post'
    })
}

// 同步ES数据
export function syncStockToEs() {
    return request({
        url: '/admin/stock/sync-es',
        method: 'post'
    })
}

// 批量更新
export function batchUpdateStock() {
    return request({
        url: '/admin/stock/batch-update',
        method: 'post'
    })
}
