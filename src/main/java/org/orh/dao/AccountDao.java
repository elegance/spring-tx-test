package org.orh.dao;

import org.orh.domain.Account;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class AccountDao extends JdbcDaoSupport {


    /**
     * 转出
     *
     * @param outAccount   转出账户
     * @param money 金额
     */
    public void outMoney(String outAccount, Double money) {
        String sql = "update account set money = money - ? where name = ?";
        this.getJdbcTemplate().update(sql, money, outAccount);
    }

    /**
     * 转入
     *
     * @param inAccount   转入账户
     * @param money 金额
     */
    public void inMoney(String inAccount, Double money) {
        String sql = "update account set money = money + ? where name = ?";
        this.getJdbcTemplate().update(sql, money, inAccount);
    }

    /**
     * 获得账户余额
     * @param account
     * @return
     */
    public Double getAccountBalance(String account) {
        return this.getJdbcTemplate().queryForObject("select money from account where name = ?", Double.class, account);
    }
}
