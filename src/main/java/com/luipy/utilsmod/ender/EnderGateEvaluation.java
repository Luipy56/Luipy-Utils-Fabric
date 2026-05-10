package com.luipy.utilsmod.ender;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class EnderGateEvaluation {
	/**
	 * Fixed Chebyshev radius (max axis distance from feet) for detecting a loaded ender chest block.
	 * Matches the maximum chunk-aligned search the mod already capped at 128.
	 */
	public static final int LOADED_ENDER_CHEST_SEARCH_RADIUS_BLOCKS = 128;

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

	private static Optional<BlockPos> findNearestLoadedEnderChest(Player player, Level level) {
		int r = LOADED_ENDER_CHEST_SEARCH_RADIUS_BLOCKS;
		BlockPos origin = player.blockPosition();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestDistSq = Double.MAX_VALUE;
		for (int dx = -r; dx <= r; dx++) {
			for (int dy = -r; dy <= r; dy++) {
				for (int dz = -r; dz <= r; dz++) {
					pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
					if (!level.hasChunkAt(pos)) {
						continue;
					}
					if (!level.getBlockState(pos).is(Blocks.ENDER_CHEST)) {
						continue;
					}
					double dSq = player.distanceToSqr(Vec3.atCenterOf(pos));
					if (dSq < bestDistSq) {
						bestDistSq = dSq;
						best = pos.immutable();
					}
				}
			}
		}
		return Optional.ofNullable(best);
	}

	public static boolean hasLoadedEnderChestNearby(Player player, Level level) {
		return findNearestLoadedEnderChest(player, level).isPresent();
	}

	public static boolean passesGate(LuipyUtilsConfig cfg, Player player, Level level) {
		if (!cfg.masterEnabled) {
			return false;
		}
		if (cfg.alwaysAllowVirtualOpen) {
			return true;
		}
		boolean needItem = cfg.requireEnderChestItem;
		boolean needBlock = cfg.requireNearbyEnderChestBlock;
		if (!needItem && !needBlock) {
			return false;
		}
		boolean hasItem = !needItem || playerCarriesEnderChest(player);
		boolean hasBlock = !needBlock || hasLoadedEnderChestNearby(player, level);
		return hasItem || hasBlock;
	}
}
