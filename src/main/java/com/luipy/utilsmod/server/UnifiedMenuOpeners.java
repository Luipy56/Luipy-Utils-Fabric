package com.luipy.utilsmod.server;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.inventory.LuipyUnifiedMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public final class UnifiedMenuOpeners {
	private UnifiedMenuOpeners() {
	}

	/**
	 * Opens {@link LuipyUnifiedMenu} after {@code C2S_OPEN_UNIFIED_MENU}.
	 * <p>
	 * We intentionally do <strong>not</strong> re-run {@link com.luipy.utilsmod.ender.EnderGateEvaluation} here:
	 * the client already applied item / block / {@code alwaysAllowVirtualOpen} gates before sending the packet.
	 */
	public static void tryOpenFor(ServerPlayer player) {
		if (player.isCreative()) {
			return;
		}
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		if (!cfg.masterEnabled) {
			return;
		}
		SimpleMenuProvider factory = new SimpleMenuProvider(
			(syncId, inventory, p) -> new LuipyUnifiedMenu(syncId, inventory),
			Component.translatable("luipy-utils-mod.screen.unified_menu")
		);
		player.openMenu(factory);
	}
}
