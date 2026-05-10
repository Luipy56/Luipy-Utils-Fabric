package com.luipy.utilsmod.client;

import com.luipy.utilsmod.client.inventory.LuipyInventoryWithEnderScreen;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.inventory.LuipyMenuTypes;
import com.luipy.utilsmod.network.LuipyNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;

public class LuipyUtilsModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LuipyUtilsConfigManager.load();
		MenuScreens.register(LuipyMenuTypes.INVENTORY_WITH_ENDER, LuipyInventoryWithEnderScreen::new);
		ClientPlayNetworking.registerGlobalReceiver(LuipyNetworking.S2C_SERVER_PRESENT, (client, handler, buf, responseSender) ->
			client.execute(() -> LuipyClientState.setServerHasLuipyMod(true))
		);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
			client.execute(() -> LuipyClientState.setServerHasLuipyMod(false))
		);
	}
}
