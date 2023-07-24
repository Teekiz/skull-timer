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
import net.runelite.api.Player;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
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
	private final Duration durationPlayer = Duration.ofMinutes(30);
	private String lastAttackedPlayer = "";

	@Override
	protected void shutDown() throws Exception
	{
		removeTimer();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChangedEvent)
	{
		if (gameStateChangedEvent.getGameState().equals(GameState.LOGGED_IN) && client.getLocalPlayer().getSkullIcon() != null)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "[Skull Timer] Cannot determine skull duration left. Please re-skull to enable timer.", null);
		}
	}

	//if the player talks to the emblem trader.
	@Subscribe
	public void onChatMessage(ChatMessage messageEvent)
	{
		if (messageEvent.getType() != ChatMessageType.MESBOX || !config.ETCheck())
		{
			return;
		}
		if (messageEvent.getMessage().equalsIgnoreCase("Your PK skull will now last for the full 20 minutes.") ||
		messageEvent.getMessage().equalsIgnoreCase("You are now skulled."))
		{
			addTimer(durationTrader);
		}
	}

	//if the player attacks another player
	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatAppliedEvent)
	{
		if (!config.PKCheck())
		{
			return;
		}

		if (hitsplatAppliedEvent.getActor() instanceof Player && !hitsplatAppliedEvent.getActor().getName().equalsIgnoreCase(client.getLocalPlayer().getName()) &&
			client.getLocalPlayer().getSkullIcon() != null)
		{
			if (!lastAttackedPlayer.equalsIgnoreCase(hitsplatAppliedEvent.getActor().getName()))
			{
				addTimer(durationPlayer);
				lastAttackedPlayer = hitsplatAppliedEvent.getActor().getName();
			}
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
	}



	@Provides
	SkullTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkullTimerConfig.class);
	}
}
