package com.luipy.utilsmod.client.highlight;

import com.luipy.utilsmod.LuipyUtilsMod;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import com.luipy.utilsmod.client.highlight.pack.HighlightCustomTexturePack;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Persists and loads optional user highlight frame PNGs (per profile with global fallback) from the mod config directory.
 */
public final class HighlightEmphasisTextures {
	public static final ResourceLocation BUNDLED_TEXTURE = LuipyUtilsMod.id("textures/block/highlight_emphasis.png");
	public static final ResourceLocation PREVIEW_TEXTURE = LuipyUtilsMod.id("dynamic/highlight_preview");
	private static final int RECOMMENDED_SIZE = 16;

	private static DynamicTexture previewTexture;
	private static final DynamicTexture[] profilePreviewTextures = new DynamicTexture[LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT];
	private static final ResourceLocation[] PROFILE_PREVIEW_TEXTURES = {
		LuipyUtilsMod.id("dynamic/highlight_preview_0"),
		LuipyUtilsMod.id("dynamic/highlight_preview_1"),
		LuipyUtilsMod.id("dynamic/highlight_preview_2"),
	};

	private HighlightEmphasisTextures() {
	}

	public static Path globalCustomFilePath() {
		return FabricLoader.getInstance().getConfigDir().resolve(LuipyUtilsMod.MOD_ID).resolve("highlight_emphasis.png");
	}

	public static Path profileCustomFilePath(int profileIndex) {
		return FabricLoader.getInstance().getConfigDir()
			.resolve(LuipyUtilsMod.MOD_ID)
			.resolve("highlight_profile_" + profileIndex + ".png");
	}

	/** @deprecated use {@link #globalCustomFilePath()} */
	@Deprecated
	public static Path customFilePath() {
		return globalCustomFilePath();
	}

	public static boolean isCustomActive() {
		return resolveActiveTexturePath().isPresent();
	}

	public static Optional<Path> resolveActiveTexturePath() {
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		cfg.ensureProfilesInitialized();
		int activeIndex = Math.floorMod(cfg.activeBlockHighlightProfile, LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT);
		LuipyUtilsConfig.HighlightProfile active = cfg.blockHighlightProfiles[activeIndex];
		Path profilePath = profileCustomFilePath(activeIndex);
		if (active.useCustomTexture && Files.isRegularFile(profilePath)) {
			return Optional.of(profilePath);
		}
		Path globalPath = globalCustomFilePath();
		if (cfg.blockHighlightUseCustomTexture && Files.isRegularFile(globalPath)) {
			return Optional.of(globalPath);
		}
		return Optional.empty();
	}

	public static HighlightCustomTexturePack createPack() {
		Path path = resolveActiveTexturePath().orElse(globalCustomFilePath());
		return new HighlightCustomTexturePack(path);
	}

	public static void refreshPreviewTexture() {
		releasePreviewTexture();
		Optional<NativeImage> image = readPreviewImageForActiveProfile();
		if (image.isEmpty()) {
			return;
		}
		previewTexture = new DynamicTexture(image.get());
		Minecraft.getInstance().getTextureManager().register(PREVIEW_TEXTURE, previewTexture);
	}

	public static void refreshAllProfilePreviewTextures() {
		for (int i = 0; i < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; i++) {
			refreshProfilePreviewTexture(i);
		}
	}

	public static void refreshProfilePreviewTexture(int profileIndex) {
		releaseProfilePreviewTexture(profileIndex);
		Optional<NativeImage> image = readPreviewImageForProfile(profileIndex);
		if (image.isEmpty()) {
			return;
		}
		profilePreviewTextures[profileIndex] = new DynamicTexture(image.get());
		Minecraft.getInstance().getTextureManager().register(PROFILE_PREVIEW_TEXTURES[profileIndex], profilePreviewTextures[profileIndex]);
	}

	public static ResourceLocation previewTextureId() {
		return previewTexture != null ? PREVIEW_TEXTURE : BUNDLED_TEXTURE;
	}

	public static ResourceLocation profilePreviewTextureId(int profileIndex) {
		return profilePreviewTextures[profileIndex] != null
			? PROFILE_PREVIEW_TEXTURES[profileIndex]
			: BUNDLED_TEXTURE;
	}

	public static boolean previewUsesDynamicTexture() {
		return previewTexture != null;
	}

	public static boolean profilePreviewUsesDynamicTexture(int profileIndex) {
		return profilePreviewTextures[profileIndex] != null;
	}

	public static @Nullable DynamicTexture previewTexture() {
		return previewTexture;
	}

	public static @Nullable DynamicTexture profilePreviewTexture(int profileIndex) {
		return profilePreviewTextures[profileIndex];
	}

