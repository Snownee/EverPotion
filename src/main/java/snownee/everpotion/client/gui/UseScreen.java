package snownee.everpotion.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.client.settings.KeyModifier;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.PotionType;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.client.ClientHandler;
import snownee.everpotion.handler.EverHandler;
import snownee.everpotion.handler.EverHandler.Cache;

public class UseScreen extends Screen {

	private static final Component TITLE = new TranslatableComponent("gui.everpotion.use.title");
	private EverHandler handler;
	private final float[] scales = new float[4];
	private final String[] names = new String[4];
	private boolean closing;
	private float openTick;
	private int clickIndex = -1;
	private float drinkTick;

	private KeyMapping[] keyBindsHotbar;

	public UseScreen() {
		super(TITLE);
	}

	@Override
	protected void init() {
		if (minecraft.player == null) {
			return;
		}
		handler = minecraft.player.getCapability(EverCapabilities.HANDLER).orElse(null);
		keyBindsHotbar = minecraft.options.keyHotbarSlots;
	}

	@Override
	public void render(PoseStack matrix, int mouseX, int mouseY, float pTicks) {
		if (handler == null) {
			return;
		}

		openTick += closing ? -pTicks * .4f : pTicks * .2f;
		if (closing && openTick <= 0) {
			Minecraft.getInstance().setScreen(null);
			return;
		}
		openTick = Mth.clamp(openTick, 0, 1);

		float xCenter = width / 2f;
		float yCenter = height / 2f;
		if (EverCommonConfig.maxSlots == 3) {
			yCenter += 20;
		}

		if (!closing && (drinkTick > 0 || handler.drinkIndex != -1)) {
			drinkTick += pTicks;
			if (drinkTick > EverCommonConfig.drinkDelay) {
				onClose();
			}
		}

		float offset = 35 + openTick * 25;
		if (EverCommonConfig.maxSlots == 1) {
			drawButton(matrix, xCenter, yCenter, mouseX, mouseY, 0, pTicks);
		} else if (EverCommonConfig.maxSlots == 2) {
			drawButton(matrix, xCenter - offset, yCenter, mouseX, mouseY, 0, pTicks);
			drawButton(matrix, xCenter + offset, yCenter, mouseX, mouseY, 1, pTicks);
		} else if (EverCommonConfig.maxSlots == 3) {
			drawButton(matrix, xCenter - offset, yCenter, mouseX, mouseY, 0, pTicks);
			drawButton(matrix, xCenter, yCenter - offset, mouseX, mouseY, 1, pTicks);
			drawButton(matrix, xCenter + offset, yCenter, mouseX, mouseY, 2, pTicks);
		} else if (EverCommonConfig.maxSlots == 4) {
			drawButton(matrix, xCenter - offset, yCenter, mouseX, mouseY, 0, pTicks);
			drawButton(matrix, xCenter, yCenter - offset, mouseX, mouseY, 1, pTicks);
			drawButton(matrix, xCenter + offset, yCenter, mouseX, mouseY, 2, pTicks);
			drawButton(matrix, xCenter, yCenter + offset, mouseX, mouseY, 3, pTicks);
		}
		if (clickIndex < 0) {
			int range = EverCommonConfig.maxSlots == 1 ? 60 : 120;
			boolean out = Math.abs(mouseX - xCenter) + Math.abs(mouseY - yCenter) > range;
			clickIndex = out ? -2 : -1;
		}

		RenderSystem.disableBlend();

		super.render(matrix, mouseX, mouseY, pTicks);
	}

