package com.luipy.utilsmod.client.config;

import com.luipy.utilsmod.config.EnderChestAccessMode;
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

		general.addEntry(eb.startEnumSelector(
			Component.translatable("luipy-utils-mod.config.ender_chest.access"),
			EnderChestAccessMode.class,
			cfg.enderChestAccess
		)
			.setDefaultValue(EnderChestAccessMode.BOTH)
			.setSaveConsumer(v -> cfg.enderChestAccess = v)
			.build());

		general.addEntry(eb.startBooleanToggle(Component.translatable("luipy-utils-mod.config.show_ender_gate_hud"), cfg.showEnderGateHudIndicator)
			.setDefaultValue(true)
			.setSaveConsumer(v -> cfg.showEnderGateHudIndicator = v)
			.build());

		general.addEntry(eb.startBooleanToggle(Component.translatable("luipy-utils-mod.config.show_toasts"), cfg.showToastsOnFailure)
			.setDefaultValue(true)
			.setSaveConsumer(v -> cfg.showToastsOnFailure = v)
			.build());

		general.addEntry(eb.startBooleanToggle(Component.translatable("luipy-utils-mod.config.enchantment_preview"), cfg.showEnchantmentPreview)
			.setDefaultValue(true)
			.setSaveConsumer(v -> cfg.showEnchantmentPreview = v)
			.build());

		general.addEntry(eb.startBooleanToggle(Component.translatable("luipy-utils-mod.config.open_shulker_from_inventory"), cfg.allowOpenShulkerFromInventory)
			.setDefaultValue(true)
			.setSaveConsumer(v -> cfg.allowOpenShulkerFromInventory = v)
			.build());

		general.addEntry(eb.startBooleanToggle(Component.translatable("luipy-utils-mod.config.show_crafting_table_with_inventory"), cfg.showCraftingTableWithInventory)
			.setDefaultValue(false)
			.setSaveConsumer(v -> cfg.showCraftingTableWithInventory = v)
			.build());

		builder.setSavingRunnable(LuipyUtilsConfigManager::save);
		return builder.build();
	}
}
