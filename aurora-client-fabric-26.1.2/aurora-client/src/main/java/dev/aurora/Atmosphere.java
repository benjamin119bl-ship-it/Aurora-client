package dev.aurora;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;

/**
 * Screen-space colour grade + soft vignette, drawn through Fabric's HUD API.
 * It is 3 draw calls per frame, so it costs effectively nothing, and because it never
 * touches the sky/lightmap renderers it can't conflict with other rendering mods.
 * AUTO reacts to rain, thunder and night; the other presets are fixed looks.
 */
final class Atmosphere {
    enum Preset {
        AUTO("Auto", 0, 0, 0, 0, 0f),
        GOLDEN("Golden Hour", 255, 154, 60, 42, 0.30f),
        MOONLIT("Moonlit", 48, 80, 200, 48, 0.40f),
        NEON("Neon", 176, 48, 255, 34, 0.45f),
        FROST("Frost", 144, 216, 255, 36, 0.25f),
        OFF("Off", 0, 0, 0, 0, 0f);

        final String label;
        final float r, g, b, a, v;

        Preset(String label, float r, float g, float b, float a, float v) {
            this.label = label;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.v = v;
        }

        Preset next() {
            Preset[] all = values();
            return all[(ordinal() + 1) % all.length];
        }

        static Preset parse(String s) {
            try {
                return valueOf(s.toUpperCase(java.util.Locale.ROOT));
            } catch (Exception e) {
                return AUTO;
            }
        }
    }

    // target and current state: r, g, b, alpha (0-255), vignette strength (0-1)
    private static float tr, tg, tb, ta, tv;
    private static float cr, cg, cb, ca, cv;

    private Atmosphere() {}

    static Preset current() {
        return Preset.parse(Config.get().atmosphere);
    }

    private static void target(float r, float g, float b, float a, float v) {
        tr = r; tg = g; tb = b; ta = a; tv = v;
    }

    /** Called once per client tick. */
    static void tick(ClientLevel level) {
        Preset p = current();
        switch (p) {
            case OFF -> { ta = 0; tv = 0; }
            case AUTO -> auto(level);
            default -> target(p.r, p.g, p.b, p.a, p.v);
        }
    }

    private static void auto(ClientLevel level) {
        if (level.dimension() != Level.OVERWORLD) {
            ta = 0; tv = 0;
            return;
        }
        float rain = level.getRainLevel(1.0F);
        float thunder = level.getThunderLevel(1.0F);
        if (rain > 0.01F) {
            target(105, 125, 150, 40 * rain + 22 * thunder, 0.35F * rain + 0.20F * thunder);
        } else if (Soft.isNight(level)) {
            target(40, 70, 150, 22, 0.25F);
        } else {
            target(255, 214, 150, 10, 0.12F);
        }
    }

    /** Called every frame from the HUD layer. */
    static void render(GuiGraphicsExtractor g, int w, int h, float dt) {
        float k = 1f - (float) Math.exp(-dt * 2.5f);
        cr += (tr - cr) * k;
        cg += (tg - cg) * k;
        cb += (tb - cb) * k;
        ca += (ta - ca) * k;
        cv += (tv - cv) * k;

        int a = Math.min(90, (int) ca);
        if (a > 0) {
            int rgb = (clamp(cr) << 16) | (clamp(cg) << 8) | clamp(cb);
            g.fill(0, 0, w, h, (a << 24) | rgb);
        }
        int va = Math.min(120, (int) (cv * 110f));
        if (va > 0) {
            int band = (int) (h * 0.25f);
            int dark = va << 24;
            g.fillGradient(0, 0, w, band, dark, 0);
            g.fillGradient(0, h - band, w, h, 0, dark);
        }
    }

    private static int clamp(float v) {
        return Math.max(0, Math.min(255, (int) v));
    }
}
