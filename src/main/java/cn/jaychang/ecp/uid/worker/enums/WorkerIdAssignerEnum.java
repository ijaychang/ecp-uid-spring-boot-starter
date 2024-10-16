package cn.jaychang.ecp.uid.worker.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WorkerIdAssignerEnum {

    ZK("zk"), DB("db"), REDIS("redis"), SIMPLE("simple");

    private final String name;
}
