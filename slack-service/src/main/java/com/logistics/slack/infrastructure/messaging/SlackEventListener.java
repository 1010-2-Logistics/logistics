package com.logistics.slack.infrastructure.messaging;


import com.logistics.slack.application.event.SlackSendEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SlackEventListener {
    private final SlackEventPublisher slackEventPublisher;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            SlackSendEvent slackSendEvent
    ) {
        slackEventPublisher.publish(slackSendEvent);
    }
}
