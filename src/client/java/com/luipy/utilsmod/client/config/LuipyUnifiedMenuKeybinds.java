package com.luipy.utilsmod.client.config;

import com.luipy.utilsmod.client.ender.LuipyUnifiedMenuOpener;
import com.luipy.utilsmod.client.inventory.LuipyUnifiedScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Default gesture: press <strong>R</strong> to toggle {@link com.luipy.utilsmod.inventory.LuipyUnifiedMenu}
 * (open when closed, close when {@link LuipyUnifiedScreen} is active).
 * Plain R is unbound in vanilla 1.20.1 default controls (unlike L for swap-offhand), so no Alt chord is needed.
 * A {@link KeyMapping} is registered for discoverability in Controls; detection uses edge-detect on the
 * bound key plus {@link KeyMapping#consumeClick()} as fallback so R works reliably after rebinding.
 */
public final class LuipyUnifiedMenuKeybinds {
	public static final KeyMapping OPEN_UNIFIED_MENU = new KeyMapping(
		"key.luipy-utils-mod.open_unified_menu",
		GLFW.GLFW_KEY_R,
		LuipyConfigKeybinds.CATEGORY
	);

	private static boolean keyWasDown;

	private LuipyUnifiedMenuKeybinds() {
	}

	public static void register() {
		KeyBindingHelper.registerKeyBinding(OPEN_UNIFIED_MENU);
		ClientTickEvents.END_CLIENT_TICK.register(LuipyUnifiedMenuKeybinds::onClientTick);
	}

	private static void onClientTick(Minecraft client) {
		if (client.player == null) {
			keyWasDown = false;
			return;
		}

		var screen = client.screen;
		if (screen != null && !(screen instanceof LuipyUnifiedScreen)) {
			keyWasDown = false;
			return;
		}

		boolean keyDown = OPEN_UNIFIED_MENU.isDown();
		boolean edge = keyDown && !keyWasDown;
		keyWasDown = keyDown;

		if (!edge && !OPEN_UNIFIED_MENU.consumeClick()) {
			return;
		}

		if (screen instanceof LuipyUnifiedScreen unifiedScreen) {
			unifiedScreen.onClose();
		} else {
			LuipyUnifiedMenuOpener.tryOpen(client);
		}
	}
}
