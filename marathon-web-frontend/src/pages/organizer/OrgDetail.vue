<template>
  <div class="comment-container">
    <div class="comment-header">
      <h2>{{ raceDetail.raceName }}</h2>
      <el-button @click="showEditDialog = true">修改赛事信息</el-button>
    </div>
    <div class="race-details-container">
      <div class="race-details">
        <!-- 赛事详情 -->
        <div class="detail-content">
          <p>赛事编号：{{ raceDetail.raceId }}</p>
          <p>赛事类型：{{ convertRaceType(raceDetail.raceType) }}</p>
          <p>地点：{{ raceDetail.location }}</p>
          <p>赛事时间：{{ formatDate(raceDetail.raceDate) }}</p>
          <p>赛事状态：{{ convertRaceStatus(raceDetail.raceStatus) }}</p>
          <p>报名模式：{{ convertRegistrationMode(raceDetail.registrationMode) }}</p>
          <p>赛事信息：{{ raceDetail.raceInfo }}</p>
          <p>报名开始时间：{{ formatDate(raceDetail.enrollStart) }}</p>
          <p>报名结束时间：{{ formatDate(raceDetail.enrollEnd) }}</p>
          <p>费用：{{ raceDetail.fee }}</p>

          <!-- 参赛人数图表 -->
          <div class="chart-container">
            <div
              id="participantsChart"
              style="width: 100%; height: 300px"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="showEditDialog" title="修改赛事信息" width="60%">
      <el-form :model="editRaceDetail" :rules="editRules" ref="editFormRef">
        <el-form-item label="赛事编号" prop="raceId">
          <el-input v-model="editRaceDetail.raceId" disabled></el-input>
        </el-form-item>
        <el-form-item label="赛事类型" prop="raceType">
          <el-select
            v-model="editRaceDetail.raceType"
            placeholder="请选择赛事类型"
          >
            <el-option label="全马" value="1"></el-option>
            <el-option label="半马" value="2"></el-option>
            <el-option label="10 公里" value="3"></el-option>
            <el-option label="5 公里" value="4"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="editRaceDetail.location"></el-input>
        </el-form-item>
        <el-form-item label="赛事时间" prop="raceDate">
          <el-date-picker
            v-model="editRaceDetail.raceDate"
            type="datetime"
            placeholder="选择日期时间"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="赛事状态" prop="raceStatus">
          <el-select
            v-model="editRaceDetail.raceStatus"
            placeholder="请选择赛事状态"
          >
            <el-option label="未开始" value="1"></el-option>
            <el-option label="报名中" value="2"></el-option>
            <el-option label="报名结束" value="3"></el-option>
            <el-option label="已结束" value="4"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="赛事信息" prop="raceInfo">
          <el-input
            type="textarea"
            v-model="editRaceDetail.raceInfo"
            :rows="4"
          ></el-input>
        </el-form-item>
        <el-form-item label="报名模式" prop="registrationMode">
          <el-select
            v-model="editRaceDetail.registrationMode"
            placeholder="请选择报名模式"
          >
            <el-option label="超额报名后抽签" value="1"></el-option>
            <el-option label="限额报名，报完即止" value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="报名开始时间" prop="enrollStart">
          <el-date-picker
            v-model="editRaceDetail.enrollStart"
            type="datetime"
            placeholder="选择日期时间"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="报名结束时间" prop="enrollEnd">
          <el-date-picker
            v-model="editRaceDetail.enrollEnd"
            type="datetime"
            placeholder="选择日期时间"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="费用" prop="fee">
          <el-input
            v-model.number="editRaceDetail.fee"
            type="number"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitEditForm">修改</el-button>
          <el-button @click="showEditDialog = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, watch } from "vue";
import { getRaceDetail } from "@/api/user";
import { getRegistCount, existRoute } from "@/api/org";
import { useRouter } from "vue-router";
import * as echarts from "echarts";
import { ElMessage } from "element-plus";
import { updateRace } from "@/api/org";

// 定义类型转换函数
const convertRaceType = (type) => {
  switch (type) {
    case 1:
      return "全马";
    case 2:
      return "半马";
    case 3:
      return "10 公里";
    case 4:
      return "5 公里";
    default:
      return "未知类型";
  }
};

const convertRaceStatus = (status) => {
  const numStatus = Number(status); // 确保 status 是数字
  switch (numStatus) {
    case 1:
      return "未开始";
    case 2:
      return "报名中";
    case 3:
      return "报名结束";
    case 4:
      return "已结束";
    default:
      return "未知状态";
  }
};

