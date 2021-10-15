package com.wuc.arouter_api;

import android.app.Activity;
import android.util.Log;
import android.util.LruCache;

/**
 * 参数的 加载管理器
 * TODO 这是用于接收参数的
 *
 * 第一步：查找 PersonalMainActivity$$Parameter
 * 第二步：使用 PersonalMainActivity$$Parameter  this 给他
 */
public class ParameterManager {

  private volatile static ParameterManager instance = null;

  // LRU缓存 key=类名      value=参数加载接口
  private LruCache<String, ParameterGet> cache;

  private ParameterManager() {
    cache = new LruCache<>(100);
  }

  public static ParameterManager getInstance() {
    if (instance == null) {
      synchronized (ParameterManager.class) {
        if (instance == null) {
          instance = new ParameterManager();
        }
      }
    }
    return instance;
  }

  // 为什么还要拼接，此次拼接 是为了寻找他
  static final String FILE_SUFFIX_NAME = "$$Parameter"; // 为了这个效果：OrderMainActivity + $$Parameter

  // 使用者 只需要调用这一个方法，就可以进行参数的接收
  public void loadParameter(Activity activity) {// 必须拿到 PersonalMainActivity
    String className = activity.getClass().getName(); // className == PersonalMainActivity
    ParameterGet parameterLoad = cache.get(className);
    if (null == parameterLoad) {
      // 拼接 如：OrderMainActivity + $$Parameter
      // 类加载OrderMainActivity + $$Parameter
      try {
        // 类加载PersonalMainActivity + $$Parameter
        Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
        // 用接口parameterLoad = 接口的实现类PersonalMainActivity
        parameterLoad = (ParameterGet) aClass.newInstance();
        cache.put(className, parameterLoad);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    parameterLoad.getParameter(activity);
  }
}
