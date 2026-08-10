package com.logistics.delivery.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.logistics.delivery.application.service.DeliveryManagerAssignmentService;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerAssignmentStateRepository;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Mock 대신 인메모리 Fake 레포지토리를 써서, 여러 번 연속 호출했을 때
 * 라운드로빈 알고리즘 자체가 실제로 사이클을 도는지 검증하는 시나리오 테스트.
 * (실제 DB 락 동작까지 검증하진 않음 — 그건 별도 통합 테스트가 필요함)
 */
class DeliveryManagerAssignmentServiceScenarioTest {

    private FakeDeliveryManagerRepository managerRepository;
    private FakeDeliveryManagerAssignmentStateRepository stateRepository;
    private DeliveryManagerAssignmentService assignmentService;

    @BeforeEach
    void setUp() {
        managerRepository = new FakeDeliveryManagerRepository();
        stateRepository = new FakeDeliveryManagerAssignmentStateRepository();
        assignmentService = new DeliveryManagerAssignmentService(managerRepository, stateRepository);
        stateRepository.save(DeliveryManagerAssignmentState.init(ManagerType.HUB_DELIVERY_MANAGER, null));
    }

    @Test
    void 담당자_세_명에게_연속_배정하면_순번대로_돌고_한바퀴_돌면_처음으로_wrap된다() {
        // given
        managerRepository.add(DeliveryManager.create(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER, 0));
        managerRepository.add(DeliveryManager.create(2L, null, "U02", ManagerType.HUB_DELIVERY_MANAGER, 1));
        managerRepository.add(DeliveryManager.create(3L, null, "U03", ManagerType.HUB_DELIVERY_MANAGER, 2));

        // when & then
        assertThat(assign().getDeliveryManagerId()).isEqualTo(1L);
        assertThat(assign().getDeliveryManagerId()).isEqualTo(2L);
        assertThat(assign().getDeliveryManagerId()).isEqualTo(3L);
        assertThat(assign().getDeliveryManagerId()).isEqualTo(1L); // 한 바퀴 돌아서 다시 처음
        assertThat(assign().getDeliveryManagerId()).isEqualTo(2L);
    }

    @Test
    void 중간_담당자가_삭제되어도_남은_담당자끼리_순번_재정렬_없이_라운드로빈된다() {
        // given: 발제 요구사항 - 삭제돼도 다른 담당자 시퀀스는 안 바뀜
        DeliveryManager manager1 = DeliveryManager.create(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER, 0);
        DeliveryManager manager2 = DeliveryManager.create(2L, null, "U02", ManagerType.HUB_DELIVERY_MANAGER, 1);
        DeliveryManager manager3 = DeliveryManager.create(3L, null, "U03", ManagerType.HUB_DELIVERY_MANAGER, 2);
        managerRepository.add(manager1);
        managerRepository.add(manager2);
        managerRepository.add(manager3);

        assertThat(assign().getDeliveryManagerId()).isEqualTo(1L); // seq 0 배정, last=0

        // 가운데(seq 1) 담당자 삭제
        manager2.markDeleted(1L);

        // when & then: seq 1은 삭제됐으니 건너뛰고 seq 2로, 그다음은 wrap해서 다시 seq 0
        assertThat(assign().getDeliveryManagerId()).isEqualTo(3L);
        assertThat(assign().getDeliveryManagerId()).isEqualTo(1L);
        assertThat(assign().getDeliveryManagerId()).isEqualTo(3L);
    }

    @Test
    void 담당자가_한_명뿐이면_계속_같은_담당자에게_반복_배정된다() {
        // given
        managerRepository.add(DeliveryManager.create(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER, 0));

        // when & then
        assertThat(assign().getDeliveryManagerId()).isEqualTo(1L);
        assertThat(assign().getDeliveryManagerId()).isEqualTo(1L);
        assertThat(assign().getDeliveryManagerId()).isEqualTo(1L);
    }

    @Test
    void managerType이_같아도_hubId가_다르면_서로_다른_풀로_독립적으로_배정된다() {
        // given
        UUID hubA = UUID.randomUUID();
        UUID hubB = UUID.randomUUID();

        stateRepository.save(DeliveryManagerAssignmentState.init(ManagerType.COMPANY_DELIVERY_MANAGER, hubA));
        stateRepository.save(DeliveryManagerAssignmentState.init(ManagerType.COMPANY_DELIVERY_MANAGER, hubB));

        managerRepository.add(DeliveryManager.create(10L, hubA, "A01", ManagerType.COMPANY_DELIVERY_MANAGER, 0));
        managerRepository.add(DeliveryManager.create(11L, hubA, "A02", ManagerType.COMPANY_DELIVERY_MANAGER, 1));
        managerRepository.add(DeliveryManager.create(20L, hubB, "B01", ManagerType.COMPANY_DELIVERY_MANAGER, 0));

        // when & then: hubB 배정이 hubA 순번에 영향을 주면 안 됨
        assertThat(assignmentService.assignNextManager(ManagerType.COMPANY_DELIVERY_MANAGER, hubA).getDeliveryManagerId())
                .isEqualTo(10L);
        assertThat(assignmentService.assignNextManager(ManagerType.COMPANY_DELIVERY_MANAGER, hubB).getDeliveryManagerId())
                .isEqualTo(20L);
        assertThat(assignmentService.assignNextManager(ManagerType.COMPANY_DELIVERY_MANAGER, hubA).getDeliveryManagerId())
                .isEqualTo(11L);
        assertThat(assignmentService.assignNextManager(ManagerType.COMPANY_DELIVERY_MANAGER, hubB).getDeliveryManagerId())
                .isEqualTo(20L); // hubB엔 담당자 한 명뿐이라 계속 자기 자신으로 wrap
    }

