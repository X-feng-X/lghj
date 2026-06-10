<template>
  <div class="prediction-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">Agent 智能投资顾问</span>
          <div class="header-actions">
            <el-select
              v-model="selectedAgentId"
              placeholder="选择智能体"
              :loading="agentLoading"
              class="agent-select"
              filterable
              @change="handleAgentChange"
            >
              <el-option
                v-for="agent in agents"
                :key="agent.agentId"
                :label="agent.agentName"
                :value="agent.agentId"
              />
            </el-select>
            <el-button
              type="primary"
              plain
              :icon="Refresh"
              :disabled="!selectedAgentId"
              :loading="sessionLoading"
              @click="handleNewSession"
            >
              新建会话
            </el-button>
          </div>
        </div>
      </template>

      <el-alert
        v-if="agentInitError"
        :title="agentInitError"
        type="warning"
        show-icon
        :closable="false"
        class="agent-alert"
      />

      <div class="advisor-content">
        <section class="chat-section">
          <div class="chat-meta">
            <div>
              <h3>{{ selectedAgent?.agentName || '智能投资顾问' }}</h3>
              <p>{{ selectedAgent?.agentDesc || '结合行情、模拟交易画像和风险约束，提供投资分析与观察建议。' }}</p>
            </div>
            <el-tag v-if="sessionId" type="success">会话已连接</el-tag>
            <el-tag v-else type="info">待建立会话</el-tag>
          </div>

          <div ref="messageListRef" class="message-list">
            <div v-for="message in messages" :key="message.id" class="chat-message" :class="message.role">
              <div class="message-avatar">
                <el-icon>
                  <User v-if="message.role === 'user'" />
                  <Service v-else />
                </el-icon>
              </div>
              <div class="message-bubble">
                <div class="message-role">{{ message.role === 'user' ? '我' : '投资顾问' }}</div>
                <div class="message-content markdown-body" v-html="renderMarkdown(message.content)"></div>
              </div>
            </div>

            <div v-if="advisorLoading" class="chat-message assistant">
              <div class="message-avatar">
                <el-icon><Service /></el-icon>
              </div>
              <div class="message-bubble">
                <div class="message-role">投资顾问</div>
                <div class="typing-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>

          <div class="composer">
            <el-input
              v-model="question"
              type="textarea"
              :rows="4"
              resize="none"
              maxlength="800"
              show-word-limit
              placeholder="输入你的投资问题，例如：请结合我的模拟交易记录分析当前持仓风险"
            />
            <div class="composer-actions">
              <span class="session-tip">
                {{ sessionId ? `Session: ${sessionId}` : '首次发送将自动创建会话' }}
              </span>
              <el-button
                type="primary"
                :icon="ChatLineRound"
                :loading="advisorLoading"
                :disabled="!question.trim() || !selectedAgentId"
                @click="handleSend"
              >
                发送咨询
              </el-button>
            </div>
          </div>
        </section>

        <aside class="context-section">
          <div class="context-panel conversation-panel">
            <div class="panel-title with-action">
              <span>会话记录</span>
              <el-button link type="primary" :icon="Refresh" :loading="sessionLoading" @click="handleNewSession">
                新建
              </el-button>
            </div>
            <div v-if="conversations.length" class="conversation-list">
              <div
                v-for="conversation in conversations"
                :key="conversation.localId"
                class="conversation-item"
                :class="{ active: conversation.localId === activeConversationId }"
              >
                <button type="button" class="conversation-main" @click="openConversation(conversation.localId)">
                  <span class="conversation-title">{{ conversation.title }}</span>
                  <span class="conversation-meta">
                    {{ formatConversationTime(conversation.updatedAt) }}
                    <em>{{ conversation.sessionId ? '已连接' : '草稿' }}</em>
                  </span>
                </button>
                <el-button
                  link
                  type="danger"
                  :icon="Delete"
                  class="conversation-delete"
                  @click.stop="handleDeleteConversation(conversation.localId)"
                />
              </div>
            </div>
            <div v-else class="empty-conversation">暂无历史会话</div>
          </div>

          <div class="context-panel">
            <div class="panel-title">
              <el-icon><Search /></el-icon>
              <span>股票上下文</span>
            </div>
            <el-select
              v-model="selectedSymbol"
              placeholder="搜索股票/代码"
              remote
              :remote-method="handleSearchStock"
              :loading="searchLoading"
              filterable
              clearable
              class="stock-select"
              @change="handleSelectStock"
            >
              <el-option
                v-for="item in searchResults"
                :key="item.symbol"
                :label="item.name + ' (' + item.symbol + ')'"
                :value="item.symbol"
              />
            </el-select>
            <div v-if="selectedSymbol" class="selected-stock">
              <span>已关联</span>
              <strong>{{ selectedSymbol }}</strong>
            </div>
          </div>

          <div class="context-panel">
            <div class="panel-title">
              <el-icon><ChatLineRound /></el-icon>
              <span>快捷问题</span>
            </div>
            <div class="prompt-list">
              <el-button
                v-for="prompt in quickPrompts"
                :key="prompt"
                text
                class="prompt-button"
                @click="applyPrompt(prompt)"
              >
                {{ prompt }}
              </el-button>
            </div>
          </div>

          <div class="risk-note">
            <strong>合规提示</strong>
            <p>顾问回复仅用于信息参考，不构成收益承诺或确定性证券投资建议。请结合自身风险承受能力独立判断。</p>
          </div>
        </aside>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ChatLineRound, Delete, Refresh, Search, Service, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryAgentConfigs, createAgentSession, chatAgent } from '../api/agent'
