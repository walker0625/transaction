package com.jmw.transaction.apply;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Call;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class TxInternalCallV1Test {

    @Autowired
    CallService callService;

    @Test
    void interTest() {
        callService.internal();
    }

    @Test
    void exTest() {
        callService.external(); // interal()의 transaction이 적용되지 않음
    }

    @TestConfiguration
    static class Config {

        @Bean
        CallService callService() {
            return new CallService();
        }
    }

    @Slf4j
    static class CallService {

        public void external() {
            log.info("call ex");
            printTx();
            internal(); // proxy.internal()을 호출해야 transaction이 적용되는데, 내부메소드를 호출하면 this.internal()이 호출됨
        }

        @Transactional
        public void internal() {
            log.info("call inter");
            printTx();
        }

        private void printTx() {
            boolean txYN = TransactionSynchronizationManager.isSynchronizationActive();
            log.info("txYN {}", txYN);
        }

    }

}
