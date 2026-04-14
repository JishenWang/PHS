package com.pethospital.pet_hospital.vo.owner;

import lombok.Data;
import java.util.List;

/**
 * 咨询详情视图对象
 */
@Data
public class ConsultDetailVo {

    private Long id;

    private String consultationNo;

    private String title;

    private String content;

    private String images;

    private String petName;

    private String doctorName;

    private String status;

    private String statusName;

    private Integer rating;

    private String ratingComment;

    private Boolean rated;

    private String createTime;

    private List<ConsultReplyVo> replies;
}

@Data
class ConsultReplyVo {
    private Long id;
    private String senderType;
    private String senderName;
    private String content;
    private String images;
    private String createTime;
}