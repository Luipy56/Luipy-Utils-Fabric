package com.luipy.utilsmod.client.highlight;

import com.luipy.utilsmod.LuipyUtilsMod;
import com.luipy.utilsmod.client.config.ui.LuipyConfigScreen;
import java.awt.FileDialog;
import java.awt.HeadlessException;
import java.io.File;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Opens a native file dialog on a background thread and copies the chosen PNG into the config folder.
 */
public final class HighlightTextureFileChooser {
	private HighlightTextureFileChooser() {
	}

	static {
		if (Boolean.getBoolean("java.awt.headless")) {
			System.setProperty("java.awt.headless", "false");
		}
	}

	public static void openAsync() {
		openAsync(com.luipy.utilsmod.config.LuipyUtilsConfigManager.get().activeBlockHighlightProfile);
	}

	public static void openAsync(int profileIndex) {
		Thread thread = new Thread(() -> runDialog(profileIndex), "luipy-highlight-texture-chooser");
		thread.setDaemon(true);
		thread.start();
	}

	private static void runDialog(int profileIndex) {
		Path selected = null;
		boolean dialogFailed = false;
		try {
			selected = pickWithSwing();
		} catch (Exception e) {
			LuipyUtilsMod.LOGGER.warn("Swing file chooser failed for highlight texture", e);
			dialogFailed = true;
		}
		if (selected == null) {
			try {
				selected = pickWithAwt();
			} catch (HeadlessException e) {
				LuipyUtilsMod.LOGGER.warn("AWT file dialog unavailable (headless)", e);
				dialogFailed = true;
			} catch (Exception e) {
				LuipyUtilsMod.LOGGER.warn("AWT file dialog failed for highlight texture", e);
				dialogFailed = true;
			}
		}
		Minecraft client = Minecraft.getInstance();
		if (selected == null) {
			boolean failed = dialogFailed;
			client.execute(() -> {
				if (failed) {
					notify(client, "luipy-utils-mod.config.block_highlight.texture_invalid");
				} else {
					notify(client, "luipy-utils-mod.config.block_highlight.texture_pick_cancelled");
				}
			});
			return;
		}
		Path path = selected;
		client.execute(() -> applyOnClientThread(path, profileIndex));
	}

	private static Path pickWithSwing() throws Exception {
		final Path[] result = new Path[1];
		SwingUtilities.invokeAndWait(() -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Choose highlight PNG");
			chooser.setFileFilter(new FileNameExtensionFilter("PNG images (*.png)", "png"));
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (file != null) {
					result[0] = file.toPath();
				}
			}
		});
		return result[0];
	}

	private static Path pickWithAwt() {
		FileDialog dialog = new FileDialog((java.awt.Frame) null, "Choose highlight PNG", FileDialog.LOAD);
		dialog.setFile("*.png");
		dialog.setVisible(true);
		String fileName = dialog.getFile();
		if (fileName == null) {
			return null;
		}
		String directory = dialog.getDirectory();
		if (directory == null) {
			return null;
		}
		return Path.of(directory, fileName);
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
		Component message = Component.translatable(key);
		if (client.player != null) {
			client.player.displayClientMessage(message, false);
		} else if (client.gui != null) {
			client.gui.getChat().addMessage(message);
		} else {
			LuipyUtilsMod.LOGGER.info("Highlight texture chooser: {}", key);
		}
	}
}
