package dev.lunablur;

import dev.lunablur.ui.LunaBlurClothConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class LunaBlurKeybinds {
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(LunaBlur.MOD_ID, "controls"));
    private static final KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.lunablur.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
    ));
    private static final KeyBinding TOGGLE_BLUR = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.lunablur.toggle_blur",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    ));

    private LunaBlurKeybinds() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.wasPressed()) {
                client.setScreen(LunaBlurClothConfig.create(client.currentScreen));
            }

            while (TOGGLE_BLUR.wasPressed()) {
                LunaBlur mod = LunaBlur.getInstance();
                boolean enabled = !mod.getConfig().isEnabled();
                mod.updateEnabled(enabled);
                if (client.player != null) {
                    client.player.sendMessage(
                            net.minecraft.text.Text.translatable("message.lunablur.toggled", enabled ? net.minecraft.text.Text.translatable("options.on") : net.minecraft.text.Text.translatable("options.off")),
                            true
                    );
                }
            }
        });
    }
}
