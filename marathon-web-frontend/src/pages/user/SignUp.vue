<template>
  <div class="app-page signup-page">
    <el-form
      ref="registerFormRef"
      :model="user"
      :rules="rules"
      label-width="120px"
      class="app-card app-form-card register-form"
    >
      <div class="form-title">
        <span class="app-eyebrow">JOIN US</span>
        <h1>用户注册</h1>
        <p>完善基础信息后即可报名赛事、查看成绩与接收平台通知。</p>
      </div>

      <el-form-item label="账号" prop="account">
        <el-input v-model="user.account" placeholder="请输入账号" clearable />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
          v-model="user.password"
          type="password"
          placeholder="请输入密码"
          clearable
        />
      </el-form-item>

      <el-form-item label="姓名" prop="name">
        <el-input v-model="user.name" placeholder="请输入姓名" clearable />
      </el-form-item>

      <el-form-item label="身份证号" prop="identificationNumber">
        <el-input
          v-model="user.identificationNumber"
          placeholder="请输入身份证号"
          clearable
          @input="handleIdNumberInput"
        />
      </el-form-item>

      <el-form-item label="生日" prop="birthday">
        <el-input
          v-model="user.birthday"
          readonly
          placeholder="根据身份证号自动填充"
        />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="user.phone" placeholder="请输入手机号" clearable />
      </el-form-item>

      <el-form-item label="紧急联系人电话" prop="emergencyPhone">
        <el-input
          v-model="user.emergencyPhone"
          placeholder="请输入紧急联系人电话"
          clearable
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="onSubmit">注册</el-button>
        <el-button @click="resetForm">重置</el-button>
        <el-button text @click="router.push('/login')">返回登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { signUp } from "@/api/user"; // 引入注册接口函数
import { ElMessage } from "element-plus"; // 引入消息提示组件

// 定义用户信息
const user = ref({
  account: "",
  password: "",
  name: "",
  birthday: null,
  identificationNumber: "",
  phone: "",
  emergencyPhone: "",
  type: 1,
  healthStatus: 0,
});

// 定义表单引用
const registerFormRef = ref(null);

// 定义手机号校验规则
const validatePhone = (rule, value, callback) => {
  const phoneReg = /^1[3-9]\d{9}$/;
  if (!value) {
    return callback(new Error("手机号不能为空"));
  }
  if (!phoneReg.test(value)) {
    return callback(new Error("请输入有效的手机号"));
  }
  callback();
};

// 定义身份证号校验规则
const validateIdentificationNumber = (rule, value, callback) => {
  const idReg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
  if (!value) {
    return callback(new Error("身份证号不能为空"));
  }
  if (!idReg.test(value)) {
    return callback(new Error("请输入有效的身份证号"));
  }
  callback();
};

// 处理身份证号输入的方法
const handleIdNumberInput = () => {
  const idNumber = user.value.identificationNumber;
  if (idNumber.length === 18) {
    const year = idNumber.slice(6, 10);
    const month = idNumber.slice(10, 12);
    const day = idNumber.slice(12, 14);
    user.value.birthday = `${year}-${month}-${day}`;
  } else if (idNumber.length === 15) {
    const year = `19${idNumber.slice(6, 8)}`;
    const month = idNumber.slice(8, 10);
    const day = idNumber.slice(10, 12);
    user.value.birthday = `${year}-${month}-${day}`;
  } else {
    user.value.birthday = null;
  }
};

// 将日期转换为 datetime 格式
const convertToDatetime = (dateStr) => {
  if (!dateStr) return null;
  return `${dateStr}T00:00:00`;
};

// 定义表单校验规则
const rules = {
  account: [{ required: true, message: "账号不能为空", trigger: "blur" }],
  password: [{ required: true, message: "密码不能为空", trigger: "blur" }],
  name: [{ required: true, message: "姓名不能为空", trigger: "blur" }],
  birthday: [{ required: true, message: "生日不能为空", trigger: "change" }],
  identificationNumber: [
    { validator: validateIdentificationNumber, trigger: "blur" },
  ],
  phone: [{ validator: validatePhone, trigger: "blur" }],
  emergencyPhone: [{ validator: validatePhone, trigger: "blur" }],
};

// 提交表单方法
const router = useRouter();
const onSubmit = async () => {
  registerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 转换生日为 datetime 格式
        const userData = {
          ...user.value,
          birthday: convertToDatetime(user.value.birthday),
        };
        const response = await signUp(userData);
        // 假设接口返回成功状态码 200
        if (response.status === 200) {
          ElMessage.success("注册成功");
          router.push("/");
        } else {
          ElMessage.error("注册失败，请稍后重试");
        }
      } catch (error) {
        console.error("注册出错:", error);
        ElMessage.error("注册出错，请稍后重试");
      }
    } else {
      console.log("表单验证失败");
      return false;
    }
  });
};

// 重置表单方法
const resetForm = () => {
  registerFormRef.value.resetFields();
};
</script>

<style scoped>
.signup-page {
  display: grid;
  place-items: start center;
}

.form-title {
  margin-bottom: 28px;
  text-align: left;
}

.form-title h1 {
  margin: 0;
  color: var(--app-text);
}

.form-title p {
  margin: 10px 0 0;
  color: var(--app-text-muted);
  line-height: 1.7;
}

.register-form {
  width: min(720px, 100%);
  padding: 34px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-input {
  width: 100%;
}

.el-button {
  margin-right: 10px;
}

@media (max-width: 640px) {
  .register-form {
    padding: 22px 16px;
  }

  .register-form :deep(.el-form-item) {
    display: block;
  }
}
</style>
