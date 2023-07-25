/*
* Copyright (c) 2023, Callum Rossiter

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
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
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			if (config.skullDuration() != null && timer == null){
				addTimer(config.skullDuration());
			}
		}

		//logged out or hopping - stop timer
		else if ((gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.HOPPING) && timer != null)
		{
			removeTimer(true);
		}
	}

	//if the player talks to the emblem trader they will receive this message.
	@Subscribe
	public void onChatMessage(ChatMessage messageEvent)
	{
		if (messageEvent.getType() != ChatMessageType.MESBOX)
		{
			return;
		}

		if (messageEvent.getMessage().equalsIgnoreCase("Your PK skull will now last for the full 20 minutes.") ||
		messageEvent.getMessage().equalsIgnoreCase("You are now skulled."))
		{
			addTimer(durationTrader);
		}
	}

	//removes the timer if it expires or the player looses the skull
	@Subscribe
	public void onGameTick(GameTick gameTickEvent)
	{
		if (timer == null) {return;}

		//if the player does not have a skull icon or the timer has expired
		if (Instant.now().isAfter(timer.getEndTime()) || client.getLocalPlayer().getSkullIcon() == null)
		{
			removeTimer(false);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (timer != null) {addTimer(timer.getRemainingTime());}
	}

	public void addTimer(Duration timerDuration)
	{
		//removes the timer if a timer is already created.
		removeTimer(timer != null);
		timer = new SkulledTimer(timerDuration, itemManager.getImage(ItemID.SKULL), this, config.textColour(), config.warningTextColour());
		timer.setTooltip("Time left until your character becomes unskulled.");
		infoBoxManager.addInfoBox(timer);
	}

	public void removeTimer(boolean saveConfig)
	{
		if (saveConfig) {config.skullDuration(timer.getRemainingTime());}
		else {config.skullDuration(null);}
		infoBoxManager.removeIf(t -> t instanceof SkulledTimer);
		timer = null;
	}

	@Provides
	SkullTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkullTimerConfig.class);
	}
}
