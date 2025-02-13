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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.Timer;

@Slf4j
public class SkulledTimer extends Timer
{
	@Inject
	ItemManager itemManager;
	@Inject
	SkullTimerConfig config;

	private final Color textColour;
	private final Color warningColour;
	private final boolean isCautious;

	public SkulledTimer(Duration duration, ItemManager itemManager, SkullTimerConfig skullTimerConfig, SkullTimerPlugin plugin, boolean isCautious)
	{
		super(duration.toMillis(), ChronoUnit.MILLIS, itemManager.getImage(ItemID.SKULL), plugin);
		this.itemManager = itemManager;
		this.config = skullTimerConfig;
		this.isCautious = isCautious;

		super.setImage(getTimerIcon());

		String tooltipText = "Time left until your character becomes unskulled.";

		if (isCautious)
		{
			this.textColour = config.textColourCautious();
			this.warningColour = config.warningTextColourCautious();
			tooltipText += "  WARNING: THIS TIMER MAY BE INACCURATE.";
		} else
		{
			this.textColour = config.textColour();
			this.warningColour = config.warningTextColour();
		}

		this.setTooltip(tooltipText);
	}

	public Duration getRemainingTime()
	{
		return Duration.between(Instant.now(), getEndTime());
	}

	public Color getTextColor()
	{
		if (getRemainingTime().getSeconds() <= 30)
		{
			return warningColour;
		}
		else
		{
			return textColour;
		}
	}

	public boolean isCautious()
	{
		return isCautious;
	}

	public BufferedImage getTimerIcon()
	{
		try (InputStream stream = SkulledTimer.class.getResourceAsStream("/timericon.png"))
		{
			if (stream == null)
			{
				log.debug("Stream is null, using default icon.");
				return itemManager.getImage(ItemID.SKULL);
			}
			return ImageIO.read(stream);
		} catch (IOException e)
		{
			log.debug("Cannot find timer icon, using default.");
			return itemManager.getImage(ItemID.SKULL);
		}
	}
}
