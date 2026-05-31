package com.luipy.utilsmod.client.config.ui;

import com.luipy.utilsmod.client.highlight.BlockHighlightManager;
import com.luipy.utilsmod.config.LuipyUtilsConfig;
import com.luipy.utilsmod.config.LuipyUtilsConfigManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * First-party in-game config screen (masa-style category sidebar + scrolling option rows).
 * Open with {@link com.luipy.utilsmod.client.config.LuipyConfigKeybinds} (default: hold X + L).
 */
public class LuipyConfigScreen extends Screen {
	private static final int SIDEBAR_WIDTH = 130;
	private static final int CONTENT_PADDING = 10;
	private static final int ROW_HEIGHT = 40;
	private static final int FOOTER_HEIGHT = 36;
	private static final int TOGGLE_WIDTH = 80;
	private static final int WORLD_EXTRA_HEIGHT = 88;

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
	private EditBox blockIdsField;

	public LuipyConfigScreen(Screen parent) {
		super(Component.translatable("luipy-utils-mod.config.title"));
		this.parent = parent;
		this.config = LuipyUtilsConfigManager.get();
	}

	public static Screen create(Screen parent) {
		return new LuipyConfigScreen(parent);
	}

	@Override
	protected void init() {
		this.toggleButtons.clear();
		this.clearWidgets();
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
			boolean selected = cat == this.selectedCategory;
			this.addRenderableWidget(
				Button.builder(category.title(), btn -> this.selectCategory(cat))
					.bounds(CONTENT_PADDING, y, SIDEBAR_WIDTH - CONTENT_PADDING * 2, 20)
					.build()
			);
			if (selected) {
				// Highlight handled in render pass via selectedCategory.
			}
			y += 24;
		}
	}

	private void selectCategory(LuipyConfigCategory category) {
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

		int fieldY = this.contentTop + ROW_HEIGHT + 12 - (int) this.scrollOffset;
		int fieldWidth = this.contentRight - this.contentLeft - 8;
		if (fieldY + 52 >= this.contentTop && fieldY <= this.contentBottom) {
			this.blockIdsField = new EditBox(
				this.font,
				this.contentLeft,
				fieldY,
				fieldWidth,
				20,
				Component.translatable("luipy-utils-mod.config.block_highlight_ids")
			);
			this.blockIdsField.setMaxLength(2048);
			this.blockIdsField.setValue(this.config.blockHighlightIds);
			this.blockIdsField.setHint(Component.translatable("luipy-utils-mod.config.block_highlight_ids.hint"));
			this.addRenderableWidget(this.blockIdsField);

			this.addRenderableWidget(
				Button.builder(Component.translatable("luipy-utils-mod.config.block_highlight_apply"), btn -> {
					if (this.blockIdsField != null) {
						BlockHighlightManager.applyFromConfig(this.blockIdsField.getValue());
					}
				}).bounds(this.contentLeft, fieldY + 28, 160, 20).build()
			);
		}

		int contentHeight = this.contentBottom - this.contentTop;
		this.maxScroll = Math.max(0, ROW_HEIGHT + WORLD_EXTRA_HEIGHT - contentHeight);
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
		if (this.blockIdsField != null) {
			this.config.blockHighlightIds = this.blockIdsField.getValue();
			BlockHighlightManager.reloadFromConfig();
		}
		LuipyUtilsConfigManager.save();
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		}
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
					graphics.drawString(this.font, entry.label(), this.contentLeft, y, 0xFFFFFF);
					graphics.drawString(this.font, entry.description(), this.contentLeft, y + 12, 0x888888);
				}
				y += ROW_HEIGHT;
			}
			int idsLabelY = this.contentTop + ROW_HEIGHT + 4 - (int) this.scrollOffset;
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
				graphics.drawString(this.font, entry.label(), this.contentLeft, y, 0xFFFFFF);
				graphics.drawString(this.font, entry.description(), this.contentLeft, y + 12, 0x888888);
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
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		}
	}
}
