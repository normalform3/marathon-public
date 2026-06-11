<template>
  <div>
    <el-button @click="handleCreateOrder">创建/查询订单</el-button>
    <el-button @click="handlePaySuccess" :disabled="!orderId"
      >支付成功</el-button
    >
    <div v-if="orderId">订单号: {{ orderId }}</div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { createOrder, paySuccess } from "@/api/user";

const route = useRoute();
const orderId = ref("");
const raceId = route.params.raceId;
const userId = localStorage.getItem("userId");

const handleCreateOrder = async () => {
  try {
    const data = {
      raceId: Number(raceId),
      userId: Number(userId),
    };
    const response = await createOrder(data);
    if (response.data.code === 200) {
      orderId.value = response.data.data;
      console.log("创建订单成功，订单号:", orderId.value);
    } else {
      console.error("创建订单失败:", response.data.message);
    }
  } catch (error) {
    console.error("创建订单出错:", error);
  }
};

const router = useRouter();
const handlePaySuccess = async () => {
  try {
    const data = {
      id: Number(orderId.value),
      raceId: Number(raceId),
      userId: Number(userId),
    };
    console.log(data);
    const response = await paySuccess(data);
    console.log(response.data);
    if (response.data.code === 200) {
      router.push("/user/enroll");
    } else {
      console.error("支付失败:", response.data.message);
    }
  } catch (error) {
    console.error("支付出错:", error);
  }
};
</script>

<style scoped>
/* 可根据需要添加样式 */
</style>
