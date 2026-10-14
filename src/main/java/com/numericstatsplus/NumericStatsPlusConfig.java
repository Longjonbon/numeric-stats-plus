package com.numericstatsplus;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

import java.awt.*;

@ConfigGroup("numericstatsplus")
public interface NumericStatsPlusConfig extends Config
{
	@ConfigSection(
			name = "Stats",
			description = "Which stats and icons to show, and where.",
			position = 0
	)
	String statsSection = "statsSection";

	@ConfigSection(
			name = "Text",
			description = "Size, font, outline and position of the numbers.",
			position = 1
	)
	String textSection = "textSection";

	@ConfigSection(
			name = "Warnings",
			description = "Highlight HP or prayer when they drop to a level you pick.",
			position = 2
	)
	String warningsSection = "warningsSection";

	@ConfigSection(
			name = "Colors",
			description = "Colors of each number.",
			position = 3,
			closedByDefault = true
	)
	String colorsSection = "colorsSection";

	// ---------- Stats ----------

	@ConfigItem(
			keyName = "healthPosition",
			name = "Health position",
			description = "Where and if to display health.",
			position = 0,
			section = statsSection
	)
	default PlayerStatsPositionType healthPosition()
	{
		return PlayerStatsPositionType.Right;
	}

	@ConfigItem(
			keyName = "prayerPosition",
			name = "Prayer position",
			description = "Where and if to display prayer.",
			position = 1,
			section = statsSection
	)
	default PlayerStatsPositionType prayerPosition()
	{
		return PlayerStatsPositionType.Right;
	}

	@ConfigItem(
			keyName = "runEnergyPosition",
			name = "Run energy position",
			description = "Where and if to display run energy.",
			position = 2,
			section = statsSection
	)
	default PlayerStatsPositionType runEnergyPosition()
	{
		return PlayerStatsPositionType.None;
	}

	@ConfigItem(
			keyName = "specPosition",
			name = "Spec position",
			description = "Where and if to display spec.",
			position = 3,
			section = statsSection
	)
	default PlayerStatsPositionType specPosition()
	{
		return PlayerStatsPositionType.None;
	}

	@ConfigItem(
			keyName = "onlyInCombat",
			name = "Only show in combat",
			description = "Hide the numbers when you have not attacked or been hit for a few seconds. Warnings still show.",
			position = 4,
			section = statsSection
	)
	default boolean onlyInCombat()
	{
		return false;
	}

	@ConfigItem(
			keyName = "displayVeng",
			name = "Display vengeance",
			description = "Show vengeance on the player model.",
			position = 5,
			section = statsSection
	)
	default boolean displayVeng()
	{
		return false;
	}

	@ConfigItem(
			keyName = "displayStam",
			name = "Display stamina pot",
			description = "Show stamina on the player model.",
			position = 6,
			section = statsSection
	)
	default boolean displayStam()
	{
		return false;
	}

	@ConfigItem(
			keyName = "displayOverload",
			name = "Display overload",
			description = "Show overload effects on the player model.",
			position = 7,
			section = statsSection
	)
	default boolean displayOverload()
	{
		return false;
	}

	@ConfigItem(
			keyName = "displayPoT",
			name = "Display PoT",
			description = "Show prayer over time effects on the player model.",
			position = 8,
			section = statsSection
	)
	default boolean displayPoT()
	{
		return false;
	}

	// ---------- Text ----------

	@Range(min = 8, max = 64)
	@Units(Units.PIXELS)
	@ConfigItem(
			keyName = "fontSize",
			name = "Font size",
			description = "Size of the numbers. Icons scale with it.",
			position = 0,
			section = textSection
	)
	default int fontSize()
	{
		return 16;
	}

	@ConfigItem(
			keyName = "fontType",
			name = "Font",
			description = "Font used for the numbers.",
			position = 1,
			section = textSection
	)
	default PlayerStatsFontType fontType()
	{
		return PlayerStatsFontType.BOLD;
	}

