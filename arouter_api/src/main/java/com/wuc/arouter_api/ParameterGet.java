package com.wuc.arouter_api;

/**
 * @author : wuchao5
 * @date : 2020/11/21 17:50
 * @desciption :
 */
public interface ParameterGet {

  /**
   * 目标对象.属性名 = getIntent().属性类型... 完成赋值操作
   *
   * @param targetParameter 目标对象：例如：MainActivity 中的那些属性
   */
  void getParameter(Object targetParameter);
}
