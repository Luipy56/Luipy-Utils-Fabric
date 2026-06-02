package com.luipy.utilsmod.client.config.ui;

import com.luipy.utilsmod.client.highlight.BlockHighlightManager;
import com.luipy.utilsmod.client.highlight.HighlightEmphasisTextures;
import com.luipy.utilsmod.client.highlight.HighlightTextureFileChooser;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
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
	private static final int WORLD_HEADER_HEIGHT = 52;
	private static final int ICON_SIZE = 16;
	private static final int ICON_TEXT_GAP = 22;
	private static final int FOOTER_HEIGHT = 36;
	private static final int TOGGLE_WIDTH = 80;
	private static final int ACTIVE_BUTTON_WIDTH = 72;
	private static final int TEXTURE_PREVIEW_SIZE = 48;
	private static final int RESET_ICON_SIZE = 14;
	private static final int PROFILE_CONTROLS_HEIGHT = 24;
	private static final int PROFILE_IDS_HEIGHT = 32;
	private static final int PROFILE_TEXTURE_HEIGHT = 52;
	private static final int PROFILE_SECTION_HEIGHT =
		PROFILE_CONTROLS_HEIGHT + PROFILE_IDS_HEIGHT + PROFILE_TEXTURE_HEIGHT + 8;

	private final Screen parent;
	private final LuipyUtilsConfig config;
	private LuipyConfigCategory selectedCategory = LuipyConfigCategory.GENERAL;
	private double scrollOffset;
	private int contentTop;
	private int contentBottom;
	private int contentLeft;
	private int contentRight;
	private int maxScroll;
	private final List<LuipyConfigOnOffToggle> toggleButtons = new ArrayList<>();
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

		List<LuipyConfigRowEntry> entries = LuipyConfigCategories.forCategory(this.selectedCategory);
		int y = this.contentTop - (int) this.scrollOffset;

		for (LuipyConfigRowEntry entry : entries) {
			if (y + ROW_HEIGHT < this.contentTop || y > this.contentBottom) {
				y += ROW_HEIGHT;
				continue;
			}

			if (entry instanceof LuipyConfigBooleanEntry booleanEntry) {
				boolean current = booleanEntry.getter().apply(this.config);
				LuipyConfigOnOffToggle toggle = LuipyConfigOnOffToggle.create(
					this.contentRight - TOGGLE_WIDTH,
					y + 8,
					TOGGLE_WIDTH,
					20,
					current,
					booleanEntry.label(),
					(btn, value) -> booleanEntry.setter().accept(this.config, value)
				);
				this.toggleButtons.add(toggle);
				this.addRenderableWidget(toggle);
			} else if (entry instanceof LuipyConfigCycleEntry<?> cycleEntry) {
				this.addCycleToggle(cycleEntry, y);
			}
			y += ROW_HEIGHT;
		}

		this.maxScroll = Math.max(0, entries.size() * ROW_HEIGHT - (this.contentBottom - this.contentTop));
	}

	private <T> void addCycleToggle(LuipyConfigCycleEntry<T> entry, int y) {
		T current = entry.getter().apply(this.config);
		LuipyConfigCycleToggle<T> toggle = LuipyConfigCycleToggle.create(
			this.contentRight - TOGGLE_WIDTH,
			y + 8,
			TOGGLE_WIDTH,
			20,
			current,
			entry.values(),
			entry::valueLabel,
			entry.valueColor(),
			entry.label(),
			(btn, value) -> entry.setter().accept(this.config, value)
		);
		this.addRenderableWidget(toggle);
	}

	private void rebuildWorldWidgets() {
		List<LuipyConfigBooleanEntry> entries = LuipyConfigCategories.booleanEntriesForCategory(LuipyConfigCategory.WORLD);
		int toggleX = this.contentRight - TOGGLE_WIDTH;
		int y = this.contentTop - (int) this.scrollOffset;

		for (LuipyConfigBooleanEntry entry : entries) {
			if (y + WORLD_HEADER_HEIGHT >= this.contentTop && y <= this.contentBottom) {
				boolean current = entry.getter().apply(this.config);
				LuipyConfigOnOffToggle toggle = LuipyConfigOnOffToggle.create(
					toggleX,
					y + 8,
					TOGGLE_WIDTH,
					20,
					current,
					entry.label(),
					(btn, value) -> entry.setter().accept(this.config, value)
				);
				this.toggleButtons.add(toggle);
				this.addRenderableWidget(toggle);
			}
			y += WORLD_HEADER_HEIGHT;
		}

		int profileStartY = this.contentTop + WORLD_HEADER_HEIGHT - (int) this.scrollOffset;
		int fieldWidth = this.contentRight - this.contentLeft - 8;
		int activeButtonX = toggleX - ACTIVE_BUTTON_WIDTH - 6;

		for (int profileIndex = 0; profileIndex < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; profileIndex++) {
			LuipyUtilsConfig.HighlightProfile profile = this.config.blockHighlightProfiles[profileIndex];
			int sectionY = profileStartY + profileIndex * PROFILE_SECTION_HEIGHT;

			int controlsY = sectionY;
			if (controlsY + PROFILE_CONTROLS_HEIGHT >= this.contentTop && controlsY <= this.contentBottom) {
				final int idx = profileIndex;
				boolean isActive = this.config.activeBlockHighlightProfile == profileIndex;
				Button activeButton = Button.builder(
					isActive
						? Component.translatable("luipy-utils-mod.config.block_highlight.profile_active")
						: Component.translatable("luipy-utils-mod.config.block_highlight.profile_select"),
					btn -> this.selectActiveProfile(idx)
				).bounds(activeButtonX, controlsY, ACTIVE_BUTTON_WIDTH, 20).build();
				if (isActive) {
					activeButton.active = false;
				}
				this.addRenderableWidget(activeButton);
				LuipyConfigOnOffToggle enabledToggle = LuipyConfigOnOffToggle.create(
					toggleX,
					controlsY,
					TOGGLE_WIDTH,
					20,
					profile.enabled,
					Component.translatable("luipy-utils-mod.config.block_highlight.profile_enabled"),
					(btn, value) -> this.config.blockHighlightProfiles[idx].enabled = value
				);
				this.toggleButtons.add(enabledToggle);
				this.addRenderableWidget(enabledToggle);
			}

			int idsY = sectionY + PROFILE_CONTROLS_HEIGHT + 4;
			if (idsY + 20 >= this.contentTop && idsY <= this.contentBottom) {
				EditBox idsField = new EditBox(
					this.font,
					this.contentLeft,
					idsY,
					fieldWidth,
					20,
					Component.empty()
				);
				idsField.setMaxLength(2048);
				idsField.setValue(profile.blockIds != null ? profile.blockIds : "");
				this.profileBlockIdFields[profileIndex] = idsField;
				this.addRenderableWidget(idsField);
			}

			int previewY = sectionY + PROFILE_CONTROLS_HEIGHT + PROFILE_IDS_HEIGHT + 4;
			if (previewY + RESET_ICON_SIZE >= this.contentTop && previewY <= this.contentBottom) {
				final int idx = profileIndex;
				int resetX = this.contentLeft + TEXTURE_PREVIEW_SIZE + 4;
				this.addRenderableWidget(
					Button.builder(Component.literal("\u21BB"), btn -> {
						HighlightEmphasisTextures.resetProfileToDefault(idx);
						this.init();
					})
						.bounds(resetX, previewY + TEXTURE_PREVIEW_SIZE - RESET_ICON_SIZE, RESET_ICON_SIZE, RESET_ICON_SIZE)
						.tooltip(Tooltip.create(Component.translatable("luipy-utils-mod.config.block_highlight.texture_reset")))
						.build()
				);
			}
		}

		int contentHeight = this.contentBottom - this.contentTop;
		int totalHeight = WORLD_HEADER_HEIGHT + LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT * PROFILE_SECTION_HEIGHT;
		this.maxScroll = Math.max(0, totalHeight - contentHeight);
	}

	private void selectActiveProfile(int profileIndex) {
		if (this.config.activeBlockHighlightProfile == profileIndex) {
			return;
		}
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

		this.renderContentLabels(graphics, mouseX, mouseY);

		if (this.maxScroll > 0) {
			this.renderScrollbar(graphics);
		}
	}

	private void renderContentLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.enableScissor(this.contentLeft, this.contentTop, this.contentRight, this.contentBottom);

		if (this.selectedCategory == LuipyConfigCategory.WORLD) {
			int y = this.contentTop + 4 - (int) this.scrollOffset;
			List<LuipyConfigBooleanEntry> entries =
				LuipyConfigCategories.booleanEntriesForCategory(LuipyConfigCategory.WORLD);
			for (LuipyConfigBooleanEntry entry : entries) {
				if (y + WORLD_HEADER_HEIGHT >= this.contentTop && y <= this.contentBottom) {
					int labelX = this.contentLeft;
					if (entry.iconItem() != null) {
						graphics.renderItem(entry.iconItem().getDefaultInstance(), this.contentLeft, y + 2);
						labelX += ICON_TEXT_GAP;
					}
					graphics.drawString(this.font, entry.label(), labelX, y, 0xFFFFFF);
					graphics.drawString(this.font, entry.description(), labelX, y + 12, 0x888888);
					graphics.drawString(
						this.font,
						Component.translatable("luipy-utils-mod.config.block_highlight_ids.desc"),
						labelX,
						y + 24,
						0x888888
					);
				}
				y += WORLD_HEADER_HEIGHT;
			}

			int profileStartY = this.contentTop + WORLD_HEADER_HEIGHT - (int) this.scrollOffset;
			for (int profileIndex = 0; profileIndex < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; profileIndex++) {
				int sectionY = profileStartY + profileIndex * PROFILE_SECTION_HEIGHT;
				int idsLabelY = sectionY + PROFILE_CONTROLS_HEIGHT;

				if (idsLabelY >= this.contentTop && idsLabelY <= this.contentBottom) {
					graphics.drawString(
						this.font,
						Component.translatable("luipy-utils-mod.config.block_highlight_ids"),
						this.contentLeft,
						idsLabelY,
						0xFFFFFF
					);
				}
				this.renderProfileTexturePreview(graphics, profileIndex, sectionY, mouseX, mouseY);
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

		List<LuipyConfigRowEntry> entries = LuipyConfigCategories.forCategory(this.selectedCategory);
		int y = this.contentTop + 4 - (int) this.scrollOffset;

		for (LuipyConfigRowEntry entry : entries) {
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
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && this.selectedCategory == LuipyConfigCategory.WORLD) {
			int profileIndex = profileIndexAtPreview(mouseX, mouseY);
			if (profileIndex >= 0) {
				HighlightTextureFileChooser.openAsync(profileIndex);
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private int profileIndexAtPreview(double mouseX, double mouseY) {
		if (mouseX < this.contentLeft || mouseX > this.contentLeft + TEXTURE_PREVIEW_SIZE
			|| mouseY < this.contentTop || mouseY > this.contentBottom) {
			return -1;
		}
		int profileStartY = this.contentTop + WORLD_HEADER_HEIGHT - (int) this.scrollOffset;
		for (int profileIndex = 0; profileIndex < LuipyUtilsConfig.HIGHLIGHT_PROFILE_COUNT; profileIndex++) {
			int sectionY = profileStartY + profileIndex * PROFILE_SECTION_HEIGHT;
			int previewY = sectionY + PROFILE_CONTROLS_HEIGHT + PROFILE_IDS_HEIGHT + 4;
			if (mouseY >= previewY && mouseY < previewY + TEXTURE_PREVIEW_SIZE) {
				return profileIndex;
			}
		}
		return -1;
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

	private void renderProfileTexturePreview(GuiGraphics graphics, int profileIndex, int sectionY, int mouseX, int mouseY) {
		int previewX = this.contentLeft;
		int previewY = sectionY + PROFILE_CONTROLS_HEIGHT + PROFILE_IDS_HEIGHT + 4;
		if (previewY + TEXTURE_PREVIEW_SIZE < this.contentTop || previewY > this.contentBottom) {
			return;
		}
		ResourceLocation textureId = HighlightEmphasisTextures.profilePreviewTextureId(profileIndex);
		var dynamicTexture = HighlightEmphasisTextures.profilePreviewTexture(profileIndex);
		int texWidth = dynamicTexture != null ? dynamicTexture.getPixels().getWidth() : 16;
		int texHeight = dynamicTexture != null ? dynamicTexture.getPixels().getHeight() : 16;

		boolean hovered = mouseX >= previewX && mouseX < previewX + TEXTURE_PREVIEW_SIZE
			&& mouseY >= previewY && mouseY < previewY + TEXTURE_PREVIEW_SIZE;
		int borderColor = hovered ? 0xFFAAAAAA : 0xFF303030;
		graphics.fill(previewX - 1, previewY - 1, previewX + TEXTURE_PREVIEW_SIZE + 1, previewY + TEXTURE_PREVIEW_SIZE + 1, borderColor);
		graphics.blit(
			textureId,
			previewX,
			previewY,
			TEXTURE_PREVIEW_SIZE,
			TEXTURE_PREVIEW_SIZE,
			0,
			0,
			texWidth,
			texHeight,
			texWidth,
			texHeight
		);
	}
}
