package com.logistics.user.infrastructure.persistence.repository;

import com.logistics.user.domain.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserIdAndDeletedAtIsNull(Long UserId);

    @Query("SELECT s FROM User s WHERE s.deletedAt IS NULL "
            + "AND (:keyword IS NULL OR s.username LIKE %:keyword%)")
    Page<User> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByUsernameAndDeletedAtIsNull(String username);

    boolean existsBySlackIdAndDeletedAtIsNull(
            String slackId
    );

    Optional<User> findByUsername(String username);
}
