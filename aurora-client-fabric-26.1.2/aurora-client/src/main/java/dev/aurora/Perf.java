package dev.aurora;

/**
 * Frame-time governor. Effects ask scale() for a 0..1 multiplier so they back off
 * automatically when the game is struggling (full speed at >=50 FPS, off at <=20 FPS).
 */
final class Perf {
    private static long lastFrame;
    private static long lastUpdate;
    private static float emaMs = 16.7f;
    private static float lastDt = 0.016f;

    private Perf() {}

    /** Call once per rendered frame. */
    static void frame() {
        long now = System.nanoTime();
        if (lastFrame != 0) {
            float ms = (now - lastFrame) / 1_000_000f;
            if (ms < 1000f) {
                emaMs += (ms - emaMs) * 0.05f;
                lastDt = ms / 1000f;
            }
        }
        lastFrame = now;
        lastUpdate = now;
    }

    static float dt() {
        return Math.min(lastDt, 0.1f);
    }

    static int fps() {
        return emaMs <= 0 ? 0 : Math.round(1000f / emaMs);
    }

    static float scale() {
        if (!Config.get().adaptivePerformance) return 1f;
        // No fresh frame data (e.g. HUD hidden with F1): don't throttle blindly.
        if (System.nanoTime() - lastUpdate > 500_000_000L) return 1f;
        if (emaMs <= 20f) return 1f;
        if (emaMs >= 50f) return 0f;
        return (50f - emaMs) / 30f;
    }
}
