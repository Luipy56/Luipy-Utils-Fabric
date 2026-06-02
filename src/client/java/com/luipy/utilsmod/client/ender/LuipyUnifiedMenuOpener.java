package com.luipy.utilsmod.client.ender;

import com.luipy.utilsmod.client.LuipyClientMessages;
import com.luipy.utilsmod.client.LuipyClientState;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.ender.EnderGateAccess;
import com.luipy.utilsmod.ender.EnderGateEvaluation;
import com.luipy.utilsmod.network.LuipyNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;

/**
 * Opens the unified menu (R) when config and server gates allow.
 * Vanilla {@code E} is never intercepted — see {@link com.luipy.utilsmod.client.config.LuipyUnifiedMenuKeybinds}.
 */
public final class LuipyUnifiedMenuOpener {
	private LuipyUnifiedMenuOpener() {
	}

	public static void tryOpen(Minecraft client) {
		if (client.player == null || client.level == null || client.screen != null) {
			return;
		}
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		if (!cfg.masterEnabled) {
			LuipyClientMessages.actionBlocked(client, "luipy-utils-mod.message.master_disabled");
			return;
		}
		if (client.player.isCreative()) {
			LuipyClientMessages.featureFailure(client, "luipy-utils-mod.message.unified_creative");
			return;
		}
		if (EnderGateAccess.enderGateBlocksUnifiedMenu(cfg, client.player, client.level)) {
			String key = EnderGateEvaluation.failureMessageKey(cfg, client.player, client.level);
			if (key != null) {
				LuipyClientMessages.featureFailure(client, key);
			}
			return;
		}

		boolean modServerOrSingleplayer = LuipyClientState.serverHasLuipyMod() || client.getSingleplayerServer() != null;
		if (modServerOrSingleplayer) {
			ClientPlayNetworking.send(LuipyNetworking.C2S_OPEN_UNIFIED_MENU, PacketByteBufs.empty());
			return;
		}

		LuipyClientMessages.featureFailure(client, "luipy-utils-mod.message.unified_requires_mod_on_server");
	}
}
