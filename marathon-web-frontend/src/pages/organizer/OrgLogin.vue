<template>
  <div class="app-auth-page organizer-auth">
    <section class="app-card app-auth-card">
      <span class="app-eyebrow">ORGANIZER ACCESS</span>
      <h1>赛事方入口</h1>
      <p class="auth-subtitle">管理赛事信息、路线、运动员名单与赛后反馈。</p>

      <el-form label-position="top">
        <el-form-item label="账号">
          <el-input v-model="account" placeholder="请输入赛事方账号" clearable />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <div class="app-actions">
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <el-button plain @click="router.push('/orgsignup')">申请举办赛事</el-button>
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
    if (
      response.data.code === 200 &&
      response.data.data.type === 2 &&
      response.data.data.raceId
    ) {
      localStorage.clear();
      localStorage.setItem("token", response.data.data.token);
      localStorage.setItem("userId", response.data.data.id);
      localStorage.setItem("type", response.data.data.type);
      localStorage.setItem("raceId", response.data.data.raceId);
      localStorage.setItem(
        "userName",
        response.data.data.name || response.data.data.account || "赛事方",
      );
      window.dispatchEvent(new Event("storage-changed"));
      router.push("/organizer");
      ElMessage.success("登录成功");
    } else {
      let message = response.data.message || "账号或密码错误";
      if (response.data.code === 200 && response.data.data.type !== 2) {
        message = "该账号不是赛事方账号";
      } else if (response.data.code === 200 && !response.data.data.raceId) {
        message = "该赛事方账号暂未绑定赛事，请等待审核通过后再登录";
      }
      ElMessage.error(message);
    }
  } catch (error) {
    console.error("登录失败:", error);
    ElMessage.error("登录失败，请稍后重试");
  }
};
</script>

<style scoped>
.organizer-auth {
  background:
    linear-gradient(120deg, rgba(244, 247, 251, 0.84), rgba(244, 247, 251, 0.94)),
    url("https://images.unsplash.com/photo-1518600506278-4e8ef466b810?auto=format&fit=crop&w=1600&q=82")
      center/cover;
}
</style>
