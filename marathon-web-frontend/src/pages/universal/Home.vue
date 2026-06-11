<template>
  <div class="home-page">
    <section class="hero">
      <el-carousel
        class="hero-carousel"
        height="560px"
        :interval="5200"
        arrow="always"
      >
        <el-carousel-item v-for="item in carouselItems" :key="item.title">
          <div class="hero-slide">
            <img :src="item.imageUrl" :alt="item.title" class="hero-image" />
            <div class="hero-overlay"></div>
            <div class="hero-content">
              <span class="app-eyebrow">MARATHON ELITE</span>
              <h1>{{ item.title }}</h1>
              <p>{{ item.description }}</p>
              <div class="hero-actions">
                <el-button type="primary" size="large" @click="router.push('/racelist')">
                  浏览赛事
                </el-button>
                <el-button size="large" plain @click="router.push('/news')">
                  查看公告
                </el-button>
              </div>
            </div>
            <div class="hero-stats app-card">
              <div v-for="stat in heroStats" :key="stat.label">
                <strong>{{ stat.value }}</strong>
                <span>{{ stat.label }}</span>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <section class="app-page">
      <div class="app-section-title">
        <div>
          <span class="app-eyebrow">INFORMATION</span>
          <h2>赛事资讯与平台公告</h2>
          <p>集中呈现最新新闻、报名提醒和重要通知，让参赛者快速掌握赛事动态。</p>
        </div>
        <el-button plain @click="router.push('/news')">全部内容</el-button>
      </div>

      <div class="content-grid">
        <article class="app-card content-card stagger-item">
          <div class="card-heading">
            <span>最新新闻</span>
            <el-tag effect="light">NEWS</el-tag>
          </div>
          <ul class="content-list">
            <li v-for="item in newsItems.slice(0, 6)" :key="item.id || item.title">
              <button type="button" @click="showDialog(item.title, item.content)">
                {{ item.title }}
              </button>
              <time>{{ formatDate(item.createTime) }}</time>
            </li>
          </ul>
          <el-empty v-if="newsItems.length === 0" description="暂无新闻" />
        </article>

        <article class="app-card content-card content-card--accent stagger-item">
          <div class="card-heading">
            <span>重要公告</span>
            <el-tag type="warning" effect="light">NOTICE</el-tag>
          </div>
          <ul class="content-list">
            <li v-for="item in noticeItems.slice(0, 6)" :key="item.id || item.title">
              <button type="button" @click="showDialog(item.title, item.content)">
                {{ item.title }}
              </button>
              <time>{{ formatDate(item.createTime) }}</time>
            </li>
          </ul>
          <el-empty v-if="noticeItems.length === 0" description="暂无公告" />
        </article>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="min(620px, 92vw)">
      <p class="dialog-content">{{ dialogContent }}</p>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getHomePageNews } from "@/api/user";
import { useRouter } from "vue-router";
import { formatDate } from "@/utils/display";

const router = useRouter();

const carouselItems = [
  {
    imageUrl:
      "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?auto=format&fit=crop&w=1920&q=85",
    title: "奔向城市的高光时刻",
    description: "精选城市马拉松赛事，连接跑者、组织者与每一次值得纪念的出发。",
  },
  {
    imageUrl:
      "https://images.unsplash.com/photo-1502904550040-7534597429ae?auto=format&fit=crop&w=1920&q=85",
    title: "用专业体验承载每一步",
    description: "报名、审核、成绩、通知与路线展示一站完成，赛事体验更流畅。",
  },
  {
    imageUrl:
      "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&w=1920&q=85",
    title: "从报名到冲线，全程在线",
    description: "实时获取赛事公告与报名进展，让参赛准备更从容、更确定。",
  },
];

const heroStats = [
  { value: "24H", label: "在线赛事服务" },
  { value: "4", label: "赛事类型覆盖" },
  { value: "1站式", label: "报名与成绩管理" },
];

const newsItems = ref([]);
const noticeItems = ref([]);
const dialogVisible = ref(false);
const dialogTitle = ref("内容详情");
const dialogContent = ref("");