    @Test
    void 신규_담당자가_등록된_후에도_사이클에_자연스럽게_포함된다() {
        // given: seq 0, 1 두 명으로 시작
        managerRepository.add(DeliveryManager.create(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER, 0));
        managerRepository.add(DeliveryManager.create(2L, null, "U02", ManagerType.HUB_DELIVERY_MANAGER, 1));
        assertThat(assign().getDeliveryManagerId()).isEqualTo(1L);
        assertThat(assign().getDeliveryManagerId()).isEqualTo(2L); // last=1

        // when: seq 2로 신규 담당자 등록
        managerRepository.add(DeliveryManager.create(3L, null, "U03", ManagerType.HUB_DELIVERY_MANAGER, 2));

        // then: 다음 배정은 자연스럽게 새 담당자로
        assertThat(assign().getDeliveryManagerId()).isEqualTo(3L);
        assertThat(assign().getDeliveryManagerId()).isEqualTo(1L); // 다시 wrap
    }

    private DeliveryManager assign() {
        return assignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null);
    }

    /**
     * 테스트 전용 인메모리 Fake. 실제 SELECT ... FOR UPDATE 락 동작까지는 흉내내지 않음
     * (그건 실제 DB가 있어야 검증 가능한 영역).
     */
    private static class FakeDeliveryManagerRepository implements DeliveryManagerRepository {
        private final List<DeliveryManager> managers = new ArrayList<>();

        void add(DeliveryManager manager) {
            managers.add(manager);
        }

        @Override
        public DeliveryManager save(DeliveryManager manager) {
            managers.add(manager);
            return manager;
        }

        @Override
        public boolean existsByDeliveryManagerIdAndDeletedAtIsNull(Long deliveryManagerId) {
            return managers.stream()
                    .anyMatch(m -> m.getDeliveryManagerId().equals(deliveryManagerId) && m.getDeletedAt() == null);
        }

        @Override
        public Optional<Integer> findMaxSequence(ManagerType managerType, UUID hubId) {
            return managers.stream()
                    .filter(m -> m.getDeletedAt() == null)
                    .filter(m -> m.getManagerType() == managerType)
                    .filter(m -> Objects.equals(m.getHubId(), hubId))
                    .map(DeliveryManager::getDeliverySequence)
                    .max(Integer::compareTo);
        }

        @Override
        public Optional<DeliveryManager> findByIdAndDeletedAtIsNull(Long deliveryManagerId) {
            return managers.stream()
                    .filter(m -> m.getDeliveryManagerId().equals(deliveryManagerId) && m.getDeletedAt() == null)
                    .findFirst();
        }

        @Override
        public Optional<DeliveryManager> findNextCandidate(ManagerType managerType, UUID hubId, int afterSequence) {
            return managers.stream()
                    .filter(m -> m.getDeletedAt() == null)
                    .filter(m -> m.getManagerType() == managerType)
                    .filter(m -> Objects.equals(m.getHubId(), hubId))
                    .filter(m -> m.getDeliverySequence() > afterSequence)
                    .min(Comparator.comparingInt(DeliveryManager::getDeliverySequence));
        }

        @Override
        public Optional<DeliveryManager> findFirstCandidate(ManagerType managerType, UUID hubId) {
            return managers.stream()
                    .filter(m -> m.getDeletedAt() == null)
                    .filter(m -> m.getManagerType() == managerType)
                    .filter(m -> Objects.equals(m.getHubId(), hubId))
                    .min(Comparator.comparingInt(DeliveryManager::getDeliverySequence));
        }

        @Override
        public Page<DeliveryManager> search(ManagerType managerType, UUID hubId, Pageable pageable) {
            throw new UnsupportedOperationException("이 시나리오 테스트에선 사용하지 않음");
        }
    }

    private static class FakeDeliveryManagerAssignmentStateRepository implements DeliveryManagerAssignmentStateRepository {
        private final Map<String, DeliveryManagerAssignmentState> store = new HashMap<>();

        private String key(ManagerType managerType, UUID hubId) {
            return managerType + "|" + hubId;
        }

        @Override
        public Optional<DeliveryManagerAssignmentState> findForUpdate(ManagerType managerType, UUID hubId) {
            return Optional.ofNullable(store.get(key(managerType, hubId)));
        }

        @Override
        public DeliveryManagerAssignmentState save(DeliveryManagerAssignmentState state) {
            store.put(key(state.getManagerType(), state.getHubId()), state);
            return state;
        }
    }
}
