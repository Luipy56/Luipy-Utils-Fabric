package com.luipy.utilsmod.client.config.ui;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Metadata for one boolean toggle row bound to a {@link LuipyUtilsConfig} field.
 */
public record LuipyConfigBooleanEntry(
	LuipyConfigCategory category,
	String labelKey,
	String descriptionKey,
	Function<LuipyUtilsConfig, Boolean> getter,
	BiConsumer<LuipyUtilsConfig, Boolean> setter,
	boolean defaultValue,
	@Nullable Item iconItem
) {
	public LuipyConfigBooleanEntry(
		LuipyConfigCategory category,
		String labelKey,
		String descriptionKey,
		Function<LuipyUtilsConfig, Boolean> getter,
		BiConsumer<LuipyUtilsConfig, Boolean> setter,
		boolean defaultValue
	) {
		this(category, labelKey, descriptionKey, getter, setter, defaultValue, null);
	}
	public Component label() {
		return Component.translatable(this.labelKey);
	}

	public Component description() {
		return Component.translatable(this.descriptionKey);
	}
}
