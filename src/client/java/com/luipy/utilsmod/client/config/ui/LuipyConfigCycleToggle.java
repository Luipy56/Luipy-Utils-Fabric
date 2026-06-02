package com.luipy.utilsmod.client.config.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Multi-option config toggle (cycles on click) with vanilla button styling.
 */
public final class LuipyConfigCycleToggle<T> extends AbstractButton {
	private static final int TEXTURE_Y_OFFSET = 46;
	private static final int TEXTURE_HEIGHT = 20;
	private static final int COLOR_OFF_TEXT = 0xFFFF8888;
	private static final int COLOR_ON_TEXT = 0xFF88FF88;
	private static final int COLOR_NEUTRAL_TEXT = 0xFFCCCCCC;
	private static final int COLOR_DISABLED_TEXT = 0xFFA0A0A0;

	private T value;
	private final List<T> values;
	private final Function<T, Component> valueLabel;
	private final Function<T, Integer> valueColor;
	private final Component optionLabel;
	private final OnValueChange<T> onValueChange;

	@FunctionalInterface
	public interface OnValueChange<T> {
		void onValueChange(LuipyConfigCycleToggle<T> button, T value);
	}

	private LuipyConfigCycleToggle(
		int x,
		int y,
		int width,
		int height,
		T initialValue,
		List<T> values,
		Function<T, Component> valueLabel,
		Function<T, Integer> valueColor,
		Component optionLabel,
		OnValueChange<T> onValueChange
	) {
		super(x, y, width, height, valueLabel.apply(initialValue));
		this.value = initialValue;
		this.values = values;
		this.valueLabel = valueLabel;
		this.valueColor = valueColor;
		this.optionLabel = optionLabel;
		this.onValueChange = onValueChange;
	}

	public static <T> LuipyConfigCycleToggle<T> create(
		int x,
		int y,
		int width,
		int height,
		T initialValue,
		List<T> values,
		Function<T, Component> valueLabel,
		Function<T, Integer> valueColor,
		Component optionLabel,
		OnValueChange<T> onValueChange
	) {
		return new LuipyConfigCycleToggle<>(
			x,
			y,
			width,
			height,
			initialValue,
			values,
			valueLabel,
			valueColor,
			optionLabel,
			onValueChange
		);
	}

	public T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		this.value = value;
		this.setMessage(this.valueLabel.apply(value));
	}

	@Override
	public void onPress() {
		int index = this.values.indexOf(this.value);
		int nextIndex = index < 0 ? 0 : (index + 1) % this.values.size();
		this.setValue(this.values.get(nextIndex));
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
			this.getTextureY()
		);
		graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

		int textColor = this.active
			? this.valueColor.apply(this.value)
			: COLOR_DISABLED_TEXT;
		this.renderString(graphics, minecraft.font, textColor | Mth.ceil(this.alpha * 255.0F) << 24);
	}

	private int getTextureY() {
		int variant = 1;
		if (!this.active) {
			variant = 0;
		} else if (this.isHoveredOrFocused()) {
			variant = 2;
		}
		return TEXTURE_Y_OFFSET + variant * TEXTURE_HEIGHT;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput output) {
		output.add(
			NarratedElementType.TITLE,
			Component.translatable("narration.cycle_button.usage", this.optionLabel, this.getMessage())
		);
	}

	public static <T> Function<T, Integer> offNeutralAlwaysColors(T offValue, T alwaysValue) {
		return value -> {
			if (value.equals(offValue)) {
				return COLOR_OFF_TEXT;
			}
			if (value.equals(alwaysValue)) {
				return COLOR_ON_TEXT;
			}
			return COLOR_NEUTRAL_TEXT;
		};
	}

	public static <T> Function<T, Integer> enderAccessColors(T offValue, T alwaysValue) {
		return offNeutralAlwaysColors(offValue, alwaysValue);
	}
}
