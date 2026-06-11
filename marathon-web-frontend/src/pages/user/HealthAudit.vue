<template>
  <div class="form-container">
    <el-form :model="form" ref="formRef" label-width="180px">
      <!-- 个人信息 -->
      <el-form-item
        label="年龄"
        prop="age"
        :rules="{ required: true, message: '请输入年龄', trigger: 'blur' }"
      >
        <el-input
          v-model="form.age"
          type="number"
          placeholder="请输入年龄"
        ></el-input>
      </el-form-item>
      <el-form-item
        label="性别"
        prop="sex"
        :rules="{ required: true, message: '请选择性别', trigger: 'change' }"
      >
        <el-radio-group v-model="form.sex">
          <el-radio :label="1">男</el-radio>
          <el-radio :label="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="身高"
        prop="height"
        :rules="{ required: true, message: '请输入身高', trigger: 'blur' }"
      >
        <el-input
          v-model="form.height"
          type="number"
          placeholder="请输入身高"
          @input="calculateBMI"
        ></el-input>
      </el-form-item>
      <el-form-item
        label="体重"
        prop="weight"
        :rules="{ required: true, message: '请输入体重', trigger: 'blur' }"
      >
        <el-input
          v-model="form.weight"
          type="number"
          placeholder="请输入体重"
          @input="calculateBMI"
        ></el-input>
      </el-form-item>
      <el-form-item label="BMI">
        <el-input v-model="form.bmi" type="number" disabled></el-input>
      </el-form-item>

      <!-- 运动习惯 -->
      <el-form-item
        label="平时是否定期跑步训练"
        prop="regularRun"
        :rules="{
          required: true,
          message: '请选择是否定期跑步训练',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.regularRun">
          <el-radio :label="1">是</el-radio>
          <el-radio :label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="每周跑量"
        prop="weeklyRunDistance"
        :rules="{
          required: true,
          message: '请选择每周跑量',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.weeklyRunDistance">
          <el-radio :label="1">5公里以下</el-radio>
          <el-radio :label="2">5 - 10公里</el-radio>
          <el-radio :label="3">10 - 20公里</el-radio>
          <el-radio :label="4">20公里以上</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="是否有其他运动的习惯"
        prop="otherSport"
        :rules="{
          required: true,
          message: '请选择是否有其他运动的习惯',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.otherSport">
          <el-radio :label="1">是</el-radio>
          <el-radio :label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-if="form.otherSport === 1"
        label="其他运动每周的频率"
        prop="weeklySportTime"
        :rules="{
          required: true,
          message: '请选择其他运动每周的频率',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.weeklySportTime">
          <el-radio :label="1">1 - 2次</el-radio>
          <el-radio :label="2">3 - 6次</el-radio>
          <el-radio :label="3">7次以上</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="过去一年内完成过的赛事"
        prop="pastYearRace"
        :rules="{
          required: true,
          message: '请选择过去一年内完成过的赛事',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.pastYearRace">
          <el-radio :label="1">全马</el-radio>
          <el-radio :label="2">半马</el-radio>
          <el-radio :label="3">无</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 既往病史 -->
      <el-form-item
        label="是否有慢性疾病（心血管、呼吸、神经等）"
        prop="haveChronicDisease"
        :rules="{
          required: true,
          message: '请选择是否有慢性疾病',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.haveChronicDisease">
          <el-radio :label="1">是</el-radio>
          <el-radio :label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="近期是否有严重外伤、骨折、关节问题"
        prop="haveBoneInjury"
        :rules="{
          required: true,
          message: '请选择近期是否有严重外伤、骨折、关节问题',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.haveBoneInjury">
          <el-radio :label="1">是</el-radio>
          <el-radio :label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="过去一年内是否因健康问题接受住院治疗"
        prop="haveHospitalized"
        :rules="{
          required: true,
          message: '请选择过去一年内是否因健康问题接受住院治疗',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.haveHospitalized">
          <el-radio :label="1">是</el-radio>
          <el-radio :label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="是否有医生建议你不要剧烈运动"
        prop="haveDoctorAdvice"
        :rules="{
          required: true,
          message: '请选择是否有医生建议你不要剧烈运动',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.haveDoctorAdvice">
          <el-radio :label="1">是</el-radio>
          <el-radio :label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="直系亲属（父母、兄弟姐妹）是否有心血管疾病史"
        prop="haveFamilyChronicDisease"
        :rules="{
          required: true,
          message: '请选择直系亲属是否有心血管疾病史',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.haveFamilyChronicDisease">
          <el-radio :label="1">是</el-radio>
          <el-radio :label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="有无家族猝死史"
        prop="haveFamilySuddenDeath"
        :rules="{
          required: true,
          message: '请选择有无家族猝死史',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.haveFamilySuddenDeath">
          <el-radio :label="1">是</el-radio>
          <el-radio :label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 是否请求人工复审 -->
      <!-- <el-form-item
        label="是否请求人工复审"
        prop="needReview"
        :rules="{
          required: true,
          message: '请选择是否请求人工复审',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.needReview">
          <el-radio :label="1">是</el-radio>
          <el-radio :label="2">否</el-radio>
        </el-radio-group>
      </el-form-item> -->

      <!-- 文件上传 -->
      <el-form-item
        label="体检报告上传"
        :rules="{
          required: true,
          message: '请选择体检报告文件',
          trigger: 'change',
        }"
      >
        <el-button @click="handleChooseFile">选择文件</el-button>
        <input
          type="file"
          ref="fileInput"
          style="display: none"
          @change="onFileChange"
        />
        <div v-if="selectedFile">{{ selectedFile.name }}</div>
      </el-form-item>

      <el-form-item label="其他平台完赛记录上传">
        <el-button @click="handleChooseAppendixFile">选择附件</el-button>
        <input
          type="file"
          ref="appendixFileInput"
          style="display: none"
          @change="onAppendixFileChange"
        />
        <div v-if="selectedAppendixFile">{{ selectedAppendixFile.name }}</div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { healthAudit, fileUpload, checkAuditStatus } from "@/api/user";
