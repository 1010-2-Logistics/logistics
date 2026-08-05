package com.logistics.slack.application.service;

import com.logistics.slack.application.dto.command.CreateSampleCommand;
import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.command.UpdateSampleCommand;
import com.logistics.slack.application.dto.result.SlackCreatResult;
import com.logistics.slack.application.event.SampleCreatedEvent;
import com.logistics.slack.application.port.EventPublisher;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.repository.SlackCommandRepository;
import com.logistics.slack.global.exception.CustomException;
import com.logistics.slack.global.exception.SlackErrorCode;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SampleCommandService {
    private final SlackCommandRepository slackCommandRepository;
    private final EventPublisher eventPublisher;

    public SlackCreatResult createSlack(SlackCreateCommand slackCreateCommand) {
        return null;
    }

    public void updateSlack(UpdateSampleCommand command) {

    }

    public void deleteSlack(UUID sampleId, String deletedBy) {

    }
}
