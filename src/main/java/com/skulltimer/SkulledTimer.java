package com.skulltimer;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import net.runelite.client.ui.overlay.infobox.Timer;

public class SkulledTimer extends Timer
{
	public SkulledTimer(Duration duration, BufferedImage image, SkullTimerPlugin plugin)
	{
		super(duration.toMillis(), ChronoUnit.MILLIS, image, plugin);
	}
}