const fetchData = async () => {
  try {
    const response = await getHomePageNews();
    const data = response.data;
    newsItems.value = data.data.filter(
      (item) => item.type === 1 && item.status === 1,
    );
    noticeItems.value = data.data.filter(
      (item) => item.type === 2 && item.status === 1,
    );
  } catch (error) {
    console.error("获取数据失败:", error);
  }
};

const showDialog = (title, content) => {
  dialogTitle.value = title || "内容详情";
  dialogContent.value = content;
  dialogVisible.value = true;
};

onMounted(fetchData);
</script>

<style scoped>
.home-page {
  padding-bottom: 24px;
}

.hero {
  width: min(1360px, calc(100% - 32px));
  margin: 24px auto 0;
  overflow: hidden;
  border-radius: 24px;
  box-shadow: var(--app-shadow-lg);
}

.hero-carousel {
  background: #0f172a;
}

.hero-slide {
  position: relative;
  width: 100%;
  height: 100%;
}

.hero-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  animation: hero-zoom 7s ease both;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(9, 17, 32, 0.8), rgba(9, 17, 32, 0.38), rgba(9, 17, 32, 0.08)),
    linear-gradient(0deg, rgba(9, 17, 32, 0.38), transparent 48%);
}

.hero-content {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: center;
  width: min(680px, 88%);
  height: 100%;
  padding: 70px 0 90px 76px;
  color: #fff;
  flex-direction: column;
  animation: page-rise 0.62s ease both;
}

.hero-content .app-eyebrow {
  color: #d7f4ef;
}

.hero-content h1 {
  margin: 0;
  font-size: clamp(38px, 5vw, 68px);
  line-height: 1.06;
}

.hero-content p {
  max-width: 590px;
  margin: 22px 0 0;
  color: rgba(255, 255, 255, 0.84);
  font-size: 18px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 34px;
}

.hero-stats {
  position: absolute;
  z-index: 3;
  right: 44px;
  bottom: 36px;
  display: grid;
  grid-template-columns: repeat(3, minmax(110px, 1fr));
  padding: 20px 22px;
  background: rgba(255, 255, 255, 0.88);
}

.hero-stats div {
  padding: 0 18px;
  border-right: 1px solid var(--app-border);
}

.hero-stats div:last-child {
  border-right: 0;
}

.hero-stats strong,
.hero-stats span {
  display: block;
}

.hero-stats strong {
  color: var(--app-primary-dark);
  font-size: 24px;
}

.hero-stats span {
  margin-top: 6px;
  color: var(--app-text-muted);
  font-size: 13px;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 22px;
}

.content-card {
  padding: 26px;
}

.content-card--accent {
  background:
    linear-gradient(140deg, rgba(255, 255, 255, 0.94), rgba(255, 249, 235, 0.92));
}

.card-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: var(--app-text);
  font-size: 20px;
  font-weight: 800;
}

.content-list {
  padding: 0;
  margin: 0;
  list-style: none;
}

.content-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 16px 0;
  border-bottom: 1px solid rgba(33, 53, 82, 0.08);
}

.content-list li:last-child {
  border-bottom: 0;
}

.content-list button {
  min-width: 0;
  padding: 0;
  overflow: hidden;
  color: var(--app-text);
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  background: transparent;
  border: 0;
  transition: color 0.2s ease;
}

.content-list button:hover {
  color: var(--app-primary);
}

.content-list time {
  color: var(--app-text-muted);
  font-size: 13px;
  white-space: nowrap;
}

.dialog-content {
  line-height: 1.8;
}

@keyframes hero-zoom {
  from {
    transform: scale(1);
  }

  to {
    transform: scale(1.035);
  }
}

@media (max-width: 920px) {
  .hero {
    width: calc(100% - 24px);
    border-radius: 18px;
  }

  .hero-content {
    padding: 56px 24px 160px;
  }

  .hero-stats {
    right: 20px;
    left: 20px;
    grid-template-columns: repeat(3, 1fr);
  }

  .hero-stats div {
    padding: 0 10px;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 620px) {
  .hero-carousel {
    height: 620px !important;
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }

  .hero-stats div {
    padding: 10px 0;
    border-right: 0;
    border-bottom: 1px solid var(--app-border);
  }

  .hero-stats div:last-child {
    border-bottom: 0;
  }
}
</style>
