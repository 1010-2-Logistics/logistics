package com.logistics.user.application.service;

import com.logistics.user.application.dto.query.GetMyInfoQueryDto;
import com.logistics.user.application.dto.query.GetUserDetailQueryDto;
import com.logistics.user.application.dto.query.GetUserQueryDto;
import com.logistics.user.application.dto.query.SearchUserQueryDto;
import com.logistics.user.application.dto.result.InternalUserResultDto;
import com.logistics.user.application.dto.result.UserDetailResultDto;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // 사용자 정보 목록 조회
    public Page<User> search(
            SearchUserQueryDto query
    ) {
        // 조회 권한과 허브 범위 검증
        validateSearchAccess(query);

        //HUB_MANAGER는 요청한 hubId가 아니라 본인 담당 허브만 가능하도록 조회 범위 제한
        UUID searchHubId =
                resolveSearchHubId(query);

        // page, size, sort, direction 정책 검증
        Pageable pageable =
                createPageable(query);

        return UserQueryRepository.search(
                query.username(),
                query.status(),
                query.role(),
                searchHubId,
                query.companyId(),
                pageable
        );
    }

    private Pageable createPageable(
            SearchUserQueryDto query
    ) {
        // 요청값이 음수인 page는 첫 페이지(0)로 보정
        int validatedPage =
                Math.max(query.page(), 0);
        // 허용된 size(10, 30, 50)만 사용하고 그 외에는 10으로 보정
        int validatedSize =
                resolvePageSize(query.size());
        // ASC만 명시적으로 허용하고 그 외 값은 DESC로 처리
        Sort.Direction sortDirection =
                "ASC".equalsIgnoreCase(query.direction())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return PageRequest.of(
                validatedPage,
                validatedSize,
                Sort.by(
                        sortDirection,
                        resolveSortField(query.sort())
                )
        );
    }

    //목록 조회에 허용된 페이지 크기 정의
    private int resolvePageSize(
            int size
    ) {
        return (size == 10 || size == 30 || size == 50)
                ? size
                : 10;
    }
    //API 명세에서 허용한 정렬 필드 정의
    private String resolveSortField(
            String sort
    ) {
        return switch (sort) {
            case "username" -> "username";
            case "status" -> "status";
            case "createdAt" -> "createdAt";
            default -> "createdAt";
        };
    }

    //사용자 목록 조회 권한과 HUB_MANAGER의 허브 접근 범위 검증
    private void validateSearchAccess(
            SearchUserQueryDto query
    ) {
        if (query.requesterRole() == UserRole.MASTER) {
            return;
        }

        if (query.requesterRole() != UserRole.HUB_MANAGER) {
            throw new CustomException(
                    UserErrorCode.USER_ACCESS_DENIED
            );
        }

        if (query.requesterHubId() == null) {
            throw new CustomException(
                    UserErrorCode.USER_ACCESS_DENIED
            );
        }

        if (query.hubId() != null
                && !query.requesterHubId()
                .equals(query.hubId())) {

            throw new CustomException(
                    UserErrorCode.USER_ACCESS_DENIED
            );
        }
    }

    private UUID resolveSearchHubId(
            SearchUserQueryDto query
    ) {
        if (query.requesterRole()
                == UserRole.HUB_MANAGER) {

            return query.requesterHubId();
        }

        return query.hubId();
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

    @Transactional(readOnly = true)
    public InternalUserResultDto getInternalUser(
            Long userId
    ) {
        if (userId == null || userId <= 0) {
            throw new CustomException(
                    UserErrorCode.USER_INVALID_REQUEST
            );
        }

        User user = UserQueryRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(
                        UserErrorCode.USER_NOT_FOUND
                ));

        return InternalUserResultDto.from(user);
    }
}
