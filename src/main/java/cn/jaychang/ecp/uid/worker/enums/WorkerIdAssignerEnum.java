package cn.jaychang.ecp.uid.worker.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * workerId 分配方式枚举
 * @author jaychang
 */
@Getter
@AllArgsConstructor
public enum WorkerIdAssignerEnum {

    ZK("zk"), DB("db"), REDIS("redis"), SIMPLE("simple");

    private final String name;
}
