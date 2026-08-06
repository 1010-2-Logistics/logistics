package com.logistics.user.domain.entity;

/**
 * 관리자가 가입 신청에 내릴 수 있는 선택지
 *
 * APPROVE:
 * - User 상태를 APPROVED로 변경
 *
 * REJECT:
 * - User 상태를 REJECTED로 변경
 */
public enum ApprovalDecision {
    APPROVE,
    REJECT
}