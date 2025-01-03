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
import net.runelite.api.Item;
import net.runelite.api.EquipmentInventorySlot;
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
	private SkullTimerConfig config;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private ItemManager itemManager;
	private SkulledTimer timer;
	private final Duration durationTrader = Duration.ofMinutes(20);

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
			addTimer(config.skullDuration());
			log.debug("Skull timer started with {} minutes remaining.", timer.getRemainingTime().toMinutes());
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
			addTimer(durationTrader);
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
		// Check for amulet of avarice
		checkAmuletOfAvarice();
	}

	private void checkAmuletOfAvarice()
	{
		// Get the player's equipment
		final ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

		// Ensure the equipment is not null (e.g., during loading screens)
		if (equipment == null)
		{
			return;
		}

		// Check if amulet is being worn and if its amulet of avarice
		final Item amulet = equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx());
		if (amulet != null && amulet.getId() == ItemID.AMULET_OF_AVARICE) {
			// Check if timer is not null
			if (timer != null) {
				removeTimer(true);
			}

			// Ensure the player remains skulled while the amulet is worn
			if (client.getLocalPlayer().getSkullIcon() != SkullIcon.SKULL) {
				log.debug("Player remains skulled due to wearing the Amulet of Avarice.");
			}
		}

		// If amulet of avarice is unequipped, start a 20-minute timer
		if (timer == null)
		{
			log.debug("Amulet of Avarice unequipped. Starting 20-minute skull timer.");
			addTimer(Duration.ofMinutes(20));
		}
	}


	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (timer != null) {addTimer(timer.getRemainingTime());}
	}

	public void addTimer(Duration timerDuration) throws IllegalArgumentException
	{
		//removes the timer if a timer is already created.
		removeTimer(timer != null);
		timer = new SkulledTimer(timerDuration, itemManager.getImage(ItemID.SKULL), this, config.textColour(), config.warningTextColour());
		timer.setTooltip("Time left until your character becomes unskulled");
		infoBoxManager.addInfoBox(timer);
		log.debug("Created skull duration timer.");
	}

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

	@Provides
	SkullTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkullTimerConfig.class);
	}
}
