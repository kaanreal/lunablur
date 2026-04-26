package dev.lunablur;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lunablur.ui.LunaBlurClothConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.UniformValue;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import dev.lunablur.mixin.PostEffectPassAccessor;
import dev.lunablur.mixin.PostEffectProcessorAccessor;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LunaBlur {
    public static final String MOD_ID = "lunablur";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Identifier EFFECT_ID = Identifier.of(MOD_ID, "motion_blur");
    private static final Set<Identifier> EXTERNAL_TARGETS = Set.of(PostEffectProcessor.MAIN);
    private static LunaBlur instance;

    private final LunaBlurConfig config;
    private PostEffectProcessor currentProcessor;
    private float appliedBlendFactor = -1.0F;

    public LunaBlur() {
        this.config = LunaBlurConfig.load();
        instance = this;
    }

    public void initialize() {
        registerCommands();
    }

    public static LunaBlur getInstance() {
        return instance;
    }

    public static void render(MinecraftClient client, ObjectAllocator allocator) {
        if (instance != null) {
            instance.renderInternal(client, allocator);
        }
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("lunablur")
                        .executes(context -> openMenu(context.getSource()))
                        .then(ClientCommandManager.argument("percent", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 100))
                                .executes(context -> setStrength(
                                        context.getSource(),
                                        com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "percent")
                                )))
        ));
    }

    private int openMenu(FabricClientCommandSource source) {
        MinecraftClient client = source.getClient();
        client.send(() -> client.setScreen(LunaBlurClothConfig.create(client.currentScreen)));
        return 1;
    }

    private int setStrength(FabricClientCommandSource source, int percent) {
        updateStrength(percent);
        source.sendFeedback(Text.literal("LunaBlur strength set to " + config.getStrengthPercent() + "%"));
        return 1;
    }

    public LunaBlurConfig getConfig() {
        return config;
    }

    public void updateEnabled(boolean enabled) {
        config.setEnabled(enabled);
        config.save();
        appliedBlendFactor = -1.0F;
    }

    public void updateStrength(int percent) {
        config.setStrengthPercent(percent);
        config.save();
        appliedBlendFactor = -1.0F;
    }

    private boolean shouldRender() {
        return config.isEnabled() && config.getStrengthPercent() > 0;
    }

    private float getBlendFactor() {
        return config.getBlendFactor();
    }

    private void renderInternal(MinecraftClient client, ObjectAllocator allocator) {
        if (!shouldRender() || client.world == null || client.player == null) {
            return;
        }

        PostEffectProcessor processor = client.getShaderLoader().loadPostEffect(EFFECT_ID, EXTERNAL_TARGETS);
        if (processor == null) {
            return;
        }

        float blendFactor = getBlendFactor();
        if (processor != currentProcessor || Float.compare(appliedBlendFactor, blendFactor) != 0) {
            applyBlendFactor(processor, blendFactor);
            currentProcessor = processor;
            appliedBlendFactor = blendFactor;
        }

        processor.render(client.getFramebuffer(), allocator);
    }

    private void applyBlendFactor(PostEffectProcessor processor, float blendFactor) {
        GpuBuffer replacement = createBlendFactorBuffer(blendFactor);
        if (replacement == null) {
            return;
        }

        boolean applied = false;
        for (PostEffectPass pass : ((PostEffectProcessorAccessor) processor).lunablur$getPasses()) {
            Map<String, GpuBuffer> uniformBuffers = ((PostEffectPassAccessor) pass).lunablur$getUniformBuffers();
            GpuBuffer previous = uniformBuffers.put("BlurConfig", replacement);
            if (previous != null) {
                previous.close();
                applied = true;
                break;
            }
        }

        if (!applied) {
            replacement.close();
            LOGGER.warn("Could not find BlurConfig uniform buffer in LunaBlur post effect");
        }
    }

    private GpuBuffer createBlendFactorBuffer(float blendFactor) {
        UniformValue.FloatValue value = new UniformValue.FloatValue(blendFactor);
        Std140SizeCalculator calculator = new Std140SizeCalculator();
        value.addSize(calculator);
        int size = calculator.get();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder builder = Std140Builder.onStack(stack, size);
            value.write(builder);
            return RenderSystem.getDevice().createBuffer(
                    () -> "lunablur_blend_factor",
                    GpuBuffer.USAGE_UNIFORM,
                    builder.get()
            );
        } catch (Exception exception) {
            LOGGER.error("Failed to create LunaBlur uniform buffer", exception);
            return null;
        }
    }

}
