package com.bitartisan1.playeresp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerESPClient implements ClientModInitializer {

    // Might refactor MOD_ID later if needed in multiple classes
    public static final String MOD_ID = "playeresp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Global key bindings
    private static KeyBinding hitboxKey;
    private static KeyBinding nameKey;
    private static KeyBinding configKey;

    // State flags
    private static boolean shouldShowHitbox = true;
    private static boolean shouldShowName = true;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[PlayerESP] Client init started...");

        // Registering key bindings – note: I chose these keys mostly arbitrarily
        hitboxKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.playeresp.toggle_hitbox",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H, // H for Hitbox
            "category.playeresp.main"
        ));

        nameKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.playeresp.toggle_name",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_N, // N for Name
            "category.playeresp.main"
        ));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.playeresp.open_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J, // No real reason – just felt right
            "category.playeresp.main"
        ));

        // Hooking into tick loop to listen for key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle hitbox toggle
            if (hitboxKey.wasPressed()) {
                shouldShowHitbox = !shouldShowHitbox;

                if (client.player != null) {
                    client.player.sendMessage(
                        net.minecraft.text.Text.literal("Hitbox ESP is now " + (shouldShowHitbox ? "enabled" : "disabled")),
                        true
                    );
                }
            }

            // Handle name label toggle
            if (nameKey.wasPressed()) {
                shouldShowName = !shouldShowName;

                if (client.player != null) {
                    client.player.sendMessage(
                        net.minecraft.text.Text.literal("Name ESP is now " + (shouldShowName ? "enabled" : "disabled")),
                        true
                    );
                }
            }

            // Config screen trigger (this could maybe be moved to a command instead)
            if (configKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new com.bitartisan1.playeresp.gui.PlayerESPConfigScreen(null));
                }
            }
        });

        // Visual rendering hook for our ESP overlay stuff
        WorldRenderEvents.AFTER_TRANSLUCENT.register(new PlayerESPRenderer());

        // Register custom client-side commands
        ClientCommandRegistrationCallback.EVENT.register(PlayerESPCommand::register);

        // Might eventually add more here later, e.g., telemetry or settings file loading
    }

    // Just quick accessors — might want to move to a config singleton eventually
    public static boolean isShowHitbox() {
        return shouldShowHitbox;
    }

    public static boolean isShowName() {
        return shouldShowName;
    }

    public static void setShowHitbox(boolean value) {
        shouldShowHitbox = value;
    }

    public static void setShowName(boolean value) {
        shouldShowName = value;
    }
}
