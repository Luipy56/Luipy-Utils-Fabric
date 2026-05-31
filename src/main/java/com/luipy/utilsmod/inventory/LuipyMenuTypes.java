package com.luipy.utilsmod.inventory;

import com.luipy.utilsmod.LuipyUtilsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class LuipyMenuTypes {
	public static MenuType<LuipyUnifiedMenu> UNIFIED;
	public static MenuType<LuipyShulkerMenu> SHULKER_VIRTUAL;

	private LuipyMenuTypes() {
	}

	public static void register() {
		UNIFIED = Registry.register(
			BuiltInRegistries.MENU,
			LuipyUtilsMod.id("unified_menu"),
			new MenuType<>(LuipyUnifiedMenu::new, FeatureFlags.DEFAULT_FLAGS)
		);
		SHULKER_VIRTUAL = Registry.register(
			BuiltInRegistries.MENU,
			LuipyUtilsMod.id("shulker_virtual"),
			new MenuType<>(LuipyShulkerMenu::new, FeatureFlags.DEFAULT_FLAGS)
		);
	}
}
