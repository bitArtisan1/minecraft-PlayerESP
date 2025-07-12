package com.bitartisan1.playeresp.gui;

import com.bitartisan1.playeresp.PlayerESPClient;
import com.bitartisan1.playeresp.PlayerESPCommand;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class PlayerESPConfigScreen extends Screen {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/demo_background.png");
    private static final Identifier EFFECT_BACKGROUND = Identifier.of("minecraft", "textures/gui/sprites/hud/effect_background.png");
    private final Screen parent;
    private final int windowWidth = 235;
    private final int windowHeight = 166;
    private int windowX, windowY;
    
    private SliderWidget redSlider, greenSlider, blueSlider, alphaSlider;
    private float[] currentColor;
    
    public PlayerESPConfigScreen(Screen parent) {
        super(Text.literal("PlayerESP Settings"));
        this.parent = parent;
        this.currentColor = PlayerESPCommand.getCustomColor();
        if (this.currentColor == null) {
            this.currentColor = new float[]{1.0f, 0.0f, 0.0f, 1.0f}; // Default red
        }
    }
    
    @Override
    protected void init() {
        // Center the window
        this.windowX = (this.width - windowWidth) / 2;
        this.windowY = (this.height - windowHeight) / 2;
        
        int leftColumn = windowX + 8;
        int startY = windowY + 20;
        int spacing = 18;
        int buttonWidth = 160;
        
        // ESP Toggle Buttons
        ButtonWidget hitboxToggle = ButtonWidget.builder(
            Text.literal("Hitbox ESP: " + (PlayerESPClient.isShowHitbox() ? "ON" : "OFF"))
                .formatted(PlayerESPClient.isShowHitbox() ? Formatting.GREEN : Formatting.RED),
            button -> {
                PlayerESPClient.setShowHitbox(!PlayerESPClient.isShowHitbox());
                button.setMessage(Text.literal("Hitbox ESP: " + (PlayerESPClient.isShowHitbox() ? "ON" : "OFF"))
                    .formatted(PlayerESPClient.isShowHitbox() ? Formatting.GREEN : Formatting.RED));
            })
            .dimensions(leftColumn, startY, buttonWidth, 16)
            .build();
        this.addDrawableChild(hitboxToggle);
        
        ButtonWidget nameToggle = ButtonWidget.builder(
            Text.literal("Name ESP: " + (PlayerESPClient.isShowName() ? "ON" : "OFF"))
                .formatted(PlayerESPClient.isShowName() ? Formatting.GREEN : Formatting.RED),
            button -> {
                PlayerESPClient.setShowName(!PlayerESPClient.isShowName());
                button.setMessage(Text.literal("Name ESP: " + (PlayerESPClient.isShowName() ? "ON" : "OFF"))
                    .formatted(PlayerESPClient.isShowName() ? Formatting.GREEN : Formatting.RED));
            })
            .dimensions(leftColumn, startY + spacing, buttonWidth, 16)
            .build();
        this.addDrawableChild(nameToggle);
        
        // Color Sliders
        this.redSlider = new SliderWidget(leftColumn, startY + spacing * 2 + 5, buttonWidth, 16, 
            Text.literal("Red: " + String.format("%.2f", currentColor[0])), currentColor[0]) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Red: " + String.format("%.2f", value)));
            }
            
            @Override
            protected void applyValue() {
                currentColor[0] = (float) value;
                updateColor();
            }
        };
        this.addDrawableChild(redSlider);
        
        this.greenSlider = new SliderWidget(leftColumn, startY + spacing * 3 + 5, buttonWidth, 16, 
            Text.literal("Green: " + String.format("%.2f", currentColor[1])), currentColor[1]) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Green: " + String.format("%.2f", value)));
            }
            
            @Override
            protected void applyValue() {
                currentColor[1] = (float) value;
                updateColor();
            }
        };
        this.addDrawableChild(greenSlider);
        
        this.blueSlider = new SliderWidget(leftColumn, startY + spacing * 4 + 5, buttonWidth, 16, 
            Text.literal("Blue: " + String.format("%.2f", currentColor[2])), currentColor[2]) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Blue: " + String.format("%.2f", value)));
            }
            
            @Override
            protected void applyValue() {
                currentColor[2] = (float) value;
                updateColor();
            }
        };
        this.addDrawableChild(blueSlider);
        
        this.alphaSlider = new SliderWidget(leftColumn, startY + spacing * 5 + 5, buttonWidth, 16, 
            Text.literal("Alpha: " + String.format("%.2f", currentColor[3])), currentColor[3]) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Alpha: " + String.format("%.2f", value)));
            }
            
            @Override
            protected void applyValue() {
                currentColor[3] = (float) value;
                updateColor();
            }
        };
        this.addDrawableChild(alphaSlider);
        
        // Action Buttons
        ButtonWidget whitelistButton = ButtonWidget.builder(
            Text.literal("Manage Whitelist"),
            button -> this.client.setScreen(new WhitelistManagementScreen(this)))
            .dimensions(leftColumn, startY + spacing * 6 + 10, 100, 16)
            .build();
        this.addDrawableChild(whitelistButton);
        
        ButtonWidget infoButton = ButtonWidget.builder(
            Text.literal("Info"),
            button -> this.client.setScreen(new InfoScreen(this)))
            .dimensions(leftColumn + 105, startY + spacing * 6 + 10, 40, 16)
            .build();
        this.addDrawableChild(infoButton);
        
        ButtonWidget resetButton = ButtonWidget.builder(
            Text.literal("Reset"),
            button -> {
                PlayerESPCommand.setCustomColor(null);
                this.client.setScreen(new PlayerESPConfigScreen(parent));
            })
            .dimensions(leftColumn + 150, startY + spacing * 6 + 10, 48, 16)
            .build();
        this.addDrawableChild(resetButton);
        
        // Close Button
        ButtonWidget closeButton = ButtonWidget.builder(
            Text.literal("✕"),
            button -> this.close())
            .dimensions(windowX + windowWidth - 30, windowY + 4, 16, 16)
            .build();
        this.addDrawableChild(closeButton);
    }
    
    private void updateColor() {
        PlayerESPCommand.setCustomColor(currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render the dimmed background
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Draw the vanilla chest GUI texture
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, windowX, windowY, 0.0f, 0.0f, windowWidth, windowHeight, 235, 256);
        
        // Render widgets
        super.render(context, mouseX, mouseY, delta);
        
        // Draw title in the center of the window
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, windowX + windowWidth / 2, windowY + 6, 0xFFFFFF);
        
        // Draw color preview with effect background
        int previewX = windowX + 185;
        int previewY = windowY + 60;
        int effectBgSize = 24; // Standard effect background size
        int colorPreviewSize = 18; // Color preview inside the effect background
        
        // Draw the effect background (item slot style)
        context.drawTexture(RenderLayer::getGuiTextured, EFFECT_BACKGROUND, previewX, previewY, 0.0f, 0.0f, effectBgSize, effectBgSize, 24, 24);
        
        // Draw color preview inside the effect background
        int colorRGB = ((int)(currentColor[0] * 255) << 16) | 
                      ((int)(currentColor[1] * 255) << 8) | 
                      (int)(currentColor[2] * 255);
        int colorWithAlpha = ((int)(currentColor[3] * 255) << 24) | colorRGB;
        
        int colorX = previewX + 3; // Offset to center inside effect background
        int colorY = previewY + 3;
        context.fill(colorX, colorY, colorX + colorPreviewSize, colorY + colorPreviewSize, colorWithAlpha);

    }
    
    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game
    }
    
    @Override
    public void close() {
        this.client.setScreen(parent);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}