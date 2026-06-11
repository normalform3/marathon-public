<template>
  <div class="user-center-page">
    <section class="user-center-card">
      <div class="user-center-header">
        <div>
          <span class="app-eyebrow">PROFILE</span>
          <h1>{{ userDetail.name || "个人信息" }}</h1>
          <p>管理账号、身份信息和紧急联系人资料。</p>
        </div>
        <el-button type="primary" @click="showEditDialog = true">修改个人信息</el-button>
      </div>

      <div class="profile-grid">
        <div class="profile-avatar">
          <span>{{ (userDetail.name || userDetail.account || "用").slice(0, 1) }}</span>
          <strong>{{ userDetail.name || "未命名用户" }}</strong>
          <small>{{ convertUserType(userDetail.type) }}</small>
        </div>

        <div class="info-list">
          <div class="info-row">
            <span>账号</span>
            <strong>{{ userDetail.account || "-" }}</strong>
          </div>
          <div class="info-row">
            <span>身份证号</span>
            <strong>{{ maskIdNumber(userDetail.identificationNumber) || "-" }}</strong>
          </div>
          <div class="info-row">
            <span>绑定手机</span>
            <strong>{{ maskPhoneNumber(userDetail.phone) || "-" }}</strong>
          </div>
          <div class="info-row">
            <span>紧急联系人电话</span>
            <strong>{{ maskPhoneNumber(userDetail.emergencyPhone) || "-" }}</strong>
          </div>
        </div>
      </div>
    </section>

    <el-dialog v-model="showEditDialog" title="修改个人信息" width="min(620px, 92vw)">
      <el-form :model="editUserDetail" :rules="editRules" ref="editFormRef" label-width="130px">
        <el-form-item label="账号" prop="account">
          <el-input v-model="editUserDetail.account" disabled></el-input>
        </el-form-item>
        <el-form-item label="身份证号" prop="identificationNumber">
          <el-input
            v-model="editUserDetail.identificationNumber"
            placeholder="请输入您的完整身份证号"
          ></el-input>
        </el-form-item>
        <el-form-item label="绑定手机" prop="phone">
          <el-input v-model="editUserDetail.phone"></el-input>
        </el-form-item>
        <el-form-item label="紧急联系人电话" prop="emergencyPhone">
          <el-input v-model="editUserDetail.emergencyPhone"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitEditForm">保存修改</el-button>
          <el-button @click="showEditDialog = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, watch } from "vue";
import { getUserDetail, updateUserInfo } from "@/api/user";
import { ElMessage } from "element-plus";

const convertUserType = (type) => {
  switch (type) {
    case 1:
      return "普通用户";
    case 2:
      return "赛事方";
    case 3:
      return "管理员";
    default:
      return "用户";
  }
};

const maskIdNumber = (idNumber) => {
  if (!idNumber) return "";
  return idNumber.slice(0, 6) + "********" + idNumber.slice(-4);
};

const maskPhoneNumber = (phone) => {
  if (!phone) return "";
  return phone.slice(0, 3) + "****" + phone.slice(-4);
};

const userDetail = ref({});
const showEditDialog = ref(false);

const editUserDetail = reactive({
  account: "",
  identificationNumber: "",
  phone: "",
  emergencyPhone: "",
});

const editRules = {
  identificationNumber: [
    { required: true, message: "请输入身份证号", trigger: "blur" },
  ],
  phone: [
    { required: true, message: "请输入绑定手机", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号",
      trigger: "blur",
    },
  ],
  emergencyPhone: [
    { required: true, message: "请输入紧急联系人电话", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号",
      trigger: "blur",
    },
  ],
};

const fetchUserDetail = async () => {
  try {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      ElMessage.error("请先登录");
      return;
    }
    const res = await getUserDetail({ id: userId });
    if (res.data && res.data.data) {
      userDetail.value = res.data.data;
      localStorage.setItem("userName", userDetail.value.name || userDetail.value.account || "用户");
      editUserDetail.account = userDetail.value.account;
      editUserDetail.identificationNumber = userDetail.value.identificationNumber;
      editUserDetail.phone = userDetail.value.phone;
      editUserDetail.emergencyPhone = userDetail.value.emergencyPhone;
    }
  } catch (error) {
    console.error("获取用户详情失败:", error);
    ElMessage.error("获取用户详情失败，请稍后重试");
  }
};

const submitEditForm = async () => {
  try {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      ElMessage.error("请先登录");
      return;
    }
    const data = {
      userId,
      identificationNumber: editUserDetail.identificationNumber,
      phone: editUserDetail.phone,
      emergencyPhone: editUserDetail.emergencyPhone,
    };
    const res = await updateUserInfo(data);
    if (res.data && res.data.code === 200) {
      ElMessage.success(res.data.message);
      showEditDialog.value = false;
      await fetchUserDetail();
    } else {
      ElMessage.error(res.data.message || "修改失败");
    }
  } catch (error) {
    console.error("修改个人信息失败:", error);
    ElMessage.error("修改个人信息失败，请稍后重试");
  }
};

watch(
  () => userDetail.value,
  (newVal) => {
    if (newVal) {
      editUserDetail.account = newVal.account;
      editUserDetail.identificationNumber = newVal.identificationNumber;
      editUserDetail.phone = newVal.phone;
      editUserDetail.emergencyPhone = newVal.emergencyPhone;
    }
  },
  { deep: true },
);

watch(showEditDialog, (newVal) => {
  if (newVal) {
    editUserDetail.identificationNumber = "";
  }
});

onMounted(fetchUserDetail);
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 22px;
}

.profile-avatar {
  display: grid;
  min-height: 260px;
  place-items: center;
  align-content: center;
  padding: 24px;
  text-align: center;
  border-radius: 18px;
  background:
    linear-gradient(145deg, rgba(15, 118, 110, 0.12), rgba(215, 168, 79, 0.12)),
    #fff;
}

.profile-avatar span {
  display: grid;
  width: 82px;
  height: 82px;
  margin-bottom: 16px;
  color: #fff;
  font-size: 34px;
  font-weight: 800;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--app-primary), #14213d);
  box-shadow: 0 18px 34px rgba(15, 118, 110, 0.24);
}

.profile-avatar strong,
.profile-avatar small {
  display: block;
}

.profile-avatar strong {
  color: var(--app-text);
  font-size: 22px;
}

.profile-avatar small {
  margin-top: 8px;
  color: var(--app-text-muted);
}

.info-list {
  display: grid;
  gap: 14px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 20px;
  border: 1px solid rgba(33, 53, 82, 0.08);
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.75);
}

.info-row span {
  color: var(--app-text-muted);
}

.info-row strong {
  color: var(--app-text);
}

@media (max-width: 760px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .info-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
