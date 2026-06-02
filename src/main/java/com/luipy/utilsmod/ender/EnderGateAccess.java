package com.luipy.utilsmod.ender;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Shared ender-gate checks for the unified menu opener and client HUD indicator.
 */
public final class EnderGateAccess {
	private EnderGateAccess() {
	}

	/** When true, the unified menu must not open because the ender panel is enabled but its gate failed. */
	public static boolean enderGateBlocksUnifiedMenu(LuipyUtilsConfig cfg, Player player, Level level) {
		return cfg.showEnderChestWithInventory && !EnderGateEvaluation.passesGate(cfg, player, level);
	}

	/** When true, the ender chest HUD indicator may be shown (config + gate only; client adds server/SP checks). */
	public static boolean enderHudGatePasses(LuipyUtilsConfig cfg, Player player, Level level) {
		return cfg.showEnderGateHudIndicator
			&& cfg.showEnderChestWithInventory
			&& EnderGateEvaluation.passesGate(cfg, player, level);
	}
}
