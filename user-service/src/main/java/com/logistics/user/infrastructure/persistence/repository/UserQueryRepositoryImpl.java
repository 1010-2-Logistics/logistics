package com.logistics.user.infrastructure.persistence.repository;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserQueryRepository;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findByIdAndDeletedAtIsNull(Long userId) {
        return userJpaRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public Page<User> search(
            String username,
            UserStatus status,
            UserRole role,
            UUID hubId,
            UUID companyId,
            Pageable pageable
    ) {
        return userJpaRepository.search(
                username,
                status,
                role,
                hubId,
                companyId,
                pageable
        );
    }
    @Override
    public boolean existsByUsername(
            String username
    ) {
        return userJpaRepository
                .existsByUsernameAndDeletedAtIsNull(username);
    }

    @Override
    public boolean existsBySlackId(String slackId) {
        return userJpaRepository
                .existsBySlackIdAndDeletedAtIsNull(slackId);
    }

    @Override
    public Optional<User> findByUsername(
            String username
    ) {
        return userJpaRepository.findByUsername(username);
    }
}
