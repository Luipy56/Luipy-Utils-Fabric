package com.luipy.utilsmod.client.config.ui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * One row in the in-game config screen (boolean toggle or multi-option cycle).
 */
public sealed interface LuipyConfigRowEntry permits LuipyConfigBooleanEntry, LuipyConfigCycleEntry {
	LuipyConfigCategory category();

	Component label();

	Component description();

	@Nullable Item iconItem();
}
