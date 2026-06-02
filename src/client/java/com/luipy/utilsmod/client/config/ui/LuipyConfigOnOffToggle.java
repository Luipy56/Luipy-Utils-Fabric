package com.luipy.utilsmod.client.config.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * On/Off config toggle with vanilla button styling and green (on) / red (off) label text.
 */
public final class LuipyConfigOnOffToggle extends AbstractButton {
	private static final int COLOR_ON_TEXT = 0xFF88FF88;
	private static final int COLOR_OFF_TEXT = 0xFFFF8888;
	private static final int COLOR_DISABLED_TEXT = 0xFFA0A0A0;

	private boolean value;
	private final Component optionLabel;
	private final OnValueChange onValueChange;

	@FunctionalInterface
	public interface OnValueChange {
		void onValueChange(LuipyConfigOnOffToggle button, boolean value);
	}

	private LuipyConfigOnOffToggle(
		int x,
		int y,
		int width,
		int height,
		boolean initialValue,
		Component optionLabel,
		OnValueChange onValueChange
	) {
		super(x, y, width, height, valueText(initialValue));
		this.value = initialValue;
		this.optionLabel = optionLabel;
		this.onValueChange = onValueChange;
	}

	public static LuipyConfigOnOffToggle create(
		int x,
		int y,
		int width,
		int height,
		boolean initialValue,
		Component optionLabel,
		OnValueChange onValueChange
	) {
		return new LuipyConfigOnOffToggle(x, y, width, height, initialValue, optionLabel, onValueChange);
	}

	public boolean getValue() {
		return this.value;
	}

	public void setValue(boolean value) {
		this.value = value;
		this.setMessage(valueText(value));
	}

	private static Component valueText(boolean on) {
		return on ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
	}

	@Override
	public void onPress() {
		this.setValue(!this.value);
		this.onValueChange.onValueChange(this, this.value);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		graphics.blitNineSliced(
			WIDGETS_LOCATION,
			this.getX(),
			this.getY(),
			this.getWidth(),
			this.getHeight(),
			20,
			4,
			200,
			20,
			0,
			this.buttonTextureVOffset()
		);
		graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

		int textColor = this.active
			? (this.value ? COLOR_ON_TEXT : COLOR_OFF_TEXT)
			: COLOR_DISABLED_TEXT;
		this.renderString(graphics, minecraft.font, textColor | Mth.ceil(this.alpha * 255.0F) << 24);
	}

	private int buttonTextureVOffset() {
		if (!this.active) {
			return 0;
		}
		return this.isHoveredOrFocused() ? 2 : 1;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput output) {
		output.add(
			NarratedElementType.TITLE,
			Component.translatable("narration.cycle_button.usage", this.optionLabel, this.getMessage())
		);
	}
}
