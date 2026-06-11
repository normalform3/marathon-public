<template>
  <div class="user-center-page">
    <section class="user-center-card">
      <div class="user-center-header">
        <div>
          <span class="app-eyebrow">NOTIFICATIONS</span>
          <h1>通知中心</h1>
          <p>查看抽签、赛事变更、成绩录入等重要消息。</p>
        </div>
        <el-button plain @click="fetchNoticeList">刷新</el-button>
      </div>

      <div class="user-stat-grid">
        <div class="user-stat">
          <span>全部通知</span>
          <strong>{{ noticeList.length }}</strong>
        </div>
        <div class="user-stat">
          <span>未读</span>
          <strong>{{ unreadCount }}</strong>
        </div>
        <div class="user-stat">
          <span>已读</span>
          <strong>{{ noticeList.length - unreadCount }}</strong>
        </div>
      </div>

      <el-table :data="noticeList" stripe style="width: 100%" empty-text="暂无通知">
        <el-table-column prop="content" label="通知内容" min-width="300" align="left" />
        <el-table-column label="通知类型" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="noticeTag(row.type)" effect="light">
              {{ convertNoticeType(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="通知时间" width="190" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 0 ? 'danger' : 'success'" effect="light">
              {{ row.isRead === 0 ? "未读" : "已读" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.isRead === 0"
              type="primary"
              size="small"
              @click="setNoticeRead(row.subscribeId)"
            >
              设为已读
            </el-button>
            <span v-else class="read-text">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { getNoticeById, noticeSetRead } from "@/api/user";
import { ElMessage } from "element-plus";
import dayjs from "dayjs";

const convertNoticeType = (type) => {
  const numType = Number(type);
  switch (numType) {
    case 1:
      return "抽签结果";
    case 2:
      return "赛事变更";
    case 3:
      return "成绩通知";
    case 4:
      return "其他";
    default:
      return "未知类型";
  }
};

const noticeTag = (type) => {
  const numType = Number(type);
  if (numType === 1) return "warning";
  if (numType === 2) return "primary";
  if (numType === 3) return "success";
  return "info";
};

const formatDateTime = (dateTime) => {
  return dayjs(dateTime).format("YYYY-MM-DD HH:mm:ss");
};

const noticeList = ref([]);
const unreadCount = computed(
  () => noticeList.value.filter((notice) => notice.isRead === 0).length,
);

const fetchNoticeList = async () => {
  try {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      ElMessage.error("请先登录");
      return;
    }
    const res = await getNoticeById({ userId: parseInt(userId) });
    if (res.data && res.data.data) {
      noticeList.value = res.data.data;
    }
  } catch (error) {
    console.error("获取用户通知信息列表失败:", error);
    ElMessage.error("获取通知失败，请稍后重试");
  }
};

const setNoticeRead = async (subscribeId) => {
  try {
    const res = await noticeSetRead({ subscribeId });
    if (res.data.code === 200) {
      const index = noticeList.value.findIndex(
        (notice) => notice.subscribeId === subscribeId,
      );
      if (index > -1) {
        noticeList.value[index].isRead = 1;
      }
      ElMessage.success("已标记为已读");
    } else {
      ElMessage.error(res.data.message || "操作失败");
    }
  } catch (error) {
    console.error("设置通知为已读失败:", error);
    ElMessage.error("设置通知为已读失败");
  }
};

onMounted(fetchNoticeList);
</script>

<style scoped>
.read-text {
  color: var(--app-text-muted);
  font-size: 13px;
}
</style>
