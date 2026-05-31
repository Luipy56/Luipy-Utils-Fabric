package com.luipy.utilsmod.client.config.ui;

import net.minecraft.network.chat.Component;

/**
 * Sidebar categories for the first-party config screen.
 * Add a constant here and register entries in {@link LuipyConfigCategories}.
 */
public enum LuipyConfigCategory {
	GENERAL("luipy-utils-mod.config.category.general"),
	INVENTORY("luipy-utils-mod.config.category.inventory"),
	FEATURES("luipy-utils-mod.config.category.features"),
	WORLD("luipy-utils-mod.config.category.world"),
	KEYBINDS("luipy-utils-mod.config.category.keybinds");

	private final String translationKey;

	LuipyConfigCategory(String translationKey) {
		this.translationKey = translationKey;
	}

	public Component title() {
		return Component.translatable(this.translationKey);
	}
}
