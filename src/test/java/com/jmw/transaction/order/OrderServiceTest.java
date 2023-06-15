package com.jmw.transaction.order;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void 정상결제() throws NotEnoughMoneyException {
        //given
        Order order = new Order();
        order.setUsername("정상");

        //when
        orderService.order(order);

        //then
        Order orderFind = orderRepository.findById(order.getId()).get();
        assertThat(orderFind.getPayStatus()).isEqualTo("완료");
    }

    @Test
    void runtime() throws NotEnoughMoneyException { // rollback
        //given
        Order order = new Order();
        order.setUsername("예외");

        //when
        assertThatThrownBy(() -> orderService.order(order)).isInstanceOf(RuntimeException.class);

        //then(rollback이 되어 order가 없음 - save 취소)
        Optional<Order> optionalOrder = orderRepository.findById(order.getId());
        assertThat(optionalOrder.isEmpty()).isTrue();
    }

    @Test
    void bizException() { // commit
        //given
        Order order = new Order();
        order.setUsername("잔고부족");

        //when
        try {
            orderService.order(order);
        } catch (NotEnoughMoneyException e) {
            // 저장된 order를 커밋하고 이후 처리를 진행
            log.info("고객에게 잔고 부족을 알리고 별도의 계좌 입금을 안내");
        }

        //then(commit이 되어 order 검색이 가능)
        Order orderFinded = orderRepository.findById(order.getId()).get();
        assertThat(orderFinded.getPayStatus()).isEqualTo("대기");
    }


}