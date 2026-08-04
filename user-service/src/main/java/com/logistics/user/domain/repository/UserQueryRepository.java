package com.logistics.user.domain.repository;

import com.logistics.user.domain.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQueryRepository {

    Optional<User> findByIdAndDeletedAtIsNull(Long userId);

    Page<User> search(String keyword, Pageable pageable);
}
