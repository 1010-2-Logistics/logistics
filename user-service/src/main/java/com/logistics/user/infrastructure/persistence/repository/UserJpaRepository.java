package com.logistics.user.infrastructure.persistence.repository;

import com.logistics.user.domain.entity.User;
import java.util.Optional;
import java.util.UUID;

import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserIdAndDeletedAtIsNull(Long UserId);

    @Query("""
    SELECT u
    FROM User u
    WHERE u.deletedAt IS NULL

      AND (
          :username IS NULL
          OR u.username LIKE %:username%
      )

      AND (
          :status IS NULL
          OR u.status = :status
      )

      AND (
          :role IS NULL
          OR u.role = :role
      )

      AND (
          :hubId IS NULL
          OR u.hubId = :hubId
      )

      AND (
          :companyId IS NULL
          OR u.companyId = :companyId
      )
""")
    Page<User> search(
            @Param("username")
            String username,

            @Param("status")
            UserStatus status,

            @Param("role")
            UserRole role,

            @Param("hubId")
            UUID hubId,

            @Param("companyId")
            UUID companyId,

            Pageable pageable
    );

    boolean existsByUsernameAndDeletedAtIsNull(String username);

    boolean existsBySlackIdAndDeletedAtIsNull(
            String slackId
    );

    Optional<User> findByUsername(String username);

    boolean existsByRoleAndDeletedAtIsNull(
            UserRole role
    );
}
