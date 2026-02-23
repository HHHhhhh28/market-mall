package com.zky.common.constans;

public class Constants {

    public final static String SPLIT = ",";
    public final static String COLON = ":";
    public final static String SPACE = " ";
    public final static String UNDERLINE = "_";

    public static class RedisKey {
        /**
         * 首页商品缓存前缀
         */
        public static final String MALL_PRODUCT_KEY = "mall_product_key_";
        /**
         * 拼团商品缓存前缀
         */
        public static final String GROUP_BUY_PRODUCT_KEY = "group_buy_product_key_";
        /**
         * 用户缓存前缀
         */
        public static final String MALL_USER_KEY = "mall_user_key_";

    }
}
