package dev.aurora;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/** HUD layers: the atmosphere grade, a compact info readout and short toast messages. */
final class Overlay {
    private static String toast = "";
    private static long toastUntil;

    private Overlay() {}

    static void toast(String text) {
        toast = text;
        toastUntil = System.currentTimeMillis() + 1800;
    }

    static void grade(GuiGraphicsExtractor g, DeltaTracker delta) {
        Perf.frame();
        Minecraft mc = Minecraft.getInstance();
        Atmosphere.render(g, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), Perf.dt());
    }

    static void info(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        if (System.currentTimeMillis() < toastUntil) {
            int tw = mc.font.width(toast);
            g.text(mc.font, toast, (w - tw) / 2, h - 64, 0xFF7FE9FF, true);
        }

        LocalPlayer p = mc.player;
        if (!Config.get().hud || p == null) return;

        int fps = Perf.fps();
        int fpsColor = fps >= 60 ? 0xFF6BFF8A : fps >= 30 ? 0xFFFFD35C : 0xFFFF6B6B;
        String face = p.getDirection().getSerializedName().substring(0, 1).toUpperCase();
        String pos = Mth.floor(p.getX()) + "  " + Mth.floor(p.getY()) + "  " + Mth.floor(p.getZ());

        g.text(mc.font, "FPS " + fps, 6, 6, fpsColor, true);
        g.text(mc.font, "XYZ " + pos + "  " + face, 6, 17, 0xFFE6E6E6, true);
        g.text(mc.font, "Look: " + Atmosphere.current().label, 6, 28, 0xFF9AA7B5, true);
    }
}
