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

package com.skulltimer.managers;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.SkullIcon;

/**
 * A manager to keep track of when a skull icon is created/removed. This object is primarily created for debugging purposes.
 */
@Slf4j
public class StatusManager
{
	private final Client client;
	private boolean doesPlayerHaveSkullIcon;
	private Instant skullIconStartTime;

	@Getter
	private int skullIconTickStartTime;
	@Setter
	private Instant timerEndTime;

	private final DateTimeFormatter dateTimeFormatter;

	/**
	 * The constructor for a {@link StatusManager} object.
	 * @param client Runelite's {@link Client} object.
	 */
	@Inject
	public StatusManager(Client client)
	{
		this.client = client;
		this.doesPlayerHaveSkullIcon = false;
		this.dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneOffset.UTC);
	}

	/**
	 * A method used to check the players current status at the moment.
	 * @return {@code true} if the player does have a skull icon, otherwise {@code false}.
	 */
	public boolean doesPlayerCurrentlyHaveSkullIcon()
	{
		return client.getLocalPlayer().getSkullIcon() != SkullIcon.NONE;
	}

	/**
	 * A method used to keep track and log when a skull icon has started or expired.
	 * @param currentTick The {@link Integer} value representing the current tick number.
	 */
	public void checkSkulledStatus(int currentTick)
	{
		//check if there has been a change in status.
		if (doesPlayerHaveSkullIcon == doesPlayerCurrentlyHaveSkullIcon())
		{
			return;
		}

		Instant now = Instant.now();

		if (doesPlayerCurrentlyHaveSkullIcon())
		{
			skullIconStartTime = now;
			skullIconTickStartTime = currentTick;
			log.debug("Skull icon has started: Start time: {} (tick number: {}).", dateTimeFormatter.format(skullIconStartTime), currentTick);
			doesPlayerHaveSkullIcon = true;
		}
		else if (skullIconStartTime != null)
		{
			Duration skulledDuration = Duration.between(skullIconStartTime, now);
			long skulledDurationMinutes = skulledDuration.toMinutes();
			long skulledDurationSeconds = skulledDuration.toSeconds() % 60;

			log.debug("Skull icon has expired. Start time: {}. End time: {}. Duration {} minutes and {} seconds.(tick number: {}).",
				dateTimeFormatter.format(skullIconStartTime),
				dateTimeFormatter.format(now),
				skulledDurationMinutes,
				skulledDurationSeconds,
				currentTick);

			if (timerEndTime != null)
			{
				Duration timerExpiredDuration = Duration.between(timerEndTime, now);
				long timerExpiredMinutes = timerExpiredDuration.toMinutes();
				long timerExpiredSeconds = timerExpiredDuration.toSeconds() % 60;
				int tickDuration = currentTick - skullIconTickStartTime;

				log.debug("Previous timer expired at {} ({} minutes and {} seconds before skulled status. Tick duration: {}).",
					dateTimeFormatter.format(timerEndTime),
					timerExpiredMinutes,
					timerExpiredSeconds,
					tickDuration);
				timerEndTime = null;
			}

			skullIconTickStartTime = 0;
			doesPlayerHaveSkullIcon = false;
		} else {
			log.warn("Skull icon expired, but start time was null.");
		}
	}
}
