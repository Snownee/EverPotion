package snownee.skillslots.client.gui;

import java.util.Objects;

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
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.client.settings.KeyModifier;
import snownee.skillslots.SkillSlotsCommonConfig;
import snownee.skillslots.SkillSlotsHandler;
import snownee.skillslots.SkillSlotsModule;
import snownee.skillslots.client.SkillClientHandler;
import snownee.skillslots.client.SkillSlotsClient;
import snownee.skillslots.network.COpenContainerPacket;
import snownee.skillslots.skill.Skill;

public class UseScreen extends Screen {

	private static final Component TITLE = Component.translatable("gui.skillslots.use.title");

	private final float[] scales = new float[4];
	private final Component[] names = new Component[4];
	private SkillSlotsHandler handler;
	private boolean closing;
	private float openTick;
	private int clickIndex = -1;
	private int useIndex = -1;
	private float useTicks;
	private float useTicksTotal;
	private KeyMapping[] keyBindsHotbar;

	public UseScreen() {
		super(TITLE);
	}

	@Override
	protected void init() {
		Objects.requireNonNull(minecraft);
		Objects.requireNonNull(minecraft.player);
		handler = SkillSlotsHandler.of(minecraft.player);
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
		if (SkillSlotsCommonConfig.maxSlots == 3) {
			yCenter += 20;
		}

		if (!closing && useIndex != -1) {
			useTicks += pTicks;
			if (useTicks >= useTicksTotal) {
				useTicks = useTicksTotal;
				onClose();
			}
		}

		int oClickIndex = clickIndex;
		float offset = 35 + openTick * 25;
		if (SkillSlotsCommonConfig.maxSlots == 1) {
			drawButton(matrix, xCenter, yCenter, mouseX, mouseY, 0, pTicks);
		} else if (SkillSlotsCommonConfig.maxSlots == 2) {
			drawButton(matrix, xCenter - offset, yCenter, mouseX, mouseY, 0, pTicks);
			drawButton(matrix, xCenter + offset, yCenter, mouseX, mouseY, 1, pTicks);
		} else if (SkillSlotsCommonConfig.maxSlots == 3) {
			drawButton(matrix, xCenter - offset, yCenter, mouseX, mouseY, 0, pTicks);
			drawButton(matrix, xCenter, yCenter - offset, mouseX, mouseY, 1, pTicks);
			drawButton(matrix, xCenter + offset, yCenter, mouseX, mouseY, 2, pTicks);
		} else if (SkillSlotsCommonConfig.maxSlots == 4) {
			drawButton(matrix, xCenter - offset, yCenter, mouseX, mouseY, 0, pTicks);
			drawButton(matrix, xCenter, yCenter - offset, mouseX, mouseY, 1, pTicks);
			drawButton(matrix, xCenter + offset, yCenter, mouseX, mouseY, 2, pTicks);
			drawButton(matrix, xCenter, yCenter + offset, mouseX, mouseY, 3, pTicks);
		}
		if (clickIndex < 0) {
			int range = SkillSlotsCommonConfig.maxSlots == 1 ? 60 : 120;
			boolean out = Math.abs(mouseX - xCenter) + Math.abs(mouseY - yCenter) > range;
			clickIndex = out ? -2 : -1;
		} else {
			Skill skill = handler.skills.get(clickIndex);
			if (skill.isEmpty() && clickIndex < handler.getContainerSize()) {
				Component tooltip = Component.translatable("tip.skillslots.emptySlot", SkillSlotsClient.kbUse.getTranslatedKeyMessage());
				renderTooltip(matrix, tooltip, mouseX, mouseY);
			}
			if (!skill.isEmpty() && oClickIndex != clickIndex) {
				SkillSlotsClient.playSound(SkillSlotsModule.HOVER_SOUND.get());
			}
		}

		RenderSystem.disableBlend();

		super.render(matrix, mouseX, mouseY, pTicks);
	}

