package com.logistics.ai.infrastructure.adapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.result.DispatchDeadlineResultDto;
import com.logistics.ai.application.port.out.DispatchDeadlineGenerationPort;
import com.logistics.ai.infrastructure.feign.client.GeminiClient;
import com.logistics.ai.infrastructure.feign.request.AiRequestDto;
import com.logistics.ai.infrastructure.feign.response.AiResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DispatchDeadlineGenerationAdapter implements DispatchDeadlineGenerationPort {
	
	private static final Pattern DATE_TIME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}(:\\d{2})?");
	
	private final GeminiClient geminiClient;

	@Override
	public DispatchDeadlineResultDto generate(String requestPrompt, String aiModel) {
		AiRequestDto request = AiRequestDto.from(requestPrompt);
		
		AiResponseDto response = geminiClient.generateText(aiModel, request);
		
		String responsePrompt = response.getText();
		
		LocalDateTime finalDeadline = parseDeadline(responsePrompt);
		
		return DispatchDeadlineResultDto.of(responsePrompt, finalDeadline);
	}

	private LocalDateTime parseDeadline(String text) {
		if (text == null || text.isBlank()) {
      return null;
	  }
		
	  Matcher matcher = DATE_TIME_PATTERN.matcher(text);
	  if (matcher.find()) {
	      String dateTimeStr = matcher.group().replace("T", " ");
	      
	      if (dateTimeStr.length() == 16) {
	          return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	      } else if (dateTimeStr.length() == 19) {
	          return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	      }
	  }
	  
	  return null;
	}
	
}
