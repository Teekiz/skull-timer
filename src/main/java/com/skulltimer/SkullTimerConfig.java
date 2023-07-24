package com.skulltimer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Skull Timer")
public interface SkullTimerConfig extends Config
{
	@ConfigItem(
		keyName = "emblemTimer",
		name = "Emblem Trader Timer",
		description = "Enables/disables the timer when receiving a skull from the emblem trader."
	)
	default boolean ETCheck()
	{
		return true;
	}

	@ConfigItem(
		keyName = "pvpTimer",
		name = "PVP Timer",
		description = "Enables/disables the timer when receiving a skull PVP."
	)
	default boolean PKCheck()
	{
		return true;
	}
}