	@SuppressWarnings("null")
	private void drawButton(PoseStack matrix, float xCenter, float yCenter, int mouseX, int mouseY, int index, float pTicks) {
		Skill skill = handler.skills.get(index);
		float a = .5F * openTick;

		float hd = 40;
		boolean hover = !closing && openTick == 1 && index < handler.getContainerSize() && Math.abs(mouseX - xCenter) + Math.abs(mouseY - yCenter) < hd + 10;
		hover = hover && (useIndex == index || useIndex == -1);
		if (hover) {
			clickIndex = index;
			if (skill.isEmpty()) {
				hover = false;
			}
		} else if (clickIndex == index) {
			clickIndex = -1;
		}

		scales[index] += (hover ? pTicks : -pTicks) * 0.5f;
		scales[index] = Mth.clamp(scales[index], 0, 1);
		hd += scales[index] * 5;

		float r, g, b;
		if (hover) {
			int color = skill.color;
			float scale = scales[index] * .2f;
			r = Math.max(.1F, (color >> 16 & 255) / 255.0F * scale);
			g = Math.max(.1F, (color >> 8 & 255) / 255.0F * scale);
			b = Math.max(.1F, (color & 255) / 255.0F * scale);
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
		if (hover || handler.toggles.get(index) || useIndex == index) {
			int color = skill.color;
			float scale = scales[index];
			if (!hover) {
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
		BufferUploader.drawWithShader(buffer.end());

		if (useIndex == index) {
			float h = hd * 2 * useTicks / useTicksTotal - hd;
			float ia = openTick * .2f;
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
			BufferUploader.drawWithShader(buffer.end());
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
		BufferUploader.drawWithShader(buffer.end());
		RenderSystem.enableTexture();

		refreshName(index);
		matrix.pushPose();
		int textAlpha = (int) (openTick * 255);
		int textColor = textAlpha << 24 | 0xffffff;

		Component name;
		if (hover) {
			name = skill.getActionDescription();
		} else {
			name = names[index];
		}
		Objects.requireNonNull(minecraft);
		float progress = skill.getDisplayChargeProgress(minecraft.player, pTicks);
		if (progress != 1) {
			float percent = 100 * progress;
			name = Component.literal((int) percent + "%");
		}

		if (skill.isEmpty()) {
			matrix.translate(xCenter, yCenter - 3, 0);
			matrix.scale(0.75f, 0.75f, 0.75f);
			drawCenteredString(matrix, font, name, 0, 0, textColor);
		} else {
			SkillClientHandler<Skill> handler = SkillSlotsClient.getClientHandler(skill);
			if (handler != null) {
				handler.renderGUI(skill, matrix, xCenter, yCenter, scales[index], openTick, name, textColor);
			}
		}
		matrix.popPose();
	}

	private void refreshName(int i) {
		if (names[i] != null) {
			return;
		}
		Skill skill = handler.skills.get(i);
		if (skill.isEmpty()) {
			if (i < handler.getContainerSize()) {
				names[i] = Component.empty();
			} else {
				names[i] = Component.translatable("msg.skillslots.locked").withStyle(ChatFormatting.GRAY);
			}
		} else {
			names[i] = skill.getDisplayName();
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
		Skill skill = handler.skills.get(clickIndex);
		if (skill.isEmpty()) {
			COpenContainerPacket.I.sendToServer($ -> {
			});
			return true;
		}
		startUse(clickIndex);
		return true;
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (closing || useIndex != -1) {
			return false;
		}
		for (int i = 0; i < handler.getContainerSize(); i++) {
			if (keyBindsHotbar[i].getKey().getValue() == key) {
				startUse(i);
				return true;
			}
		}
		if (SkillSlotsClient.kbUse.getKeyModifier().isActive(null) && SkillSlotsClient.kbUse.getKey().getValue() == key) {
			if (SkillSlotsClient.kbUse.getKeyModifier() == KeyModifier.NONE && modifiers != 0) {
				return false;
			}
			onClose();
			return true;
		}
		return super.keyPressed(key, scanCode, modifiers);
	}

	private void startUse(int i) {
		if (!handler.canUseSlot(i)) {
			return;
		}
		clickIndex = i;
		scales[i] = 1;
		handler.startUsing(clickIndex);
		if (handler.useIndex == -1) {
			onClose();
		}
		useTicksTotal = handler.skills.get(i).getUseDuration();
		useIndex = i;
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
