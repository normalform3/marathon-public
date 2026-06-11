<template>
  <div class="dashboard-shell">
    <aside class="dashboard-sidebar">
      <div class="dashboard-brand">
        <span>ME</span>
        <div>
          <strong>赛事方后台</strong>
          <small>Organizer Console</small>
        </div>
      </div>

      <el-menu :default-active="route.path" router class="dashboard-menu">
        <el-menu-item index="/organizer/detail">
          <el-icon><IconMenu /></el-icon>
          <span>赛事详情</span>
        </el-menu-item>
        <el-menu-item index="/organizer/route">
          <el-icon><Location /></el-icon>
          <span>赛事路线</span>
        </el-menu-item>
        <el-menu-item index="/organizer/athlete">
          <el-icon><Files /></el-icon>
          <span>运动员详情</span>
        </el-menu-item>
        <el-menu-item index="/organizer/grade">
          <el-icon><Document /></el-icon>
          <span>成绩录入</span>
        </el-menu-item>
        <el-menu-item index="/organizer/comment">
          <el-icon><Comment /></el-icon>
          <span>赛后反馈</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <section class="dashboard-main">
      <header class="dashboard-header">
        <div>
          <span class="app-eyebrow">ORGANIZER</span>
          <h1>举办方管理后台</h1>
        </div>
        <el-button plain @click="logout">退出登录</el-button>
      </header>

      <main class="dashboard-content">
        <router-view></router-view>
      </main>
    </section>
  </div>
</template>

<script lang="ts" setup>
import {
  Document,
  Menu as IconMenu,
  Files,
  Comment,
  Location,
} from "@element-plus/icons-vue";
import { useRoute, useRouter } from "vue-router";

const router = useRouter();
const route = useRoute();
const logout = () => {
  localStorage.clear();
  router.push("/");
};
</script>

<style scoped>
.dashboard-shell {
  display: grid;
  min-height: 100vh;
  grid-template-columns: 238px minmax(0, 1fr);
  background: var(--app-bg);
}

.dashboard-sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 22px 14px;
  color: #dbeafe;
  background: linear-gradient(180deg, #0f172a, #0b4f4a);
  box-shadow: 16px 0 44px rgba(15, 23, 42, 0.18);
}

.dashboard-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 8px 26px;
}

.dashboard-brand span {
  display: grid;
  width: 42px;
  height: 42px;
  color: #0f172a;
  font-weight: 900;
  place-items: center;
  border-radius: 13px;
  background: linear-gradient(135deg, #ffffff, #d7a84f);
}

.dashboard-brand strong,
.dashboard-brand small {
  display: block;
}

.dashboard-brand small {
  margin-top: 3px;
  color: rgba(255, 255, 255, 0.64);
}

.dashboard-menu {
  border-right: 0;
  background: transparent;
}

.dashboard-menu :deep(.el-menu-item) {
  height: 46px;
  margin: 6px 0;
  color: rgba(255, 255, 255, 0.78);
  border-radius: 12px;
}

.dashboard-menu :deep(.el-menu-item:hover),
.dashboard-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: rgba(255, 255, 255, 0.14);
}

.dashboard-main {
  min-width: 0;
}

.dashboard-header {
  position: sticky;
  z-index: 10;
  top: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 82px;
  padding: 18px 30px;
  border-bottom: 1px solid rgba(33, 53, 82, 0.08);
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(18px);
}

.dashboard-header h1 {
  margin: 0;
  font-size: 24px;
}

.dashboard-content {
  padding: 26px;
  animation: page-rise 0.48s ease both;
}

@media (max-width: 820px) {
  .dashboard-shell {
    grid-template-columns: 1fr;
  }

  .dashboard-sidebar {
    position: relative;
    height: auto;
  }
}
</style>