const convertRegistrationMode = (mode) => {
  switch (Number(mode || 1)) {
    case 1:
      return "超额报名后抽签";
    case 2:
      return "限额报名，报完即止";
    default:
      return "未知模式";
  }
};

// 日期格式化函数，精确到小时
const formatDate = (date) => {
  if (!date) return "";
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const hour = String(d.getHours()).padStart(2, "0");
  return `${year}-${month}-${day} ${hour}:00`;
};

const raceDetail = ref({});
const registCount = ref(0);
const hasRoute = ref(false);
const showEditDialog = ref(false);
const loading = ref(false); // 添加加载状态

// 编辑表单数据
const editRaceDetail = reactive({
  id: "", // 添加id字段，对应数据库主键
  raceId: "",
  raceName: "",
  raceType: "",
  location: "",
  raceDate: "",
  raceStatus: "",
  registrationMode: "1",
  raceInfo: "",
  enrollStart: "",
  enrollEnd: "",
  fee: 0,
});

// 表单验证规则
const editRules = {
  raceName: [{ required: true, message: "请输入赛事名称", trigger: "blur" }],
  raceType: [{ required: true, message: "请选择赛事类型", trigger: "change" }],
  location: [{ required: true, message: "请输入地点", trigger: "blur" }],
  raceDate: [{ required: true, message: "请选择赛事时间", trigger: "change" }],
  raceStatus: [
    { required: true, message: "请选择赛事状态", trigger: "change" },
  ],
  registrationMode: [
    { required: true, message: "请选择报名模式", trigger: "change" },
  ],
  enrollStart: [
    { required: true, message: "请选择报名开始时间", trigger: "change" },
  ],
  enrollEnd: [
    { required: true, message: "请选择报名结束时间", trigger: "change" },
    {
      validator: (rule, value, callback) => {
        if (
          value &&
          editRaceDetail.enrollStart &&
          new Date(value) <= new Date(editRaceDetail.enrollStart)
        ) {
          callback(new Error("报名结束时间必须晚于报名开始时间"));
        } else {
          callback();
        }
      },
      trigger: "change",
    },
  ],
  fee: [
    { required: true, message: "请输入费用", trigger: "blur" },
    { type: "number", message: "费用必须为数字值", trigger: "blur" },
  ],
};

const editFormRef = ref(null);

// 初始化图表
const initChart = () => {
  const chartDom = document.getElementById("participantsChart");
  if (!chartDom) return;

  const myChart = echarts.init(chartDom);
  const option = {
    title: {
      text: "参赛人数统计",
      left: "center",
      textStyle: {
        color: "#333",
        fontSize: 16,
        fontWeight: "bold",
      },
    },
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow",
      },
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: ["参赛人数", "已报名人数"],
      axisLine: {
        lineStyle: {
          color: "#999",
        },
      },
      axisLabel: {
        color: "#666",
      },
    },
    yAxis: {
      type: "value",
      axisLine: {
        show: true,
        lineStyle: {
          color: "#999",
        },
      },
      axisLabel: {
        color: "#666",
      },
      splitLine: {
        lineStyle: {
          color: "#eee",
        },
      },
    },
    series: [
      {
        name: "人数",
        type: "bar",
        data: [
          {
            value: raceDetail.value.participantNumber,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "#83bff6" },
                { offset: 0.5, color: "#188df0" },
                { offset: 1, color: "#0a5bbf" },
              ]),
            },
          },
          {
            value: registCount.value,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "#ff9a9e" },
                { offset: 0.5, color: "#fad0c4" },
                { offset: 1, color: "#f47d7d" },
              ]),
            },
          },
        ],
        barWidth: "40%",
        label: {
          show: true,
          position: "top",
          formatter: "{c}",
          color: "#333",
        },
      },
    ],
  };

  myChart.setOption(option);

  // 响应式调整
  window.addEventListener("resize", function () {
    myChart.resize();
  });
};

