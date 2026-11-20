package dev.aurora;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Client-only quality-of-life tools: zoom and fullbright. */
final class Utility {
    private static int savedFov = -1;
    private static boolean weAppliedNightVision;

    private Utility() {}

    /** Smooth FOV zoom while the key is held; restores your FOV on release or when a screen opens. */
    static void tickZoom(Minecraft mc, boolean held) {
        OptionInstance<Integer> fov = mc.options.fov();
        boolean want = held && mc.screen == null && mc.player != null;

        if (want) {
            if (savedFov < 0) savedFov = fov.get();
            int target = Math.min(Config.get().zoomFov, savedFov);
            fov.set(approach(fov.get(), target));
        } else if (savedFov >= 0) {
            int next = approach(fov.get(), savedFov);
            if (next == savedFov) savedFov = -1;
            fov.set(next);
        }
    }

    private static int approach(int cur, int target) {
        if (cur == target) return cur;
        int step = Math.round((target - cur) * 0.4f);
        if (step == 0) step = target > cur ? 1 : -1;
        int next = cur + step;
        return (target > cur) ? Math.min(next, target) : Math.max(next, target);
    }

    /**
     * Fullbright via a hidden, client-only Night Vision effect (never sent to the server).
     * Refreshed before it can start flickering, removed again when toggled off.
     */
    static void tickFullbright(LocalPlayer p) {
        boolean want = Config.get().fullbright;
        MobEffectInstance cur = p.getEffect(MobEffects.NIGHT_VISION);
        if (want) {
            if (cur == null || cur.getDuration() < 300) {
                p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 800, 0, false, false, false));
                weAppliedNightVision = true;
            }
        } else if (weAppliedNightVision) {
            if (cur != null && !cur.isVisible()) p.removeEffect(MobEffects.NIGHT_VISION);
            weAppliedNightVision = false;
        }
    }
}
