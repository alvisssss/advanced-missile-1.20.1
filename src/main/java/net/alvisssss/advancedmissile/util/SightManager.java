package net.alvisssss.advancedmissile.util;

import java.util.UUID;

public class SightManager {
    public enum ZoomLevel {
        ONE_X(1.0f),
        TWO_X(2.0f),
        FOUR_X(4.0f),
        TWELVE_X(12.0f);

        private final float factor;

        ZoomLevel(float factor) {
            this.factor = factor;
        }

        public float getFactor() {
            return factor;
        }

        public float getReciprocal() {
            return 1.0f / factor;
        }

        public ZoomLevel getNext() {
            return values()[(this.ordinal() + 1) % values().length];
        }

        public ZoomLevel getPrevious() {
            return values()[(this.ordinal() - 1 + values().length) % values().length];
        }
    }

    private static ZoomLevel currentZoomLevel = ZoomLevel.ONE_X;

    private static UUID targetUuid = UUID.randomUUID();

    public static float getZoomLevelReciprocal() {
        return currentZoomLevel.getReciprocal();
    }

    public static void resetZoomLevel() {
        currentZoomLevel = ZoomLevel.ONE_X;
    }

    public static void changeZoomLevel(double scrollAmount) {
        if (scrollAmount > 0) {
            currentZoomLevel = currentZoomLevel.getNext();
        } else if (scrollAmount < 0) {
            currentZoomLevel = currentZoomLevel.getPrevious();
        }
    }

    public static UUID getTargetUuid() {
        return targetUuid;
    }
    public static void setTargetUuid(UUID uuid) {
        targetUuid = uuid;
    }

    public static void resetTargetUuid() {
        targetUuid = null;
    }

}
