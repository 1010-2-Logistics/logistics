package com.logistics.user.application.service;

import com.logistics.user.application.dto.command.ChangeApprovalCommandDto;
import com.logistics.user.application.dto.result.ChangeApprovalResultDto;
import com.logistics.user.domain.entity.ApprovalDecision;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 1. 요청값 검증
 * 2. 처리 관리자 조회
 * 3. 대상 사용자 조회
 * 4. 관리자 권한과 담당 범위 검증
 * 5. User 도메인의 상태 변경 메서드 호출
 * 6. 처리 결과 반환
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserApprovalService {

    private final UserQueryRepository userQueryRepository;

    public ChangeApprovalResultDto changeApproval(
            ChangeApprovalCommandDto command
    ) {
        // 1. 서비스 계층에서도 필수 입력값을 검증
        validateCommand(command);

        // 2. 요청을 수행한 관리자 조회
        User processor = userQueryRepository
                .findByIdAndDeletedAtIsNull(
                        command.processedBy()
                )
                .orElseThrow(() -> new CustomException(
                        UserErrorCode.USER_APPROVAL_ACCESS_DENIED
                ));

        // 3. 승인 또는 거절 대상 사용자 조회
        User targetUser = userQueryRepository
                .findByIdAndDeletedAtIsNull(
                        command.targetUserId()
                )
                .orElseThrow(() -> new CustomException(
                        UserErrorCode.USER_NOT_FOUND
                ));

        // 4. 처리 관리자에게 대상 사용자를 처리할 권한이 있는지 확인
        validateApprovalAuthority(
                processor,
                targetUser
        );

        // 5. 상태 변경 전 값을 응답용으로 보관
        UserStatus previousStatus =
                targetUser.getStatus();

        // 6. User 도메인 객체에 상태 변경을 요청
        changeUserStatus(
                targetUser,
                command.decision()
        );

        // 7. 응답에 사용할 처리 시간 저장
        LocalDateTime processedAt =
                LocalDateTime.now();

        /*
         * 별도의 save() 호출없이 업데이트 됨.
         */
        return new ChangeApprovalResultDto(
                targetUser.getUserId(),
                previousStatus,
                targetUser.getStatus(),
                processor.getUserId(),
                processedAt
        );
    }

    /**
     * 필수 입력값 검증.
     */
    private void validateCommand(
            ChangeApprovalCommandDto command
    ) {
        if (command == null
                || command.targetUserId() == null
                || command.targetUserId() <= 0
                || command.processedBy() == null
                || command.processedBy() <= 0
                || command.decision() == null) {

            throw new CustomException(
                    UserErrorCode.USER_APPROVAL_INVALID_REQUEST
            );
        }
    }

    /**
     * 관리자의 역할과 담당 범위 검증한
     */
    private void validateApprovalAuthority(
            User processor,
            User targetUser
    ) {
        /*
         * MASTER는 모든 사용자의 가입 신청을 처리 가능
         */
        if (processor.getRole() == UserRole.MASTER) {
            return;
        }

        /*
         * MASTER가 아니라면 HUB_MANAGER만 처리할 수 있다.
         */
        if (processor.getRole() != UserRole.HUB_MANAGER) {
            throw new CustomException(
                    UserErrorCode.USER_APPROVAL_ACCESS_DENIED
            );
        }

        /*
         * HUB_MANAGER는 MASTER 가입 신청을 처리할 수 없다.
         */
        if (targetUser.getRole() == UserRole.MASTER) {
            throw new CustomException(
                    UserErrorCode.USER_APPROVAL_ACCESS_DENIED
            );
        }

        /*
         * HUB_MANAGER는 자신의 담당 허브 사용자만 처리할 수 있다.
         *
         * 처리 관리자와 대상 사용자 모두 hubId가 있어야 하며,
         * 두 hubId가 동일해야 함.
         */
        if (processor.getHubId() == null
                || targetUser.getHubId() == null
                || !processor.getHubId().equals(
                targetUser.getHubId()
        )) {

            throw new CustomException(
                    UserErrorCode.USER_APPROVAL_ACCESS_DENIED
            );
        }
    }

    /**
     * 승인 결정에 따라 User 도메인 메서드 호출
     */
    private void changeUserStatus(
            User targetUser,
            ApprovalDecision decision
    ) {
        if (decision == ApprovalDecision.APPROVE) {
            targetUser.approve();
            return;
        }

        targetUser.reject();
    }
}