package jsr380.strategy;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Objects;

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
  }
}
