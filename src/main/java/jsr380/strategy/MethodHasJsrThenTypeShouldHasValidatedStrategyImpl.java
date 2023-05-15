package jsr380.strategy;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author: yuanjinzhong
 * @date: 2023/5/9 17:08
 * @description: 若方法上参数上有加JSR的约束注解的话,就检查当前类和父接口上有没有@validate注解
 */
public class MethodHasJsrThenTypeShouldHasValidatedStrategyImpl extends Jsr380LinterStrategy {
  @Override
  public void linter() throws MojoFailureException, MojoExecutionException {

    for (Class facadeClass : facadeImps.keySet()) {

      for (Method method : facadeClass.getDeclaredMethods()) {

        // 范型参数列表(包含普通参数)
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        // 参数上的注解列表
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        // 遍历参数
        for (int i = 0; i < genericParameterTypes.length; i++) {
          for (Annotation annotation : parameterAnnotations[i]) {

            if (annotation instanceof Valid || isJsr380Annotation(annotation)) {
              if (Objects.isNull(facadeClass.getAnnotation(Validated.class))
                  || Objects.nonNull(facadeImps.get(facadeClass).getAnnotation(Validated.class))) {

                linterErrorMsgs.add(
                    String.format(
                        "方法【%s】上含有jsr约束注解，则%s需要添加【Validated.class】注解或者%s不应该加【Validated.class】",
                        method.getName(),
                        facadeClass.getSimpleName(),
                        facadeImps.get(facadeClass).getSimpleName()));
              }
            }
          }
        }
      }
    }
  }
}