import { searchStock } from '../api/stock'
import { useUserStore } from '../stores/user'

const DEFAULT_AGENT_ID = 'investment-advisor'
const STORAGE_LIMIT = 30
const WELCOME_MESSAGE = '你好，我是量股化金 Agent 智能投资顾问。你可以咨询个股分析、持仓风险、模拟交易复盘或组合观察清单。'

const route = useRoute()
const userStore = useUserStore()

const agents = ref([])
const selectedAgentId = ref('')
const selectedSymbol = ref('')
const sessionId = ref('')
const question = ref('')
const messages = ref([
  {
    id: Date.now(),
    role: 'assistant',
    content: WELCOME_MESSAGE
  }
])
const conversations = ref([])
const activeConversationId = ref('')

const agentLoading = ref(false)
const sessionLoading = ref(false)
const advisorLoading = ref(false)
const searchLoading = ref(false)
const searchResults = ref([])
const agentInitError = ref('')
const messageListRef = ref(null)

const selectedAgent = computed(() => agents.value.find(agent => agent.agentId === selectedAgentId.value))
const userId = computed(() => {
  const info = userStore.userInfo || {}
  return String(info.id || info.username || 'guest')
})
const storageKey = computed(() => `lghj_agent_conversations_${userId.value}`)

const quickPrompts = computed(() => {
  if (selectedSymbol.value) {
    return [
      `请分析 ${selectedSymbol.value} 的基本面、技术面和主要风险`,
      `结合我的模拟交易画像，评估 ${selectedSymbol.value} 是否适合继续观察`,
      `为 ${selectedSymbol.value} 制定一份仓位纪律和风险观察清单`
    ]
  }

  return [
    '请结合我的模拟交易记录分析当前持仓风险',
    '帮我梳理一份本周组合观察清单',
    '从风险控制角度复盘我最近的交易行为'
  ]
})

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const createWelcomeMessage = (content = WELCOME_MESSAGE) => ({
  id: Date.now() + Math.random(),
  role: 'assistant',
  content
})

const createLocalConversation = (options = {}) => ({
  localId: `local-${Date.now()}-${Math.random().toString(16).slice(2)}`,
  sessionId: options.sessionId || '',
  agentId: selectedAgentId.value || DEFAULT_AGENT_ID,
  agentName: selectedAgent.value?.agentName || '智能投资顾问',
  selectedSymbol: selectedSymbol.value || '',
  title: options.title || '新建顾问会话',
  messages: options.messages || [createWelcomeMessage(options.welcome)],
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString()
})

