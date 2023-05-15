package jsr380;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author: yuanjinzhong
 * @date: 2023/5/10 10:40
 * @description:
 */
public interface LinterContext {

  /**
   * 当遍历的的方法
   *
   * @return
   */
  Method getMethod();

  /**
   * 当前的facade接口
   *
   * @return
   */
  Class getFacadeClass();

  /**
   * 当前接口的实现类
   *
   * @return
   */
  Class getFacadeImplClass();

  /**
   * facade class 和impl 的 映射,key：facade, value：impl
   *
   * @return
   */
  Map<Class, Class> getFacadeImps();
}
