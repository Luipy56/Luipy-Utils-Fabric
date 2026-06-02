package com.luipy.utilsmod.inventory.workstation;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Workstation panels that may appear in the left column of {@link com.luipy.utilsmod.inventory.LuipyUnifiedMenu}.
 * Order is fixed top-to-bottom when multiple panels are enabled.
 */
public enum WorkstationKind {
	ANVIL(
		Blocks.ANVIL,
		166,
		cfg -> cfg.showAnvilWithInventory,
		cfg -> cfg.anvilAlwaysAvailable,
		cfg -> cfg.anvilRequireNearbyBlock,
		(cfg, v) -> cfg.showAnvilWithInventory = v,
		(cfg, v) -> cfg.anvilAlwaysAvailable = v,
		(cfg, v) -> cfg.anvilRequireNearbyBlock = v
	),
	SMITHING(
		Blocks.SMITHING_TABLE,
		166,
		cfg -> cfg.showSmithingTableWithInventory,
		cfg -> cfg.smithingTableAlwaysAvailable,
		cfg -> cfg.smithingTableRequireNearbyBlock,
		(cfg, v) -> cfg.showSmithingTableWithInventory = v,
		(cfg, v) -> cfg.smithingTableAlwaysAvailable = v,
		(cfg, v) -> cfg.smithingTableRequireNearbyBlock = v
	),
	CARTOGRAPHY(
		Blocks.CARTOGRAPHY_TABLE,
		166,
		cfg -> cfg.showCartographyTableWithInventory,
		cfg -> cfg.cartographyTableAlwaysAvailable,
		cfg -> cfg.cartographyTableRequireNearbyBlock,
		(cfg, v) -> cfg.showCartographyTableWithInventory = v,
		(cfg, v) -> cfg.cartographyTableAlwaysAvailable = v,
		(cfg, v) -> cfg.cartographyTableRequireNearbyBlock = v
	),
	GRINDSTONE(
		Blocks.GRINDSTONE,
		79,
		cfg -> cfg.showGrindstoneWithInventory,
		cfg -> cfg.grindstoneAlwaysAvailable,
		cfg -> cfg.grindstoneRequireNearbyBlock,
		(cfg, v) -> cfg.showGrindstoneWithInventory = v,
		(cfg, v) -> cfg.grindstoneAlwaysAvailable = v,
		(cfg, v) -> cfg.grindstoneRequireNearbyBlock = v
	),
	STONECUTTER(
		Blocks.STONECUTTER,
		166,
		cfg -> cfg.showStonecutterWithInventory,
		cfg -> cfg.stonecutterAlwaysAvailable,
		cfg -> cfg.stonecutterRequireNearbyBlock,
		(cfg, v) -> cfg.showStonecutterWithInventory = v,
		(cfg, v) -> cfg.stonecutterAlwaysAvailable = v,
		(cfg, v) -> cfg.stonecutterRequireNearbyBlock = v
	),
	LOOM(
		Blocks.LOOM,
		166,
		cfg -> cfg.showLoomWithInventory,
		cfg -> cfg.loomAlwaysAvailable,
		cfg -> cfg.loomRequireNearbyBlock,
		(cfg, v) -> cfg.showLoomWithInventory = v,
		(cfg, v) -> cfg.loomAlwaysAvailable = v,
		(cfg, v) -> cfg.loomRequireNearbyBlock = v
	);

	public final Block block;
	public final int panelHeight;
	private final Function<LuipyUtilsConfig, Boolean> showGetter;
	private final Function<LuipyUtilsConfig, Boolean> alwaysAvailableGetter;
	private final Function<LuipyUtilsConfig, Boolean> requireNearbyGetter;
	private final BiConsumer<LuipyUtilsConfig, Boolean> showSetter;
	private final BiConsumer<LuipyUtilsConfig, Boolean> alwaysAvailableSetter;
	private final BiConsumer<LuipyUtilsConfig, Boolean> requireNearbySetter;

	WorkstationKind(
		Block block,
		int panelHeight,
		Function<LuipyUtilsConfig, Boolean> showGetter,
		Function<LuipyUtilsConfig, Boolean> alwaysAvailableGetter,
		Function<LuipyUtilsConfig, Boolean> requireNearbyGetter,
		BiConsumer<LuipyUtilsConfig, Boolean> showSetter,
		BiConsumer<LuipyUtilsConfig, Boolean> alwaysAvailableSetter,
		BiConsumer<LuipyUtilsConfig, Boolean> requireNearbySetter
	) {
		this.block = block;
		this.panelHeight = panelHeight;
		this.showGetter = showGetter;
		this.alwaysAvailableGetter = alwaysAvailableGetter;
		this.requireNearbyGetter = requireNearbyGetter;
		this.showSetter = showSetter;
		this.alwaysAvailableSetter = alwaysAvailableSetter;
		this.requireNearbySetter = requireNearbySetter;
	}

	public boolean showEnabled(LuipyUtilsConfig cfg) {
		return showGetter.apply(cfg);
	}

	public boolean alwaysAvailable(LuipyUtilsConfig cfg) {
		return alwaysAvailableGetter.apply(cfg);
	}

	public boolean requireNearbyBlock(LuipyUtilsConfig cfg) {
		return requireNearbyGetter.apply(cfg);
	}

	public void setShow(LuipyUtilsConfig cfg, boolean value) {
		showSetter.accept(cfg, value);
	}

	public void setAlwaysAvailable(LuipyUtilsConfig cfg, boolean value) {
		alwaysAvailableSetter.accept(cfg, value);
	}

	public void setRequireNearbyBlock(LuipyUtilsConfig cfg, boolean value) {
		requireNearbySetter.accept(cfg, value);
	}

	public String showConfigKey() {
		return "luipy-utils-mod.config.workstation." + name().toLowerCase() + ".show";
	}

	public String alwaysConfigKey() {
		return "luipy-utils-mod.config.workstation." + name().toLowerCase() + ".always";
	}

	public String nearbyConfigKey() {
		return "luipy-utils-mod.config.workstation." + name().toLowerCase() + ".nearby";
	}
}
