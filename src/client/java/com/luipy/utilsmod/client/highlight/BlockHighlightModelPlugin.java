package com.luipy.utilsmod.client.highlight;

import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Registers Fabric model overrides for configured highlight blocks during each client resource reload.
 */
public final class BlockHighlightModelPlugin {
	static final ResourceLocation EMPHASIS_MODEL = new ResourceLocation("luipy-utils-mod", "block/highlight/emphasis");

	private BlockHighlightModelPlugin() {
	}

	public static void register() {
		ModelLoadingPlugin.register(context -> {
			context.addModels(EMPHASIS_MODEL);
			if (!BlockHighlightManager.shouldApplyModelOverrides()) {
				return;
			}
			for (Block block : BlockHighlightManager.getTargetBlocks()) {
				context.registerBlockStateResolver(block, BlockHighlightModelPlugin::resolveHighlightStates);
			}
		});
	}

	private static void resolveHighlightStates(BlockStateResolver.Context context) {
		UnbakedModel emphasis = context.getOrLoadModel(EMPHASIS_MODEL);
		for (BlockState state : context.block().getStateDefinition().getPossibleStates()) {
			context.setModel(state, emphasis);
		}
	}
}
