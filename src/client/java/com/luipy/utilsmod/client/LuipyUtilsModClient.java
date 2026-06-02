package com.luipy.utilsmod.client;

import com.luipy.utilsmod.client.config.LuipyConfigKeybinds;
import com.luipy.utilsmod.client.config.BlockHighlightProfileKeybinds;
import com.luipy.utilsmod.client.ender.EnderGateHudIndicator;
import com.luipy.utilsmod.client.config.LuipyUnifiedMenuKeybinds;
import com.luipy.utilsmod.client.highlight.BlockHighlightManager;
import com.luipy.utilsmod.client.highlight.BlockHighlightModelPlugin;
import com.luipy.utilsmod.client.highlight.HighlightEmphasisTextures;
import com.luipy.utilsmod.client.inventory.LuipyShulkerScreen;
import com.luipy.utilsmod.client.inventory.LuipyUnifiedScreen;
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
		LuipyConfigKeybinds.register();
		LuipyUnifiedMenuKeybinds.register();
		BlockHighlightProfileKeybinds.register();
		EnderGateHudIndicator.register();
		BlockHighlightModelPlugin.register();
		HighlightEmphasisTextures.syncCustomFlagFromDisk();
		BlockHighlightManager.register();
		MenuScreens.register(LuipyMenuTypes.UNIFIED, LuipyUnifiedScreen::new);
		MenuScreens.register(LuipyMenuTypes.SHULKER_VIRTUAL, LuipyShulkerScreen::new);
		ClientPlayNetworking.registerGlobalReceiver(LuipyNetworking.S2C_SERVER_PRESENT, (client, handler, buf, responseSender) ->
			client.execute(() -> LuipyClientState.setServerHasLuipyMod(true))
		);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
			client.execute(() -> LuipyClientState.setServerHasLuipyMod(false))
		);
	}
}
