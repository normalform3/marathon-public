import service from "../request";

//查询赛事列表
export function getRaceList(data) {
  return service({
    url: "/race/list",
    method: "post",
    //请求体
    data,
  });
}

//查询赛事详情
export function getRaceDetail(data) {
  return service({
    url: "/race/detail",
    method: "get",
    //查询参数
    params: data,
  });
}

//报名赛事
export function registerRace(data) {
  return service({
    url: "/registration/add",
    method: "post",
    data,
  });
}

//是否报名
export function isRegister(data) {
  return service({
    url: "/registration/isRegister",
    method: "post",
    data,
  });
}

//登录
export function login(data) {
  return service({
    url: "/user/login",
    method: "post",
    params: data,
  });
}

//注册
export function signUp(data) {
  return service({
    url: "/user/signup",
    method: "post",
    data,
  });
}

//获取用户信息
export function getUserDetail(data) {
  return service({
    url: "/user/detail",
    method: "get",
    params: data,
  });
}

//获取用户报名信息
export function getUserRegistrationList(data) {
  return service({
    url: "/registration/list",
    method: "post",
    data,
  });
}

//健康评估
export function healthAudit(data) {
  return service({
    url: "/health/audit",
    method: "post",
    data,
  });
}

//文件上传 返回url
export function fileUpload(data) {
  return service({
    url: "/health/upload",
    method: "post",
    data,
  });
}

//查询用户成绩
export function getGradeList(data) {
  return service({
    url: "/grade/list",
    method: "get",
    params: data,
  });
}

//查询用户健康档案
export function getHealthByUserId(data) {
  return service({
    url: "/health/getDocsByUserId",
    method: "get",
    params: data,
  });
}

//检查用户是否有已提交的审核，或者已审核且在有效期的记录
export function checkAuditStatus(data) {
  return service({
    url: "/health/checkAuditStatus",
    method: "get",
    params: data,
  });
}

//重新提交材料
export function reSubmit(data) {
  return service({
    url: "/health/resubmit",
    method: "post",
    data,
  });
}

//创建订单
export function createOrder(data) {
  return service({
    url: "/order/create",
    method: "post",
    data,
  });
}

//支付成功
export function paySuccess(data) {
  return service({
    url: "/order/paySuccess",
    method: "post",
    data,
  });
}

// 提交评论
export function submitComment(data) {
  return service({
    url: "/comment/submit",
    method: "post",
    data,
  });
}

//根据id查看评论
export function getCommentById(data) {
  return service({
    url: "/comment/getById",
    method: "get",
    params: data,
  });
}

//根据赛事id查看评论
export function getCommentByRaceId(data) {
  return service({
    url: "/comment/getByRaceId",
    method: "get",
    params: data,
  });
}

//获取首页的新闻、公告
export function getHomePageNews() {
  return service({
    url: "/news/getHomePage",
    method: "get",
  });
}

//获取通知
export function getNoticeById(data) {
  return service({
    url: "/notice/getNotice",
    method: "get",
    params: data,
  });
}

//通知设为已读
export function noticeSetRead(data) {
  return service({
    url: "/notice/setRead",
    method: "get",
    params: data,
  });
}

//查询路线
export function getRoute(data) {
  return service({
    url: "/route/get",
    method: "get",
    params: data,
  });
}

//获取全部新闻公告
export function getNewsList() {
  return service({
    url: "/news/getAll",
    method: "get",
  });
}

// 修改个人信息
export function updateUserInfo(data) {
  return service({
    url: "/user/update",
    method: "post",
    data,
  });
}
