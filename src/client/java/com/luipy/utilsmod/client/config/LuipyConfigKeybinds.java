package com.luipy.utilsmod.client.config;

import com.luipy.utilsmod.client.config.ui.LuipyConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Opens {@link LuipyConfigScreen} when the player holds <strong>both</strong> {@code X} and {@code R}.
 * <p>
 * A {@link KeyMapping} is registered for discoverability in Controls (category: Luipy Utils), but the
 * actual open gesture requires both keys down at once — matching the requested {@code X+R} chord UX.
 * Rebinding via vanilla controls is not wired yet; see the Keybinds category stub in the config screen.
 */
public final class LuipyConfigKeybinds {
	public static final String CATEGORY = "key.categories.luipy-utils-mod";
	public static final KeyMapping OPEN_CONFIG = new KeyMapping(
		"key.luipy-utils-mod.open_config",
		InputConstants.UNKNOWN.getValue(),
		CATEGORY
	);

	private static boolean chordWasActive;

	private LuipyConfigKeybinds() {
	}

	public static void register() {
		net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(OPEN_CONFIG);
		ClientTickEvents.END_CLIENT_TICK.register(LuipyConfigKeybinds::onClientTick);
	}

	private static void onClientTick(Minecraft client) {
		if (client.player == null) {
			chordWasActive = false;
			return;
		}

		long window = client.getWindow().getWindow();
		boolean xDown = org.lwjgl.glfw.GLFW.glfwGetKey(window, GLFW.GLFW_KEY_X) == GLFW.GLFW_PRESS;
		boolean rDown = org.lwjgl.glfw.GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
		boolean chordActive = xDown && rDown;

		if (chordActive && !chordWasActive) {
			Screen current = client.screen;
			if (current == null || current.isPauseScreen()) {
				client.setScreen(LuipyConfigScreen.create(current));
			}
		}

		chordWasActive = chordActive;
	}

	public static Component openConfigKeyName() {
		return Component.translatable("key.luipy-utils-mod.open_config");
	}
}
