package com.luipy.utilsmod.client.config.ui;

import com.luipy.utilsmod.client.highlight.BlockHighlightManager;
import com.luipy.utilsmod.client.highlight.HighlightEmphasisTextures;
import com.luipy.utilsmod.client.highlight.HighlightTextureFileChooser;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * First-party in-game config screen (masa-style category sidebar + scrolling option rows).
 * Open with {@link com.luipy.utilsmod.client.config.LuipyConfigKeybinds} (default: hold X + R).
 */
public class LuipyConfigScreen extends Screen {
	private static final int SIDEBAR_WIDTH = 130;
	private static final int CONTENT_PADDING = 10;
	private static final int ROW_HEIGHT = 40;
	private static final int ICON_SIZE = 16;
	private static final int ICON_TEXT_GAP = 22;
	private static final int FOOTER_HEIGHT = 36;
	private static final int TOGGLE_WIDTH = 80;
	private static final int TEXTURE_PREVIEW_SIZE = 48;
	private static final int PROFILE_NAME_HEIGHT = 24;
	private static final int PROFILE_CONTROLS_HEIGHT = 28;
	private static final int PROFILE_IDS_HEIGHT = 44;
	private static final int PROFILE_TEXTURE_HEIGHT = 78;
	private static final int PROFILE_SECTION_HEIGHT =
		PROFILE_NAME_HEIGHT + PROFILE_CONTROLS_HEIGHT + PROFILE_IDS_HEIGHT + PROFILE_TEXTURE_HEIGHT + 8;

	private final Screen parent;
	private final LuipyUtilsConfig config;
	private LuipyConfigCategory selectedCategory = LuipyConfigCategory.GENERAL;
	private double scrollOffset;
	private int contentTop;
	private int contentBottom;
	private int contentLeft;
	private int contentRight;
	private int maxScroll;
	private final List<CycleButton<Boolean>> toggleButtons = new ArrayList<>();
	private final EditBox[] profileNameFields = new EditBox[LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT];
	private final EditBox[] profileBlockIdFields = new EditBox[LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT];
	private final String[] initialProfileBlockIds = new String[LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT];
	private boolean visitedWorldTab;

	public LuipyConfigScreen(Screen parent) {
		super(Component.translatable("luipy-utils-mod.config.title"));
		this.parent = parent;
		this.config = LuipyUtilsConfigManager.get();
		this.config.ensureProfilesInitialized();
		for (int i = 0; i < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; i++) {
			this.initialProfileBlockIds[i] = normalizeBlockHighlightIds(this.config.blockHighlightProfiles[i].blockIds);
		}
		HighlightEmphasisTextures.onConfigScreenOpened();
	}

	public static Screen create(Screen parent) {
		return new LuipyConfigScreen(parent);
	}

	/** Called after async texture upload so the World tab preview can refresh. */
	public void onHighlightTextureChanged() {
		HighlightEmphasisTextures.onConfigScreenOpened();
		this.init();
	}

	@Override
	protected void init() {
		this.toggleButtons.clear();
		this.clearWidgets();
		Arrays.fill(this.profileNameFields, null);
		Arrays.fill(this.profileBlockIdFields, null);
		this.computeContentBounds();
		this.rebuildSidebar();
		this.rebuildContentWidgets();
		this.rebuildFooter();
		this.clampScroll();
	}

	private void computeContentBounds() {
		this.contentLeft = SIDEBAR_WIDTH + CONTENT_PADDING;
		this.contentRight = this.width - CONTENT_PADDING;
		this.contentTop = CONTENT_PADDING + 8;
		this.contentBottom = this.height - FOOTER_HEIGHT - CONTENT_PADDING;
	}

	private void rebuildSidebar() {
		int y = CONTENT_PADDING + 8;
		for (LuipyConfigCategory category : LuipyConfigCategory.values()) {
			LuipyConfigCategory cat = category;
			this.addRenderableWidget(
				Button.builder(category.title(), btn -> this.selectCategory(cat))
					.bounds(CONTENT_PADDING, y, SIDEBAR_WIDTH - CONTENT_PADDING * 2, 20)
					.build()
			);
			y += 24;
		}
	}

