package com.wuc.arouter_api;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.Serializable;

/**
 * @author : wuchao5
 * @date : 2020/11/24 10:56
 * @desciption : 跳转时 ，用于参数的传递
 */
public class BundleManager {
  // Intent传输  携带的值，保存到这里
  private Bundle bundle = new Bundle();

  public Bundle getBundle() {
    return bundle;
  }

  //TODO 新增点
  //底层业务接口
  private Call call;

  public Call getCall() {
    return call;
  }

  public void setCall(Call call) {
    this.call = call;
  }

  // 对外界提供，可以携带参数的方法
  public BundleManager withString(@NonNull String key, @Nullable String value) {
    bundle.putString(key, value);
    return this;
  }

  public BundleManager withBoolean(@NonNull String key, @Nullable boolean value) {
    bundle.putBoolean(key, value);
    return this;
  }

  public BundleManager withInt(@NonNull String key, @Nullable int value) {
    bundle.putInt(key, value);
    return this;
  }

  public BundleManager withSerializable(@NonNull String key, @Nullable Serializable object) {
    bundle.putSerializable(key, object);
    return this;
  }

  public BundleManager withBundle(Bundle bundle) {
    this.bundle = bundle;
    return this;
  }

  // 直接完成跳转
  public Object navigation(Context context) {
    return RouterManager.getInstance().navigation(context, this);
  }
}
