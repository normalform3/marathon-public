import service from "../request";

//查询所有健康档案
export function getAllDocs() {
  return service({
    url: "/health/getAllDocs",
    method: "get",
  });
}

//审核通过
export function pass(data) {
  return service({
    url: "/health/pass",
    method: "get",
    params: data,
  });
}

//审核不通过
export function notPass(data) {
  return service({
    url: "/health/notPass",
    method: "get",
    params: data,
  });
}

//体检单不合格
export function notQualified(data) {
  return service({
    url: "/health/notQualified",
    method: "get",
    params: data,
  });
}

//健康档案设为待定
export function setPending(data) {
  return service({
    url: "/health/setPending",
    method: "get",
    params: data,
  });
}

//查看所有举办方申请
export function getAllApply() {
  return service({
    url: "/apply/list",
    method: "get",
  });
}

//通过举办方申请
export function approveApply(data) {
  return service({
    url: "/apply/approve",
    method: "post",
    params: data,
  });
}

//拒绝举办方申请
export function rejectApply(data) {
  return service({
    url: "/apply/reject",
    method: "post",
    params: data,
  });
}

//查看所有评论
export function getAllComments() {
  return service({
    url: "/comment/getAll",
    method: "get",
  });
}

//通过评论
export function passComment(data) {
  return service({
    url: "/comment/passById",
    method: "get",
    params: data,
  });
}

//删除评论
export function deleteComment(data) {
  return service({
    url: "/comment/deleteById",
    method: "get",
    params: data,
  });
}

//获取全部新闻公告
export function getAllNews() {
  return service({
    url: "/news/getAll",
    method: "get",
  });
}

//新增新闻
export function addNews(data) {
  return service({
    url: "/news/add",
    method: "post",
    data,
  });
}

//显示
export function showNews(data) {
  return service({
    url: "/news/show",
    method: "get",
    params: data,
  });
}

//隐藏
export function hideNews(data) {
  return service({
    url: "/news/hide",
    method: "get",
    params: data,
  });
}

//删除新闻
export function deleteNews(data) {
  return service({
    url: "/news/delete",
    method: "get",
    params: data,
  });
}

//查询全部规则
export function getAllRules() {
  return service({
    url: "/rule/all",
    method: "get",
  });
}

//启用规则
export function enableRule(data) {
  return service({
    url: "/rule/enable",
    method: "get",
    params: data,
  });
}

//新增规则
export function addRule(data) {
  return service({
    url: "/rule/add",
    method: "post",
    data,
  });
}

//删除规则
export function deleteRule(data) {
  return service({
    url: "/rule/delete",
    method: "get",
    params: data,
  });
}
