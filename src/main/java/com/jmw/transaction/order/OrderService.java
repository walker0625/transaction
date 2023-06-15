package com.jmw.transaction.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("save order");

        orderRepository.save(order);

        log.info("charge start");

        if (order.getUsername().equals("예외")) {
            log.info("시스템예외");
            throw new RuntimeException("시스템 예외 발생");
        } else if (order.getUsername().equals("잔고부족")) {
            log.info("잔고부족");
            order.setPayStatus("대기");
            throw new NotEnoughMoneyException("잔고가 부족합니다");
        } else {
            log.info("정상 승인");
            order.setPayStatus("완료");
        }

        log.info("charge end");
    }

}