package com.zky.domain.dto;

import lombok.Data;
import com.zky.common.enums.RecommendationType;

@Data
public class RecommendationRequestDTO {
    private String userId;
    private RecommendationType type;
    private int pageNum;
    private int pageSize;
}
