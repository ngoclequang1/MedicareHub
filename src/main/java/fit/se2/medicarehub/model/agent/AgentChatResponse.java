package fit.se2.medicarehub.model.agent;

import java.util.List;
import java.util.Map;

public record AgentChatResponse(
        String reply,
        String intent,
        String safetyLevel,
        String toolName,
        boolean requiresEmergencyAction,
        List<Map<String, Object>> data
) {
    public static AgentChatResponse text(String reply, String intent, String safetyLevel) {
        return new AgentChatResponse(reply, intent, safetyLevel, null, false, List.of());
    }
}
