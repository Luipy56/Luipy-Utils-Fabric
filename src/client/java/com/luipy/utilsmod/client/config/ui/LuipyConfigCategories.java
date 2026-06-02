package com.luipy.utilsmod.client.config.ui;

import com.luipy.utilsmod.client.highlight.HighlightEmphasisTextures;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.inventory.workstation.WorkstationKind;
import java.util.ArrayList;
import net.minecraft.world.item.Items;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry mapping {@link LuipyUtilsConfig} fields to UI metadata.
 * To add a new option: config field + lang keys + one {@link LuipyConfigBooleanEntry} below.
 */
public final class LuipyConfigCategories {
	private static final List<LuipyConfigBooleanEntry> ENTRIES = buildEntries();
	private static final Map<LuipyConfigCategory, List<LuipyConfigBooleanEntry>> BY_CATEGORY = indexByCategory();

	private LuipyConfigCategories() {
	}

	private static List<LuipyConfigBooleanEntry> buildEntries() {
		List<LuipyConfigBooleanEntry> list = new ArrayList<>();

		list.add(new LuipyConfigBooleanEntry(
			LuipyConfigCategory.GENERAL,
			"luipy-utils-mod.config.master_enabled",
			"luipy-utils-mod.config.master_enabled.desc",
			cfg -> cfg.masterEnabled,
			(cfg, v) -> cfg.masterEnabled = v,
			true
		));
		list.add(new LuipyConfigBooleanEntry(
			LuipyConfigCategory.GENERAL,
			"luipy-utils-mod.config.show_toasts",
			"luipy-utils-mod.config.show_toasts.desc",
			cfg -> cfg.showToastsOnFailure,
			(cfg, v) -> cfg.showToastsOnFailure = v,
			true
		));

		list.add(new LuipyConfigBooleanEntry(
			LuipyConfigCategory.INVENTORY,
			"luipy-utils-mod.config.show_ender_with_inventory",
			"luipy-utils-mod.config.show_ender_with_inventory.desc",
			cfg -> cfg.showEnderChestWithInventory,
			(cfg, v) -> cfg.showEnderChestWithInventory = v,
			true,
			Items.ENDER_CHEST
		));
		list.add(new LuipyConfigBooleanEntry(
			LuipyConfigCategory.INVENTORY,
			"luipy-utils-mod.config.show_ender_gate_hud",
			"luipy-utils-mod.config.show_ender_gate_hud.desc",
			cfg -> cfg.showEnderGateHudIndicator,
			(cfg, v) -> cfg.showEnderGateHudIndicator = v,
			true,
			Items.ENDER_CHEST
		));
		list.add(new LuipyConfigBooleanEntry(
			LuipyConfigCategory.INVENTORY,
			"luipy-utils-mod.config.show_crafting_table_with_inventory",
			"luipy-utils-mod.config.show_crafting_table_with_inventory.desc",
			cfg -> cfg.showCraftingTableWithInventory,
			(cfg, v) -> cfg.showCraftingTableWithInventory = v,
			false
		));
		list.add(new LuipyConfigBooleanEntry(
			LuipyConfigCategory.INVENTORY,
			"luipy-utils-mod.config.always_virtual",
			"luipy-utils-mod.config.always_virtual.desc",
			cfg -> cfg.alwaysAllowVirtualOpen,
			(cfg, v) -> cfg.alwaysAllowVirtualOpen = v,
			false,
			Items.ENDER_CHEST
		));
		list.add(new LuipyConfigBooleanEntry(
			LuipyConfigCategory.INVENTORY,
			"luipy-utils-mod.config.require_item",
			"luipy-utils-mod.config.require_item.desc",
			cfg -> cfg.requireEnderChestItem,
			(cfg, v) -> cfg.requireEnderChestItem = v,
			true,
			Items.ENDER_CHEST
		));
		list.add(new LuipyConfigBooleanEntry(
			LuipyConfigCategory.INVENTORY,
			"luipy-utils-mod.config.require_block",
			"luipy-utils-mod.config.require_block.desc",
			cfg -> cfg.requireNearbyEnderChestBlock,
			(cfg, v) -> cfg.requireNearbyEnderChestBlock = v,
			true,
			Items.ENDER_CHEST
		));

		addWorkstationEntries(list);

		list.add(new LuipyConfigBooleanEntry(
			LuipyConfigCategory.FEATURES,
			"luipy-utils-mod.config.enchantment_preview",
			"luipy-utils-mod.config.enchantment_preview.desc",
			cfg -> cfg.showEnchantmentPreview,
			(cfg, v) -> cfg.showEnchantmentPreview = v,
			true
		));
		list.add(new LuipyConfigBooleanEntry(
			LuipyConfigCategory.FEATURES,
			"luipy-utils-mod.config.open_shulker_from_inventory",
			"luipy-utils-mod.config.open_shulker_from_inventory.desc",
			cfg -> cfg.allowOpenShulkerFromInventory,
			(cfg, v) -> cfg.allowOpenShulkerFromInventory = v,
			true
		));

		list.add(new LuipyConfigBooleanEntry(
			LuipyConfigCategory.WORLD,
			"luipy-utils-mod.config.block_highlight_enabled",
			"luipy-utils-mod.config.block_highlight_enabled.desc",
			cfg -> cfg.blockHighlightEnabled,
			(cfg, v) -> cfg.blockHighlightEnabled = v,
			false
		));

		return Collections.unmodifiableList(list);
	}

