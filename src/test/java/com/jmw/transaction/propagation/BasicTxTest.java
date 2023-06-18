package com.jmw.transaction.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager platformTransactionManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 start");
        TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());

        log.info("커밋 start");
        platformTransactionManager.commit(status);
        log.info("커밋 complete");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 start");
        TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());

        log.info("롤백 start");
        platformTransactionManager.rollback(status);
        log.info("롤백 complete");
    }

    @Test
    void double_commit() {
        log.info("트랜잭션1 start");
        TransactionStatus status1 = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 start");
        platformTransactionManager.commit(status1);
        // pool에 반환된 connection0을 공유하여 같은 connection을 사용(물리 connection)
        // proxy 객체는 다름 - 물리 connection을 감싸는 객체 (proxy connection)
        log.info("트랜잭션2 start");
        TransactionStatus status2 = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋 start");
        platformTransactionManager.commit(status2);
    }

    @Test
    void double_commit_rollback() {
        log.info("트랜잭션1 start");
        TransactionStatus status1 = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 start");
        platformTransactionManager.commit(status1);

        log.info("트랜잭션2 start");
        TransactionStatus status2 = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 롤백 start");
        platformTransactionManager.rollback(status2);
    }

    @Test
    void inner_outer_commit() {
        log.info("외부 트랜잭션 start");
        TransactionStatus outer = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 start");
        TransactionStatus inner = platformTransactionManager.getTransaction(new DefaultTransactionAttribute()); // 외부 트랜잭션에 참여(다른 connection을 만들지 않음)
        log.info("outer.isNewTransaction={}", inner.isNewTransaction()); // false

        log.info("내부 커밋");
        // 물리 트랜잭션이 동작하지 않음(처음 트랜잭션을 시작한 외부 트랜잭션이 종료되어야 동작! - 물리 트랜잭션의 통제권은 먼저 시작한 트랜잭션이 소유)
        platformTransactionManager.commit(inner); // 신규 트랜잭션(외부 트랜잭션)이 아니면 실제 커밋을 호출하지 않음!

        log.info("외부 커밋");
        platformTransactionManager.commit(outer);
    }

    @Test
    void outer_rollback() {
        log.info("외부 트랜잭션 start");
        TransactionStatus outer = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 start");
        TransactionStatus inner = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 커밋");
        platformTransactionManager.commit(inner);

        log.info("외부 롤 ");
        platformTransactionManager.rollback(outer);
    }

    @Test
    void inner_rollback() {
        log.info("외부 트랜잭션 start");
        TransactionStatus outer = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 start");
        TransactionStatus inner = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 롤백");
        platformTransactionManager.rollback(inner); // 물리 트랜잭션에 rollback mark

        log.info("외부 커밋");

        Assertions.assertThatThrownBy(() -> platformTransactionManager.commit(outer)) // commit을 시도하다가 물리 트랜잭션에 rollback mark 된 걸 확인하고 rollback
                                    .isInstanceOf(UnexpectedRollbackException.class); // commit은 시도 했으므로 명령과 다른 결과가 되어 UnexpectedRollbackException이 발생
    }

    @Test
    void inner_rollback_requires_new() {
        log.info("외부 트랜잭션 start");
        TransactionStatus outer = platformTransactionManager.getTransaction(new DefaultTransactionAttribute()); // con0
        log.info("outer.isNewTransaction={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 start");
        DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 물리 connection 자체가 다름(con1)
        TransactionStatus inner = platformTransactionManager.getTransaction(definition);
        log.info("inner.isNewTransaction={}", inner.isNewTransaction()); // true


        // 별도의 connection이므로 rollback과 commit 모두 독립적으로 동작함

        log.info("내부 롤백");
        platformTransactionManager.rollback(inner);

        log.info("외부 커밋");
        platformTransactionManager.commit(outer);
    }

}