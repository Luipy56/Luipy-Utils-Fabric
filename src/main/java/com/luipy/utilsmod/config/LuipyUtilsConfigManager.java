package com.luipy.utilsmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luipy.utilsmod.LuipyUtilsMod;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class LuipyUtilsConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static LuipyUtilsConfig instance = new LuipyUtilsConfig();

	public static LuipyUtilsConfig get() {
		return instance;
	}

	public static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(LuipyUtilsMod.MOD_ID + ".json");
	}

	public static void load() {
		Path path = configPath();
		if (!Files.isRegularFile(path)) {
			instance = new LuipyUtilsConfig();
			instance.ensureProfilesInitialized();
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			LuipyUtilsConfig read = GSON.fromJson(reader, LuipyUtilsConfig.class);
			instance = read != null ? read : new LuipyUtilsConfig();
			instance.ensureProfilesInitialized();
			instance.migrateLegacyBlockHighlightIds();
		} catch (Exception e) {
			LuipyUtilsMod.LOGGER.warn("Failed to load config, using defaults", e);
			instance = new LuipyUtilsConfig();
			instance.ensureProfilesInitialized();
		}
	}

	public static void save() {
		Path path = configPath();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			LuipyUtilsMod.LOGGER.warn("Failed to save config", e);
		}
	}

	public static void replace(LuipyUtilsConfig next) {
		instance = next != null ? next : new LuipyUtilsConfig();
	}
}
