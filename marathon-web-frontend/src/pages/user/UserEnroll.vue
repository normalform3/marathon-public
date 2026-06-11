<template>
  <div class="user-center-page">
    <section class="user-center-card">
      <div class="user-center-header">
        <div>
          <span class="app-eyebrow">REGISTRATION</span>
          <h1>我的报名</h1>
          <p>查看报名状态、支付状态和参赛号，快速进入赛事详情。</p>
        </div>
        <el-button plain @click="fetchRegistrationList">刷新</el-button>
      </div>

      <div class="user-stat-grid">
        <div class="user-stat">
          <span>报名记录</span>
          <strong>{{ registrationList.length }}</strong>
        </div>
        <div class="user-stat">
          <span>待支付</span>
          <strong>{{ unpaidCount }}</strong>
        </div>
        <div class="user-stat">
          <span>已支付</span>
          <strong>{{ paidCount }}</strong>
        </div>
      </div>

      <el-table :data="registrationList" stripe style="width: 100%" empty-text="暂无报名记录">
        <el-table-column prop="raceName" label="赛事名称" min-width="200" align="left" />
        <el-table-column label="赛事类型" width="110" align="center">
          <template #default="{ row }">
            {{ convertRaceType(row.raceType) }}
          </template>
        </el-table-column>
        <el-table-column label="报名状态" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="registrationTag(row.status)" effect="light">
              {{ convertRegistrationStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="convertPaymentStatus(row.status) === '已支付' ? 'success' : 'warning'" effect="light">
              {{ convertPaymentStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="参赛号" width="110" align="center">
          <template #default="{ row }">
            {{ row.athleteNumber || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="getRaceDetail(row.raceId)">
              查看详情
            </el-button>
            <el-button
              v-if="convertPaymentStatus(row.status) === '未支付'"
              type="success"
              size="small"
              @click="goToPayment(row.raceId)"
            >
              前往支付
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { getUserRegistrationList } from "@/api/user";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { raceTypeText } from "@/utils/display";

const registrationList = ref([]);
const router = useRouter();

const unpaidCount = computed(
  () => registrationList.value.filter((item) => convertPaymentStatus(item.status) === "未支付").length,
);
const paidCount = computed(
  () => registrationList.value.filter((item) => convertPaymentStatus(item.status) === "已支付").length,
);

const convertRaceType = (type) => raceTypeText(type);

const convertRegistrationStatus = (status) => {
  const numStatus = Number(status);
  switch (numStatus) {
    case 1:
      return "待抽签";
    case 2:
      return "已取消";
    case 3:
      return "未中签";
    case 4:
    case 5:
      return "已中签";
    default:
      return "未知状态";
  }
};

const registrationTag = (status) => {
  const numStatus = Number(status);
  if (numStatus === 4 || numStatus === 5) return "success";
  if (numStatus === 3) return "danger";
  if (numStatus === 2) return "info";
  return "warning";
};

const convertPaymentStatus = (status) => {
  const numStatus = Number(status);
  switch (numStatus) {
    case 4:
      return "未支付";
    case 5:
      return "已支付";
    case 1:
    case 2:
    case 3:
      return "-";
    default:
      return "未知";
  }
};

const fetchRegistrationList = async () => {
  try {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      ElMessage.error("请先登录");
      return;
    }
    const param = {
      userId: parseInt(userId),
      raceId: null,
      status: null,
    };
    const res = await getUserRegistrationList(param);
    if (res.data && res.data.data) {
      registrationList.value = res.data.data;
    }
  } catch (error) {
    console.error("获取用户报名信息列表失败:", error);
    ElMessage.error("获取报名信息失败，请稍后重试");
  }
};

const getRaceDetail = (raceId) => {
  router.push(`/race/detail/${raceId}`);
};

const goToPayment = (raceId) => {
  router.push(`/user/payment/${raceId}`);
};

onMounted(fetchRegistrationList);
</script>
