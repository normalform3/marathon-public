import axios from "axios";

// 创建axios实例
const service = axios.create({
  baseURL: "/api",
  timeout: 30000,
});

// 添加请求拦截器
service.interceptors.request.use(
  (config) => {
    // 在发送请求之前动态获取token
    const token = localStorage.getItem("token");
    if (token) {
      // 设置请求头
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    // 处理请求错误
    console.log(error);
    return Promise.reject(error);
  }
);

export default service;