	@SuppressWarnings("null")
	private void drawButton(PoseStack matrix, float xCenter, float yCenter, int mouseX, int mouseY, int index, float pTicks) {
		Cache cache = handler.caches[index];
		float a = .5F * openTick;

		float hd = 40;
		boolean hover = !closing && cache != null && openTick == 1 && Math.abs(mouseX - xCenter) + Math.abs(mouseY - yCenter) < hd + 10;
		hover = hover && (handler.drinkIndex == index || handler.drinkIndex == -1);
		if (hover) {
			clickIndex = index;
		} else if (clickIndex == index) {
			clickIndex = -1;
		}

		scales[index] += (hover ? pTicks : -pTicks) * 0.5f;
		scales[index] = Mth.clamp(scales[index], 0, 1);
		hd += scales[index] * 5;

		float r, g, b;
		if (hover) {
			int color = cache.color;
			r = Math.max(.1F, (color >> 16 & 255) / 255.0F * .2f * scales[index]);
			g = Math.max(.1F, (color >> 8 & 255) / 255.0F * .2f * scales[index]);
			b = Math.max(.1F, (color & 255) / 255.0F * .2f * scales[index]);
		} else {
			r = .1F;
			g = .1F;
			b = .1F;
		}
		Matrix4f matrix4f = matrix.last().pose();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(matrix4f, xCenter - hd, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter, yCenter + hd, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter + hd, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter, yCenter - hd, 0.0F).color(r, g, b, a).endVertex();

		a = .75f * openTick;
		if (hover || handler.tipIndex == index) {
			int color = cache.color;
			float scale = scales[index];
			if (handler.tipIndex == index) {
				scale = Math.max(scale, .75f);
			}
			r = Math.max(.1F, (color >> 16 & 255) / 255.0F * scale);
			g = Math.max(.1F, (color >> 8 & 255) / 255.0F * scale);
			b = Math.max(.1F, (color & 255) / 255.0F * scale);
		}

		float hdborder = hd + 3;

		buffer.vertex(matrix4f, xCenter, yCenter - hd, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter + hd, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter + hdborder, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter, yCenter - hdborder, 0.0F).color(r, g, b, a).endVertex();

		buffer.vertex(matrix4f, xCenter, yCenter + hdborder, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter + hdborder, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter + hd, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter, yCenter + hd, 0.0F).color(r, g, b, a).endVertex();

		buffer.vertex(matrix4f, xCenter, yCenter + hd, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter - hd, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter - hdborder, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter, yCenter + hdborder, 0.0F).color(r, g, b, a).endVertex();

		buffer.vertex(matrix4f, xCenter, yCenter - hdborder, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter - hdborder, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter - hd, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter, yCenter - hd, 0.0F).color(r, g, b, a).endVertex();
		buffer.end();
		BufferUploader.end(buffer);

		if (handler.drinkIndex == index) {
			float h = hd * 2 * drinkTick / EverCommonConfig.drinkDelay - hd;
			float ia = .2f;
			buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
			buffer.vertex(matrix4f, xCenter - hd + Math.abs(h), yCenter - h, 0.0F).color(1, 1, 1, ia).endVertex();
			if (h > 0) {
				buffer.vertex(matrix4f, xCenter - hd, yCenter, 0.0F).color(1, 1, 1, ia).endVertex();
			}
			buffer.vertex(matrix4f, xCenter, yCenter + hd, 0.0F).color(1, 1, 1, ia).endVertex();
			if (h > 0) {
				buffer.vertex(matrix4f, xCenter + hd, yCenter, 0.0F).color(1, 1, 1, ia).endVertex();
			}
			buffer.vertex(matrix4f, xCenter + hd - Math.abs(h), yCenter - h, 0.0F).color(1, 1, 1, ia).endVertex();
			buffer.end();
			BufferUploader.end(buffer);
		}

		float hdshadow;
		if (hover) {
			a = .3f * openTick;
			hdshadow = hdborder + 6;
		} else {
			a = .2f * openTick;
			hdshadow = hdborder + 5;
		}
		r = .1F;
		g = .1F;
		b = .1F;

		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(matrix4f, xCenter, yCenter - hdborder, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter + hdborder, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter + hdshadow, yCenter, 0.0F).color(r, g, b, 0).endVertex();
		buffer.vertex(matrix4f, xCenter, yCenter - hdshadow, 0.0F).color(r, g, b, 0).endVertex();

		buffer.vertex(matrix4f, xCenter, yCenter + hdshadow, 0.0F).color(r, g, b, 0).endVertex();
		buffer.vertex(matrix4f, xCenter + hdshadow, yCenter, 0.0F).color(r, g, b, 0).endVertex();
		buffer.vertex(matrix4f, xCenter + hdborder, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter, yCenter + hdborder, 0.0F).color(r, g, b, a).endVertex();

		buffer.vertex(matrix4f, xCenter, yCenter + hdborder, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter - hdborder, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter - hdshadow, yCenter, 0.0F).color(r, g, b, 0).endVertex();
		buffer.vertex(matrix4f, xCenter, yCenter + hdshadow, 0.0F).color(r, g, b, 0).endVertex();

		buffer.vertex(matrix4f, xCenter, yCenter - hdshadow, 0.0F).color(r, g, b, 0).endVertex();
		buffer.vertex(matrix4f, xCenter - hdshadow, yCenter, 0.0F).color(r, g, b, 0).endVertex();
		buffer.vertex(matrix4f, xCenter - hdborder, yCenter, 0.0F).color(r, g, b, a).endVertex();
		buffer.vertex(matrix4f, xCenter, yCenter - hdborder, 0.0F).color(r, g, b, a).endVertex();
		buffer.end();
		BufferUploader.end(buffer);
		RenderSystem.enableTexture();

		refreshName(index);
		matrix.pushPose();
		RenderSystem.setShaderColor(1, 1, 1, openTick);
		int textAlpha = (int) (openTick * 255);
		int textColor = textAlpha << 24 | 0xffffff;

		String name;
		if (hover && cache.type != PotionType.NORMAL) {
			name = I18n.get(cache.type.getDescKey());
		} else {
			name = names[index];
		}
		if (cache != null && cache.progress < EverCommonConfig.refillTime) {
			float percent = 100 * cache.progress / EverCommonConfig.refillTime;
			name = (int) percent + "%";
		}

		if (cache == null) {
			matrix.translate(xCenter, yCenter - 3, 0);
			matrix.scale(0.75f, 0.75f, 0.75f);
			drawCenteredString(matrix, font, name, 0, 0, textColor);
		} else if (cache.effect != null) {
			MobEffectTextureManager potionspriteuploader = this.minecraft.getMobEffectTextures();
			TextureAtlasSprite sprite = potionspriteuploader.get(cache.effect.getEffect());
			RenderSystem.setShaderTexture(0, sprite.atlas().location());

			float yCenter2 = yCenter - 6 * (1 + 0.125f * scales[index]);
			float halfwidth = 12 + 1.5f * scales[index];
			float left = xCenter - halfwidth;
			float right = xCenter + halfwidth;
			float top = yCenter2 - halfwidth;
			float bottom = yCenter2 + halfwidth;

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			buffer.vertex(matrix4f, left, bottom, 0).uv(sprite.getU0(), sprite.getV1()).endVertex();
			buffer.vertex(matrix4f, right, bottom, 0).uv(sprite.getU1(), sprite.getV1()).endVertex();
			buffer.vertex(matrix4f, right, top, 0).uv(sprite.getU1(), sprite.getV0()).endVertex();
			buffer.vertex(matrix4f, left, top, 0).uv(sprite.getU0(), sprite.getV0()).endVertex();
			buffer.end();
			BufferUploader.end(buffer);

			matrix.translate(xCenter, yCenter + 10, 0);
			matrix.scale(0.75f, 0.75f, 0.75f);
			drawCenteredString(matrix, font, name, 0, 0, textColor);
		} else {
			matrix.translate(xCenter, yCenter - 3, 0);
			matrix.scale(0.75f, 0.75f, 0.75f);
			drawCenteredString(matrix, font, name, 0, 0, textColor);
		}
		matrix.popPose();
	}

