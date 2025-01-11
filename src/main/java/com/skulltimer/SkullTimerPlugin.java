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
import com.skulltimer.managers.EquipmentManager;
import com.skulltimer.managers.LocationManager;
import com.skulltimer.managers.TimerManager;
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

	private EquipmentManager equipmentManager;
	private LocationManager locationManager;
	private TimerManager timerManager;

	@Override
	protected void startUp() throws Exception
	{
		timerManager = new TimerManager(this, config, infoBoxManager, itemManager);
		equipmentManager = new EquipmentManager(client);
		locationManager = new LocationManager(client);

		clientThread.invoke(() -> {
			equipmentManager.isWearingSkulledItem();
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		//save the timer when shutting down if it exists
		timerManager.removeTimer(timerManager.getTimer() != null);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) throws InterruptedException
	{
		//logging in - create timer
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			if (config.skullDuration() != null && timerManager.getTimer() == null) {
				timerManager.addTimer(config.skullDuration());
			}

			//sets the initial state of the equipment checker.
			equipmentManager.isWearingSkulledItem();

			//checks to see if the new location is in the abyss.
			if (locationManager.isInAbyss()) {
				timerManager.addTimer(TimerDurations.ABYSS_DURATION.getDuration());
			}
		}
		//logged out or hopping - stop timer
		else if ((gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.HOPPING) && timerManager.getTimer() != null)
		{
			log.debug("Skull timer paused with {} minutes remaining.", timerManager.getTimer().getRemainingTime().toMinutes());
			timerManager.removeTimer(true);
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
			timerManager.addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration());
		}
	}

	//removes the timer if it expires or the player looses the skull
	@Subscribe
	public void onGameTick(GameTick gameTickEvent)
	{
		//if the player does not have a skull icon or the timer has expired
		if (timerManager.getTimer() != null && (Instant.now().isAfter(timerManager.getTimer().getEndTime()) ||
			client.getLocalPlayer().getSkullIcon() == SkullIcon.NONE))
		{
			timerManager.removeTimer(false);
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		// checks to see if the changes made are to the equipment
		if (equipmentManager.getEquipment() != null &&
			itemContainerChanged.getItemContainer() == equipmentManager.getEquipment())
		{
			if (equipmentManager.hasEquipmentChanged() && client.getLocalPlayer().getSkullIcon() != SkullIcon.NONE) {
				timerManager.addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration());
			}
			//if the player has any skulled equipment on, and there is an existing timer
			else if (equipmentManager.isWearingSkulledItem() &&
				client.getLocalPlayer().getSkullIcon() != SkullIcon.NONE && timerManager.getTimer() != null) {
				log.debug("Removing timer as player has equipped a skulled item.");
				timerManager.removeTimer(false);
			}
		}
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged overheadTextChanged)
	{
		if (overheadTextChanged.getActor().getName() != null &&
			overheadTextChanged.getActor().getName().equalsIgnoreCase("Mage of Zamorak") &&
		 	overheadTextChanged.getOverheadText().equalsIgnoreCase("Veniens! Sallakar! Rinnesset!")){
			//sets one of the conditions to add the abyss timer.
			locationManager.setHasBeenTeleportedIntoAbyss(true);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (timerManager.getTimer() != null) {
			timerManager.addTimer(timerManager.getTimer().getRemainingTime());
		}
	}

	@Provides
	SkullTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkullTimerConfig.class);
	}
}
