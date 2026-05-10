package com.luipy.utilsmod.client.ender;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.ender.EnderGateEvaluation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Vanilla dedicated servers cannot open {@link com.luipy.utilsmod.inventory.LuipyInventoryWithEnderMenu}.
 * Simulates a right-click on the nearest ender chest within client pick range (+ bonus) so the stock ender GUI can open.
 */
public final class LuipyVanillaServerEnderFallback {
	private LuipyVanillaServerEnderFallback() {
	}

	public static void tryUseNearestEnderChest(Minecraft client, LuipyUtilsConfig cfg) {
		if (client.player == null || client.level == null || client.gameMode == null) {
			return;
		}
		float pick = client.gameMode.getPickRange();
		double maxEyeSq = Mth.square((double) pick + cfg.enderOpenReachBonus);
		BlockPos chest = EnderGateEvaluation.findNearestLoadedEnderChest(
			client.player,
			client.level,
			cfg.nearbySearchRadiusBlocks,
			maxEyeSq
		).orElse(null);
		if (chest == null) {
			return;
		}
		Vec3 center = Vec3.atCenterOf(chest);
		Vec3 eye = client.player.getEyePosition(1.0F);
		Vec3 toward = center.subtract(eye);
		double len = toward.length();
		Vec3 rayEnd = len < 1.0E-4 ? eye : eye.add(toward.normalize().scale(pick));
		BlockHitResult traced = client.level.clip(
			new ClipContext(eye, rayEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, client.player)
		);
		BlockHitResult hit;
		if (traced.getType() == HitResult.Type.BLOCK && chest.equals(traced.getBlockPos())) {
			hit = traced;
		} else {
			Vec3 delta = center.subtract(eye);
			Direction face = Direction.getNearest(delta.x, delta.y, delta.z);
			hit = new BlockHitResult(center, face, chest, false);
		}
		client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
	}
}
