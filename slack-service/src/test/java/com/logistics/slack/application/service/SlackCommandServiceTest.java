package com.logistics.slack.application.service;

import com.logistics.slack.application.authorization.SlackAuthorizationService;
import com.logistics.slack.application.dto.auth.AuthenticatedUser;
import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.result.SlackRetryResult;
import com.logistics.slack.application.event.SlackSendEvent;
import com.logistics.slack.application.port.SlackMessageSender;
import com.logistics.slack.domain.entity.Role;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.entity.SlackStatus;
import com.logistics.slack.domain.repository.SlackCommandRepository;
import com.logistics.slack.global.exception.CustomException;
import com.logistics.slack.infrastructure.messaging.SlackEventPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class SlackCommandServiceTest {
    private final UUID slackMessageId = UUID.randomUUID();
    private final Long senderId = 1L;
    private final Long receiverId = 2L;
    private final UUID referenceId = UUID.randomUUID();

    private final String receiverSlackId = "U123456789";
    private final AuthenticatedUser authenticatedUser = new AuthenticatedUser(
            senderId,
            Role.MASTER,
            null,
            null
    );

    @Mock
    private SlackCommandRepository slackCommandRepository;

    @Mock
    private SlackEventPublisher slackEventPublisher;

    @Mock
    private SlackMessageSender slackMessageSender;

    @InjectMocks
    private SlackCommandService slackCommandService;

    @Mock
    private SlackAuthorizationService slackAuthorizationService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private Slack createFailedSlack() {
        Slack slack = crateSlack();
        slack.markFailed("최초 발송 실패");

        // ReflectionTestUtils : Slack.create()는 slackMessageId를 설정하지 않기때문에
        ReflectionTestUtils.setField(
                slack,
                "slackMessageId",
                slackMessageId
        );

        return slack;
    }

    private Slack crateSlack() {
        return Slack.create(
                senderId,
                receiverId,
                "주문이 생성되었습니다.",
                referenceId
        );
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Nested
    @DisplayName("슬랙 메시지 생성")
    class slack_create {
        @Test
        @DisplayName("슬랙 메시지를 생성하고 발송 이벤트를 발행")
        void slack_create_success() {
            SlackCreateCommand slackCreateCommand = new SlackCreateCommand(
                    senderId,
                    receiverId,
                    "주문이 생성되었습니다.",
                    referenceId
            );
            TransactionSynchronizationManager.initSynchronization();

            slackCommandService.createSlack(slackCreateCommand, authenticatedUser);
            ArgumentCaptor<Slack> slackCaptor = ArgumentCaptor.forClass(Slack.class);
            verify(slackCommandRepository).save(slackCaptor.capture());
            Slack savedSlack = slackCaptor.getValue();

            assertThat(savedSlack.getSenderId()).isEqualTo(senderId);
            assertThat(savedSlack.getReceiverId()).isEqualTo(receiverId);
            assertThat(savedSlack.getMessage()).isEqualTo("주문이 생성되었습니다.");
            assertThat(savedSlack.getReferenceId()).isEqualTo(referenceId);
            assertThat(savedSlack.getRetryCount()).isZero();
            assertThat(savedSlack.getStatus()).isEqualTo(SlackStatus.PENDING);

            verify(slackEventPublisher, never()).publish(any(SlackSendEvent.class));
        }
    }

    @Nested
    @DisplayName("슬랙 메시지 발송")
    class slack_send {
        @Test
        @DisplayName("Webhook 발송에 성공하면 상태가 SUCCESS로 변경")
        void slack_send_success() {
            Slack slack = createFailedSlack();

            given(slackCommandRepository.findByIdAndDeletedAtIsNull(slackMessageId)).willReturn(Optional.of(slack));
            slackCommandService.send(
                    slackMessageId,
                    receiverSlackId
            );
            verify(slackMessageSender).send(
                    receiverSlackId,
                    "주문이 생성되었습니다."
            );

            assertThat(slack.getStatus()).isEqualTo(SlackStatus.SUCCESS);
            assertThat(slack.getSentAt()).isNotNull();
            assertThat(slack.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("Webhook 발송에 실패하면 상태가 FAILED로 변경")
        void slack_send_failure() {
            Slack slack = createFailedSlack();

            given(slackCommandRepository.findByIdAndDeletedAtIsNull(slackMessageId)).willReturn(Optional.of(slack));
            willThrow(new RuntimeException("Webhook 발송 실패")).given(slackMessageSender).send(
                    receiverSlackId,
                    "주문이 생성되었습니다."
            );
            slackCommandService.send(
                    slackMessageId,
                    receiverSlackId
            );

            verify(slackMessageSender).send(
                    receiverSlackId,
                    "주문이 생성되었습니다."
            );

            assertThat(slack.getStatus()).isEqualTo(SlackStatus.FAILED);
            assertThat(slack.getErrorMessage()).isEqualTo("Webhook 발송 실패");
        }

        @Test
        @DisplayName("존재하지 않는 슬랙 메시지를 발송하면 예외")
        void slack_send_not_found() {
            given(slackCommandRepository.findByIdAndDeletedAtIsNull(slackMessageId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> slackCommandService.send(
                    slackMessageId,
                    receiverSlackId
            ))
                    .isInstanceOf(Exception.class);

            verify(slackMessageSender, never()).send(
                    any(String.class),
                    any(String.class)
            );
        }
    }


    @Nested
    @DisplayName("슬랙 메시지 재발송")
    class slack_retry {
        @Test
        @DisplayName("재발송 요청 시 횟수와 상태 변경")
        void slack_retry_success() {
            Slack slack = createFailedSlack();
            given(slackCommandRepository.findByIdAndDeletedAtIsNull(slackMessageId)).willReturn(Optional.of(slack));
            TransactionSynchronizationManager.initSynchronization();
            SlackRetryResult result = slackCommandService.retrySlack(
                    slackMessageId,
                    authenticatedUser
            );
            verify(slackEventPublisher, never()).publish(any(SlackSendEvent.class));
            assertThat(slack.getRetryCount()).isEqualTo(1);
            assertThat(slack.getStatus()).isEqualTo(SlackStatus.PENDING);
            assertThat(slack.getErrorMessage()).isNull();
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 슬랙 메시지를 재발송하면 예외")
        void slack_retry_not_found() {
            given(slackCommandRepository.findByIdAndDeletedAtIsNull(slackMessageId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> slackCommandService.retrySlack(
                    slackMessageId,
                    authenticatedUser
            )).isInstanceOf(CustomException.class);

            verify(slackEventPublisher, never()).publish(any(SlackSendEvent.class));
        }

        @Test
        @DisplayName("최대 재시도 횟수를 초과하면 예외")
        void slack_retry_max_count() {
            Slack slack = createFailedSlack();
            slack.retry(3);
            slack.markFailed("1회 재발송 실패");
            slack.retry(3);
            slack.markFailed("2회 재발송 실패");
            slack.retry(3);
            slack.markFailed("3회 재발송 실패");

            given(slackCommandRepository.findByIdAndDeletedAtIsNull(slackMessageId)).willReturn(Optional.of(slack));

            TransactionSynchronizationManager.initSynchronization();

            assertThatThrownBy(() -> slackCommandService.retrySlack(
                    slackMessageId,
                    authenticatedUser
            ))
                    .isInstanceOf(CustomException.class);

            assertThat(slack.getRetryCount()).isEqualTo(3);
            assertThat(slack.getStatus()).isEqualTo(SlackStatus.FAILED);

            verify(slackEventPublisher, never()).publish(any(SlackSendEvent.class));
        }
    }

    @Nested
    @DisplayName("슬랙 메시지 삭제")
    class slack_delete {
        @Test
        @DisplayName("슬랙 메시지 삭제 시 삭제 정보 기록")
        void slack_delete_success() {
            Slack slack = createFailedSlack();
            Long deletedBy = 1L;

            given(slackCommandRepository.findByIdAndDeletedAtIsNull(slackMessageId)).willReturn(Optional.of(slack));

            slackCommandService.deleteSlack(
                    slackMessageId,
                    authenticatedUser
            );

            assertThat(slack.getDeletedAt()).isNotNull();
            assertThat(slack.getDeletedBy()).isEqualTo(deletedBy);
        }

        @Test
        @DisplayName("존재하지 않는 메시지 삭제 시 예외")
        void slack_delete_not_found() {
            given(slackCommandRepository.findByIdAndDeletedAtIsNull(slackMessageId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> slackCommandService.deleteSlack(
                    slackMessageId,
                    authenticatedUser
            )).isInstanceOf(CustomException.class);
        }
    }
}