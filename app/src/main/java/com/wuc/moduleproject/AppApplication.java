package com.wuc.moduleproject;

import android.app.Application;
import com.wuc.common.RecordPathManager;
import com.wuc.order.OrderMainActivity;
import com.wuc.personal.PersonalMainActivity;

/**
 * @author : wuchao5
 * @date : 2020/11/15 17:13
 * @desciption :
 */
public class AppApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    // 如果项目有100个Activity，这种加法会不会太那个？  缺点
    RecordPathManager.addGroupInfo("app", "MainActivity", MainActivity.class);
    RecordPathManager.addGroupInfo("order", "OrderMainActivity", OrderMainActivity.class);
    RecordPathManager.addGroupInfo("personal", "PersonalMainActivity", PersonalMainActivity.class);
  }
}
