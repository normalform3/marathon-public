<template>
  <div class="app-auth-page admin-auth">
    <section class="app-card app-auth-card">
      <span class="app-eyebrow">ADMIN ACCESS</span>
      <h1>管理员入口</h1>
      <p class="auth-subtitle">进入审核、新闻公告和规则管理后台。</p>

      <el-form label-position="top">
        <el-form-item label="账号">
          <el-input v-model="account" placeholder="请输入管理员账号" clearable />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <div class="app-actions">
          <el-button type="primary" @click="handleLogin">登录</el-button>
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
    if (response.data.code === 200 && response.data.data.type === 3) {
      localStorage.clear();
      localStorage.setItem("token", response.data.data.token);
      localStorage.setItem("userId", response.data.data.id);
      localStorage.setItem("type", response.data.data.type);
      localStorage.setItem(
        "userName",
        response.data.data.name || response.data.data.account || "管理员",
      );
      window.dispatchEvent(new Event("storage-changed"));
      router.push("/admin");
      ElMessage.success("登录成功");
    } else {
      const message =
        response.data.code === 200
          ? "该账号不是管理员账号"
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
.admin-auth {
  background:
    radial-gradient(circle at 20% 20%, rgba(15, 118, 110, 0.16), transparent 26rem),
    linear-gradient(135deg, #f8fafc, #eef3f8);
}
</style>
