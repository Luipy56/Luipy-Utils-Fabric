package com.luipy.utilsmod.client.highlight;

import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/**
 * Parses configured block ids and drives client resource reloads so highlighted blocks use an emphasis model.
 *
 * <p>The previous implementation scanned every loaded chunk section, cached every matching {@link net.minecraft.core.BlockPos}
 * in a {@code HashSet}, and drew wireframe boxes each frame. That collapsed FPS for common blocks (gravel, clay, etc.)
 * and the outlines were not reliably visible on 1.20.1. Highlighting now scales with the number of configured block
 * <em>types</em> only: {@link BlockHighlightModelPlugin} registers Fabric block-state resolvers at reload time that
 * composite each matching block's vanilla model with a shared emphasis frame overlay shipped in mod assets.
 * Toggling off or clearing the list triggers
 * {@link Minecraft#reloadResourcePacks()} so vanilla models are restored without restarting.
 */
public final class BlockHighlightManager {
	private static Set<Block> targetBlocks = Set.of();
	private static boolean suppressResourceRefresh;

	private BlockHighlightManager() {
	}

	public static void register() {
		suppressResourceRefresh = true;
		reloadFromConfig();
		suppressResourceRefresh = false;
	}

	public static boolean shouldApplyModelOverrides() {
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		cfg.ensureProfilesInitialized();
		LuipyUtilsConfig.HighlightProfile active = cfg.activeHighlightProfile();
		return cfg.masterEnabled && cfg.blockHighlightEnabled && active.enabled && !targetBlocks.isEmpty();
	}

	public static Set<Block> getTargetBlocks() {
		return targetBlocks;
	}

	public static void reloadFromConfig() {
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		cfg.ensureProfilesInitialized();
		ParseResult result = parseBlockIds(cfg.activeHighlightProfile().blockIds);
		targetBlocks = result.blocks();
		if (!suppressResourceRefresh) {
			refreshClientResources();
		}
	}

	/**
	 * Re-parses the active profile block list, persists config, reloads client block models, and reports in chat.
	 */
	public static void applyActiveProfileFromConfig() {
		LuipyUtilsConfig cfg = LuipyUtilsConfigManager.get();
		cfg.ensureProfilesInitialized();
		LuipyUtilsConfig.HighlightProfile active = cfg.activeHighlightProfile();
		ParseResult result = parseBlockIds(active.blockIds);
		targetBlocks = result.blocks();
		LuipyUtilsConfigManager.save();
		refreshClientResources();
		notifyApplyResult(result);
	}

	private static void notifyApplyResult(ParseResult result) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}

		Component message;
		if (result.unknownIds().isEmpty()) {
			message = Component.translatable(
				"luipy-utils-mod.config.block_highlight.apply_success",
				result.blocks().size()
			);
		} else {
			message = Component.translatable(
				"luipy-utils-mod.config.block_highlight.apply_partial",
				result.blocks().size(),
				result.unknownIds().size(),
				String.join(", ", result.unknownIds())
			);
		}
		client.player.displayClientMessage(message, false);
	}

	static void refreshClientResources() {
		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			client.execute(client::reloadResourcePacks);
		}
	}

	static ParseResult parseBlockIds(String raw) {
		if (raw == null || raw.isBlank()) {
			return new ParseResult(Set.of(), List.of());
		}

		Set<Block> blocks = new HashSet<>();
		List<String> unknown = new ArrayList<>();
		String[] tokens = raw.split(", ");
		for (String token : tokens) {
			String trimmed = token.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			ResourceLocation id = resolveId(trimmed);
			if (id == null) {
				unknown.add(trimmed);
				continue;
			}
			if (!BuiltInRegistries.BLOCK.containsKey(id)) {
				unknown.add(trimmed);
				continue;
			}
			Block block = BuiltInRegistries.BLOCK.get(id);
			if (block != null && !block.defaultBlockState().isAir()) {
				blocks.add(block);
			}
		}
		return new ParseResult(Collections.unmodifiableSet(blocks), List.copyOf(unknown));
	}

	private static ResourceLocation resolveId(String token) {
		if (token.contains(":")) {
			return ResourceLocation.tryParse(token);
		}
		return ResourceLocation.tryParse(ResourceLocation.DEFAULT_NAMESPACE + ":" + token);
	}

	record ParseResult(Set<Block> blocks, List<String> unknownIds) {
	}
}
