package com.logistics.delivery.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.logistics.delivery.application.dto.command.ChangeDeliveryRouteStatusCommand;
import com.logistics.delivery.application.dto.result.DeliveryResults.RouteStatusChangeResult;
import com.logistics.delivery.application.service.DeliveryCommandService;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.entity.DeliveryRoute;
import com.logistics.delivery.domain.entity.DeliveryRouteStatus;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.entity.Role;
import com.logistics.delivery.domain.repository.DeliveryManagerAssignmentStateRepository;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.domain.repository.DeliveryRepository;
import com.logistics.delivery.domain.repository.DeliveryRouteRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * 배송 경로 상태 변경(changeDeliveryRouteStatus)의 락이 진짜 동시 요청에서
 * 이중 배정을 막는지 확인하는 통합 테스트.
 * DeliveryManagerAssignmentConcurrencyTest는 배정 자체(assignNextManager)의
 * 동시성을 검증하고, 이 테스트는 그 배정을 "호출하는 쪽"인 경로 상태 변경에
 * 락이 없어 같은 요청이 중복 호출될 수 있었던 문제를 검증한다.
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("local")
class DeliveryRouteStatusConcurrencyTest {

    private static final int THREAD_COUNT = 10;
    private static final UserPrincipal MASTER = new UserPrincipal(1L, Role.MASTER, null, null);

    @Autowired
    private DeliveryCommandService deliveryCommandService;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryRouteRepository deliveryRouteRepository;

    @Autowired
    private DeliveryManagerRepository deliveryManagerRepository;

    @Autowired
    private DeliveryManagerAssignmentStateRepository assignmentStateRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID testEndHubId;

    @AfterEach
    void cleanUp() {
        if (testEndHubId == null) {
            return;
        }
        jdbcTemplate.update("DELETE FROM delivery_service.p_delivery_route WHERE end_hub_id = ?", testEndHubId);
        jdbcTemplate.update("DELETE FROM delivery_service.p_delivery WHERE end_hub_id = ?", testEndHubId);
        jdbcTemplate.update(
                "DELETE FROM delivery_service.p_delivery_manager_assignment_state WHERE hub_id = ?", testEndHubId);
        jdbcTemplate.update("DELETE FROM delivery_service.p_delivery_manager WHERE hub_id = ?", testEndHubId);
    }

    @Test
    void 마지막_구간_도착이_동시에_와도_업체담당자는_한_번만_배정된다() throws InterruptedException {
        // given: 마지막(유일한) 구간이 HUB_MOVING인 배송 + 업체담당자 풀 2명
        testEndHubId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();

        Delivery delivery = deliveryRepository.save(Delivery.create(
                UUID.randomUUID(), startHubId, testEndHubId,
                "테스트 주소", "테스트 수령인", "TEST_SLACK",
                UUID.randomUUID(), UUID.randomUUID()));

        DeliveryRoute route = DeliveryRoute.create(
                delivery.getDeliveryId(), 0, startHubId, testEndHubId, 999L, BigDecimal.TEN, 30);
        route.changeStatus(DeliveryRouteStatus.HUB_MOVING);
        route = deliveryRouteRepository.save(route);

        List<Long> managerIds = registerCompanyManagers(2);
        assignmentStateRepository.save(
                DeliveryManagerAssignmentState.init(ManagerType.COMPANY_DELIVERY_MANAGER, testEndHubId));

        // when: 같은 routeId에 DEST_HUB_ARRIVED 요청을 동시에 여러 번 보냄
        UUID deliveryId = delivery.getDeliveryId();
        UUID routeId = route.getDeliveryRouteId();
        ChangeDeliveryRouteStatusCommand command =
                new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.DEST_HUB_ARRIVED, null, null);

        List<RouteStatusChangeResult> successes = new CopyOnWriteArrayList<>();
        List<CustomException> failures = new CopyOnWriteArrayList<>();
        runConcurrently(THREAD_COUNT, () -> {
            try {
                successes.add(deliveryCommandService.changeDeliveryRouteStatus(deliveryId, routeId, command, MASTER));
            } catch (CustomException e) {
                failures.add(e);
            }
        });

        // then: 정확히 한 건만 성공하고, 나머지는 이미 완료된 전이라 거부된다
        assertThat(successes).as("동시 요청 중 한 건만 성공해야 한다").hasSize(1);
        assertThat(failures).hasSize(THREAD_COUNT - 1);
        assertThat(failures).allSatisfy(e ->
                assertThat(e.getErrorCode()).isEqualTo(DeliveryErrorCode.DELIVERY_INVALID_STATUS_TRANSITION));

        // and: 업체담당자가 정확히 한 번만 배정됐다
        Delivery result = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId).orElseThrow();
        assertThat(result.getCompanyDeliveryManagerId()).isIn(managerIds);

        Integer lastAssignedSequence = jdbcTemplate.queryForObject(
                "SELECT last_assigned_sequence FROM delivery_service.p_delivery_manager_assignment_state "
                        + "WHERE manager_type = ? AND hub_id = ?",
                Integer.class, ManagerType.COMPANY_DELIVERY_MANAGER.name(), testEndHubId);
        assertThat(lastAssignedSequence)
                .as("배정이 두 번 일어났다면 커서가 두 칸 이상 전진했을 것")
                .isEqualTo(0);
    }

    private List<Long> registerCompanyManagers(int count) {
        List<Long> ids = new ArrayList<>();
        long base = System.nanoTime() % 1_000_000_000L;
        for (int i = 0; i < count; i++) {
            long id = base + i;
            deliveryManagerRepository.save(DeliveryManager.create(
                    id, testEndHubId, "TEST_SLACK_" + id, ManagerType.COMPANY_DELIVERY_MANAGER, i));
            ids.add(id);
        }
        return ids;
    }

    private void runConcurrently(int threadCount, Runnable task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
        }
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdownNow();
        assertThat(completed).as("30초 안에 안 끝남 - 데드락 의심").isTrue();
    }
}