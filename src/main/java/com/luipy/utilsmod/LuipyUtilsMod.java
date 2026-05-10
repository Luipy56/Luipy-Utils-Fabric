package com.luipy.utilsmod;

import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.inventory.LuipyMenuTypes;
import com.luipy.utilsmod.network.LuipyNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuipyUtilsMod implements ModInitializer {
	public static final String MOD_ID = "luipy-utils-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		LuipyUtilsConfigManager.load();
		LuipyMenuTypes.register();
		LuipyNetworking.registerServer();
		LOGGER.info("LuipyUtils server init");
	}
}
