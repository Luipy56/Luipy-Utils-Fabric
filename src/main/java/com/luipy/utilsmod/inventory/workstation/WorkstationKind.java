package com.luipy.utilsmod.inventory.workstation;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.WorkstationAccessMode;
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
		84,
		cfg -> cfg.anvilAccess,
		(cfg, mode) -> cfg.anvilAccess = mode
	),
	SMITHING(
		Blocks.SMITHING_TABLE,
		84,
		cfg -> cfg.smithingAccess,
		(cfg, mode) -> cfg.smithingAccess = mode
	),
	CARTOGRAPHY(
		Blocks.CARTOGRAPHY_TABLE,
		84,
		cfg -> cfg.cartographyAccess,
		(cfg, mode) -> cfg.cartographyAccess = mode
	),
	GRINDSTONE(
		Blocks.GRINDSTONE,
		79,
		cfg -> cfg.grindstoneAccess,
		(cfg, mode) -> cfg.grindstoneAccess = mode
	),
	STONECUTTER(
		Blocks.STONECUTTER,
		84,
		cfg -> cfg.stonecutterAccess,
		(cfg, mode) -> cfg.stonecutterAccess = mode
	),
	LOOM(
		Blocks.LOOM,
		84,
		cfg -> cfg.loomAccess,
		(cfg, mode) -> cfg.loomAccess = mode
	);

	/** Pixels trimmed from the bottom of each panel blit (decorative strip below workstation slots). */
	public static final int PANEL_BOTTOM_TRIM = 2;

	public final Block block;
	public final int panelHeight;
	private final Function<LuipyUtilsConfig, WorkstationAccessMode> accessGetter;
	private final BiConsumer<LuipyUtilsConfig, WorkstationAccessMode> accessSetter;

	WorkstationKind(
		Block block,
		int panelHeight,
		Function<LuipyUtilsConfig, WorkstationAccessMode> accessGetter,
		BiConsumer<LuipyUtilsConfig, WorkstationAccessMode> accessSetter
	) {
		this.block = block;
		this.panelHeight = panelHeight;
		this.accessGetter = accessGetter;
		this.accessSetter = accessSetter;
	}

	public WorkstationAccessMode accessMode(LuipyUtilsConfig cfg) {
		return accessGetter.apply(cfg);
	}

	public void setAccessMode(LuipyUtilsConfig cfg, WorkstationAccessMode mode) {
		accessSetter.accept(cfg, mode);
	}

	/** Height used for blit and vertical stacking (excludes {@link #PANEL_BOTTOM_TRIM}). */
	public int layoutHeight() {
		return this.panelHeight - PANEL_BOTTOM_TRIM;
	}

	public String accessConfigKey() {
		return "luipy-utils-mod.config.workstation." + name().toLowerCase() + ".access";
	}
}
