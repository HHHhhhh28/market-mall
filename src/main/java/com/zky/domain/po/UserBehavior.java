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
public class UserBehavior {
    private Long id;
    private String userId;
    private String productId;
    private String behaviorType; // CLICK, COLLECT, BUY 点击/收藏/购买
    private Date createTime;
}
