import service from "../request";

//查询赛事报名人数
export function getRegistCount(data) {
  return service({
    url: "/registration/count",
    method: "get",
    params: data,
  });
}

//提交举办方申请
export function organizerApply(data) {
  return service({
    url: "/apply/submit",
    method: "post",
    data,
  });
}

//查看赛事运动员信息
export function getAthletes(data) {
  return service({
    url: "/registration/athleteList",
    method: "get",
    params: data,
  });
}

//下载成绩模板
export function getTemplate(data) {
  return service({
    url: "/grade/download-template",
    method: "get",
    params: data,
    responseType: "blob", //使用文件流返回
  });
}

//上传成绩文件
export function uploadGrades(formData) {
  return service({
    url: "/grade/upload-grades",
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data", // 上传文件
    },
  });
}

//获取赛事状态
export function getRaceStatus(data) {
  return service({
    url: "/grade/race-status",
    method: "get",
    params: data,
  });
}

//提交路线
export function submitRoute(data) {
  return service({
    url: "/route/submit",
    method: "post",
    data,
  });
}

//是否存在路线
export function existRoute(data) {
  return service({
    url: "/route/exist",
    method: "get",
    params: data,
  });
}

//修改赛事信息
export function updateRace(data) {
  return service({
    url: "/race/update",
    method: "post",
    data,
  });
}
