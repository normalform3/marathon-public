package org.example.marathonservice.param;

import lombok.Data;

@Data
public class BasePageParam {
    //查询页码,当前第几页,0时不分页,小于0只查询总数
    private Integer currentPage = 1;

    //每页显示x条,0时不分页,小于0只查询总数
    private Integer pageSize = 10;
}

