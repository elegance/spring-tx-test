package org.orh.service;

import org.orh.dao.AccountDao;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 危险 - 无事务管理器，直接在service方法中访问多个dao方法
 */
public class AccountServiceNonTrx {

    private AccountDao accountDao;


    /**
     * 转账
     */
    public void transfer(String out, String in, Double money, boolean rollback) {
        accountDao.outMoney(out, money);

        if (rollback) {
            throw new RuntimeException("出现异常，需要回滚事务");
        }

        accountDao.inMoney(in, money);
    }

    /**
     * 获取账户余额
     */
    public Double getAccountBalance(String account) {
        return accountDao.getAccountBalance(account);
    }


    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }
}
