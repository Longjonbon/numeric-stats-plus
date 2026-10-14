package com.numericstatsplus;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Numeric Stats Plus",
	description = "HP, prayer, run and spec as numbers next to your character, with adjustable size and outline",
	tags = {"hp", "prayer", "health", "numbers", "stats", "overlay"}
)
public class NumericStatsPlusPlugin extends Plugin
{
	// How long the numbers stay up after the last sign of combat (10 ticks = 6 seconds).
	private static final int COMBAT_TIMEOUT_TICKS = 10;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NumericStatsPlusOverlay numericStatsPlusOverlay;

	@Inject
	private NumericStatsPlusConfig config;

	private int lastCombatTick = -COMBAT_TIMEOUT_TICKS - 1;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(numericStatsPlusOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(numericStatsPlusOverlay);
	}

	boolean isInCombat()
	{
		return client.getTickCount() - lastCombatTick <= COMBAT_TIMEOUT_TICKS;
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		// Being hit, or landing a hit on something, counts as combat.
		if (event.getActor() == client.getLocalPlayer() || event.getHitsplat().isMine())
		{
			lastCombatTick = client.getTickCount();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Attacking something keeps the numbers up between hits; talking to a banker does not.
		Player player = client.getLocalPlayer();
		Actor target = player == null ? null : player.getInteracting();
		if (target instanceof Player || (target instanceof NPC && ((NPC) target).getCombatLevel() > 0))
		{
			lastCombatTick = client.getTickCount();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("numericstatsplus"))
		{
			numericStatsPlusOverlay.updateConfig();
		}
	}

	@Provides
	NumericStatsPlusConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NumericStatsPlusConfig.class);
	}
}
