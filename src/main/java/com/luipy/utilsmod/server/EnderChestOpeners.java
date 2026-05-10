package com.luipy.utilsmod.server;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.ender.EnderGateEvaluation;
import com.luipy.utilsmod.inventory.LuipyInventoryWithEnderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;

public final class EnderChestOpeners {
	private EnderChestOpeners() {
	}

	public static void tryOpenFor(ServerPlayer player) {
		if (player.isCreative()) {
			return;
		}
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		if (!cfg.masterEnabled || !cfg.showEnderChestWithInventory) {
			return;
		}
		Level level = player.level();
		if (!EnderGateEvaluation.passesGate(cfg, player, level)) {
			return;
		}
		SimpleMenuProvider factory = new SimpleMenuProvider(
			(syncId, inventory, p) -> new LuipyInventoryWithEnderMenu(syncId, inventory),
			Component.empty()
		);
		player.openMenu(factory);
	}
}
