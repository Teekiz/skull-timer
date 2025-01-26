package com.skulltimer;

import java.awt.Color;
import java.time.Duration;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("Skull Timer")
public interface SkullTimerConfig extends Config
{
	@ConfigSection(
		position = 0,
		name = "Settings",
		description = "Standard timer configuration settings."
	)
	String settings = "settings";

	@ConfigItem(
		keyName = "textColour",
		name="Text Colour",
		description = "The colour of the countdown text displayed on the timer.",
		section = settings
	)
	default Color textColour() {return Color.WHITE;}

	@ConfigItem(
		keyName = "warningTextColour",
		name="Warning Text Colour",
		description = "The colour of the countdown text displayed on the timer when 30 seconds or less is left on the timer.",
		section = settings
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

	@ConfigItem(
		keyName = "cautiousTimer",
		name = "",
		description = "",
		hidden = true
	)
	boolean cautiousTimer();

	@ConfigItem(
		keyName = "cautiousTimer",
		name = "",
		description = ""
	)
	void cautiousTimer(boolean cautiousTimer);

	@ConfigSection(
		position = 1,
		name = "Experimental",
		description = "Configuration for the timer that may not be fully accurate."
	)
	String experimental = "experimental";

	@ConfigItem(
		keyName = "pvpToggle",
		name="Enable PVP timer",
		description = "Toggles the timer to track PVP skulls.",
		section = experimental
	)
	default boolean pvpToggle() {return true;}

	@ConfigItem(
		keyName = "cautiousTimerToggle",
		name="Enable cautious timer",
		description = "Toggles whether the timer should appear differently if the plugin is unable to determine if the timer is accurate.",
		section = experimental
	)
	default boolean cautiousTimerToggle() {return true;}

	@ConfigItem(
		keyName = "textColourCautious",
		name="Text Colour (Cautious)",
		description = "The colour of the countdown text displayed on the timer when the timer is unsure of its accuracy.",
		section = experimental
	)
	default Color textColourCautious() {return Color.YELLOW;}

	@ConfigItem(
		keyName = "warningTextColourCautious",
		name="Warning Text Colour (Cautious)",
		description = "The colour of the countdown text displayed on the timer when the timer is unsure of its accuracy.",
		section = experimental
	)
	default Color warningTextColourCautious() {return Color.magenta;}
}