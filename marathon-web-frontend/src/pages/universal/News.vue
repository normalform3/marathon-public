<template>
  <div class="app-page app-page--wide news-page">
    <div class="app-section-title">
      <div>
        <span class="app-eyebrow">NEWSROOM</span>
        <h1>新闻和公告</h1>
        <p>集中浏览平台发布的赛事新闻、报名提醒与重要公告。</p>
      </div>
      <div class="content-filters">
        <el-select v-model="selectedContentType" placeholder="选择内容类型">
          <el-option label="全部" value="all"></el-option>
          <el-option label="新闻" value="1"></el-option>
          <el-option label="公告" value="2"></el-option>
        </el-select>
        <el-button plain @click="goBack">返回首页</el-button>
      </div>
    </div>

    <section class="app-card app-table-card">
      <el-table :data="filteredContentList" stripe style="width: 100%">
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'warning'" effect="light">
              {{ row.type === 1 ? "新闻" : "公告" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标题" min-width="220" align="left">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="showNewsDetail(row)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>

  <el-dialog v-model="showDetailDialog" title="内容详情" width="min(760px, 92vw)">
    <div class="news-detail">
      <h3 class="detail-title">{{ currentNews.title }}</h3>
      <div class="detail-content" v-html="currentNews.content"></div>
    </div>
    <template #footer>
      <el-button @click="showDetailDialog = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, ref, computed } from "vue";
import { ElMessage } from "element-plus";
import { getNewsList } from "@/api/user";
import { useRouter } from "vue-router";
import { formatDate } from "@/utils/display";

const contentList = ref([]);
const selectedContentType = ref("all");
const showDetailDialog = ref(false);
const currentNews = ref({});

const filteredContentList = computed(() => {
  const data = [...contentList.value].sort(
    (a, b) => new Date(b.createTime) - new Date(a.createTime),
  );
  return selectedContentType.value === "all"
    ? data
    : data.filter(
        (content) => content.type.toString() === selectedContentType.value,
      );
});

const fetchContentList = async () => {
  try {
    const res = await getNewsList();
    if (res.data && res.data.code === 200) {
      contentList.value = res.data.data;
    } else {
      ElMessage.error(res.data.message || "获取内容列表失败");
    }
  } catch (error) {
    console.error("获取内容列表出错", error);
    ElMessage.error("获取内容列表失败，请稍后重试");
  }
};

const showNewsDetail = (news) => {
  currentNews.value = { ...news };
  showDetailDialog.value = true;
};

const router = useRouter();
const goBack = () => {
  router.push("/");
};

onMounted(fetchContentList);
</script>

<style scoped>
.content-filters {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content-filters :deep(.el-select) {
  width: 160px;
}

.news-detail {
  padding: 8px 4px;
}

.detail-title {
  margin: 0 0 18px;
  color: var(--app-text);
  font-size: 22px;
}

.detail-content {
  color: #475569;
  line-height: 1.9;
}

.detail-content img {
  max-width: 100%;
  height: auto;
  margin: 12px 0;
  border-radius: 12px;
}

@media (max-width: 680px) {
  .content-filters {
    align-items: stretch;
    width: 100%;
    flex-direction: column;
  }

  .content-filters :deep(.el-select) {
    width: 100%;
  }
}
</style>
