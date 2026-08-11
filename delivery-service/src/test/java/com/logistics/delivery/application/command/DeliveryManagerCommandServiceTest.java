package com.logistics.delivery.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.logistics.delivery.application.dto.command.RegisterDeliveryManagerCommand;
import com.logistics.delivery.application.service.DeliveryManagerCommandService;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.domain.repository.DeliveryManagerAssignmentStateRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.application.port.HubPort;
import com.logistics.delivery.application.port.UserAffiliationPort;
import com.logistics.delivery.domain.entity.Role;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
import feign.FeignException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private HubPort hubPort;

    @Mock
    private UserAffiliationPort userAffiliationPort;

    @InjectMocks
    private DeliveryManagerCommandService deliveryManagerCommandService;

    private static final UserPrincipal MASTER = new UserPrincipal(1L, Role.MASTER, null, null);

    @Test
    void 이미_등록된_담당자면_예외를_던진다() {
        // given
        when(deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(1L)).thenReturn(true);
        RegisterDeliveryManagerCommand command =
                new RegisterDeliveryManagerCommand(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER);

        // when & then
        assertThatThrownBy(() -> deliveryManagerCommandService.registerDeliveryManager(command))
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
        DeliveryManager result = deliveryManagerCommandService.registerDeliveryManager(command);

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
        assertThatThrownBy(() -> deliveryManagerCommandService.registerDeliveryManager(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);
    }

    @Test
    void 업체_배송담당자인데_존재하지_않는_허브면_예외() {
        // given
        UUID hubId = UUID.randomUUID();
        when(deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(3L)).thenReturn(false);
        when(hubPort.validateHubIds(List.of(hubId))).thenReturn(Set.of());

        RegisterDeliveryManagerCommand command =
                new RegisterDeliveryManagerCommand(3L, hubId, "U03", ManagerType.COMPANY_DELIVERY_MANAGER);

        // when & then
        assertThatThrownBy(() -> deliveryManagerCommandService.registerDeliveryManager(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);
    }

    @Test
    void Hub_서비스_호출_실패시_503_예외() {
        // given
        UUID hubId = UUID.randomUUID();
        FeignException feignException = mock(FeignException.class);
        lenient().when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0]);
        when(hubPort.validateHubIds(List.of(hubId))).thenThrow(feignException);

        RegisterDeliveryManagerCommand command =
                new RegisterDeliveryManagerCommand(4L, hubId, "U04", ManagerType.COMPANY_DELIVERY_MANAGER);

        // when & then
        assertThatThrownBy(() -> deliveryManagerCommandService.registerDeliveryManager(command))
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
        DeliveryManager result = deliveryManagerCommandService.registerDeliveryManager(command);

        // then
        assertThat(result.getDeliverySequence()).isEqualTo(4);
    }

    @Test
    void 소속변경_API_호출_실패시_503_예외로_등록이_롤백된다() {
        // given
        when(deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(6L)).thenReturn(false);
        when(deliveryManagerRepository.findMaxSequence(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(Optional.empty());
        when(deliveryManagerRepository.save(any(DeliveryManager.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FeignException feignException = mock(FeignException.class);
        lenient().when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0]);
        doThrow(feignException).when(userAffiliationPort).changeAffiliation(eq(6L), any(), any());

        RegisterDeliveryManagerCommand command =
                new RegisterDeliveryManagerCommand(6L, null, "U06", ManagerType.HUB_DELIVERY_MANAGER);

        // when & then
        assertThatThrownBy(() -> deliveryManagerCommandService.registerDeliveryManager(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_EXTERNAL_SERVICE_UNAVAILABLE);
    }

    @Test
    void 존재하지_않는_담당자_수정시_예외() {
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryManagerCommandService.updateDeliveryManager(999L, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND);
    }

    @Test
    void 업체_담당자_hubId_정상_수정() {
        UUID newHubId = UUID.randomUUID();
        DeliveryManager manager = DeliveryManager.create(10L, UUID.randomUUID(), "U10", ManagerType.COMPANY_DELIVERY_MANAGER, 0);
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(manager));
        when(hubPort.validateHubIds(List.of(newHubId))).thenReturn(Set.of(newHubId));

        DeliveryManager result = deliveryManagerCommandService.updateDeliveryManager(10L, newHubId);

        assertThat(result.getHubId()).isEqualTo(newHubId);
    }

    @Test
    void 허브_담당자에게_hubId_수정_시도하면_예외() {
        DeliveryManager manager = DeliveryManager.create(11L, null, "U11", ManagerType.HUB_DELIVERY_MANAGER, 0);
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> deliveryManagerCommandService.updateDeliveryManager(11L, UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_TYPE_HUB_MISMATCH);
    }

    @Test
    void 존재하지_않는_담당자_삭제시_예외() {
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryManagerCommandService.deleteDeliveryManager(999L, MASTER))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND);
    }

    @Test
    void 정상_삭제되면_deletedAt이_채워진다() {
        DeliveryManager manager = DeliveryManager.create(20L, null, "U20", ManagerType.HUB_DELIVERY_MANAGER, 0);
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(manager));

        deliveryManagerCommandService.deleteDeliveryManager(20L, MASTER);

        assertThat(manager.getDeletedAt()).isNotNull();
        assertThat(manager.getDeletedBy()).isEqualTo(MASTER.getUserId());
    }

    @Test
    void 소속변경_API가_409면_이미_동일_소속이므로_성공_처리된다() {
        when(deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(32L)).thenReturn(false);
        when(deliveryManagerRepository.findMaxSequence(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(Optional.empty());
        when(deliveryManagerRepository.save(any(DeliveryManager.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FeignException.Conflict conflict = mock(FeignException.Conflict.class);
        lenient().when(conflict.getStackTrace()).thenReturn(new StackTraceElement[0]);
        doThrow(conflict).when(userAffiliationPort).changeAffiliation(eq(32L), any(), any());

        RegisterDeliveryManagerCommand command =
                new RegisterDeliveryManagerCommand(32L, null, "U32", ManagerType.HUB_DELIVERY_MANAGER);

        DeliveryManager result = deliveryManagerCommandService.registerDeliveryManager(command);

        assertThat(result.getDeliveryManagerId()).isEqualTo(32L);
    }

    @Test
    void 이미_삭제된_담당자_재삭제시_예외() {
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(21L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryManagerCommandService.deleteDeliveryManager(21L, MASTER))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND);
    }
}