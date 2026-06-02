package com.luipy.utilsmod.client.config;

import com.luipy.utilsmod.client.ender.LuipyUnifiedMenuOpener;
import com.luipy.utilsmod.client.inventory.LuipyUnifiedScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Default gesture: hold <strong>Alt</strong> and press <strong>R</strong> to toggle
 * {@link com.luipy.utilsmod.inventory.LuipyUnifiedMenu} (open when closed, close when
 * {@link LuipyUnifiedScreen} is active). Plain R collides with other bindings on many setups;
 * Alt+R uses the same modifier-chord pattern as the former Alt+L handler.
 * A {@link KeyMapping} uses {@link InputConstants#UNKNOWN} because the gesture is a chord, not a single key.
 */
public final class LuipyUnifiedMenuKeybinds {
	public static final KeyMapping OPEN_UNIFIED_MENU = new KeyMapping(
		"key.luipy-utils-mod.open_unified_menu",
		InputConstants.UNKNOWN.getValue(),
		LuipyConfigKeybinds.CATEGORY
	);

	private static boolean altRWasActive;

	private LuipyUnifiedMenuKeybinds() {
	}

	public static void register() {
		KeyBindingHelper.registerKeyBinding(OPEN_UNIFIED_MENU);
		ClientTickEvents.END_CLIENT_TICK.register(LuipyUnifiedMenuKeybinds::onClientTick);
	}

	private static void onClientTick(Minecraft client) {
		if (client.player == null) {
			altRWasActive = false;
			return;
		}

		var screen = client.screen;
		if (screen != null && !(screen instanceof LuipyUnifiedScreen)) {
			altRWasActive = false;
			return;
		}

		long window = client.getWindow().getWindow();
		boolean altDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT)
			|| InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
		boolean rDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_R);
		boolean altRActive = altDown && rDown;

		if (altRActive && !altRWasActive) {
			if (screen instanceof LuipyUnifiedScreen unifiedScreen) {
				unifiedScreen.onClose();
			} else {
				LuipyUnifiedMenuOpener.tryOpen(client);
			}
		}

		altRWasActive = altRActive;
	}
}
