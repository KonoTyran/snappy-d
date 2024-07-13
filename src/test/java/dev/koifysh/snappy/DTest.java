package dev.koifysh.snappy;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(D.class);
		RuneLite.main(args);
	}
}