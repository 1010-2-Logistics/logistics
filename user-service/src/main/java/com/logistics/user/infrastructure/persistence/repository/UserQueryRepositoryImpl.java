package com.logistics.user.infrastructure.persistence.repository;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.repository.UserQueryRepository;
import java.util.Optional;
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
    public Page<User> search(String keyword, Pageable pageable) {
        return userJpaRepository.search(keyword, pageable);
    }

    @Override
    public boolean existsByUsername(
            String username
    ) {
        return userJpaRepository
                .existsByUsername(username);
    }

    @Override
    public boolean existsBySlackId(
            String slackId
    ) {
        return userJpaRepository
                .existsBySlackId(slackId);
    }

    @Override
    public Optional<User> findByUsername(
            String username
    ) {
        return userJpaRepository.findByUsername(username);
    }
}
