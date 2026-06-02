package com.luipy.utilsmod.client.highlight;

import com.luipy.utilsmod.LuipyUtilsMod;
import com.luipy.utilsmod.client.config.ui.LuipyConfigScreen;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import java.awt.FileDialog;
import java.io.File;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Opens a native file dialog on a background thread and copies the chosen PNG into the config folder.
 */
public final class HighlightTextureFileChooser {
	private HighlightTextureFileChooser() {
	}

	public static void openAsync() {
		openAsync(LuipyUtilsConfigManager.get().activeBlockHighlightProfile);
	}

	public static void openAsync(int profileIndex) {
		Thread thread = new Thread(() -> runDialog(profileIndex), "luipy-highlight-texture-chooser");
		thread.setDaemon(true);
		thread.start();
	}

	private static void runDialog(int profileIndex) {
		FileDialog dialog = new FileDialog((java.awt.Frame) null, "Choose highlight PNG", FileDialog.LOAD);
		dialog.setFile("*.png");
		dialog.setVisible(true);
		String fileName = dialog.getFile();
		if (fileName == null) {
			return;
		}
		String directory = dialog.getDirectory();
		if (directory == null) {
			return;
		}
		Path source = Path.of(directory, fileName);
		Minecraft.getInstance().execute(() -> applyOnClientThread(source, profileIndex));
	}

	private static void applyOnClientThread(Path source, int profileIndex) {
		Minecraft client = Minecraft.getInstance();
		if (!source.toString().toLowerCase().endsWith(".png")) {
			notify(client, "luipy-utils-mod.config.block_highlight.texture_invalid");
			return;
		}
		try {
			HighlightEmphasisTextures.applyUserFile(source, profileIndex);
			if (client.screen instanceof LuipyConfigScreen screen) {
				screen.onHighlightTextureChanged();
			}
		} catch (Exception e) {
			LuipyUtilsMod.LOGGER.warn("Failed to apply custom highlight texture from {}", source, e);
			notify(client, "luipy-utils-mod.config.block_highlight.texture_invalid");
		}
	}

	private static void notify(Minecraft client, String key) {
		if (client.player != null) {
			client.player.displayClientMessage(Component.translatable(key), false);
		}
	}
}
