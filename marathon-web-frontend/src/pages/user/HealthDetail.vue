<template>
  <div class="user-center-page">
    <section class="user-center-card">
      <div class="user-center-header">
        <div>
          <span class="app-eyebrow">HEALTH</span>
          <h1>健康审核及档案</h1>
          <p>查看健康审核状态、历史档案和材料有效期。</p>
        </div>
      </div>
      <!-- 健康审核状态和按钮 -->
      <div class="health-status-row">
        <p class="status-text">健康审核状态：{{ convertHealthStatus(healthStatus) }}</p>
        <template v-if="healthStatus !== 1">
          <el-button @click="router.push('/user/healthaudit')" type="primary">
            前往健康审核
          </el-button>
        </template>
      </div>

      <!-- 健康档案列表部分 -->
      <div class="table-card">
        <el-table
          :data="healthRecords"
          :border="true"
          stripe
          style="width: 100%; height: 100%"
          v-loading="loading"
        >
          <el-table-column
            prop="id"
            label="档案 ID"
            width="200"
            align="center"
          ></el-table-column>

          <!-- <el-table-column
            prop="riskLevel"
            label="风险等级"
            width="100"
            align="center"
          ></el-table-column> -->
          <el-table-column prop="reportUrl" label="体检报告" align="center">
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
          <el-table-column prop="appendixUrl" label="附件链接" align="center">
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
            width="150"
            align="center"
          ></el-table-column>
          <el-table-column
            prop="auditStatusText"
            label="审核状态"
            width="100"
            align="center"
          ></el-table-column>
          <el-table-column
            prop="validTime"
            label="有效日期"
            width="180"
            align="center"
          ></el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="{ row }" class="button-container">
              <template v-if="row.auditStatus === 2">
                <el-button
                  type="warning"
                  size="small"
                  @click="showReSubmitForm(row.id)"
                >
                  重新提交材料
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 重新提交材料表单 -->
      <el-dialog
        v-model="isReSubmitFormVisible"
        title="重新提交材料"
        width="400px"
      >
        <el-form label-width="80px">
          <el-form-item label="体检报告">
            <el-button @click="handleChooseFile">选择文件</el-button>
            <input
              type="file"
              ref="fileInput"
              style="display: none"
              @change="onFileChange"
            />
            <div v-if="selectedFile">{{ selectedFile.name }}</div>
          </el-form-item>
          <el-form-item label="附件">
            <el-button @click="handleChooseAppendixFile">选择附件</el-button>
            <input
              type="file"
              ref="appendixFileInput"
              style="display: none"
              @change="onAppendixFileChange"
            />
            <div v-if="selectedAppendixFile">
              {{ selectedAppendixFile.name }}
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="isReSubmitFormVisible = false">取消</el-button>
            <el-button type="primary" @click="submitReSubmitForm"
              >提交</el-button
            >
          </span>
        </template>
      </el-dialog>

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
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getUserDetail } from "@/api/user";
import { getHealthByUserId } from "@/api/user";
import { ElMessage, ElDialog, ElForm, ElInput } from "element-plus";
import router from "@/router";
import { reSubmit, fileUpload } from "@/api/user";

const loading = ref(false); // 添加加载状态

// 定义健康审核状态转换函数
const convertHealthStatus = (status) => {
  switch (status) {
    case 0:
      return "未审核";
    case 1:
      return "可参加全部赛事";
    case 2:
      return "可参加除全马以外赛事";
    case 3:
      return "可参加10、5公里";
    case 4:
      return "不宜参加任何赛事";
    case 5:
      return "审核中";
    default:
      return "未知状态";
  }
};

