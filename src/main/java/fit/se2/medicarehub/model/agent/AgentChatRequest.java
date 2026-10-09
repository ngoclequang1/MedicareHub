package fit.se2.medicarehub.model.agent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgentChatRequest(
        @NotBlank @Size(max = 2000) String message
) {
}
