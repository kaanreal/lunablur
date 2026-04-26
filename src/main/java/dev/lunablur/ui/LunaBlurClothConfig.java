package dev.lunablur.ui;

import dev.lunablur.LunaBlur;
import dev.lunablur.LunaBlurConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class LunaBlurClothConfig {
    private LunaBlurClothConfig() {
    }

    public static Screen create(Screen parent) {
        LunaBlur mod = LunaBlur.getInstance();
        LunaBlurConfig config = mod.getConfig();

        AtomicBoolean enabled = new AtomicBoolean(config.isEnabled());
        AtomicInteger strength = new AtomicInteger(config.getStrengthPercent());

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("screen.lunablur.title"))
                .setSavingRunnable(() -> {
                    mod.updateEnabled(enabled.get());
                    mod.updateStrength(strength.get());
                });

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("screen.lunablur.category.general"));
        ConfigEntryBuilder entries = builder.entryBuilder();

        general.addEntry(entries.startBooleanToggle(Text.translatable("option.lunablur.enabled"), enabled.get())
                .setDefaultValue(true)
                .setSaveConsumer(enabled::set)
                .build());

        general.addEntry(entries.startIntSlider(Text.translatable("option.lunablur.strength"), strength.get(), 0, 100)
                .setDefaultValue(50)
                .setSaveConsumer(strength::set)
                .build());

        return builder.build();
    }
}