const cloneMessages = () => messages.value.map(message => ({
  id: message.id,
  role: message.role,
  content: message.content
}))

const getConversationTitle = (items, fallback = '新建顾问会话') => {
  const firstUserMessage = items.find(item => item.role === 'user')?.content?.trim()
  if (!firstUserMessage) return fallback
  return firstUserMessage.length > 24 ? `${firstUserMessage.slice(0, 24)}...` : firstUserMessage
}

const persistConversations = () => {
  const payload = conversations.value.slice(0, STORAGE_LIMIT)
  localStorage.setItem(storageKey.value, JSON.stringify(payload))
}

const saveActiveConversation = () => {
  if (!activeConversationId.value) return

  const index = conversations.value.findIndex(item => item.localId === activeConversationId.value)
  if (index === -1) return

  const currentMessages = cloneMessages()
  const current = conversations.value[index]
  conversations.value[index] = {
    ...current,
    sessionId: sessionId.value,
    agentId: selectedAgentId.value || current.agentId,
    agentName: selectedAgent.value?.agentName || current.agentName,
    selectedSymbol: selectedSymbol.value,
    title: getConversationTitle(currentMessages, selectedSymbol.value ? `${selectedSymbol.value} 顾问会话` : current.title),
    messages: currentMessages,
    updatedAt: new Date().toISOString()
  }

  conversations.value.sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
  persistConversations()
}

const loadConversations = () => {
  try {
    const raw = localStorage.getItem(storageKey.value)
    const parsed = raw ? JSON.parse(raw) : []
    conversations.value = Array.isArray(parsed)
      ? parsed.map(item => ({
        localId: item.localId || `local-${Date.now()}-${Math.random().toString(16).slice(2)}`,
        sessionId: item.sessionId || '',
        agentId: item.agentId || DEFAULT_AGENT_ID,
        agentName: item.agentName || '智能投资顾问',
        selectedSymbol: item.selectedSymbol || '',
        title: item.title || '顾问会话',
        messages: Array.isArray(item.messages) && item.messages.length ? item.messages : [createWelcomeMessage()],
        createdAt: item.createdAt || new Date().toISOString(),
        updatedAt: item.updatedAt || item.createdAt || new Date().toISOString()
      })).sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
      : []
  } catch (error) {
    console.error(error)
    conversations.value = []
  }
}

const openConversation = (localId) => {
  const conversation = conversations.value.find(item => item.localId === localId)
  if (!conversation) return

  activeConversationId.value = conversation.localId
  selectedAgentId.value = conversation.agentId
  selectedSymbol.value = conversation.selectedSymbol || ''
  sessionId.value = conversation.sessionId || ''
  messages.value = conversation.messages.map(message => ({ ...message }))
  scrollToBottom()
}

const resetCurrentConversation = () => {
  activeConversationId.value = ''
  sessionId.value = ''
  selectedSymbol.value = ''
  question.value = ''
  messages.value = [createWelcomeMessage()]
  scrollToBottom()
}

const handleDeleteConversation = async (localId) => {
  const conversation = conversations.value.find(item => item.localId === localId)
  if (!conversation) return

  try {
    await ElMessageBox.confirm(`确定要删除会话“${conversation.title}”吗？`, '删除会话', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })

    const isActive = activeConversationId.value === localId
    conversations.value = conversations.value.filter(item => item.localId !== localId)
    persistConversations()

    if (isActive) {
      if (conversations.value.length) {
        openConversation(conversations.value[0].localId)
      } else {
        resetCurrentConversation()
      }
    }

    ElMessage.success('会话已删除')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error(error)
    }
  }
}

const ensureActiveConversation = () => {
  if (activeConversationId.value) return

  const conversation = createLocalConversation({
    sessionId: sessionId.value,
    messages: cloneMessages()
  })
  conversations.value.unshift(conversation)
  activeConversationId.value = conversation.localId
  persistConversations()
}

const formatConversationTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${month}/${day} ${hour}:${minute}`
}

const escapeHtml = (value = '') => String(value)
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;')

const renderInlineMarkdown = (text) => {
  let html = escapeHtml(text)
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/__([^_]+)__/g, '<strong>$1</strong>')
  html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  html = html.replace(/\[([^\]]+)\]\((https?:\/\/[^)\s]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
  return html
}

const renderMarkdown = (content = '') => {
  const lines = String(content).replace(/\r\n/g, '\n').split('\n')
  const html = []
  let inCodeBlock = false
  let codeLines = []
  let listType = ''

  const closeList = () => {
    if (listType) {
      html.push(`</${listType}>`)
      listType = ''
    }
  }

  const openList = (type) => {
    if (listType === type) return
    closeList()
    listType = type
    html.push(`<${type}>`)
  }

  lines.forEach(line => {
    const trimmed = line.trim()

    if (trimmed.startsWith('```')) {
      if (inCodeBlock) {
        html.push(`<pre><code>${escapeHtml(codeLines.join('\n'))}</code></pre>`)
        codeLines = []
        inCodeBlock = false
      } else {
        closeList()
        inCodeBlock = true
      }
      return
    }

    if (inCodeBlock) {
      codeLines.push(line)
      return
    }

    if (!trimmed) {
      closeList()
      return
    }

    const heading = trimmed.match(/^(#{1,4})\s+(.+)$/)
    if (heading) {
      closeList()
      const level = heading[1].length
      html.push(`<h${level}>${renderInlineMarkdown(heading[2])}</h${level}>`)
      return
    }

    const ordered = trimmed.match(/^\d+\.\s+(.+)$/)
    if (ordered) {
      openList('ol')
      html.push(`<li>${renderInlineMarkdown(ordered[1])}</li>`)
      return
    }

    const unordered = trimmed.match(/^[-*]\s+(.+)$/)
    if (unordered) {
      openList('ul')
      html.push(`<li>${renderInlineMarkdown(unordered[1])}</li>`)
      return
    }

    const quote = trimmed.match(/^>\s?(.+)$/)
    if (quote) {
      closeList()
      html.push(`<blockquote>${renderInlineMarkdown(quote[1])}</blockquote>`)
      return
    }

    closeList()
    html.push(`<p>${renderInlineMarkdown(line)}</p>`)
  })

  if (inCodeBlock) {
    html.push(`<pre><code>${escapeHtml(codeLines.join('\n'))}</code></pre>`)
  }
  closeList()

  return html.join('')
}

const pushMessage = (role, content) => {
  ensureActiveConversation()
  messages.value.push({
    id: Date.now() + Math.random(),
    role,
    content
  })
  saveActiveConversation()
  scrollToBottom()
}

const loadAgents = async () => {
  agentLoading.value = true
  agentInitError.value = ''
  try {
    const res = await queryAgentConfigs()
    agents.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error(error)
    agentInitError.value = '智能体列表获取失败，已使用默认投资顾问配置。请确认 agent 后端 8091 服务已启动。'
    agents.value = []
  } finally {
    if (agents.value.length === 0) {
      agents.value = [{
        agentId: DEFAULT_AGENT_ID,
        agentName: '智能投资顾问',
        agentDesc: '基于多智能体编排的股票投资咨询助手'
      }]
    }

    selectedAgentId.value = agents.value.some(agent => agent.agentId === DEFAULT_AGENT_ID)
      ? DEFAULT_AGENT_ID
      : agents.value[0].agentId
    agentLoading.value = false
  }
}

const createSession = async () => {
  const res = await createAgentSession({
    agentId: selectedAgentId.value,
    userId: userId.value
  })
  sessionId.value = res.data?.sessionId || ''
  if (!sessionId.value) {
    throw new Error('后端未返回 sessionId')
  }
}

