<template>
  <div class="map-container-page">
    <div class="map-header">
      <h2>地图路线信息</h2>
    </div>
    <div id="map-container" style="width: 100%; height: 400px">
      <p v-if="!isRouteExist" class="no-route-message">暂未发布路线</p>
    </div>
    <div class="route-buttons">
      <el-button v-if="hasRoute" @click="modifyRoute">修改路线</el-button>
      <el-button v-else @click="modifyRoute">上传路线</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { existRoute } from "@/api/org";
import { getRoute } from "@/api/user";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";

const isRouteExist = ref(false);
const hasRoute = ref(false);
const router = useRouter();

const initMap = async () => {
  try {
    const raceId = localStorage.getItem("raceId");
    if (!raceId) {
      throw new Error("未获取到 raceId");
    }
    // 检查是否存在路线
    const existRes = await existRoute({ raceId: parseInt(raceId) });
    if (existRes.data && existRes.data.code === 200) {
      isRouteExist.value = true;
      hasRoute.value = true;
      const routeRes = await getRoute({ raceId: parseInt(raceId) });
      if (routeRes.data && routeRes.data.code === 200) {
        const routeData = JSON.parse(routeRes.data.data.route);
        const map = new AMap.Map("map-container", {
          center: [routeData[0].lng, routeData[0].lat],
          zoom: 13,
        });

        const polyline = new AMap.Polyline({
          path: routeData.map((coord) => [coord.lng, coord.lat]),
          strokeColor: "#007AFF",
          strokeOpacity: 1,
          strokeWeight: 4,
        });

        polyline.setMap(map);

        // 添加起点标注 - 更紧凑的样式
        const startMarker = new AMap.Text({
          position: [routeData[0].lng, routeData[0].lat],
          text: "起点",
          style: {
            padding: "2px 6px",
            "background-color": "white",
            border: "1px solid #007AFF",
            "border-radius": "3px",
            "font-size": "12px",
            color: "#007AFF",
            "text-align": "center",
            "white-space": "nowrap",
          },
          offset: new AMap.Pixel(-15, -5),
        });
        map.add(startMarker);

        // 添加终点标注 - 更紧凑的样式
        const endMarker = new AMap.Text({
          position: [
            routeData[routeData.length - 1].lng,
            routeData[routeData.length - 1].lat,
          ],
          text: "终点",
          style: {
            padding: "2px 6px",
            "background-color": "white",
            border: "1px solid #FF0000",
            "border-radius": "3px",
            "font-size": "12px",
            color: "#FF0000",
            "text-align": "center",
            "white-space": "nowrap",
          },
          offset: new AMap.Pixel(-15, -5),
        });
        map.add(endMarker);

        // 添加途径点标注
        for (let i = 1; i < routeData.length - 1; i++) {
          const marker = new AMap.Text({
            position: [routeData[i].lng, routeData[i].lat],
            text: `途径点 ${i}`,
            style: {
              padding: "2px 6px",
              "background-color": "white",
              border: "1px solid #FFA500",
              "border-radius": "3px",
              "font-size": "12px",
              color: "#FFA500",
              "text-align": "center",
              "white-space": "nowrap",
            },
            offset: new AMap.Pixel(-15, -5),
          });
          map.add(marker);
        }
      }
    } else {
      hasRoute.value = false;
    }
  } catch (error) {
    console.error("初始化地图失败:", error);
    ElMessage.error("初始化地图失败，请稍后重试");
  }
};

const modifyRoute = () => {
  router.push("/organizer/draw");
};

onMounted(async () => {
  // 加载高德地图 API
  const script = document.createElement("script");
  script.src = `https://webapi.amap.com/maps?v=2.0&key=44fbaeb5331194be2b07ef1c121827aa&plugin=AMap.PolyEditor,AMap.CircleEditor`;
  script.onload = async () => {
    await initMap();
  };
  document.head.appendChild(script);
});
</script>

<style scoped>
.map-container-page {
  width: 90%;
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.map-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.no-route-message {
  text-align: center;
  margin-top: 20px;
  font-size: 16px;
  color: #999;
}

.route-buttons {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
