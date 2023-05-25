package jsr380.strategy;

import cn.huolala.arch.hermes.api.annotation.HermesService;
import jsr380.LinterResourceManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.reflections.Reflections;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yuanjinzhong
 * @date: 2023/5/6 13:54
 * @description:
 */
public abstract class Jsr380LinterStrategy {

  /** facade class 和impl 的 映射,key：facade, value：impl */
  protected static Map<Class, Class> facadeImps = new ConcurrentHashMap<>(256);

  /** 实现为key,接口为value */
  protected static Map<Class, Class> impFacades = new ConcurrentHashMap<>(256);

  protected static Set<Class<?>> classWithValidated;

  protected static Reflections reflections;

  private static Class<? extends Annotation> rpcAnnotationType = HermesService.class;

  private static Class<? extends Annotation> validAnnotationType = Validated.class;

  private static final List<Class> jsrClassList = new ArrayList();

  // 只会存在一个策略返回的结果
  protected static final List<String> linterErrorMsgs = new ArrayList();

  static {
    reflections = new Reflections(LinterResourceManager.getPath());
    Set<Class<?>> hermesImpls = reflections.getTypesAnnotatedWith(rpcAnnotationType);
    classWithValidated = reflections.getTypesAnnotatedWith(validAnnotationType);
    // 维护hermes实现类和接口的关系
    for (Class<?> hermesImpl : hermesImpls) {
      Assert.isTrue(!hermesImpl.isInterface(), "HermesService.class注解只能添加到类上面:" + hermesImpl);
      Assert.notNull(hermesImpl.getInterfaces(), "HermesService标注类必须实现某个Facade接口:" + hermesImpl);
      Assert.isTrue(
          !(hermesImpl.getInterfaces().length > 1), "HermesService标注类只能实现一个Facade接口:" + hermesImpl);
      facadeImps.put(hermesImpl.getInterfaces()[0], hermesImpl);
      impFacades.put(hermesImpl, hermesImpl.getInterfaces()[0]);
    }
    // 只是为了打日志
    for (Class facadeClass : facadeImps.keySet()) {
      for (Method declaredMethod : facadeClass.getDeclaredMethods()) {
        new SystemStreamLog()
            .info(
                "扫描到RPC接口:"
                    + declaredMethod.getDeclaringClass().getSimpleName()
                    + "#"
                    + declaredMethod.getName());
      }
    }

    // 获取javax/validation约束类
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    try {
      Resource[] resources =
          resolver.getResources("classpath*:javax/validation/constraints/*.class");
      for (Resource res : resources) {
        String clsName =
            new SimpleMetadataReaderFactory()
                .getMetadataReader(res)
                .getClassMetadata()
                .getClassName();
        jsrClassList.add(Class.forName(clsName));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /** 一些符合项目要求的linter */
  protected abstract void linter() throws MojoFailureException, MojoExecutionException;

  private void printLinterResult() throws MojoFailureException {
    if (!CollectionUtils.isEmpty(linterErrorMsgs)) {
      StringBuilder sb = new StringBuilder();
      sb.append(System.getProperty("line.separator"));
      // 输出错误信息
      for (String errorMsg : linterErrorMsgs) {
        sb.append(errorMsg);
        sb.append(System.getProperty("line.separator"));
      }
      throw new MojoFailureException(sb.toString());
    }
  }

  /**
   * 当前注解是不是jsr380的注解
   *
   * @param annotation
   * @return
   */
  protected boolean isJsr380Annotation(Annotation annotation) {
    for (Class aClass : jsrClassList) {
      if (annotation.annotationType().isAssignableFrom(aClass)) {
        return true;
      }
    }
    return false;
  }

  public void doLinter() throws MojoExecutionException, MojoFailureException {

    linter();

    printLinterResult();
  }
}
