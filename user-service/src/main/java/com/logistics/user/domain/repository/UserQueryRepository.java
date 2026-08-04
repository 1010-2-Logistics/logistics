package com.logistics.user.domain.repository;

import com.logistics.user.domain.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQueryRepository {

    Optional<User> findByIdAndDeletedAtIsNull(Long userId);

    Page<User> search(String keyword, Pageable pageable);

    /**
     * 삭제되지 않은 사용자 중 username 중복 여부 확인
     */
    boolean existsByUsernameAndDeletedAtIsNull(String username);

    /**
     * 삭제되지 않은 사용자 중 Slack ID 중복 여부 확인
     */
    boolean existsBySlackIdAndDeletedAtIsNull(String slackId);
}
