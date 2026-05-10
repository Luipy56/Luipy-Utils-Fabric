package com.luipy.utilsmod.ender;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

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

	public static boolean hasLoadedEnderChestNearby(Player player, Level level, int radius) {
		if (radius <= 0) {
			return false;
		}
		int r = Math.min(radius, 128);
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