	private void selectCategory(LuipyConfigCategory category) {
		if (this.selectedCategory == LuipyConfigCategory.WORLD && category != LuipyConfigCategory.WORLD) {
			HighlightEmphasisTextures.onConfigScreenClosed();
		}
		if (category == LuipyConfigCategory.WORLD) {
			this.visitedWorldTab = true;
			HighlightEmphasisTextures.onConfigScreenOpened();
		}
		this.selectedCategory = category;
		this.scrollOffset = 0;
		this.init();
	}

	private void rebuildContentWidgets() {
		if (this.selectedCategory == LuipyConfigCategory.KEYBINDS) {
			return;
		}
		if (this.selectedCategory == LuipyConfigCategory.WORLD) {
			this.rebuildWorldWidgets();
			return;
		}

		List<LuipyConfigBooleanEntry> entries = LuipyConfigCategories.forCategory(this.selectedCategory);
		int toggleX = this.contentRight - TOGGLE_WIDTH;
		int y = this.contentTop - (int) this.scrollOffset;

		for (LuipyConfigBooleanEntry entry : entries) {
			if (y + ROW_HEIGHT < this.contentTop || y > this.contentBottom) {
				y += ROW_HEIGHT;
				continue;
			}

			boolean current = entry.getter().apply(this.config);
			CycleButton<Boolean> toggle = CycleButton.onOffBuilder(current)
				.displayOnlyValue()
				.create(toggleX, y + 8, TOGGLE_WIDTH, 20, entry.label(), (btn, value) -> entry.setter().accept(this.config, value));
			this.toggleButtons.add(toggle);
			this.addRenderableWidget(toggle);
			y += ROW_HEIGHT;
		}

		this.maxScroll = Math.max(0, entries.size() * ROW_HEIGHT - (this.contentBottom - this.contentTop));
	}

	private void rebuildWorldWidgets() {
		List<LuipyConfigBooleanEntry> entries = LuipyConfigCategories.forCategory(LuipyConfigCategory.WORLD);
		int toggleX = this.contentRight - TOGGLE_WIDTH;
		int y = this.contentTop - (int) this.scrollOffset;

		for (LuipyConfigBooleanEntry entry : entries) {
			if (y + ROW_HEIGHT >= this.contentTop && y <= this.contentBottom) {
				boolean current = entry.getter().apply(this.config);
				CycleButton<Boolean> toggle = CycleButton.onOffBuilder(current)
					.displayOnlyValue()
					.create(toggleX, y + 8, TOGGLE_WIDTH, 20, entry.label(), (btn, value) -> entry.setter().accept(this.config, value));
				this.toggleButtons.add(toggle);
				this.addRenderableWidget(toggle);
			}
			y += ROW_HEIGHT;
		}

		int profileStartY = this.contentTop + ROW_HEIGHT - (int) this.scrollOffset;
		int fieldWidth = this.contentRight - this.contentLeft - 8;
		int halfWidth = Math.max(80, (fieldWidth - 8) / 2);

		for (int profileIndex = 0; profileIndex < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; profileIndex++) {
			LuipyUtilsConfig.HighlightProfile profile = this.config.blockHighlightProfiles[profileIndex];
			int sectionY = profileStartY + profileIndex * PROFILE_SECTION_HEIGHT;

			int nameY = sectionY;
			if (nameY + PROFILE_NAME_HEIGHT >= this.contentTop && nameY <= this.contentBottom) {
				EditBox nameField = new EditBox(
					this.font,
					this.contentLeft,
					nameY,
					fieldWidth,
					20,
					Component.translatable("luipy-utils-mod.config.block_highlight.profile_name")
				);
				nameField.setMaxLength(32);
				nameField.setValue(profile.name != null ? profile.name : "");
				this.profileNameFields[profileIndex] = nameField;
				this.addRenderableWidget(nameField);
			}

			int controlsY = sectionY + PROFILE_NAME_HEIGHT;
			if (controlsY + PROFILE_CONTROLS_HEIGHT >= this.contentTop && controlsY <= this.contentBottom) {
				final int idx = profileIndex;
				boolean isActive = this.config.activeBlockHighlightProfile == profileIndex;
				this.addRenderableWidget(
					Button.builder(
						isActive
							? Component.translatable("luipy-utils-mod.config.block_highlight.profile_active")
							: Component.translatable("luipy-utils-mod.config.block_highlight.profile_select"),
						btn -> this.selectActiveProfile(idx)
					).bounds(this.contentLeft, controlsY, halfWidth, 20).build()
				);
				CycleButton<Boolean> enabledToggle = CycleButton.onOffBuilder(profile.enabled)
					.displayOnlyValue()
					.create(
						this.contentRight - TOGGLE_WIDTH,
						controlsY,
						TOGGLE_WIDTH,
						20,
						Component.translatable("luipy-utils-mod.config.block_highlight.profile_enabled"),
						(btn, value) -> this.config.blockHighlightProfiles[idx].enabled = value
					);
				this.toggleButtons.add(enabledToggle);
				this.addRenderableWidget(enabledToggle);
			}

			int idsY = sectionY + PROFILE_NAME_HEIGHT + PROFILE_CONTROLS_HEIGHT + 12;
			if (idsY + 20 >= this.contentTop && idsY <= this.contentBottom) {
				EditBox idsField = new EditBox(
					this.font,
					this.contentLeft,
					idsY,
					fieldWidth,
					20,
					Component.translatable("luipy-utils-mod.config.block_highlight_ids")
				);
				idsField.setMaxLength(2048);
				idsField.setValue(profile.blockIds != null ? profile.blockIds : "");
				idsField.setHint(Component.translatable("luipy-utils-mod.config.block_highlight_ids.hint"));
				this.profileBlockIdFields[profileIndex] = idsField;
				this.addRenderableWidget(idsField);
			}

			int buttonY = sectionY + PROFILE_NAME_HEIGHT + PROFILE_CONTROLS_HEIGHT + PROFILE_IDS_HEIGHT + TEXTURE_PREVIEW_SIZE + 6;
			if (buttonY + 20 >= this.contentTop && buttonY <= this.contentBottom) {
				final int idx = profileIndex;
				this.addRenderableWidget(
					Button.builder(Component.translatable("luipy-utils-mod.config.block_highlight.texture_choose"), btn ->
						HighlightTextureFileChooser.openAsync(idx)
					).bounds(this.contentLeft, buttonY, halfWidth, 20).build()
				);
				this.addRenderableWidget(
					Button.builder(Component.translatable("luipy-utils-mod.config.block_highlight.texture_reset"), btn -> {
						HighlightEmphasisTextures.resetProfileToDefault(idx);
						this.init();
					}).bounds(this.contentLeft + halfWidth + 8, buttonY, halfWidth, 20).build()
				);
			}
		}

		int contentHeight = this.contentBottom - this.contentTop;
		int totalHeight = ROW_HEIGHT + LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT * PROFILE_SECTION_HEIGHT;
		this.maxScroll = Math.max(0, totalHeight - contentHeight);
	}

