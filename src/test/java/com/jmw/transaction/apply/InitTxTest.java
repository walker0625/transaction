package com.jmw.transaction.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;

@SpringBootTest
public class InitTxTest {

    @Autowired
    private Hello hello;

    @Test
    void postInitTest() {
        // spring boot 초기화 실행결과
        // initV1 > Hello init @PostConstruct tx active false > @PostConstruct가 먼저 동작하고 @Transactional이 동작하여 적용 x
        // initV2 > Hello init @EventListener tx active true > ApplicationReadyEvent(Container 세팅완료 이후) @Transactional이 적용 o

        // hello.initV1(); 직접 호출하면 transaction 적용 가능
    }

    @TestConfiguration
    static class Config {
        @Bean
        Hello hello() {
            return new Hello();
        }
    }

    @Slf4j
    static class Hello {

        // @PostConstruct이 먼저 실행되기 때문에 @Transactional이 적용되지 못함
        @PostConstruct
        @Transactional
        public void initV1() {
            boolean active = TransactionSynchronizationManager.isSynchronizationActive();
            log.info("Hello init @PostConstruct tx active {}", active);
        }

        // 모든 등록(+proxy 생성) 이후이므로 @Transactional이 적용됨
        @EventListener(ApplicationReadyEvent.class)
        @Transactional
        public void initV2() {
            boolean active = TransactionSynchronizationManager.isSynchronizationActive();
            log.info("Hello init @EventListener tx active {}", active);
        }

    }
}
