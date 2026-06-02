package com.luipy.utilsmod.client.highlight;

import com.luipy.utilsmod.LuipyUtilsMod;
import com.luipy.utilsmod.client.config.ui.LuipyConfigScreen;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Opens a native file dialog on a background thread and copies the chosen PNG into the config folder.
 */
public final class HighlightTextureFileChooser {
	static {
		System.setProperty("java.awt.headless", "false");
	}

	private HighlightTextureFileChooser() {
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
		DialogResult result = pickFile();
		Minecraft client = Minecraft.getInstance();
		if (result.path() == null) {
			boolean failed = result.dialogFailed();
			client.execute(() -> {
				if (failed) {
					notify(client, "luipy-utils-mod.config.block_highlight.texture_dialog_failed");
				} else {
					notify(client, "luipy-utils-mod.config.block_highlight.texture_pick_cancelled");
				}
			});
			return;
		}
		Path path = result.path();
		client.execute(() -> applyOnClientThread(path, profileIndex));
	}

	private record DialogResult(Path path, boolean dialogFailed) {
	}

	private static DialogResult pickFile() {
		try {
			Path swing = pickWithSwing();
			if (swing != null) {
				return new DialogResult(swing, false);
			}
		} catch (Exception e) {
			LuipyUtilsMod.LOGGER.warn("Swing file chooser failed for highlight texture", e);
		}

		try {
			Path awt = pickWithAwt();
			if (awt != null) {
				return new DialogResult(awt, false);
			}
		} catch (HeadlessException e) {
			LuipyUtilsMod.LOGGER.warn("AWT file dialog unavailable (headless)", e);
		} catch (Exception e) {
			LuipyUtilsMod.LOGGER.warn("AWT file dialog failed for highlight texture", e);
		}

		try {
			Path cli = pickWithDesktopCli();
			if (cli != null) {
				return new DialogResult(cli, false);
			}
		} catch (Exception e) {
			LuipyUtilsMod.LOGGER.warn("Desktop CLI file picker failed for highlight texture", e);
		}

		return new DialogResult(null, true);
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
		Frame owner = new Frame();
		owner.setUndecorated(true);
		owner.setSize(0, 0);
		owner.setLocationRelativeTo(null);
		owner.setVisible(true);
		try {
			FileDialog dialog = new FileDialog(owner, "Choose highlight PNG", FileDialog.LOAD);
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
		} finally {
			owner.dispose();
		}
	}

	private static Path pickWithDesktopCli() throws Exception {
		String os = System.getProperty("os.name", "").toLowerCase();
		if (os.contains("linux")) {
			Path zenity = pickWithCommand(new String[] {
				"zenity",
				"--file-selection",
				"--title=Choose highlight PNG",
				"--file-filter=PNG files (*.png) | *.png"
			});
			if (zenity != null) {
				return zenity;
			}
			return pickWithCommand(new String[] {
				"kdialog",
				"--getopenfilename",
				System.getProperty("user.home"),
				"PNG images (*.png)"
			});
		}
		if (os.contains("mac")) {
			return pickWithCommand(new String[] {
				"osascript",
				"-e",
				"POSIX path of (choose file of type {\"png\"} with prompt \"Choose highlight PNG\")"
			});
		}
		return null;
	}

	private static Path pickWithCommand(String[] command) throws Exception {
		Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
		boolean finished = process.waitFor(120, TimeUnit.SECONDS);
		if (!finished) {
			process.destroyForcibly();
			return null;
		}
		if (process.exitValue() != 0) {
			return null;
		}
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
		)) {
			String line = reader.readLine();
			if (line == null || line.isBlank()) {
				return null;
			}
			Path path = Path.of(line.trim());
			return Files.isRegularFile(path) ? path : null;
		}
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
