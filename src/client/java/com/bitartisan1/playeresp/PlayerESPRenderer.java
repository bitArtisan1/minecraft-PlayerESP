package com.bitartisan1.playeresp;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class PlayerESPRenderer implements WorldRenderEvents.AfterTranslucent {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    @Override
    public void afterTranslucent(WorldRenderContext context) {
        if (mc.world == null || mc.player == null) return;
        
        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();
        float tickDelta = context.tickCounter().getTickDelta(true);
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            
            // Check whitelist
            if (!PlayerESPCommand.isWhitelisted(player.getName().getString())) {
                continue;
            }
            
            double x = MathHelper.lerp(tickDelta, player.prevX, player.getX()) - cameraPos.x;
            double y = MathHelper.lerp(tickDelta, player.prevY, player.getY()) - cameraPos.y;
            double z = MathHelper.lerp(tickDelta, player.prevZ, player.getZ()) - cameraPos.z;
            
            matrices.push();
            matrices.translate(x, y, z);
            
            if (PlayerESPClient.isShowHitbox()) {
                renderHitbox(matrices, player);
            }
            
            if (PlayerESPClient.isShowName()) {
                renderPlayerName(matrices, player, context.camera().getYaw(), context.camera().getPitch());
            }
            
            matrices.pop();
        }
    }
    
    private void renderHitbox(MatrixStack matrices, PlayerEntity player) {
        matrices.push();
        
        // Get color based on player name
        float[] color = getColorFromName(player.getDisplayName().getString());
        
        // Setup rendering state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        float width = player.getWidth();
        float height = player.getHeight();
        
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        // Bottom rectangle
        addLine(buffer, matrix, -width/2, 0, -width/2, -width/2, 0, width/2, color);
        addLine(buffer, matrix, -width/2, 0, width/2, width/2, 0, width/2, color);
        addLine(buffer, matrix, width/2, 0, width/2, width/2, 0, -width/2, color);
        addLine(buffer, matrix, width/2, 0, -width/2, -width/2, 0, -width/2, color);
        
        // Top rectangle
        addLine(buffer, matrix, -width/2, height, -width/2, -width/2, height, width/2, color);
        addLine(buffer, matrix, -width/2, height, width/2, width/2, height, width/2, color);
        addLine(buffer, matrix, width/2, height, width/2, width/2, height, -width/2, color);
        addLine(buffer, matrix, width/2, height, -width/2, -width/2, height, -width/2, color);
        
        // Vertical lines
        addLine(buffer, matrix, -width/2, 0, -width/2, -width/2, height, -width/2, color);
        addLine(buffer, matrix, -width/2, 0, width/2, -width/2, height, width/2, color);
        addLine(buffer, matrix, width/2, 0, width/2, width/2, height, width/2, color);
        addLine(buffer, matrix, width/2, 0, -width/2, width/2, height, -width/2, color);
        
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        
        // Reset rendering state
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        
        matrices.pop();
    }
    
    private void addLine(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float[] color) {
        buffer.vertex(matrix, x1, y1, z1).color(color[0], color[1], color[2], 1.0f);
        buffer.vertex(matrix, x2, y2, z2).color(color[0], color[1], color[2], 1.0f);
    }
    
    private void renderPlayerName(MatrixStack matrices, PlayerEntity player, float cameraYaw, float cameraPitch) {
        matrices.push();
        
        // Move to above player's head
        matrices.translate(0, player.getHeight() + 0.5, 0);
        
        // Face the camera
        matrices.multiply(new org.joml.Quaternionf().rotationY(-cameraYaw * 0.017453292F));
        matrices.multiply(new org.joml.Quaternionf().rotationX(cameraPitch * 0.017453292F));
        
        // Scale based on distance
        double distance = mc.player.distanceTo(player);
        float scale = (float) Math.max(0.02, Math.min(distance * 0.002, 0.1));
        matrices.scale(-scale, -scale, scale);
        
        TextRenderer textRenderer = mc.textRenderer;
        Text displayName = player.getDisplayName();
        String nameText = displayName.getString();
        
        int textWidth = textRenderer.getWidth(nameText);
        int x = -textWidth / 2;
        int y = 0;
        
        // Setup rendering
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        
        // Draw background
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        // Background rectangle
        int padding = 2;
        buffer.vertex(matrix, x - padding, y - padding, 0).color(0, 0, 0, 0.5f);
        buffer.vertex(matrix, x - padding, y + textRenderer.fontHeight + padding, 0).color(0, 0, 0, 0.5f);
        buffer.vertex(matrix, x + textWidth + padding, y + textRenderer.fontHeight + padding, 0).color(0, 0, 0, 0.5f);
        buffer.vertex(matrix, x + textWidth + padding, y - padding, 0).color(0, 0, 0, 0.5f);
        
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        
        // Draw text with proper vertex consumer
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // Use immediate vertex consumer for proper text rendering
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        textRenderer.draw(nameText, x, y, 0xFFFFFF, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
        immediate.draw();
        
        // Reset rendering state
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        
        matrices.pop();
    }
    
    private float[] getColorFromName(String name) {
        // Check if custom color is set
        float[] customColor = PlayerESPCommand.getCustomColor();
        if (customColor != null) {
            return new float[]{customColor[0], customColor[1], customColor[2]};
        }
        
        // Check for formatting codes in the name
        for (Formatting format : Formatting.values()) {
            if (name.contains(format.toString())) {
                return getColorFromFormatting(format);
            }
        }
        
        // Default to red if no formatting found
        return new float[]{1.0f, 0.0f, 0.0f};
    }
    
    private float[] getColorFromFormatting(Formatting format) {
        switch (format) {
            case BLACK: return new float[]{0.0f, 0.0f, 0.0f};
            case DARK_BLUE: return new float[]{0.0f, 0.0f, 0.5f};
            case DARK_GREEN: return new float[]{0.0f, 0.5f, 0.0f};
            case DARK_AQUA: return new float[]{0.0f, 0.5f, 0.5f};
            case DARK_RED: return new float[]{0.5f, 0.0f, 0.0f};
            case DARK_PURPLE: return new float[]{0.5f, 0.0f, 0.5f};
            case GOLD: return new float[]{1.0f, 0.5f, 0.0f};
            case GRAY: return new float[]{0.5f, 0.5f, 0.5f};
            case DARK_GRAY: return new float[]{0.25f, 0.25f, 0.25f};
            case BLUE: return new float[]{0.3f, 0.3f, 1.0f};
            case GREEN: return new float[]{0.3f, 1.0f, 0.3f};
            case AQUA: return new float[]{0.3f, 1.0f, 1.0f};
            case RED: return new float[]{1.0f, 0.3f, 0.3f};
            case LIGHT_PURPLE: return new float[]{1.0f, 0.3f, 1.0f};
            case YELLOW: return new float[]{1.0f, 1.0f, 0.3f};
            case WHITE: default: return new float[]{1.0f, 1.0f, 1.0f};
        }
    }
}
