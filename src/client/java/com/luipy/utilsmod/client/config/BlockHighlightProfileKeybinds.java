package com.luipy.utilsmod.client.config;

import com.luipy.utilsmod.client.highlight.BlockHighlightManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Cycles the active block highlight profile in-game (default: {@code H}).
 */
public final class BlockHighlightProfileKeybinds {
	public static final KeyMapping CYCLE_PROFILE = new KeyMapping(
		"key.luipy-utils-mod.cycle_highlight_profile",
		GLFW.GLFW_KEY_H,
		LuipyConfigKeybinds.CATEGORY
	);

	private BlockHighlightProfileKeybinds() {
	}

	public static void register() {
		KeyBindingHelper.registerKeyBinding(CYCLE_PROFILE);
		ClientTickEvents.END_CLIENT_TICK.register(BlockHighlightProfileKeybinds::onClientTick);
	}

	private static void onClientTick(Minecraft client) {
		if (client.player == null || client.screen != null) {
			return;
		}
		while (CYCLE_PROFILE.consumeClick()) {
			BlockHighlightManager.cycleActiveProfile();
		}
	}
}
