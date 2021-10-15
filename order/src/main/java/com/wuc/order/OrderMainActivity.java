package com.wuc.order;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.wuc.arouter_annotation.ARouter;
import com.wuc.arouter_annotation.Parameter;
import com.wuc.arouter_api.ParameterManager;
import com.wuc.arouter_api.RouterManager;
import com.wuc.common.RecordPathManager;
import com.wuc.common.utils.Cons;

@ARouter(path = "/order/OrderMainActivity")
public class OrderMainActivity extends AppCompatActivity {

  @Parameter
  String name;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.order_activity_main);
    // 用到才加载 == 懒加载
    ParameterManager.getInstance().loadParameter(this);

    Log.e(Cons.TAG, "order/OrderMainActivity name:" + name);
  }

  public void jumpApp(View view) {}

  public void jumpPersonal(View view) {
    RouterManager.getInstance()
        .build("/personal/PersonalMainActivity")
        .withString("name", "李元霸")
        .withString("sex", "男")
        .withInt("age", 99)
        .navigation(this);
    // todo 方式一 类加载
    // 类加载跳转，可以成功。维护成本较高且容易出现人为失误
    // try {
    //   Class targetClass = Class.forName("com.wuc.personal.PersonalMainActivity");
    //   Intent intent = new Intent(this, targetClass);
    //   intent.putExtra("name", "Wuc");
    //   startActivity(intent);
    // } catch (ClassNotFoundException e) {
    //   e.printStackTrace();
    // }

    // personal/PersonalMainActivity getMap
    // todo 方式二 全局Map
    // Class<?> targetActivity =
    //     RecordPathManager.startTargetActivity("personal", "PersonalMainActivity");
    // startActivity(new Intent(this, targetActivity));
  }
}