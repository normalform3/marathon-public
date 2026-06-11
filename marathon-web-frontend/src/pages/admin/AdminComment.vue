<template>
  <div class="comment-container">
    <div class="comment-header">
      <h2>评论审查列表</h2>
      <div class="filters">
        <el-select
          v-model="selectedType"
          class="type-filter"
          placeholder="选择评论类型"
        >
          <el-option label="全部评论" value="all" />
          <el-option label="好评" :value="0" />
          <el-option label="差评" :value="1" />
        </el-select>
        <el-select
          v-model="selectedStatus"
          class="status-filter"
          placeholder="选择评论状态"
        >
          <el-option label="全部状态" value="all" />
          <el-option label="未审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已删除" :value="2" />
        </el-select>
      </div>
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
        <el-table-column label="评论状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{
              convertCommentStatus(row.status)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              @click="passComment(row.id)"
              type="success"
            >
              通过
            </el-button>
            <el-button
              type="info"
              v-if="row.status === 0 || row.status === 1"
              @click="deleteComment(row.id)"
            >
              删除
            </el-button>
            <el-button
              v-if="row.status === 2"
              @click="restoreComment(row.id)"
              type="primary"
            >
              恢复
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from "vue";
import {
  getAllComments,
  passComment as apiPassComment,
  deleteComment as apiDeleteComment,
} from "@/api/admin";
import { ElMessage } from "element-plus";

const commentList = ref([]);
const selectedType = ref("all");
const selectedStatus = ref("all");

// 评论类型转换
const convertCommentType = (type) => {
  return type === 0 ? "好评" : "差评";
};

// 评论状态转换
const convertCommentStatus = (status) => {
  if (status === 0) return "待审核";
  if (status === 1) return "已通过";
  if (status === 2) return "已删除";
  return "";
};

// 获取评论状态对应的标签类型
const getStatusType = (status) => {
  if (status === 0) return "warning";
  if (status === 1) return "success";
  if (status === 2) return "danger";
  return "";
};

// 筛选后的评论列表
const filteredCommentList = computed(() => {
  let filteredByType =
    selectedType.value === "all"
      ? commentList.value
      : commentList.value.filter(
          (comment) => comment.type === parseInt(selectedType.value)
        );
  return selectedStatus.value === "all"
    ? filteredByType
    : filteredByType.filter(
        (comment) => comment.status === parseInt(selectedStatus.value)
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
    const res = await getAllComments();
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

// 通过评论
const passComment = async (id) => {
  try {
    const res = await apiPassComment({ id });
    if (res.data && res.data.code === 200) {
      ElMessage.success(res.data.message);
      // 重新获取评论列表
      await fetchCommentList();
    } else {
      ElMessage.error(res.data.message || "评论通过失败");
    }
  } catch (error) {
    console.error("通过评论出错", error);
    ElMessage.error("评论通过失败，请稍后重试");
  }
};

// 删除评论
const deleteComment = async (id) => {
  try {
    const res = await apiDeleteComment({ id });
    if (res.data && res.data.code === 200) {
      ElMessage.success(res.data.message);
      // 重新获取评论列表
      await fetchCommentList();
    } else {
      ElMessage.error(res.data.message || "评论删除失败");
    }
  } catch (error) {
    console.error("删除评论出错", error);
    ElMessage.error("评论删除失败，请稍后重试");
  }
};

// 恢复评论
const restoreComment = async (id) => {
  try {
    const res = await apiPassComment({ id });
    if (res.data && res.data.code === 200) {
      ElMessage.success("评论已恢复");
      // 重新获取评论列表
      await fetchCommentList();
    } else {
      ElMessage.error(res.data.message || "评论恢复失败");
    }
  } catch (error) {
    console.error("恢复评论出错", error);
    ElMessage.error("评论恢复失败，请稍后重试");
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

.filters {
  display: flex;
  gap: 10px;
}

.type-filter,
.status-filter {
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
