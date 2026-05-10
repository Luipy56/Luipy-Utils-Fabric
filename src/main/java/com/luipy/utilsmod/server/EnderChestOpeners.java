package com.luipy.utilsmod.server;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.inventory.LuipyInventoryWithEnderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public final class EnderChestOpeners {
	private EnderChestOpeners() {
	}

	/**
	 * Opens the combined inventory + ender menu after {@code C2S_OPEN_ENDER}.
	 * <p>
	 * We intentionally do <strong>not</strong> re-run {@link com.luipy.utilsmod.ender.EnderGateEvaluation} here:
	 * the client already applied item / block / {@code alwaysAllowVirtualOpen} and, on success, suppresses
	 * {@link net.minecraft.client.gui.screens.inventory.InventoryScreen}. If the server used its own config
	 * copy (e.g. {@code alwaysAllowVirtualOpen} only on the client), {@code passesGate} would fail and the
	 * player would end up with no GUI at all.
	 */
	public static void tryOpenFor(ServerPlayer player) {
		if (player.isCreative()) {
			return;
		}
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		if (!cfg.masterEnabled || !cfg.showEnderChestWithInventory) {
			return;
		}
		SimpleMenuProvider factory = new SimpleMenuProvider(
			(syncId, inventory, p) -> new LuipyInventoryWithEnderMenu(syncId, inventory),
			Component.translatable("luipy-utils-mod.screen.inventory_with_ender")
		);
		player.openMenu(factory);
	}
}
