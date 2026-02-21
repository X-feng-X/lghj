import request from '../utils/request'

// 关注和取关
export function followUser(id, isFollow) {
    return request({
        url: `/user/follow/${id}/${isFollow}`,
        method: 'put'
    })
}

// 判断是否关注
export function checkFollow(id) {
    return request({
        url: `/user/follow/or/not/${id}`,
        method: 'get'
    })
}
