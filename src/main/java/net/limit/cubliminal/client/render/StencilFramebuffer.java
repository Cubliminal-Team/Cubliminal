package net.limit.cubliminal.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class StencilFramebuffer extends Framebuffer {

    private int stencilAttachment = -1;
    private final boolean useStencilAttachment;

    public StencilFramebuffer(boolean useDepth, boolean useStencil) {
        super(useDepth);
        this.useStencilAttachment = useStencil;
    }

    @Override
    public void initFbo(int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        int maxSize = RenderSystem.maxSupportedTextureSize();
        if (width > 0 && width <= maxSize && height > 0 && height <= maxSize) {
            this.viewportWidth = width;
            this.viewportHeight = height;
            this.textureWidth = width;
            this.textureHeight = height;
            this.fbo = GlStateManager.glGenFramebuffers();
            this.colorAttachment = TextureUtil.generateTextureId();

            // Setup color texture
            GlStateManager._bindTexture(this.colorAttachment);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager._texImage2D(
                    GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height,
                    0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null
            );

            if (this.useDepthAttachment || this.useStencilAttachment) {
                // Create combined depth-stencil renderbuffer
                this.depthAttachment = GlStateManager.glGenRenderbuffers();
                GlStateManager._glBindRenderbuffer(GL30.GL_RENDERBUFFER, this.depthAttachment);

                if (this.useStencilAttachment) {
                    // Use packed depth-stencil format
                    GL30.glRenderbufferStorage(
                            GL30.GL_RENDERBUFFER,
                            GL30.GL_DEPTH24_STENCIL8,
                            width,
                            height
                    );
                } else {
                    // Regular depth only
                    GlStateManager._glRenderbufferStorage(
                            GL30.GL_RENDERBUFFER,
                            GL11.GL_DEPTH_COMPONENT,
                            width,
                            height
                    );
                }
            }

            // Attach to FBO
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.fbo);
            GlStateManager._glFramebufferTexture2D(
                    GL30.GL_FRAMEBUFFER,
                    GL30.GL_COLOR_ATTACHMENT0,
                    GL11.GL_TEXTURE_2D,
                    this.colorAttachment,
                    0
            );

            if (this.useDepthAttachment || this.useStencilAttachment) {
                if (this.useStencilAttachment) {
                    // Attach combined depth-stencil
                    GL30.glFramebufferRenderbuffer(
                            GL30.GL_FRAMEBUFFER,
                            GL30.GL_DEPTH_STENCIL_ATTACHMENT,
                            GL30.GL_RENDERBUFFER,
                            this.depthAttachment
                    );
                } else {
                    // Attach depth only
                    GlStateManager._glFramebufferRenderbuffer(
                            GL30.GL_FRAMEBUFFER,
                            GL30.GL_DEPTH_ATTACHMENT,
                            GL30.GL_RENDERBUFFER,
                            this.depthAttachment
                    );
                }
            }

            this.checkFramebufferStatus();
            this.clear();
            this.endRead();
        } else {
            throw new IllegalArgumentException("Invalid framebuffer size: " + width + "x" + height);
        }
    }

    @Override
    public void delete() {
        super.delete();
        if (this.stencilAttachment > -1) {
            GlStateManager._glDeleteRenderbuffers(this.stencilAttachment);
            this.stencilAttachment = -1;
        }
    }

    @Override
    public void clear() {
        RenderSystem.assertOnRenderThreadOrInit();
        this.beginWrite(true);

        int mask = GL11.GL_COLOR_BUFFER_BIT;
        if (this.useDepthAttachment) {
            mask |= GL11.GL_DEPTH_BUFFER_BIT;
        }
        if (this.useStencilAttachment) {
            mask |= GL11.GL_STENCIL_BUFFER_BIT;
        }

        GlStateManager._clearColor(
                this.clearColor[0],
                this.clearColor[1],
                this.clearColor[2],
                this.clearColor[3]
        );
        if (this.useDepthAttachment) {
            GlStateManager._clearDepth(1.0);
        }
        if (this.useStencilAttachment) {
            GL11.glClearStencil(0);
        }

        GlStateManager._clear(mask);
        this.endWrite();
    }

    public boolean hasStencil() {
        return this.useStencilAttachment;
    }
}
