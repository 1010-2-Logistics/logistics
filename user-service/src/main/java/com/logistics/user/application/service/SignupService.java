package com.logistics.user.application.service;

import com.logistics.user.application.dto.command.SignupCommand;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.repository.UserCommandRepository;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입
 *
 * 주요 책임:
 * - 비밀번호 확인값 검증
 * - username 중복 검증
 * - Slack ID 중복 검증
 * - 비밀번호 암호화
 * - User 도메인 생성 및 저장
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SignupService {

    private final UserCommandRepository userCommandRepository;
    private final UserQueryRepository userQueryRepository;
    private final PasswordEncoder passwordEncoder;

    public User signup(SignupCommand command) {
        // 1. 비밀번호 확인값이 같은지 검증
        validatePasswordConfirmation(command);

        // 2. username 중복 검증
        validateDuplicateUsername(command.username());

        // 3. Slack ID 중복 검증
        validateDuplicateSlackId(command.slackId());

        // 4. 평문 비밀번호를 BCrypt 해시값 변환
        String encodedPassword =
                passwordEncoder.encode(command.password());

        // 5. 역할과 소속 검증은 User 도메인이 담당한다.
        User user = User.create(
                command.username(),
                encodedPassword,
                command.slackId(),
                command.role(),
                command.companyId(),
                command.hubId()
        );

        // 6. 생성된 user 저장
        return userCommandRepository.save(user);
    }

    //비밀번호 충돌 검증
    private void validatePasswordConfirmation(
            SignupCommand command
    ) {
        if (!command.password().equals(
                command.passwordConfirm()
        )) {
            throw new CustomException(
                    UserErrorCode.USER_PASSWORD_CONFIRM_MISMATCH
            );
        }
    }

    //username 중복 검증
    private void validateDuplicateUsername(
            String username
    ) {
        boolean duplicated =
                userQueryRepository
                        .existsByUsername(
                                username
                        );

        if (duplicated) {
            throw new CustomException(
                    UserErrorCode.USER_USERNAME_DUPLICATED
            );
        }
    }
    //slackId 중복 검증
    private void validateDuplicateSlackId(
            String slackId
    ) {
        boolean duplicated =
                userQueryRepository
                        .existsBySlackId(
                                slackId
                        );

        if (duplicated) {
            throw new CustomException(
                    UserErrorCode.USER_SLACK_ID_DUPLICATED
            );
        }
    }
}