/*
 * Copyright (c) 2022, LlemonDuck <napkinorton@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.numericstatsplus;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
class NumericStatsPlusOverlay extends Overlay
{

	/**
	 * This overlay is largely based on the plugin Party (PartyStatusOverlay.java), but with some small edits.
	 */
	private static final int RIGHT_SIDE_GAP = 16;
	private static final int ICON_GAP = 2;

	private Color COLOR_HEALTH_MAX = Color.green;
	private Color COLOR_HEALTH_MIN = Color.red;
	private Color COLOR_PRAYER = new Color(50, 200, 200);
	private Color COLOR_STAMINA = new Color(160, 124, 72);
	private Color COLOR_STAMINA_BOOSTED = new Color(220, 184, 132);
	private Color COLOR_SPEC = new Color(225, 225, 0);
	private Font overlayFont = FontManager.getRunescapeBoldFont().deriveFont(16f);

	private final Client client;
	private final SpriteManager spriteManager;
	private final NumericStatsPlusConfig config;
	private final ItemManager itemManager;
	private final NumericStatsPlusPlugin plugin;

	// Potion icons resized to the current font, so they are not rescaled every frame.
	private final Map<Integer, BufferedImage> iconCache = new ConcurrentHashMap<>();
	private final Set<BufferedImage> watchedIcons = ConcurrentHashMap.newKeySet();
	private int iconHeight = -1;

	private boolean renderHealth = false;
	private boolean renderPrayer = false;
	private boolean renderStamina = false;
	private boolean renderSpec = false;
	private boolean renderVeng = false;

	private boolean displayStam = false;
	private boolean displayOverload = false;
	private boolean displayPoT = false;

	private boolean healthLeft = true;
	private boolean prayerLeft = true;
	private boolean staminaLeft = false;
	private boolean specLeft = false;

	private int offsetLeft = 15;
	private int offsetRight = 5;
	private int offsetVertical = 0;
	private int outlineWidth = 0;

	private boolean onlyInCombat = false;
	private int lowHealthThreshold = 0;
	private int lowPrayerThreshold = 0;
	private Color warningColor = Color.red;
	private boolean warningFlash = true;

	@Inject
	private NumericStatsPlusOverlay(
		Client client, SpriteManager spriteManager, NumericStatsPlusConfig config, ItemManager itemManager,
		NumericStatsPlusPlugin plugin
	)
	{
		this.plugin = plugin;
		this.client = client;
		this.spriteManager = spriteManager;
		this.config = config;
		this.itemManager = itemManager;

		updateConfig();

		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Player player = client.getLocalPlayer();
		if (player == null || player.getLocalLocation() == null)
		{
			return null;
		}

		final int healthCurrent = client.getBoostedSkillLevel(Skill.HITPOINTS);
		final int prayerCurrent = client.getBoostedSkillLevel(Skill.PRAYER);
		final boolean healthWarning = lowHealthThreshold > 0 && healthCurrent <= lowHealthThreshold;
		final boolean prayerWarning = lowPrayerThreshold > 0 && prayerCurrent <= lowPrayerThreshold;
		if (onlyInCombat && !healthWarning && !prayerWarning && !plugin.isInCombat())
		{
			return null;
		}

		Point anchor = Perspective.localToCanvas(client, player.getLocalLocation(), client.getTopLevelWorldView().getPlane(), player.getLogicalHeight());
		if (anchor == null)
		{
			return null;
		}

		graphics.setFont(overlayFont);
		// The RuneScape fonts are pixel fonts; scaled past 16px they show gaps unless smoothed.
		Object previousTextHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		FontMetrics fm = graphics.getFontMetrics();
		if (iconHeight != fm.getAscent())
		{
			iconHeight = fm.getAscent();
			iconCache.clear();
		}

		int renderIxRight = 0;
		int renderIxLeft = 0;

		final int healthMax = client.getRealSkillLevel(Skill.HITPOINTS);
		final int runEnergy = (int) (client.getEnergy() / 100.0);
		final int specEnergy = client.getVarpValue(VarPlayerID.SA_ENERGY) / 10;

		final boolean vengActive = client.getVarbitValue(VarbitID.VENGEANCE_REBOUND) == 1;
		final boolean runEnergyBoosted = client.getVarbitValue(VarbitID.STAMINA_ACTIVE) != 0;
		final boolean hasPoT = client.getVarbitValue(VarbitID.RAIDS_PRAYERENHANCE_TIMER) != 0;
		final boolean hasOverload = client.getVarbitValue(VarbitID.RAIDS_OVERLOAD_TIMER) != 0;

		if (renderHealth)
		{
			double healthRatio = Math.min(1.0, (double) healthCurrent / healthMax);
			Color healthColor = ColorUtil.colorLerp(COLOR_HEALTH_MIN, COLOR_HEALTH_MAX, healthRatio);
			if (healthWarning)
			{
				healthColor = warningColorFor(healthColor);
			}
			BufferedImage icon = displayOverload && hasOverload ? itemIcon(ItemID.RAIDS_VIAL_OVERLOAD_STRONG_4) : null;
			renderStat(graphics, fm, anchor, String.valueOf(healthCurrent), healthColor, icon, healthLeft ? renderIxLeft++ : renderIxRight++, healthLeft);
		}
		if (renderPrayer)
		{
			BufferedImage icon = displayPoT && hasPoT ? itemIcon(ItemID.RAIDS_VIAL_PRAYER_STRONG_4) : null;
			Color prayerColor = prayerWarning ? warningColorFor(COLOR_PRAYER) : COLOR_PRAYER;
			renderStat(graphics, fm, anchor, String.valueOf(prayerCurrent), prayerColor, icon, prayerLeft ? renderIxLeft++ : renderIxRight++, prayerLeft);
		}
		if (renderStamina)
		{
			BufferedImage icon = displayStam && runEnergyBoosted ? itemIcon(ItemID._4DOSESTAMINA) : null;
			renderStat(graphics, fm, anchor, String.valueOf(runEnergy), runEnergyBoosted ? COLOR_STAMINA_BOOSTED : COLOR_STAMINA, icon, staminaLeft ? renderIxLeft++ : renderIxRight++, staminaLeft);
		}
		else if (displayStam && runEnergyBoosted)
		{
			// Run energy is hidden, so show the stamina icon on its own row.
			renderStat(graphics, fm, anchor, null, null, itemIcon(ItemID._4DOSESTAMINA), renderIxLeft++, true);
		}
		if (renderSpec)
		{
			renderStat(graphics, fm, anchor, String.valueOf(specEnergy), COLOR_SPEC, null, specLeft ? renderIxLeft++ : renderIxRight++, specLeft);
		}
		if (renderVeng && vengActive)
		{
			BufferedImage vengIcon = spriteManager.getSprite(SpriteID.LunarMagicOn.VENGEANCE_OTHER, 0);
			if (vengIcon != null)
			{
				renderPlayerOverlay(graphics, player, vengIcon);
			}
		}

		if (previousTextHint != null)
		{
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, previousTextHint);
		}
		return null;
	}

	/**
	 * Draws one stat row. Left-side text is right-aligned against the player so bigger fonts grow outwards;
	 * an optional icon sits on the outer side of the text.
	 */
	private void renderStat(Graphics2D graphics, FontMetrics fm, Point anchor, String text, Color color, BufferedImage icon, int renderIx, boolean left)
	{
		int baseline = anchor.getY() + offsetVertical + fm.getHeight() * renderIx;
		int textWidth = text == null ? 0 : fm.stringWidth(text);
		int textX = left ? anchor.getX() - offsetLeft - textWidth : anchor.getX() + offsetRight + RIGHT_SIDE_GAP;

		if (text != null)
		{
			renderText(graphics, textX, baseline, text, color);
		}

		if (icon != null)
		{
			int gap = text == null ? 0 : ICON_GAP;
			int iconX = left ? textX - gap - icon.getWidth() : textX + textWidth + gap;
			int iconY = baseline - icon.getHeight();
			OverlayUtil.renderImageLocation(graphics, new Point(iconX, iconY), icon);
		}
	}

	// Solid warning color, or blinking between it and the normal color about twice a second.
	private Color warningColorFor(Color normal)
	{
		if (!warningFlash)
		{
			return warningColor;
		}
		return (System.currentTimeMillis() / 250) % 2 == 0 ? warningColor : normal;
	}

	private void renderText(Graphics2D graphics, int x, int y, String text, Color color)
	{
		if (outlineWidth <= 0)
		{
			OverlayUtil.renderTextLocation(graphics, new Point(x, y), text, color);
			return;
		}

		graphics.setColor(new Color(0, 0, 0, color.getAlpha()));
		for (int dx = -outlineWidth; dx <= outlineWidth; dx++)
		{
			for (int dy = -outlineWidth; dy <= outlineWidth; dy++)
			{
				if ((dx != 0 || dy != 0) && dx * dx + dy * dy <= outlineWidth * outlineWidth + 1)
				{
					graphics.drawString(text, x + dx, y + dy);
				}
			}
		}
		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}

	private void renderPlayerOverlay(Graphics2D graphics, Player player, BufferedImage image)
	{
		Point textLocation = player.getCanvasImageLocation(image, player.getLogicalHeight() / 2);
		if (textLocation != null)
		{
			OverlayUtil.renderImageLocation(graphics, textLocation, image);
		}
	}

	private BufferedImage itemIcon(int itemId)
	{
		BufferedImage cached = iconCache.get(itemId);
		if (cached != null)
		{
			return cached;
		}

		AsyncBufferedImage source = itemManager.getImage(itemId);
		if (source == null)
		{
			return null;
		}
		if (watchedIcons.add(source))
		{
			// Item images load in the background; rescale again once the real pixels arrive.
			source.onLoaded(() -> iconCache.remove(itemId));
		}

		int height = Math.max(1, iconHeight);
		int width = Math.max(1, Math.round(height * 18f / 16f));
		BufferedImage resized = rescale(source, width, height);
		iconCache.put(itemId, resized);
		return resized;
	}

	private BufferedImage rescale(BufferedImage img, int newWidth, int newHeight)
	{
		BufferedImage imgResized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics2D = imgResized.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(img, 0, 0, newWidth, newHeight, null);
		graphics2D.dispose();

		return imgResized;
	}

	private static Font buildFont(PlayerStatsFontType type, int size)
	{
		switch (type)
		{
			case REGULAR:
				return FontManager.getRunescapeFont().deriveFont((float) size);
			case SMALL:
				return FontManager.getRunescapeSmallFont().deriveFont((float) size);
			case ARIAL:
				return new Font("Arial", Font.BOLD, size);
			case BOLD:
			default:
				return FontManager.getRunescapeBoldFont().deriveFont((float) size);
		}
	}

	void updateConfig()
	{
		this.renderHealth = config.healthPosition() != PlayerStatsPositionType.None;
		this.renderPrayer = config.prayerPosition() != PlayerStatsPositionType.None;
		this.renderStamina = config.runEnergyPosition() != PlayerStatsPositionType.None;
		this.renderSpec = config.specPosition() != PlayerStatsPositionType.None;
		this.renderVeng = config.displayVeng();

		this.displayStam = config.displayStam();
		this.displayOverload = config.displayOverload();
		this.displayPoT = config.displayPoT();

		this.healthLeft = config.healthPosition() == PlayerStatsPositionType.Left;
		this.prayerLeft = config.prayerPosition() == PlayerStatsPositionType.Left;
		this.staminaLeft = config.runEnergyPosition() == PlayerStatsPositionType.Left;
		this.specLeft = config.specPosition() == PlayerStatsPositionType.Left;

		this.offsetRight = config.offsetRight();
		this.offsetLeft = config.offsetLeft();
		this.offsetVertical = config.offsetVertical();
		this.outlineWidth = config.outlineWidth();

		this.onlyInCombat = config.onlyInCombat();
		this.lowHealthThreshold = config.lowHealthThreshold();
		this.lowPrayerThreshold = config.lowPrayerThreshold();
		this.warningColor = config.warningColor();
		this.warningFlash = config.warningFlash();

		this.overlayFont = buildFont(config.fontType(), config.fontSize());

		this.COLOR_HEALTH_MAX = config.maxHealthColor();
		this.COLOR_HEALTH_MIN = config.minHealthColor();
		this.COLOR_PRAYER = config.prayerColor();
		this.COLOR_STAMINA = config.runEnergyColor();
		this.COLOR_STAMINA_BOOSTED = config.staminaBoostedColor();
		this.COLOR_SPEC = config.specColor();
	}
}
