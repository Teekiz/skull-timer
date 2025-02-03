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
	@Inject
	private final Client client;
	private boolean doesPlayerHaveSkullIcon;
	private Instant skullIconStartTime;
	private final DateTimeFormatter dateTimeFormatter;
	@Setter
	private Instant timerEndTime;

	/**
	 * The constructor for a {@link StatusManager} object.
	 * @param client Runelite's {@link Client} object.
	 */
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
	private boolean doesPlayerCurrentlyHaveSkullIcon()
	{
		return client.getLocalPlayer().getSkullIcon() != SkullIcon.NONE;
	}

	/**
	 * A method used to keep track and log when a skull icon has started or expired.
	 */
	public void checkSkulledStatus()
	{
		//check if there has been a change in status.
		if (doesPlayerHaveSkullIcon == doesPlayerCurrentlyHaveSkullIcon()){
			return;
		}

		Instant now = Instant.now();

		if (doesPlayerCurrentlyHaveSkullIcon()){
			skullIconStartTime = now;
			log.debug("Skull icon has started: Start time: {}.", dateTimeFormatter.format(skullIconStartTime));
			doesPlayerHaveSkullIcon = true;
		} else if (skullIconStartTime != null){
			Duration skulledDuration = Duration.between(skullIconStartTime, now);
			long skulledDurationMinutes = skulledDuration.toMinutes();
			long skulledDurationSeconds = skulledDuration.toSeconds() % 60;

			log.debug("Skull icon has expired. Start time: {}. End time: {}. Duration {} minutes and {} seconds.",
				dateTimeFormatter.format(skullIconStartTime),
				dateTimeFormatter.format(now),
				skulledDurationMinutes,
				skulledDurationSeconds);

			if (timerEndTime != null){
				Duration timerExpiredDuration = Duration.between(timerEndTime, now);
				long timerExpiredMinutes = timerExpiredDuration.toMinutes();
				long timerExpiredSeconds = timerExpiredDuration.toSeconds() % 60;

				log.debug("Previous timer expired at {} ({} minutes and {} seconds before skulled status).",
					dateTimeFormatter.format(timerEndTime),
					timerExpiredMinutes,
					timerExpiredSeconds);
				timerEndTime = null;
			}

			doesPlayerHaveSkullIcon = false;
		} else {
			log.warn("Skull icon expired, but start time was null.");
		}
	}
}
