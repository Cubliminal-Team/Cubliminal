package net.limit.cubliminal.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.IniterClient;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.RenderPhase.*;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RenderLayers implements IniterClient {
    public static final ShaderProgramKey RENDERTYPE_CUBLIMINAL_MANILA_SKYBOX = ShaderProgramKeys.register(
            "rendertype_manila_skybox", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);

    public static final ShaderProgramKey RENDERTYPE_FLUX_CAPACITOR = ShaderProgramKeys.register(
            "rendertype_flux_capacitor", VertexFormats.POSITION_COLOR);

    public static final ShaderProgram MANILA_PROGRAM = new ShaderProgram(RENDERTYPE_CUBLIMINAL_MANILA_SKYBOX);
    public static final ShaderProgram FLUX_CAPACITOR_PROGRAM = new ShaderProgram(RENDERTYPE_FLUX_CAPACITOR);

    private static final RenderLayer MANILA = RenderLayer.of(
            "manila",
            VertexFormats.POSITION,
            VertexFormat.DrawMode.QUADS,
            1536,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(MANILA_PROGRAM)
                    .texture(createCubemap(Cubliminal.id("manila"), false, false))
                    .cull(RenderPhase.ENABLE_CULLING)
                    .build(false));

    private static final RenderLayer FLUX_CAPACITOR = RenderLayer.of(
            "flux_capacitor",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS,
            1536,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(FLUX_CAPACITOR_PROGRAM)
                    .build(false));


    public static RenderLayer getManilaSkybox() {
        return MANILA;
    }

    public static RenderLayer getFluxCapacitor() {
        return FLUX_CAPACITOR;
    }

    public static TextureBase createCubemap(Identifier texture, boolean blur, boolean mipmap) {
        Textures.Builder builder = RenderPhase.Textures.create();
        for (int i = 0; i < 6; i++) {
            builder.add(Identifier.of(texture.getNamespace(), "textures/cubemap/" + texture.getPath() + "_" + i + ".png"), blur, mipmap);
        }
        return builder.build();
    }
}
