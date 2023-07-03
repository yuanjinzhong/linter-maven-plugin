import jsr380.Jsr380LinterExecutor;
import jsr380.LinterResourceManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.springframework.util.StopWatch;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: yuanjinzhong
 * @date: 2023/5/6 18:54
 * @description:
 */
@Mojo(
    name = "check",
    defaultPhase = LifecyclePhase.COMPILE,
    threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE)
public class Jsr380Mojo extends AbstractMojo {

  /** 扫描的目标目录，去检查该露目录下的jsr 380 相关注解有没有符合项目规范;默认值:cn.huolala.customer.application.command */
  @Parameter(property = "packageToScan", defaultValue = "cn.huolala.customer.application.command")
  private String packageToScan;

  /** 默认严格模式，严格模式:相关规范只能在facade接口层使用，非严格模式：可以在@Service层使用 */
  @Parameter(property = "isStrictMode", defaultValue = "true")
  private boolean isStrictMode;

  /** 当前classpath下一级元素（依赖） */
  @Parameter(defaultValue = "${project.compileClasspathElements}", readonly = true, required = true)
  private List<String> compilePath;

  /** 配置校验策略：可用值见：StrategyEnum */
  @Parameter(
      property = "strategyCodes",
      defaultValue =
          "PREVENT_MISS_USE,CLASS_HAS_VALIDATED,CASCADE_FOR_VALID,METHOD_HAS_JSR,OVERRIDE_METHOD_NOT_ALTER_CONSTRAINTS")
  private List<String> strategyCodes;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    StopWatch stopWatch = new StopWatch("reflection耗时");
    stopWatch.start("reflection-cost");
    getLog().info("linter扫描目录:" + packageToScan);

    redefineWorkClassLoader();

    LinterResourceManager.setPath(packageToScan);

    LinterResourceManager.setMode(isStrictMode);

    Jsr380LinterExecutor.doLinter(strategyCodes);

    stopWatch.stop();
    getLog().info("linter耗时:" + stopWatch.getTotalTimeSeconds() + "秒");
  }

  /**
   * maven plugin内运行时的类加载器无法加载项目文件，所以。。。。
   *
   * @see <a herf="https://maven.apache.org/guides/mini/guide-maven-classloading.html#overview"/>
   *     https://maven.apache.org/guides/mini/guide-maven-classloading.html#overview<a/>
   */
  private void redefineWorkClassLoader() {
    Set<URL> urls = new HashSet<>();
    for (String element : compilePath) {
      try {
        urls.add(new File(element).toURI().toURL());
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    ClassLoader contextClassLoader =
        URLClassLoader.newInstance(
            urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
    Thread.currentThread().setContextClassLoader(contextClassLoader);
  }
}
