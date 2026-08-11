//package com.logistics.template.application.command;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.logistics.hub.application.dto.command.HubCreateCommand;
//import com.logistics.hub.application.dto.command.HubUpdateCommand;
//import com.logistics.hub.application.port.EventPublisher;
//import com.logistics.hub.application.service.HubCommandService;
//import com.logistics.hub.domain.entity.Hub;
//import com.logistics.hub.domain.repository.HubCommandRepository;
//import com.logistics.hub.global.exception.CustomException;
//import com.logistics.hub.presentation.dto.dto.response.HubCreateResponseDto;
//import com.logistics.hub.presentation.dto.dto.response.HubResponseDto;
//import java.math.BigDecimal;
//import java.util.Optional;
//import java.util.UUID;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//class HubCommandServiceTest {
//
//    @Mock
//    private HubCommandRepository hubCommandRepository;
//
//    @Mock
//    private EventPublisher eventPublisher;
//
//    @InjectMocks
//    private HubCommandService hubCommandService;
//
//    // ==================== 1. 허브 등록 (createHub) 테스트 ====================
//    @Nested
//    @DisplayName("허브 등록 테스트")
//    class CreateHubTest {
//
//        @Test
//        @DisplayName("정상적인 명령이 주어지면 허브를 생성하고 저장한다")
//        void createHub_Success() {
//            // given
//            HubCreateCommand command = new HubCreateCommand(
//                    "서울 센터",
//                    "서울시 강남구",
//                    BigDecimal.valueOf(37.123456),
//                    BigDecimal.valueOf(127.123456),
//                    1L
//            );
//
//            when(hubCommandRepository.existsByLatitudeAndLongitudeAndDeletedAtIsNull(command.latitude(), command.longitude()))
//                    .thenReturn(false);
//            when(hubCommandRepository.existsByHubAddressAndDeletedAtIsNull(command.hubAddress()))
//                    .thenReturn(false);
//
//            // when
//            HubCreateResponseDto response = hubCommandService.createHub(command);
//
//            // then
//            verify(hubCommandRepository).save(any(Hub.class));
//            assertThat(response).isNotNull();
//        }
//
//        @Test
//        @DisplayName("동일한 위도/경도가 이미 존재하는 경우 예외를 던진다")
//        void createHub_DuplicateLatLong_ThrowsException() {
//            // given
//            HubCreateCommand command = new HubCreateCommand(
//                    "서울 센터",
//                    "서울시 강남구",
//                    BigDecimal.valueOf(37.123456),
//                    BigDecimal.valueOf(127.123456),
//                    1L
//            );
//
//            when(hubCommandRepository.existsByLatitudeAndLongitudeAndDeletedAtIsNull(command.latitude(), command.longitude()))
//                    .thenReturn(true);
//
//            // when & then
//            assertThatThrownBy(() -> hubCommandService.createHub(command))
//                    .isInstanceOf(CustomException.class);
//
//            verify(hubCommandRepository, never()).save(any(Hub.class));
//        }
//
//        @Test
//        @DisplayName("동일한 주소가 이미 존재하는 경우 예외를 던진다")
//        void createHub_DuplicateAddress_ThrowsException() {
//            // given
//            HubCreateCommand command = new HubCreateCommand(
//                    "서울 센터",
//                    "서울시 강남구",
//                    BigDecimal.valueOf(37.123456),
//                    BigDecimal.valueOf(127.123456),
//                    1L
//            );
//
//            when(hubCommandRepository.existsByLatitudeAndLongitudeAndDeletedAtIsNull(command.latitude(), command.longitude()))
//                    .thenReturn(false);
//            when(hubCommandRepository.existsByHubAddressAndDeletedAtIsNull(command.hubAddress()))
//                    .thenReturn(true);
//
//            // when & then
//            assertThatThrownBy(() -> hubCommandService.createHub(command))
//                    .isInstanceOf(CustomException.class);
//
//            verify(hubCommandRepository, never()).save(any(Hub.class));
//        }
//    }
//
//    // ==================== 2. 허브 수정 (updateHub) 테스트 ====================
//    @Nested
//    @DisplayName("허브 수정 테스트")
//    class UpdateHubTest {
//
//        @Test
//        @DisplayName("정상적으로 정보 변경 요청 시 허브 정보를 수정한다")
//        void updateHub_Success() {
//            // given
//            UUID hubId = UUID.randomUUID();
//            HubUpdateCommand command = new HubUpdateCommand(
//                    "수정된 서울 센터",
//                    "서울시 서초구",
//                    BigDecimal.valueOf(37.654321),
//                    BigDecimal.valueOf(127.654321)
//            );
//
//            Hub existingHub = Hub.create(
//                    "기존 서울 센터",
//                    "서울시 강남구",
//                    BigDecimal.valueOf(37.123456),
//                    BigDecimal.valueOf(127.123456),
//                    1L
//            );
//
//            when(hubCommandRepository.findByhubIdAndDeletedAtIsNull(hubId)).thenReturn(true);
//            when(hubCommandRepository.existsDuplicateHubForUpdate(
//                    eq(hubId), eq(command.latitude()), eq(command.longitude()), eq(command.hubAddress())))
//                    .thenReturn(false);
//            when(hubCommandRepository.findByIdAndDeletedAtIsNull(hubId)).thenReturn(Optional.of(existingHub));
//
//            // when
//            HubResponseDto response = hubCommandService.updateHub(hubId, command);
//
//            // then
//            assertThat(response.name()).isEqualTo("수정된 서울 센터");
//            assertThat(response.hubAddress()).isEqualTo("서울시 서초구");
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 허브 ID 수정 시 예외를 던진다")
//        void updateHub_NotFound_ThrowsException() {
//            // given
//            UUID hubId = UUID.randomUUID();
//            HubUpdateCommand command = new HubUpdateCommand(
//                    "수정된 센터",
//                    "서울시 서초구",
//                    BigDecimal.valueOf(37.654321),
//                    BigDecimal.valueOf(127.654321)
//            );
//
//            when(hubCommandRepository.findByhubIdAndDeletedAtIsNull(hubId)).thenReturn(false);
//
//            // when & then
//            assertThatThrownBy(() -> hubCommandService.updateHub(hubId, command))
//                    .isInstanceOf(CustomException.class);
//        }
//
//        @Test
//        @DisplayName("수정하려는 위도/경도/주소가 타 허브와 중복되면 예외를 던진다")
//        void updateHub_DuplicateInfo_ThrowsException() {
//            // given
//            UUID hubId = UUID.randomUUID();
//            HubUpdateCommand command = new HubUpdateCommand(
//                    "수정된 센터",
//                    "서울시 서초구",
//                    BigDecimal.valueOf(37.654321),
//                    BigDecimal.valueOf(127.654321)
//            );
//
//            when(hubCommandRepository.findByhubIdAndDeletedAtIsNull(hubId)).thenReturn(true);
//            when(hubCommandRepository.existsDuplicateHubForUpdate(
//                    eq(hubId), eq(command.latitude()), eq(command.longitude()), eq(command.hubAddress())))
//                    .thenReturn(true);
//
//            // when & then
//            assertThatThrownBy(() -> hubCommandService.updateHub(hubId, command))
//                    .isInstanceOf(CustomException.class);
//        }
//    }
//
//    // ==================== 3. 허브 삭제 (deleteHub) 테스트 ====================
//    @Nested
//    @DisplayName("허브 삭제 테스트")
//    class DeleteHubTest {
//
//        @Test
//        @DisplayName("존재하는 허브 삭제 성공 시 삭제 처리 상태로 전환된다")
//        void deleteHub_Success() {
//            // given
//            UUID hubId = UUID.randomUUID();
//            long deletedBy = 999L;
//
//            Hub existingHub = Hub.create(
//                    "삭제될 센터",
//                    "서울시 강남구",
//                    BigDecimal.valueOf(37.123456),
//                    BigDecimal.valueOf(127.123456),
//                    1L
//            );
//
//            when(hubCommandRepository.findByhubIdAndDeletedAtIsNull(hubId)).thenReturn(true);
//            when(hubCommandRepository.findByIdAndDeletedAtIsNull(hubId)).thenReturn(Optional.of(existingHub));
//
//            // when
//            hubCommandService.deleteHub(hubId, deletedBy);
//
//            // then
//            verify(hubCommandRepository).findByIdAndDeletedAtIsNull(hubId);
//        }
//
//        @Test
//        @DisplayName("이미 삭제되었거나 존재하지 않는 허브일 경우 예외를 던진다")
//        void deleteHub_AlreadyDeletedOrNotFound_ThrowsException() {
//            // given
//            UUID hubId = UUID.randomUUID();
//            long deletedBy = 999L;
//
//            when(hubCommandRepository.findByhubIdAndDeletedAtIsNull(hubId)).thenReturn(false);
//
//            // when & then
//            assertThatThrownBy(() -> hubCommandService.deleteHub(hubId, deletedBy))
//                    .isInstanceOf(CustomException.class);
//        }
//    }
//}
