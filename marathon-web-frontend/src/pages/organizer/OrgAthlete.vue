<template>
  <div class="comment-container">
    <div class="comment-header">
      <h2>运动员分组信息列表</h2>
    </div>
    <div class="flex justify-between">
      <div class="w-1 table-container">
        <el-table :data="athletesGroup1" :border="true" style="width: 100%">
          <el-table-column
            prop="name"
            label="运动员名字"
            align="center"
          ></el-table-column>
          <el-table-column
            prop="athleteNumber"
            label="运动员参赛号"
            align="center"
          ></el-table-column>
          <el-table-column
            prop="phone"
            label="联系电话"
            align="center"
          ></el-table-column>
        </el-table>
      </div>
      <div class="w-1 table-container">
        <el-table :data="athletesGroup2" :border="true" style="width: 100%">
          <el-table-column
            prop="name"
            label="运动员名字"
            align="center"
          ></el-table-column>
          <el-table-column
            prop="athleteNumber"
            label="运动员参赛号"
            align="center"
          ></el-table-column>
          <el-table-column
            prop="phone"
            label="联系电话"
            align="center"
          ></el-table-column>
        </el-table>
      </div>
      <div class="w-1 table-container">
        <el-table :data="athletesGroup3" :border="true" style="width: 100%">
          <el-table-column
            prop="name"
            label="运动员名字"
            align="center"
          ></el-table-column>
          <el-table-column
            prop="athleteNumber"
            label="运动员参赛号"
            align="center"
          ></el-table-column>
          <el-table-column
            prop="phone"
            label="联系电话"
            align="center"
          ></el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getAthletes } from "@/api/org";

const athletes = ref([]);
const athletesGroup1 = ref([]);
const athletesGroup2 = ref([]);
const athletesGroup3 = ref([]);

const fetchAthletes = async () => {
  const raceId = localStorage.getItem("raceId");
  if (!raceId) {
    console.error("未找到 raceId");
    return;
  }
  try {
    const response = await getAthletes({ raceId });
    athletes.value = response.data.data;
    const total = athletes.value.length;
    const perGroup = Math.ceil(total / 3);
    athletesGroup1.value = athletes.value.slice(0, perGroup);
    athletesGroup2.value = athletes.value.slice(perGroup, perGroup * 2);
    athletesGroup3.value = athletes.value.slice(perGroup * 2);
  } catch (error) {
    console.error("获取运动员信息失败:", error);
  }
};

onMounted(() => {
  fetchAthletes();
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

.flex {
  display: flex;
  flex-wrap: nowrap; /* 禁止换行，使三个表格在一行显示 */
}

.w-1 {
  width: 33.33%; /* 确保每个表格占据三分之一的宽度 */
}

.table-container {
  margin: 0 10px; /* 设置左右外边距，可根据需求调整间距大小 */
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
