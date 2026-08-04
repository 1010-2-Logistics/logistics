package com.logistics.user.domain.entity;

/**
 * 물류 시스템 내 사용자의 역할을 정의한다.
 */
public enum UserRole {

    /**
     * 전체 시스템을 관리하는 최고 관리자
     */
    MASTER,

    /**
     * 특정 허브를 관리
     */
    HUB_MANAGER,

    /**
     * 허브 간 배송을 담당
     */
    HUB_DELIVERY_MANAGER,

    /**
     * 특정 업체를 관리
     */
    COMPANY_MANAGER,

    /**
     * 허브에서 업체까지의 배송을 담당
     */
    COMPANY_DELIVERY_MANAGER
}