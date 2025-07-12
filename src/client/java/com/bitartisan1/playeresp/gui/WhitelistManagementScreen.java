package com.bitartisan1.playeresp.gui;

import com.bitartisan1.playeresp.PlayerESPCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public class WhitelistManagementScreen extends Screen {
    // GUI textures - borrowed from vanilla minecraft
    private static final Identifier BG_TEXTURE = Identifier.of("minecraft", "textures/gui/demo_background.png");
    private static final Identifier LIST_BG_TEXTURE = Identifier.of("minecraft", "textures/gui/recipe_book.png");
    private static final Identifier HEADER_SELECTED = Identifier.of("minecraft", "textures/gui/sprites/advancements/box_obtained.png");
    private static final Identifier HEADER_NORMAL = Identifier.of("minecraft", "textures/gui/sprites/advancements/box_unobtained.png");
    
    private final Screen previousScreen;
    
    // Window dimensions - tweaked these until it looked good
    private final int guiWidth = 370;
    private final int guiHeight = 360;
    private int startX, startY;
    
    private TextFieldWidget playerSearchBox;
    private final List<PlayerInfo> availablePlayers = new ArrayList<>();
    private final List<PlayerInfo> currentWhitelist = new ArrayList<>();
    
    // Scrolling stuff
    private int leftListScroll = 0;
    private int rightListScroll = 0;
    private final int entriesPerPage = 6; // how many players we can show at once
    
    // Simple container for player data
    private static class PlayerInfo {
        final String playerName;
        final Identifier headTexture;
        boolean isSelected = false;
        
        PlayerInfo(String name, Identifier texture) {
            this.playerName = name;
            this.headTexture = texture;
        }
    }
    
    public WhitelistManagementScreen(Screen parent) {
        super(Text.literal("Whitelist Management").formatted(Formatting.WHITE));
        this.previousScreen = parent;
        refreshPlayerData();
    }
    
    // Rebuild the player lists from current game state
    private void refreshPlayerData() {
        availablePlayers.clear();
        currentWhitelist.clear();
        
        MinecraftClient mc = MinecraftClient.getInstance();
        Set<String> whitelistedNames = new HashSet<>(PlayerESPCommand.getWhitelist());
        
        // Get players from the server player list
        if (mc.getNetworkHandler() != null) {
            for (PlayerListEntry player : mc.getNetworkHandler().getPlayerList()) {
                String name = player.getProfile().getName();
                
                // Don't show ourselves in the list
                if (!name.equals(mc.getSession().getUsername())) {
                    SkinTextures skinData = player.getSkinTextures();
                    Identifier headTexture = skinData != null ? skinData.texture() : null;
                    
                    if (whitelistedNames.contains(name)) {
                        currentWhitelist.add(new PlayerInfo(name, headTexture));
                    } else {
                        availablePlayers.add(new PlayerInfo(name, headTexture));
                    }
                }
            }
        }
        
        // Sort alphabetically because why not
        availablePlayers.sort(Comparator.comparing(p -> p.playerName));
        currentWhitelist.sort(Comparator.comparing(p -> p.playerName));
    }
    
    @Override
    protected void init() {
        // Center the window on screen
        this.startX = (this.width - guiWidth) / 2;
        this.startY = (this.height - guiHeight) / 2;
        
        // Search box at the top
        this.playerSearchBox = new TextFieldWidget(this.textRenderer, startX + 40, startY + 50, 100, 12, Text.literal("Search"));
        this.playerSearchBox.setPlaceholder(Text.literal("Search players..."));
        this.addSelectableChild(playerSearchBox);
        
        // Add/remove buttons in the middle
        ButtonWidget moveToWhitelistBtn = ButtonWidget.builder(
            Text.literal("→"),
            btn -> moveSelectedToWhitelist())
            .dimensions(startX + 167, startY + 100, 20, 16)
            .build();
        this.addDrawableChild(moveToWhitelistBtn);
        
        ButtonWidget removeFromWhitelistBtn = ButtonWidget.builder(
            Text.literal("←"),
            btn -> removeSelectedFromWhitelist())
            .dimensions(startX + 167, startY + 120, 20, 16)
            .build();
        this.addDrawableChild(removeFromWhitelistBtn);
        
        // Utility buttons at the bottom
        ButtonWidget selectAllBtn = ButtonWidget.builder(
            Text.literal("Select All"),
            btn -> {
                for (PlayerInfo player : availablePlayers) {
                    player.isSelected = true;
                }
            })
            .dimensions(startX + 8, startY + 200, 60, 16)
            .build();
        this.addDrawableChild(selectAllBtn);
        
        ButtonWidget deselectAllBtn = ButtonWidget.builder(
            Text.literal("Clear"),
            btn -> {
                for (PlayerInfo player : availablePlayers) {
                    player.isSelected = false;
                }
                for (PlayerInfo player : currentWhitelist) {
                    player.isSelected = false;
                }
            })
            .dimensions(startX + 72, startY + 200, 40, 16)
            .build();
        this.addDrawableChild(deselectAllBtn);
        
        ButtonWidget nukeWhitelistBtn = ButtonWidget.builder(
            Text.literal("Clear All"),
            btn -> {
                PlayerESPCommand.clearWhitelist();
                refreshPlayerData();
            })
            .dimensions(startX + guiWidth - 135, startY + 200, 60, 16)
            .build();
        this.addDrawableChild(nukeWhitelistBtn);
        
        // Scroll buttons for left list
        ButtonWidget leftScrollUp = ButtonWidget.builder(
            Text.literal("▲"),
            btn -> leftListScroll = Math.max(0, leftListScroll - 1))
            .dimensions(startX + 130, startY + 27, 16, 12)
            .build();
        this.addDrawableChild(leftScrollUp);
        
        ButtonWidget leftScrollDown = ButtonWidget.builder(
            Text.literal("▼"),
            btn -> leftListScroll = Math.min(Math.max(0, availablePlayers.size() - entriesPerPage), leftListScroll + 1))
            .dimensions(startX + 130, startY + 180, 16, 12)
            .build();
        this.addDrawableChild(leftScrollDown);
        
        // Scroll buttons for right list
        ButtonWidget rightScrollUp = ButtonWidget.builder(
            Text.literal("▲"),
            btn -> rightListScroll = Math.max(0, rightListScroll - 1))
            .dimensions(startX + 317, startY + 27, 16, 12)
            .build();
        this.addDrawableChild(rightScrollUp);
        
        ButtonWidget rightScrollDown = ButtonWidget.builder(
            Text.literal("▼"),
            btn -> rightListScroll = Math.min(Math.max(0, currentWhitelist.size() - entriesPerPage), rightListScroll + 1))
            .dimensions(startX + 317, startY + 180, 16, 12)
            .build();
        this.addDrawableChild(rightScrollDown);
        
        // Close button
        ButtonWidget doneBtn = ButtonWidget.builder(
            Text.literal("Done"),
            btn -> this.close())
            .dimensions(startX + guiWidth - 70, startY + 200, 40, 16)
            .build();
        this.addDrawableChild(doneBtn);
    }
    
    private void moveSelectedToWhitelist() {
        for (PlayerInfo player : availablePlayers) {
            if (player.isSelected) {
                PlayerESPCommand.addToWhitelist(player.playerName);
            }
        }
        refreshPlayerData();
    }
    
    private void removeSelectedFromWhitelist() {
        for (PlayerInfo player : currentWhitelist) {
            if (player.isSelected) {
                PlayerESPCommand.removeFromWhitelist(player.playerName);
            }
        }
        refreshPlayerData();
    }
    
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float deltaTime) {
        // Dark overlay behind the GUI
        this.renderBackground(ctx, mouseX, mouseY, deltaTime);
        
        // Main window background
        ctx.drawTexture(RenderLayer::getGuiTextured, BG_TEXTURE, startX, startY, 0.0f, 0.0f, guiWidth, guiHeight, 370, 360);
        
        // Let the widgets draw themselves
        super.render(ctx, mouseX, mouseY, deltaTime);
        
        // Window title
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, startX + guiWidth / 2, startY + 6, 0xFFFFFF);
        
        // Left side header (available players)
        ctx.drawTexture(RenderLayer::getGuiTextured, HEADER_NORMAL, startX + 8, startY + 23, 0.0f, 0.0f, 120, 16, 120, 16);
        Text leftHeader = Text.literal("Online Players (").formatted(Formatting.WHITE)
            .append(Text.literal(String.valueOf(availablePlayers.size())).formatted(Formatting.AQUA))
            .append(Text.literal(")").formatted(Formatting.WHITE));
        ctx.drawText(this.textRenderer, leftHeader, startX + 12, startY + 27, 0xFFFFFF, false);
        
        // Right side header (whitelisted players)
        ctx.drawTexture(RenderLayer::getGuiTextured, HEADER_SELECTED, startX + 195, startY + 23, 0.0f, 0.0f, 120, 16, 120, 16);
        Text rightHeader = Text.literal("Whitelisted (").formatted(Formatting.WHITE)
            .append(Text.literal(String.valueOf(currentWhitelist.size())).formatted(Formatting.GOLD))
            .append(Text.literal(")").formatted(Formatting.WHITE));
        ctx.drawText(this.textRenderer, rightHeader, startX + 200, startY + 27, 0xFFFFFF, false);
           
        // List backgrounds
        ctx.drawTexture(RenderLayer::getGuiTextured, LIST_BG_TEXTURE, startX + 18, startY + 40, 0.0f, 0.0f, 228, 210, 228, 210);   
        ctx.drawTexture(RenderLayer::getGuiTextured, LIST_BG_TEXTURE, startX + 205, startY + 40, 0.0f, 0.0f, 228, 210, 228, 210);   

        // Hide the search icon on the right list by drawing over it
        ctx.fill(startX + 215, startY + 50, startX + 235, startY + 62, 0xFF373737);

        // Draw the search box on top
        this.playerSearchBox.render(ctx, mouseX, mouseY, deltaTime);
        
        // Draw the player lists
        drawPlayerList(ctx, availablePlayers, startX + 25, startY + 65, leftListScroll, mouseX, mouseY, false);
        drawPlayerList(ctx, currentWhitelist, startX + 213, startY + 65, rightListScroll, mouseX, mouseY, true);
    }
    
    private void drawPlayerList(DrawContext ctx, List<PlayerInfo> players, int x, int y, int scroll, int mouseX, int mouseY, boolean isRightSide) {
        int listW = 120;
        int listH = 220;
        int rowHeight = 14;
        
        // Transparent list area (the texture already provides the background)
        ctx.fill(x, y, x + listW, y + listH, 0x00000000);
        ctx.drawBorder(x, y, listW, listH, 0x00000000);
        
        // Draw visible player entries
        for (int i = scroll; i < Math.min(players.size(), scroll + entriesPerPage); i++) {
            PlayerInfo player = players.get(i);
            int rowY = y + (i - scroll) * rowHeight + 2;
            
            // Search filtering
            String searchText = playerSearchBox.getText().toLowerCase();
            if (!searchText.isEmpty() && !player.playerName.toLowerCase().contains(searchText)) {
                continue;
            }
            
            // Hover highlight
            if (mouseX >= x && mouseX <= x + listW && mouseY >= rowY && mouseY <= rowY + rowHeight - 2) {
                ctx.fill(x + 2, rowY, x + listW - 2, rowY + rowHeight - 2, 0x44FFFFFF);
            }
            
            // Checkbox
            boolean checkboxHovered = mouseX >= x + 2 && mouseX <= x + 10 && mouseY >= rowY + 2 && mouseY <= rowY + 10;
            ctx.fill(x + 2, rowY + 2, x + 10, rowY + 10, player.isSelected ? 0xFF00AA00 : 0xFF444444);
            if (checkboxHovered) {
                ctx.drawBorder(x + 2, rowY + 2, 8, 8, 0xFFFFFFFF);
            }
            if (player.isSelected) {
                ctx.drawText(this.textRenderer, Text.literal("✓"), x + 3, rowY + 3, 0xFFFFFF, false);
            }
            
            // Player head
            if (player.headTexture != null) {
                int headSize = 10;
                int headX = x + 14;
                int headY = rowY + 1;
                
                // Draw face layer (main skin)
                ctx.drawTexture(RenderLayer::getGuiTextured, player.headTexture, headX, headY, 8.0f, 8.0f, headSize, headSize, 8, 8, 64, 64);
                // Draw hat layer (overlay)
                ctx.drawTexture(RenderLayer::getGuiTextured, player.headTexture, headX, headY, 40.0f, 8.0f, headSize, headSize, 8, 8, 64, 64);
            }
            
            // Player name (truncate if too long)
            String displayName = player.playerName;
            int nameX = x + 28;
            if (this.textRenderer.getWidth(displayName) > listW - 30) {
                displayName = displayName.substring(0, Math.min(displayName.length(), 8)) + "...";
            }
            ctx.drawText(this.textRenderer, Text.literal(displayName), nameX, rowY + 3, 0xFFFFFF, false);
        }
        
        // Pagination info
        if (players.size() > entriesPerPage) {
            String pageInfo = (scroll + 1) + "-" + Math.min(scroll + entriesPerPage, players.size()) + " of " + players.size();
            ctx.drawText(this.textRenderer, Text.literal(pageInfo), x, y + listH + 2, 0x404040, false);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle clicking on checkboxes
        if (handleCheckboxClick(availablePlayers, startX + 25, startY + 65, leftListScroll, mouseX, mouseY) ||
            handleCheckboxClick(currentWhitelist, startX + 213, startY + 65, rightListScroll, mouseX, mouseY)) {
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private boolean handleCheckboxClick(List<PlayerInfo> players, int x, int y, int scroll, double mouseX, double mouseY) {
        int rowHeight = 14;
        
        for (int i = scroll; i < Math.min(players.size(), scroll + entriesPerPage); i++) {
            PlayerInfo player = players.get(i);
            int rowY = y + (i - scroll) * rowHeight + 2;
            
            // Skip if filtered out by search
            String searchText = playerSearchBox.getText().toLowerCase();
            if (!searchText.isEmpty() && !player.playerName.toLowerCase().contains(searchText)) {
                continue;
            }
            
            // Check if click was on the checkbox
            if (mouseX >= x + 2 && mouseX <= x + 10 && mouseY >= rowY + 2 && mouseY <= rowY + 10) {
                player.isSelected = !player.isSelected;
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game
    }
    
    @Override
    public void close() {
        this.client.setScreen(previousScreen);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Let the search box handle typing first
        if (playerSearchBox.isFocused() && playerSearchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        
        // ESC to close
        if (keyCode == 256) {
            this.close();
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (playerSearchBox.isFocused()) {
            return playerSearchBox.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }
}