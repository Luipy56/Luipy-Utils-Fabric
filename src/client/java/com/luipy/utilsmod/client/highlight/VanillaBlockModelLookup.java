package com.luipy.utilsmod.client.highlight;

import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves the vanilla {@link UnbakedModel} for a block state by reading its blockstate JSON directly.
 * This avoids circular loads through {@link BlockStateResolver}, which replaces blockstate resolution.
 */
final class VanillaBlockModelLookup {
	private static final Logger LOGGER = LoggerFactory.getLogger(VanillaBlockModelLookup.class);

	private VanillaBlockModelLookup() {
	}

	static UnbakedModel resolveBaseModel(BlockStateResolver.Context context, BlockState state) {
		Block block = state.getBlock();
		ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
		ResourceLocation blockstatePath = ModelBakery.BLOCKSTATE_LISTER.idToFile(blockId);

		ResourceManager resourceManager = net.minecraft.client.Minecraft.getInstance().getResourceManager();
		Resource resource = resourceManager.getResource(blockstatePath).orElse(null);
		if (resource == null) {
			LOGGER.warn("Missing blockstate {} for highlight base model; using missing model", blockstatePath);
			return context.getOrLoadModel(ModelBakery.MISSING_MODEL_LOCATION);
		}

		StateDefinition<Block, BlockState> definition = block.getStateDefinition();
		BlockModelDefinition.Context parseContext = new BlockModelDefinition.Context();
		parseContext.setDefinition(definition);

		BlockModelDefinition blockModelDefinition;
		try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
			blockModelDefinition = BlockModelDefinition.fromStream(parseContext, reader);
		} catch (IOException | JsonParseException exception) {
			LOGGER.warn("Failed to parse blockstate {} for highlight base model", blockstatePath, exception);
			return context.getOrLoadModel(ModelBakery.MISSING_MODEL_LOCATION);
		}

		if (blockModelDefinition.isMultiPart()) {
			return blockModelDefinition.getMultiPart();
		}

		String variant = BlockModelShaper.statePropertiesToString(state.getValues());
		if (!blockModelDefinition.hasVariant(variant) && blockModelDefinition.hasVariant("")) {
			variant = "";
		}
		if (!blockModelDefinition.hasVariant(variant)) {
			LOGGER.warn("No variant {} in blockstate {} for highlight base model", variant, blockstatePath);
			return context.getOrLoadModel(ModelBakery.MISSING_MODEL_LOCATION);
		}

		MultiVariant multiVariant = blockModelDefinition.getVariant(variant);
		if (multiVariant.getVariants().isEmpty()) {
			return context.getOrLoadModel(ModelBakery.MISSING_MODEL_LOCATION);
		}

		return multiVariant;
	}
}
