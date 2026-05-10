package com.luipy.utilsmod.client.config;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class LuipyConfigScreens {
	private LuipyConfigScreens() {
	}

	public static Screen create(Screen parent) {
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Component.translatable("luipy-utils-mod.config.title"));
		ConfigCategory general = builder.getOrCreateCategory(Component.translatable("luipy-utils-mod.config.category.general"));
		ConfigEntryBuilder eb = builder.entryBuilder();

		general.addEntry(eb.startBooleanToggle(Component.translatable("luipy-utils-mod.config.master_enabled"), cfg.masterEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(v -> cfg.masterEnabled = v)
			.build());

		general.addEntry(eb.startBooleanToggle(Component.translatable("luipy-utils-mod.config.show_ender_with_inventory"), cfg.showEnderChestWithInventory)
			.setDefaultValue(true)
			.setSaveConsumer(v -> cfg.showEnderChestWithInventory = v)
			.build());

		general.addEntry(eb.startBooleanToggle(Component.translatable("luipy-utils-mod.config.always_virtual"), cfg.alwaysAllowVirtualOpen)
			.setDefaultValue(false)
			.setSaveConsumer(v -> cfg.alwaysAllowVirtualOpen = v)
			.build());

		general.addEntry(eb.startBooleanToggle(Component.translatable("luipy-utils-mod.config.require_item"), cfg.requireEnderChestItem)
			.setDefaultValue(true)
			.setSaveConsumer(v -> cfg.requireEnderChestItem = v)
			.build());

		general.addEntry(eb.startBooleanToggle(Component.translatable("luipy-utils-mod.config.require_block"), cfg.requireNearbyEnderChestBlock)
			.setDefaultValue(true)
			.setSaveConsumer(v -> cfg.requireNearbyEnderChestBlock = v)
			.build());

		general.addEntry(eb.startBooleanToggle(Component.translatable("luipy-utils-mod.config.show_toasts"), cfg.showToastsOnFailure)
			.setDefaultValue(true)
			.setSaveConsumer(v -> cfg.showToastsOnFailure = v)
			.build());

		builder.setSavingRunnable(LuipyUtilsConfigManager::save);
		return builder.build();
	}
}
