package dev.aurora;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * "Soft binding" for vanilla methods whose names/shape move around between 26.x releases
 * (time-of-day handling is data-driven now). Looked up once by name; if none exist the
 * dependent feature quietly turns off instead of crashing the game.
 */
final class Soft {
    private static final Map<String, Optional<Method>> CACHE = new HashMap<>();
    private static boolean warned;

    private Soft() {}

    private static Boolean bool(Object target, String name) {
        Optional<Method> m = CACHE.computeIfAbsent(name, n -> {
            try {
                Method x = target.getClass().getMethod(n);
                return x.getReturnType() == boolean.class ? Optional.of(x) : Optional.empty();
            } catch (ReflectiveOperationException e) {
                return Optional.empty();
            }
        });
        if (m.isEmpty()) return null;
        try {
            return (Boolean) m.get().invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    static boolean isNight(Object level) {
        Boolean b = bool(level, "isDarkOutside");
        if (b != null) return b;
        b = bool(level, "isBrightOutside");
        if (b != null) return !b;
        b = bool(level, "isNight");
        if (b != null) return b;
        b = bool(level, "isDay");
        if (b != null) return !b;
        if (!warned) {
            warned = true;
            Config.LOG.warn("Aurora: no day/night query found on this Minecraft build; night effects disabled.");
        }
        return false;
    }
}
