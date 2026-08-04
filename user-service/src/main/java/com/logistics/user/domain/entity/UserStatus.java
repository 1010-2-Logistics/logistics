package com.logistics.user.domain.entity;

/**
 * 사용자의 가입 승인 상태다.
 */
public enum UserStatus {

    /**
     * 회원가입은 완료했지만 관리자의 승인을 기다리는 상태
     * 현재 정책상 로그인은 가능하지만
     * 다른 비즈니스 서비스 이용은 제한.
     */
    PENDING,

    /**
     * 관리자의 승인이 완료된 상태다.
     */
    APPROVED,

    /**
     * 관리자가 회원가입 요청을 거절한 상태
     * 다른 비즈니스 서비스 이용은 제한한다.
     */
    REJECTED
}