	private void selectActiveProfile(int profileIndex) {
		this.config.activeBlockHighlightProfile = profileIndex;
		BlockHighlightManager.reloadFromConfig();
		this.init();
	}

	private void rebuildFooter() {
		int buttonWidth = 120;
		int gap = 8;
		int totalWidth = buttonWidth * 2 + gap;
		int startX = (this.width - totalWidth) / 2;
		int y = this.height - FOOTER_HEIGHT + 6;

		this.addRenderableWidget(
			Button.builder(Component.translatable("luipy-utils-mod.config.reset_category"), btn -> this.resetCurrentCategory())
				.bounds(startX, y, buttonWidth, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(Component.translatable("gui.done"), btn -> this.saveAndClose())
				.bounds(startX + buttonWidth + gap, y, buttonWidth, 20)
				.build()
		);
	}

	private void resetCurrentCategory() {
		if (this.selectedCategory == LuipyConfigCategory.KEYBINDS) {
			return;
		}
		LuipyConfigCategories.resetCategoryDefaults(this.config, this.selectedCategory);
		BlockHighlightManager.reloadFromConfig();
		this.init();
	}

	private void saveAndClose() {
		HighlightEmphasisTextures.onConfigScreenClosed();
		if (this.visitedWorldTab) {
			syncWorldTabToConfig();
			boolean blockIdsChanged = false;
			for (int i = 0; i < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; i++) {
				String current = this.config.blockHighlightProfiles[i].blockIds;
				if (!normalizeBlockHighlightIds(current).equals(this.initialProfileBlockIds[i])) {
					blockIdsChanged = true;
					break;
				}
			}
			if (blockIdsChanged) {
				BlockHighlightManager.applyActiveProfileFromConfig();
			}
		}
		LuipyUtilsConfigManager.save();
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		}
	}

