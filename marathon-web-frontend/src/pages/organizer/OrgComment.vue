<template>
  <div class="comment-container">
    <div class="comment-header">
      <h2>参赛者评论列表</h2>
      <el-select
        v-model="selectedType"
        class="type-filter"
        placeholder="选择评论类型"
      >
        <el-option label="全部评论" value="all" />
        <el-option label="好评" :value="0" />
        <el-option label="差评" :value="1" />
      </el-select>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <el-table :data="filteredCommentList" :border="true" style="width: 100%">
        <el-table-column label="评论类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 0 ? 'success' : 'danger'">
              {{ convertCommentType(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="content"
          label="评论内容"
          min-width="200"
          align="center"
        ></el-table-column>
        <el-table-column
          prop="commentTime"
          label="评论时间"
          width="180"
          align="center"
        >
          <template #default="{ row }">
            {{ formatDate(row.commentTime) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from "vue";
import { getCommentByRaceId } from "@/api/user";
import { ElMessage } from "element-plus";

const commentList = ref([]);
const selectedType = ref("all");

// 评论类型转换
const convertCommentType = (type) => {
  return type === 0 ? "好评" : "差评";
};

// 筛选后的评论列表
const filteredCommentList = computed(() => {
  if (selectedType.value === "all") {
    return commentList.value;
  }
  return commentList.value.filter(
    (comment) => comment.type === selectedType.value
  );
});

// 时间格式化
const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  const hours = `${date.getHours()}`.padStart(2, "0");
  const minutes = `${date.getMinutes()}`.padStart(2, "0");
  const seconds = `${date.getSeconds()}`.padStart(2, "0");
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 获取评论列表
const fetchCommentList = async () => {
  try {
    const raceId = localStorage.getItem("raceId");
    if (!raceId) {
      ElMessage.error("未找到赛事 ID");
      return;
    }
    const res = await getCommentByRaceId({ raceId: parseInt(raceId) });
    if (res.data && res.data.code === 200) {
      commentList.value = res.data.data;
    } else {
      ElMessage.error(res.data.message || "获取评论列表失败");
    }
  } catch (error) {
    console.error("获取评论列表出错", error);
    ElMessage.error("获取评论列表失败，请稍后重试");
  }
};

onMounted(fetchCommentList);
</script>

<style scoped>
.comment-container {
  width: 90%;
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.comment-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.type-filter {
  width: 150px;
}

.comment-list {
  margin-top: 20px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table--border) {
  border: 1px solid #ebeef5;
}

:deep(.el-tag) {
  min-width: 60px;
}
</style>
