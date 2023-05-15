package jsr380.strategy;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * @author: yuanjinzhong
 * @date: 2023/5/15 16:19
 * @description: 重写方法必须不涉及参数约束
 *     <p>父接口类上有@validate，方法上有JSr380注解时，实现类重写方法时，不可以改变约束注解（可以不写注解，但是不可以改变）
 */
public class OverrideMethodMustNotAlterParameterConstraintsStrategyImpl
    extends Jsr380LinterStrategy {
  @Override
  protected void linter() throws MojoFailureException, MojoExecutionException {

    for (Map.Entry<Class, Class> entry : facadeImps.entrySet()) {
      // facade接口有Validated.class
      if (Objects.nonNull(entry.getKey().getAnnotation(Validated.class))) {
        for (Method method : entry.getKey().getDeclaredMethods()) {
          // default 不管，也管不了
          if (method.isDefault()) {
            return;
          }

          // 范型参数列表(包含普通参数)
          Type[] genericParameterTypes = method.getGenericParameterTypes();

          // 参数上的注解列表
          Annotation[][] parameterAnnotations = method.getParameterAnnotations();

          // 只要方法上有约束注解，则校验实现类不方法不能该表约束注解
          if (hasJsr380Annotation(parameterAnnotations)) {
            // 遍历参数
            for (int i = 0; i < genericParameterTypes.length; i++) {
              // 父方法第i个参数的注解
              Annotation[] parameterAnnotation = parameterAnnotations[i];

              // 获取实现类对应的方法
              Method subTypeMethod = null;
              try {
                subTypeMethod =
                    entry
                        .getValue()
                        .getDeclaredMethod(method.getName(), method.getParameterTypes());
              } catch (NoSuchMethodException e) {
                // nothing
              }
              if (subTypeMethod != null) {
                Annotation[][] subtypeParameterAnnotations =
                    subTypeMethod.getParameterAnnotations();
                // 实现类方法上第i个参数的注解
                Annotation[] subtypeParameterAnnotation = subtypeParameterAnnotations[i];

                boolean sameConstrained =
                    isEquallyParameterConstrained(parameterAnnotation, subtypeParameterAnnotation);

                if (!sameConstrained) {
                  linterErrorMsgs.add(
                      String.format(
                          "实现类重写方法时，不可以改变约束注解===》【%s】",
                          entry.getValue().getSimpleName() + "#" + method.getName()));
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * 判断各个index的注解是否一样
   *
   * @param parameterAnnotation 接口方法上的注解
   * @param subtypeParameterAnnotation 实现类上的方法注解
   * @return
   */
  private boolean isEquallyParameterConstrained(
      Annotation[] parameterAnnotation, Annotation[] subtypeParameterAnnotation) {

    boolean sameConstrained = true;

    // 只要子接口没有注解，则无脑通过
    if (subtypeParameterAnnotation.length == 0) {
      return true;
    }

    // 注解长度不一致
    if (subtypeParameterAnnotation.length != parameterAnnotation.length) {
      return false;
    }

    // 长度一致了
    int length = subtypeParameterAnnotation.length;
    for (int i = 0; i < length; i++) {
      if (!subtypeParameterAnnotation[i].toString().equals(parameterAnnotation[i].toString())) {
        sameConstrained = false;
      }
    }

    return sameConstrained;
  }

  /**
   * 方法参数上有 约束注解
   *
   * @param parameterAnnotations
   * @return
   */
  private boolean hasJsr380Annotation(Annotation[][] parameterAnnotations) {
    for (Annotation[] parameterAnnotation : parameterAnnotations) {
      for (Annotation annotation : parameterAnnotation) {
        if (annotation instanceof Valid || isJsr380Annotation(annotation)) {
          return true;
        }
      }
    }
    return false;
  }
}
