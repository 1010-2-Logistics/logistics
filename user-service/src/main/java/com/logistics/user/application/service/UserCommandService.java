package com.logistics.user.application.service;

import com.logistics.user.application.dto.command.CreateUserCommandDto;
import com.logistics.user.application.dto.command.UpdateMySlackIdCommandDto;
import com.logistics.user.application.dto.command.UpdateUserCommandDto;
import com.logistics.user.application.dto.result.UpdateMyInfoResultDto;
import com.logistics.user.application.event.UserCreatedEvent;
import com.logistics.user.application.port.EventPublisher;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.repository.UserCommandRepository;
import com.logistics.user.domain.repository.UserQueryRepository;
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
    private final UserQueryRepository userQueryRepository;

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

    public UpdateMyInfoResultDto updateMySlackId(
            UpdateMySlackIdCommandDto command
    ) {
        /*
         * 삭제된 사용자는 조회 대상에서 제외한다.
         * 존재하지 않거나 삭제된 경우 모두 USER_NOT_FOUND.
         */
        User user = userCommandRepository
                .findByIdAndDeletedAtIsNull(command.userId())
                .orElseThrow(() -> new CustomException(
                        UserErrorCode.USER_NOT_FOUND
                ));

        /*
         * 현재 Slack ID와 동일한 값인지 먼저 확인한다.
         */
        if (user.getSlackId().equals(command.slackId())) {
            throw new CustomException(
                    UserErrorCode.USER_SAME_SLACK_ID_CONFLICT
            );
        }

        /*
         * 다른 활성 사용자가 이미 사용 중인지 확인한다.
         */
        if (userQueryRepository.existsBySlackId(
                command.slackId()
        )) {
            throw new CustomException(
                    UserErrorCode.USER_SLACK_ID_CONFLICT
            );
        }

        /*
         * User 객체가 자신의 상태를 직접 변경한다.
         */
        user.updateSlackId(command.slackId());

        return UpdateMyInfoResultDto.from(user);
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