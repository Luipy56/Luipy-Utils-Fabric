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

	/**
	 * Nearest loaded ender chest block within Chebyshev radius of the player's feet, or empty if none.
	 * When {@code maxEyeToCenterDistSq} is finite, only chests whose centre is within that squared distance
	 * from the player's eyes are considered (for client-side interaction reach).
	 */
	public static Optional<BlockPos> findNearestLoadedEnderChest(Player player, Level level, int radius, double maxEyeToCenterDistSq) {
		if (radius <= 0) {
			return Optional.empty();
		}
		int r = Math.min(radius, 128);
		BlockPos origin = player.blockPosition();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestDistSq = Double.MAX_VALUE;
		Vec3 eye = player.getEyePosition(1.0F);
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
					Vec3 center = Vec3.atCenterOf(pos);
					double eyeSq = eye.distanceToSqr(center);
					if (eyeSq > maxEyeToCenterDistSq) {
						continue;
					}
					if (eyeSq < bestDistSq) {
						bestDistSq = eyeSq;
						best = pos.immutable();
					}
				}
			}
		}
		return Optional.ofNullable(best);
	}

	public static Optional<BlockPos> findNearestLoadedEnderChest(Player player, Level level, int radius) {
		return findNearestLoadedEnderChest(player, level, radius, Double.POSITIVE_INFINITY);
	}

	public static boolean hasLoadedEnderChestNearby(Player player, Level level, int radius) {
		return findNearestLoadedEnderChest(player, level, radius).isPresent();
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
		boolean hasBlock = !needBlock || hasLoadedEnderChestNearby(player, level, cfg.nearbySearchRadiusBlocks);
		return hasItem || hasBlock;
	}
}
