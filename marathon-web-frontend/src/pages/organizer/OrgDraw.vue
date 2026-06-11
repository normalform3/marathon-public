<template>
  <div class="map-container">
    <div class="map-header">
      <h2>赛事路线点选</h2>
    </div>
    <div class="map-content">
      <div id="container" style="width: 70%; height: 500px"></div>
      <div class="info-panel">
        <h3>
          已点选的点
          <el-button
            @click="undoLastPoint"
            type="warning"
            style="margin-left: 20px"
            >撤回上一个点</el-button
          >
        </h3>
        <h3>路线长度: {{ routeLength.toFixed(2) }} 米</h3>

        <ul style="max-height: 310px; overflow-y: auto">
          <!-- 固定高度并添加滑动窗口 -->
          <li v-for="(point, index) in points" :key="index">
            经度: {{ point.lng }}, 纬度: {{ point.lat }}
          </li>
        </ul>
      </div>
    </div>
    <el-button @click="submitRoute" type="primary" style="margin-top: 10px"
      >提交路线</el-button
    >
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { submitRoute as submitRouteApi } from "@/api/org";
import { useRouter } from "vue-router";

// 存储点选的坐标点
const points = ref([]);
// 存储路线长度
const routeLength = ref(0);
// 存储标记点
const markers = ref([]);
let map;
let polyline;

// 获取当前位置
const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { longitude, latitude } = position.coords;
          resolve([longitude, latitude]);
        },
        (error) => {
          reject(error);
        }
      );
    } else {
      reject(new Error("浏览器不支持地理定位"));
    }
  });
};

// 初始化地图
const initMap = async () => {
  try {
    const currentLocation = await getCurrentLocation();
    map = new AMap.Map("container", {
      zoom: 13,
      center: currentLocation,
    });
  } catch (error) {
    console.error("获取当前位置失败:", error);
    map = new AMap.Map("container", {
      zoom: 13,
      center: [120.215, 30.21],
    });
  }

  // 监听地图点击事件
  map.on("click", (e) => {
    const point = {
      lng: e.lnglat.getLng(),
      lat: e.lnglat.getLat(),
    };
    points.value.push(point);

    // 创建标记点
    const marker = new AMap.Marker({
      position: [point.lng, point.lat],
      icon: "https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png", // 可以根据需要更换图标
      anchor: "bottom-center", // 设置锚点为底部中心
    });
    marker.setMap(map);
    markers.value.push(marker);

    // 更新折线
    if (polyline) {
      polyline.setPath(points.value.map((p) => [p.lng, p.lat]));
    } else {
      polyline = new AMap.Polyline({
        path: points.value.map((p) => [p.lng, p.lat]),
        strokeColor: "#FF33FF",
        strokeWeight: 2,
        strokeOpacity: 1,
        lineJoin: "round",
        lineCap: "round",
        zIndex: 50,
      });
      polyline.setMap(map);
    }

    // 计算路线长度
    if (points.value.length > 1) {
      routeLength.value = AMap.GeometryUtil.distanceOfLine(
        points.value.map((p) => [p.lng, p.lat])
      );
    }
  });
};

// 撤回上一个点
const undoLastPoint = () => {
  if (points.value.length > 0) {
    points.value.pop();
    if (markers.value.length > 0) {
      const lastMarker = markers.value.pop();
      lastMarker.setMap(null);
    }
    if (points.value.length === 0) {
      if (polyline) {
        polyline.setMap(null);
        polyline = null;
      }
      routeLength.value = 0;
    } else {
      polyline.setPath(points.value.map((p) => [p.lng, p.lat]));
      routeLength.value = AMap.GeometryUtil.distanceOfLine(
        points.value.map((p) => [p.lng, p.lat])
      );
    }
  } else {
    ElMessage.warning("没有点可以撤回");
  }
};

const router = useRouter();
// 提交路线
const submitRoute = async () => {
  if (points.value.length === 0) {
    ElMessage.warning("请至少选择一个点");
    return;
  }
  try {
    const routeData = {
      raceId: localStorage.getItem("raceId"),
      route: JSON.stringify(points.value),
    };
    const response = await submitRouteApi(routeData);
    if (response) {
      ElMessage.success("路线提交成功");
      // 清空点选和折线
      points.value = [];
      routeLength.value = 0;
      router.push("/organizer/detail");
    } else {
      ElMessage.error("路线提交失败");
    }
  } catch (error) {
    console.error("路线提交出错:", error);
    ElMessage.error("路线提交出错，请稍后重试");
  }
};

onMounted(() => {
  // 请替换为你的高德地图 API Key
  const script = document.createElement("script");
  script.src =
    "https://webapi.amap.com/maps?v=2.0&key=44fbaeb5331194be2b07ef1c121827aa";
  script.onload = initMap;
  document.head.appendChild(script);
});
</script>

<style scoped>
.map-container {
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

.map-content {
  display: flex;
  gap: 20px;
}

.info-panel {
  width: 30%;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.info-panel h3 {
  margin-top: 0;
}
</style>
