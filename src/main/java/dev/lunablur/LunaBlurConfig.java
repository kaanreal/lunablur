package dev.lunablur;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LunaBlurConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("lunablur.json");

    private boolean enabled = true;
    private int strengthPercent = 50;

    public static LunaBlurConfig load() {
        if (Files.notExists(PATH)) {
            LunaBlurConfig config = new LunaBlurConfig();
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            LunaBlurConfig config = GSON.fromJson(reader, LunaBlurConfig.class);
            if (config == null) {
                config = new LunaBlurConfig();
            }
            config.strengthPercent = clampPercent(config.strengthPercent);
            config.save();
            return config;
        } catch (IOException | JsonParseException exception) {
            LunaBlur.LOGGER.warn("Failed to load LunaBlur config, using defaults", exception);
            LunaBlurConfig config = new LunaBlurConfig();
            config.save();
            return config;
        }
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            LunaBlur.LOGGER.error("Failed to save LunaBlur config", exception);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getStrengthPercent() {
        return strengthPercent;
    }

    public void setStrengthPercent(int strengthPercent) {
        this.strengthPercent = clampPercent(strengthPercent);
    }

    public float getBlendFactor() {
        return Math.min(strengthPercent, 99) / 100.0F;
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
