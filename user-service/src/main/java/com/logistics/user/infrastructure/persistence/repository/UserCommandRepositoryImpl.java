package com.logistics.user.infrastructure.persistence.repository;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.repository.UserCommandRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCommandRepositoryImpl implements UserCommandRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User User) {
        return jpaRepository.save(User);
    }

    @Override
    public Optional<User> findByIdAndDeletedAtIsNull(Long userId) {
        return jpaRepository.findByUserIdAndDeletedAtIsNull(userId);
    }
}
