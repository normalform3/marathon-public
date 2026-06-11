export const raceTypeText = (type: number | string | undefined) => {
  const typeMap: Record<number, string> = {
    1: "全马",
    2: "半马",
    3: "10公里",
    4: "5公里",
  };
  return typeMap[Number(type)] || "未知类型";
};

export const raceStatusText = (status: number | string | undefined) => {
  const statusMap: Record<number, string> = {
    1: "未开始",
    2: "报名中",
    3: "报名结束",
    4: "已结束",
  };
  return statusMap[Number(status)] || "未知状态";
};

export const raceStatusTag = (status: number | string | undefined) => {
  const statusMap: Record<number, "info" | "success" | "warning" | "danger"> = {
    1: "info",
    2: "success",
    3: "warning",
    4: "danger",
  };
  return statusMap[Number(status)] || "info";
};

export const registrationModeText = (mode: number | string | undefined) => {
  const modeMap: Record<number, string> = {
    1: "超额报名后抽签",
    2: "限额报名，报完即止",
  };
  return modeMap[Number(mode || 1)] || "未知模式";
};

export const formatDate = (date: string | number | Date | undefined | null) => {
  if (!date) return "";
  const d = new Date(date);
  if (Number.isNaN(d.getTime())) return "";
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

export const formatDateHour = (
  date: string | number | Date | undefined | null,
) => {
  if (!date) return "";
  const d = new Date(date);
  if (Number.isNaN(d.getTime())) return "";
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const hour = String(d.getHours()).padStart(2, "0");
  return `${year}-${month}-${day} ${hour}:00`;
};
