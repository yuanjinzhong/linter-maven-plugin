package jsr380.strategy;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: yuanjinzhong
 * @date: 2023/5/9 17:01
 * @description:
 */
public enum StrategyEnum {
  PREVENT_MISS_USE("PREVENT_MISS_USE", OnlyRpcFacadeCanUseValidatedStrategyImpl.class),
  CLASS_HAS_VALIDATED(
      "CLASS_HAS_VALIDATED", MethodHasJsrThenTypeShouldHasValidatedStrategyImpl.class),
  CASCADE_FOR_VALID("CASCADE_FOR_VALID", CascadeForValidLinterStrategyImpl.class),
  METHOD_HAS_JSR("METHOD_HAS_JSR", FacadeHasValidatedThenMethodShouldHasJsrStrategyImpl.class),
  OVERRIDE_METHOD_NOT_ALTER_CONSTRAINTS(
      "OVERRIDE_METHOD_NOT_ALTER_CONSTRAINTS",
      OverrideMethodMustNotAlterParameterConstraintsStrategyImpl.class);
  private String code;
  private Class<Jsr380LinterStrategy> strategyClass;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Class getStrategyClass() {
    return strategyClass;
  }

  public void setStrategyClass(Class strategyClass) {
    this.strategyClass = strategyClass;
  }

  StrategyEnum(String code, Class strategyClass) {
    this.code = code;
    this.strategyClass = strategyClass;
  }

  public static List getStrategyList(List<String> codes) {
    return Stream.of(StrategyEnum.values())
        .filter(x -> codes.contains(x.getCode()))
        .map(StrategyEnum::getStrategyClass)
        .collect(Collectors.toList());
  }
}
