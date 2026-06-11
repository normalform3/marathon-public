<template>
  <div class="user-center-page">
    <section class="user-center-card">
      <div class="user-center-header">
        <div>
          <span class="app-eyebrow">RESULTS</span>
          <h1>我的成绩</h1>
          <p>回顾历史参赛表现，查看排名并提交赛后反馈。</p>
        </div>
        <el-button plain @click="fetchGradeList">刷新</el-button>
      </div>

      <div class="user-stat-grid">
        <div class="user-stat">
          <span>完赛记录</span>
          <strong>{{ gradeList.length }}</strong>
        </div>
        <div class="user-stat">
          <span>最好成绩</span>
          <strong>{{ bestGrade || "-" }}</strong>
        </div>
        <div class="user-stat">
          <span>最近赛事</span>
          <strong>{{ latestRaceName || "-" }}</strong>
        </div>
      </div>

      <el-table :data="gradeList" stripe style="width: 100%" empty-text="暂无成绩记录">
        <el-table-column prop="raceDate" label="时间" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.raceDate) || "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="raceName" label="赛事名称" min-width="190" align="left">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="getRaceDetail(row.raceId)">
              {{ row.raceName || "赛事已下线" }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="赛事类型" width="100" align="center">
          <template #default="{ row }">
            {{ convertRaceType(row.raceType) }}
          </template>
        </el-table-column>
        <el-table-column prop="athleteNumber" label="参赛号" width="110" align="center" />
        <el-table-column prop="name" label="姓名" width="100" align="center" />
        <el-table-column prop="grade" label="成绩" width="120" align="center" />
        <el-table-column prop="rank" label="排名" width="100" align="center" />
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="Number(row.isComment) === 0"
              type="primary"
              size="small"
              @click="openCommentDialog(row)"
            >
              评论赛事
            </el-button>
            <el-button v-else size="small" @click="viewComments(row.isComment)">
              查看评论
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="chart-card">
        <div ref="chartRef" class="grade-chart"></div>
      </div>
    </section>

    <el-dialog v-model="commentDialogVisible" title="评论赛事" width="min(540px, 92vw)">
      <div class="comment-dialog-content">
        <div class="race-info">
          <h3>{{ currentRace?.raceName }}</h3>
          <p>参赛号：{{ currentRace?.athleteNumber || "-" }}</p>
          <p>成绩：{{ currentRace?.grade || "-" }}</p>
        </div>
        <el-radio-group v-model="commentType">
          <el-radio :label="0">好评</el-radio>
          <el-radio :label="1">差评</el-radio>
        </el-radio-group>
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="4"
          placeholder="请输入评论内容"
          class="comment-input"
        ></el-input>
      </div>
      <template #footer>
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewCommentDialogVisible" title="查看评论" width="min(520px, 92vw)">
      <div class="comment-info">
        <p v-if="viewedComment">
          <strong>评论类型：</strong>{{ viewedComment.type === 0 ? "好评" : "差评" }}
        </p>
        <p v-if="viewedComment">
          <strong>评论内容：</strong>{{ viewedComment.content }}
        </p>
        <p v-if="viewedComment">
          <strong>评论时间：</strong>{{ formatDate(viewedComment.commentTime) }}
        </p>
        <p v-else>暂无评论信息</p>
      </div>
      <template #footer>
        <el-button @click="viewCommentDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, onUnmounted } from "vue";
import {
  getGradeList,
  submitComment as apiSubmitComment,
  getCommentById,
} from "@/api/user";
import * as echarts from "echarts";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { formatDate, raceTypeText } from "@/utils/display";

const gradeList = ref([]);
const router = useRouter();
const chartRef = ref(null);
let myChart = null;

const commentDialogVisible = ref(false);
const commentRaceId = ref(null);
const commentGradeId = ref(null);
const commentType = ref(0);
const commentContent = ref("");
const currentRace = ref(null);
const viewCommentDialogVisible = ref(false);
const viewedComment = ref(null);

const bestGrade = computed(() => {
  const valid = gradeList.value
    .map((item) => ({ raw: item.grade, seconds: timeStringToSeconds(item.grade) }))
    .filter((item) => Number.isFinite(item.seconds));
  if (!valid.length) return "";
  return valid.sort((a, b) => a.seconds - b.seconds)[0].raw;
});

const latestRaceName = computed(() => gradeList.value[0]?.raceName || "");

const convertRaceType = (type) => raceTypeText(type);

const timeStringToSeconds = (timeStr) => {
  if (!timeStr || typeof timeStr !== "string") return Number.NaN;
  const parts = timeStr.split(":").map(Number);
  if (parts.some((part) => Number.isNaN(part))) return Number.NaN;
  if (parts.length === 2) {
    return parts[0] * 60 + parts[1];
  }
  if (parts.length === 3) {
    return parts[0] * 3600 + parts[1] * 60 + parts[2];
  }
  if (parts.length === 1) {
    return parts[0];
  }
  return Number.NaN;
};

const secondsToTimeString = (seconds) => {
  if (!Number.isFinite(seconds)) return "-";
  const h = String(Math.floor(seconds / 3600)).padStart(2, "0");
  const m = String(Math.floor((seconds % 3600) / 60)).padStart(2, "0");
  const s = String(seconds % 60).padStart(2, "0");
  return `${h}:${m}:${s}`;
};

const fetchGradeList = async () => {
  try {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      ElMessage.error("请先登录");
      return;
    }

    const res = await getGradeList({ userId: parseInt(userId) });
    if (res.data && res.data.code === 200) {
      gradeList.value = (res.data.data || []).sort((a, b) => {
        return new Date(b.raceDate || 0) - new Date(a.raceDate || 0);
      });
      await nextTick();
      initChart();
    } else {
      ElMessage.error(res.data.message || "获取成绩失败");
    }
  } catch (error) {
    console.error("获取成绩失败", error);
    ElMessage.error("获取成绩失败，请稍后重试");
  }
};

