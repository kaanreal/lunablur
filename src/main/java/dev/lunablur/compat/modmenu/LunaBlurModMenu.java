package dev.lunablur.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.lunablur.ui.LunaBlurClothConfig;

public final class LunaBlurModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LunaBlurClothConfig::create;
    }
}
