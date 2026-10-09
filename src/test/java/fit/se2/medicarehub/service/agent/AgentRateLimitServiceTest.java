package fit.se2.medicarehub.service.agent;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentRateLimitServiceTest {

    @Test
    void rejectsRequestsOverConfiguredLimitWithinSameMinute() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneOffset.UTC);
        AgentRateLimitService service = new AgentRateLimitService(2, clock);

        assertTrue(service.tryAcquire("patient@example.com"));
        assertTrue(service.tryAcquire("patient@example.com"));
        assertFalse(service.tryAcquire("patient@example.com"));
        assertTrue(service.tryAcquire("another@example.com"));
    }
}
