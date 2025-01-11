package com.skulltimer.managers;

import com.skulltimer.SkullTimerConfig;
import com.skulltimer.SkulledTimer;
import java.time.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import javax.inject.Inject;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import com.skulltimer.SkullTimerPlugin;

@Slf4j
public class TimerManager
{
	@Inject
	private final SkullTimerConfig config;
	@Inject
	private final InfoBoxManager infoBoxManager;
	@Inject
	private final ItemManager itemManager;
	@Inject
	private final SkullTimerPlugin skullTimerPlugin;
	@Getter
	private SkulledTimer timer;

	/**
	 * The constructor for a {@link TimerManager} object.
	 * @param skullTimerPlugin The plugin object.
	 * @param config The configuration file for the {@link SkullTimerPlugin}.
	 * @param infoBoxManager Runelite's {@link InfoBoxManager} object.
	 * @param itemManager Runelite's {@link ItemManager} object.
	 *
	 */
	public TimerManager(SkullTimerPlugin skullTimerPlugin, SkullTimerConfig config, InfoBoxManager infoBoxManager, ItemManager itemManager)
	{
		this.skullTimerPlugin = skullTimerPlugin;
		this.config = config;
		this.infoBoxManager = infoBoxManager;
		this.itemManager = itemManager;
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
		//removes the timer if a timer is already created.
		removeTimer(timer != null);

		if (!timerDuration.isNegative() && !timerDuration.isZero()) {
			timer = new SkulledTimer(timerDuration, itemManager.getImage(ItemID.SKULL), skullTimerPlugin, config.textColour(), config.warningTextColour());
			timer.setTooltip("Time left until your character becomes unskulled");
			infoBoxManager.addInfoBox(timer);
			log.debug("Skull timer started with {} minutes remaining.", getTimer().getRemainingTime().toMinutes());
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
		if (saveConfig) {
			config.skullDuration(timer.getRemainingTime());
		}
		else {
			config.skullDuration(Duration.ofMinutes(0));
		}

		infoBoxManager.removeIf(t -> t instanceof SkulledTimer);
		timer = null;
		log.debug("Removed skull duration timer.");
	}
}
