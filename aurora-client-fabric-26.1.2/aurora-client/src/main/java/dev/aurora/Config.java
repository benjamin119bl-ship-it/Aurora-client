package dev.aurora;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

/** Tiny JSON config at config/aurora.json (edit while the game is closed). */
public final class Config {
    static final Logger LOG = LoggerFactory.getLogger("Aurora");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config instance = new Config();

    /** Extra rain impact splashes on the ground around you. */
    public boolean rainSplashes = true;
    /** Max splash particles per tick at full rain (before the performance governor). */
    public int rainDensity = 10;
    /** Horizontal radius (blocks) where splashes are spawned. */
    public int rainRadius = 14;
    /** Night-time meteors. */
    public boolean meteors = true;
    public boolean fullbright = false;
    /** AUTO, GOLDEN, MOONLIT, NEON, FROST, OFF */
    public String atmosphere = "AUTO";
    public boolean hud = true;
    /** FOV while zooming (30 is the lowest vanilla allows). */
    public int zoomFov = 30;
    /** Scale effects down automatically when your frame rate drops. */
    public boolean adaptivePerformance = true;

    public static Config get() {
        return instance;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("aurora.json");
    }

    public static void load() {
        Path p = path();
        try {
            if (Files.exists(p)) {
                Config c = GSON.fromJson(Files.readString(p), Config.class);
                if (c != null) instance = c;
            }
        } catch (Exception e) {
            LOG.warn("Could not read aurora.json, using defaults", e);
        }
        instance.sanitize();
        save();
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(instance));
        } catch (Exception e) {
            LOG.warn("Could not save aurora.json", e);
        }
    }

    private void sanitize() {
        rainDensity = Math.max(0, Math.min(32, rainDensity));
        rainRadius = Math.max(4, Math.min(24, rainRadius));
        zoomFov = Math.max(30, Math.min(90, zoomFov));
        if (atmosphere == null) atmosphere = "AUTO";
    }
}
