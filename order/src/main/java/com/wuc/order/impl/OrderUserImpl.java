package com.wuc.order.impl;

import com.wuc.arouter_annotation.ARouter;
import com.wuc.common.order.user.BaseUser;
import com.wuc.common.order.user.IUser;
import com.wuc.order.model.UserInfo;

/**
 * personal模块实现的内容
 */
@ARouter(path = "/order/getUserInfo")
public class OrderUserImpl implements IUser {

  @Override
  public BaseUser getUserInfo() {
    // 我order模块，具体的Bean，由我自己
    UserInfo userInfo = new UserInfo();
    userInfo.setName("Derry");
    userInfo.setAccount("154325354");
    userInfo.setPassword("1234567890");
    userInfo.setVipLevel(999);
    return userInfo;
  }
}
