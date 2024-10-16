package cn.jaychang.ecp.uid.baidu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UidGeneratorTypeEnum {
    DEFAULT("default"),
    CACHE("cache");

    private final String name;
}