const handleNewSession = async () => {
  if (!selectedAgentId.value) return

  sessionLoading.value = true
  try {
    await createSession()
    const conversation = createLocalConversation({
      sessionId: sessionId.value,
      welcome: '新会话已建立。你可以继续提问，我会结合行情、交易画像和风险边界给出分析。'
    })
    conversations.value.unshift(conversation)
    activeConversationId.value = conversation.localId
    messages.value = conversation.messages.map(message => ({ ...message }))
    persistConversations()
    scrollToBottom()
    ElMessage.success('已创建新的顾问会话')
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '会话创建失败')
  } finally {
    sessionLoading.value = false
  }
}

const ensureSession = async () => {
  if (sessionId.value) return
  await createSession()
  ensureActiveConversation()
  saveActiveConversation()
}

const handleSend = async () => {
  const message = question.value.trim()
  if (!message || !selectedAgentId.value) return

  question.value = ''
  pushMessage('user', message)
  advisorLoading.value = true

  try {
    await ensureSession()
    const res = await chatAgent({
      agentId: selectedAgentId.value,
      userId: userId.value,
      sessionId: sessionId.value,
      message
    })
    const content = res.data?.content || '顾问暂未返回有效内容，请稍后重试。'
    pushMessage('assistant', content)
  } catch (error) {
    console.error(error)
    pushMessage('assistant', '本次咨询请求失败。请确认 agent 后端服务可用后重试。')
  } finally {
    advisorLoading.value = false
  }
}

const handleSearchStock = async (query) => {
  if (!query) {
    searchResults.value = []
    return
  }

  searchLoading.value = true
  try {
    const res = await searchStock(query)
    searchResults.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error(error)
  } finally {
    searchLoading.value = false
  }
}

const handleSelectStock = (symbol) => {
  if (!symbol) return
  selectedSymbol.value = symbol
  question.value = `请分析 ${symbol} 的投资价值、主要风险和后续观察清单。`
  saveActiveConversation()
}

const applyPrompt = (prompt) => {
  question.value = prompt
}

const handleAgentChange = () => {
  sessionId.value = ''
  if (messages.value.length <= 1) {
    messages.value = [createWelcomeMessage()]
  }
  saveActiveConversation()
}

onMounted(async () => {
  await loadAgents()
  loadConversations()

  if (route.query.symbol) {
    selectedSymbol.value = String(route.query.symbol)
    handleSelectStock(selectedSymbol.value)
  } else if (conversations.value.length) {
    openConversation(conversations.value[0].localId)
  } else {
    ensureActiveConversation()
  }
})
</script>

<style scoped>
.prediction-page {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  border-left: 4px solid var(--el-color-primary);
  padding-left: 10px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.agent-select {
  width: 240px;
}

.agent-alert {
  margin-bottom: 16px;
}

.advisor-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
}

.chat-section,
.context-panel,
.risk-note {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
}

.chat-section {
  display: flex;
  flex-direction: column;
  min-height: 680px;
  overflow: hidden;
}

.chat-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  padding: 18px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.chat-meta h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #333;
}

.chat-meta p {
  margin: 0;
  color: #666;
  line-height: 1.6;
  font-size: 13px;
}

.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #fafafa;
}

.chat-message {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #f0f2f5;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
}

.chat-message.assistant .message-avatar {
  background: rgba(211, 47, 47, 0.1);
  color: var(--el-color-primary);
}

