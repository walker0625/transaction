package com.jmw.transaction.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LogRepository {

    private final EntityManager entityManager;

    // 멤버는 저장하고 로그 저장은 실패해도 됨상관 없도록 별도의 Transaction을 생성
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Log logData) {
        log.info("로그 저장");
        entityManager.persist(logData);

        if (logData.getMessage().contains("로그예외")) {
            log.info("로그 저장시 예외 발생");
            throw new RuntimeException("예외 발생"); // 롤백됨
        }
    }

    public Optional<Log> find(String message) {
        return entityManager.createQuery("SELECT l FROM Log l WHERE l.message = :message", Log.class)
                .setParameter("message", message)
                .getResultList().stream().findAny();
    }

}
