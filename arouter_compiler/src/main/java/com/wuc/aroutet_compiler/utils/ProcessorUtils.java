package com.wuc.aroutet_compiler.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author : wuchao5
 * @date : 2020/11/19 16:01
 * @desciption : 字符串、集合判空工具
 */
public class ProcessorUtils {
  public static boolean isEmpty(CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  public static boolean isEmpty(Collection<?> coll) {
    return coll == null || coll.isEmpty();
  }

  public static boolean isEmpty(final Map<?, ?> map) {
    return map == null || map.isEmpty();
  }
}
