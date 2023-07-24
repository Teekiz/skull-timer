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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@Slf4j
@PluginDescriptor(
	name = "Skull Timer"
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
		config.skullDuration(timer.getDuration());
		removeTimer();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) throws InterruptedException
	{
		//logging in - create timer
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			if (config.skullDuration() != null){
				addTimer(config.skullDuration());
			}
		}
		//connection lost - stop timer
		else if (gameStateChanged.getGameState() == GameState.CONNECTION_LOST && timer != null)
		{
			config.skullDuration(getDuration(Instant.now(), timer.getEndTime()));
			removeTimer();
		}

		//if the player is hopping - stop timer
		else if (gameStateChanged.getGameState() == GameState.HOPPING && timer != null)
		{
			config.skullDuration(getDuration(Instant.now(), timer.getEndTime()));
			removeTimer();
		}
	}

	//if the player talks to the emblem trader.
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
			removeTimer();
			config.skullDuration(null);
		}
	}

	public void addTimer(Duration timerDuration)
	{
		//removes the timer if a timer is already created.
		removeTimer();
		timer = new SkulledTimer(timerDuration, itemManager.getImage(ItemID.SKULL), this);
		timer.setTooltip("Time left until your character becomes unskulled.");
		infoBoxManager.addInfoBox(timer);
	}

	public void removeTimer()
	{
		infoBoxManager.removeIf(t -> t instanceof SkulledTimer);
		timer = null;
	}

	public Duration getDuration(Instant start, Instant end)
	{
		Duration duration = Duration.between(start, end);
		return duration;
	}

	@Provides
	SkullTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkullTimerConfig.class);
	}
}
