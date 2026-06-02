package com.luipy.utilsmod.ender;

import com.luipy.utilsmod.config.EnderChestAccessMode;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class EnderGateEvaluation {
	/**
	 * Chebyshev radius (max axis distance from feet) for detecting a loaded ender chest block.
	 * Larger values make each inventory open very expensive (O(r³)); 48 matches the former default.
	 */
	public static final int LOADED_ENDER_CHEST_SEARCH_RADIUS_BLOCKS = 48;

	private EnderGateEvaluation() {
	}

	public static boolean playerCarriesEnderChest(Player player) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			if (player.getInventory().getItem(i).is(Items.ENDER_CHEST)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasLoadedEnderChestNearby(Player player, Level level) {
		int r = LOADED_ENDER_CHEST_SEARCH_RADIUS_BLOCKS;
		BlockPos origin = player.blockPosition();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = -r; dx <= r; dx++) {
			for (int dy = -r; dy <= r; dy++) {
				for (int dz = -r; dz <= r; dz++) {
					pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
					if (!level.hasChunkAt(pos)) {
						continue;
					}
					if (level.getBlockState(pos).is(Blocks.ENDER_CHEST)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean passesGate(LuipyUtilsConfig cfg, Player player, Level level) {
		if (!cfg.masterEnabled) {
			return false;
		}
		return switch (cfg.enderChestAccess) {
			case OFF -> false;
			case ALWAYS -> true;
			case BLOCK -> hasLoadedEnderChestNearby(player, level);
			case ITEM -> playerCarriesEnderChest(player);
			case BOTH -> playerCarriesEnderChest(player) || hasLoadedEnderChestNearby(player, level);
		};
	}

	/**
	 * Lang key for why {@link #passesGate} failed, or {@code null} when the gate passes or has no user-facing reason.
	 */
	public static String failureMessageKey(LuipyUtilsConfig cfg, Player player, Level level) {
		if (passesGate(cfg, player, level)) {
			return null;
		}
		return switch (cfg.enderChestAccess) {
			case OFF, ALWAYS -> null;
			case ITEM -> "luipy-utils-mod.message.ender_gate_need_item";
			case BLOCK -> "luipy-utils-mod.message.ender_gate_need_block";
			case BOTH -> "luipy-utils-mod.message.ender_gate_need_item_or_block";
		};
	}
}
