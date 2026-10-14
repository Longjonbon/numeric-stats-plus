package com.numericstatsplus;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class NumericStatsPlusPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NumericStatsPlusPlugin.class);
		RuneLite.main(args);
	}
}