	private static void addWorkstationEntries(List<LuipyConfigBooleanEntry> list) {
		for (WorkstationKind kind : WorkstationKind.values()) {
			var blockItem = net.minecraft.world.item.Item.BY_BLOCK.get(kind.block);
			list.add(new LuipyConfigBooleanEntry(
				LuipyConfigCategory.INVENTORY,
				kind.showConfigKey(),
				kind.showConfigKey() + ".desc",
				kind::showEnabled,
				kind::setShow,
				false,
				blockItem
			));
			list.add(new LuipyConfigBooleanEntry(
				LuipyConfigCategory.INVENTORY,
				kind.alwaysConfigKey(),
				kind.alwaysConfigKey() + ".desc",
				kind::alwaysAvailable,
				kind::setAlwaysAvailable,
				false,
				blockItem
			));
			list.add(new LuipyConfigBooleanEntry(
				LuipyConfigCategory.INVENTORY,
				kind.nearbyConfigKey(),
				kind.nearbyConfigKey() + ".desc",
				kind::requireNearbyBlock,
				kind::setRequireNearbyBlock,
				true,
				blockItem
			));
		}
	}

	private static Map<LuipyConfigCategory, List<LuipyConfigBooleanEntry>> indexByCategory() {
		Map<LuipyConfigCategory, List<LuipyConfigBooleanEntry>> map = new EnumMap<>(LuipyConfigCategory.class);
		for (LuipyConfigCategory category : LuipyConfigCategory.values()) {
			map.put(category, new ArrayList<>());
		}
		for (LuipyConfigBooleanEntry entry : ENTRIES) {
			map.get(entry.category()).add(entry);
		}
		for (LuipyConfigCategory category : LuipyConfigCategory.values()) {
			map.put(category, Collections.unmodifiableList(map.get(category)));
		}
		return Collections.unmodifiableMap(map);
	}

	public static List<LuipyConfigBooleanEntry> allEntries() {
		return ENTRIES;
	}

	public static List<LuipyConfigBooleanEntry> forCategory(LuipyConfigCategory category) {
		return BY_CATEGORY.getOrDefault(category, List.of());
	}

	/** Restores defaults for every boolean entry in the given category. */
	public static void resetCategoryDefaults(LuipyUtilsConfig cfg, LuipyConfigCategory category) {
		for (LuipyConfigBooleanEntry entry : forCategory(category)) {
			entry.setter().accept(cfg, entry.defaultValue());
		}
		if (category == LuipyConfigCategory.WORLD) {
			cfg.ensureProfilesInitialized();
			for (int i = 0; i < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; i++) {
				LuipyUtilsConfig.HighlightProfile profile = cfg.blockHighlightProfiles[i];
				profile.name = "Profile " + (i + 1);
				profile.blockIds = "";
				profile.enabled = true;
				profile.useCustomTexture = false;
			}
			cfg.activeBlockHighlightProfile = 0;
			cfg.blockHighlightIds = "";
			HighlightEmphasisTextures.resetGlobalToDefault();
		}
	}
}