import { ElMessage } from "element-plus";

const router = useRouter();
const formRef = ref(null);
const form = ref({
  userId: null,
  age: null,
  sex: null,
  height: null,
  weight: null,
  bmi: null,
  regularRun: null,
  weeklyRunDistance: null,
  otherSport: null,
  weeklySportTime: null,
  pastYearRace: null,
  haveChronicDisease: null,
  haveBoneInjury: null,
  haveHospitalized: null,
  haveDoctorAdvice: null,
  haveFamilyChronicDisease: null,
  haveFamilySuddenDeath: null,
  needReview: null,
  reportUrl: null,
  appendixUrl: null,
});

// 获取 localStorage 中的 userId
const userId = localStorage.getItem("userId");
if (userId) {
  form.value.userId = parseInt(userId);
}

const calculateBMI = () => {
  const height = form.value.height;
  const weight = form.value.weight;
  if (height && weight) {
    form.value.bmi = (weight / ((height / 100) * (height / 100))).toFixed(2);
  } else {
    form.value.bmi = null;
  }
};

const submitForm = async () => {
  formRef.value.validate(async (valid) => {
    try {
      // 审核状态检查
      const auditStatus = await checkAuditStatus({ userId: form.value.userId });
      if (auditStatus.data.data) {
        ElMessage.error("您已有处于审核中或有效期内的记录，请勿重复提交");
        return;
      }
    } catch (error) {
      console.error("审核状态检查失败", error);
      return;
    }

    if (!valid) {
      ElMessage.error("请填写完整表单");
      return; // 校验不通过时，直接返回
    }

    if (!selectedFile.value) {
      ElMessage.error("请选择体检报告文件");
      return;
    }

    try {
      const formData = new FormData();
      formData.append("file", selectedFile.value);
      const response = await fileUpload(formData);
      console.log("文件上传成功", response.data.data);
      form.value.reportUrl = response.data.data;

      if (selectedAppendixFile.value) {
        const appendixFormData = new FormData();
        appendixFormData.append("file", selectedAppendixFile.value);
        const appendixResponse = await fileUpload(appendixFormData);
        console.log("附件上传成功", appendixResponse.data.data);
        form.value.appendixUrl = appendixResponse.data.data;
      }
    } catch (error) {
      console.error("文件上传失败", error);
      return;
    }

    try {
      const response = await healthAudit(form.value);
      console.log("提交成功", response);
      // 提交成功后进行路由跳转
      router.push("/user/health");
    } catch (error) {
      console.error("提交失败", error);
      // 可以在这里添加提交失败后的提示逻辑
    }
  });
};

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields(); // 重置表单字段
  }
  // 重新初始化 form 数据
  form.value = {
    userId: form.value.userId, // 保留 userId
    age: null,
    sex: null,
    height: null,
    weight: null,
    bmi: null,
    regularRun: null,
    weeklyRunDistance: null,
    otherSport: null,
    weeklySportTime: null,
    pastYearRace: null,
    haveChronicDisease: null,
    haveBoneInjury: null,
    haveHospitalized: null,
    haveDoctorAdvice: null,
    haveFamilyChronicDisease: null,
    haveFamilySuddenDeath: null,
    needReview: null,
    reportUrl: null,
    appendixUrl: null,
  };
  selectedFile.value = null; // 重置已选择的文件
  selectedAppendixFile.value = null; // 重置已选择的附件
};

const fileInput = ref(null);
const selectedFile = ref(null);
const appendixFileInput = ref(null);
const selectedAppendixFile = ref(null);

const handleChooseFile = () => {
  fileInput.value.click();
};

const onFileChange = (e) => {
  selectedFile.value = e.target.files[0];
};

const handleChooseAppendixFile = () => {
  appendixFileInput.value.click();
};

const onAppendixFileChange = (e) => {
  selectedAppendixFile.value = e.target.files[0];
};
</script>

<style scoped>
.form-container {
  width: 90%;
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.el-form {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.el-form-item {
  padding: 10px 0;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebeef5; /* 添加底部边框 */
}

.el-form-item:last-child {
  border-bottom: none; /* 最后一个表单项去掉底部边框 */
}

.el-form-item__label {
  flex: 0 0 180px;
  text-align: right;
  font-weight: bold;
  white-space: nowrap;
  color: #303133; /* 统一标签颜色 */
}

.el-form-item__content {
  flex: 1;
}

.el-input,
.el-radio-group {
  width: 100%; /* 让输入框和单选组宽度撑满父容器 */
}

.el-button {
  border-radius: 4px; /* 统一按钮圆角 */
  padding: 8px 16px; /* 调整按钮内边距 */
}

.el-button--primary {
  background-color: #409eff; /* 统一主要按钮颜色 */
  border-color: #409eff;
}
</style>
