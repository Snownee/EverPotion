package snownee.everpotion.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.container.PlaceContainer;

// from HopperScreen
public class PlaceScreen extends ContainerScreen<PlaceContainer> {

    private static final ResourceLocation HOPPER_GUI_TEXTURE = new ResourceLocation("textures/gui/container/hopper.png");

    public PlaceScreen(PlaceContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this./*passEvents*/field_230711_n_ = false;
        this.ySize = 133;
        this.field_238745_s_ = this.ySize - 94;
    }

    @Override
    public void /*render*/ func_230430_a_(MatrixStack matrix, int p_render_1_, int p_render_2_, float p_render_3_) {
        this./*renderBackground*/func_230446_a_(matrix);
        super./*render*/func_230430_a_(matrix, p_render_1_, p_render_2_, p_render_3_);
        this./*renderHoveredToolTip*/func_230459_a_(matrix, p_render_1_, p_render_2_);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void /*drawGuiContainerBackgroundLayer*/ func_230450_a_(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this./*minecraft*/field_230706_i_.getTextureManager().bindTexture(HOPPER_GUI_TEXTURE);
        int i = (this./*width*/field_230708_k_ - this.xSize) / 2;
        int j = (this./*height*/field_230709_l_ - this.ySize) / 2;
        this./*blit*/func_238474_b_(matrix, i, j, 0, 0, this.xSize, this.ySize);
        int slots = container.getSlots();
        int xOffset = 2 - EverCommonConfig.maxSlots / 2;
        int xStart = guiLeft + 43 + xOffset * 18;
        if (xOffset > 0) {
            /*fill*/func_238467_a_(matrix, guiLeft + 43, guiTop + 19, xStart, guiTop + 39, 0xffc6c6c6);
        }
        xStart += slots * 18;
        /*fill*/func_238467_a_(matrix, xStart, guiTop + 19, guiLeft + 133, guiTop + 39, 0xffc6c6c6);
    }

}
