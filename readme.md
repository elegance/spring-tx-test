## Spring 事务管理

#### 1. 问题起源

Q: 前些天阿里interview时，问到了如果一个service中的方法异步的去调用另一个service的方法，另一个service的方法发生异常，Spring的事务会怎么处理？

A: 由于没做过这个方面的测试也没看过这个地方的源码，我就推测了，service对外提供服务，如果内部发生异常，应该整体体现原子性，Spring应该是做的回滚....

but，我的假想是没有错，但是Spring事务并不会去管我们的异步子线程，异步的处理需要我们自己做好协作，然后做返回，如果方法返回，异步还在执行，这个异步的事情Spring是不会处理的。这个在源码的具体体现是`org.springframework.transaction.support.TransactionTemplate` 的`execute`方法: `result = action.doInTransaction(status)` 你得方法如果不管异步线程就返回了，那么这里就是无异常提交的，这并不是Spring处理不了，而是我们的自身异步协作出现的问题。

于是痛定思痛，决定把Spring事务，以及分布式事务相关再捋一遍，这里只列Spring的事务管理。

## 进入正题

Spring 事务管理有以下三个顶级接口：

* **`PlatformTransactionManager`** , Spring 为不同持久层提供了不同实现， 如`DatasourceTransactionManager` 用于`jdbc/Mybatis`
* **`TransactionDefinition`**, 定义事务隔离级别、传播、超时、只读等定义信息
* **`TransactionStatus`** ，事务具体运行状态

[Spring-事务脑图](http://naotu.baidu.com/file/154be1061d262172842e578fd533f115?token=d05a38760c1bc6c6)

#### 1. 环境

1. 建立数据库与表，将`spring-tx-db.sql` 在数据中执行
2. 修改`jdbc.properties`配置

#### 2. 概览下整个TestCase

| 说明                 | 测试入口                                     | Service方法                                | spring配置文件                               |
| ------------------ | ---------------------------------------- | ---------------------------------------- | ---------------------------------------- |
| 1. 无事务             | [AccountServiceNonTrxTest.java](src/test/java/AccountServiceNonTrxTest.java) | [AccountServiceNonTrx.java](src/main/java/org/orh/service/AccountServiceNonTrx.java) | [beans-non-trx.xml](src/main/resources/META-INF/spring/beans-non-trx.xml) |
| 2. 使用template编程式事务 | [AccountServiceTrxTemplateTest.java](src/test/java/AccountServiceTrxTemplateTest.java) | [AccountServiceTrxTemplate.java](src/main/java/org/orh/service/AccountServiceTrxTemplate.java) | [beans-trx-template.xml](src/main/resources/META-INF/spring/beans-trx-template.xml) |
| 3. 使用代理Bean        | [AccountServiceTrxProxyTest.java](src/test/java/AccountServiceTrxProxyTest.java) | [AccountServiceTrxProxy.java](src/main/java/org/orh/service/AccountServiceTrxProxy.java) | [beans-trx-proxy.xml](src/main/resources/META-INF/spring/beans-trx-proxy.xml) |
| **4. 使用AOP**       | [AccountServiceTrxAopTest.java](src/test/java/AccountServiceTrxAopTest.java) | [AccountServiceTrxAop.java](src/main/java/org/orh/service/AccountServiceTrxAop.java) | [beans-trx-aop.xml](src/main/resources/META-INF/spring/beans-trx-aop.xml) |
| **5. 使用注解**        | [AccountServiceTrxAnnotationTest.java](src/test/java/AccountServiceTrxAnnotationTest.java) | [**AccountServiceTrxAnnotation.java**](src/main/java/org/orh/service/AccountServiceTrxAnnotation.java) | [beans-trx-annotation.xml](src/main/resources/META-INF/spring/beans-trx-annotation.xml) |

#### 3. 一点说明

上面加粗的4、5是常用的方式。2、3、4中, 普通的组合dao层方法、service方法事务没有问题，如果在service中开启线程，线程出现异常，事务不会回滚，要做到回滚，需要像5中一样处理完子线程的事情再返回方法。

5中主要使用`CompletableFuture future = CompletableFuture.runAsync(...)`，然后使用`future.get()`等待子线程完成后才继续做处理。