/*
 * Copyright (c) 2023, Callum Rossiter
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
		name = "General Settings",
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

	@ConfigSection(
		position = 1,
		name = "PVP Settings",
		description = "Settings that affect PVP timers only."
	)
	String experimental = "experimental";

	@ConfigItem(
		keyName = "pvpToggle",
		name="Enable PVP timer",
		description = "Toggles the plugin to track PVP skulls. Please note, this timer may not always be 100% accurate.",
		section = experimental
	)
	default boolean pvpToggle() {return true;}
}