package snownee.everpotion.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.menu.PlaceMenu;

// from HopperScreen
public class PlaceScreen extends AbstractContainerScreen<PlaceMenu> {

	private static final ResourceLocation HOPPER_LOCATION = new ResourceLocation("textures/gui/container/hopper.png");

	public PlaceScreen(PlaceMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.passEvents = false;
		this.imageHeight = 133;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void render(PoseStack matrix, int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground(matrix);
		super.render(matrix, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(matrix, p_render_1_, p_render_2_);
	}

	/**
     * Draws the background layer of this container (behind the items).
     */
	@Override
	protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, HOPPER_LOCATION);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(matrix, i, j, 0, 0, this.imageWidth, this.imageHeight);
		int slots = menu.getSlots();
		int xOffset = 2 - EverCommonConfig.maxSlots / 2;
		int xStart = leftPos + 43 + xOffset * 18;
		if (xOffset > 0) {
			fill(matrix, leftPos + 43, topPos + 19, xStart, topPos + 39, 0xffc6c6c6);
		}
		xStart += slots * 18;
		fill(matrix, xStart, topPos + 19, leftPos + 133, topPos + 39, 0xffc6c6c6);
	}

}
