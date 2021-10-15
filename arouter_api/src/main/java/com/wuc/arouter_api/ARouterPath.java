package com.wuc.arouter_api;

import com.wuc.arouter_annotation.bean.RouterBean;
import java.util.Map;

/**
 * @author : wuchao5
 * @date : 2020/11/18 16:01
 * @desciption :
 * 其实就是 路由组 Group 对应的 ---- 详细Path加载数据接口 ARouterPath
 * 例如：order分组 对应 ---- 有那些类需要加载（OrderMainActivity  OrderMainActivity2 ...）
 *
 * TODO
 *  key:   /app/MainActivity
 *  value:  RouterBean(MainActivity.class)
 */
public interface ARouterPath {
  /**
   * 例如：order分组下有这些信息，personal分组下有这些信息
   *
   * @return key:"/order/OrderMainActivity"   或  "/personal/PersonalMainActivity"
   * value: RouterBean==OrderMainActivity.class 或 RouterBean=PersonalMainActivity.class
   */
  Map<String, RouterBean> getPathMap();
}
