package com.logistics.delivery.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.logistics.delivery.application.service.DeliveryManagerAssignmentService;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerAssignmentStateRepository;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * 실제 postgres에 붙어서 SELECT ... FOR UPDATE 락과 유니크 인덱스가
 * 진짜 동시 요청 상황에서 동작하는지 확인하는 통합 테스트.
 *
 * 로컬 docker-compose postgres가 떠 있어야 통과합니다.
 * ./gradlew :delivery-service:integrationTest 로 실행하세요 (기본 test 태스크엔 안 포함됨).
 *
 * 다른 실제 데이터와 안 섞이도록, 매 테스트마다 랜덤 hubId로 COMPANY_DELIVERY_MANAGER
 * 풀을 새로 만들어서 완전히 격리된 상태에서 검증합니다.
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("local")
class DeliveryManagerAssignmentConcurrencyTest {

    private static final int THREAD_COUNT = 20;

    @Autowired
    private DeliveryManagerAssignmentService assignmentService;

    @Autowired
    private DeliveryManagerRepository deliveryManagerRepository;

    @Autowired
    private DeliveryManagerAssignmentStateRepository assignmentStateRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID testHubId;

    @AfterEach
    void cleanUp() {
        if (testHubId == null) {
            return;
        }
        jdbcTemplate.update(
                "DELETE FROM delivery_service.p_delivery_manager_assignment_state WHERE hub_id = ?", testHubId);
        jdbcTemplate.update(
                "DELETE FROM delivery_service.p_delivery_manager WHERE hub_id = ?", testHubId);
    }

    @Test
    void 상태_row가_이미_있으면_동시_요청이_와도_담당자가_고르게_분배되고_유실되지_않는다() throws InterruptedException {
        // given: 담당자 5명 + 배정 상태 row를 미리 만들어둠 (등록 시점에 만드는 것과 동일한 시나리오)
        testHubId = UUID.randomUUID();
        int managerCount = 5;
        List<Long> managerIds = registerTestManagers(managerCount);
        assignmentStateRepository.save(DeliveryManagerAssignmentState.init(ManagerType.COMPANY_DELIVERY_MANAGER, testHubId));

        // when: 스레드 20개가 동시에 배정 요청
        List<Long> results = new CopyOnWriteArrayList<>();
        List<Exception> failures = new CopyOnWriteArrayList<>();
        runConcurrently(THREAD_COUNT, () -> {
            DeliveryManager assigned = assignmentService.assignNextManager(ManagerType.COMPANY_DELIVERY_MANAGER, testHubId);
            results.add(assigned.getDeliveryManagerId());
        }, failures);

        // then: 예외 없이 20건 모두 처리됐고, 특정 한 명에게 심하게 몰리지 않음 (락이 정상 작동한다는 정황 증거)
        assertThat(failures).isEmpty();
        assertThat(results).hasSize(THREAD_COUNT);
        assertThat(results).allMatch(managerIds::contains);

        Map<Long, Long> distribution = results.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
        assertThat(distribution.values()).allSatisfy(count ->
                assertThat(count).as("한 명에게 과도하게 몰리면 락이 제대로 안 걸린 것").isLessThanOrEqualTo(THREAD_COUNT / managerCount + 3));
    }

    @Test
    void 상태_row가_없는_최초_시점에_동시_요청이_와도_유니크_인덱스_덕분에_row가_중복_생성되지_않는다() throws InterruptedException {
        // given: 담당자만 만들고, 배정 상태 row는 일부러 안 만듦 (최초 배정 경쟁 상황 재현)
        testHubId = UUID.randomUUID();
        registerTestManagers(3);

        // when: 스레드 여러 개가 "이 풀에 대한 최초 배정"을 동시에 시도
        List<Long> results = new CopyOnWriteArrayList<>();
        List<Exception> failures = new CopyOnWriteArrayList<>();
        runConcurrently(THREAD_COUNT, () -> {
            DeliveryManager assigned = assignmentService.assignNextManager(ManagerType.COMPANY_DELIVERY_MANAGER, testHubId);
            results.add(assigned.getDeliveryManagerId());
        }, failures);

        // then: 일부는 유니크 인덱스 위반으로 실패할 수 있음(재시도 로직이 없으므로) — 이건 의도된 트레이드오프.
        // 핵심은 실패가 나더라도 DB엔 절대 중복 row가 남지 않아야 한다는 것.
        assertThat(results.size() + failures.size()).isEqualTo(THREAD_COUNT);

        Integer stateRowCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM delivery_service.p_delivery_manager_assignment_state "
                        + "WHERE manager_type = ? AND hub_id = ?",
                Integer.class, ManagerType.COMPANY_DELIVERY_MANAGER.name(), testHubId);
        assertThat(stateRowCount).as("유니크 인덱스가 있으면 동시에 여러 번 시도해도 row는 하나만 남아야 함").isEqualTo(1);
    }

    private List<Long> registerTestManagers(int count) {
        List<Long> ids = new java.util.ArrayList<>();
        long base = System.nanoTime() % 1_000_000_000L;
        for (int i = 0; i < count; i++) {
            long id = base + i;
            deliveryManagerRepository.save(
                    DeliveryManager.create(id, testHubId, "TEST_SLACK_" + id, ManagerType.COMPANY_DELIVERY_MANAGER, i));
            ids.add(id);
        }
        return ids;
    }

    private void runConcurrently(int threadCount, Runnable task, List<Exception> failures) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger started = new AtomicInteger();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                started.incrementAndGet();
                try {
                    task.run();
                } catch (Exception e) {
                    failures.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdownNow();
        assertThat(completed).as("30초 안에 안 끝남 - 데드락 의심").isTrue();
        assertThat(started.get()).isEqualTo(threadCount);
    }
}
