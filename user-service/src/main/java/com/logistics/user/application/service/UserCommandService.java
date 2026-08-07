package com.logistics.user.application.service;

import com.logistics.user.application.dto.command.ChangePasswordCommandDto;
import com.logistics.user.application.dto.command.CreateUserCommandDto;
import com.logistics.user.application.dto.command.UpdateMySlackIdCommandDto;
import com.logistics.user.application.dto.command.WithdrawUserCommandDto;
import com.logistics.user.application.dto.result.ChangePasswordResultDto;
import com.logistics.user.application.dto.result.UpdateMyInfoResultDto;
import com.logistics.user.application.event.UserCreatedEvent;
import com.logistics.user.application.port.EventPublisher;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.repository.UserCommandRepository;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserCommandRepository userCommandRepository;
    private final EventPublisher eventPublisher;
    private final UserQueryRepository userQueryRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 생성
     * 현재 command.encodedPassword()는 이름상 암호화된 값
     * 실제 회원가입 구현 시 반드시 BCrypt 처리된 값이 전달되어야 한다.
     */
    public Long create(CreateUserCommandDto command) {
        User user = User.create(
                command.username(),
                command.encodedPassword(),
                command.slackId(),
                command.role(),
                command.companyId(),
                command.hubId()
        );

        userCommandRepository.save(user);

        eventPublisher.publish(
                new UserCreatedEvent(
                        user.getUserId(),
                        user.getUsername(),
                        user.getRole(),
                        user.getStatus()
                )
        );

        return user.getUserId();
    }

    public UpdateMyInfoResultDto updateMySlackId(
            UpdateMySlackIdCommandDto command
    ) {
        /*
         * 삭제된 사용자는 조회 대상에서 제외한다.
         * 존재하지 않거나 삭제된 경우 모두 USER_NOT_FOUND.
         */
        User user = userCommandRepository
                .findByIdAndDeletedAtIsNull(command.userId())
                .orElseThrow(() -> new CustomException(
                        UserErrorCode.USER_NOT_FOUND
                ));

        /*
         * 현재 Slack ID와 동일한 값인지 먼저 확인한다.
         */
        if (user.getSlackId().equals(command.slackId())) {
            throw new CustomException(
                    UserErrorCode.USER_SAME_SLACK_ID_CONFLICT
            );
        }

        /*
         * 다른 활성 사용자가 이미 사용 중인지 확인한다.
         */
        if (userQueryRepository.existsBySlackId(
                command.slackId()
        )) {
            throw new CustomException(
                    UserErrorCode.USER_SLACK_ID_CONFLICT
            );
        }

        /*
         * User 객체가 자신의 상태를 직접 변경한다.
         */
        user.updateSlackId(command.slackId());

        return UpdateMyInfoResultDto.from(user);
    }

    /**
     * 로그인한 사용자의 비밀번호를 변경한다.
     */
    public ChangePasswordResultDto changePassword(
            ChangePasswordCommandDto command
    ) {
        validatePasswordCommand(command);

        /*
         * 삭제된 사용자는 조회 대상에서 제외한다.
         * 존재하지 않거나 삭제된 경우 USER_NOT_FOUND.
         */
        User user = userCommandRepository
                .findByIdAndDeletedAtIsNull(command.userId())
                .orElseThrow(() -> new CustomException(
                        UserErrorCode.USER_NOT_FOUND
                ));

        /*
         * 사용자가 입력한 현재 비밀번호와
         * DB에 저장된 BCrypt 해시값을 비교한다.
         */
        validateCurrentPassword(
                command.currentPassword(),
                user.getPassword()
        );

        /*
         * 새 비밀번호와 확인 값이 일치해야 한다.
         */
        validatePasswordConfirm(
                command.newPassword(),
                command.newPasswordConfirm()
        );

        /*
         * 새 비밀번호가 현재 비밀번호와 같으면 변경할 수 없다.
         *
         * BCrypt는 같은 평문도 매번 다른 해시가 생성되므로
         * 문자열 비교가 아니라 matches()를 사용한다.
         */
        validateNewPasswordDifferent(
                command.newPassword(),
                user.getPassword()
        );

        /*
         * 새 비밀번호를 BCrypt 단방향 해시로 변환한다.
         */
        String encodedNewPassword =
                passwordEncoder.encode(
                        command.newPassword()
                );

        /*
         * User 객체가 자신의 비밀번호 상태를 변경한다.
         */
        user.changePassword(encodedNewPassword);

        return ChangePasswordResultDto.from(user);
    }

    // 요청값 확인
    private void validatePasswordCommand(
            ChangePasswordCommandDto command
    ) {
        if (command == null
                || command.userId() == null
                || command.currentPassword() == null
                || command.currentPassword().isBlank()
                || command.newPassword() == null
                || command.newPassword().isBlank()
                || command.newPasswordConfirm() == null
                || command.newPasswordConfirm().isBlank()) {

            throw new CustomException(
                    UserErrorCode.USER_PASSWORD_INVALID_REQUEST
            );
        }
    }

    // 현재 비밀번호 확인
    private void validateCurrentPassword(
            String currentPassword,
            String encodedPassword
    ) {
        boolean matched = passwordEncoder.matches(
                currentPassword,
                encodedPassword
        );

        if (!matched) {
            throw new CustomException(
                    UserErrorCode.USER_PASSWORD_MISMATCH
            );
        }
    }

    // 변경할 비밀번호 확인값이 일치하는지 검증
    private void validatePasswordConfirm(
            String newPassword,
            String newPasswordConfirm
    ) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new CustomException(
                    UserErrorCode.USER_PASSWORD_CONFIRM_MISMATCH
            );
        }
    }

    // 변경하려는 비밀번호가 현재 비밀번호와 같은지 검증
    private void validateNewPasswordDifferent(
            String newPassword,
            String encodedCurrentPassword
    ) {
        boolean samePassword = passwordEncoder.matches(
                newPassword,
                encodedCurrentPassword
        );

        if (samePassword) {
            throw new CustomException(
                    UserErrorCode.USER_PASSWORD_CONFLICT
            );
        }
    }

    /**
     * 로그인한 사용자의 계정을 탈퇴 처리한다.
     */
    public void withdraw(
            WithdrawUserCommandDto command
    ) {
        validateWithdrawCommand(command);

        /*
         * 삭제된 사용자는 조회 대상에서 제외한다.
         * 존재하지 않거나 이미 삭제된 경우 USER_NOT_FOUND.
         */
        User user = userCommandRepository
                .findByIdAndDeletedAtIsNull(command.userId())
                .orElseThrow(() -> new CustomException(
                        UserErrorCode.USER_NOT_FOUND
                ));

        /*
         * 본인 확인을 위해 현재 비밀번호를 검증한다.
         */
        validateWithdrawPassword(
                command.password(),
                user.getPassword()
        );

        /*
         * 진행 중 업무 검증.
         *
         * 현재는 관련 서비스 연동 전이므로 TODO로 남긴다.
         * 추후 Delivery Service, Company Service,
         * Hub Service 내부 API 호출로 검증한다.
         */
        validateNoActiveTasks(user);

        /*
         * Soft Delete 처리.
         *
         * 본인이 본인 계정을 삭제하므로
         * deletedBy에는 현재 사용자 ID를 사용한다.
         */
        user.markDeleted(command.userId());
    }

    //사용자 정보가 없거나 이미 삭제된 사용자인지 검증
    private void validateWithdrawCommand(
            WithdrawUserCommandDto command
    ) {
        if (command == null
                || command.userId() == null
                || command.password() == null
                || command.password().isBlank()) {

            throw new CustomException(
                    UserErrorCode.USER_INVALID_REQUEST
            );
        }
    }

    //본인확인을 위한 현재 비밀번호 검증
    private void validateWithdrawPassword(
            String rawPassword,
            String encodedPassword
    ) {
        boolean matched = passwordEncoder.matches(
                rawPassword,
                encodedPassword
        );

        if (!matched) {
            throw new CustomException(
                    UserErrorCode.USER_PASSWORD_MISMATCH
            );
        }
    }

    //진행중인 배송이 있다면 탈퇴 불가능.
    private void validateNoActiveTasks(User user) {
        /*
         * TODO
         *
         * HUB_DELIVERY_MANAGER / COMPANY_DELIVERY_MANAGER
         * → 진행 중 배송 확인
         *
         * COMPANY_MANAGER
         * → 업체의 유일한 관리자 여부 확인
         *
         * HUB_MANAGER
         * → 담당 업무 이전 여부 확인
         */
    }






    /**
     * 사용자를 논리 삭제
     * deletedBy는 삭제를 수행한 사용자의 내부 PK다
     */
    public void delete(Long userId, Long deletedBy) {
        User user = userCommandRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(
                        () -> new CustomException(
                                UserErrorCode.USER_NOT_FOUND
                        )
                );

        user.markDeleted(deletedBy);
    }
}