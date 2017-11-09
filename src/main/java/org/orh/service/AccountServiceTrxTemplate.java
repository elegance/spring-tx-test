package org.orh.service;

import org.orh.dao.AccountDao;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

public class AccountServiceTrxTemplate {

    private AccountDao accountDao;

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    private TransactionTemplate transactionTemplate;

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public void transfer(final String out, final String in, final Double money, final boolean rollback) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                accountDao.outMoney(out, money);

                if (rollback) {
                    // 模拟 系统中途出现错误
                    throw new RuntimeException("出现错误，需要回滚事务.");
                }

                accountDao.inMoney(in, money);
            }
        });
    }

    /**
     * 测试子线程 如果发生异常是否可以回滚
     */
    public void transferMultiThread(final String out, final String in, final Double money, final boolean rollback) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
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
        });
    }


    public Double getAccountBalance(String account) {
        return accountDao.getAccountBalance(account);
    }
}
