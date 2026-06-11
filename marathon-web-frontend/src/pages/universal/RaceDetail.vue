<template>
  <div class="app-page app-page--wide race-detail-page">
    <section class="detail-hero app-card">
      <div>
        <span class="app-eyebrow">RACE DETAIL</span>
        <h1>{{ raceDetail.raceName || "赛事详情" }}</h1>
        <p>{{ raceDetail.raceInfo || "完整了解赛事时间、地点、报名方式和路线信息。" }}</p>
      </div>
      <el-tag :type="raceStatusTag(raceDetail.raceStatus)" size="large" effect="light">
        {{ convertRaceStatus(raceDetail.raceStatus) }}
      </el-tag>
    </section>

    <div class="detail-grid">
      <section class="app-card detail-card">
        <div class="app-section-title">
          <div>
            <h2>赛事信息</h2>
            <p>报名、参赛与时间安排一目了然。</p>
          </div>
        </div>

        <div class="info-grid">
          <div v-for="item in detailItems" :key="item.label" class="info-item">
            <span>{{ item.label }}</span>
            <strong>{{ item.value || "-" }}</strong>
          </div>
        </div>
      </section>

      <aside class="app-card enroll-card">
        <span class="app-eyebrow">ENTRY</span>
        <h2>{{ buttonText }}</h2>
        <p>
          报名开放时可直接提交报名申请；如需健康评估，系统会引导你前往对应页面。
        </p>
        <el-button
          type="primary"
          size="large"
          :disabled="isRegistered || raceDetail.raceStatus !== 2"
          @click="handleEnroll"
        >
          {{ buttonText }}
        </el-button>
      </aside>
    </div>

    <section class="app-card route-card">
      <div class="app-section-title">
        <div>
          <span class="app-eyebrow">ROUTE</span>
          <h2>赛事路线</h2>
          <p>查看起点、终点与途经点，提前熟悉比赛动线。</p>
        </div>
      </div>
      <div id="map-container" class="map-container">
        <p v-if="!isRouteExist" class="no-route-message">赛事方暂未发布路线</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getRaceDetail, registerRace, isRegister, getRoute } from "@/api/user";
import { existRoute } from "@/api/org";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  formatDateHour,
  raceStatusTag,
  raceStatusText,
  raceTypeText,
  registrationModeText,
} from "@/utils/display";

const convertRaceType = (type) => raceTypeText(type);
const convertRaceStatus = (status) => raceStatusText(status);
const convertRegistrationMode = (mode) => registrationModeText(mode);
const formatDate = (date) => formatDateHour(date);

const route = useRoute();
const router = useRouter();
const raceDetail = ref({});
const isRegistered = ref(false);
const buttonText = ref("");
const isRouteExist = ref(false);

const detailItems = computed(() => [
  { label: "赛事编号", value: raceDetail.value.raceId },
  { label: "赛事类型", value: convertRaceType(raceDetail.value.raceType) },
  { label: "地点", value: raceDetail.value.location },
  { label: "赛事时间", value: formatDate(raceDetail.value.raceDate) },
  { label: "报名模式", value: convertRegistrationMode(raceDetail.value.registrationMode) },
  { label: "参赛人数", value: raceDetail.value.participantNumber },
  { label: "报名开始", value: formatDate(raceDetail.value.enrollStart) },
  { label: "报名结束", value: formatDate(raceDetail.value.enrollEnd) },
  { label: "费用", value: raceDetail.value.fee ? `¥ ${raceDetail.value.fee}` : "免费" },
]);

const fetchRaceDetail = async () => {
  try {
    const res = await getRaceDetail({ id: route.params.id });
    if (res.data && res.data.data) {
      raceDetail.value = res.data.data;
      raceDetail.value.typeText = convertRaceType(raceDetail.value.raceType);
      raceDetail.value.statusText = convertRaceStatus(raceDetail.value.raceStatus);
      const userId = localStorage.getItem("userId");
      const registrationParam = {
        raceId: raceDetail.value.id,
        userId: parseInt(userId),
      };
      const registerRes = await isRegister(registrationParam);
      if (registerRes.data && registerRes.data.data) {
        isRegistered.value = registerRes.data.data;
      }
      buttonText.value = isRegistered.value
        ? "已报名"
        : convertRaceStatus(raceDetail.value.raceStatus);
    }
  } catch (error) {
    console.error("获取赛事详情失败:", error);
  }
};

const handleEnroll = async () => {
  try {
    const message = `是否确定要报名该赛事？<br>赛事日期为：${formatDate(
      raceDetail.value.raceDate,
    )}`;
    const result = await ElMessageBox.confirm(message, "确认报名", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
      dangerouslyUseHTMLString: true,
    });
    if (result === "confirm") {
      const userId = localStorage.getItem("userId");
      const registrationParam = {
        raceId: raceDetail.value.id,
        userId: parseInt(userId),
      };
      const response = await registerRace(registrationParam);
      if (response.data && response.data.code === 200) {
        ElMessage.success(response.data.data);
        isRegistered.value = true;
        buttonText.value = "已报名";
      } else {
        await ElMessageBox.alert(
          response.data.message || "报名失败，请稍后重试",
          "报名失败",
          {
            confirmButtonText: "确定",
          },
        );
        if (response.data.message === "请先进行健康评估") {
          router.push("/user/health");
        }
      }
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("报名失败:", error);
      ElMessage.error("报名失败，请稍后重试");
    }
  }
};

