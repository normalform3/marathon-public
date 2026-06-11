<template>
  <div class="app-page app-page--wide race-list-container">
    <div class="app-section-title">
      <div>
        <span class="app-eyebrow">RACES</span>
        <h1>精选赛事</h1>
        <p>按名称、类型与城市快速筛选，找到适合自己的下一场比赛。</p>
      </div>
    </div>

    <section class="app-card search-card">
      <el-form :inline="true" :model="param" class="search-form">
        <el-form-item label="赛事名称">
          <el-input v-model="param.raceName" placeholder="请输入赛事名称" clearable />
        </el-form-item>

        <el-form-item label="赛事类型">
          <el-select v-model="param.raceType" placeholder="请选择赛事类型" clearable>
            <el-option label="全马" :value="1" />
            <el-option label="半马" :value="2" />
            <el-option label="10公里" :value="3" />
            <el-option label="5公里" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="地点">
          <el-input v-model="param.location" placeholder="请输入地点" clearable />
        </el-form-item>

        <el-form-item class="form-actions">
          <el-button type="primary" @click="fetchRaceList">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="app-card app-table-card table-card">
      <el-table
        :data="raceList"
        stripe
        style="width: 100%"
        v-loading="loading"
        empty-text="暂无赛事数据"
      >
        <el-table-column prop="raceId" label="赛事编号" width="120" align="center" />
        <el-table-column prop="raceName" label="赛事名称" min-width="200" align="center" />
        <el-table-column prop="typeText" label="赛事类型" width="100" align="center">
          <template #default="{ row }">
            {{ convertRaceType(row.raceType) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="160" align="center">
          <template #default="{ row }">
            <el-tooltip :content="row.location" placement="top">
              <span class="location-text">{{ row.location }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="赛事时间" width="130" align="center">
          <template #default="{ row }">
            <div class="date-cell">{{ formatDate(row.raceDate) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="220" align="center">
          <template #default="{ row }">
            <div class="date-range">
              <span>{{ formatDate(row.enrollStart) }}</span>
              <span class="date-separator">至</span>
              <span>{{ formatDate(row.enrollEnd) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="赛事状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.raceStatus)" effect="light">
              {{ convertRaceStatus(row.raceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="getRaceDetail(row.id)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getRaceList } from "@/api/user";
import { useRouter } from "vue-router";
import {
  formatDate,
  raceStatusTag,
  raceStatusText,
  raceTypeText,
} from "@/utils/display";

const router = useRouter();
const loading = ref(false);
const raceList = ref([]);
const param = ref({
  raceName: "",
  raceType: "",
  location: "",
});

const fetchRaceList = async () => {
  try {
    loading.value = true;
    const res = await getRaceList(param.value);
    if (res.data && res.data.data) {
      raceList.value = res.data.data.records || res.data.data;
      raceList.value = raceList.value.map((item) => ({
        ...item,
        typeText: convertRaceType(item.raceType),
        statusText: convertRaceStatus(item.raceStatus),
      }));
    }
  } catch (error) {
    console.error("获取赛事列表失败:", error);
  } finally {
    loading.value = false;
  }
};

const resetFilters = () => {
  param.value = {
    raceName: "",
    raceType: "",
    location: "",
  };
  fetchRaceList();
};

const getRaceDetail = (id) => {
  router.push(`/race/detail/${id}`);
};

const convertRaceType = (type) => raceTypeText(type);
const convertRaceStatus = (status) => raceStatusText(status);
const getStatusTagType = (status) => raceStatusTag(status);

onMounted(fetchRaceList);
</script>

<style scoped>
.search-card {
  padding: 22px;
  margin-bottom: 22px;
}

.search-form {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 8px;
}

.search-form :deep(.el-input),
.search-form :deep(.el-select) {
  width: 210px;
}

.form-actions {
  margin-left: auto;
}

.table-card {
  overflow: hidden;
}

:deep(.el-tag) {
  min-width: 64px;
}

.date-cell,
.date-range {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.date-range {
  color: #64748b;
  font-size: 13px;
  flex-direction: column;
}

.date-separator {
  color: var(--app-accent);
  font-size: 12px;
}

.location-text {
  display: inline-block;
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .search-form {
    display: block;
  }

  .search-form :deep(.el-input),
  .search-form :deep(.el-select) {
    width: 100%;
  }

  .form-actions {
    display: flex;
    justify-content: center;
    margin-left: 0;
  }
}
</style>
