package com.skulltimer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import net.runelite.client.ui.overlay.infobox.Timer;

public class SkulledTimer extends Timer
{
	private final Color textColour;
	private final Color warningColour;
	public SkulledTimer(Duration duration, BufferedImage image, SkullTimerPlugin plugin, Color textColour, Color warningColour)
	{
		super(duration.toMillis(), ChronoUnit.MILLIS, image, plugin);
		this.textColour = textColour;
		this.warningColour = warningColour;
	}

	public Duration getRemainingTime()
	{
		Duration remainingTime = Duration.between(Instant.now(), getEndTime());
		return remainingTime;
	}

	public Color getTextColor()
	{
		if (getRemainingTime().getSeconds() <= 30) {return warningColour;}
		else {return textColour;}
	}
}
