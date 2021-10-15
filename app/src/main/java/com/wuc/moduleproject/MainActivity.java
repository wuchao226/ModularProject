package com.wuc.moduleproject;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatImageView;
import com.wuc.arouter_annotation.ARouter;
import com.wuc.arouter_annotation.Parameter;
import com.wuc.arouter_api.ParameterManager;
import com.wuc.arouter_api.RouterManager;
import com.wuc.common.bean.Student;
import com.wuc.common.order.OrderDrawable;
import com.wuc.common.order.user.IUser;
import com.wuc.common.utils.Cons;
import com.wuc.order.OrderMainActivity;
import com.wuc.personal.PersonalMainActivity;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

  @Parameter(name = "/order/getDrawable")
  OrderDrawable orderDrawable;// 公共基础库common
  @Parameter(name = "/order/getUserInfo")
  IUser iUser; // 公共基础库common

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (BuildConfig.isRelease) {
      Log.e("MainActivity", "当前为：集成化模式，除app可运行，其他子模块都是Android Library");
    } else {
      Log.e("MainActivity", "当前为：组件化模式，app/order/personal子模块都可独立运行");
    }
    ParameterManager.getInstance().loadParameter(this);
    int drawable = orderDrawable.getDrawable();
    AppCompatImageView img = findViewById(R.id.img);
    img.setImageResource(drawable);

    // 我输出 order模块的Bean休息
    Log.d(Cons.TAG, "order的Bean MainActivity onCreate: " + iUser.getUserInfo().toString());
  }

  // app ---> Order订单
  public void jumpOrder(View view) {
    // Intent intent = new Intent(this, OrderMainActivity.class);
    // intent.putExtra("name", "Wuc");
    // startActivity(intent);
    // 使用我们自己写的路由 跳转交互
    RouterManager.getInstance()
        .build("/order/OrderMainActivity")
        .withString("name", "杜子腾")
        .navigation(this); // 组件和组件通信
  }

  // app ---> Personal我的
  public void jumpPersonal(View view) {
    // Intent intent = new Intent(this, PersonalMainActivity.class);
    // intent.putExtra("name", "Wuc");
    // startActivity(intent);

    Student student = new Student("Derry大大", "男", 99);

    // 使用我们自己写的路由 跳转交互
    RouterManager.getInstance()
        .build("/personal/PersonalMainActivity")
        .withString("name", "史甄湘")
        .withString("sex", "男")
        .withInt("age", 99)
        .withSerializable("student", student)
        .navigation(this);
  }
}