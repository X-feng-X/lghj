import request from '../utils/request'

// 发布博客
export function publishBlog(data) {
    // Backend expects 'context' field instead of 'content'
    const payload = {
        ...data,
        context: data.content
    }
    delete payload.content
    
    return request({
        url: '/user/blog',
        method: 'post',
        data: payload
    })
}

// 点赞博客
export function likeBlog(id) {
    return request({
        url: `/user/blog/like/${id}`,
        method: 'put'
    })
}

// 分页查看登录用户自己的博客内容
export function getMyBlogs(current = 1, size = 10) {
    return request({
        url: '/user/blog/query/of/me',
        method: 'get',
        params: { current, size }
    })
}

// 查询热门博客
export function getHotBlogs(current = 1, size = 10) {
    return request({
        url: '/user/blog/query/hot',
        method: 'get',
        params: { current, size }
    })
}

// 根据id查询博客
export function getBlogDetail(id) {
    return request({
        url: `/user/blog/query/${id}`,
        method: 'get'
    })
}

// 查看指定用户发的博客
export function getUserBlogs(id, current = 1, size = 10) {
    return request({
        url: '/user/blog/query/of/user',
        method: 'get',
        params: { id, current, size }
    })
}

// 粉丝查看关注所有用户博客接口
export function getFollowedBlogs(current = 1, size = 10) {
    return request({
        url: '/user/blog/query/of/follow',
        method: 'get',
        params: { current, size }
    })
}

// 用户删除自己的博客
export function deleteBlog(id) {
    return request({
        url: `/user/blog/delete/${id}`,
        method: 'delete'
    })
}

// 用户编辑自己的博客
export function updateBlog(data) {
    return request({
        url: '/user/blog/update',
        method: 'put',
        data // id, title, content
    })
}

// 新增博客评论（一级/二级通用）
export function addComment(data) {
    return request({
        url: '/user/blog/comments/add',
        method: 'post',
        data // blogId, userId, content, parentId
    })
}

// 查询博客评论列表（带二级评论，树形结构）
export function getComments(blogId, pageNum = 1, pageSize = 10) {
    return request({
        url: '/user/blog/comments/list',
        method: 'get',
        params: { blogId, pageNum, pageSize }
    })
}

// 评论点赞/取消点赞
export function likeComment(commentId) {
    return request({
        url: `/user/blog/comments/like/${commentId}`,
        method: 'post'
    })
}

// 删除评论（逻辑删除）
export function deleteComment(commentId) {
    return request({
        url: `/user/blog/comments/delete/${commentId}`,
        method: 'delete'
    })
}