const initMap = async () => {
  try {
    const existRes = await existRoute({ raceId: raceDetail.value.id });
    if (existRes.data && existRes.data.code === 200) {
      isRouteExist.value = true;
      const routeRes = await getRoute({ raceId: raceDetail.value.id });
      if (routeRes.data && routeRes.data.code === 200) {
        const routeData = JSON.parse(routeRes.data.data.route);
        const map = new AMap.Map("map-container", {
          center: [routeData[0].lng, routeData[0].lat],
          zoom: 13,
        });

        const polyline = new AMap.Polyline({
          path: routeData.map((coord) => [coord.lng, coord.lat]),
          strokeColor: "#0f766e",
          strokeOpacity: 1,
          strokeWeight: 5,
        });

        polyline.setMap(map);

        const startMarker = new AMap.Text({
          position: [routeData[0].lng, routeData[0].lat],
          text: "起点",
          style: markerStyle("#0f766e"),
          offset: new AMap.Pixel(-15, -5),
        });
        map.add(startMarker);

        const endMarker = new AMap.Text({
          position: [
            routeData[routeData.length - 1].lng,
            routeData[routeData.length - 1].lat,
          ],
          text: "终点",
          style: markerStyle("#dc2626"),
          offset: new AMap.Pixel(-15, -5),
        });
        map.add(endMarker);

        for (let i = 1; i < routeData.length - 1; i++) {
          const marker = new AMap.Text({
            position: [routeData[i].lng, routeData[i].lat],
            text: `途经点 ${i}`,
            style: markerStyle("#d7a84f"),
            offset: new AMap.Pixel(-15, -5),
          });
          map.add(marker);
        }
      }
    }
  } catch (error) {
    console.error("初始化地图失败:", error);
  }
};

const markerStyle = (color) => ({
  padding: "4px 9px",
  "background-color": "white",
  border: `1px solid ${color}`,
  "border-radius": "999px",
  "font-size": "12px",
  color,
  "box-shadow": "0 8px 20px rgba(15, 23, 42, 0.16)",
  "text-align": "center",
  "white-space": "nowrap",
});

onMounted(async () => {
  await fetchRaceDetail();
  const script = document.createElement("script");
  script.src = `https://webapi.amap.com/maps?v=2.0&key=44fbaeb5331194be2b07ef1c121827aa&plugin=AMap.PolyEditor,AMap.CircleEditor`;
  script.onload = async () => {
    await initMap();
  };
  document.head.appendChild(script);
});
</script>

<style scoped>
.detail-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  min-height: 280px;
  padding: 42px;
  color: #fff;
  background:
    linear-gradient(120deg, rgba(9, 17, 32, 0.86), rgba(15, 118, 110, 0.68)),
    url("https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1600&q=82")
      center/cover;
}

.detail-hero .app-eyebrow,
.detail-hero p {
  color: rgba(255, 255, 255, 0.82);
}

.detail-hero h1 {
  max-width: 760px;
  margin: 0;
  font-size: clamp(34px, 5vw, 58px);
  line-height: 1.08;
}

.detail-hero p {
  max-width: 680px;
  margin: 18px 0 0;
  line-height: 1.8;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 22px;
  margin-top: 22px;
}

.detail-card,
.enroll-card,
.route-card {
  padding: 28px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.info-item {
  min-height: 94px;
  padding: 18px;
  border: 1px solid rgba(33, 53, 82, 0.08);
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.76);
}

.info-item span,
.info-item strong {
  display: block;
}

.info-item span {
  margin-bottom: 10px;
  color: var(--app-text-muted);
  font-size: 13px;
}

.info-item strong {
  color: var(--app-text);
  line-height: 1.5;
}

.enroll-card {
  align-self: start;
  background:
    linear-gradient(150deg, rgba(255, 255, 255, 0.95), rgba(232, 243, 242, 0.95));
}

.enroll-card h2 {
  margin: 0;
  font-size: 28px;
}

.enroll-card p {
  margin: 14px 0 24px;
  color: var(--app-text-muted);
  line-height: 1.8;
}

.enroll-card .el-button {
  width: 100%;
}

.route-card {
  margin-top: 22px;
}

.map-container {
  position: relative;
  width: 100%;
  height: 440px;
  overflow: hidden;
  border: 1px solid rgba(33, 53, 82, 0.08);
  border-radius: 16px;
  background: #edf2f7;
}

.no-route-message {
  position: absolute;
  inset: 0;
  display: grid;
  margin: 0;
  color: var(--app-text-muted);
  place-items: center;
}

@media (max-width: 980px) {
  .detail-hero {
    align-items: flex-start;
    flex-direction: column;
    padding: 32px 24px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .map-container {
    height: 360px;
  }
}
</style>
