package com.logistics.user.infrastructure.persistence.repository;

import com.logistics.user.domain.entity.User;
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

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findByIdAndDeletedAtIsNull(Long userId) {
        return jpaRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public Page<User> search(String keyword, Pageable pageable) {
        return jpaRepository.search(keyword, pageable);
    }
}
