<template>
  <div class="app-page organizer-apply-page">
    <el-form :model="form" ref="formRef" label-width="180px" class="app-card app-form-card apply-form">
      <div class="form-title">
        <span class="app-eyebrow">ORGANIZER APPLY</span>
        <h1>申请举办赛事</h1>
        <p>提交举办方资质与赛事基础信息，审核通过后即可进入赛事方后台。</p>
      </div>
      <!-- 举办方信息 -->
      <el-form-item
        label="举办方名称"
        prop="name"
        :rules="{
          required: true,
          message: '请输入举办方名称',
          trigger: 'blur',
        }"
      >
        <el-input v-model="form.name" placeholder="请输入举办方名称"></el-input>
      </el-form-item>
      <el-form-item
        label="举办方类型"
        prop="type"
        :rules="{
          required: true,
          message: '请选择举办方类型',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.type">
          <el-radio :label="1">政府</el-radio>
          <el-radio :label="2">企业</el-radio>
          <el-radio :label="3">个人</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="联系人姓名"
        prop="contactName"
        :rules="{
          required: true,
          message: '请输入联系人姓名',
          trigger: 'blur',
        }"
      >
        <el-input
          v-model="form.contactName"
          placeholder="请输入联系人姓名"
        ></el-input>
      </el-form-item>
      <el-form-item
        label="联系人电话"
        prop="contactPhone"
        :rules="{
          required: true,
          message: '请输入联系人电话',
          trigger: 'blur',
        }"
      >
        <el-input
          v-model="form.contactPhone"
          placeholder="请输入联系人电话"
        ></el-input>
      </el-form-item>
      <el-form-item
        label="联系人邮箱"
        prop="contactEmail"
        :rules="{
          required: true,
          message: '请输入联系人邮箱',
          trigger: 'blur',
        }"
      >
        <el-input
          v-model="form.contactEmail"
          placeholder="用于接收审批结果，请确保邮箱正确"
        ></el-input>
      </el-form-item>
      <el-form-item
        label="资质材料上传"
        :rules="{
          required: true,
          message: '请选择资质材料文件',
          trigger: 'change',
        }"
      >
        <el-button @click="handleChooseQualificationFile">选择文件</el-button>
        <input
          type="file"
          ref="qualificationFileInput"
          style="display: none"
          @change="onQualificationFileChange"
        />
        <div v-if="selectedQualificationFile">
          {{ selectedQualificationFile.name }}
        </div>
      </el-form-item>
      <el-form-item
        label="授权书上传"
        :rules="{
          required: true,
          message: '请选择授权书文件',
          trigger: 'change',
        }"
      >
        <el-button @click="handleChooseAuthorizationFile">选择文件</el-button>
        <input
          type="file"
          ref="authorizationFileInput"
          style="display: none"
          @change="onAuthorizationFileChange"
        />
        <div v-if="selectedAuthorizationFile">
          {{ selectedAuthorizationFile.name }}
        </div>
      </el-form-item>

      <!-- 赛事信息 -->
      <el-form-item
        label="赛事名称"
        prop="raceName"
        :rules="{ required: true, message: '请输入赛事名称', trigger: 'blur' }"
      >
        <el-input
          v-model="form.raceName"
          placeholder="请输入赛事名称"
        ></el-input>
      </el-form-item>
      <el-form-item
        label="赛事信息"
        prop="raceInfo"
        :rules="{ required: true, message: '请输入赛事信息', trigger: 'blur' }"
      >
        <el-input
          v-model="form.raceInfo"
          type="textarea"
          placeholder="请简要说明赛事信息"
        ></el-input>
      </el-form-item>
      <el-form-item
        label="赛事类型"
        prop="raceType"
        :rules="{
          required: true,
          message: '请选择赛事类型',
          trigger: 'change',
        }"
      >
        <el-radio-group v-model="form.raceType">
          <el-radio :label="1">全马</el-radio>
          <el-radio :label="2">半马</el-radio>
          <el-radio :label="3">10公里</el-radio>
          <el-radio :label="4">5公里</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="参赛人数"
        prop="participantNumber"
        :rules="{ required: true, message: '请输入参赛人数', trigger: 'blur' }"
      >
        <el-input
          v-model="form.participantNumber"
          type="number"
          placeholder="请输入参赛人数"
        ></el-input>
      </el-form-item>
      <el-form-item
        label="报名模式"
        prop="registrationMode"
        :rules="{ required: true, message: '请选择报名模式', trigger: 'change' }"
      >
        <el-radio-group v-model="form.registrationMode">
          <el-radio :label="1">超额报名后抽签</el-radio>
          <el-radio :label="2">限额报名，报完即止</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="费用"
        prop="fee"
        :rules="{ required: true, message: '请输入费用', trigger: 'blur' }"
      >
        <el-input
          v-model="form.fee"
          type="number"
          placeholder="请输入费用"
        ></el-input>
      </el-form-item>
      <el-form-item
        label="地点"
        prop="location"
        :rules="{ required: true, message: '请输入地点', trigger: 'blur' }"
      >
        <el-input v-model="form.location" placeholder="请输入地点"></el-input>
      </el-form-item>
      <el-form-item
        label="赛事开始时间"
        prop="raceDate"
        :rules="{
          required: true,
          message: '请选择赛事开始时间',
          trigger: 'change',
        }"
      >
        <el-date-picker
          v-model="form.raceDate"
          type="datetime"
          placeholder="需晚于当前时间3个月以上"
        ></el-date-picker>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
        <el-button text @click="router.push('/orglogin')">返回登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { organizerApply } from "@/api/org";
