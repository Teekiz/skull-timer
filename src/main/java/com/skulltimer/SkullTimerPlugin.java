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
