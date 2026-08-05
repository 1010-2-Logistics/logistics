package com.logistics.delivery.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.logistics.delivery.application.dto.command.RegisterDeliveryManagerCommand;
import com.logistics.delivery.application.service.DeliveryManagerCommandService;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.domain.repository.DeliveryManagerAssignmentStateRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.feign.client.HubClient;
import com.logistics.delivery.infrastructure.feign.response.HubValidationResponse;
import feign.FeignException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryManagerCommandServiceTest {

    @Mock
    private DeliveryManagerRepository deliveryManagerRepository;

    @Mock
    private DeliveryManagerAssignmentStateRepository assignmentStateRepository;

    @Mock
    private HubClient hubClient;

    @InjectMocks
    private DeliveryManagerCommandService deliveryManagerCommandService;

    @Test
    void 이미_등록된_담당자면_예외를_던진다() {
        // given
        when(deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(1L)).thenReturn(true);
        RegisterDeliveryManagerCommand command =
                new RegisterDeliveryManagerCommand(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER);

        // when & then
        assertThatThrownBy(() -> deliveryManagerCommandService.register(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_ALREADY_EXISTS);
    }

    @Test
    void 허브_배송담당자는_hubId_검증을_안한다() {
        // given
        when(deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(1L)).thenReturn(false);
        when(deliveryManagerRepository.findMaxSequence(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(Optional.empty());
        when(deliveryManagerRepository.save(any(DeliveryManager.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterDeliveryManagerCommand command =
                new RegisterDeliveryManagerCommand(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER);

        // when
        DeliveryManager result = deliveryManagerCommandService.register(command);

        // then: 첫 등록이라 순번 0, hubClient는 호출 안 됨(허브 담당자라서)
        assertThat(result.getDeliverySequence()).isEqualTo(0);
    }

    @Test
    void 업체_배송담당자인데_hubId가_없으면_예외() {
        // given
        when(deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(2L)).thenReturn(false);
        RegisterDeliveryManagerCommand command =
                new RegisterDeliveryManagerCommand(2L, null, "U02", ManagerType.COMPANY_DELIVERY_MANAGER);

        // when & then
        assertThatThrownBy(() -> deliveryManagerCommandService.register(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);
    }

    @Test
    void 업체_배송담당자인데_존재하지_않는_허브면_예외() {
        // given
        UUID hubId = UUID.randomUUID();
        when(deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(3L)).thenReturn(false);
        when(hubClient.getHub(hubId)).thenThrow(mock(FeignException.NotFound.class));

        RegisterDeliveryManagerCommand command =
                new RegisterDeliveryManagerCommand(3L, hubId, "U03", ManagerType.COMPANY_DELIVERY_MANAGER);

        // when & then
        assertThatThrownBy(() -> deliveryManagerCommandService.register(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);
    }

    @Test
    void Hub_서비스_호출_실패시_503_예외() {
        // given
        UUID hubId = UUID.randomUUID();
        when(deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(4L)).thenReturn(false);
        when(hubClient.getHub(hubId)).thenThrow(FeignException.class);

        RegisterDeliveryManagerCommand command =
                new RegisterDeliveryManagerCommand(4L, hubId, "U04", ManagerType.COMPANY_DELIVERY_MANAGER);

        // when & then
        assertThatThrownBy(() -> deliveryManagerCommandService.register(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_EXTERNAL_SERVICE_UNAVAILABLE);
    }

    @Test
    void 기존_순번이_있으면_마지막_순번_다음으로_배정된다() {
        // given
        when(deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(5L)).thenReturn(false);
        when(deliveryManagerRepository.findMaxSequence(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(Optional.of(3));
        when(deliveryManagerRepository.save(any(DeliveryManager.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterDeliveryManagerCommand command =
                new RegisterDeliveryManagerCommand(5L, null, "U05", ManagerType.HUB_DELIVERY_MANAGER);

        // when
        DeliveryManager result = deliveryManagerCommandService.register(command);

        // then
        assertThat(result.getDeliverySequence()).isEqualTo(4);
    }

    @Test
    void 존재하지_않는_담당자_수정시_예외() {
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryManagerCommandService.update(999L, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND);
    }

    @Test
    void 업체_담당자_hubId_정상_수정() {
        UUID newHubId = UUID.randomUUID();
        DeliveryManager manager = DeliveryManager.create(10L, UUID.randomUUID(), "U10", ManagerType.COMPANY_DELIVERY_MANAGER, 0);
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(manager));

        DeliveryManager result = deliveryManagerCommandService.update(10L, newHubId);

        assertThat(result.getHubId()).isEqualTo(newHubId);
    }

    @Test
    void 허브_담당자에게_hubId_수정_시도하면_예외() {
        DeliveryManager manager = DeliveryManager.create(11L, null, "U11", ManagerType.HUB_DELIVERY_MANAGER, 0);
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> deliveryManagerCommandService.update(11L, UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_TYPE_HUB_MISMATCH);
    }

    @Test
    void 존재하지_않는_담당자_삭제시_예외() {
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryManagerCommandService.delete(999L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND);
    }

    @Test
    void 정상_삭제되면_deletedAt이_채워진다() {
        DeliveryManager manager = DeliveryManager.create(20L, null, "U20", ManagerType.HUB_DELIVERY_MANAGER, 0);
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(manager));

        deliveryManagerCommandService.delete(20L);

        assertThat(manager.getDeletedAt()).isNotNull();
    }

    @Test
    void 이미_삭제된_담당자_재삭제시_예외() {
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(21L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryManagerCommandService.delete(21L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND);
    }
}