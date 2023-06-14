package com.jmw.transaction.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/*
    내부호출(this) 문제를 해결하기 위해 transaction이 적용된 부분을 별도의 class로 분리
 */
@Slf4j
@SpringBootTest
public class TxInternalCallV2Test {

    @Autowired
    CallService callService;

    @Test
    void exTest() {
        callService.external(); // transaction이 적용됨
    }

    @TestConfiguration
    static class Config {

        @Bean
        InternalService internalService() {
            return new InternalService();
        }

        @Bean
        CallService callService() {
            return new CallService(internalService());
        }

    }

    static class InternalService {

        @Transactional
        public void internal() { // public만 transaction 적용이 가능한 점 주의
            log.info("call inter");
            printTx();
        }

        private void printTx() {
            boolean txYN = TransactionSynchronizationManager.isSynchronizationActive();
            log.info("txYN {}", txYN);
        }

    }

    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        public void external() {
            log.info("call ex");
            printTx();
            internalService.internal(); // proxy.internal()을 호출해야 transaction이 적용되는데, 내부메소드를 호출하면 this.internal()이 호출됨
        }

        private void printTx() {
            boolean txYN = TransactionSynchronizationManager.isSynchronizationActive();
            log.info("txYN {}", txYN);
        }

    }

}
