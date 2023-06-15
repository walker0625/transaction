package com.jmw.transaction.exception;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class RollbackTest {

    @Autowired
    RollbackService rollbackService;

    @Test
    void runtime() {
        Assertions.assertThatThrownBy(() -> rollbackService.runtimeException()).isInstanceOf(RuntimeException.class);
    }

    @Test
    void checked() {
        Assertions.assertThatThrownBy(() -> rollbackService.checkedException()).isInstanceOf(MyException.class);
    }

    @Test
    void rollBackFor() {
        Assertions.assertThatThrownBy(() -> rollbackService.rollbackFor()).isInstanceOf(MyException.class);
    }

    @TestConfiguration
    static class config {

        @Bean
        RollbackService rollbackService() {
            return new RollbackService();
        }

    }

    @Slf4j
    static class RollbackService {

        @Transactional
        public void runtimeException() {
            log.info("CALL RUNTIME");
            throw new RuntimeException();
        }

        // 커밋 됨
        @Transactional
        public void checkedException() throws MyException {
            log.info("CALL checkedException");
            throw new MyException();
        }

        // 롤백 처리 됨
        @Transactional(rollbackFor = MyException.class)
        public void rollbackFor() throws MyException {
            log.info("CALL rollbackFor");
            throw new MyException();
        }

    }

    static class MyException extends Exception {

    }

}