.message-bubble {
  max-width: min(720px, 78%);
  padding: 12px 14px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.chat-message.user .message-bubble {
  background: var(--el-color-primary);
  color: #fff;
  border-color: var(--el-color-primary);
}

.message-role {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.chat-message.user .message-role {
  color: rgba(255, 255, 255, 0.75);
}

.message-content {
  white-space: pre-wrap;
  line-height: 1.7;
  font-size: 14px;
  word-break: break-word;
}

.typing-dots {
  display: flex;
  gap: 5px;
  align-items: center;
  height: 22px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--el-color-primary);
  animation: typing 1s infinite ease-in-out;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.3s;
}

.composer {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.composer-actions {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.session-tip {
  color: #909399;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.context-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.context-panel {
  padding: 16px;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #333;
  margin-bottom: 14px;
}

.panel-title.with-action {
  justify-content: space-between;
}

.conversation-panel {
  padding-bottom: 12px;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 260px;
  overflow-y: auto;
}

.conversation-item {
  width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 6px 4px 10px;
  transition: all 0.2s ease;
}

.conversation-item:hover {
  border-color: rgba(211, 47, 47, 0.35);
  background: #fff7f7;
}

.conversation-item.active {
  border-color: var(--el-color-primary);
  background: rgba(211, 47, 47, 0.06);
}

.conversation-main {
  min-width: 0;
  flex: 1;
  border: none;
  background: transparent;
  padding: 6px 0;
  text-align: left;
  cursor: pointer;
}

.conversation-delete {
  flex: 0 0 auto;
  width: 28px;
  height: 28px;
  color: #b0b3b8;
}

.conversation-delete:hover {
  color: var(--el-color-danger);
}

.conversation-title {
  display: block;
  color: #333;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}

.conversation-meta em {
  font-style: normal;
  color: var(--el-color-primary);
  white-space: nowrap;
}

.empty-conversation {
  padding: 20px 0;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.stock-select {
  width: 100%;
}

.selected-stock {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  background: #f5f7fa;
  display: flex;
  justify-content: space-between;
  color: #606266;
}

.selected-stock strong {
  color: var(--el-color-primary);
}

.prompt-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.prompt-button {
  justify-content: flex-start;
  height: auto;
  min-height: 34px;
  padding: 8px 10px;
  white-space: normal;
  text-align: left;
  line-height: 1.5;
  color: #606266;
  background: #f8f9fb;
}

.prompt-button:hover {
  color: var(--el-color-primary);
  background: rgba(211, 47, 47, 0.06);
}

.risk-note {
  padding: 16px;
  background: #fff7f7;
  border-color: rgba(211, 47, 47, 0.16);
}

.risk-note strong {
  color: var(--el-color-primary);
}

.risk-note p {
  margin: 8px 0 0;
  color: #666;
  font-size: 13px;
  line-height: 1.7;
}

.markdown-body :deep(p) {
  margin: 0 0 10px;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin: 10px 0 8px;
  color: inherit;
  line-height: 1.35;
}

.markdown-body :deep(h1) {
  font-size: 20px;
}

.markdown-body :deep(h2) {
  font-size: 18px;
}

.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  font-size: 16px;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 8px 0 10px;
  padding-left: 22px;
}

.markdown-body :deep(li) {
  margin: 4px 0;
}

.markdown-body :deep(blockquote) {
  margin: 8px 0 10px;
  padding: 8px 12px;
  border-left: 3px solid var(--el-color-primary);
  background: rgba(211, 47, 47, 0.05);
  color: #606266;
}

.markdown-body :deep(pre) {
  margin: 10px 0;
  padding: 12px;
  border-radius: 6px;
  background: #1f2937;
  color: #f8fafc;
  overflow-x: auto;
  line-height: 1.6;
}

.markdown-body :deep(code) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
}

.markdown-body :deep(p code),
.markdown-body :deep(li code) {
  padding: 2px 5px;
  border-radius: 4px;
  background: #f0f2f5;
  color: #b71c1c;
}

.markdown-body :deep(a) {
  color: var(--el-color-primary);
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.chat-message.user .markdown-body :deep(p code),
.chat-message.user .markdown-body :deep(li code) {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
}

.chat-message.user .markdown-body :deep(a) {
  color: #fff;
  text-decoration: underline;
}

@keyframes typing {
  0%,
  80%,
  100% {
    opacity: 0.35;
    transform: translateY(0);
  }
  40% {
    opacity: 1;
    transform: translateY(-3px);
  }
}

@media (max-width: 960px) {
  .card-header,
  .header-actions,
  .composer-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .advisor-content {
    grid-template-columns: 1fr;
  }

  .agent-select {
    width: 100%;
  }

  .chat-section {
    min-height: 620px;
  }

  .message-bubble {
    max-width: 84%;
  }
}
</style>
