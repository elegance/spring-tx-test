package org.orh.service;

import org.orh.dao.AccountDao;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.*;

/**
 * 利用 代理类 做声明式事务
 */
public class AccountServiceTrxAnnotation {

    private AccountDao accountDao;

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Transactional(propagation = Propagation.REQUIRED)
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
    @Transactional(propagation = Propagation.REQUIRED)
    public void transferMultiThread(String out, String in, Double money, boolean rollback) {
        accountDao.outMoney(out, money);

        CompletableFuture c = CompletableFuture.runAsync(() -> {
            if (rollback) {
                // 模拟 系统中途出现错误
                throw new RuntimeException("出现错误，需要回滚事务.");
            }
            accountDao.inMoney(in, money);
        });
        try {
            c.get();
        } catch (Exception e) {
            // 外部必须抛出异常才能回滚事务
            throw new RuntimeException("执行远程调用异常，事务回滚");
        }
    }


    public Double getAccountBalance(String account) {
        return accountDao.getAccountBalance(account);
    }
}
