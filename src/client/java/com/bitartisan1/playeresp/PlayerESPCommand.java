package com.bitartisan1.playeresp;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class PlayerESPCommand {

    // Yeah, just keeping this in memory — should be okay for small use
    private static final List<String> playerWhitelist = new ArrayList<>();

    // This holds the RGBA color. Null = use default (by player name)
    private static float[] overrideColor = null;

    // Suggest names from currently online players (excluding yourself)
    private static final SuggestionProvider<FabricClientCommandSource> ONLINE_PLAYER_SUGGESTIONS = (ctx, builder) -> {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().getPlayerList().forEach(entry -> {
                String name = entry.getProfile().getName();
                if (!name.equals(mc.getSession().getUsername()) && name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(name);
                }
            });
        }
        return builder.buildFuture();
    };

    // Suggest players already added to the whitelist
    private static final SuggestionProvider<FabricClientCommandSource> WHITELISTED_PLAYER_SUGGESTIONS = (ctx, builder) -> {
        for (String savedName : playerWhitelist) {
            if (savedName.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                builder.suggest(savedName);
            }
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("pesp")
            .then(ClientCommandManager.literal("on").executes(PlayerESPCommand::enableESP))
            .then(ClientCommandManager.literal("off").executes(PlayerESPCommand::disableESP))
            .then(ClientCommandManager.literal("color")
                .then(ClientCommandManager.argument("r", FloatArgumentType.floatArg(0f, 1f))
                    .then(ClientCommandManager.argument("g", FloatArgumentType.floatArg(0f, 1f))
                        .then(ClientCommandManager.argument("b", FloatArgumentType.floatArg(0f, 1f))
                            .executes(PlayerESPCommand::setColorRGB)
                            .then(ClientCommandManager.argument("a", FloatArgumentType.floatArg(0f, 1f))
                                .executes(PlayerESPCommand::setColorRGBA))))))
            .then(ClientCommandManager.literal("whitelist")
                .then(ClientCommandManager.argument("player", StringArgumentType.greedyString())
                    .suggests(ONLINE_PLAYER_SUGGESTIONS)
                    .executes(PlayerESPCommand::addToWhitelistCmd)))
            .then(ClientCommandManager.literal("whitelist-remove")
                .then(ClientCommandManager.argument("player", StringArgumentType.greedyString())
                    .suggests(WHITELISTED_PLAYER_SUGGESTIONS)
                    .executes(PlayerESPCommand::removeFromWhitelistCmd)))
            .then(ClientCommandManager.literal("whitelist-clear")
                .executes(PlayerESPCommand::clearWhitelistCmd))
            .then(ClientCommandManager.literal("status")
                .executes(PlayerESPCommand::printStatus))
            .then(ClientCommandManager.literal("help")
                .executes(PlayerESPCommand::printHelp))
        );
    }

    private static int enableESP(CommandContext<FabricClientCommandSource> ctx) {
        PlayerESPClient.setShowHitbox(true);
        PlayerESPClient.setShowName(true);
        ctx.getSource().sendFeedback(Text.literal("§aESP is now enabled"));
        return 1;
    }

    private static int disableESP(CommandContext<FabricClientCommandSource> ctx) {
        PlayerESPClient.setShowHitbox(false);
        PlayerESPClient.setShowName(false);
        ctx.getSource().sendFeedback(Text.literal("§cESP is now disabled"));
        return 1;
    }

    private static int setColorRGB(CommandContext<FabricClientCommandSource> ctx) {
        float r = FloatArgumentType.getFloat(ctx, "r");
        float g = FloatArgumentType.getFloat(ctx, "g");
        float b = FloatArgumentType.getFloat(ctx, "b");

        overrideColor = new float[] { r, g, b, 1.0f };  // Alpha defaults to 1
        ctx.getSource().sendFeedback(Text.literal(String.format("§aSet color to RGB(%.2f, %.2f, %.2f)", r, g, b)));
        return 1;
    }

    private static int setColorRGBA(CommandContext<FabricClientCommandSource> ctx) {
        float r = FloatArgumentType.getFloat(ctx, "r");
        float g = FloatArgumentType.getFloat(ctx, "g");
        float b = FloatArgumentType.getFloat(ctx, "b");
        float a = FloatArgumentType.getFloat(ctx, "a");

        overrideColor = new float[] { r, g, b, a };
        ctx.getSource().sendFeedback(Text.literal(String.format("§aSet color to RGBA(%.2f, %.2f, %.2f, %.2f)", r, g, b, a)));
        return 1;
    }

    private static int addToWhitelistCmd(CommandContext<FabricClientCommandSource> ctx) {
        String input = StringArgumentType.getString(ctx, "player");
        String[] names = input.split("\\s+");

        for (String name : names) {
            if (!playerWhitelist.contains(name)) {
                playerWhitelist.add(name);
                ctx.getSource().sendFeedback(Text.literal("§aAdded §f" + name + " §ato whitelist"));
            } else {
                ctx.getSource().sendFeedback(Text.literal("§e" + name + " is already whitelisted"));
            }
        }
        return 1;
    }

    private static int removeFromWhitelistCmd(CommandContext<FabricClientCommandSource> ctx) {
        String input = StringArgumentType.getString(ctx, "player");
        String[] names = input.split("\\s+");

        for (String name : names) {
            if (playerWhitelist.remove(name)) {
                ctx.getSource().sendFeedback(Text.literal("§cRemoved §f" + name + " §cfrom whitelist"));
            } else {
                ctx.getSource().sendFeedback(Text.literal("§e" + name + " wasn't found in the whitelist"));
            }
        }
        return 1;
    }

    private static int clearWhitelistCmd(CommandContext<FabricClientCommandSource> ctx) {
        int count = playerWhitelist.size();
        playerWhitelist.clear();
        ctx.getSource().sendFeedback(Text.literal("§aCleared whitelist (" + count + " entries removed)"));
        return 1;
    }

    private static int printStatus(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().sendFeedback(Text.literal("§b--- PlayerESP Status ---"));
        ctx.getSource().sendFeedback(Text.literal("§7Hitbox: " + (PlayerESPClient.isShowHitbox() ? "§aON" : "§cOFF")));
        ctx.getSource().sendFeedback(Text.literal("§7Name Tags: " + (PlayerESPClient.isShowName() ? "§aON" : "§cOFF")));

        if (overrideColor != null) {
            ctx.getSource().sendFeedback(Text.literal(String.format("§7Custom Color: §fRGBA(%.2f, %.2f, %.2f, %.2f)", overrideColor[0], overrideColor[1], overrideColor[2], overrideColor[3])));
        } else {
            ctx.getSource().sendFeedback(Text.literal("§7Custom Color: §fNone (using defaults)"));
        }

        if (!playerWhitelist.isEmpty()) {
            ctx.getSource().sendFeedback(Text.literal("§7Whitelist: §f" + String.join(", ", playerWhitelist)));
        } else {
            ctx.getSource().sendFeedback(Text.literal("§7Whitelist: §fEmpty (all players shown)"));
        }

        return 1;
    }

    private static int printHelp(CommandContext<FabricClientCommandSource> ctx) {
        // Might split this into a scrollable page in the future
        ctx.getSource().sendFeedback(Text.literal("§bPlayerESP Commands:"));
        ctx.getSource().sendFeedback(Text.literal("§7/pesp on §f- Enable ESP"));
        ctx.getSource().sendFeedback(Text.literal("§7/pesp off §f- Disable ESP"));
        ctx.getSource().sendFeedback(Text.literal("§7/pesp color <r> <g> <b> [a] §f- Set ESP color (0-1 floats)"));
        ctx.getSource().sendFeedback(Text.literal("§7/pesp whitelist <players...> §f- Add players"));
        ctx.getSource().sendFeedback(Text.literal("§7/pesp whitelist-remove <players...> §f- Remove players"));
        ctx.getSource().sendFeedback(Text.literal("§7/pesp whitelist-clear §f- Empty the whitelist"));
        ctx.getSource().sendFeedback(Text.literal("§7/pesp status §f- See current config"));
        ctx.getSource().sendFeedback(Text.literal("§7/pesp help §f- Show this help message"));
        ctx.getSource().sendFeedback(Text.literal("§eTip: Press TAB to autocomplete names."));
        return 1;
    }

    // --- Utility-style accessors below ---

    public static boolean isWhitelisted(String name) {
        return playerWhitelist.isEmpty() || playerWhitelist.contains(name);
    }

    public static float[] getCustomColor() {
        return overrideColor;
    }

    public static void setCustomColor(float[] rgba) {
        overrideColor = rgba;
    }

    public static void setCustomColor(Float r, Float g, Float b, Float a) {
        if (r == null || g == null || b == null) {
            overrideColor = null;  // fallback to defaults
        } else {
            overrideColor = new float[] { r, g, b, (a != null ? a : 1.0f) };
        }
    }

    public static void addToWhitelist(String name) {
        if (!playerWhitelist.contains(name)) {
            playerWhitelist.add(name);
        }
    }

    public static void removeFromWhitelist(String name) {
        playerWhitelist.remove(name);
    }

    public static void clearWhitelist() {
        playerWhitelist.clear();
    }

    public static List<String> getWhitelist() {
        return new ArrayList<>(playerWhitelist);
    }
}
