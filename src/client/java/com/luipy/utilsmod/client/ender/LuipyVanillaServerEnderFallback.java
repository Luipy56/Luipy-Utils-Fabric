package com.luipy.utilsmod.client.ender;

import com.luipy.utilsmod.ender.EnderGateEvaluation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Vanilla dedicated servers cannot open {@link com.luipy.utilsmod.inventory.LuipyInventoryWithEnderMenu}.
 * This class simulates a right-click on the nearest ender chest so the stock ender screen opens.
 */
public final class LuipyVanillaServerEnderFallback {
	private LuipyVanillaServerEnderFallback() {
	}

	public static void tryUseNearestEnderChest(Minecraft client, int radiusBlocks) {
		if (client.player == null || client.level == null || client.gameMode == null) {
			return;
		}
		BlockPos chest = EnderGateEvaluation.findNearestLoadedEnderChest(client.player, client.level, radiusBlocks).orElse(null);
		if (chest == null) {
			return;
		}
		Vec3 center = Vec3.atCenterOf(chest);
		Vec3 eye = client.player.getEyePosition(1.0F);
		Vec3 delta = center.subtract(eye);
		Direction face = Direction.getNearest(delta.x, delta.y, delta.z);
		BlockHitResult hit = new BlockHitResult(center, face, chest, false);
		client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
	}
}
