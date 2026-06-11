package org.example.marathonservice.param;

import lombok.Data;

@Data
public class ReSubmitParam {
    //档案id
    private Long docId;
    //体检报告url
    private String reportUrl;
    //附件url
    private String appendixUrl;
}
