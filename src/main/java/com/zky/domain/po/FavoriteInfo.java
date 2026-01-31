package com.zky.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteInfo {
    private Long id;
    private String favoriteId;
    private String userId;
    private String productId;
    private Date createTime;
}
