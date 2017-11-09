import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orh.service.AccountServiceTrxAnnotation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:META-INF/spring/beans-trx-annotation.xml")
public class AccountServiceTrxAnnotationTest {

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 注意此处 直接使用 Bean 就可以了，bean 在运行时成为了 动态代理的bean ,带上了事务处理
     */
    @Resource
    private AccountServiceTrxAnnotation accountServiceTrxAnnotation;

    @Before
    public void setUp() {
        // 清空账户表
        jdbcTemplate.execute("truncate table account");

        // 插入两条测试数据
        Object[] o1 = {"Bob", 100};
        Object[] o2 = {"Smith", 100};

        jdbcTemplate.batchUpdate("insert into account(name, money) value(?, ?)", Arrays.asList(o1, o2));
    }

    @Test
    public void testTransfer() {
        System.out.println("before transfer:");
        printAccountBalance();

        accountServiceTrxAnnotation.transfer("Bob", "Smith", 100d, false);

        System.out.println("after transfer:");
        printAccountBalance();
    }

    @Test(expected = RuntimeException.class)
    public void testTransferRollback() {
        System.out.println("before transfer:");
        printAccountBalance();

        try {
            // throw runtime exception
            accountServiceTrxAnnotation.transfer("Bob", "Smith", 100d, true);
        } finally {
            System.out.println("after transfer:");
            printAccountBalance();
        }
    }

    /**
     * 特殊的多线程：钱不翼而飞了，结果中还没体现异常
     * 1. 结合 Spring 的事务管理， action 中新建立的线程，如果子线程运行时出现异常，事务将得不到回滚，因为 默认的 TransactionTemplate 并没有处理 action 中子线程的问题
     * 2. Junit 主线程结束，其实service 中的子线程并没有完全执行完
     */
    @Test(expected = RuntimeException.class)
    public void testTransferMultiThreadRollback() {
        System.out.println("before transfer:");
        printAccountBalance();

        try {
            // throw runtime exception
            accountServiceTrxAnnotation.transferMultiThread("Bob", "Smith", 100d, true);
        } finally {
            System.out.println("after transfer:");
            printAccountBalance();
        }
    }

    public void printAccountBalance() {
        Double bobBalance = accountServiceTrxAnnotation.getAccountBalance("Bob");
        Double smithBalance = accountServiceTrxAnnotation.getAccountBalance("Smith");
        System.out.printf("Bob balance: %s, Smith balance: %s\n", bobBalance, smithBalance);
    }
}
