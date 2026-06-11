<template>
  <div class="app-auth-page auth-visual">
    <section class="app-card app-auth-card">
      <span class="app-eyebrow">RUNNER ACCESS</span>
      <h1>欢迎登录马拉松平台</h1>
      <p class="auth-subtitle">查看报名、健康审核、成绩与通知，继续你的赛事旅程。</p>

      <el-form label-position="top">
        <el-form-item label="账号">
          <el-input v-model="account" placeholder="请输入账号" clearable />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <div class="app-actions">
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <el-button plain @click="router.push('/signup')">注册</el-button>
          <el-button text @click="router.push('/')">返回主页</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { login } from "@/api/user";

const router = useRouter();
const account = ref("");
const password = ref("");

const handleLogin = async () => {
  const data = {
    account: account.value,
    password: password.value,
  };
  try {
    const response = await login(data);
    if (response.data.code === 200 && response.data.data.type === 1) {
      localStorage.setItem("token", response.data.data.token);
      localStorage.setItem("userId", response.data.data.id);
      localStorage.setItem("type", response.data.data.type);
      localStorage.setItem(
        "userName",
        response.data.data.name || response.data.data.account || "用户",
      );
      window.dispatchEvent(new Event("storage-changed"));
      router.push("/");
      ElMessage.success("登录成功");
    } else {
      const message =
        response.data.code === 200
          ? "该账号不是普通用户账号，请使用对应入口登录"
          : response.data.message || "账号或密码错误";
      ElMessage.error(message);
    }
  } catch (error) {
    console.error("登录失败:", error);
    ElMessage.error("登录失败，请稍后重试");
  }
};
</script>

<style scoped>
.auth-visual {
  background:
    linear-gradient(120deg, rgba(244, 247, 251, 0.86), rgba(244, 247, 251, 0.94)),
    url("https://images.unsplash.com/photo-1553969546-6f7388a7e07c?auto=format&fit=crop&w=1600&q=82")
      center/cover;
}
</style>