const fetchRaceDetail = async () => {
  try {
    // 从localStorage里获取raceId
    const raceId = localStorage.getItem("raceId");
    if (!raceId) {
      console.error("未在localStorage中找到raceId");
      return;
    }
    const res = await getRaceDetail({ id: parseInt(raceId) });
    const count = await getRegistCount({ raceId: parseInt(raceId) });
    if (res.data && res.data.data) {
      raceDetail.value = res.data.data;
      raceDetail.value.typeText = convertRaceType(raceDetail.value.raceType);
      raceDetail.value.statusText = convertRaceStatus(
        raceDetail.value.raceStatus
      );

      // 初始化编辑表单数据，注意添加id字段
      editRaceDetail.id = raceDetail.value.id;
      editRaceDetail.raceId = raceDetail.value.raceId;
      editRaceDetail.raceName = raceDetail.value.raceName;
      editRaceDetail.raceType = String(raceDetail.value.raceType);
      editRaceDetail.location = raceDetail.value.location;
      editRaceDetail.raceDate = raceDetail.value.raceDate;
      editRaceDetail.raceStatus = String(raceDetail.value.raceStatus);
      editRaceDetail.registrationMode = String(raceDetail.value.registrationMode || 1);
      editRaceDetail.raceInfo = raceDetail.value.raceInfo;
      editRaceDetail.enrollStart = raceDetail.value.enrollStart;
      editRaceDetail.enrollEnd = raceDetail.value.enrollEnd;
      editRaceDetail.fee = raceDetail.value.fee;
    }
    if (count.data && count.data.data) {
      registCount.value = count.data.data;
      // 数据获取完成后初始化图表
      initChart();
    }
    // 检查是否存在路线
    const routeExists = await existRoute({ raceId: parseInt(raceId) });
    if (routeExists.data.code === 200) {
      hasRoute.value = true;
    } else {
      hasRoute.value = false;
    }
  } catch (error) {
    console.error("获取赛事详情失败:", error);
  }
};

// 检查三个时间的合理性
const validateRaceTimes = () => {
  const enrollStart = new Date(editRaceDetail.enrollStart);
  const enrollEnd = new Date(editRaceDetail.enrollEnd);
  const raceDate = new Date(editRaceDetail.raceDate);

  // 检查是否都有值
  if (isNaN(enrollStart.getTime())) {
    ElMessage.error("请选择有效的报名开始时间");
    return false;
  }

  if (isNaN(enrollEnd.getTime())) {
    ElMessage.error("请选择有效的报名结束时间");
    return false;
  }

  if (isNaN(raceDate.getTime())) {
    ElMessage.error("请选择有效的赛事开始时间");
    return false;
  }

  // 检查报名结束时间是否晚于报名开始时间
  if (enrollEnd <= enrollStart) {
    ElMessage.error("报名结束时间必须晚于报名开始时间");
    return false;
  }

  // 检查赛事开始时间是否晚于报名结束时间
  if (raceDate <= enrollEnd) {
    ElMessage.error("赛事开始时间必须晚于报名结束时间");
    return false;
  }

  return true;
};

// 提交编辑表单
const submitEditForm = () => {
  if (!editFormRef.value) return;

  editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true;

        // 检查三个时间的合理性
        if (!validateRaceTimes()) {
          loading.value = false;
          return;
        }

        // 准备提交的数据
        const submitData = {
          id: editRaceDetail.id,
          raceId: editRaceDetail.raceId,
          raceName: editRaceDetail.raceName,
          raceType: parseInt(editRaceDetail.raceType),
          location: editRaceDetail.location,
          raceDate: editRaceDetail.raceDate,
          raceStatus: parseInt(editRaceDetail.raceStatus),
          registrationMode: parseInt(editRaceDetail.registrationMode),
          raceInfo: editRaceDetail.raceInfo,
          enrollStart: editRaceDetail.enrollStart,
          enrollEnd: editRaceDetail.enrollEnd,
          fee: editRaceDetail.fee,
        };

        // 调用后端API
        const res = await updateRace(submitData);

        console.log("API响应:", res);

        if (res.data.code === 200) {
          ElMessage.success("修改成功！");
          showEditDialog.value = false;

          // 重新获取赛事详情以更新UI
          await fetchRaceDetail();
        } else {
          ElMessage.error(res.message || "修改失败，请稍后重试");
        }
      } catch (error) {
        console.error("修改赛事信息失败:", error);
        ElMessage.error("请检查赛事时间合理性");
      } finally {
        loading.value = false;
      }
    } else {
      ElMessage.error("表单验证失败，请检查输入！");
      return false;
    }
  });
};

onMounted(fetchRaceDetail);
</script>

<style scoped>
.comment-container {
  width: 90%;
  max-width: 800px;
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

.race-details-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.race-details {
  width: 80%;
  max-width: 600px;
  padding: 20px;
}

.title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-content {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.chart-container {
  margin-top: 30px;
  padding: 10px;
  background-color: #f9fafc;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}
</style>
