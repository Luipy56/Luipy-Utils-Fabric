package com.luipy.utilsmod.client.config.ui;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Metadata for one multi-option cycle row bound to a {@link LuipyUtilsConfig} field.
 */
public record LuipyConfigCycleEntry<T>(
	LuipyConfigCategory category,
	String labelKey,
	String descriptionKey,
	Function<LuipyUtilsConfig, T> getter,
	BiConsumer<LuipyUtilsConfig, T> setter,
	T defaultValue,
	List<T> values,
	Function<T, String> valueLabelKey,
	Function<T, Integer> valueColor,
	@Nullable Item iconItem
) implements LuipyConfigRowEntry {
	public Component label() {
		return Component.translatable(this.labelKey);
	}

	public Component description() {
		return Component.translatable(this.descriptionKey);
	}

	public Component valueLabel(T value) {
		return Component.translatable(this.valueLabelKey.apply(value));
	}
}
