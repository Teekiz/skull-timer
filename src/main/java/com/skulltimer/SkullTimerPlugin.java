package com.skulltimer;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

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

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	//teleported
	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		//10 min
		if (gameStateChanged.getGameState() == GameState.LOADING){
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "player load", null);
		}
	}

	//pked
	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		//if the hitsplat is a player and the player has a skullicon
		if (hitsplatApplied.getActor() instanceof Player && client.getLocalPlayer().getSkullIcon() != null){
			//set time to 30 mins
		}
	}

	//cape and amulet removed
	@Subscribe
	public void onMenuOptionClicked(MenuEntry menuEntry)
	{
		//if the hitsplat is a player and the player has a skullicon
		if (hitsplatApplied.getActor() instanceof Player && client.getLocalPlayer().getSkullIcon() != null){
			//set time to 30 mins
		}
	}


	@Provides
	SkullTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkullTimerConfig.class);
	}
}
