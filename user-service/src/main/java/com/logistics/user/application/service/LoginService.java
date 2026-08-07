package com.logistics.user.application.service;

import com.logistics.user.application.dto.command.LoginCommandDto;
import com.logistics.user.application.dto.result.LoginResultDto;
import com.logistics.user.application.dto.result.LoginUserResultDto;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.AuthErrorCode;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.infrastructure.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 1. 로그인 요청값 검증
 * 2. username으로 사용자 조회
 * 3. 삭제 사용자 차단
 * 4. BCrypt 비밀번호 검증
 * 5. 승인 상태 검증
 * 6. Access Token과 Refresh Token 발급
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

    private static final String GRANT_TYPE = "Bearer";

    private final UserQueryRepository userQueryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResultDto login(
            LoginCommandDto command
    ) {
        // 1. 필수값 검증(아이디, 패스워드)
        validateCommand(command);

        /*
         * 2. username으로 사용자 조회
         */
        User user = userQueryRepository
                .findByUsername(command.username())
                .orElseThrow(() -> new CustomException(
                        AuthErrorCode.AUTH_INVALID_CREDENTIALS
                ));

        // 3. Soft Delete된 사용자 로그인 차단
        validateNotDeleted(user);

        // 4. 사용자가 입력한 비밀번호와 저장된 BCrypt 해시 비교
        validatePassword(
                command.password(),
                user.getPassword()
        );

        // 5. 가입 승인 상태 확인
        validateApprovalStatus(user);

        // 6. Access Token 생성
        String accessToken =
                jwtProvider.createAccessToken(user);

        // 7. Refresh Token 생성
        String refreshToken =
                jwtProvider.createRefreshToken(user);

        // 8. Application 결과 반환
        return new LoginResultDto(
                GRANT_TYPE,
                accessToken,
                refreshToken,
                jwtProvider.getAccessTokenExpirationSeconds(),
                jwtProvider.getRefreshTokenExpirationSeconds(),
                LoginUserResultDto.from(user)
        );
    }

    /**
     * 아이디, 패스워드 입력 확인
     */
    private void validateCommand(
            LoginCommandDto command
    ) {
        if (command == null
                || command.username() == null
                || command.username().isBlank()
                || command.password() == null
                || command.password().isBlank()) {

            throw new CustomException(
                    AuthErrorCode.AUTH_LOGIN_INVALID_REQUEST
            );
        }
    }

    /**
     * 삭제된 사용자는 로그인 불가능
     */
    private void validateNotDeleted(
            User user
    ) {
        if (user.getDeletedAt() != null) {
            throw new CustomException(
                    AuthErrorCode.AUTH_USER_DELETED
            );
        }
    }

    /**
     * 평문 비밀번호와 DB의 BCrypt 해시값 비교 검증
     */
    private void validatePassword(
            String rawPassword,
            String encodedPassword
    ) {
        boolean matched = passwordEncoder.matches(
                rawPassword,
                encodedPassword
        );

        if (!matched) {
            throw new CustomException(
                    AuthErrorCode.AUTH_INVALID_CREDENTIALS
            );
        }
    }

    /**
     * 승인된 사용자만 로그인 가능
     */
    private void validateApprovalStatus(
            User user
    ) {
        if (user.getStatus() == UserStatus.PENDING) {
            throw new CustomException(
                    AuthErrorCode.AUTH_APPROVAL_PENDING
            );
        }

        if (user.getStatus() == UserStatus.REJECTED) {
            throw new CustomException(
                    AuthErrorCode.AUTH_APPROVAL_REJECTED
            );
        }
    }
}