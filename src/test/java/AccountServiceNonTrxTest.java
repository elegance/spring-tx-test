import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orh.service.AccountServiceNonTrx;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:META-INF/spring/beans-non-trx.xml")
public class AccountServiceNonTrxTest {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private AccountServiceNonTrx accountServiceNonTrx;

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

        accountServiceNonTrx.transfer("Bob", "Smith", 100d, false);

        System.out.println("after transfer:");
        printAccountBalance();
    }

    @Test(expected = RuntimeException.class)
    public void testTransferRollback() {
        System.out.println("before transfer:");
        printAccountBalance();

        try {
            // throw runtime exception
            accountServiceNonTrx.transfer("Bob", "Smith", 100d, true);
        } finally {
            System.out.println("after transfer:");
            printAccountBalance();
        }

    }

    public void printAccountBalance() {
        Double bobBalance = accountServiceNonTrx.getAccountBalance("Bob");
        Double smithBalance = accountServiceNonTrx.getAccountBalance("Smith");
        System.out.printf("Bob balance: %s, Smith balance: %s\n", bobBalance, smithBalance);
    }
}
