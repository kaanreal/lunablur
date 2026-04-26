package dev.lunablur;

import net.fabricmc.api.ClientModInitializer;

public final class LunaBlurClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new LunaBlur().initialize();
        LunaBlurKeybinds.register();
    }
}
