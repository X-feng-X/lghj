<template>
  <div class="community-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">股友社区</span>
          <el-button type="primary" @click="handleCreate">发布观点</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="热门推荐" name="hot">
          <div v-loading="loading">
            <div v-if="blogs.length === 0" class="empty-list">暂无内容</div>
            <div v-for="blog in blogs" :key="blog.id" class="blog-item" @click="openBlog(blog)">
              <div class="blog-main">
                <h3 class="blog-title">{{ blog.title }}</h3>
                <p class="blog-preview">{{ (blog.content || blog.context || '').substring(0, 100) }}...</p>
                <div class="blog-meta">
                  <span>{{ blog.name || '用户 #' + blog.userId }}</span>
                  <span class="meta-divider">·</span>
                  <span>{{ blog.createTime }}</span>
                  <span class="meta-divider">·</span>
                  <span>点赞 {{ blog.liked ?? blog.likeCount ?? 0 }}</span>
                  <span class="meta-divider">·</span>
                  <span><el-icon><ChatDotRound /></el-icon> {{ blog.comments ?? blog.commentCount ?? 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="最新发布" name="latest">
          <div v-loading="loading">
            <div v-if="blogs.length === 0" class="empty-list">暂无内容</div>
            <div v-for="blog in blogs" :key="blog.id" class="blog-item" @click="openBlog(blog)">
              <div class="blog-main">
                <h3 class="blog-title">{{ blog.title }}</h3>
                <p class="blog-preview">{{ (blog.content || blog.context || '').substring(0, 100) }}...</p>
                <div class="blog-meta">
                  <span>{{ blog.name || '用户 #' + blog.userId }}</span>
                  <span class="meta-divider">·</span>
                  <span>{{ blog.createTime }}</span>
                  <span class="meta-divider">·</span>
                  <span>点赞 {{ blog.liked ?? blog.likeCount ?? 0 }}</span>
                  <span class="meta-divider">·</span>
                  <span><el-icon><ChatDotRound /></el-icon> {{ blog.comments ?? blog.commentCount ?? 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="我的关注" name="follow">
          <div v-loading="loading">
            <div v-if="blogs.length === 0" class="empty-list">暂无内容</div>
            <div v-for="blog in blogs" :key="blog.id" class="blog-item" @click="openBlog(blog)">
              <div class="blog-main">
                <h3 class="blog-title">{{ blog.title }}</h3>
                <p class="blog-preview">{{ (blog.content || blog.context || '').substring(0, 100) }}...</p>
                <div class="blog-meta">
                  <span>{{ blog.name || '用户 #' + blog.userId }}</span>
                  <span class="meta-divider">·</span>
                  <span>{{ blog.createTime }}</span>
                  <span class="meta-divider">·</span>
                  <span>点赞 {{ blog.liked ?? blog.likeCount ?? 0 }}</span>
                  <span class="meta-divider">·</span>
                  <span><el-icon><ChatDotRound /></el-icon> {{ blog.comments ?? blog.commentCount ?? 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="我的发布" name="me">
          <div v-loading="loading">
            <div v-if="blogs.length === 0" class="empty-list">暂无内容</div>
            <div v-for="blog in blogs" :key="blog.id" class="blog-item" @click="openBlog(blog)">
              <div class="blog-main">
                <h3 class="blog-title">{{ blog.title }}</h3>
                <p class="blog-preview">{{ (blog.content || blog.context || '').substring(0, 100) }}...</p>
                <div class="blog-meta">
                  <span>我</span>
                  <span class="meta-divider">·</span>
                  <span>{{ blog.createTime }}</span>
                  <span class="meta-divider">·</span>
                  <span>点赞 {{ blog.liked ?? blog.likeCount ?? 0 }}</span>
                  <span class="meta-divider">·</span>
                  <span><el-icon><ChatDotRound /></el-icon> {{ blog.comments ?? blog.commentCount ?? 0 }}</span>
                  <span class="meta-divider">·</span>
                  <el-button link type="danger" size="small" @click.stop="handleDelete(blog.id)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <el-pagination v-if="blogs.length > 0" background layout="prev, pager, next" :total="total" :page-size="pageSize"
        v-model:current-page="currentPage" @current-change="fetchBlogs" class="pagination" />
    </el-card>

    <el-dialog v-model="createDialogVisible" title="发布观点" width="600px">
      <el-form :model="createForm">
        <el-form-item label="标题">
          <el-input v-model="createForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="createForm.content" type="textarea" rows="6" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="publishLoading">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="博客详情" width="820px">
      <div v-if="currentBlog" class="blog-detail">
        <h2>{{ currentBlog.title }}</h2>
        <div class="blog-meta detail-meta">
          <span class="detail-user">
            <el-avatar :size="24" :src="currentBlog.icon" />
            <span>{{ currentBlog.name || ('用户 #' + currentBlog.userId) }}</span>
          </span>
          <span>{{ currentBlog.createTime }}</span>
          <el-button type="primary" link v-if="currentBlog.userId !== userStore.userInfo.id"
            @click="handleFollow(currentBlog.userId)">
            {{ isFollowing ? '已关注' : '关注' }}
          </el-button>
        </div>
        <div class="blog-content">{{ currentBlog.content || currentBlog.context }}</div>

        <div class="blog-actions">
          <el-button type="primary" plain @click="handleLikeBlog">
            {{ currentBlog.isLike ? '已点赞' : '点赞' }} {{ currentBlog.liked ?? currentBlog.likeCount ?? 0 }}
          </el-button>
        </div>

        <div class="comments-section">
          <h3>评论 ({{ currentBlog.comments ?? currentBlog.commentCount ?? 0 }})</h3>
          <div class="comment-input">
            <el-input v-model="commentContent" placeholder="写下你的评论..." class="input-with-select">
              <template #append>
                <el-button type="primary" :disabled="!commentContent.trim()" :loading="commentsSubmitting"
                  @click="submitComment">发送</el-button>
              </template>
            </el-input>
          </div>

          <div class="comment-list" v-loading="commentsLoading">
            <div v-if="comments.length === 0" class="empty-list">暂无评论</div>
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <span class="comment-user">
                  <el-avatar :size="22" :src="comment.user?.avatar" />
                  <span>{{ comment.user?.nickname || ('用户 #' + comment.user?.id) }}</span>
                </span>
                <span class="comment-time">{{ comment.createTime }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-actions">
                <el-button link size="small" @click="handleLikeComment(comment)">
                  {{ comment.isLiked ? '已点赞' : '点赞' }} {{ comment.liked ?? 0 }}
                </el-button>
                <el-button link size="small" @click="replyComment(comment)">回复</el-button>
              </div>

              <div v-if="comment.children && comment.children.length > 0" class="child-comments">
                <div v-for="child in comment.children" :key="child.id" class="child-comment-item">
                  <div class="comment-header">
                    <span class="comment-user">
                      <el-avatar :size="20" :src="child.user?.avatar" />
                      <span>{{ child.user?.nickname || ('用户 #' + child.user?.id) }}</span>
                    </span>
                    <span class="comment-time">{{ child.createTime }}</span>
                  </div>
                  <div class="comment-content">{{ child.content }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Community'
}
</script>

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import {
  getHotBlogs, getMyBlogs, getFollowedBlogs, publishBlog, deleteBlog, getBlogDetail,
  likeBlog, getComments, addComment, likeComment
} from '../api/community'
import { followUser, checkFollow } from '../api/user'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('hot')
const blogs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const isLoaded = ref(false)

const createDialogVisible = ref(false)
const createForm = reactive({
  title: '',
  content: ''
})
const publishLoading = ref(false)

const detailDialogVisible = ref(false)
const currentBlog = ref(null)
const isFollowing = ref(false)

const comments = ref([])
const commentsLoading = ref(false)
const commentsSubmitting = ref(false)
const commentContent = ref('')

const handleTabChange = () => {
  currentPage.value = 1
  fetchBlogs()
}

const applyBlogPatch = (blogId, patch) => {
  const idx = blogs.value.findIndex(b => b.id === blogId)
  if (idx >= 0) {
    blogs.value[idx] = { ...blogs.value[idx], ...patch }
  }
}

const handleCreate = () => {
  createForm.title = ''
  createForm.content = ''
  createDialogVisible.value = true
}

const submitCreate = async () => {
  const title = (createForm.title || '').trim()
  const content = (createForm.content || '').trim()
  if (!title || !content) {
    ElMessage.warning('标题和内容不能为空')
    return
  }

  publishLoading.value = true
  try {
    await publishBlog({ title, content })
    ElMessage.success('发布成功')
    createDialogVisible.value = false
    currentPage.value = 1
    fetchBlogs()
  } catch (e) {
    console.error(e)
  } finally {
    publishLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这条博客吗？', '提示', { type: 'warning' })
    await deleteBlog(id)
    ElMessage.success('删除成功')
    fetchBlogs()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const fetchComments = async (blogId) => {
  commentsLoading.value = true
  try {
    const res = await getComments(blogId, 1, 50)
    const data = res.data
    if (Array.isArray(data)) {
      comments.value = data
    } else {
      comments.value = data?.list || data?.records || []
    }
  } catch (e) {
    console.error(e)
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

const submitComment = async () => {
  if (!currentBlog.value) return
  const content = commentContent.value.trim()
  if (!content) return

  commentsSubmitting.value = true
  try {
    await addComment({ blogId: currentBlog.value.id, parentId: 0, content })
    commentContent.value = ''
    await fetchComments(currentBlog.value.id)

    const nextCount = (currentBlog.value.comments ?? currentBlog.value.commentCount ?? 0) + 1
    currentBlog.value.comments = nextCount
    applyBlogPatch(currentBlog.value.id, { comments: nextCount })
  } catch (e) {
    console.error(e)
  } finally {
    commentsSubmitting.value = false
  }
}

const replyComment = async (comment) => {
  if (!currentBlog.value) return
  try {
    const { value } = await ElMessageBox.prompt('请输入回复内容', '回复', {
      confirmButtonText: '发送',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValidator: (v) => (v || '').trim().length > 0 || '回复内容不能为空'
    })
    await addComment({ blogId: currentBlog.value.id, parentId: comment.id, content: value.trim() })
    await fetchComments(currentBlog.value.id)

    const nextCount = (currentBlog.value.comments ?? currentBlog.value.commentCount ?? 0) + 1
    currentBlog.value.comments = nextCount
    applyBlogPatch(currentBlog.value.id, { comments: nextCount })
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleLikeComment = async (comment) => {
  try {
    await likeComment(comment.id)
    const isLiked = !!comment.isLiked
    comment.isLiked = isLiked ? 0 : 1
    const nextLiked = (comment.liked ?? 0) + (isLiked ? -1 : 1)
    comment.liked = Math.max(0, nextLiked)
  } catch (e) {
    console.error(e)
  }
}

const fetchBlogs = async () => {
  loading.value = true
  try {
    let res
    if (activeTab.value === 'hot') {
      res = await getHotBlogs(currentPage.value, pageSize.value)
      if (Array.isArray(res.data)) {
        blogs.value = res.data
        total.value = res.data.length
      } else if (res.data && res.data.records) {
        blogs.value = res.data.records
        total.value = res.data.total
      } else if (res.data && res.data.list) {
        blogs.value = res.data.list
        total.value = res.data.total || res.data.list.length
      }
    } else if (activeTab.value === 'latest') {
      res = await getHotBlogs(currentPage.value, pageSize.value)
      if (Array.isArray(res.data)) {
        res.data.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
        blogs.value = res.data
        total.value = res.data.length
      } else if (res.data && res.data.list) {
        const list = [...res.data.list]
        list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
        blogs.value = list
        total.value = res.data.total || list.length
      }
    } else if (activeTab.value === 'me') {
      res = await getMyBlogs(currentPage.value, pageSize.value)
      if (Array.isArray(res.data)) {
        blogs.value = res.data
        total.value = res.data.length
      } else if (res.data && res.data.records) {
        blogs.value = res.data.records
        total.value = res.data.total
      } else if (res.data && res.data.list) {
        blogs.value = res.data.list
        total.value = res.data.total || res.data.list.length
      }
    } else if (activeTab.value === 'follow') {
      res = await getFollowedBlogs(currentPage.value, pageSize.value)
      if (res.data && res.data.records) {
        blogs.value = res.data.records
        total.value = res.data.total
      } else if (res.data && res.data.list) {
        blogs.value = res.data.list
        total.value = res.data.total || res.data.list.length
      } else {
        blogs.value = []
        total.value = 0
      }
    }
    isLoaded.value = true
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleLikeBlog = async () => {
  if (!currentBlog.value) return
  try {
    await likeBlog(currentBlog.value.id)
    ElMessage.success('操作成功')

    const wasLiked = !!currentBlog.value.isLike
    currentBlog.value.isLike = wasLiked ? 0 : 1
    const delta = wasLiked ? -1 : 1
    const nextLiked = (currentBlog.value.liked ?? currentBlog.value.likeCount ?? 0) + delta
    currentBlog.value.liked = Math.max(0, nextLiked)
    applyBlogPatch(currentBlog.value.id, { liked: currentBlog.value.liked })
  } catch (error) {
    console.error(error)
  }
}

const openBlog = async (blog) => {
  detailDialogVisible.value = true
  try {
    const res = await getBlogDetail(blog.id)
    currentBlog.value = res.data || blog
  } catch (e) {
    console.error(e)
    currentBlog.value = blog
  }

  fetchComments(blog.id)

  if (blog.userId !== userStore.userInfo.id) {
    try {
      const res = await checkFollow(blog.userId)
      isFollowing.value = !!res.data
    } catch (e) {
      console.error(e)
    }
  } else {
    isFollowing.value = false
  }
}

const handleFollow = async (userId) => {
  if (!userId) return
  try {
    await followUser(userId, !isFollowing.value)
    isFollowing.value = !isFollowing.value
    ElMessage.success(isFollowing.value ? '关注成功' : '已取消关注')
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  if (!isLoaded.value) {
    fetchBlogs()
  }
})

onActivated(() => {
  if (!loading.value) fetchBlogs()
})
</script>

<style scoped>
.community-page {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.blog-item {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.blog-item:hover {
  background-color: #fafafa;
}

.blog-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
}

.blog-preview {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  line-height: 1.5;
}

.blog-meta {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
}

.meta-divider {
  margin: 0 8px;
}

.empty-list {
  text-align: center;
  padding: 40px;
  color: #999;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}

.blog-detail h2 {
  margin-top: 0;
}

.detail-meta {
  margin-bottom: 20px;
  font-size: 14px;
}

.detail-user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.blog-content {
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 30px;
  white-space: pre-wrap;
}

.blog-actions {
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.comments-section {
  margin-top: 20px;
}

.comment-input {
  margin-bottom: 20px;
}

.comment-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 12px;
  color: #999;
}

.comment-user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.comment-content {
  font-size: 14px;
  color: #333;
  margin-bottom: 10px;
}

.comment-actions {
  text-align: right;
}

.child-comments {
  margin-top: 10px;
  background-color: #f9f9f9;
  padding: 10px;
  border-radius: 4px;
}

.child-comment-item {
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.child-comment-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}
</style>