	private void syncWorldTabToConfig() {
		for (int i = 0; i < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; i++) {
			if (this.profileNameFields[i] != null) {
				this.config.blockHighlightProfiles[i].name = this.profileNameFields[i].getValue().trim();
			}
			if (this.profileBlockIdFields[i] != null) {
				this.config.blockHighlightProfiles[i].blockIds = this.profileBlockIdFields[i].getValue();
			}
		}
	}

	private static String normalizeBlockHighlightIds(String raw) {
		return raw == null ? "" : raw.trim();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);

		graphics.fill(0, 0, SIDEBAR_WIDTH, this.height, 0xAA000000);
		graphics.fill(SIDEBAR_WIDTH, 0, SIDEBAR_WIDTH + 1, this.height, 0xFF404040);

		int selectedIndex = this.selectedCategory.ordinal();
		int highlightY = CONTENT_PADDING + 8 + selectedIndex * 24;
		graphics.fill(CONTENT_PADDING - 2, highlightY - 1, SIDEBAR_WIDTH - CONTENT_PADDING + 2, highlightY + 21, 0x5533AAFF);

		graphics.drawCenteredString(this.font, this.title, this.width / 2, 4, 0xFFFFFF);

		this.renderContentLabels(graphics);

		if (this.maxScroll > 0) {
			this.renderScrollbar(graphics);
		}
	}

	private void renderContentLabels(GuiGraphics graphics) {
		graphics.enableScissor(this.contentLeft, this.contentTop, this.contentRight, this.contentBottom);

		if (this.selectedCategory == LuipyConfigCategory.WORLD) {
			int y = this.contentTop + 4 - (int) this.scrollOffset;
			List<LuipyConfigBooleanEntry> entries = LuipyConfigCategories.forCategory(LuipyConfigCategory.WORLD);
			for (LuipyConfigBooleanEntry entry : entries) {
				if (y + ROW_HEIGHT >= this.contentTop && y <= this.contentBottom) {
					int labelX = this.contentLeft;
					if (entry.iconItem() != null) {
						graphics.renderItem(entry.iconItem().getDefaultInstance(), this.contentLeft, y + 2);
						labelX += ICON_TEXT_GAP;
					}
					graphics.drawString(this.font, entry.label(), labelX, y, 0xFFFFFF);
					graphics.drawString(this.font, entry.description(), labelX, y + 12, 0x888888);
				}
				y += ROW_HEIGHT;
			}

			int profileStartY = this.contentTop + ROW_HEIGHT - (int) this.scrollOffset;
			for (int profileIndex = 0; profileIndex < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; profileIndex++) {
				int sectionY = profileStartY + profileIndex * PROFILE_SECTION_HEIGHT;
				int controlsY = sectionY + PROFILE_NAME_HEIGHT;
				int idsLabelY = controlsY + PROFILE_CONTROLS_HEIGHT;
				int textureLabelY = idsLabelY + 28;

				if (controlsY >= this.contentTop && controlsY <= this.contentBottom) {
					graphics.drawString(
						this.font,
						Component.translatable("luipy-utils-mod.config.block_highlight.profile_enabled"),
						this.contentLeft,
						controlsY + 6,
						0x888888
					);
				}
				if (idsLabelY >= this.contentTop && idsLabelY <= this.contentBottom) {
					graphics.drawString(
						this.font,
						Component.translatable("luipy-utils-mod.config.block_highlight_ids"),
						this.contentLeft,
						idsLabelY,
						0xFFFFFF
					);
					graphics.drawString(
						this.font,
						Component.translatable("luipy-utils-mod.config.block_highlight_ids.desc"),
						this.contentLeft,
						idsLabelY + 12,
						0x888888
					);
				}
				if (textureLabelY >= this.contentTop && textureLabelY <= this.contentBottom) {
					graphics.drawString(
						this.font,
						Component.translatable("luipy-utils-mod.config.block_highlight.texture"),
						this.contentLeft,
						textureLabelY,
						0xFFFFFF
					);
					graphics.drawString(
						this.font,
						Component.translatable("luipy-utils-mod.config.block_highlight.texture.desc"),
						this.contentLeft + TEXTURE_PREVIEW_SIZE + 8,
						textureLabelY,
						0x888888
					);
				}
				this.renderProfileTexturePreview(graphics, profileIndex, sectionY);
			}
			graphics.disableScissor();
			return;
		}

		if (this.selectedCategory == LuipyConfigCategory.KEYBINDS) {
			int y = this.contentTop + 4;
			graphics.drawString(this.font, Component.translatable("luipy-utils-mod.config.keybinds.stub_title"), this.contentLeft, y, 0xFFFFFF);
			y += 14;
			graphics.drawString(this.font, Component.translatable("luipy-utils-mod.config.keybind.open_config"), this.contentLeft, y, 0xCCCCCC);
			y += 12;
			graphics.drawString(this.font, Component.translatable("luipy-utils-mod.config.keybind.open_config.desc"), this.contentLeft, y, 0x888888);
			y += 20;
			graphics.drawString(this.font, Component.translatable("luipy-utils-mod.config.keybind.open_unified_menu"), this.contentLeft, y, 0xCCCCCC);
			y += 12;
			graphics.drawString(this.font, Component.translatable("luipy-utils-mod.config.keybind.open_unified_menu.desc"), this.contentLeft, y, 0x888888);
			graphics.disableScissor();
			return;
		}

		List<LuipyConfigBooleanEntry> entries = LuipyConfigCategories.forCategory(this.selectedCategory);
		int y = this.contentTop + 4 - (int) this.scrollOffset;

		for (LuipyConfigBooleanEntry entry : entries) {
			if (y + ROW_HEIGHT >= this.contentTop && y <= this.contentBottom) {
				int labelX = this.contentLeft;
				if (entry.iconItem() != null) {
					graphics.renderItem(entry.iconItem().getDefaultInstance(), this.contentLeft, y + 2);
					labelX += ICON_TEXT_GAP;
				}
				graphics.drawString(this.font, entry.label(), labelX, y, 0xFFFFFF);
				graphics.drawString(this.font, entry.description(), labelX, y + 12, 0x888888);
			}
			y += ROW_HEIGHT;
		}

		graphics.disableScissor();
	}

	private void renderScrollbar(GuiGraphics graphics) {
		int trackX = this.contentRight - 4;
		int trackTop = this.contentTop;
		int trackHeight = this.contentBottom - this.contentTop;
		graphics.fill(trackX, trackTop, trackX + 3, this.contentBottom, 0x44FFFFFF);

		double ratio = this.scrollOffset / this.maxScroll;
		int thumbHeight = Math.max(20, trackHeight * trackHeight / (trackHeight + this.maxScroll));
		int thumbY = trackTop + (int) ((trackHeight - thumbHeight) * ratio);
		graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight, 0xCCFFFFFF);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (mouseX >= this.contentLeft && mouseX <= this.contentRight
			&& mouseY >= this.contentTop && mouseY <= this.contentBottom) {
			this.scrollOffset = Math.max(0, Math.min(this.maxScroll, this.scrollOffset - delta * ROW_HEIGHT));
			this.init();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	private void clampScroll() {
		this.scrollOffset = Math.max(0, Math.min(this.maxScroll, this.scrollOffset));
	}

	@Override
	public void onClose() {
		HighlightEmphasisTextures.onConfigScreenClosed();
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		}
	}

	private void renderProfileTexturePreview(GuiGraphics graphics, int profileIndex, int sectionY) {
		int previewX = this.contentLeft;
		int previewY = sectionY + PROFILE_NAME_HEIGHT + PROFILE_CONTROLS_HEIGHT + PROFILE_IDS_HEIGHT + 8;
		if (previewY + TEXTURE_PREVIEW_SIZE < this.contentTop || previewY > this.contentBottom) {
			return;
		}
		if (HighlightEmphasisTextures.profilePreviewTexture(profileIndex) == null) {
			return;
		}
		graphics.fill(previewX - 1, previewY - 1, previewX + TEXTURE_PREVIEW_SIZE + 1, previewY + TEXTURE_PREVIEW_SIZE + 1, 0xFF303030);
		ResourceLocation textureId = HighlightEmphasisTextures.profilePreviewTextureId(profileIndex);
		int texWidth = HighlightEmphasisTextures.profilePreviewTexture(profileIndex).getPixels().getWidth();
		int texHeight = HighlightEmphasisTextures.profilePreviewTexture(profileIndex).getPixels().getHeight();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, textureId);
		graphics.blit(textureId, previewX, previewY, 0, 0, TEXTURE_PREVIEW_SIZE, TEXTURE_PREVIEW_SIZE, texWidth, texHeight);
	}
}
