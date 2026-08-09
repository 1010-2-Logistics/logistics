package com.logistics.slack.application.service;

import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.result.SlackCreateResult;
import com.logistics.slack.application.dto.result.SlackRetryResult;
import com.logistics.slack.application.event.SlackSendEvent;
import com.logistics.slack.application.port.SlackMessageSender;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.repository.SlackCommandRepository;

import java.util.UUID;

import com.logistics.slack.global.exception.CustomException;
import com.logistics.slack.global.exception.SlackErrorCode;
import com.logistics.slack.infrastructure.messaging.SlackEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class SlackCommandService {
    private static final int MAX_RETRY_COUNT = 3;
    private final SlackCommandRepository slackCommandRepository;
    private final SlackEventPublisher slackEventPublisher;
    private final SlackMessageSender slackMessageSender;

    public SlackCreateResult createSlack(
            SlackCreateCommand slackCreateCommand
    ) {
        Slack slack = Slack.create(
                slackCreateCommand.senderId(),
                slackCreateCommand.receiverId(),
                slackCreateCommand.message(),
                slackCreateCommand.referenceId()
        );
        slackCommandRepository.save(slack);

        // DB 커밋 전에 RabbitMQ 이벤트가 소비되는 문제 방지를 위해
        // 트랜잭션 커밋 이후 이벤트 발행
        publishAfterCommit(slack.getSlackMessageId());
        // 이 시점의 slack.status는 아직 PENDING!
        return SlackCreateResult.from(slack);
    }

    public void send(
            UUID slackMessageId
    ) {
        Slack slack = slackCommandRepository
                .findByIdAndDeletedAtIsNull(slackMessageId)
                .orElseThrow();

        try {
            slackMessageSender.send(slack.getMessage());
            slack.markSuccess();

        } catch (Exception e) {
            slack.markFailed(e.getMessage());
        }
    }

    public SlackRetryResult retrySlack(
            UUID slackMessageId
    ) {
        Slack slack = slackCommandRepository.findById(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_NOT_FOUND));

        slack.retry(MAX_RETRY_COUNT);

        publishAfterCommit(slack.getSlackMessageId());

        return SlackRetryResult.from(slack);
    }

    public void deleteSlack(
            UUID slackMessageId,
            Long deletedBy
    ) {
        Slack slack = slackCommandRepository.findByIdAndDeletedAtIsNull(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_NOT_FOUND));

        slack.markDeleted(deletedBy);
    }

    // 스프링이 제공하는 트랜잭션 유틸 클래스 친구들
    // 1. TransactionSynchronization
    // 2. TransactionSynchronizationManager
    // -> 지금 트랜잭션이 진짜 커밋된 뒤에 이 코드 실행해라고 예약하는 기능
    // afterCommit() 안에 래빗엠큐 발행을 넣어서 "DB 저장완료 그 다음에 래빗엠큐 발행" 이 순서를 보장한다
    // 결과 : 성공적!
    // TroubleShooting01 - 결국 원인은 리스너가 너무 빨리 실행되면서 아직 커밋되지 않은 이전 데이터를 읽을 수 있던 흐름이 문제

    // 그런데 유틸 클래스인데, service에 있는 게 맞을까?
    // -> 범용 유틸이 아니라 커밋 후 발행 흐름이라 괜찮다고 판단된다
    private void publishAfterCommit(UUID slackMessageId) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        slackEventPublisher.publish(
                                new SlackSendEvent(slackMessageId)
                        );
                    }
                }
        );
    }
}