	private void refreshName(int i) {
		if (names[i] != null) {
			return;
		}
		Cache cache = handler.caches[i];
		if (cache == null) {
			if (i < handler.getSlots()) {
				names[i] = "";
			} else {
				names[i] = ChatFormatting.GRAY + I18n.get("msg.everpotion.locked");
			}
		} else if (cache.effect == null) {
			names[i] = I18n.get("effect.none");
		} else {
			names[i] = I18n.get(cache.effect.getDescriptionId());
			if (cache.effect.getAmplifier() > 0) {
				names[i] += " " + I18n.get("potion.potency." + cache.effect.getAmplifier());
			}
		}
	}

	@Override
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if (clickIndex == -2) { // click outside of main area
			onClose();
			return true;
		}
		if (closing || clickIndex == -1) {
			return false;
		}
		if (!handler.canUseSlot(clickIndex, true)) {
			return false;
		}
		scales[clickIndex] = 0.25f;
		handler.startDrinking(clickIndex);
		if (handler.drinkIndex == -1) {
			onClose();
		}
		return true;
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (closing || handler.drinkIndex != -1) {
			return false;
		}
		for (int i = 0; i < handler.getSlots(); i++) {
			if (handler.canUseSlot(i, true) && keyBindsHotbar[i].getKey().getValue() == key) {
				clickIndex = i;
				scales[i] = 1;
				handler.startDrinking(clickIndex);
				if (handler.drinkIndex == -1) {
					onClose();
				}
				return true;
			}
		}
		if (ClientHandler.kbUse.getKeyModifier().isActive(null) && ClientHandler.kbUse.getKey().getValue() == key) {
			if (ClientHandler.kbUse.getKeyModifier() == KeyModifier.NONE && modifiers != 0) {
				return false;
			}
			onClose();
			return true;
		}
		return super.keyPressed(key, scanCode, modifiers);
	}

	@Override
	public void onClose() {
		closing = true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

}
