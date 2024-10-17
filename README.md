# ecp-uid-spring-boot-starter
基于美团leaf、百度UidGenerator、原生snowflake 进行整合的 唯一ID生成器，本项目在ecp-uid基础上，再整合成spring boot starter,
方便spring boot类型项目引入ecp-uid

一、介绍
-------------------
   1、本项目为uid生成器，支持segment、snowflake、UidGenerator、spring四种策略生成id
   
   2、本项目可生成混淆id，目前混淆策略为：gene(基因法)

   3、原项目地址：
      github ： https://github.com/linhuaichuan/ecp-uid
      码云： https://gitee.com/zmds/ecp-uid
   
二、策略说明
-------------------
   1、snowflake
     snowflake 是基于Twitter [snowflake](https://github.com/twitter/snowflake) 算法的优化策略
     本策略优化了闰秒回拨处理、新增默认workId 与 datacenterId 的提供方法。
     <bean id="snowflakeUidStrategy" class="**.TwitterSnowflakeStrategy"/> 
     
   2、baidu
      是 基于[百度UidGenerator](https://github.com/baidu/uid-generator)上的的优化策略。
     	<bean id="baiduUidStrategy" class="**.BaiduUidStrategy"/> 
     		 
     (1)、workerId提供策略
         * DisposableWorkerIdAssigner，利用数据库来管理生成workId，依赖数据库和spring-jdbc框架(需有jdbcTemplate的bean)。mysql表示例：
        DROP TABLE IF EXISTS worker_node;
        CREATE TABLE worker_node (
        id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'auto increment id',
        host_name VARCHAR (64) NOT NULL COMMENT 'host name',
        `port` VARCHAR (64) NOT NULL COMMENT 'port',
        `type` INT NOT NULL COMMENT 'node type: ACTUAL or CONTAINER',
        `launch_date` DATE NOT NULL COMMENT 'launch date',
        `update_time` DATETIME NOT NULL COMMENT 'update time',
        `create_time` DATETIME NOT NULL COMMENT 'create time',
        PRIMARY KEY (ID)
        ) COMMENT = 'DB WorkerID Assigner for UID Generator',
        ENGINE = INNODB;      

		 
		示例：
		<bean id="disposableWorker" class="**.DisposableWorkerIdAssigner"/>
		<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">...</bean>
			
         * SimpleWorkerIdAssigner ，固定了workId的提供。值为0.示例：
		<bean id="simpleWorker" class="**.SimpleWorkerIdAssigner"/>
         
         * ZkWorkerIdAssigner ，利用zookeeper来实现wordId的提供管理，依赖原生Zookeeper驱动.示例：
		<bean id="zkWorker" class="**.ZkWorkerIdAssigner"/>
		可设置interval-心跳间隔、pidHome-workerId文件存储目录、zkAddress-zk地址、pidPort-心跳端口
         
         * RedisWorkIdAssigner ，利用redis来实现wordId的提供管理，依赖了spring-data-redis框架的RedisTemplate.示例：
		<bean id="redisWorker" class="**.RedisWorkIdAssigner"/>
		可设置interval-心跳间隔、pidHome-workerId文件存储目录、pidPort-心跳端口

     (2)、uid生成策略
         * DefaultUidGenerator 是Snowflake算法的变种，取消datacenterId, 并扩展了支持自定义workerId位数和初始化策略。
           a、可配置 delta seconds (28 bits)  
	          当前时间，相对于时间基点"2016-05-20"的增量值，单位：秒，最多可支持约8.7年

           b、worker id (22 bits)  
                  机器id，最多可支持约420w次机器启动。内置实现为在启动时由数据库分配，默认分配策略为用后即弃，后续可提供复用策略。

           c、sequence (13 bits)   
                  每秒下的并发序列，13 bits可支持每秒8192个并发。
                                   
           注： 三者之和为63
                            
           示例：
            <bean id="defaultUidGenerator" class="DefaultUidGenerator" scope="prototype">
               <property name="workerIdAssigner" ref="disposableWorkerIdAssigner"/>
               <property name="timeBits" value="29"/>
               <property name="workerBits" value="21"/>
               <property name="seqBits" value="13"/>
               <property name="epochStr" value="2017-12-25"/>
            </bean>
            
         * CachedUidGenerator借用未来时间来解决sequence天然存在的并发限制; 采用RingBuffer来缓存已生成的UID, 并行化UID的生产和消费,
	    同时对CacheLine补齐，避免了由RingBuffer带来的硬件级「伪共享」问题. 最终单机QPS可达600万

	    示例：
            <bean id="cachedUidGenerator" class="CachedUidGenerator" scope="prototype">
               <property name="workerIdAssigner" ref="disposableWorkerIdAssigner" />

               <!-- 以下为可选配置, 如未指定将采用默认值 -->
               <!-- RingBuffer size扩容参数, 可提高UID生成的吞吐量. --> 
               <!-- 默认:3， 原bufferSize=8192, 扩容后bufferSize= 8192 << 3 = 65536 -->
               <!--<property name="boostPower" value="3"></property>--> 
               
               <!-- 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50 -->
               <!-- 举例: bufferSize=1024, paddingFactor=50 -> threshold=1024 * 50 / 100 = 512. -->
               <!-- 当环上可用UID数量 < 512时, 将自动对RingBuffer进行填充补全 -->
               <!--<property name="paddingFactor" value="50"></property>--> 
               
               <!-- 另外一种RingBuffer填充时机, 在Schedule线程中, 周期性检查填充 -->
               <!-- 默认:不配置此项, 即不实用Schedule线程. 如需使用, 请指定Schedule线程时间间隔, 单位:秒 -->
               <!--<property name="scheduleInterval" value="60"></property>--> 
               
               <!-- 拒绝策略: 当环已满, 无法继续填充时 -->
               <!-- 默认无需指定, 将丢弃Put操作, 仅日志记录. 如有特殊需求, 请实现RejectedPutBufferHandler接口(支持Lambda表达式) -->
               <!--<property name="rejectedPutBufferHandler" ref="XxxxYourPutRejectPolicy"></property>--> 
               
               <!-- 拒绝策略: 当环已空, 无法继续获取时 -->
               <!-- 默认无需指定, 将记录日志, 并抛出UidGenerateException异常. 如有特殊需求, 请实现RejectedTakeBufferHandler接口 -->
               <!--<property name="rejectedPutBufferHandler" ref="XxxxYourPutRejectPolicy"></property>--> 
            </bean>
            
     (3)、比特分配的建议
         *对于并发数要求不高、期望长期使用的应用, 可增加```timeBits```位数, 减少```seqBits```位数. 
	   例如节点采取用完即弃的WorkerIdAssigner策略, 重启频率为12次/天,
	   那么配置成```{"workerBits":23,"timeBits":31,"seqBits":9}```时, 可支持28个节点以整体并发量14400 UID/s的速度持续运行68年.

         *对于节点重启频率频繁、期望长期使用的应用, 可增加```workerBits```和```timeBits```位数, 减少```seqBits```位数.
	   例如节点采取用完即弃的WorkerIdAssigner策略, 重启频率为24*12次/天,
	   那么配置成```{"workerBits":27,"timeBits":30,"seqBits":6}```时, 可支持37个节点以整体并发量2400 UID/s的速度持续运行34年.
                           
   3、segment
     是 基于美团[leaf-segment](https://tech.meituan.com/MT_Leaf.html) 的优化策略, 使用双Buffer实现。依赖数据库与spring-jdbc框架
     <bean id="leafUidStrategy" class="**.LeafSegmentStrategy"/> 
     
     (1)、SegmentServiceImpl 是具体实现类，数据库表结构为(mysql示例)：
	  DROP TABLE IF EXISTS id_segment;
	  CREATE TABLE id_segment (
		BIZ_TAG VARCHAR(64) NOT NULL COMMENT '业务标识',
		STEP int NOT NULL COMMENT '步长',
		MAX_ID BIGINT NOT NULL COMMENT '最大值',
		LAST_UPDATE_TIME TIMESTAMP NOT NULL COMMENT '上次修改时间',
		CURRENT_UPDATE_TIME TIMESTAMP NOT NULL COMMENT '当前修改时间',
		PRIMARY KEY(BIZ_TAG)
	  ) COMMENT='号段存储表',ENGINE = INNODB;
     
     (2)、支持 同步/异步两种更新数据库方式。可选配置asynLoadingSegment(true-异步，false-同步)，默认使用异步。
          示例：
          <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">...</bean>
                    
   4、spring 增量ID
      是 基于 segment策略提供给spring 增量实现。非直接使用的策略
   
   5、混淆算法
      是 基于 基因分库法这个理论扩展出来的混淆算法
      
三 、快速开始
-------------------

pom引入ecp-uid-spring-boot-starter
```xml
<dependency>
    <groupId>cn.jaychang</groupId>
    <artifactId>ecp-uid-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

* 使用Baidu Uid-Generator 生成策略，workId分配方式使用 zookeeper 
pom还需要引入 zkclient
```xml
<dependency>
    <groupId>com.101tec</groupId>
    <artifactId>zkclient</artifactId>
    <version>0.11</version>
</dependency>
```
application.yml
```yaml
ecp:
  uid:
    strategy: baidu
    baidu-uid:
      workerIdAssigner: zk
      zkAddr: localhost:2181
```

* 使用Baidu Uid-Generator 生成策略，workId分配方式使用 redis

pom还需要引入 spring-boot-starter-data-redis 
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <!-- Import dependency management from Spring Boot -->
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    其他省略...
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>io.lettuce</groupId>
        <artifactId>lettuce-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-pool2</artifactId>
    </dependency>
</dependencies>
```
application.yml
```yaml
ecp:
  uid:
    strategy: baidu-uid
    baidu-uid:
      workerIdAssigner: redis

spring:
  redis:
    host: localhost
    port: 6379

```

* 使用Baidu Uid-Generator 生成策略，workId分配方式使用 db
  
创建数据库表

```sql
CREATE TABLE `worker_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'auto increment id',
  `host_name` varchar(64) NOT NULL COMMENT 'host name',
  `port` varchar(64) NOT NULL COMMENT 'port',
  `type` int(11) NOT NULL COMMENT 'node type: [2=ACTUAL or 1=CONTAINER]',
  `launch_date` date NOT NULL COMMENT 'launch date',
  `update_time` datetime NOT NULL COMMENT 'update time',
  `create_time` datetime NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DB WorkerID Assigner for UID Generator';
```

pom还需要引入 spring-boot-starter-jdbc


```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```



application.yml

```yaml
ecp:
  uid:
    strategy: baidu-uid
    baidu-uid:
      workerIdAssigner: db

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&connectTimeout=60000&socketTimeout=60000&autoReconnect=true&failOverReadOnly=false&useSSL=true&useUnicode=true
    hikari:
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: root
      password: 123456
```