	public static void releasePreviewTexture() {
		if (previewTexture == null) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			client.getTextureManager().release(PREVIEW_TEXTURE);
		}
		previewTexture.close();
		previewTexture = null;
	}

	public static void releaseProfilePreviewTexture(int profileIndex) {
		if (profilePreviewTextures[profileIndex] == null) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			client.getTextureManager().release(PROFILE_PREVIEW_TEXTURES[profileIndex]);
		}
		profilePreviewTextures[profileIndex].close();
		profilePreviewTextures[profileIndex] = null;
	}

	public static void releaseAllProfilePreviewTextures() {
		for (int i = 0; i < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; i++) {
			releaseProfilePreviewTexture(i);
		}
	}

	public static Optional<NativeImage> readPreviewImageForActiveProfile() {
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		cfg.ensureProfilesInitialized();
		int activeIndex = Math.floorMod(cfg.activeBlockHighlightProfile, LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT);
		return readPreviewImageForProfile(activeIndex);
	}

	public static Optional<NativeImage> readPreviewImageForProfile(int profileIndex) {
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		cfg.ensureProfilesInitialized();
		LuipyUtilsConfig.HighlightProfile profile = cfg.blockHighlightProfiles[profileIndex];
		Path profilePath = profileCustomFilePath(profileIndex);
		if (profile.useCustomTexture && Files.isRegularFile(profilePath)) {
			try {
				return Optional.of(NativeImage.read(Files.newInputStream(profilePath)));
			} catch (IOException e) {
				LuipyUtilsMod.LOGGER.warn("Failed to read profile highlight preview texture {}", profilePath, e);
			}
		}
		Path globalPath = globalCustomFilePath();
		if (cfg.blockHighlightUseCustomTexture && Files.isRegularFile(globalPath)) {
			try {
				return Optional.of(NativeImage.read(Files.newInputStream(globalPath)));
			} catch (IOException e) {
				LuipyUtilsMod.LOGGER.warn("Failed to read global highlight preview texture {}", globalPath, e);
			}
		}
		return readBundledImage();
	}

	public static Optional<NativeImage> readPreviewImage() {
		return readPreviewImageForActiveProfile();
	}

	private static Optional<NativeImage> readBundledImage() {
		try {
			Minecraft client = Minecraft.getInstance();
			if (client == null) {
				return Optional.empty();
			}
			try (InputStream in = client.getResourceManager().getResource(BUNDLED_TEXTURE).get().open()) {
				return Optional.of(NativeImage.read(in));
			}
		} catch (IOException e) {
			LuipyUtilsMod.LOGGER.warn("Failed to read bundled highlight preview texture", e);
			return Optional.empty();
		}
	}

	public static void applyUserFile(Path source) {
		try {
			applyUserFile(source, LuipyUtilsConfigManager.get().activeBlockHighlightProfile);
		} catch (IOException e) {
			LuipyUtilsMod.LOGGER.warn("Failed to apply custom highlight texture from {}", source, e);
		}
	}

	public static void applyUserFile(Path source, int profileIndex) throws IOException {
		Files.createDirectories(profileCustomFilePath(profileIndex).getParent());
		NativeImage image;
		try (InputStream in = Files.newInputStream(source)) {
			image = NativeImage.read(in);
		}
		try {
			int width = image.getWidth();
			int height = image.getHeight();
			if (width <= 0 || height <= 0) {
				throw new IOException("invalid dimensions");
			}
			Files.copy(source, profileCustomFilePath(profileIndex), StandardCopyOption.REPLACE_EXISTING);
			LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
			cfg.ensureProfilesInitialized();
			cfg.blockHighlightProfiles[profileIndex].useCustomTexture = true;
			LuipyUtilsConfigManager.save();
			refreshProfilePreviewTexture(profileIndex);
			refreshPreviewTexture();
			BlockHighlightManager.refreshClientResources();
			notifyPlayer(
				width != RECOMMENDED_SIZE || height != RECOMMENDED_SIZE
					? "luipy-utils-mod.config.block_highlight.texture_applied_warn_size"
					: "luipy-utils-mod.config.block_highlight.texture_applied"
			);
		} finally {
			image.close();
		}
	}

	public static void resetToDefault() {
		resetProfileToDefault(LuipyUtilsConfigManager.get().activeBlockHighlightProfile);
	}

	public static void resetProfileToDefault(int profileIndex) {
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		cfg.ensureProfilesInitialized();
		cfg.blockHighlightProfiles[profileIndex].useCustomTexture = false;
		LuipyUtilsConfigManager.save();
		try {
			Files.deleteIfExists(profileCustomFilePath(profileIndex));
		} catch (IOException e) {
			LuipyUtilsMod.LOGGER.warn("Failed to delete profile highlight texture {}", profileIndex, e);
		}
		refreshProfilePreviewTexture(profileIndex);
		refreshPreviewTexture();
		BlockHighlightManager.refreshClientResources();
		notifyPlayer("luipy-utils-mod.config.block_highlight.texture_reset_success");
	}

	public static void resetGlobalToDefault() {
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		cfg.blockHighlightUseCustomTexture = false;
		LuipyUtilsConfigManager.save();
		try {
			Files.deleteIfExists(globalCustomFilePath());
		} catch (IOException e) {
			LuipyUtilsMod.LOGGER.warn("Failed to delete global custom highlight texture", e);
		}
		releasePreviewTexture();
		releaseAllProfilePreviewTextures();
		refreshAllProfilePreviewTextures();
		BlockHighlightManager.refreshClientResources();
	}

	public static void syncCustomFlagFromDisk() {
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		cfg.ensureProfilesInitialized();
		for (int i = 0; i < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; i++) {
			if (Files.isRegularFile(profileCustomFilePath(i)) && !cfg.blockHighlightProfiles[i].useCustomTexture) {
				cfg.blockHighlightProfiles[i].useCustomTexture = true;
			}
		}
		if (Files.isRegularFile(globalCustomFilePath()) && !cfg.blockHighlightUseCustomTexture) {
			cfg.blockHighlightUseCustomTexture = true;
		}
		LuipyUtilsConfigManager.save();
	}

	public static void onConfigScreenOpened() {
		syncCustomFlagFromDisk();
		refreshAllProfilePreviewTextures();
		refreshPreviewTexture();
	}

	public static void onConfigScreenClosed() {
		releasePreviewTexture();
		releaseAllProfilePreviewTextures();
	}

	private static void notifyPlayer(String messageKey) {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null) {
			client.player.displayClientMessage(Component.translatable(messageKey), false);
		}
	}
}
