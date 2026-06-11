<template>
  <div class="comment-container">
    <div class="comment-header">
      <h2>模板下载与成绩上传</h2>
    </div>
    <div>
      <el-button :disabled="!canDownload" @click="downloadTemplate"
        >下载模板</el-button
      >
      <el-button :disabled="!canUpload" @click="handleUploadClick"
        >上传成绩</el-button
      >
      <input
        ref="uploadInput"
        type="file"
        style="display: none"
        @change="handleFileUpload"
      />
    </div>
    <h3>成绩登记请使用 HH:MM:SS 的格式，谢谢</h3>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getTemplate, uploadGrades, getRaceStatus } from "@/api/org";

const uploadInput = ref(null);
const canDownload = ref(false);
const canUpload = ref(false);

const getStatus = async () => {
  try {
    const raceId = localStorage.getItem("raceId");
    if (!raceId) {
      console.error("未找到 raceId");
      return;
    }
    const response = await getRaceStatus({ raceId: parseInt(raceId) });
    const status = response.data.data;
    console.log("赛事状态:", status);
    if (status === 3) {
      canDownload.value = true;
    } else if (status === 4) {
      canDownload.value = true;
      canUpload.value = true;
    }
  } catch (error) {
    console.error("获取赛事状态失败", error);
  }
};

const downloadTemplate = async () => {
  try {
    const raceId = localStorage.getItem("raceId");
    if (!raceId) {
      console.error("未找到 raceId");
      return;
    }
    const response = await getTemplate({ raceId: parseInt(raceId) });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "score_template.xlsx");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error("下载模板失败", error);
  }
};

const handleUploadClick = () => {
  if (uploadInput.value) {
    uploadInput.value.click();
  }
};

const handleFileUpload = async () => {
  const file = uploadInput.value.files[0];
  if (!file) return;
  const formData = new FormData();
  formData.append("file", file);
  const raceId = localStorage.getItem("raceId");
  formData.append("raceId", raceId);

  try {
    await uploadGrades(formData);
    console.log("文件上传成功");
    uploadInput.value.value = "";
  } catch (error) {
    console.error("文件上传失败", error);
  }
};

onMounted(() => {
  getStatus();
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
</style>
