package com.wuc.arouter_api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import com.wuc.arouter_annotation.bean.RouterBean;

/**
 * 整个目标
 * 第一步：查找 ARouter$$Group$$personal ---> ARouter$$Path$$personal
 * 第二步：使用 ARouter$$Group$$personal ---> ARouter$$Path$$personal
 */
public class RouterManager {

  // 路由的组名 app，order，personal ...
  private String group;
  // 路由的路径  例如：/order/OrderMainActivity
  private String path;
  /**
   * 上面定义的两个成员变量意义：
   * 1.拿到ARouter$$Group$$personal  根据组名 拿到 ARouter$$Path$$personal
   * 2.操作路径，通过路径 拿到  PersonalMainActivity.class，就可以实现跳转了
   */

  private volatile static RouterManager instance;

  private RouterManager() {
    groupLruCache = new LruCache<>(100);
    pathLruCache = new LruCache<>(100);
  }

  public static RouterManager getInstance() {
    if (instance == null) {
      synchronized (RouterManager.class) {
        if (instance == null) {
          instance = new RouterManager();
        }
      }
    }
    return instance;
  }

  // 提供性能  LRU缓存
  private LruCache<String, ARouterGroup> groupLruCache;
  private LruCache<String, ARouterPath> pathLruCache;

  // 为了拼接，例如:ARouter$$Group$$personal
  private final static String FILE_GROUP_NAME = "ARouter$$Group$$";

  /***
   * @param path 例如：/order/Order_MainActivity
   * @return BundleManager
   */
  public BundleManager build(String path) {
    if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
      throw new IllegalArgumentException("path 写法错误，正确写法：如 /order/OrderMainActivity");
    }

    if (path.lastIndexOf("/") == 0) {// 只写了一个 /
      throw new IllegalArgumentException("path 写法错误，正确写法：如 /order/OrderMainActivity");
    }

    // 截取组名  /order/OrderMainActivity  finalGroup=order
    String finalGroup = path.substring(1, path.indexOf("/", 1));// finalGroup = order
    if (TextUtils.isEmpty(finalGroup)) {
      throw new IllegalArgumentException("path 写法错误，正确写法：如 /order/OrderMainActivity");
    }
    // 例如：order，personal
    this.group = finalGroup;
    // 最终的效果：如 /order/OrderMainActivity
    this.path = path;
    return new BundleManager();
  }

  public Object navigation(Context context, BundleManager bundleManager) {
    // 例如：寻找 ARouter$$Group$$personal  寻址   ARouter$$Group$$order   ARouter$$Group$$app
    String groupClassName = context.getPackageName() + "." + FILE_GROUP_NAME + group;
    Log.e("wuc >>>", "navigation: groupClassName=" + groupClassName);

    try {
      // TODO 第一步 读取路由组Group类文件
      ARouterGroup loadGroup = groupLruCache.get(group);
      if (null == loadGroup) {
        // 加载APT路由组Group类文件 例如：ARouter$$Group$$order
        Class<?> aClass = Class.forName(groupClassName);
        // 初始化类文件
        loadGroup = (ARouterGroup) aClass.newInstance();
        groupLruCache.put(group, loadGroup);

        if (loadGroup.getGroupMap().isEmpty()) {
          throw new RuntimeException("路由表Group报废了..."); // Group这个类 加载失败
        }
      }
      // TODO 第二步 读取路由Path类文件
      ARouterPath loadPath = pathLruCache.get(path);
      if (null == loadPath) {
        // 1.invoke loadGroup
        // 2.Map<String, Class<? extends ARouterLoadPath>>
        Class<? extends ARouterPath> aClass = loadGroup.getGroupMap().get(group);
        // 3.从map里面获取 ARouter$$Path$$personal.class
        loadPath = aClass.newInstance();
        pathLruCache.put(path, loadPath);
      }
      // TODO 第三步 跳转
      if (loadPath != null) {
        if (loadPath.getPathMap().isEmpty()) {// pathMap.get("key") == null
          throw new RuntimeException("路由表Path报废了...");
        }
        RouterBean routerBean = loadPath.getPathMap().get(path);
        if (routerBean != null) {
          switch (routerBean.getTypeEnum()) {
            case ACTIVITY:
              Intent intent = new Intent(context, routerBean.getMyClass()); // 例如：getClazz == Order_MainActivity.class
              intent.putExtras(bundleManager.getBundle()); // 携带参数
              context.startActivity(intent);
              break;
            case CALL:
              Class<?> clazz = routerBean.getMyClass();
              Call call = (Call) clazz.newInstance();
              bundleManager.setCall(call);
              return bundleManager.getCall();
            default:
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
