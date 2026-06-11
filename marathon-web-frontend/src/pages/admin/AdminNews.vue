<template>
  <div class="admin-container">
    <div class="admin-header flex justify-between items-center">
      <h2>新闻和公告管理</h2>
      <div class="content-filters flex gap-4 items-center">
        <el-select
          v-model="selectedContentType"
          placeholder="选择内容类型"
          width="120"
        >
          <el-option label="全部" value="all"></el-option>
          <el-option label="新闻" value="1"></el-option>
          <el-option label="公告" value="2"></el-option>
        </el-select>
        <el-select v-model="selectedStatus" placeholder="选择状态">
          <el-option label="全部" value="all"></el-option>
          <el-option label="显示" value="1"></el-option>
          <el-option label="隐藏" value="0"></el-option>
        </el-select>
        <el-button type="primary" @click="showAddForm = true"
          >添加新闻</el-button
        >
      </div>
    </div>
    <div class="content-list">
      <el-table :data="filteredContentList" :border="true" style="width: 100%">
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'warning'">
              {{ row.type === 1 ? "新闻" : "公告" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="title"
          label="标题"
          min-width="200"
          align="center"
        ></el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
          align="center"
        >
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{
              convertStatus(row.status)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button
              :type="row.status === 0 ? 'primary' : 'warning'"
              @click="toggleStatus(row.id, row.status)"
            >
              {{ row.status === 0 ? "显示" : "隐藏" }}
            </el-button>
            <el-button @click="deleteContent(row.id)" type="danger"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="showAddForm" title="添加新闻" width="50%">
      <el-form
        :model="newContent"
        :rules="contentRules"
        ref="contentFormRef"
        label-width="80px"
      >
        <el-form-item label="类型" prop="type">
          <el-select v-model="newContent.type" placeholder="请选择内容类型">
            <el-option label="新闻" value="1"></el-option>
            <el-option label="公告" value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="newContent.title"
            placeholder="请输入新闻标题"
          ></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="newContent.content"
            type="textarea"
            placeholder="请输入新闻内容"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addContent">提交</el-button>
          <el-button @click="showAddForm = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getAllNews,
  addNews,
  showNews,
  hideNews,
  deleteNews,
} from "@/api/admin";

const contentList = ref([]);
const selectedContentType = ref("all");
const selectedStatus = ref("all");
const newContent = ref({
  type: null, // 将 type 默认值设为 null
  title: "",
  content: "",
});
const contentFormRef = ref(null);
const showAddForm = ref(false);

const convertStatus = (status) => {
  return status === 1 ? "显示" : "隐藏";
};

const getStatusType = (status) => {
  return status === 1 ? "success" : "info";
};

const filteredContentList = computed(() => {
  let filteredByType =
    selectedContentType.value === "all"
      ? contentList.value
      : contentList.value.filter(
          (content) => content.type.toString() === selectedContentType.value
        );
  return selectedStatus.value === "all"
    ? filteredByType
    : filteredByType.filter(
        (content) => content.status.toString() === selectedStatus.value
      );
});

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

const fetchContentList = async () => {
  try {
    const res = await getAllNews();
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

const addContent = async () => {
  await contentFormRef.value.validate();
  try {
    const res = await addNews(newContent.value);
    if (res.data && res.data.code === 200) {
      ElMessage.success(res.data.message);
      await fetchContentList();
      newContent.value = {
        type: null,
        title: "",
        content: "",
      };
      showAddForm.value = false;
    } else {
      ElMessage.error(res.data.message || "添加内容失败");
    }
  } catch (error) {
    console.error("添加内容出错", error);
    ElMessage.error("添加内容失败，请稍后重试");
  }
};

const toggleStatus = async (id, status) => {
  try {
    if (status === 0) {
      const res = await showNews({ id });
      if (res.data && res.data.code === 200) {
        ElMessage.success(res.data.message);
        await fetchContentList();
      } else {
        ElMessage.error(res.data.message || "显示内容失败");
      }
    } else {
      const res = await hideNews({ id });
      if (res.data && res.data.code === 200) {
        ElMessage.success(res.data.message);
        await fetchContentList();
      } else {
        ElMessage.error(res.data.message || "隐藏内容失败");
      }
    }
  } catch (error) {
    console.error("切换状态出错", error);
    ElMessage.error("切换状态失败，请稍后重试");
  }
};

const deleteContent = async (id) => {
  const confirmResult = await ElMessageBox.confirm(
    "确定要删除该内容吗？",
    "提示",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  );
  if (confirmResult === "confirm") {
    try {
      const res = await deleteNews({ id });
      if (res.data && res.data.code === 200) {
        ElMessage.success(res.data.message);
        await fetchContentList();
      } else {
        ElMessage.error(res.data.message || "删除内容失败");
      }
    } catch (error) {
      console.error("删除内容出错", error);
      ElMessage.error("删除内容失败，请稍后重试");
    }
  }
};

const contentRules = {
  type: [{ required: true, message: "请选择内容类型", trigger: "change" }],
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  content: [{ required: true, message: "请输入内容", trigger: "blur" }],
};

onMounted(fetchContentList);
</script>

<style scoped>
.admin-container {
  width: 90%;
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.admin-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.content-filters {
  display: flex;
  gap: 10px;
  width: 300px;
}

.content-list {
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
