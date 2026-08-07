package com.logistics.user.application.service;

import com.logistics.user.application.dto.query.GetMyInfoQueryDto;
import com.logistics.user.application.dto.query.GetUserDetailQueryDto;
import com.logistics.user.application.dto.query.GetUserQueryDto;
import com.logistics.user.application.dto.query.SearchUserQueryDto;
import com.logistics.user.application.dto.result.UserDetailResultDto;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserQueryRepository UserQueryRepository;

    public User get(GetUserQueryDto query) {
        return UserQueryRepository.findByIdAndDeletedAtIsNull(query.UserId())
                .orElseThrow(
                        () -> new CustomException(
                                UserErrorCode.USER_NOT_FOUND
                        )
                );
    }

    public Page<User> search(SearchUserQueryDto query) {
        return UserQueryRepository.search(query.keyword(), query.pageable());
    }

    /**
     * 로그인한 사용자의 정보를 조회한다.
     */
    public UserDetailResultDto getMyInfo(
            GetMyInfoQueryDto query
    ) {
        validateQuery(query);

        /*
         * 삭제된 사용자는 Repository 조회 단계에서 제외된다.
         *
         * 존재하지 않는 사용자와 삭제된 사용자 모두
         * USER_NOT_FOUND로 처리된다.
         */
        User user = UserQueryRepository
                .findByIdAndDeletedAtIsNull(query.userId())
                .orElseThrow(() -> new CustomException(
                        UserErrorCode.USER_NOT_FOUND
                ));

        validateApproved(user);

        return UserDetailResultDto.from(user);
    }

    /**
     * Application 계층으로 전달된 입력값을 방어적으로 검증한다.
     */
    private void validateQuery(
            GetMyInfoQueryDto query
    ) {
        if (query == null
                || query.userId() == null
                || query.userId() <= 0) {

            throw new CustomException(
                    UserErrorCode.USER_NOT_FOUND
            );
        }
    }

    /**
     * 명세상 승인된 사용자만 자신의 정보를 조회할 수 있다.
     */
    private void validateApproved(User user) {
        if (user.getStatus() != UserStatus.APPROVED) {
            throw new CustomException(
                    UserErrorCode.USER_NOT_APPROVED
            );
        }
    }

    /**
     * 관리자가 사용자 상세 정보를 조회하는 메서드
     */
    public UserDetailResultDto getUserDetail(
            GetUserDetailQueryDto query
    ) {
        validateGetUserDetailQuery(query);

        User targetUser = UserQueryRepository
                .findByIdAndDeletedAtIsNull(query.targetUserId())
                .orElseThrow(() -> new CustomException(
                        UserErrorCode.USER_NOT_FOUND
                ));

        validateDetailAccess(
                query.requesterRole(),
                query.requesterHubId(),
                targetUser
        );

        return UserDetailResultDto.from(targetUser);
    }

    // 입력값 검증
    private void validateGetUserDetailQuery(
            GetUserDetailQueryDto query
    ) {
        if (query == null
                || query.requesterId() == null
                || query.requesterRole() == null
                || query.targetUserId() == null
                || query.targetUserId() <= 0) {

            throw new CustomException(
                    UserErrorCode.USER_INVALID_REQUEST
            );
        }
    }

    //권한 검증
    private void validateDetailAccess(
            UserRole requesterRole,
            UUID requesterHubId,
            User targetUser
    ) {
        //마스터인가?
        if (requesterRole == UserRole.MASTER) {
            return;
        }
        //허브 매니저인가?
        if (requesterRole != UserRole.HUB_MANAGER) {
            throw new CustomException(
                    UserErrorCode.USER_ACCESS_DENIED
            );
        }
        //요청자가 허브 id를 가지고 있는가?
        //조회하려는 사용자가 hubId를 가지고 있는가?
        //허브가 같은 사용자의 정보를 조회하려 하는가?
        if (requesterHubId == null
                || targetUser.getHubId() == null
                || !requesterHubId.equals(targetUser.getHubId())) {

            throw new CustomException(
                    UserErrorCode.USER_ACCESS_DENIED
            );
        }
    }
}
