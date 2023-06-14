package com.jmw.transaction.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class TxLevelTest {

    @Autowired
    LevelService levelService;

    @Test
    void orderTest() {
        levelService.write();
        levelService.read();
    }

    @TestConfiguration
    static class TxConfig {
        @Bean
        LevelService levelService(){
            return new LevelService();
        }
    }

    @Slf4j
    @Transactional(readOnly = true)
    static class LevelService {

        @Transactional(readOnly = false)
        public void write() {
            log.info("call write");
            printTx();
        }

        public void read() {
            log.info("call read");
            printTx();
        }

        private void printTx() {
            boolean txYN = TransactionSynchronizationManager.isSynchronizationActive();
            log.info("txYN {}", txYN);

            boolean readYN = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("readYN {}", readYN);
        }

    }

}
