package com.logistics.ai.infrastructure.feign.response;

import java.util.List;

public record AiResponseDto(
		List<Candidate> candidates
) {
	public record Candidate(
      Content content,
      String finishReason
  ) {}
	
	public record Content(
      List<Part> parts,
      String role
  ) {}

  public record Part(
      String text
  ) {}
  
  public String getText() {
    if (candidates != null && !candidates.isEmpty()) {
        Candidate candidate = candidates.get(0);
        if (candidate.content() != null && candidate.content().parts() != null && !candidate.content().parts().isEmpty()) {
            return candidate.content().parts().get(0).text();
        }
    }
	  return "";
	}
}
