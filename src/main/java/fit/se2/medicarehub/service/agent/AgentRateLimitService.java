package fit.se2.medicarehub.service.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentRateLimitService {

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int requestsPerMinute;
    private final Clock clock;

    public AgentRateLimitService(@Value("${app.ai.rate-limit-per-minute:20}") int requestsPerMinute) {
        this(requestsPerMinute, Clock.systemUTC());
    }

    AgentRateLimitService(int requestsPerMinute, Clock clock) {
        this.requestsPerMinute = requestsPerMinute;
        this.clock = clock;
    }

    public boolean tryAcquire(String key) {
        long currentMinute = clock.instant().getEpochSecond() / 60;
        Window updated = windows.compute(key, (ignored, existing) -> {
            if (existing == null || existing.minute() != currentMinute) {
                return new Window(currentMinute, 1);
            }
            return new Window(currentMinute, existing.count() + 1);
        });
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> entry.getValue().minute() < currentMinute - 1);
        }
        return updated.count() <= requestsPerMinute;
    }

    private record Window(long minute, int count) {
    }
}
