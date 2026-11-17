package org.skysongdev.skysongitems.transform;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests working out how many transformations an item needs when several came due at once. */
class TransformChainTest {
    /** spring, summer, autumn, winter and round again, reached from a seed that sprouts once. */
    private static final Map<String, String> SEASONS = Map.of("seed", "spring", "spring", "summer",
            "summer", "autumn", "autumn", "winter", "winter", "spring");
    private static final Function<String, Optional<String>> NEXT = form -> Optional.ofNullable(SEASONS.get(form));

    @Test
    void aCycleOnlyNeedsTheRemainderOfTheCount() {
        assertEquals(2, TransformChain.stepsNeeded(NEXT, "spring", 14));
        assertEquals(0, TransformChain.stepsNeeded(NEXT, "spring", 4_000_000_000L));
        assertEquals(3, TransformChain.stepsNeeded(NEXT, "spring", 3));
    }

    @Test
    void formsBeforeTheCycleAreWalkedOnce() {
        // seed, then 13 more round the four seasons: 1 + 13 % 4.
        assertEquals(2, TransformChain.stepsNeeded(NEXT, "seed", 14));
    }

    @Test
    void aChainThatEndsStopsAtItsLastForm() {
        Function<String, Optional<String>> once = form -> "egg".equals(form) ? Optional.of("chick") : Optional.empty();

        assertEquals(1, TransformChain.stepsNeeded(once, "egg", 50));
        assertEquals(0, TransformChain.stepsNeeded(once, null, 50));
    }
}
