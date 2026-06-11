<template>
  <div class="rule-container">
    <div class="rule-header">
      <h2>规则管理列表</h2>
      <el-button @click="showAddRuleDialog" type="primary">新增规则</el-button>
    </div>

    <!-- 规则列表 -->
    <div class="rule-list">
      <el-table :data="ruleList" :border="true" style="width: 100%">
        <el-table-column label="切换" width="100" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              @click="enableRule(row.id)"
              type="success"
            >
              启用
            </el-button>
          </template>
        </el-table-column>
        <el-table-column
          prop="name"
          label="规则名称"
          width="150"
          align="center"
        ></el-table-column>
        <el-table-column label="规则内容" width="200" align="center">
          <template #default="{ row }">
            <el-link @click="showRuleDetail(row.content)">查看详情</el-link>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="200"
          align="center"
        >
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="规则状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{
              convertRuleStatus(row.status)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button @click="deleteRuleConfirm(row.id)" type="danger">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 规则详情对话框 -->
    <el-dialog v-model="dialogVisible" title="规则详情">
      <p>{{ ruleDetail }}</p>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增规则对话框 -->
    <el-dialog v-model="addRuleDialogVisible" title="新增规则">
      <el-form :model="newRule" ref="addRuleFormRef" label-width="80px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="newRule.name"></el-input>
        </el-form-item>
        <el-form-item label="规则内容" prop="content">
          <el-input v-model="newRule.content" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addRuleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddRuleForm">确定</el-button>
      </template>
    </el-dialog>
    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteConfirmVisible" title="确认删除">
      <p>确定要删除该规则吗？</p>
      <template #footer>
        <el-button @click="deleteConfirmVisible = false">取消</el-button>
        <el-button type="danger" @click="deleteSelectedRule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from "vue";
import {
  getAllRules,
  enableRule as enableRule2,
  addRule,
  deleteRule,
} from "@/api/admin";
import { ElMessage } from "element-plus";

const ruleList = ref([]);
const dialogVisible = ref(false);
const ruleDetail = ref("");
const addRuleDialogVisible = ref(false);
const deleteConfirmVisible = ref(false);
const newRule = reactive({
  name: "",
  content: "",
  status: 0,
  createTime: new Date(),
});
const addRuleFormRef = ref(null);
const ruleToDelete = ref(null);

// 规则状态转换
const convertRuleStatus = (status) => {
  if (status === 0) return "未启用";
  if (status === 1) return "已启用";
  return "";
};

// 获取规则状态对应的标签类型
const getStatusType = (status) => {
  if (status === 0) return "warning";
  if (status === 1) return "success";
  return "";
};

// 时间格式化
const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  const hours = `${date.getHours()}`.padStart(2, "0");
  const minutes = `${date.getMinutes()}`.padStart(2, "0");
  const seconds = `${date.getSeconds()}`.padStart(2, "0");
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 获取规则列表
const fetchRuleList = async () => {
  try {
    const res = await getAllRules();
    if (res.data && res.data.code === 200) {
      ruleList.value = res.data.data;
    } else {
      ElMessage.error(res.data.message || "获取规则列表失败");
    }
  } catch (error) {
    console.error("获取规则列表出错", error);
    ElMessage.error("获取规则列表失败，请稍后重试");
  }
};

// 启用规则
const enableRule = async (id) => {
  try {
    const res = await enableRule2({ ruleId: id });
    if (res.data && res.data.code === 200) {
      ElMessage.success(res.data.message);
      // 重新获取规则列表
      await fetchRuleList();
    } else {
      ElMessage.error(res.data.message || "规则启用失败");
    }
  } catch (error) {
    console.error("启用规则出错", error);
    ElMessage.error("规则启用失败，请稍后重试");
  }
};

// 显示规则详情弹窗
const showRuleDetail = (content) => {
  ruleDetail.value = content;
  dialogVisible.value = true;
};

// 显示新增规则弹窗
const showAddRuleDialog = () => {
  addRuleDialogVisible.value = true;
};

// 提交新增规则表单
const submitAddRuleForm = async () => {
  try {
    const res = await addRule(newRule);
    if (res.data && res.data.code === 200) {
      ElMessage.success(res.data.message);
      addRuleDialogVisible.value = false;
      // 重新获取规则列表
      await fetchRuleList();

      // 重置表单
      newRule.name = "";
      newRule.content = "";
    } else {
      ElMessage.error(res.data.message || "规则添加失败");
    }
  } catch (error) {
    console.error("添加规则出错", error);
    ElMessage.error("规则添加失败，请稍后重试");
  }
};

// 删除规则确认
const deleteRuleConfirm = (id) => {
  ruleToDelete.value = id;
  deleteConfirmVisible.value = true;
};

// 删除选中的规则
const deleteSelectedRule = async () => {
  try {
    const res = await deleteRule({ ruleId: ruleToDelete.value });
    if (res.data && res.data.code === 200) {
      ElMessage.success(res.data.message);
      deleteConfirmVisible.value = false;
      // 重新获取规则列表
      await fetchRuleList();
    } else {
      ElMessage.error(res.data.message || "规则删除失败");
    }
  } catch (error) {
    console.error("删除规则出错", error);
    ElMessage.error("规则删除失败，请稍后重试");
  }
};

onMounted(fetchRuleList);
</script>

<style scoped>
.rule-container {
  width: 90%;
  max-width: 925px;
  margin: 20px auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.rule-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.rule-list {
  margin-top: 20px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table--border) {
  border: 1px solid #ebeef5;
}

:deep(.el-tag) {
  min-width: 60px;
}
</style>
