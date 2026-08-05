package com.logistics.template.application.service;

import com.logistics.template.application.dto.command.CreateSampleCommand;
import com.logistics.template.application.dto.command.UpdateSampleCommand;
import com.logistics.template.application.event.SampleCreatedEvent;
import com.logistics.template.application.port.EventPublisher;
import com.logistics.template.domain.entity.Sample;
import com.logistics.template.domain.repository.SampleCommandRepository;
import com.logistics.template.global.exception.CustomException;
import com.logistics.template.global.exception.SampleErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SampleCommandService {

    private final SampleCommandRepository sampleCommandRepository;
    private final EventPublisher eventPublisher;

    public UUID create(CreateSampleCommand command) {
        Sample sample = Sample.create(command.name());
        sampleCommandRepository.save(sample);
        eventPublisher.publish(new SampleCreatedEvent(sample.getSampleId(), sample.getName()));
        return sample.getSampleId();
    }

    public void update(UpdateSampleCommand command) {
        Sample sample = sampleCommandRepository.findByIdAndDeletedAtIsNull(command.sampleId())
                .orElseThrow(() -> new CustomException(SampleErrorCode.SAMPLE_NOT_FOUND));
        sample.update(command.name());
    }

    public void delete(UUID sampleId, String deletedBy) {
        Sample sample = sampleCommandRepository.findByIdAndDeletedAtIsNull(sampleId)
                .orElseThrow(() -> new CustomException(SampleErrorCode.SAMPLE_NOT_FOUND));
        sample.markDeleted(deletedBy);
    }
}
