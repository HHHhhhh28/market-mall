package com.zky.domain.dto;

import lombok.Data;
import com.zky.domain.vo.ProductVO;
import java.util.List;

@Data
public class RecommendationResponseDTO {
    private List<ProductVO> items;
    private String recommendationId;
    private String algorithmUsed;
}
