package com.logistics.user.domain.entity;

import com.logistics.user.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 계정, 가입 승인 상태, 권한, 소속 정보를 관리하는 엔티티
 *
 * 주요 책임:
 * - 사용자 계정 정보 보유
 * - 가입 승인 상태 변경
 * - 역할과 소속 정보의 일관성 유지
 * - 비밀번호 및 Slack ID 변경
 * - 논리 삭제 여부는 BaseEntity를 통해 관리
 */
@Getter
@Entity
@Table(name = "p_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    /**
     * 사용자 내부 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    /**
     * 로그인에 사용하는 사용자 아이디.
     *
     * 요구사항:
     * - 4자 이상 10자 이하
     * - 영문 소문자와 숫자로 구성
     *
     * 형식 검증은 요청 DTO와 애플리케이션 계층에서 수행
     */
    @Column(
            name = "username",
            nullable = false,
            length = 10,
            unique = true
    )
    private String username;

    /**
     * BCrypt로 암호화된 비밀번호 해시값.
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Slack 사용자 식별자.
     */
    @Column(
            name = "slack_id",
            nullable = false,
            length = 255,
            unique = true
    )
    private String slackId;

    /**
     * 승인 상태.
     *
     * Enum의 순서가 아닌 문자열 이름을 DB에 저장한다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    /**
     * 시스템 내 사용자 권한.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    /**
     * 업체 소속 사용자가 참조하는 업체 ID.
     *
     * Company Service와 물리 FK를 연결하지 않고
     * UUID 값만 논리적으로
     */
    @Column(name = "company_id")
    private UUID companyId;

    /**
     * 허브 소속 사용자가 참조하는 허브 ID.
     *
     * Hub Service와 물리 FK를 연결하지 않는다.
     */
    @Column(name = "hub_id")
    private UUID hubId;

    /**
     * 회원가입 요청을 기반으로 사용자를 생성한다.
     *
     * 신규 사용자는 관리자 승인 전이므로 PENDING 상태로 시작한다.
     */
    public static User create(
            String username,
            String encodedPassword,
            String slackId,
            UserRole role,
            UUID companyId,
            UUID hubId
    ) {
        validateRequiredFields(
                username,
                encodedPassword,
                slackId,
                role
        );

        validateAffiliation(
                role,
                companyId,
                hubId
        );

        User user = new User();
        user.username = username;
        user.password = encodedPassword;
        user.slackId = slackId;
        user.status = UserStatus.PENDING;
        user.role = role;
        user.companyId = companyId;
        user.hubId = hubId;

        return user;
    }

    /**
     * 사용자의 Slack ID를 변경
     *
     * username은 로그인 식별자이므로 일반 수정 대상에서 제외
     */
    public void updateSlackId(String slackId) {
        if (slackId == null || slackId.isBlank()) {
            throw new IllegalArgumentException(
                    "Slack ID는 비어 있을 수 없습니다."
            );
        }

        this.slackId = slackId;
    }

    /**
     * 암호화된 새 비밀번호로 변경한다.
     *
     * 이 메서드에는 평문이 아닌 BCrypt 처리된 값을 전달
     */
    public void changePassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "암호화된 비밀번호는 비어 있을 수 없습니다."
            );
        }

        this.password = encodedPassword;
    }

    /**
     * 가입 요청 승인
     */
    public void approve() {
        validateNotSameStatus(UserStatus.APPROVED);
        this.status = UserStatus.APPROVED;
    }

    /**
     * 가입 요청 거절
     */
    public void reject() {
        validateNotSameStatus(UserStatus.REJECTED);
        this.status = UserStatus.REJECTED;
    }

    /**
     * 사용자의 역할과 소속 정보를 함께 변경한다.
     *
     * 역할과 소속은 서로 연관된 값이므로 따로 변경하지 않고
     * 하나의 메서드에서 원자적으로 변경한다.
     */
    public void changeRoleAndAffiliation(
            UserRole role,
            UUID companyId,
            UUID hubId
    ) {
        Objects.requireNonNull(
                role,
                "사용자 역할은 필수입니다."
        );

        validateAffiliation(
                role,
                companyId,
                hubId
        );

        this.role = role;
        this.companyId = companyId;
        this.hubId = hubId;
    }

    private static void validateRequiredFields(
            String username,
            String encodedPassword,
            String slackId,
            UserRole role
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "username은 비어 있을 수 없습니다."
            );
        }

        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "비밀번호는 비어 있을 수 없습니다."
            );
        }

        if (slackId == null || slackId.isBlank()) {
            throw new IllegalArgumentException(
                    "Slack ID는 비어 있을 수 없습니다."
            );
        }

        Objects.requireNonNull(
                role,
                "사용자 역할은 필수입니다."
        );
    }

    /**
     * 역할과 소속 정보가 일치하는지 검증
     *
     * MASTER:
     * - companyId 없음
     * - hubId 없음
     *
     * 허브 역할:
     * - hubId 필수
     * - companyId 없음
     *
     * 업체 역할:
     * - companyId 필수
     * - hubId 없음
     */
    private static void validateAffiliation(
            UserRole role,
            UUID companyId,
            UUID hubId
    ) {
        switch (role) {
            case MASTER -> {
                if (companyId != null || hubId != null) {
                    throw new IllegalArgumentException(
                            "MASTER는 업체 또는 허브에 소속될 수 없습니다."
                    );
                }
            }

            case HUB_MANAGER, HUB_DELIVERY_MANAGER -> {
                if (hubId == null || companyId != null) {
                    throw new IllegalArgumentException(
                            "허브 역할은 hubId만 가져야 합니다."
                    );
                }
            }

            case COMPANY_MANAGER, COMPANY_DELIVERY_MANAGER -> {
                if (companyId == null || hubId != null) {
                    throw new IllegalArgumentException(
                            "업체 역할은 companyId만 가져야 합니다."
                    );
                }
            }
        }
    }

    private void validateNotSameStatus(UserStatus targetStatus) {
        if (this.status == targetStatus) {
            throw new IllegalStateException(
                    "이미 " + targetStatus + " 상태입니다."
            );
        }
    }
}