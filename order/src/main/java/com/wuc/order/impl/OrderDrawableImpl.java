package com.wuc.order.impl;

import com.wuc.arouter_annotation.ARouter;
import com.wuc.common.order.OrderDrawable;
import com.wuc.order.R;

/**
 * @author : wuchao5
 * @date : 2020/11/25 19:11
 * @desciption : order 自己决定 自己的暴漏
 */
@ARouter(path = "/order/getDrawable")
public class OrderDrawableImpl implements OrderDrawable {
  @Override public int getDrawable() {
    return R.drawable.ic_baseline_bathtub_24;
  }
}
