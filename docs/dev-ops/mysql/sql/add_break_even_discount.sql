-- 给 lottery_coupon_strategy 表加 break_even_discount 列
ALTER TABLE lottery_coupon_strategy 
  ADD COLUMN IF NOT EXISTS break_even_discount decimal(10,2) DEFAULT NULL COMMENT '保本让利上限' AFTER roi_score;

-- 补充已有数据的保本上限值
UPDATE lottery_coupon_strategy 
  SET break_even_discount = ROUND(avg_order_price * (category_margin - 0.05), 2) 
  WHERE break_even_discount IS NULL AND avg_order_price > 0;