import { fileUpload } from "@/api/user";
import { useRouter } from "vue-router";

const formRef = ref(null);
const form = ref({
  name: null,
  type: null,
  contactName: null,
  contactPhone: null,
  contactEmail: null,
  qualificationUrl: null,
  authorizationUrl: null,
  raceName: null,
  raceInfo: null,
  raceType: null,
  participantNumber: null,
  registrationMode: 1,
  fee: null,
  location: null,
  raceDate: null,
  organizerId: null,
});

const qualificationFileInput = ref(null);
const selectedQualificationFile = ref(null);
const authorizationFileInput = ref(null);
const selectedAuthorizationFile = ref(null);

const handleChooseQualificationFile = () => {
  qualificationFileInput.value.click();
};

const onQualificationFileChange = (e) => {
  selectedQualificationFile.value = e.target.files[0];
};

const handleChooseAuthorizationFile = () => {
  authorizationFileInput.value.click();
};

const onAuthorizationFileChange = (e) => {
  selectedAuthorizationFile.value = e.target.files[0];
};

const router = useRouter();
const submitForm = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error("请填写完整表单");
      return;
    }

    if (!selectedQualificationFile.value || !selectedAuthorizationFile.value) {
      ElMessage.error("请选择资质材料和授权书文件");
      return;
    }

    try {
      const qualificationFormData = new FormData();
      qualificationFormData.append("file", selectedQualificationFile.value);
      const qualificationResponse = await fileUpload(qualificationFormData);
      console.log("资质材料文件上传成功", qualificationResponse.data.data);
      form.value.qualificationUrl = qualificationResponse.data.data;

      const authorizationFormData = new FormData();
      authorizationFormData.append("file", selectedAuthorizationFile.value);
      const authorizationResponse = await fileUpload(authorizationFormData);
      console.log("授权书文件上传成功", authorizationResponse.data.data);
      form.value.authorizationUrl = authorizationResponse.data.data;

      const response = await organizerApply(form.value);
      if (response.data.code === 200) {
        ElMessage.success("申请提交成功");
        router.push("/");
      } else {
        ElMessage.error("申请提交失败");
      }
    } catch (error) {
      console.error("提交失败", error);
      ElMessage.error("提交失败，请稍后重试");
    }
  });
};

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
  form.value = {
    name: null,
    type: null,
    contactName: null,
    contactPhone: null,
    contactEmail: null,
    qualificationUrl: null,
    authorizationUrl: null,
    raceName: null,
    raceInfo: null,
    raceType: null,
    participantNumber: null,
    registrationMode: 1,
    fee: null,
    location: null,
    raceDate: null,
    organizerId: null,
  };
  selectedQualificationFile.value = null;
  selectedAuthorizationFile.value = null;
};
</script>

<style scoped>
.organizer-apply-page {
  display: grid;
  place-items: start center;
}

.apply-form {
  width: 100%;
  max-width: 820px;
  margin: 0 auto;
  padding: 34px;
}

.form-title {
  margin-bottom: 28px;
}

.form-title h1 {
  margin: 0;
}

.form-title p {
  margin: 10px 0 0;
  color: var(--app-text-muted);
  line-height: 1.7;
}

.el-form-item {
  padding: 10px 0;
  display: flex;
  align-items: center;
}

.el-form-item__label {
  flex: 0 0 180px;
  text-align: right;
  font-weight: bold;
  white-space: nowrap;
}

.el-form-item__content {
  flex: 1;
}

@media (max-width: 720px) {
  .apply-form {
    padding: 22px 16px;
  }
}
</style>
