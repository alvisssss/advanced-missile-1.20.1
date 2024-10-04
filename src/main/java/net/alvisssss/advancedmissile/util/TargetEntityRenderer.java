package net.alvisssss.advancedmissile.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.UUID;

public class TargetEntityRenderer {

    private static HashMap<UUID, Double> uuidDoubleHashMap = new HashMap<>();

    public static void resetUuidDoubleHashMap() {
        if (!uuidDoubleHashMap.isEmpty()) {
            uuidDoubleHashMap.clear();
        }

    }

    public static HashMap<UUID, Double> getUuidDoubleHashMap() {
        return uuidDoubleHashMap;
    }

    public static void drawBoundingBoxesForAllEntities(MinecraftClient client, DrawContext drawContext, float tickDelta) {
        for (Entity entity : client.world.getEntities()) {
            if (entity != client.player
                    && entity instanceof LivingEntity
                    && client.player != null
                    && client.player.canSee(entity)
            ) {
                drawBoundingBoxForEntity(client, (LivingEntity) entity, drawContext, tickDelta);
            }
        }
    }

    private static void drawBoundingBoxForEntity(MinecraftClient client, LivingEntity entity, DrawContext drawContext, float tickDelta) {

        if (client.player == null) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        double aspectRatio = (double) screenWidth / screenHeight;

        Box boundingBox = entity.getBoundingBox();
        Vec3d entityCenter = new Vec3d(
                (boundingBox.minX + boundingBox.maxX) / 2,
                (boundingBox.minY + boundingBox.maxY) / 2,
                (boundingBox.minZ + boundingBox.maxZ) / 2
        );

        Vec3d cameraPos = client.player.getEyePos();
        Vec3d relativePos = entityCenter.subtract(cameraPos);

        float vfov = client.options.getFov().getValue() * SightManager.getZoomLevelReciprocal();
        double vfovInRadians = Math.toRadians(vfov);
        double hfovInRadians = 2 * Math.atan(Math.tan(vfovInRadians / 2) * aspectRatio);

        Vec3d forward = client.player.getRotationVec(tickDelta);

        Vec3d right = new Vec3d(Math.cos(Math.toRadians(client.player.getYaw(tickDelta))), 0, Math.sin(Math.toRadians(client.player.getYaw(tickDelta))));
        Vec3d up = forward.crossProduct(right);

        double xView = relativePos.dotProduct(right);
        double yView = relativePos.dotProduct(up);
        double zView = relativePos.dotProduct(forward);

        if (zView < 0) return;

        double xProjected = -(xView / (zView * Math.tan(hfovInRadians / 2)));
        double yProjected = yView / (zView * Math.tan(vfovInRadians / 2));

        if (Math.abs(yProjected) > 1.0 || Math.abs(xProjected) > 1.0) return;

        int yScreen = (int) ((screenHeight / 2) * (1 - yProjected));
        int xScreen = (int) ((screenWidth / 2) * (1 + xProjected));

        float squareSizeFactor = 0.1f;
        int squareSize = (int) (Math.min(screenWidth, screenHeight) * squareSizeFactor);
        int halfSize = squareSize / 2;

        int color = entity.getUuid().equals(SightManager.getTargetUuid())?
                0xffff0000 : 0xffffffff;

        drawTargetSquare(drawContext, xScreen, yScreen, squareSize, color);

        int crosshairX = screenWidth/2;
        int crosshairY = screenHeight/2;

        if (crosshairX >= (xScreen-halfSize) && crosshairX <= (xScreen+halfSize)
                && crosshairY >= (yScreen-halfSize) && crosshairY <= (yScreen+halfSize)) {
            uuidDoubleHashMap.put(entity.getUuid(),cameraPos.squaredDistanceTo(entityCenter));
        }
    }

    private static void drawTargetSquare(DrawContext context, int x, int y, int size, int color) {
        int halfSize = size / 2;
        int cornerLength = halfSize / 2;
        // Draw the four corners of the square
        // Top-left corner
        context.fill(x - halfSize, y - halfSize, x - halfSize + cornerLength, y - halfSize + 1, color); // Horizontal line
        context.fill(x - halfSize, y - halfSize, x - halfSize + 1, y - halfSize + cornerLength, color); // Vertical line

        // Top-right corner
        context.fill(x + halfSize - cornerLength, y - halfSize, x + halfSize, y - halfSize + 1, color); // Horizontal line
        context.fill(x + halfSize - 1, y - halfSize, x + halfSize, y - halfSize + cornerLength, color); // Vertical line

        // Bottom-left corner
        context.fill(x - halfSize, y + halfSize - 1, x - halfSize + cornerLength, y + halfSize, color); // Horizontal line
        context.fill(x - halfSize, y + halfSize - cornerLength, x - halfSize + 1, y + halfSize, color); // Vertical line

        // Bottom-right corner
        context.fill(x + halfSize - cornerLength, y + halfSize - 1, x + halfSize, y + halfSize, color); // Horizontal line
        context.fill(x + halfSize - 1, y + halfSize - cornerLength, x + halfSize, y + halfSize, color); // Vertical line
    }

}
