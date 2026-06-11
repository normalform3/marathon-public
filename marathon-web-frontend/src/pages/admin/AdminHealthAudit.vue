<template>
  <div class="comment-container">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold">健康档案管理</h2>

      <!-- 筛选按钮调整到右侧，与标题同高度 -->
      <div class="flex items-center">
        <el-button
          type="primary"
          size="small"
          @click="filterPending = !filterPending"
        >
          {{ filterPending ? "显示全部" : "仅显示待定" }}
        </el-button>
      </div>
    </div>

    <!-- 健康档案列表部分 -->
    <div class="bg-white shadow-md rounded-lg p-6">
      <el-table
        :data="displayRecords"
        stripe
        :border="true"
        style="width: 100%; height: 100%"
      >
        <el-table-column
          prop="id"
          label="档案 ID"
          width="180"
          align="center"
        ></el-table-column>
        <el-table-column
          prop="reportUrl"
          label="体检报告"
          align="center"
          width="200"
        >
          <template #default="scope">
            <el-button
              type="primary"
              link
              @click="previewImage(scope.row.reportUrl)"
            >
              查看体检报告
            </el-button>
          </template>
        </el-table-column>
        <el-table-column
          prop="appendixUrl"
          label="附件链接"
          align="center"
          width="200"
        >
          <template #default="scope">
            <el-button
              type="primary"
              link
              @click="previewImage(scope.row.appendixUrl)"
            >
              查看附件
            </el-button>
          </template>
        </el-table-column>
        <el-table-column
          prop="submitTime"
          label="提交时间"
          width="180"
          align="center"
        ></el-table-column>
        <el-table-column
          prop="auditStatusText"
          label="审核状态"
          width="100"
          align="center"
        ></el-table-column>
        <el-table-column label="操作" width="300" align="center">
          <template #default="{ row }" class="flex justify-center space-x-2">
            <!-- 通过、材料有误、不合格按钮 -->
            <el-button
              v-if="
                (filterPending && row.auditStatus === 4) ||
                (!filterPending && row.auditStatus === 0)
              "
              type="success"
              size="small"
              @click="approveHealthRecord(row.id)"
            >
              通过
            </el-button>
            <el-button
              v-if="
                (filterPending && row.auditStatus === 4) ||
                (!filterPending && row.auditStatus === 0)
              "
              type="warning"
              size="small"
              @click="rejectHealthRecord(row.id)"
            >
              材料有误
            </el-button>
            <el-button
              v-if="
                (filterPending && row.auditStatus === 4) ||
                (!filterPending && row.auditStatus === 0)
              "
              type="danger"
              size="small"
              @click="markAsUnqualified(row.id)"
            >
              不合格
            </el-button>
            <!-- 待定按钮：仅在显示全部且状态为待审核时显示 -->
            <el-button
              v-if="!filterPending && row.auditStatus === 0"
              type="info"
              size="small"
              @click="setStatusPending(row.id)"
            >
              待定
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 图片预览弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="材料预览"
      width="50%"
      :close-on-click-modal="true"
    >
      <div class="preview-container">
        <img :src="currentPreviewUrl" class="preview-image" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from "vue";
import {
  pass,
  notPass,
  notQualified,
  getAllDocs,
  setPending,
} from "@/api/admin";
import { ElMessage } from "element-plus";

// 定义审核状态转换函数
const convertAuditStatus = (status) => {
  switch (status) {
    case 0:
      return "待审核";
    case 1:
      return "已审核";
    case 2:
      return "材料有误";
    case 3:
      return "已过期";
    case 4:
      return "待定";
    default:
      return "未知状态";
  }
};

// 日期格式化函数
const formatDate = (date) => {
  if (!date) return "";
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const healthRecords = ref([]);
const filterPending = ref(false);

// 计算属性，根据筛选条件显示记录
const displayRecords = computed(() => {
  if (filterPending.value) {
    return healthRecords.value.filter((record) => record.auditStatus === 4);
  }
  return healthRecords.value;
});

const fetchHealthRecords = async () => {
  try {
    const res = await getAllDocs();
    if (res.data && res.data.data) {
      healthRecords.value = res.data.data.map((record) => ({
        ...record,
        submitTime: formatDate(record.submitTime),
        validTime: formatDate(record.validTime),
        auditStatusText: convertAuditStatus(record.auditStatus),
      }));
    }
  } catch (error) {
    console.error("获取健康档案失败:", error);
    ElMessage.error("获取健康档案失败，请稍后重试");
  }
};

// 通过审核方法
const approveHealthRecord = async (id) => {
  try {
    await pass({ docId: id });
    ElMessage.success("审核通过");
    await fetchHealthRecords();
  } catch (error) {
    console.error("审核通过失败:", error);
    ElMessage.error("审核通过失败，请稍后重试");
  }
};

// 材料有误方法
const rejectHealthRecord = async (id) => {
  try {
    await notPass({ docId: id });
    ElMessage.success("已标记为材料有误");
    await fetchHealthRecords();
  } catch (error) {
    console.error("标记材料有误失败:", error);
    ElMessage.error("标记材料有误失败，请稍后重试");
  }
};

// 不合格方法
const markAsUnqualified = async (id) => {
  try {
    await notQualified({ docId: id });
    ElMessage.success("已标记为不合格");
    await fetchHealthRecords();
  } catch (error) {
    console.error("标记不合格失败:", error);
    ElMessage.error("标记不合格失败，请稍后重试");
  }
};

// 设为待定方法
const setStatusPending = async (id) => {
  try {
    await setPending({ docId: id });
    ElMessage.success("已设为待定");
    await fetchHealthRecords();
  } catch (error) {
    console.error("设为待定失败:", error);
    ElMessage.error("设为待定失败，请稍后重试");
  }
};

// 图片预览相关
const previewDialogVisible = ref(false);
const currentPreviewUrl = ref("");

const previewImage = (url) => {
  currentPreviewUrl.value = url;
  previewDialogVisible.value = true;
};

onMounted(async () => {
  await fetchHealthRecords();
});
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

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}
</style>
