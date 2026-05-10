package com.luipy.utilsmod.client.ender;

import com.luipy.utilsmod.client.LuipyClientState;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.ender.EnderGateEvaluation;
import com.luipy.utilsmod.network.LuipyNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

public final class LuipyInventoryKeyHandler {
	private LuipyInventoryKeyHandler() {
	}

	public static void handleInventorySetScreen(Minecraft client, Screen screen) {
		if (!(screen instanceof InventoryScreen)) {
			client.setScreen(screen);
			return;
		}
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		if (!cfg.masterEnabled || !cfg.showEnderChestWithInventory) {
			client.setScreen(screen);
			return;
		}
		if (client.player == null || client.level == null) {
			client.setScreen(screen);
			return;
		}
		if (client.player.isCreative()) {
			client.setScreen(screen);
			return;
		}
		if (!EnderGateEvaluation.passesGate(cfg, client.player, client.level)) {
			client.setScreen(screen);
			return;
		}

		boolean modServerOrSingleplayer = LuipyClientState.serverHasLuipyMod() || client.getSingleplayerServer() != null;
		if (modServerOrSingleplayer) {
			ClientPlayNetworking.send(LuipyNetworking.C2S_OPEN_ENDER, PacketByteBufs.empty());
			return;
		}

		client.setScreen(screen);
		boolean willTryVanillaEnder = cfg.tryOpenNearestEnderOnVanillaServer
			&& EnderGateEvaluation.hasLoadedEnderChestNearby(client.player, client.level, cfg.nearbySearchRadiusBlocks);
		if (willTryVanillaEnder) {
			client.execute(() -> {
				if (LuipyClientState.serverHasLuipyMod()) {
					return;
				}
				if (!(client.screen instanceof InventoryScreen)) {
					return;
				}
				LuipyVanillaServerEnderFallback.tryUseNearestEnderChest(client, cfg);
			});
		} else if (cfg.showToastsOnFailure && client.player != null) {
			client.player.displayClientMessage(
				Component.translatable("luipy-utils-mod.message.combined_requires_mod_on_server"),
				false
			);
		}
	}
}
