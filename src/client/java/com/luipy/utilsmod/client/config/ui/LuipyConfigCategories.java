package com.luipy.utilsmod.client.config.ui;

import com.luipy.utilsmod.client.highlight.HighlightEmphasisTextures;
import com.luipy.utilsmod.config.EnderChestAccessMode;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.WorkstationAccessMode;
import com.luipy.utilsmod.inventory.workstation.WorkstationKind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Items;

/**
 * Central registry mapping {@link LuipyUtilsConfig} fields to UI metadata.
 */
public final class LuipyConfigCategories {
	private static final List<WorkstationAccessMode> WORKSTATION_MODES =
		List.of(WorkstationAccessMode.OFF, WorkstationAccessMode.NEARBY, WorkstationAccessMode.ALWAYS);
	private static final List<EnderChestAccessMode> ENDER_MODES = List.of(
		EnderChestAccessMode.OFF,
		EnderChestAccessMode.BLOCK,
		EnderChestAccessMode.ITEM,
		EnderChestAccessMode.BOTH,
		EnderChestAccessMode.ALWAYS
	);

	private static final List<LuipyConfigRowEntry> ENTRIES = buildEntries();
	private static final Map<LuipyConfigCategory, List<LuipyConfigRowEntry>> BY_CATEGORY = indexByCategory();

	private LuipyConfigCategories() {
	}

	private static List<LuipyConfigRowEntry> buildEntries() {
		List<LuipyConfigRowEntry> list = new ArrayList<>();

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

		list.add(new LuipyConfigCycleEntry<>(
			LuipyConfigCategory.INVENTORY,
			"luipy-utils-mod.config.ender_chest.access",
			"luipy-utils-mod.config.ender_chest.access.desc",
			cfg -> cfg.enderChestAccess,
			(cfg, v) -> cfg.enderChestAccess = v,
			EnderChestAccessMode.BOTH,
			ENDER_MODES,
			mode -> "luipy-utils-mod.config.access." + mode.name().toLowerCase(),
			LuipyConfigCycleToggle.enderAccessColors(EnderChestAccessMode.OFF, EnderChestAccessMode.ALWAYS),
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
			false,
			Items.CRAFTING_TABLE
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

	private static void addWorkstationEntries(List<LuipyConfigRowEntry> list) {
		for (WorkstationKind kind : WorkstationKind.values()) {
			var blockItem = net.minecraft.world.item.Item.BY_BLOCK.get(kind.block);
			list.add(new LuipyConfigCycleEntry<>(
				LuipyConfigCategory.INVENTORY,
				kind.accessConfigKey(),
				kind.accessConfigKey() + ".desc",
				kind::accessMode,
				kind::setAccessMode,
				WorkstationAccessMode.OFF,
				WORKSTATION_MODES,
				mode -> "luipy-utils-mod.config.access." + mode.name().toLowerCase(),
				LuipyConfigCycleToggle.offNeutralAlwaysColors(
					WorkstationAccessMode.OFF,
					WorkstationAccessMode.ALWAYS
				),
				blockItem
			));
		}
	}

	private static Map<LuipyConfigCategory, List<LuipyConfigRowEntry>> indexByCategory() {
		Map<LuipyConfigCategory, List<LuipyConfigRowEntry>> map = new EnumMap<>(LuipyConfigCategory.class);
		for (LuipyConfigCategory category : LuipyConfigCategory.values()) {
			map.put(category, new ArrayList<>());
		}
		for (LuipyConfigRowEntry entry : ENTRIES) {
			map.get(entry.category()).add(entry);
		}
		for (LuipyConfigCategory category : LuipyConfigCategory.values()) {
			map.put(category, Collections.unmodifiableList(map.get(category)));
		}
		return Collections.unmodifiableMap(map);
	}

	public static List<LuipyConfigRowEntry> allEntries() {
		return ENTRIES;
	}

	public static List<LuipyConfigRowEntry> forCategory(LuipyConfigCategory category) {
		return BY_CATEGORY.getOrDefault(category, List.of());
	}

	/** Restores defaults for every entry in the given category. */
	public static void resetCategoryDefaults(LuipyUtilsConfig cfg, LuipyConfigCategory category) {
		for (LuipyConfigRowEntry entry : forCategory(category)) {
			if (entry instanceof LuipyConfigBooleanEntry booleanEntry) {
				booleanEntry.setter().accept(cfg, booleanEntry.defaultValue());
			} else if (entry instanceof LuipyConfigCycleEntry<?> cycleEntry) {
				resetCycleDefault(cfg, cycleEntry);
			}
		}
		if (category == LuipyConfigCategory.WORLD) {
			cfg.ensureProfilesInitialized();
			for (int i = 0; i < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; i++) {
				LuipyUtilsConfig.HighlightProfile profile = cfg.blockHighlightProfiles[i];
				profile.name = "Profile " + (i + 1);
				profile.blockIds = i == 0 ? LuipyUtilsConfig.DEFAULT_HIGHLIGHT_BLOCK_IDS : "";
				profile.enabled = true;
				profile.useCustomTexture = false;
			}
			cfg.activeBlockHighlightProfile = 0;
			cfg.blockHighlightIds = "";
			HighlightEmphasisTextures.resetGlobalToDefault();
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> void resetCycleDefault(LuipyUtilsConfig cfg, LuipyConfigCycleEntry<T> entry) {
		entry.setter().accept(cfg, entry.defaultValue());
	}

	/** Boolean entries only (World tab header). */
	public static List<LuipyConfigBooleanEntry> booleanEntriesForCategory(LuipyConfigCategory category) {
		List<LuipyConfigBooleanEntry> list = new ArrayList<>();
		for (LuipyConfigRowEntry entry : forCategory(category)) {
			if (entry instanceof LuipyConfigBooleanEntry booleanEntry) {
				list.add(booleanEntry);
			}
		}
		return list;
	}
}
