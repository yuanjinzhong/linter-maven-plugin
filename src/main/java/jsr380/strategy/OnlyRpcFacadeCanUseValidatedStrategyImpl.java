package jsr380.strategy;

import cn.huolala.arch.hermes.api.annotation.HermesService;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author: yuanjinzhong
 * @date: 2023/5/6 14:07
 * @description: 防止滥用: @validate 只在RPC接口层使用 （只是规定，实现类放置也没问题，但是该linter无法通过）
 */
public class OnlyRpcFacadeCanUseValidatedStrategyImpl extends Jsr380LinterStrategy {

  @Override
  public void linter() throws MojoFailureException, MojoExecutionException {

    for (Map.Entry<Class, Class> entry : facadeImps.entrySet()) {

      if (Objects.nonNull(entry.getValue().getAnnotation(Validated.class))) {
        linterErrorMsgs.add(
            String.format("Rpc接口的实现类:【%s】不应该加【Validated.class】", entry.getValue().getSimpleName()));
      }
    }

    // 【Validated.class】只能加在Facade接口层
    for (Class<?> aClass : classWithValidated) {
      // 不是接口
      if (!aClass.isInterface()) {
        linterErrorMsgs.add(
            String.format("【Validated.class】只能加在接口上,当前类【%s】", aClass.getSimpleName()));
      } else {
        boolean subTypeHasHermes = false;
        // aClass是个接口
        Set<Class<?>> subTypesOf = reflections.getSubTypesOf((Class<Object>) aClass);
        for (Class<?> subClass : subTypesOf) {
          if (AnnotationUtils.findAnnotation(subClass, HermesService.class) != null) {
            subTypeHasHermes = true;
          }
        }
        // 不是facade层的接口
        if (!subTypeHasHermes) {
          linterErrorMsgs.add(
              String.format(
                  "【Validated.class】加在接口上,接口实现类需要有【HermesService.class】注解，当前接口【%s】",
                  aClass.getSimpleName()));
        }
      }
    }
  }
}
