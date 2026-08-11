package com.logistics.hubRoute.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.hubRoute.application.dto.command.HubRouteCreateCommand;
import com.logistics.hubRoute.application.port.EventPublisher;
import com.logistics.hubRoute.application.port.HubPort;
import com.logistics.hubRoute.application.service.HubRouteCommandService;
import com.logistics.hubRoute.domain.entity.HubRoute;
import com.logistics.hubRoute.domain.entity.Role;
import com.logistics.hubRoute.domain.repository.HubRouteCommandRepository;
import com.logistics.hubRoute.global.exception.CustomException;
import com.logistics.hubRoute.infrastructure.security.principal.UserPrincipal;
import com.logistics.hubRoute.presentation.dto.dto.request.HubRouteUpdateRequestDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteCreateResponseDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteUpdateResponseDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class HubRouteCommandServiceTest {

    @Mock
    private HubRouteCommandRepository hubRouteCommandRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private HubPort hubPort;

    @Spy
    private TransactionTemplate transactionTemplate = new DummyTransactionTemplate();

    @InjectMocks
    private HubRouteCommandService hubRouteCommandService;

    private UserPrincipal masterUser;
    private UserPrincipal normalUser;

    @BeforeEach
    void setUp() {
        // 올바른 UserPrincipal 타입 스펙: (Long userId, Role role, UUID hubId, UUID companyId)
        masterUser = new UserPrincipal(1L, Role.MASTER, null, null);
        normalUser = new UserPrincipal(2L, Role.HUB_MANAGER, null, null);
    }

    private static class DummyTransactionTemplate extends TransactionTemplate {
        @Override
        public <T> T execute(org.springframework.transaction.support.TransactionCallback<T> action) {
            return action.doInTransaction(null);
        }
    }

    @Nested
    @DisplayName("허브 경로 생성 테스트")
    class CreateHubRouteTest {

        @Test
        @DisplayName("MASTER 권한 유저가 요청 시 경로를 성공적으로 생성한다")
        void createHubRoute_Success_Master() {
            // given
            UUID startHubId = UUID.randomUUID();
            UUID endHubId = UUID.randomUUID();
            HubRouteCreateCommand command = new HubRouteCreateCommand(
                    startHubId, endHubId, 60, BigDecimal.valueOf(50.0)
            );

            when(hubPort.validateHubIds(List.of(startHubId, endHubId)))
                    .thenReturn(Set.of(startHubId, endHubId));
            when(hubRouteCommandRepository.existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(startHubId, endHubId))
                    .thenReturn(false);

            // when
            HubRouteCreateResponseDto response = hubRouteCommandService.createHubRoute(command, masterUser);

            // then
            verify(hubRouteCommandRepository).save(any(HubRoute.class));
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("MASTER 권한이 아닌 유저가 경로 생성 요청 시 예외를 던진다 (권한 검증)")
        void createHubRoute_Forbidden_WhenNotMaster() {
            // given
            HubRouteCreateCommand command = new HubRouteCreateCommand(
                    UUID.randomUUID(), UUID.randomUUID(), 60, BigDecimal.valueOf(50.0)
            );

            // when & then
            assertThatThrownBy(() -> hubRouteCommandService.createHubRoute(command, normalUser))
                    .isInstanceOf(CustomException.class);

            verify(hubRouteCommandRepository, never()).save(any(HubRoute.class));
        }
    }

    @Nested
    @DisplayName("허브 경로 수정 테스트")
    class UpdateHubRouteTest {

        @Test
        @DisplayName("MASTER 권한 유저가 요청 시 경로를 성공적으로 수정한다")
        void updateHubRoute_Success_Master() {
            // given
            UUID routeId = UUID.randomUUID();
            UUID startHubId = UUID.randomUUID();
            UUID endHubId = UUID.randomUUID();
            HubRouteUpdateRequestDto requestDto = new HubRouteUpdateRequestDto(
                    startHubId, endHubId, 120, BigDecimal.valueOf(100.0)
            );

            HubRoute existingRoute = HubRoute.create(startHubId, endHubId, 60, BigDecimal.valueOf(50.0), 1L);

            when(hubPort.validateHubIds(List.of(startHubId, endHubId)))
                    .thenReturn(Set.of(startHubId, endHubId));
            when(hubRouteCommandRepository.findByIdAndDeletedAtIsNull(routeId))
                    .thenReturn(Optional.of(existingRoute));
            when(hubRouteCommandRepository.existsByStartHubIdAndEndHubIdAndHubRouteIdNotAndDeletedAtIsNull(startHubId, endHubId, routeId))
                    .thenReturn(false);

            // when
            HubRouteUpdateResponseDto response = hubRouteCommandService.updateHubRoute(routeId, masterUser, requestDto);

            // then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("MASTER 권한이 아닌 유저가 경로 수정 요청 시 예외를 던진다 (권한 검증)")
        void updateHubRoute_Forbidden_WhenNotMaster() {
            // given
            UUID routeId = UUID.randomUUID();
            HubRouteUpdateRequestDto requestDto = new HubRouteUpdateRequestDto(
                    UUID.randomUUID(), UUID.randomUUID(), 120, BigDecimal.valueOf(100.0)
            );

            // when & then
            assertThatThrownBy(() -> hubRouteCommandService.updateHubRoute(routeId, normalUser, requestDto))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("허브 경로 삭제 테스트")
    class DeleteHubRouteTest {

        @Test
        @DisplayName("MASTER 권한 유저가 요청 시 경로를 정상 삭제 처리한다")
        void deleteHubRoute_Success_Master() {
            // given
            UUID routeId = UUID.randomUUID();
            HubRoute existingRoute = HubRoute.create(UUID.randomUUID(), UUID.randomUUID(), 60, BigDecimal.valueOf(50.0), 1L);

            when(hubRouteCommandRepository.findByHubRouteIdAndDeletedAtIsNull(routeId)).thenReturn(true);
            when(hubRouteCommandRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.of(existingRoute));

            // when
            hubRouteCommandService.deleteHubRoute(routeId, masterUser);

            // then
            assertThat(existingRoute.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("MASTER 권한이 아닌 유저가 경로 삭제 요청 시 예외를 던진다 (권한 검증)")
        void deleteHubRoute_Forbidden_WhenNotMaster() {
            // given
            UUID routeId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> hubRouteCommandService.deleteHubRoute(routeId, normalUser))
                    .isInstanceOf(CustomException.class);
        }
    }
}