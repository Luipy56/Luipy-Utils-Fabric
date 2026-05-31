package com.luipy.utilsmod.client.config;

import com.luipy.utilsmod.client.ender.LuipyUnifiedMenuOpener;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Default gesture: hold <strong>Alt</strong> and press <strong>L</strong> to open {@link com.luipy.utilsmod.inventory.LuipyUnifiedMenu}.
 * A {@link KeyMapping} is registered for discoverability in Controls; vanilla {@code E} opens stock inventory only.
 */
public final class LuipyUnifiedMenuKeybinds {
	public static final KeyMapping OPEN_UNIFIED_MENU = new KeyMapping(
		"key.luipy-utils-mod.open_unified_menu",
		GLFW.GLFW_KEY_UNKNOWN,
		LuipyConfigKeybinds.CATEGORY
	);

	private static boolean altLWasActive;

	private LuipyUnifiedMenuKeybinds() {
	}

	public static void register() {
		KeyBindingHelper.registerKeyBinding(OPEN_UNIFIED_MENU);
		ClientTickEvents.END_CLIENT_TICK.register(LuipyUnifiedMenuKeybinds::onClientTick);
	}

	private static void onClientTick(Minecraft client) {
		if (client.player == null) {
			altLWasActive = false;
			return;
		}

		long window = client.getWindow().getWindow();
		boolean altDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS
			|| GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
		boolean lDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_L) == GLFW.GLFW_PRESS;
		boolean altLActive = altDown && lDown;

		if (altLActive && !altLWasActive) {
			LuipyUnifiedMenuOpener.tryOpen(client);
		}

		altLWasActive = altLActive;
	}
}
