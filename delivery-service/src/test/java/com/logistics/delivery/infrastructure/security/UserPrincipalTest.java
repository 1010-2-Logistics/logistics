package com.logistics.delivery.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logistics.delivery.domain.entity.Role;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 게이트웨이가 주입한 헤더가 UserPrincipal로 변환되고, role별 제약이 올바르게
 * 검증되는지 확인한다. 서비스 계층 테스트로는 잡히지 않는 인증 계층 회귀를 막는다.
 */
class UserPrincipalTest {

    private static final UUID HUB_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();

    @Nested
    @DisplayName("헤더 파싱")
    class From {

        @Test
        @DisplayName("정상 헤더는 각 필드로 매핑된다")
        void 정상_헤더_파싱() {
            UserPrincipal principal =
                    UserPrincipal.from("7", "HUB_MANAGER", HUB_ID.toString(), COMPANY_ID.toString());

            assertThat(principal).isNotNull();
            assertThat(principal.getUserId()).isEqualTo(7L);
            assertThat(principal.getRole()).isEqualTo(Role.HUB_MANAGER);
            assertThat(principal.getHubId()).isEqualTo(HUB_ID);
            assertThat(principal.getCompanyId()).isEqualTo(COMPANY_ID);
        }

        @Test
        @DisplayName("userId가 없으면 null을 반환한다")
        void userId_없음() {
            assertThat(UserPrincipal.from(null, "MASTER", null, null)).isNull();
            assertThat(UserPrincipal.from("  ", "MASTER", null, null)).isNull();
        }

        @Test
        @DisplayName("형식이 잘못된 헤더는 null을 반환한다")
        void 잘못된_형식() {
            assertThat(UserPrincipal.from("abc", "MASTER", null, null)).isNull();
            assertThat(UserPrincipal.from("1", "NOT_A_ROLE", null, null)).isNull();
            assertThat(UserPrincipal.from("1", "MASTER", "not-a-uuid", null)).isNull();
        }

        @Test
        @DisplayName("선택 헤더가 없어도 파싱된다")
        void 선택_헤더_없음() {
            UserPrincipal principal = UserPrincipal.from("7", "HUB_DELIVERY_MANAGER", null, null);

            assertThat(principal).isNotNull();
            assertThat(principal.getHubId()).isNull();
            assertThat(principal.getCompanyId()).isNull();
        }
    }

    @Nested
    @DisplayName("MASTER 제약")
    class Master {

        @Test
        @DisplayName("hubId가 없으면 통과한다")
        void hubId_없음_통과() {
            UserPrincipal principal = UserPrincipal.from("1", "MASTER", null, null);

            assertThatCode(principal::validateRoleConstraints).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("hubId가 있으면 거부된다 — MASTER는 특정 허브에 소속되지 않는다")
        void hubId_있음_거부() {
            UserPrincipal principal = UserPrincipal.from("1", "MASTER", HUB_ID.toString(), null);

            assertThatThrownBy(principal::validateRoleConstraints)
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("HUB_MANAGER 제약")
    class HubManager {

        @Test
        @DisplayName("hubId가 있으면 통과한다")
        void hubId_있음_통과() {
            UserPrincipal principal = UserPrincipal.from("5", "HUB_MANAGER", HUB_ID.toString(), null);

            assertThatCode(principal::validateRoleConstraints).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("hubId가 없으면 거부된다 — 담당 허브 스코핑에 반드시 필요하다")
        void hubId_없음_거부() {
            UserPrincipal principal = UserPrincipal.from("5", "HUB_MANAGER", null, null);

            assertThatThrownBy(principal::validateRoleConstraints)
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("배송담당자 제약")
    class DeliveryManagers {

        @Test
        @DisplayName("허브 배송담당자는 hubId가 없어도 통과한다")
        void 허브_배송담당자_hubId_없음_통과() {
            // 회귀 방지: 허브 배송담당자는 구간마다 이동하므로 고정 허브가 없다.
            // DeliveryManager 엔티티도 "허브 배송담당자는 hubId null"을 강제한다.
            // company-service의 제약(hubId 필수)을 그대로 가져오면 이 role 전체가 403이 된다.
            UserPrincipal principal = UserPrincipal.from("7", "HUB_DELIVERY_MANAGER", null, null);

            assertThatCode(principal::validateRoleConstraints).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("업체 배송담당자는 hubId가 있어도 통과한다")
        void 업체_배송담당자_통과() {
            UserPrincipal principal =
                    UserPrincipal.from("9", "COMPANY_DELIVERY_MANAGER", HUB_ID.toString(), COMPANY_ID.toString());

            assertThatCode(principal::validateRoleConstraints).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("업체 관리자는 hubId 유무와 무관하게 통과한다")
        void 업체_관리자_통과() {
            UserPrincipal principal = UserPrincipal.from("11", "COMPANY_MANAGER", null, COMPANY_ID.toString());

            assertThatCode(principal::validateRoleConstraints).doesNotThrowAnyException();
        }
    }
}
