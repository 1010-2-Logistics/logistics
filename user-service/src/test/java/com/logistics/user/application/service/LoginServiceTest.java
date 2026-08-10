package com.logistics.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.logistics.user.application.dto.command.LoginCommandDto;
import com.logistics.user.application.dto.result.LoginResultDto;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.AuthErrorCode;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.infrastructure.security.JwtProvider;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 로그인 테스트.
 *
 * 검증 대상:
 * - 사용자 조회
 * - 비밀번호 비교
 * - 삭제 여부
 * - 가입 승인 상태
 * - Access Token과 Refresh Token 발급
 *
 * 실제 DB와 JWT 라이브러리는 실행하지 않고 Mock으로 대체
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(
                userQueryRepository,
                passwordEncoder,
                jwtProvider
        );
    }

    @Test
    @DisplayName("APPROVED 사용자는 로그인에 성공하고 Access Token과 Refresh Token을 발급받는다")
    void login_success() {
        // given
        UUID hubId = UUID.randomUUID();

        User user = createApprovedHubDeliveryManager(
                15L,
                hubId
        );

        LoginCommandDto command = new LoginCommandDto(
                "delivery1",
                "Password1!"
        );

        /*
         * username으로 사용자를 조회하면
         * APPROVED 상태의 사용자를 반환
         */
        given(
                userQueryRepository.findByUsername(
                        "delivery1"
                )
        ).willReturn(Optional.of(user));

        /*
         * 입력한 평문 비밀번호와 저장된 BCrypt 해시가
         * 일치한다고 설정
         */
        given(
                passwordEncoder.matches(
                        "Password1!",
                        "encodedPassword"
                )
        ).willReturn(true);

        /*
         * 실제 JWT를 만들지 않고,
         * JwtProvider가 정해진 문자열을 반환한다고 가정
         */
        given(
                jwtProvider.createAccessToken(user)
        ).willReturn("access-token");

        given(
                jwtProvider.createRefreshToken(user)
        ).willReturn("refresh-token");

        given(
                jwtProvider.getAccessTokenExpirationSeconds()
        ).willReturn(1800L);

        given(
                jwtProvider.getRefreshTokenExpirationSeconds()
        ).willReturn(1209600L);

        // when
        LoginResultDto result = loginService.login(command);

        // then
        assertThat(result.grantType())
                .isEqualTo("Bearer");

        assertThat(result.accessToken())
                .isEqualTo("access-token");

        assertThat(result.refreshToken())
                .isEqualTo("refresh-token");

        assertThat(result.accessTokenExpiresIn())
                .isEqualTo(1800L);

        assertThat(result.refreshTokenExpiresIn())
                .isEqualTo(1209600L);

        assertThat(result.user().userId())
                .isEqualTo(15L);

        assertThat(result.user().username())
                .isEqualTo("delivery1");

        assertThat(result.user().role())
                .isEqualTo(
                        UserRole.HUB_DELIVERY_MANAGER
                );

        assertThat(result.user().hubId())
                .isEqualTo(hubId);

        assertThat(result.user().companyId())
                .isNull();

        /*
         * 로그인 과정에서 실제로 호출됐는지 확인
         */
        then(userQueryRepository)
                .should()
                .findByUsername("delivery1");

        then(passwordEncoder)
                .should()
                .matches(
                        "Password1!",
                        "encodedPassword"
                );

        then(jwtProvider)
                .should()
                .createAccessToken(user);

        then(jwtProvider)
                .should()
                .createRefreshToken(user);
    }

    @Test
    @DisplayName("존재하지 않는 username이면 AUTH_INVALID_CREDENTIALS 예외가 발생한다")
    void login_fail_whenUsernameDoesNotExist() {
        // given
        LoginCommandDto command = new LoginCommandDto(
                "unknown1",
                "Password1!"
        );

        given(
                userQueryRepository.findByUsername(
                        "unknown1"
                )
        ).willReturn(Optional.empty());

        // when & then
        assertAuthException(
                () -> loginService.login(command),
                AuthErrorCode.AUTH_INVALID_CREDENTIALS
        );

        /*
         * 사용자가 없으므로 비밀번호 비교와 토큰 생성은 실행되지 않는다.
         */
        then(passwordEncoder)
                .shouldHaveNoInteractions();

        then(jwtProvider)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 AUTH_INVALID_CREDENTIALS 예외가 발생한다")
    void login_fail_whenPasswordDoesNotMatch() {
        // given
        User user = createApprovedHubDeliveryManager(
                15L,
                UUID.randomUUID()
        );

        LoginCommandDto command = new LoginCommandDto(
                "delivery1",
                "WrongPassword1!"
        );

        given(
                userQueryRepository.findByUsername(
                        "delivery1"
                )
        ).willReturn(Optional.of(user));

        given(
                passwordEncoder.matches(
                        "WrongPassword1!",
                        "encodedPassword"
                )
        ).willReturn(false);

        // when & then
        assertAuthException(
                () -> loginService.login(command),
                AuthErrorCode.AUTH_INVALID_CREDENTIALS
        );

        /*
         * 비밀번호 검증에 실패했으므로 토큰은 생성되지 않아야 한다.
         */
        then(jwtProvider)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("PENDING 사용자는 로그인할 수 없다")
    void login_fail_whenUserIsPending() {
        // given
        User user = createPendingHubDeliveryManager(
                15L,
                UUID.randomUUID()
        );

        LoginCommandDto command = new LoginCommandDto(
                "delivery1",
                "Password1!"
        );

        given(
                userQueryRepository.findByUsername(
                        "delivery1"
                )
        ).willReturn(Optional.of(user));

        given(
                passwordEncoder.matches(
                        "Password1!",
                        "encodedPassword"
                )
        ).willReturn(true);

        // when & then
        assertAuthException(
                () -> loginService.login(command),
                AuthErrorCode.AUTH_APPROVAL_PENDING
        );

        then(jwtProvider)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("REJECTED 사용자는 로그인할 수 없다")
    void login_fail_whenUserIsRejected() {
        // given
        User user = createPendingHubDeliveryManager(
                15L,
                UUID.randomUUID()
        );

        /*
         * 회원가입 직후 상태는 PENDING이므로
         * reject()를 호출해 REJECTED 상태로
         */
        // 변경
        user.reject("가입 조건을 충족하지 않았습니다.");

        LoginCommandDto command = new LoginCommandDto(
                "delivery1",
                "Password1!"
        );

        given(
                userQueryRepository.findByUsername(
                        "delivery1"
                )
        ).willReturn(Optional.of(user));

        given(
                passwordEncoder.matches(
                        "Password1!",
                        "encodedPassword"
                )
        ).willReturn(true);

        // when & then
        assertAuthException(
                () -> loginService.login(command),
                AuthErrorCode.AUTH_APPROVAL_REJECTED
        );

        then(jwtProvider)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("삭제된 사용자는 로그인할 수 없다")
    void login_fail_whenUserIsDeleted() {
        // given
        User user = createApprovedHubDeliveryManager(
                15L,
                UUID.randomUUID()
        );

        /*
         * BaseEntity의 Soft Delete 메서드를 호출한다.
         */
        user.markDeleted(1L);

        LoginCommandDto command = new LoginCommandDto(
                "delivery1",
                "Password1!"
        );

        given(
                userQueryRepository.findByUsername(
                        "delivery1"
                )
        ).willReturn(Optional.of(user));

        // when & then
        assertAuthException(
                () -> loginService.login(command),
                AuthErrorCode.AUTH_USER_DELETED
        );

        /*
         * 삭제 검증이 비밀번호 검증보다 먼저 실행되므로
         * PasswordEncoder와 JwtProvider는 호출되지 않는다.
         */
        then(passwordEncoder)
                .shouldHaveNoInteractions();

        then(jwtProvider)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("username이 비어 있으면 로그인 요청 형식 오류가 발생한다")
    void login_fail_whenUsernameIsBlank() {
        // given
        LoginCommandDto command = new LoginCommandDto(
                " ",
                "Password1!"
        );

        // when & then
        assertAuthException(
                () -> loginService.login(command),
                AuthErrorCode.AUTH_LOGIN_INVALID_REQUEST
        );

        then(userQueryRepository)
                .shouldHaveNoInteractions();

        then(passwordEncoder)
                .shouldHaveNoInteractions();

        then(jwtProvider)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("password가 비어 있으면 로그인 요청 형식 오류가 발생한다")
    void login_fail_whenPasswordIsBlank() {
        // given
        LoginCommandDto command = new LoginCommandDto(
                "delivery1",
                ""
        );

        // when & then
        assertAuthException(
                () -> loginService.login(command),
                AuthErrorCode.AUTH_LOGIN_INVALID_REQUEST
        );

        then(userQueryRepository)
                .shouldHaveNoInteractions();

        then(passwordEncoder)
                .shouldHaveNoInteractions();

        then(jwtProvider)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("아이디가 없는 경우와 비밀번호가 틀린 경우는 같은 오류 코드를 반환한다")
    void login_hidesWhetherUsernameExists() {
        // given
        LoginCommandDto unknownUsernameCommand =
                new LoginCommandDto(
                        "unknown1",
                        "Password1!"
                );

        User user = createApprovedHubDeliveryManager(
                15L,
                UUID.randomUUID()
        );

        LoginCommandDto wrongPasswordCommand =
                new LoginCommandDto(
                        "delivery1",
                        "WrongPassword1!"
                );

        given(
                userQueryRepository.findByUsername(
                        "unknown1"
                )
        ).willReturn(Optional.empty());

        given(
                userQueryRepository.findByUsername(
                        "delivery1"
                )
        ).willReturn(Optional.of(user));

        given(
                passwordEncoder.matches(
                        "WrongPassword1!",
                        "encodedPassword"
                )
        ).willReturn(false);

        // when & then
        assertAuthException(
                () -> loginService.login(
                        unknownUsernameCommand
                ),
                AuthErrorCode.AUTH_INVALID_CREDENTIALS
        );

        assertAuthException(
                () -> loginService.login(
                        wrongPasswordCommand
                ),
                AuthErrorCode.AUTH_INVALID_CREDENTIALS
        );
    }

    /**
     * PENDING 상태의 허브 배송 담당자를 생성
     *
     * User.create()는 회원가입 흐름과 동일하게
     * 사용자 PENDING 상태
     */
    private User createPendingHubDeliveryManager(
            Long userId,
            UUID hubId
    ) {
        User user = User.create(
                "delivery1",
                "encodedPassword",
                "U_DELIVERY_" + userId,
                UserRole.HUB_DELIVERY_MANAGER,
                null,
                hubId
        );

        setUserId(user, userId);

        return user;
    }

    /**
     * APPROVED 상태의 허브 배송 담당자 생성
     */
    private User createApprovedHubDeliveryManager(
            Long userId,
            UUID hubId
    ) {
        User user = createPendingHubDeliveryManager(
                userId,
                hubId
        );

        user.approve();

        return user;
    }

    /**
     * userId는 실제 환경에서는 DB가 생성한다.
     *
     * 단위 테스트에서는 DB를 사용하지 않으므로
     * ReflectionTestUtils로 식별자를 설정한다.
     */
    private void setUserId(
            User user,
            Long userId
    ) {
        ReflectionTestUtils.setField(
                user,
                "userId",
                userId
        );
    }

    /**
     * 발생한 CustomException의 ErrorCode를 공통 방식으로 검증
     */
    private void assertAuthException(
            Runnable action,
            AuthErrorCode expectedErrorCode
    ) {
        assertThatThrownBy(action::run)
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(
                            customException.getErrorCode()
                    ).isEqualTo(expectedErrorCode);
                });
    }
}