const getRaceDetail = (raceId) => {
  if (!raceId) return;
  router.push(`/race/detail/${raceId}`);
};

const openCommentDialog = (race) => {
  currentRace.value = race;
  commentRaceId.value = race.raceId;
  commentGradeId.value = race.id;
  commentDialogVisible.value = true;
  commentType.value = 0;
  commentContent.value = "";
};

const submitComment = async () => {
  try {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      ElMessage.error("用户未登录");
      return;
    }
    if (!commentRaceId.value) {
      ElMessage.error("赛事ID不能为空");
      return;
    }
    if (!commentGradeId.value) {
      ElMessage.error("成绩ID不能为空");
      return;
    }
    if (!commentContent.value.trim()) {
      ElMessage.error("请输入评论内容");
      return;
    }

    const commentParam = {
      gradeId: commentGradeId.value,
      comment: {
        userId: parseInt(userId),
        raceId: commentRaceId.value,
        type: commentType.value,
        content: commentContent.value,
        commentTime: new Date().toISOString(),
        status: 0,
      },
    };

    const res = await apiSubmitComment(commentParam);
    if (res.data && res.data.code === 200) {
      ElMessage.success("评论提交成功");
      commentDialogVisible.value = false;
      await fetchGradeList();
    } else {
      ElMessage.error(res.data.message || "评论提交失败");
    }
  } catch (error) {
    console.error("评论提交出错", error);
    ElMessage.error("评论提交失败，请稍后重试");
  }
};

const viewComments = async (id) => {
  if (!id) {
    ElMessage.error("评论ID不能为空");
    return;
  }
  try {
    const res = await getCommentById({ id });
    if (res.data && res.data.code === 200) {
      viewedComment.value = res.data.data;
      viewCommentDialogVisible.value = true;
    } else {
      ElMessage.error(res.data.message || "获取评论失败");
    }
  } catch (error) {
    console.error("获取评论出错", error);
    ElMessage.error("获取评论失败，请稍后重试");
  }
};

const initChart = () => {
  if (!chartRef.value) return;
  if (myChart) {
    myChart.dispose();
  }
  myChart = echarts.init(chartRef.value);

  const chartData = [...gradeList.value]
    .filter((item) => item.raceDate && Number.isFinite(timeStringToSeconds(item.grade)))
    .sort((a, b) => new Date(a.raceDate) - new Date(b.raceDate));

  const option = {
    title: {
      text: chartData.length ? "历史成绩变化趋势" : "暂无可绘制的成绩数据",
      left: "center",
      textStyle: { color: "#172033", fontSize: 16 },
    },
    tooltip: {
      trigger: "axis",
      formatter: (params) => {
        const data = params[0];
        return `日期：${data.axisValue}<br/>成绩：${secondsToTimeString(data.value)}`;
      },
    },
    grid: {
      top: 62,
      bottom: 54,
      left: 80,
      right: 36,
    },
    xAxis: {
      type: "category",
      data: chartData.map((item) => formatDate(item.raceDate)),
      axisLabel: { rotate: 30, interval: 0 },
    },
    yAxis: {
      type: "value",
      name: "成绩",
      axisLabel: { formatter: (val) => secondsToTimeString(val) },
      splitLine: { lineStyle: { color: "rgba(33,53,82,0.08)" } },
    },
    series: [
      {
        name: "成绩",
        type: "line",
        data: chartData.map((item) => timeStringToSeconds(item.grade)),
        smooth: true,
        lineStyle: { color: "#0f766e", width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(15,118,110,0.24)" },
            { offset: 1, color: "rgba(15,118,110,0.04)" },
          ]),
        },
        symbol: "circle",
        symbolSize: 8,
      },
    ],
  };

  myChart.setOption(option);
};

onMounted(fetchGradeList);

onUnmounted(() => {
  if (myChart) {
    myChart.dispose();
    myChart = null;
  }
});
</script>

<style scoped>
.chart-card {
  margin-top: 24px;
  padding: 20px;
  border: 1px solid rgba(33, 53, 82, 0.08);
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.75);
}

.grade-chart {
  width: 100%;
  height: 320px;
}

.race-info {
  padding: 16px;
  margin-bottom: 16px;
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.85);
}

.race-info h3 {
  margin: 0 0 10px;
}

.race-info p {
  margin: 6px 0;
  color: var(--app-text-muted);
}

.comment-input {
  margin-top: 12px;
}
</style>
