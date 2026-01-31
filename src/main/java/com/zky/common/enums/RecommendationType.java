package com.zky.common.enums;

public enum RecommendationType {
    MALL_HOME("MALL_HOME", "商城首页-普通商品"),
    GROUP_BUY("GROUP_BUY", "拼团活动-拼团商品"),
    LOTTERY("LOTTERY", "抽奖活动-抽奖商品");

    private final String code;
    private final String desc;

    RecommendationType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
