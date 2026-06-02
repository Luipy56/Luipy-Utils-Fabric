package com.luipy.utilsmod.client.config;

import com.luipy.utilsmod.client.ender.LuipyUnifiedMenuOpener;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Default gesture: press <strong>R</strong> to open {@link com.luipy.utilsmod.inventory.LuipyUnifiedMenu}.
 * Plain R is unbound in vanilla 1.20.1 default controls (unlike L for swap-offhand), so no Alt chord is needed.
 * A {@link KeyMapping} is registered for discoverability in Controls; vanilla {@code E} opens stock inventory only.
 */
public final class LuipyUnifiedMenuKeybinds {
	public static final KeyMapping OPEN_UNIFIED_MENU = new KeyMapping(
		"key.luipy-utils-mod.open_unified_menu",
		GLFW.GLFW_KEY_R,
		LuipyConfigKeybinds.CATEGORY
	);

	private static boolean rWasDown;

	private LuipyUnifiedMenuKeybinds() {
	}

	public static void register() {
		KeyBindingHelper.registerKeyBinding(OPEN_UNIFIED_MENU);
		ClientTickEvents.END_CLIENT_TICK.register(LuipyUnifiedMenuKeybinds::onClientTick);
	}

	private static void onClientTick(Minecraft client) {
		if (client.player == null) {
			rWasDown = false;
			return;
		}

		if (client.screen != null) {
			rWasDown = false;
			return;
		}

		long window = client.getWindow().getWindow();
		boolean rDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;

		if (rDown && !rWasDown) {
			LuipyUnifiedMenuOpener.tryOpen(client);
		}

		rWasDown = rDown;
	}
}
