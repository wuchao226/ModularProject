package com.wuc.personal;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatImageView;
import com.wuc.arouter_annotation.ARouter;
import com.wuc.arouter_annotation.Parameter;
import com.wuc.arouter_api.ParameterManager;
import com.wuc.common.RecordPathManager;
import com.wuc.common.bean.Student;
import com.wuc.common.order.OrderDrawable;
import com.wuc.common.order.net.OrderAddress;
import com.wuc.common.order.net.OrderBean;
import com.wuc.common.order.user.IUser;
import com.wuc.common.utils.Cons;
import java.io.IOException;

@ARouter(path = "/personal/PersonalMainActivity")
public class PersonalMainActivity extends AppCompatActivity {

  @Parameter
  String name = "name";

  @Parameter
  String sex = "sex";

  @Parameter
  int age = 9;

  @Parameter(name = "/order/getDrawable")
  OrderDrawable orderDrawable;
  @Parameter(name = "/order/getUserInfo")
  IUser iUser; // 公共基础库common

  @Parameter
  Student student;

  // 拿order模块的 网络请求功能
  @Parameter(name = "/order/getOrderBean")
  OrderAddress orderAddress;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.personal_activity_main);
    ParameterManager.getInstance().loadParameter(this);
    Log.d(Cons.TAG, "onCreate: PersonalMainActivity name:" + name + ", sex:" + sex + ", age:" + age);

    int drawable = orderDrawable.getDrawable();
    AppCompatImageView img = findViewById(R.id.img);
    img.setImageResource(drawable);

    // 我输出 order模块的Bean休息
    Log.d(Cons.TAG, "order的Bean PersonalMainActivity onCreate: " + iUser.getUserInfo().toString());

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          OrderBean orderBean = orderAddress.getOrderBean("aa205eeb45aa76c6afe3c52151b52160", "144.34.161.97");
          Log.e(Cons.TAG, "从Personal跨组件到Order，并使用Order网络请求功能：" + orderBean.toString());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();

    // 输出 Student
    Log.d(Cons.TAG, "我的Personal onCreate 对象的传递:" + student.toString());
  }

  public void jumpApp(View view) {}

  public void jumpOrder(View view) {
    // todo 方式二 全局Map
    Class<?> targetActivity =
        RecordPathManager.startTargetActivity("order", "OrderMainActivity");
    startActivity(new Intent(this, targetActivity));
  }
}