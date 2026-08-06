package com.logistics.user.application.service;

import com.logistics.user.application.dto.command.CreateUserCommandDto;
import com.logistics.user.application.dto.command.UpdateUserCommandDto;
import com.logistics.user.application.event.UserCreatedEvent;
import com.logistics.user.application.port.EventPublisher;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.repository.UserCommandRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserCommandRepository userCommandRepository;
    private final EventPublisher eventPublisher;

    /**
     * 사용자 생성
     * 현재 command.encodedPassword()는 이름상 암호화된 값
     * 실제 회원가입 구현 시 반드시 BCrypt 처리된 값이 전달되어야 한다.
     */
    public Long create(CreateUserCommandDto command) {
        User user = User.create(
                command.username(),
                command.encodedPassword(),
                command.slackId(),
                command.role(),
                command.companyId(),
                command.hubId()
        );

        userCommandRepository.save(user);

        eventPublisher.publish(
                new UserCreatedEvent(
                        user.getUserId(),
                        user.getUsername(),
                        user.getRole(),
                        user.getStatus()
                )
        );

        return user.getUserId();
    }

    /**
     * 현재 구조에서는 Slack ID만 수정
     */
    public void update(UpdateUserCommandDto command) {
        User user = userCommandRepository
                .findByIdAndDeletedAtIsNull(command.userId())
                .orElseThrow(
                        () -> new CustomException(
                                UserErrorCode.USER_NOT_FOUND
                        )
                );

        user.updateSlackId(command.slackId());
    }

    /**
     * 사용자를 논리 삭제
     * deletedBy는 삭제를 수행한 사용자의 내부 PK다
     */
    public void delete(Long userId, Long deletedBy) {
        User user = userCommandRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(
                        () -> new CustomException(
                                UserErrorCode.USER_NOT_FOUND
                        )
                );

        user.markDeleted(deletedBy);
    }
}