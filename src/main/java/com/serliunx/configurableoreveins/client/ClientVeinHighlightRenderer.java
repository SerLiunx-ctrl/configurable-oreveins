package com.serliunx.configurableoreveins.client;

import com.serliunx.configurableoreveins.config.ModConfiguration;
import com.serliunx.configurableoreveins.vein.LocatorVeinInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 客户端矿脉高亮渲染器。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
public class ClientVeinHighlightRenderer {

    /**
     * 处理渲染事件, 是否要高亮显示矿脉
     */
    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        LocatorVeinInfo target = ClientLocatorState.getHighlightedVein();
        Minecraft minecraft = Minecraft.getMinecraft();
        Entity viewer = minecraft.getRenderViewEntity();
        if (target == null
                || viewer == null
                || minecraft.world == null
                || minecraft.world.provider.getDimension() != target.getDimensionId()) {
            return;
        }

        double partialTicks = event.getPartialTicks();
        double viewerX = viewer.lastTickPosX + ((viewer.posX - viewer.lastTickPosX) * partialTicks);
        double viewerY = viewer.lastTickPosY + ((viewer.posY - viewer.lastTickPosY) * partialTicks);
        double viewerZ = viewer.lastTickPosZ + ((viewer.posZ - viewer.lastTickPosZ) * partialTicks);

        AxisAlignedBB box =
                new AxisAlignedBB(
                                target.getCenterX(),
                                target.getCenterY(),
                                target.getCenterZ(),
                                target.getCenterX() + 1.0D,
                                target.getCenterY() + 1.0D,
                                target.getCenterZ() + 1.0D)
                        .offset(-viewerX, -viewerY, -viewerZ)
                        .grow(0.03D);
        float pulse =
                (float)
                        (0.65D
                                + (Math.sin((minecraft.world.getTotalWorldTime() + partialTicks) * 0.18D) * 0.2D));
        int color = target.getHighlightColor();
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(3.0F);
        RenderGlobal.renderFilledBox(box, red, green, blue, 0.12F * pulse);
        RenderGlobal.drawSelectionBoundingBox(box, red, green, blue, pulse);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        if (!ModConfiguration.client.renderHighlightName) {
            return;
        }

        renderNameplate(
                target.getName(),
                target.getCenterX() + 0.5D,
                target.getCenterY() + 1.6D,
                target.getCenterZ() + 0.5D,
                viewerX,
                viewerY,
                viewerZ,
                red,
                green,
                blue);
    }

    /**
     * 渲染矿脉名称悬浮文字。
     */
    private void renderNameplate(
            String text,
            double worldX,
            double worldY,
            double worldZ,
            double viewerX,
            double viewerY,
            double viewerZ,
            float red,
            float green,
            float blue) {
        Minecraft minecraft = Minecraft.getMinecraft();
        FontRenderer fontRenderer = minecraft.fontRenderer;
        double x = worldX - viewerX;
        double y = worldY - viewerY;
        double z = worldZ - viewerZ;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(minecraft.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        int width = fontRenderer.getStringWidth(text) / 2;
        net.minecraft.client.gui.Gui.drawRect(-width - 3, -2, width + 3, 10, 0x66000000);
        fontRenderer.drawString(
                text,
                -width,
                0,
                ((int) (red * 255.0F) << 16) | ((int) (green * 255.0F) << 8) | (int) (blue * 255.0F));
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
