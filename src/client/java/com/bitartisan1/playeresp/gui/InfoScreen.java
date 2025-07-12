package com.bitartisan1.playeresp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class InfoScreen extends Screen {

    // Using a demo background texture from mc
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of("minecraft", "textures/gui/demo_background.png");

    private final Screen previousScreen;
    private final int boxWidth = 400;
    private final int boxHeight = 320;
    private int boxX, boxY;

    public InfoScreen(Screen parent) {
        super(Text.literal("PlayerESP Info"));
        this.previousScreen = parent;
    }

    @Override
    protected void init() {
        // Calculate centered box position (not pixel-perfect but works well enough)
        this.boxX = (this.width - boxWidth) / 2;
        this.boxY = (this.height - boxHeight) / 2;

        // Add a close button in bottom-right corner
        ButtonWidget closeBtn = ButtonWidget.builder(Text.literal("Close"), btn -> this.close())
            .dimensions(boxX + boxWidth - 70, boxY + boxHeight - 110, 50, 20)
            .build();

        this.addDrawableChild(closeBtn);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dims the background behind our GUI
        this.renderBackground(ctx, mouseX, mouseY, delta);

        // Draw textured backdrop — using Minecraft's demo screen look
        ctx.drawTexture(RenderLayer::getGuiTextured, BACKGROUND_TEXTURE, boxX, boxY, 0f, 0f, boxWidth, boxHeight, 400, 320);

        // Render any child widgets like buttons
        super.render(ctx, mouseX, mouseY, delta);

        // Title — always centered at the top
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, boxX + boxWidth / 2, boxY + 8, 0xFFFFFF);

        // Begin drawing lines of info below the title
        int leftPadding = boxX + 20;
        int cursorY = boxY + 40;
        int lineSpacing = 12;

        // Keybinds section
        ctx.drawText(this.textRenderer, Text.literal("Keybinds:").formatted(Formatting.YELLOW), leftPadding, cursorY, 0xFFFFFF, false);
        cursorY += lineSpacing + 5;
        ctx.drawText(this.textRenderer, Text.literal("H - Toggle Hitbox ESP"), leftPadding + 10, cursorY, 0xFFFFFF, false);
        cursorY += lineSpacing;
        ctx.drawText(this.textRenderer, Text.literal("N - Toggle Name ESP"), leftPadding + 10, cursorY, 0xFFFFFF, false);
        cursorY += lineSpacing;
        ctx.drawText(this.textRenderer, Text.literal("J - Open Settings GUI"), leftPadding + 10, cursorY, 0xFFFFFF, false);

        cursorY += lineSpacing + 10;

        // Commands section
        ctx.drawText(this.textRenderer, Text.literal("Commands:").formatted(Formatting.YELLOW), leftPadding, cursorY, 0xFFFFFF, false);
        cursorY += lineSpacing + 5;
        ctx.drawText(this.textRenderer, Text.literal("/pesp on/off - Toggle ESP"), leftPadding + 10, cursorY, 0xFFFFFF, false);
        cursorY += lineSpacing;
        ctx.drawText(this.textRenderer, Text.literal("/pesp color <r> <g> <b> [a] - Set color"), leftPadding + 10, cursorY, 0xFFFFFF, false);
        cursorY += lineSpacing;
        ctx.drawText(this.textRenderer, Text.literal("/pesp whitelist <player> - Add to whitelist"), leftPadding + 10, cursorY, 0xFFFFFF, false);
        cursorY += lineSpacing;
        ctx.drawText(this.textRenderer, Text.literal("/pesp whitelist-remove <player> - Remove"), leftPadding + 10, cursorY, 0xFFFFFF, false);
        cursorY += lineSpacing;
        ctx.drawText(this.textRenderer, Text.literal("/pesp status - Show current settings"), leftPadding + 10, cursorY, 0xFFFFFF, false);

        cursorY += lineSpacing + 10;

        // Footer note
        ctx.drawText(this.textRenderer,
            Text.literal("PlayerESP v1.0.0 by bitArtisan1").formatted(Formatting.GRAY),
            leftPadding, cursorY, 0xAAAAAA, true);
    }

    @Override
    public boolean shouldPause() {
        return false; // Keep the game running behind the GUI
    }

    @Override
    public void close() {
        this.client.setScreen(previousScreen);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {  // ESC key
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
