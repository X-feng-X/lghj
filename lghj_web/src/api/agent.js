import request from '../utils/request'

export function queryAgentConfigs() {
  return request({
    url: '/v1/query_ai_agent_config_list',
    method: 'get',
    timeout: 30000
  })
}

export function createAgentSession(data) {
  return request({
    url: '/v1/create_session',
    method: 'post',
    data,
    timeout: 30000
  })
}

export function chatAgent(data) {
  return request({
    url: '/v1/chat',
    method: 'post',
    data,
    timeout: 180000
  })
}
