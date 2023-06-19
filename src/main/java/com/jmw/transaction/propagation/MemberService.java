package com.jmw.transaction.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    @Transactional
    public void joinV1(String username) {
        Member member = new Member(username);
        Log logData = new Log(username);

        log.info("=========memberRepository start");
        memberRepository.save(member);
        log.info("=========memberRepository end");

        log.info("=========logRepository start");
        logRepository.save(logData);
        log.info("=========logRepository end");
    }

    @Transactional
    public void joinV2(String username) {
        Member member = new Member(username);
        Log logData = new Log(username);

        log.info("=========memberRepository start");
        memberRepository.save(member);
        log.info("=========memberRepository end");

        log.info("=========logRepository start");
        try {
            logRepository.save(logData);
        } catch (RuntimeException e) { // REQUIRES_NEW인 경우 돌려준 예외를 처리하고 끝!
            log.info("로그 저장에 실패했습니다 {}", logData.getMessage());
            log.info("이후 정상 흐름");
        }

        log.info("=========logRepository end");
    }
    
}
