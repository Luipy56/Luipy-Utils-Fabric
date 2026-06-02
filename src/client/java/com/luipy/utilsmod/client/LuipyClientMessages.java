package com.luipy.utilsmod.client;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Client chat log for feature failures when {@link LuipyUtilsConfig#showToastsOnFailure} is on.
 */
public final class LuipyClientMessages {
	private LuipyClientMessages() {
	}

	/** Logs when {@code showToastsOnFailure} and {@code masterEnabled} are both enabled. */
	public static void featureFailure(Minecraft client, String translationKey, Object... args) {
		if (client.player == null) {
			return;
		}
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		if (!cfg.showToastsOnFailure || !cfg.masterEnabled) {
			return;
		}
		client.player.displayClientMessage(Component.translatable(translationKey, args), false);
	}
}
