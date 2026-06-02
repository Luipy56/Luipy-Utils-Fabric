package com.luipy.utilsmod.client.config.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * On/Off config toggle with green (on) and red (off) styling for {@link LuipyConfigScreen}.
 */
public final class LuipyConfigOnOffToggle extends AbstractButton {
	private static final int COLOR_ON_BG = 0xFF2E6B2E;
	private static final int COLOR_ON_TEXT = 0xFF88FF88;
	private static final int COLOR_OFF_BG = 0xFF6B2E2E;
	private static final int COLOR_OFF_TEXT = 0xFFFF8888;
	private static final int COLOR_BORDER = 0xFF606060;
	private static final int COLOR_BORDER_HOVER = 0xFFAAAAAA;

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
		int bg = this.value ? COLOR_ON_BG : COLOR_OFF_BG;
		int textColor = this.value ? COLOR_ON_TEXT : COLOR_OFF_TEXT;
		int border = this.isHoveredOrFocused() ? COLOR_BORDER_HOVER : COLOR_BORDER;

		graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
		graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, border);
		graphics.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, border);
		graphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, border);
		graphics.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, border);

		graphics.drawCenteredString(
			Minecraft.getInstance().font,
			this.getMessage(),
			this.getX() + this.width / 2,
			this.getY() + (this.height - 8) / 2,
			textColor
		);
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput output) {
		output.add(
			NarratedElementType.TITLE,
			Component.translatable("narration.cycle_button.usage", this.optionLabel, this.getMessage())
		);
	}
}
