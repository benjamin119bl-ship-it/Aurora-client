package dev.aurora;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

public final class AuroraClient implements ClientModInitializer {
    public static final String ID = "aurora";

    private static KeyMapping zoomKey, fullbrightKey, atmosphereKey, fxKey, hudKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ID, "main"));
        zoomKey = key("zoom", InputConstants.KEY_C, cat);
        fullbrightKey = key("fullbright", InputConstants.KEY_B, cat);
        atmosphereKey = key("atmosphere", InputConstants.KEY_V, cat);
        fxKey = key("fx", InputConstants.KEY_G, cat);
        hudKey = key("hud", InputConstants.KEY_H, cat);

        ClientTickEvents.END_CLIENT_TICK.register(AuroraClient::tick);

        // attachElementBefore(CHAT) is the documented 26.1.2 hook. If you want the colour grade
        // *under* the hotbar too, swap the first line for HudElementRegistry.addFirst(...).
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(ID, "atmosphere"), Overlay::grade);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(ID, "info"), Overlay::info);
    }

    private static KeyMapping key(String name, int code, KeyMapping.Category cat) {
        return KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key." + ID + "." + name, InputConstants.Type.KEYSYM, code, cat));
    }

    private static void tick(Minecraft mc) {
        Config c = Config.get();

        while (fullbrightKey.consumeClick()) {
            c.fullbright = !c.fullbright;
            Config.save();
            Overlay.toast("Fullbright: " + (c.fullbright ? "ON" : "OFF"));
        }
        while (atmosphereKey.consumeClick()) {
            Atmosphere.Preset next = Atmosphere.current().next();
            c.atmosphere = next.name();
            Config.save();
            Overlay.toast("Look: " + next.label);
        }
        while (fxKey.consumeClick()) {
            boolean on = !(c.rainSplashes || c.meteors);
            c.rainSplashes = on;
            c.meteors = on;
            Config.save();
            Overlay.toast("Rain + sky FX: " + (on ? "ON" : "OFF"));
        }
        while (hudKey.consumeClick()) {
            c.hud = !c.hud;
            Config.save();
        }

        Utility.tickZoom(mc, zoomKey.isDown());

        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) {
            Meteors.reset();
            return;
        }
        if (mc.isPaused()) return;

        Utility.tickFullbright(player);
        RainFx.tick(level, player);
        Meteors.tick(level, player);
        Atmosphere.tick(level);
    }
}
