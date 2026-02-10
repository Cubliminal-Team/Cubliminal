package net.limit.cubliminal.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(DrawContext.class)
public interface DrawContextAccessor {

    @Accessor("vertexConsumers")
    VertexConsumerProvider.Immediate getConsumers();

}
