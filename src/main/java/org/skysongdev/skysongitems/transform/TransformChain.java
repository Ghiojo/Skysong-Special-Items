package org.skysongdev.skysongitems.transform;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Works out how many transformations an item really needs when it is due several at once,
 * walking its chain of forms by name only. Once the chain comes back to a form it already
 * passed, the rest of the count goes round that cycle, so only the remainder matters: a
 * four-season cycle 14 transformations behind needs 2, never 14.
 */
public final class TransformChain {
    private TransformChain() {
    }

    /**
     * @param next  the form each form transforms into, empty when it has no transformation
     * @param start the item's form now
     * @param times how many transformations came due
     * @return how many transformations to make, at most the number of forms in the chain
     */
    public static long stepsNeeded(Function<String, Optional<String>> next, @Nullable String start, long times) {
        List<String> visited = new ArrayList<>();
        String form = start;
        while (form != null && visited.size() < times) {
            int seen = visited.indexOf(form);
            if (seen >= 0) {
                long cycle = visited.size() - (long) seen;
                return seen + (times - seen) % cycle;
            }
            Optional<String> following = next.apply(form);
            if (following.isEmpty()) {
                break;
            }
            visited.add(form);
            form = following.get();
        }
        return visited.size();
    }
}
