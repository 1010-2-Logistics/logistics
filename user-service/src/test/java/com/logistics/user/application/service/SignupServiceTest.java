package com.logistics.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.logistics.user.application.dto.command.SignupCommand;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserCommandRepository;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 회원가입 유스케이스 단위 테스트.
 *
 * 실제 DB와 Spring Context를 실행하지 않고,
 * SignupService의 비즈니스 흐름만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    /**
     * 사용자 저장 책임을 가진 Repository.
     */
    @Mock
    private UserCommandRepository userCommandRepository;

    /**
     * 사용자 중복 조회 책임을 가진 Repository.
     */
    @Mock
    private UserQueryRepository userQueryRepository;

    /**
     * 비밀번호 암호화 책임을 가진 객체.
     */
    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * 테스트 대상.
     */
    private SignupService signupService;

    @BeforeEach
    void setUp() {
        signupService = new SignupService(
                userCommandRepository,
                userQueryRepository,
                passwordEncoder
        );
    }

    @Test
    @DisplayName("회원가입하면 비밀번호를 암호화하고 PENDING 사용자로 저장한다")
    void signup_success() {
        // given
        UUID hubId = UUID.randomUUID();

        SignupCommand command = new SignupCommand(
                "user01",
                "Password1!",
                "Password1!",
                "U12345678",
                UserRole.HUB_MANAGER,
                null,
                hubId
        );

        /*
         * username과 Slack ID가 아직 사용되지 않았다고 설정한다.
         */
        given(
                userQueryRepository
                        .existsByUsernameAndDeletedAtIsNull("user01")
        ).willReturn(false);

        given(
                userQueryRepository
                        .existsBySlackIdAndDeletedAtIsNull("U12345678")
        ).willReturn(false);

        /*
         * PasswordEncoder가 평문을 특정 암호문으로 변환한다고 가정한다.
         */
        given(
                passwordEncoder.encode("Password1!")
        ).willReturn("$2a$10$encodedPassword");

        /*
         * Repository가 전달받은 User를 그대로 반환하도록 설정한다.
         *
         * save()가 void라면 이 given 구문은 제거해야 한다.
         */
        given(
                userCommandRepository.save(any(User.class))
        ).willAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = signupService.signup(command);

        // then
        assertThat(result.getUsername())
                .isEqualTo("user01");

        assertThat(result.getPassword())
                .isEqualTo("$2a$10$encodedPassword");

        assertThat(result.getSlackId())
                .isEqualTo("U12345678");

        assertThat(result.getRole())
                .isEqualTo(UserRole.HUB_MANAGER);

        assertThat(result.getHubId())
                .isEqualTo(hubId);

        assertThat(result.getCompanyId())
                .isNull();

        assertThat(result.getStatus())
                .isEqualTo(UserStatus.PENDING);

        /*
         * PasswordEncoder가 실제로 호출됐는지 확인한다.
         */
        then(passwordEncoder)
                .should()
                .encode("Password1!");

        /*
         * 생성된 User가 Repository에 저장됐는지 확인한다.
         */
        then(userCommandRepository)
                .should()
                .save(any(User.class));
    }

    @Test
    @DisplayName("저장되는 비밀번호는 평문이 아니라 암호화된 값이다")
    void signup_savesEncodedPassword() {
        // given
        SignupCommand command = createHubManagerCommand();

        given(
                userQueryRepository
                        .existsByUsernameAndDeletedAtIsNull(
                                command.username()
                        )
        ).willReturn(false);

        given(
                userQueryRepository
                        .existsBySlackIdAndDeletedAtIsNull(
                                command.slackId()
                        )
        ).willReturn(false);

        given(
                passwordEncoder.encode(command.password())
        ).willReturn("$2a$10$encodedPassword");

        given(
                userCommandRepository.save(any(User.class))
        ).willAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        // when
        signupService.signup(command);

        // then
        then(userCommandRepository)
                .should()
                .save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getPassword())
                .isNotEqualTo("Password1!");

        assertThat(savedUser.getPassword())
                .isEqualTo("$2a$10$encodedPassword");
    }

    @Test
    @DisplayName("비밀번호와 비밀번호 확인값이 다르면 회원가입에 실패한다")
    void signup_fail_whenPasswordConfirmationMismatch() {
        // given
        SignupCommand command = new SignupCommand(
                "user01",
                "Password1!",
                "Different1!",
                "U12345678",
                UserRole.HUB_MANAGER,
                null,
                UUID.randomUUID()
        );

        // when & then
        assertThatThrownBy(
                () -> signupService.signup(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(customException.getErrorCode())
                            .isEqualTo(
                                    UserErrorCode
                                            .USER_PASSWORD_CONFIRM_MISMATCH
                            );
                });

        /*
         * 앞 단계에서 실패했으므로
         * DB 조회, 암호화, 저장이 수행되면 안 된다.
         */
        then(userQueryRepository)
                .shouldHaveNoInteractions();

        then(passwordEncoder)
                .shouldHaveNoInteractions();

        then(userCommandRepository)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("username이 중복되면 회원가입에 실패한다")
    void signup_fail_whenUsernameDuplicated() {
        // given
        SignupCommand command = createHubManagerCommand();

        given(
                userQueryRepository
                        .existsByUsernameAndDeletedAtIsNull(
                                command.username()
                        )
        ).willReturn(true);

        // when & then
        assertThatThrownBy(
                () -> signupService.signup(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(customException.getErrorCode())
                            .isEqualTo(
                                    UserErrorCode
                                            .USER_USERNAME_DUPLICATED
                            );
                });

        /*
         * username 검증에서 실패했기 때문에
         * Slack ID 확인이나 암호화, 저장은 수행하지 않는다.
         */
        then(userQueryRepository)
                .should()
                .existsByUsernameAndDeletedAtIsNull(
                        command.username()
                );

        then(passwordEncoder)
                .shouldHaveNoInteractions();

        then(userCommandRepository)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Slack ID가 중복되면 회원가입에 실패한다")
    void signup_fail_whenSlackIdDuplicated() {
        // given
        SignupCommand command = createHubManagerCommand();

        given(
                userQueryRepository
                        .existsByUsernameAndDeletedAtIsNull(
                                command.username()
                        )
        ).willReturn(false);

        given(
                userQueryRepository
                        .existsBySlackIdAndDeletedAtIsNull(
                                command.slackId()
                        )
        ).willReturn(true);

        // when & then
        assertThatThrownBy(
                () -> signupService.signup(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(customException.getErrorCode())
                            .isEqualTo(
                                    UserErrorCode
                                            .USER_SLACK_ID_DUPLICATED
                            );
                });

        then(passwordEncoder)
                .shouldHaveNoInteractions();

        then(userCommandRepository)
                .shouldHaveNoInteractions();
    }

    /**
     * 여러 테스트에서 공통으로 사용하는 정상 회원가입 Command.
     */
    private SignupCommand createHubManagerCommand() {
        return new SignupCommand(
                "user01",
                "Password1!",
                "Password1!",
                "U12345678",
                UserRole.HUB_MANAGER,
                null,
                UUID.randomUUID()
        );
    }
}