// 定义审核状态转换函数
const convertAuditStatus = (status) => {
  switch (status) {
    case 0:
      return "待审核";
    case 1:
      return "已审核";
    case 2:
      return "材料有误";
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

const healthStatus = ref(0);
const healthRecords = ref([]);
const isReSubmitFormVisible = ref(false);
const selectedRecordId = ref(null);
const fileInput = ref(null);
const selectedFile = ref(null);
const appendixFileInput = ref(null);
const selectedAppendixFile = ref(null);
const previewDialogVisible = ref(false);
const currentPreviewUrl = ref("");

const fetchHealthStatus = async () => {
  try {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      console.error("未找到用户 ID，请确保已登录。");
      return;
    }
    const res = await getUserDetail({ id: userId });
    if (res.data && res.data.data) {
      healthStatus.value = res.data.data.healthStatus;
    }
  } catch (error) {
    console.error("获取健康审核状态失败:", error);
    ElMessage.error("获取健康审核状态失败，请稍后重试");
  }
};

const fetchHealthRecords = async () => {
  try {
    loading.value = true;
    const userId = localStorage.getItem("userId");
    if (!userId) {
      console.error("未找到用户 ID，请确保已登录。");
      return;
    }
    const res = await getHealthByUserId({ userId });
    if (res.data && res.data.data) {
      // 按提交时间降序排列（最新的在最前面）
      healthRecords.value = res.data.data
        .sort((a, b) => new Date(b.submitTime) - new Date(a.submitTime))
        .map((record) => ({
          ...record,
          submitTime: formatDate(record.submitTime),
          validTime: formatDate(record.validTime),
          auditStatusText: convertAuditStatus(record.auditStatus),
        }));
    }
  } catch (error) {
    console.error("获取健康档案失败:", error);
    ElMessage.error("获取健康档案失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

// 显示重新提交材料表单方法
const showReSubmitForm = (id) => {
  selectedRecordId.value = id;
  isReSubmitFormVisible.value = true;
};

// 提交重新提交材料表单方法
const submitReSubmitForm = async () => {
  if (!selectedFile.value) {
    ElMessage.error("请选择体检报告文件");
    return;
  }

  try {
    const formData = new FormData();
    formData.append("file", selectedFile.value);
    const response = await fileUpload(formData);
    console.log("文件上传成功", response.data.data);
    const reportUrl = response.data.data;

    let appendixUrl = null;
    if (selectedAppendixFile.value) {
      const appendixFormData = new FormData();
      appendixFormData.append("file", selectedAppendixFile.value);
      const appendixResponse = await fileUpload(appendixFormData);
      console.log("附件上传成功", appendixResponse.data.data);
      appendixUrl = appendixResponse.data.data;
    }

    const param = {
      docId: selectedRecordId.value,
      reportUrl,
      appendixUrl,
    };

    try {
      const res = await reSubmit(param);
      console.log("重新提交成功", res);
      ElMessage.success("材料已重新提交，请等待审核");
      isReSubmitFormVisible.value = false;
      await fetchHealthRecords(); // 重新获取健康档案列表
      selectedFile.value = null; // 清空已选择的文件
    } catch (error) {
      console.error("重新提交失败", error);
      ElMessage.error("重新提交失败，请稍后重试");
    }
  } catch (error) {
    console.error("文件上传失败", error);
    ElMessage.error("文件上传失败，请稍后重试");
  }
};

const handleChooseFile = () => {
  fileInput.value.click();
};

const onFileChange = (e) => {
  selectedFile.value = e.target.files[0];
};

const handleChooseAppendixFile = () => {
  appendixFileInput.value.click();
};

const onAppendixFileChange = (e) => {
  selectedAppendixFile.value = e.target.files[0];
};

const previewImage = (url) => {
  currentPreviewUrl.value = url;
  previewDialogVisible.value = true;
};

onMounted(async () => {
  await fetchHealthStatus();
  await fetchHealthRecords();
});
</script>

<style scoped>
.health-status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  border: 1px solid rgba(33, 53, 82, 0.08);
  border-radius: 16px;
  padding: 20px;
  background: linear-gradient(145deg, rgba(15, 118, 110, 0.09), rgba(215, 168, 79, 0.1));
}

.status-text {
  margin: 0;
  color: var(--app-text);
  font-size: 16px;
  font-weight: 700;
}

.table-card {
  border: 1px solid rgba(33, 53, 82, 0.08);
  border-radius: 16px;
  padding: 20px;
  background: rgba(248, 250, 252, 0.72);
  width: 100%;
}

.button-container {
  display: flex;
  justify-content: center;
  gap: 8px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .health-status-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .table-card {
    padding: 10px;
  }

  table th,
  table td {
    font-size: 14px;
  }

  .button-container {
    flex-direction: column;
    gap: 4px;
  }
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
</style>
