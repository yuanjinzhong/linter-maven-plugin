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
 * @date: 2023/5/11 14:48
 * @description: facade接口上有@validate注解,则方法上得有@valid 或者jsr注解
 */
public class FacadeHasValidatedThenMethodShouldHasJsrStrategyImpl extends Jsr380LinterStrategy {
  @Override
  protected void linter() throws MojoFailureException, MojoExecutionException {

    Boolean PASS_THE_VERIFICATION = false;

    for (Map.Entry<Class, Class> entry : facadeImps.entrySet()) {
      // facade接口有Validated.class
      if (Objects.nonNull(entry.getKey().getAnnotation(Validated.class))) {
        for (Method method : entry.getKey().getDeclaredMethods()) {

          // 范型参数列表(包含普通参数)
          Type[] genericParameterTypes = method.getGenericParameterTypes();

          // 参数上的注解列表
          Annotation[][] parameterAnnotations = method.getParameterAnnotations();

          // 遍历参数
          for (int i = 0; i < genericParameterTypes.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
              if (annotation instanceof Valid || isJsr380Annotation(annotation)) {
                // 至少一个方法
                PASS_THE_VERIFICATION = true;
              }
            }
          }
        }
        if (!PASS_THE_VERIFICATION) {
          linterErrorMsgs.add(
              String.format(
                  "Rpc接口:【%s】包含【Validated.class】,则内部方法需要添加相关约束注解", entry.getKey().getSimpleName()));
        }
      }
    }
  }
}
