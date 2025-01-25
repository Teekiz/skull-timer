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
import com.skulltimer.managers.CombatManager;
import com.skulltimer.managers.EquipmentManager;
import com.skulltimer.managers.LocationManager;
import com.skulltimer.managers.TimerManager;
import java.time.Instant;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
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
	private CombatManager combatManager;
	private int gameTick;

	@Override
	protected void startUp() throws Exception
	{
		timerManager = new TimerManager(this, config, infoBoxManager, itemManager);
		locationManager = new LocationManager(client, timerManager);
		equipmentManager = new EquipmentManager(client, timerManager);
		combatManager = new CombatManager(timerManager);
		gameTick = 0;

		clientThread.invoke(() -> {

			equipmentManager.updateCurrentEquipment();
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		//save the timer when shutting down if it exists
		timerManager.removeTimer(timerManager.getTimer() != null);
	}

	/*
		This event if the player logs in/out or is teleported to another location (e.g. the Abyss).
 	*/
	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) throws InterruptedException
	{
		//logging in - create timer
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			//if the player has just logged in and is not in the abyss (teleporting into the abyss will cause the game state to change - therefore the timer is handled directly)
			if (config.skullDuration() != null && timerManager.getTimer() == null && !locationManager.isInAbyss()) {
				timerManager.addTimer(config.skullDuration());
			}
			//sets the initial state of the equipment checker.
			equipmentManager.updateCurrentEquipment();
		}
		//logged out or hopping - stop timer
		else if ((gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.HOPPING) && timerManager.getTimer() != null)
		{
			log.debug("Skull timer paused with {} minutes remaining.", timerManager.getTimer().getRemainingTime().toMinutes());
			timerManager.removeTimer(true);
			combatManager.getAttackerRecords().clear();
		}
	}

	/*
		This event if the player talks to the Emblem Trader.
 	*/
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

	/*
		This event is used to remove the skull timer should the icon expire.
 	*/
	@Subscribe
	public void onGameTick(GameTick gameTickEvent)
	{
		gameTick++;

		//if the player does not have a skull icon or the timer has expired
		if (timerManager.getTimer() != null && (Instant.now().isAfter(timerManager.getTimer().getEndTime()) ||
			client.getLocalPlayer().getSkullIcon() == SkullIcon.NONE))
		{
			timerManager.removeTimer(false);
		}
	}

	/*
		This event is used for item checks.
	 */
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		// checks to see if the changes made are to the equipment
		if (equipmentManager.getEquipment() != null &&
			itemContainerChanged.getItemContainer() == equipmentManager.getEquipment() &&
			!equipmentManager.getModifiedItemSlotChanges().isEmpty())
		{
			if (client.getLocalPlayer().getSkullIcon() != SkullIcon.NONE){
				equipmentManager.shouldTimerBeStarted(equipmentManager.getModifiedItemSlotChanges());
			}
			//if the player has any skulled equipment on, and there is an existing timer
			else if (client.getLocalPlayer().getSkullIcon() != SkullIcon.NONE && timerManager.getTimer() != null) {
				log.debug("Removing timer as player has equipped a skulled item.");
				timerManager.removeTimer(false);
			}
		}
	}

	/*
		This event is used to confirm if the player has been teleported to the abyss.
	 */
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

	/*
		PVP Events - Interaction then animation
	 */
	@Subscribe
	public void onInteractingChanged(InteractingChanged interactingChanged)
	{
		//if the player is not in the wilderness then skip
		if (!locationManager.isInWilderness()){
			return;
		}

		Actor target = interactingChanged.getTarget();
		Actor source = interactingChanged.getSource();

		//todo - something and null might be autoretaliate

		//if the player has been attacked/interacted with
		if (target instanceof Player && source instanceof Player
			&& target.getName() != null && target.getName().equalsIgnoreCase(client.getLocalPlayer().getName())){
			combatManager.onAnimationOrInteractionChange((Player) source, gameTick, false);
		}
	}

	//check if there is an interaction within the last 10 seconds, if there isn't and the next hitsplat is mine, assume that its attacked

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		//if the local player is not in the wilderness or if the player hit is the local player
		if (!locationManager.isInWilderness() || hitsplatApplied.getActor().getName() != null &&
			hitsplatApplied.getActor().getName().equalsIgnoreCase(client.getLocalPlayer().getName())){
			return;
		}

		//if the player attacks a player in the wilderness, and they have a skull icon
		if (hitsplatApplied.getHitsplat().isMine() && hitsplatApplied.getActor() instanceof Player
			&& client.getLocalPlayer().getSkullIcon() != SkullIcon.NONE){
			combatManager.onTargetHitsplat((Player) hitsplatApplied.getActor(), gameTick);
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged)
	{
		if (!locationManager.isInWilderness() || animationChanged.getActor().getAnimation() == -1){
			return;
		}

		if (animationChanged.getActor() != null &&
			animationChanged.getActor() instanceof Player &&
			((Player) animationChanged.getActor()).getId() != client.getLocalPlayer().getId())
		{
			combatManager.onAnimationOrInteractionChange((Player) animationChanged.getActor(), gameTick, true);
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
