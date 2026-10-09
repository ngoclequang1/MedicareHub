package fit.se2.medicarehub.controller;

import fit.se2.medicarehub.model.agent.AgentChatRequest;
import fit.se2.medicarehub.model.agent.AgentChatResponse;
import fit.se2.medicarehub.service.agent.MedicalAgentService;
import fit.se2.medicarehub.service.agent.AgentRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final MedicalAgentService medicalAgentService;
    private final AgentRateLimitService rateLimitService;

    public AgentController(MedicalAgentService medicalAgentService,
                           AgentRateLimitService rateLimitService) {
        this.medicalAgentService = medicalAgentService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/chat")
    public AgentChatResponse chat(@Valid @RequestBody AgentChatRequest request,
                                  Authentication authentication,
                                  HttpServletRequest httpRequest) {
        String identity = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())
                ? authentication.getName() : httpRequest.getRemoteAddr();
        if (!rateLimitService.tryAcquire(identity)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Bạn gửi quá nhiều yêu cầu. Vui lòng thử lại sau một phút.");
        }
        return medicalAgentService.chat(request.message(), authentication);
    }
}
