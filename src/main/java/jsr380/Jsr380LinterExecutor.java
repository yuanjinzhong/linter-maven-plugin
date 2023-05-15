package jsr380;

import jsr380.strategy.Jsr380LinterStrategy;
import jsr380.strategy.StrategyEnum;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.List;

/**
 * @author: yuanjinzhong
 * @date: 2023/5/9 17:29
 * @description:
 */
public class Jsr380LinterExecutor {

  /** @param codes 策略枚举 */
  public static void doLinter(List<String> codes)
      throws MojoFailureException, MojoExecutionException {
    List<Class<Jsr380LinterStrategy>> strategyList = StrategyEnum.getStrategyList(codes);

    for (Class<Jsr380LinterStrategy> clazz : strategyList) {
      Jsr380LinterStrategy strategy;
      try {
        strategy = clazz.newInstance();
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      strategy.doLinter();
    }
  }
}
