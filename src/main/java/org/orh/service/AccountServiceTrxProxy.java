package org.orh.service;

import org.orh.dao.AccountDao;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 利用 代理类 做声明式事务
 */
public class AccountServiceTrxProxy {

    private AccountDao accountDao;

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public void transfer(String out, String in, Double money, boolean rollback) {
        accountDao.outMoney(out, money);

        if (rollback) {
            // 模拟 系统中途出现错误
            throw new RuntimeException("出现错误，需要回滚事务.");
        }

        accountDao.inMoney(in, money);
    }

    /**
     * 测试子线程 如果发生异常是否可以回滚
     */

    public void transferMultiThread(String out, String in, Double money, boolean rollback) {
        accountDao.outMoney(out, money);

        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            if (rollback) {
                // 模拟 系统中途出现错误
                throw new RuntimeException("出现错误，需要回滚事务.");
            }

            accountDao.inMoney(in, money);
        }).start();
    }


    public Double getAccountBalance(String account) {
        return accountDao.getAccountBalance(account);
    }
}
