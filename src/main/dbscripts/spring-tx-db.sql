CREATE DATABASE IF NOT EXISTS `sprint-tx-test` DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

CREATE TABLE `sprint-tx-test`.`account`(
  `name` VARCHAR(30) NOT NULL COMMENT '账户',
  `money` NUMERIC(20,4) COMMENT '余额',
  PRIMARY KEY (`name`)
);

INSERT INTO `sprint-tx-test`.`account`(`name`,`money`)
VALUES
 ('Bob', 1000)
,('Smith', 1000);