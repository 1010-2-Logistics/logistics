package com.logistics.slack.application.service;

import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.result.SlackCreateResult;
import com.logistics.slack.application.port.SlackMessageSender;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.repository.SlackCommandRepository;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SlackCommandService {
    private final SlackCommandRepository slackCommandRepository;
    private final SlackMessageSender slackMessageSender;

    public SlackCreateResult createSlack(
            Long senderId,
            SlackCreateCommand slackCreateCommand
    ) {
        Slack slack = Slack.create(
                senderId,
                slackCreateCommand.receiverId(),
                slackCreateCommand.message(),
                slackCreateCommand.referenceId()
        );
        slackCommandRepository.save(slack);

        try {
            slackMessageSender.send(slack.getMessage());
            slack.markSuccess();

        } catch (Exception e) {
            slack.markFailed(e.getMessage());
        }

        return SlackCreateResult.from(slack);
    }

    public void deleteSlack(UUID sampleId, String deletedBy) {

    }
}
