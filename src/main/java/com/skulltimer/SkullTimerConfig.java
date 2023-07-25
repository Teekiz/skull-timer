package com.skulltimer;

import java.awt.Color;
import java.time.Duration;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Skull Timer")
public interface SkullTimerConfig extends Config
{
	@ConfigItem(
		keyName = "textColour",
		name="Text Colour",
		description = "The colour of the countdown text displayed on the timer."
	)
	default Color textColour() {return Color.WHITE;}

	@ConfigItem(
		keyName = "warningTextColour",
		name="Warning Text Colour",
		description = "The colour of the countdown text displayed on the timer when 30 seconds or less is left on the timer."
	)
	default Color warningTextColour() {return Color.RED;}

	@ConfigItem(
		keyName = "skullDuration",
		name = "",
		description = "",
		hidden = true
	)
	Duration skullDuration();

	@ConfigItem(
		keyName = "skullDuration",
		name = "",
		description = ""
	)
	void skullDuration(Duration skullDuration);
}