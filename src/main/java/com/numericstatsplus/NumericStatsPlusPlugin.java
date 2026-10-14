package com.numericstatsplus;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Numeric Stats Plus"
)
public class NumericStatsPlusPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NumericStatsPlusOverlay numericStatsPlusOverlay;

	@Inject
	private NumericStatsPlusConfig config;

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
