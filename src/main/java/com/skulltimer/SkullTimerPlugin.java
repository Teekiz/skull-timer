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

import com.google.inject.Provides;
import com.skulltimer.enums.TimerDurations;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.SkullIcon;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.ItemContainer;
import net.runelite.api.InventoryID;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@Slf4j
@PluginDescriptor(
	name = "Emblem Trader Skull Timer",
	description = "Displays a timer when your character receives a skull from the emblem trader."
)
public class SkullTimerPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private SkullTimerConfig config;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private ItemManager itemManager;
	private SkulledTimer timer;
	private EquipmentChecker equipmentChecker;
	private LocationChecker locationChecker;
	private final InventoryID equipment = InventoryID.EQUIPMENT;

	@Override
	protected void startUp() throws Exception
	{
		equipmentChecker = new EquipmentChecker();
		locationChecker = new LocationChecker(client);

		clientThread.invoke(() -> {
			equipmentChecker.isWearingSkulledItem(client.getItemContainer(equipment));
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		//save the timer when shutting down if it exists
		removeTimer(timer != null);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) throws InterruptedException
	{
		//logging in - create timer
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN && config.skullDuration() != null && timer == null)
		{
			//todo - delete
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", locationChecker.isInWilderness() ? "yes" : "no", null);
			addTimer(config.skullDuration());
			log.debug("Skull timer started with {} minutes remaining.", timer.getRemainingTime().toMinutes());
		} else if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			//sets the initial state of the equipment checker.
			//todo - delete
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", locationChecker.isInWilderness() ? "yes" : "no", null);
			equipmentChecker.isWearingSkulledItem(client.getItemContainer(equipment));
		}
		//logged out or hopping - stop timer
		else if ((gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.HOPPING) && timer != null)
		{
			log.debug("Skull timer paused with {} minutes remaining.", timer.getRemainingTime().toMinutes());
			removeTimer(true);
		}
	}

	//if the player talks to the emblem trader they will receive this message.
	@Subscribe
	public void onChatMessage(ChatMessage messageEvent)
	{
		//check the message type and content
		if (messageEvent.getType() == ChatMessageType.MESBOX && (messageEvent.getMessage().equalsIgnoreCase("Your PK skull will now last for the full 20 minutes.") ||
		messageEvent.getMessage().equalsIgnoreCase("You are now skulled.")))
		{
			addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration());
		}
	}

	//removes the timer if it expires or the player looses the skull
	@Subscribe
	public void onGameTick(GameTick gameTickEvent)
	{
		//if the player does not have a skull icon or the timer has expired
		if (timer != null && (Instant.now().isAfter(timer.getEndTime()) || client.getLocalPlayer().getSkullIcon() == SkullIcon.NONE))
		{
			removeTimer(false);
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		// checks to see if the changes made are to the equipment
		if (client.getItemContainer(equipment) != null &&
			itemContainerChanged.getItemContainer() == client.getItemContainer(equipment))
		{
			ItemContainer equipmentContainer = client.getItemContainer(equipment);

			if (equipmentChecker.hasEquipmentChanged(equipmentContainer) && client.getLocalPlayer().getSkullIcon() != SkullIcon.NONE)
			{
				addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration());
			}
			//if the player has any skulled equipment on, and there is an existing timer
			else if (equipmentChecker.isWearingSkulledItem(equipmentContainer) &&
				client.getLocalPlayer().getSkullIcon() != SkullIcon.NONE && timer != null) {
				log.debug("Removing timer as player has equipped a skulled item.");
				removeTimer(false);
			}
		}
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged overheadTextChanged)
	{
		if (overheadTextChanged.getActor().getName().equalsIgnoreCase("Mage of Zamorak")){
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "test", overheadTextChanged.getOverheadText(), null);
			//todo - delete
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", locationChecker.isInWilderness() ? "yes" : "no", null);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (timer != null) {addTimer(timer.getRemainingTime());}
	}

	/**
	 * A method that creates and adds a timer to the clients infobox. <p>
	 *
	 * If there is an existing timer, it is removed using {@code RemoveTimer}. Checks are also performed to ensure that any
	 * timer created is not negative or that the timer is zero.
	 *
	 * @param timerDuration The {@link Duration} of the timer to be created.
	 */
	private void addTimer(Duration timerDuration) throws IllegalArgumentException
	{
		//removes the timer if a timer is already created.
		removeTimer(timer != null);

		if (!timerDuration.isNegative() && !timerDuration.isZero()) {
			timer = new SkulledTimer(timerDuration, itemManager.getImage(ItemID.SKULL), this, config.textColour(), config.warningTextColour());
			timer.setTooltip("Time left until your character becomes unskulled");
			infoBoxManager.addInfoBox(timer);
			log.debug("Created skull duration timer.");
		}
	}

	/**
	 * A method that removes any existing timer.
	 * @param saveConfig A {@link Boolean} to determine if duration of the existing timer should be saved.
	 *                   If the value passed is {@code true} then the remaining time will be saved in the config file. Otherwise if {@code false}
	 *                   then the existing config will be overwritten with a duration of 0 minutes.
	 */

	private void removeTimer(boolean saveConfig) throws IllegalArgumentException
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

	@Provides
	SkullTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkullTimerConfig.class);
	}
}
