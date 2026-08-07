package com.logistics.user.application.service;

import com.logistics.user.application.dto.query.GetMyInfoQueryDto;
import com.logistics.user.application.dto.query.GetUserQueryDto;
import com.logistics.user.application.dto.query.SearchUserQueryDto;
import com.logistics.user.application.dto.result.UserDetailResultDto;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
