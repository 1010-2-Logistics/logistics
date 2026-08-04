package com.logistics.user.domain.repository;

import com.logistics.user.domain.entity.User;
import java.util.Optional;

public interface UserCommandRepository {

    User save(User User);

    Optional<User> findByIdAndDeletedAtIsNull(Long UserId);
}
