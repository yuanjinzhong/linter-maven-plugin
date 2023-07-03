package jsr380;

/**
 * @author: yuanjinzhong
 * @date: 2023/5/10 15:31
 * @description:
 */
public abstract class LinterResourceManager {
  private static final ThreadLocal<String> packageToScan = new ThreadLocal<>();

  private static final ThreadLocal<Boolean> isStrictMode = new ThreadLocal<>();

  public static final void setPath(String path) {
    packageToScan.set(path);
  }

  public static final String getPath() {
    String path = packageToScan.get();
    packageToScan.remove();
    return path;
  }

  public static final void setMode(Boolean Mode) {
    isStrictMode.set(Mode);
  }

  public static final Boolean getMode() {
    Boolean mode = isStrictMode.get();
    isStrictMode.remove();
    return mode;
  }
}
