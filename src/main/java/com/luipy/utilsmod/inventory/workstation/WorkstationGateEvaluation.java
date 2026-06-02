package com.luipy.utilsmod.inventory.workstation;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.WorkstationAccessMode;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Proximity gates for optional workstation panels in the unified menu.
 */
public final class WorkstationGateEvaluation {
	public static final int NEARBY_BLOCK_SEARCH_RADIUS_BLOCKS =
		com.luipy.utilsmod.ender.EnderGateEvaluation.LOADED_ENDER_CHEST_SEARCH_RADIUS_BLOCKS;

	private WorkstationGateEvaluation() {
	}

	public static boolean passesGate(LuipyUtilsConfig cfg, Player player, Level level, WorkstationKind kind) {
		if (!cfg.masterEnabled) {
			return false;
		}
		return switch (kind.accessMode(cfg)) {
			case OFF -> false;
			case ALWAYS -> true;
			case NEARBY -> hasLoadedBlockNearby(player, level, kind);
		};
	}

	public static boolean hasLoadedBlockNearby(Player player, Level level, WorkstationKind kind) {
		int r = NEARBY_BLOCK_SEARCH_RADIUS_BLOCKS;
		BlockPos origin = player.blockPosition();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = -r; dx <= r; dx++) {
			for (int dy = -r; dy <= r; dy++) {
				for (int dz = -r; dz <= r; dz++) {
					pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
					if (!level.hasChunkAt(pos)) {
						continue;
					}
					BlockState state = level.getBlockState(pos);
					if (matchesWorkstation(state, kind)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean matchesWorkstation(BlockState state, WorkstationKind kind) {
		return switch (kind) {
			case ANVIL -> state.getBlock() instanceof AnvilBlock || state.is(BlockTags.ANVIL);
			case SMITHING -> state.is(kind.block);
			case CARTOGRAPHY -> state.is(kind.block);
			case GRINDSTONE -> state.is(kind.block);
			case STONECUTTER -> state.is(kind.block);
			case LOOM -> state.is(kind.block);
		};
	}
}
