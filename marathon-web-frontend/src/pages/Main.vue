<template>
  <div class="site-shell">
    <header class="site-header">
      <div class="site-header__inner">
        <button class="brand" type="button" @click="router.push('/')">
          <span class="brand__mark">M</span>
          <span>
            <strong>Marathon Elite</strong>
            <small>马拉松赛事平台</small>
          </span>
        </button>

        <nav class="site-nav" aria-label="主导航">
          <button
            v-for="item in primaryNav"
            :key="item.path"
            class="site-nav__item"
            :class="{ active: isNavActive(item.path) }"
            type="button"
            @click="router.push(item.path)"
          >
            {{ item.label }}
          </button>

          <el-dropdown trigger="hover" @command="handleCommand">
            <button class="site-nav__item" type="button">赛事服务</button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="/orglogin">赛事方入口</el-dropdown-item>
                <el-dropdown-item command="/admlogin">管理员入口</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>

        <div class="site-actions">
          <el-button v-if="!isLoggedIn" type="primary" @click="router.push('/login')">
            登录
          </el-button>
          <template v-else>
            <el-dropdown trigger="hover" @command="handleUserCommand">
              <button
                class="user-pill"
                :class="{ active: route.path.startsWith('/user') }"
                type="button"
              >
                <span>{{ (userName || "用户").slice(0, 1) }}</span>
                <strong>{{ userName || "已登录" }}</strong>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/user/detail">个人信息</el-dropdown-item>
                  <el-dropdown-item command="/user/health">健康审核</el-dropdown-item>
                  <el-dropdown-item command="/user/enroll">我的报名</el-dropdown-item>
                  <el-dropdown-item command="/user/grade">我的成绩</el-dropdown-item>
                  <el-dropdown-item command="/user/notice">通知中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </header>

    <main class="site-main">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from "vue";
import { useRoute } from "vue-router";
import router from "@/router";
import { ElNotification } from "element-plus";
import { getUserDetail } from "@/api/user";

const route = useRoute();

const primaryNav = [
  { label: "首页", path: "/home" },
  { label: "赛事", path: "/racelist" },
  { label: "新闻公告", path: "/news" },
];

// 判断是否显示注销按钮
const isLoggedIn = ref(!!localStorage.getItem("userId"));
const userName = ref(localStorage.getItem("userName") || "");

// 用于存储 WebSocket 实例
let socket: WebSocket | null = null;

//注销
const handleLogout = () => {
  localStorage.clear();
  isLoggedIn.value = false;
  userName.value = "";
  router.push("/");
  // 断开 WebSocket 连接
  if (socket) {
    socket.close();
    socket = null;
  }
};

// 添加一个方法来更新登录状态
const updateLoginStatus = () => {
  isLoggedIn.value = !!localStorage.getItem("userId");
  userName.value = localStorage.getItem("userName") || "";
  if (isLoggedIn.value && !userName.value) {
    fetchUserName();
  }
};

// 监听自定义事件
window.addEventListener("storage-changed", updateLoginStatus);

const isNavActive = (path: string) => {
  if (path === "/home") return route.path === "/" || route.path === "/home";
  if (path === "/racelist") return route.path === "/racelist" || route.path.startsWith("/race/");
  return route.path === path;
};

const handleCommand = (command: string) => {
  router.push(command);
};

const handleUserCommand = (command: string) => {
  if (command === "logout") {
    handleLogout();
    return;
  }
  router.push(command);
};

const fetchUserName = async () => {
  const userId = localStorage.getItem("userId");
  if (!userId) return;
  try {
    const res = await getUserDetail({ id: userId });
    const name = res.data?.data?.name || res.data?.data?.account || "";
    if (name) {
      localStorage.setItem("userName", name);
      userName.value = name;
    }
  } catch (error) {
    console.error("获取用户信息失败:", error);
  }
};

onMounted(() => {
  if (isLoggedIn.value && !userName.value) {
    fetchUserName();
  }
});

