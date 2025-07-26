package net.limit.cubliminal.client.render;


import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.block.custom.TheLobbyGatewayBlock;
import net.limit.cubliminal.block.entity.TheLobbyGatewayBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@Environment(EnvType.CLIENT)
public class PortalRenderer implements BlockEntityRenderer<TheLobbyGatewayBlockEntity> {

    private boolean rendering = false;
    private StencilFramebuffer portalFBO;

    public PortalRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(TheLobbyGatewayBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!rendering && entity.getCachedState().get(TheLobbyGatewayBlock.LIT)) {
            rendering = true;
            MinecraftClient client = MinecraftClient.getInstance();
            if (portalFBO == null || portalFBO.textureWidth != client.getFramebuffer().textureWidth ||
                    portalFBO.textureHeight != client.getFramebuffer().textureHeight) {
                if (portalFBO != null) portalFBO.delete();
                portalFBO = new StencilFramebuffer(true, true);
                portalFBO.resize(
                        client.getFramebuffer().textureWidth,
                        client.getFramebuffer().textureHeight
                );
                portalFBO.setClearColor(0, 0, 0, 0);
            }

            int prevFBO = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            portalFBO.beginWrite(true);
            RenderSystem.clearColor(0, 0, 0, 0);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
            GL11.glStencilMask(0xFF);
            RenderSystem.colorMask(false, false, false, false); // Disable writing to the color buffer

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            // Render the full cube to define the mask
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

            matrices.push();
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            this.renderQuad(entity, positionMatrix, buffer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, Direction.SOUTH);
            this.renderQuad(entity, positionMatrix, buffer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, Direction.NORTH);
            this.renderQuad(entity, positionMatrix, buffer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.EAST);
            this.renderQuad(entity, positionMatrix, buffer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.WEST);
            this.renderQuad(entity, positionMatrix, buffer, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, Direction.DOWN);
            this.renderQuad(entity, positionMatrix, buffer, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, Direction.UP);

            RenderSystem.setShader(ShaderProgramKeys.POSITION);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);

            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            GL11.glStencilMask(0x00);

            BufferBuilder buffer2 = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            int r = 255, g = 0, b = 0, a = 255;
            buffer2.vertex(positionMatrix, 0.0f, 2.0f, 0.0f).color(r, g, b, a);
            buffer2.vertex(positionMatrix, 0.5f, 0.2f, 0.9f).color(g, r, b, a);
            buffer2.vertex(positionMatrix, 1.0f, 0.5f, 0.5f).color(b, g, r, a);

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferRenderer.drawWithGlobalProgram(buffer2.end());

            GL11.glDisable(GL11.GL_STENCIL_TEST);
            matrices.pop();

            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFBO);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            portalFBO.draw(portalFBO.textureWidth, portalFBO.textureHeight);
            RenderSystem.disableBlend();
        }
        rendering = false;
    }

    private void renderQuad(TheLobbyGatewayBlockEntity entity, Matrix4f model, BufferBuilder buffer,
                            float x1, float x2, float y1, float y2, float z1, float z2, float z3,
                            float z4, Direction side) {
        if (entity.shouldDrawSide(side)) {
            int r = 255, g = 0, b = 0, a = 255;
            buffer.vertex(model, x1, y1, z1).color(r, g, b, a);
            buffer.vertex(model, x2, y1, z2).color(r, g, b, a);
            buffer.vertex(model, x2, y2, z3).color(r, g, b, a);
            buffer.vertex(model, x1, y2, z4).color(r, g, b, a);
        }
    }
}