package com.logistics.user.application.service;

import com.logistics.user.application.dto.command.ChangeUserAffiliationCommandDto;
import com.logistics.user.application.dto.result.ChangeUserAffiliationResultDto;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 내부 서비스 요청을 통해 사용자의 소속 및 권한 변경
 *
 * - 대상 사용자 조회
 * - 승인 상태 확인
 * - 동일 정보 변경 방지
 * - User 도메인에 실제 변경 요청
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserAffiliationService {

    private final UserQueryRepository userQueryRepository;

    public ChangeUserAffiliationResultDto changeAffiliation(
            ChangeUserAffiliationCommandDto command
    ) {
        validateCommand(command);

        // 삭제되지 않은 사용자 조회
        User user = userQueryRepository
                .findByIdAndDeletedAtIsNull(command.userId())
                .orElseThrow(() -> new CustomException(
                        UserErrorCode.USER_NOT_FOUND
                ));

        // APPROVED 사용자만 수행 가능
        validateApproved(user);

        // 현재 상태와 완전히 동일한 요청 방지
        validateSameAffiliation(
                user,
                command
        );

        user.changeRoleAndAffiliation(
                command.role(),
                command.companyId(),
                command.hubId()
        );

        return ChangeUserAffiliationResultDto.from(user);
    }

    private void validateCommand(
            ChangeUserAffiliationCommandDto command
    ) {
        if (command == null
                || command.userId() == null
                || command.userId() <= 0) {

            throw new CustomException(
                    UserErrorCode.USER_AFFILIATION_INVALID
            );
        }
    }

    private void validateApproved(
            User user
    ) {
        if (user.getStatus() != UserStatus.APPROVED) {
            throw new CustomException(
                    UserErrorCode.USER_NOT_APPROVED
            );
        }
    }

    private void validateSameAffiliation(
            User user,
            ChangeUserAffiliationCommandDto command
    ) {
        boolean sameRole =
                user.getRole() == command.role();

        boolean sameCompany =
                Objects.equals(
                        user.getCompanyId(),
                        command.companyId()
                );

        boolean sameHub =
                Objects.equals(
                        user.getHubId(),
                        command.hubId()
                );

        if (sameRole && sameCompany && sameHub) {
            throw new CustomException(
                    UserErrorCode.USER_AFFILIATION_CONFLICT
            );
        }
    }
}