// 监听 isLoggedIn 变化，根据登录状态处理 WebSocket 连接
watch(
  isLoggedIn,
  (newValue) => {
    if (newValue) {
      const userId = localStorage.getItem("userId");
      if (userId) {
        // 连接到服务器端注册的 /ws 端点，并传递 userId 参数
        socket = new WebSocket(`ws://localhost:8700/ws/${userId}`);

        socket.onopen = () => {
          console.log("Connected to WebSocket");
        };

        // 监听消息事件
        socket.onmessage = (event) => {
          const notificationData = event.data;
          ElNotification({
            title: "通知",
            message: notificationData,
            type: "warning",
            duration: 10000,
            showClose: true,
          });
          console.log("收到消息: ", event.data);
        };

        socket.onerror = (error) => {
          console.error("Error connecting to WebSocket: ", error);
        };

        socket.onclose = () => {
          console.log("Disconnected from WebSocket");
        };
      }
    } else {
      if (socket) {
        socket.close();
        socket = null;
      }
    }
  },
  { immediate: true }
);

onUnmounted(() => {
  // 组件卸载时断开 WebSocket 连接
  if (socket) {
    socket.close();
    socket = null;
  }
  window.removeEventListener("storage-changed", updateLoginStatus);
});
</script>

<style scoped>
.site-shell {
  min-height: 100vh;
}

.site-header {
  position: sticky;
  z-index: 20;
  top: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.55);
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 10px 34px rgba(20, 37, 63, 0.08);
  backdrop-filter: blur(18px);
}

.site-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: min(1280px, calc(100% - 40px));
  height: 74px;
  margin: 0 auto;
  gap: 24px;
}

.brand {
  display: inline-flex;
  align-items: center;
  min-width: 220px;
  padding: 0;
  color: var(--app-text);
  text-align: left;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.brand__mark {
  display: grid;
  width: 42px;
  height: 42px;
  margin-right: 12px;
  color: #fff;
  font-weight: 800;
  place-items: center;
  border-radius: 13px;
  background: linear-gradient(135deg, var(--app-primary), #14213d);
  box-shadow: 0 12px 26px rgba(15, 118, 110, 0.28);
}

.brand strong,
.brand small {
  display: block;
}

.brand strong {
  font-size: 16px;
  letter-spacing: 0.02em;
}

.brand small {
  margin-top: 3px;
  color: var(--app-text-muted);
  font-size: 12px;
}

.site-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 8px;
}

.site-nav__item {
  position: relative;
  height: 40px;
  padding: 0 14px;
  color: #475569;
  font-size: 14px;
  font-weight: 700;
  line-height: 40px;
  background: transparent;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    transform 0.2s ease;
}

.site-nav__item:hover,
.site-nav__item.active {
  color: var(--app-primary-dark);
  background: rgba(15, 118, 110, 0.1);
  transform: translateY(-1px);
}

.site-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 160px;
  gap: 10px;
}

.user-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 150px;
  height: 38px;
  padding: 0 12px 0 7px;
  color: var(--app-primary-dark);
  font-size: 14px;
  font-weight: 700;
  background: rgba(15, 118, 110, 0.1);
  border: 1px solid rgba(15, 118, 110, 0.16);
  border-radius: 999px;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    background-color 0.2s ease;
}

.user-pill:hover {
  transform: translateY(-1px);
  background: rgba(15, 118, 110, 0.15);
}

.user-pill.active {
  background: rgba(15, 118, 110, 0.18);
  border-color: rgba(15, 118, 110, 0.28);
}

.user-pill span {
  display: grid;
  width: 26px;
  height: 26px;
  color: #fff;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--app-primary), #14213d);
}

.user-pill strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.site-main {
  min-height: calc(100vh - 74px);
}

@media (max-width: 900px) {
  .site-header__inner {
    align-items: flex-start;
    height: auto;
    padding: 14px 0;
    flex-direction: column;
  }

  .site-nav {
    justify-content: flex-start;
    width: 100%;
    overflow-x: auto;
    padding-bottom: 4px;
  }

  .site-actions {
    position: absolute;
    top: 18px;
    right: 0;
  }
}

@media (max-width: 560px) {
  .site-header__inner {
    width: calc(100% - 24px);
  }

  .brand {
    min-width: 0;
  }

  .brand small {
    display: none;
  }
}
</style>
