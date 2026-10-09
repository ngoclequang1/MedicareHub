package fit.se2.medicarehub.controller;

import fit.se2.medicarehub.model.agent.AgentChatRequest;
import fit.se2.medicarehub.model.agent.AgentChatResponse;
import fit.se2.medicarehub.service.agent.MedicalAgentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final MedicalAgentService medicalAgentService;

    public AgentController(MedicalAgentService medicalAgentService) {
        this.medicalAgentService = medicalAgentService;
    }

    @PostMapping("/chat")
    public AgentChatResponse chat(@Valid @RequestBody AgentChatRequest request,
                                  Authentication authentication) {
        return medicalAgentService.chat(request.message(), authentication);
    }
}
