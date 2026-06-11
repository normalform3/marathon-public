<template>
  <div class="comment-container">
    <h2 class="text-2xl font-bold text-center mb-6" style="text-align: center">
      赛事申请审批
    </h2>

    <!-- 申请列表部分 -->
    <div class="bg-white shadow-md rounded-lg p-6">
      <el-table :data="applyList" stripe :border="true" style="width: 100%">
        <el-table-column
          prop="name"
          label="举办方名称"
          min-width="120"
          align="center"
        ></el-table-column>
        <el-table-column
          prop="applyTime"
          label="申请时间"
          min-width="160"
          align="center"
        >
          <template #default="scope">
            {{ formatDateTime(scope.row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="applyStatusText"
          label="申请状态"
          min-width="100"
          align="center"
        ></el-table-column>
        <el-table-column
          label="操作"
          min-width="100"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              v-if="row.applyStatus === 0"
              type="primary"
              size="small"
              @click="showDetail(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="赛事申请详情"
      width="800px"
      :close-on-click-modal="true"
      class="detail-dialog"
      destroy-on-close
    >
      <div class="detail-container">
        <!-- 举办方信息 -->
        <div class="info-section">
          <h3 class="section-title">举办方信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">举办方名称：</span>
              <span class="value">{{ currentApply.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">类型：</span>
              <span class="value">{{ getTypeText(currentApply.type) }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系人姓名：</span>
              <span class="value">{{ currentApply.contactName }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系人电话：</span>
              <span class="value">{{ currentApply.contactPhone }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系人邮箱：</span>
              <span class="value">{{ currentApply.contactEmail }}</span>
            </div>
          </div>

          <div class="materials-section">
            <div class="material-item">
              <h4 class="material-title">资质材料</h4>
              <div
                class="image-preview"
                @click="previewImage(currentApply.qualificationUrl)"
              >
                <img
                  :src="currentApply.qualificationUrl"
                  class="preview-thumbnail"
                />
                <div class="preview-overlay">
                  <el-icon><ZoomIn /></el-icon>
                </div>
              </div>
            </div>

            <div class="material-item">
              <h4 class="material-title">授权书</h4>
              <div
                class="image-preview"
                @click="previewImage(currentApply.authorizationUrl)"
              >
                <img
                  :src="currentApply.authorizationUrl"
                  class="preview-thumbnail"
                />
                <div class="preview-overlay">
                  <el-icon><ZoomIn /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 赛事信息 -->
        <div class="info-section">
          <h3 class="section-title">赛事信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">赛事名称：</span>
              <span class="value">{{ currentApply.raceName }}</span>
            </div>
            <div class="info-item">
              <span class="label">赛事类型：</span>
              <span class="value">{{
                getRaceTypeText(currentApply.raceType)
              }}</span>
            </div>
            <div class="info-item">
              <span class="label">参赛人数：</span>
              <span class="value">{{ currentApply.participantNumber }}</span>
            </div>
            <div class="info-item">
              <span class="label">报名模式：</span>
              <span class="value">{{
                getRegistrationModeText(currentApply.registrationMode)
              }}</span>
            </div>
            <div class="info-item">
              <span class="label">费用：</span>
              <span class="value">{{ currentApply.fee }}</span>
            </div>
            <div class="info-item">
              <span class="label">地点：</span>
              <span class="value">{{ currentApply.location }}</span>
            </div>
            <div class="info-item">
              <span class="label">开始时间：</span>
              <span class="value">{{
                formatDateTime(currentApply.raceDate)
              }}</span>
            </div>
          </div>
          <div class="race-info">
            <h4 class="info-subtitle">赛事简介</h4>
            <div class="race-info-content">
              {{ currentApply.raceInfo }}
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button type="success" @click="approveApply(currentApply.id)">
            通过
          </el-button>
          <el-button type="danger" @click="rejectApply(currentApply.id)">
            不通过
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 图片预览弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      width="60%"
      :close-on-click-modal="true"
      :show-close="true"
      center
    >
      <div class="preview-container">
        <img :src="currentPreviewUrl" class="preview-image" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getAllApply } from "@/api/admin";
// 引入接口
import {
  approveApply as apiApproveApply,
  rejectApply as apiRejectApply,
} from "@/api/admin";
import { ElMessage } from "element-plus";
import dayjs from "dayjs";
import { ZoomIn } from "@element-plus/icons-vue";

// 定义申请状态转换函数
const convertApplyStatus = (status) => {
  switch (status) {
    case 0:
      return "待审核";
    case 1:
      return "审核通过";
    case 2:
      return "审核不通过";
    default:
      return "未知状态";
  }
};

// 类型转换函数
const getTypeText = (type) => {
  switch (type) {
    case 1:
      return "政府";
    case 2:
      return "企业";
    case 3:
      return "个人";
    default:
      return "未知类型";
  }
};

// 赛事类型转换函数
const getRaceTypeText = (raceType) => {
  switch (raceType) {
    case 1:
      return "全马";
    case 2:
      return "半马";
    case 3:
      return "10公里";
    case 4:
      return "5公里";
    default:
      return "未知赛事类型";
  }
};

const getRegistrationModeText = (registrationMode) => {
  switch (Number(registrationMode || 1)) {
    case 1:
      return "超额报名后抽签";
    case 2:
      return "限额报名，报完即止";
    default:
      return "未知报名模式";
  }
};

// 日期时间格式化函数
const formatDateTime = (dateTime) => {
  if (!dateTime) return "";
  return dayjs(dateTime).format("YYYY-MM-DD HH:mm:ss");
};

const applyList = ref([]);
const detailDialogVisible = ref(false);
const currentApply = ref({});

const fetchApplyList = async () => {
  try {
    const res = await getAllApply();
    if (res.data && res.data.data) {
      applyList.value = res.data.data.map((apply) => ({
        ...apply,
        applyTime: formatDateTime(apply.applyTime),
        applyStatusText: convertApplyStatus(apply.applyStatus),
      }));
    }
  } catch (error) {
    console.error("获取申请列表失败:", error);
    ElMessage.error("获取申请列表失败，请稍后重试");
  }
};

// 查看详情方法
const showDetail = (apply) => {
  currentApply.value = apply;
  detailDialogVisible.value = true;
};

// 通过申请方法
const approveApply = async (id) => {
  console.log("通过申请 ID:", id); // 调试信息
  try {
    const response = await apiApproveApply({ applyId: id });
    ElMessage.success(response.data.data);
    await fetchApplyList();
    detailDialogVisible.value = false;
  } catch (error) {
    console.error("审核通过失败:", error);
    ElMessage.error("审核通过失败，请稍后重试");
  }
};

// 不通过申请方法
const rejectApply = async (id) => {
  try {
    const response = await apiRejectApply({ applyId: id });
    ElMessage.success(response.data.data);
    await fetchApplyList();
    detailDialogVisible.value = false;
  } catch (error) {
    console.error("审核不通过失败:", error);
    ElMessage.error("审核不通过失败，请稍后重试");
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
  await fetchApplyList();
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

.detail-container {
  padding: 0 20px;
}

.info-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #ebeef5;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  color: #606266;
  width: 90px;
  flex-shrink: 0;
  font-size: 14px;
}

.value {
  color: #303133;
  flex-grow: 1;
  font-size: 14px;
}

.materials-section {
  margin-top: 16px;
  display: flex;
  gap: 24px;
}

.material-item {
  flex: 1;
}

.material-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #606266;
}

.image-preview {
  width: 160px;
  height: 120px;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  position: relative;
}

.preview-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-preview:hover .preview-overlay {
  opacity: 1;
}

.preview-overlay .el-icon {
  color: white;
  font-size: 24px;
}

.race-info {
  margin-top: 16px;
}

.info-subtitle {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #606266;
}

.race-info-content {
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  font-size: 14px;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}
</style>
