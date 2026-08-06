package com.logistics.delivery.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.delivery.application.service.DeliveryManagerAssignmentService;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerAssignmentStateRepository;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryManagerAssignmentServiceTest {

    @Mock
    private DeliveryManagerRepository deliveryManagerRepository;

    @Mock
    private DeliveryManagerAssignmentStateRepository assignmentStateRepository;

    @InjectMocks
    private DeliveryManagerAssignmentService deliveryManagerAssignmentService;

    @Test
    void 배정_이력이_없으면_state를_새로_만들고_가장_작은_시퀀스_담당자가_배정된다() {
        // given
        DeliveryManager manager = DeliveryManager.create(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER, 0);
        when(assignmentStateRepository.findForUpdate(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(Optional.empty());
        when(deliveryManagerRepository.findNextCandidate(ManagerType.HUB_DELIVERY_MANAGER, null, -1))
                .thenReturn(Optional.of(manager));
        when(assignmentStateRepository.save(any(DeliveryManagerAssignmentState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        DeliveryManager result = deliveryManagerAssignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null);

        // then
        assertThat(result.getDeliveryManagerId()).isEqualTo(1L);
        verify(assignmentStateRepository, times(1)).save(any(DeliveryManagerAssignmentState.class));
        // 처음이라 findFirstCandidate(wrap 경로)는 호출되면 안 됨
        verify(deliveryManagerRepository, never()).findFirstCandidate(any(), any());
    }

    @Test
    void 마지막_배정_시퀀스보다_큰_담당자가_있으면_그_담당자가_배정되고_state가_그_값으로_갱신된다() {
        // given
        DeliveryManagerAssignmentState state = DeliveryManagerAssignmentState.init(ManagerType.HUB_DELIVERY_MANAGER, null);
        DeliveryManager manager = DeliveryManager.create(3L, null, "U03", ManagerType.HUB_DELIVERY_MANAGER, 2);
        when(assignmentStateRepository.findForUpdate(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(Optional.of(state));
        when(deliveryManagerRepository.findNextCandidate(ManagerType.HUB_DELIVERY_MANAGER, null, -1))
                .thenReturn(Optional.of(manager));

        // when
        DeliveryManager result = deliveryManagerAssignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null);

        // then
        assertThat(result.getDeliveryManagerId()).isEqualTo(3L);
        assertThat(state.getLastAssignedSequence()).isEqualTo(2);
        assertThat(state.getLastAssignedManagerId()).isEqualTo(3L);
        verify(assignmentStateRepository).save(state);
    }

    @Test
    void 다음_시퀀스가_없으면_findFirstCandidate로_가장_작은_시퀀스로_wrap된다() {
        // given
        DeliveryManagerAssignmentState state = DeliveryManagerAssignmentState.init(ManagerType.HUB_DELIVERY_MANAGER, null);
        DeliveryManager firstManager = DeliveryManager.create(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER, 0);
        when(assignmentStateRepository.findForUpdate(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(Optional.of(state));
        when(deliveryManagerRepository.findNextCandidate(ManagerType.HUB_DELIVERY_MANAGER, null, -1))
                .thenReturn(Optional.empty());
        when(deliveryManagerRepository.findFirstCandidate(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(Optional.of(firstManager));

        // when
        DeliveryManager result = deliveryManagerAssignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null);

        // then
        assertThat(result.getDeliveryManagerId()).isEqualTo(1L);
        verify(deliveryManagerRepository).findFirstCandidate(ManagerType.HUB_DELIVERY_MANAGER, null);
    }

    @Test
    void 배정_가능한_담당자가_없으면_예외를_던지고_state는_저장하지_않는다() {
        // given
        when(assignmentStateRepository.findForUpdate(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(Optional.empty());
        when(deliveryManagerRepository.findNextCandidate(ManagerType.HUB_DELIVERY_MANAGER, null, -1))
                .thenReturn(Optional.empty());
        when(deliveryManagerRepository.findFirstCandidate(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryManagerAssignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_UNAVAILABLE );
        verify(assignmentStateRepository, never()).save(any());
    }

    @Test
    void COMPANY_DELIVERY_MANAGER는_전달받은_hubId_그대로_조회에_사용한다() {
        // given
        UUID hubId = UUID.randomUUID();
        DeliveryManager manager = DeliveryManager.create(5L, hubId, "U05", ManagerType.COMPANY_DELIVERY_MANAGER, 0);
        when(assignmentStateRepository.findForUpdate(ManagerType.COMPANY_DELIVERY_MANAGER, hubId))
                .thenReturn(Optional.empty());
        when(deliveryManagerRepository.findNextCandidate(eq(ManagerType.COMPANY_DELIVERY_MANAGER), eq(hubId), eq(-1)))
                .thenReturn(Optional.of(manager));
        when(assignmentStateRepository.save(any(DeliveryManagerAssignmentState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        DeliveryManager result = deliveryManagerAssignmentService.assignNextManager(ManagerType.COMPANY_DELIVERY_MANAGER, hubId);

        // then
        assertThat(result.getHubId()).isEqualTo(hubId);
        verify(assignmentStateRepository).findForUpdate(ManagerType.COMPANY_DELIVERY_MANAGER, hubId);
    }
}
