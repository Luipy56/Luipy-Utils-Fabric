package com.luipy.utilsmod.inventory;

import com.luipy.utilsmod.LuipyUtilsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class LuipyMenuTypes {
	public static MenuType<LuipyInventoryWithEnderMenu> INVENTORY_WITH_ENDER;

	private LuipyMenuTypes() {
	}

	public static void register() {
		INVENTORY_WITH_ENDER = Registry.register(
			BuiltInRegistries.MENU,
			LuipyUtilsMod.id("inventory_with_ender"),
			new MenuType<>(LuipyInventoryWithEnderMenu::new, FeatureFlags.DEFAULT_FLAGS)
		);
	}
}
