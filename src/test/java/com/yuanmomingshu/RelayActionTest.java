package com.yuanmomingshu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RelayActionTest {
    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true"})
    void missingEitherModDoesNothing(boolean create, boolean genshin) {
        RelayAction action = new RelayAction();
        AtomicInteger opens = new AtomicInteger();
        assertDoesNotThrow(() -> action.runIfMatched(create, genshin, opens::incrementAndGet));
        assertEquals(0, opens.get());
        assertTrue(action.isChecked());
    }

    @Test
    void bothModsOpenBrowserBeforeCrashingAndOnlyOnce() {
        RelayAction action = new RelayAction();
        AtomicInteger opens = new AtomicInteger();
        RelayAction.IntentionalRelayCrash crash = assertThrows(RelayAction.IntentionalRelayCrash.class,
                () -> action.runIfMatched(true, true, opens::incrementAndGet));
        assertEquals(1, opens.get());
        assertEquals(RelayAction.CRASH_MESSAGE, crash.getMessage());
        assertNull(crash.getCause());
        assertDoesNotThrow(() -> action.runIfMatched(true, true, opens::incrementAndGet));
        assertEquals(1, opens.get());
    }

    @Test
    void browserFailureStillCrashesWithOriginalCauseAndDoesNotRetry() {
        RelayAction action = new RelayAction();
        AtomicInteger opens = new AtomicInteger();
        RuntimeException browserFailure = new IllegalStateException("No browser available");
        RelayAction.IntentionalRelayCrash crash = assertThrows(RelayAction.IntentionalRelayCrash.class,
                () -> action.runIfMatched(true, true, () -> {
                    opens.incrementAndGet();
                    throw browserFailure;
                }));
        assertSame(browserFailure, crash.getCause());
        assertDoesNotThrow(() -> action.runIfMatched(true, true, opens::incrementAndGet));
        assertEquals(1, opens.get());
    }
}
