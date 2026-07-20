package com.davendra.calistrack_backend.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OnboardingAnswersRequest(
        @NotNull(message = "is required")
        @Schema(description = "Muscle up goal id",
                example = "22222222-2222-2222-2222-222222220010")
        UUID goalNodeId,

        @Schema(
                description = "List of onboarding question responses",
                example = """
                        [
                          {
                            "nodeId": "22222222-2222-2222-2222-222222220001",
                            "type": "REPS",
                            "value": "30"
                          },
                          {
                            "nodeId": "22222222-2222-2222-2222-222222220005",
                            "type": "YES_NO",
                            "value": true
                          },
                          {
                            "nodeId": "22222222-2222-2222-2222-222222220006",
                            "type": "YES_NO",
                            "value": true
                          }
                        ]
                        """
        )
        List<OnboardingAnswerDto> answers
) {
}
