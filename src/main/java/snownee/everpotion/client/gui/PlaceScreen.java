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
		passEvents = false;
		ySize = 133;
		playerInventoryTitleY = ySize - 94;
	}

	@Override
	public void render(MatrixStack matrix, int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground(matrix);
		super.render(matrix, p_render_1_, p_render_2_, p_render_3_);
		/*renderHoveredToolTip*/func_230459_a_(matrix, p_render_1_, p_render_2_);
	}

	/**
     * Draws the background layer of this container (behind the items).
     */
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(HOPPER_GUI_TEXTURE);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		this.blit(matrix, i, j, 0, 0, xSize, ySize);
		int slots = container.getSlots();
		int xOffset = 2 - EverCommonConfig.maxSlots / 2;
		int xStart = guiLeft + 43 + xOffset * 18;
		if (xOffset > 0) {
			fill(matrix, guiLeft + 43, guiTop + 19, xStart, guiTop + 39, 0xffc6c6c6);
		}
		xStart += slots * 18;
		fill(matrix, xStart, guiTop + 19, guiLeft + 133, guiTop + 39, 0xffc6c6c6);
	}

}
