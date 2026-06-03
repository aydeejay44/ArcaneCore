package me.aydeejay.arcanecore;

public final class ConfigValues {

    private ConfigValues() {
    }

    public static int getInt(ArcaneCore plugin, String path, int fallback, int min, int max) {
        int value = plugin.getConfig().getInt(path, fallback);
        return clamp(value, min, max);
    }

    public static long getLong(ArcaneCore plugin, String path, long fallback, long min, long max) {
        long value = plugin.getConfig().getLong(path, fallback);
        return clamp(value, min, max);
    }

    public static double getDouble(ArcaneCore plugin, String path, double fallback, double min, double max) {
        double value = plugin.getConfig().getDouble(path, fallback);

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return fallback;
        }

        return clamp(value, min, max);
    }

    public static float getFloat(ArcaneCore plugin, String path, float fallback, float min, float max) {
        double value = getDouble(plugin, path, fallback, min, max);
        return (float) value;
    }

    public static int getPotionAmplifier(ArcaneCore plugin, String path, int fallbackLevel) {
        int level = getInt(plugin, path, fallbackLevel, 0, 256);
        return Math.max(0, level - 1);
    }

    public static boolean isPotionEnabled(ArcaneCore plugin, String path, int fallbackLevel) {
        return getInt(plugin, path, fallbackLevel, 0, 256) > 0;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
