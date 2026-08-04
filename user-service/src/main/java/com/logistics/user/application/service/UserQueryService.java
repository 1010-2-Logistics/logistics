package com.logistics.user.application.service;

import com.logistics.user.application.dto.query.GetUserQuery;
import com.logistics.user.application.dto.query.SearchUserQuery;
import com.logistics.user.domain.entity.User;
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

    public User get(GetUserQuery query) {
        return UserQueryRepository.findByIdAndDeletedAtIsNull(query.UserId())
                .orElseThrow(
                        () -> new CustomException(
                                UserErrorCode.USER_NOT_FOUND
                        )
                );
    }

    public Page<User> search(SearchUserQuery query) {
        return UserQueryRepository.search(query.keyword(), query.pageable());
    }
}
