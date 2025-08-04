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

import com.skulltimer.SkullTimerConfig;
import com.skulltimer.SkulledTimer;
import java.time.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import com.skulltimer.SkullTimerPlugin;

/**
 * An object that is used to manage the creation and removal of a {@link SkulledTimer} object.
 */
@Slf4j
public class TimerManager
{
	private final SkullTimerConfig config;
	private final InfoBoxManager infoBoxManager;
	private final ItemManager itemManager;
	private final SkullTimerPlugin skullTimerPlugin;
	private final StatusManager statusManager;
	@Getter
	private SkulledTimer timer;

	/**
	 * The constructor for a {@link TimerManager} object.
	 * @param skullTimerPlugin The plugin object.
	 * @param config The configuration file for the {@link SkullTimerPlugin}.
	 * @param infoBoxManager Runelite's {@link InfoBoxManager} object.
	 * @param itemManager Runelite's {@link ItemManager} object.
	 * @param statusManager A manager for tracking the players skulled duration.
	 *
	 */
	public TimerManager(SkullTimerPlugin skullTimerPlugin, SkullTimerConfig config, InfoBoxManager infoBoxManager, ItemManager itemManager, StatusManager statusManager)
	{
		this.skullTimerPlugin = skullTimerPlugin;
		this.config = config;
		this.infoBoxManager = infoBoxManager;
		this.itemManager = itemManager;
		this.statusManager = statusManager;
	}

	/**
	 * A method that creates and adds a timer to the clients infobox. <p>
	 *
	 * If there is an existing timer, it is removed using {@code RemoveTimer}. Checks are also performed to ensure that any
	 * timer created is not negative or that the timer is zero.
	 *
	 * @param timerDuration The {@link Duration} of the timer to be created.
	 */
	public void addTimer(Duration timerDuration) throws IllegalArgumentException
	{
		if (shouldTimerBeUpdated(timerDuration))
		{
			//removes the timer if a timer is already created.
			removeTimer(false);

			if (!timerDuration.isNegative() && !timerDuration.isZero())
			{
				timer = new SkulledTimer(timerDuration, itemManager, config, skullTimerPlugin);

				statusManager.setTimerEndTime(timer.getEndTime());
				infoBoxManager.addInfoBox(timer);
				log.debug("Skull timer started with {} minutes remaining.", getTimer().getRemainingTime().toMinutes());
			}
		}
	}

	/**
	 * A method that removes any existing timer.
	 * @param saveConfig A {@link Boolean} to determine if duration of the existing timer should be saved.
	 *                   If the value passed is {@code true} then the remaining time will be saved in the config file. Otherwise if {@code false}
	 *                   then the existing config will be overwritten with a duration of 0 minutes.
	 */

	public void removeTimer(boolean saveConfig) throws IllegalArgumentException
	{
		// Check if timer has duration remaining (boolean), set timer accordingly
		if (saveConfig)
		{
			log.debug("Saving existing timer duration: {}.", timer.getRemainingTime());
			config.skullDuration(timer.getRemainingTime());
		}
		else
		{
			log.debug("Setting config duration to default.");
			config.skullDuration(Duration.ZERO);
		}

		infoBoxManager.removeIf(t -> t instanceof SkulledTimer);
		timer = null;
		log.debug("Removed skull duration timer.");
	}

	/**
	 * A method used to determine if a new timer should be created by checking to see if the existing timer is lower than the proposed timer.
	 * @param newDuration The new {@link Duration} to replace the existing timers' duration.
	 * @return Returns {@code true} if the new duration is greater than the existing timer or if {@code timer} is null. Returns {@code false} if the new duration is lower than or equal to the old duration.
	 */
	private boolean shouldTimerBeUpdated(Duration newDuration)
	{
		if (timer != null && timer.getRemainingTime().compareTo(newDuration) > 0)
		{
			log.debug("Existing timer {} exceeds the duration of the proposed new timer {}. The timer will not be updated.", timer.getRemainingTime(), newDuration);
			return false;
		}
		return true;
	}
}
