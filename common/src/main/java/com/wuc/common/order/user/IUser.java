package com.wuc.common.order.user;

import com.wuc.arouter_api.Call;

/**
 * @author : wuchao5
 * @date : 2020/11/25 22:36
 * @desciption :
 */
public interface IUser extends Call {
  /**
   * @return 根据不同子模块的具体实现，调用得到不同的结果
   */
  BaseUser getUserInfo();
}
