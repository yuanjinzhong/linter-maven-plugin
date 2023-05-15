package jsr380.strategy;

import javax.validation.Valid;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author: yuanjinzhong
 * @date: 2023/5/9 17:16
 * @description: 若一个方法参数是个对象，且有{@link javax.validation.Valid} 修饰，则应该级联校验该对象内部是否包含jsr注解
 *     <p>同时关注对象内部属性是否包含{@link javax.validation.Valid} 修饰
 */
public class CascadeForValidLinterStrategyImpl extends Jsr380LinterStrategy {
  @Override
  public void linter() {

    for (Class facadeClass : facadeImps.keySet()) {

      for (Method method : facadeClass.getDeclaredMethods()) {

        // 范型参数列表(包含普通参数)
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        // 参数上的注解列表
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        // 遍历参数
        for (int i = 0; i < genericParameterTypes.length; i++) {
          for (Annotation annotation : parameterAnnotations[i]) {
            // 校验不通过则将错误信息放到集合里面
            findJsrAnnotation(method, annotation, genericParameterTypes[i]);
          }
        }
      }
    }
  }

  private void findJsrAnnotation(Method method, Annotation annotation, Type genericParameterType) {

    // 有级联注解，说明需要校验注解对应的实体有没有jsr约束
    // 递归的找，实体嵌套的场景
    // 参数上有valid javax.validation.constraints
    if (annotation instanceof Valid) {

      Boolean hasJsr380 = false;

      // 则看这个参数是不是范型容器
      if (genericParameterType instanceof ParameterizedType) {

        Type[] actualTypeArguments =
            ((ParameterizedType) genericParameterType).getActualTypeArguments();

        for (Type actualTypeArgument : actualTypeArguments) {

          // 获取实际类型的参数,遍历看是否有jsr380注解
          hasJsr380 = isJsr380Exist(method, hasJsr380, (Class<?>) actualTypeArgument);
          // 范型参数错误信息记录
          if (!hasJsr380) {
            linterErrorMsgs.add(
                String.format(
                    "Rpc接口:【%s】的参数:【%s】由【@Valid】修饰，则内部字段需要添加相关约束注解",
                    method.getDeclaringClass().getSimpleName() + "#" + method.getName(),
                    ((Class<?>) actualTypeArgument).getSimpleName()));
          }
        }
      } else {

        // 普通参数
        hasJsr380 = isJsr380Exist(method, hasJsr380, (Class<?>) genericParameterType);

        if (!hasJsr380) {
          linterErrorMsgs.add(
              String.format(
                  "Rpc接口:【%s】的参数:【%s】由【@Valid】修饰，则内部字段需要添加相关约束注解",
                  method.getDeclaringClass().getSimpleName() + "#" + method.getName(),
                  genericParameterType.getTypeName()));
        }
      }
    }
  }

  private Boolean isJsr380Exist(Method method, Boolean hasJsr380, Class<?> actualTypeArgument) {
    Field[] actualArgumentFields = actualTypeArgument.getDeclaredFields();

    for (Field actualArgumentField : actualArgumentFields) {
      Annotation[] declaredAnnotations = actualArgumentField.getDeclaredAnnotations();
      for (Annotation declaredAnnotation : declaredAnnotations) {

        findJsrAnnotation(method, declaredAnnotation, actualArgumentField.getGenericType());

        if (isJsr380Annotation(declaredAnnotation)) {
          hasJsr380 = true;
        }
      }
    }
    return hasJsr380;
  }
}