	@Range(min = 0, max = 4)
	@Units(Units.PIXELS)
	@ConfigItem(
			keyName = "outlineWidth",
			name = "Outline width",
			description = "Black outline around the numbers. 0 uses the original drop shadow.",
			position = 2,
			section = textSection
	)
	default int outlineWidth()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "offsetRight",
			name = "Offset right",
			description = "Configures the offset from the player model to draw text right from.",
			position = 3,
			section = textSection
	)
	default int offsetRight()
	{
		return 5;
	}

	@ConfigItem(
			keyName = "offsetLeft",
			name = "Offset left",
			description = "Configures the offset from the player model to draw text left from.",
			position = 4,
			section = textSection
	)
	default int offsetLeft()
	{
		return 15;
	}

	@Range(min = -300, max = 300)
	@Units(Units.PIXELS)
	@ConfigItem(
			keyName = "offsetVertical",
			name = "Offset vertical",
			description = "Moves the numbers down (positive) or up (negative).",
			position = 5,
			section = textSection
	)
	default int offsetVertical()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "followZoom",
			name = "Follow zoom",
			description = "Scale the offsets with camera zoom so the numbers stay in the same spot next to your character.",
			position = 6,
			section = textSection
	)
	default boolean followZoom()
	{
		return true;
	}

	// ---------- Warnings ----------

	@Range(max = 255)
	@ConfigItem(
			keyName = "lowHealthThreshold",
			name = "Low HP at or below",
			description = "Warn when HP is at or below this number. 0 turns the warning off.",
			position = 0,
			section = warningsSection
	)
	default int lowHealthThreshold()
	{
		return 0;
	}

	@Range(max = 255)
	@ConfigItem(
			keyName = "lowPrayerThreshold",
			name = "Low prayer at or below",
			description = "Warn when prayer is at or below this number. 0 turns the warning off.",
			position = 1,
			section = warningsSection
	)
	default int lowPrayerThreshold()
	{
		return 0;
	}

	@Alpha
	@ConfigItem(
			keyName = "warningColor",
			name = "Warning color",
			description = "Color of a number while it is in warning.",
			position = 2,
			section = warningsSection
	)
	default Color warningColor()
	{
		return Color.red;
	}

	@ConfigItem(
			keyName = "warningFlash",
			name = "Flash",
			description = "Blink between the warning color and the normal color instead of staying solid.",
			position = 3,
			section = warningsSection
	)
	default boolean warningFlash()
	{
		return true;
	}

	// ---------- Colors ----------

	@Alpha
	@ConfigItem(
			keyName = "maxHealthColor",
			name = "Max health color",
			description = "Configures the color for health text when your at max health",
			position = 0,
			section = colorsSection
	)
	default Color maxHealthColor()
	{
		return Color.green;
	}

	@Alpha
	@ConfigItem(
			keyName = "minHealthColor",
			name = "Min health color",
			description = "Configures the color for health text when your at min health",
			position = 1,
			section = colorsSection
	)
	default Color minHealthColor()
	{
		return Color.red;
	}

	@Alpha
	@ConfigItem(
			keyName = "prayerColor",
			name = "Prayer color",
			description = "Configures the color for prayer text",
			position = 2,
			section = colorsSection
	)
	default Color prayerColor()
	{
		return new Color(50, 200, 200);
	}

	@Alpha
	@ConfigItem(
			keyName = "runEnergyColor",
			name = "Run energy color",
			description = "Configures the color for run energy text",
			position = 3,
			section = colorsSection
	)
	default Color runEnergyColor()
	{
		return new Color(160, 124, 72);
	}

	@Alpha
	@ConfigItem(
			keyName = "staminaBoostedColor",
			name = "Stamina boosted color",
			description = "Configures the color for run energy text when boosted",
			position = 4,
			section = colorsSection
	)
	default Color staminaBoostedColor()
	{
		return new Color(220, 184, 132);
	}

	@Alpha
	@ConfigItem(
			keyName = "specColor",
			name = "Spec color",
			description = "Configures the color for special attack text",
			position = 5,
			section = colorsSection
	)
	default Color specColor()
	{
		return Color.yellow;
	}
}
