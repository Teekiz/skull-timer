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
import com.skulltimer.data.CombatInteraction;
import com.skulltimer.enums.CombatStatus;
import com.skulltimer.enums.TimerDurations;
import com.skulltimer.enums.equipment.AttackType;
import com.skulltimer.enums.equipment.ExcludedAnimations;
import com.skulltimer.enums.equipment.WeaponHitDelay;
import com.skulltimer.managers.CombatManager;
import com.skulltimer.managers.EquipmentManager;
import com.skulltimer.managers.LocationManager;
import com.skulltimer.managers.StatusManager;
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
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.kit.KitType;
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
	name = "Skulled Timer",
	description = "Displays a timer when your character receives a skulled status effect."
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
	private StatusManager statusManager;
	private int gameTickCounter;
	private boolean hasHitSplatOccurred;

	@Override
	protected void startUp() throws Exception
	{
		// Initialize the managers and set up the initial state of the plugin
		statusManager = new StatusManager(client);
		timerManager = new TimerManager(this, config, infoBoxManager, itemManager, statusManager);
		locationManager = new LocationManager(client, timerManager);
		equipmentManager = new EquipmentManager(client, timerManager, itemManager);
		combatManager = new CombatManager(client, config, timerManager);

		gameTickCounter = 0;
		hasHitSplatOccurred = false;

		// Update the current equipment when the plugin starts
		clientThread.invoke(() -> equipmentManager.updateCurrentEquipment());
	}

	@Override
	protected void shutDown() throws Exception
	{
		// Remove the timer, save duration remaining
		timerManager.removeTimer(timerManager.getTimer() != null);
	}

	/**
	 * This event is triggered if the player logs in/out, hops worlds, or is teleported to another location (e.g., the Abyss).
	 *
	 * @param gameStateChanged The event that indicates the game state has changed.
	 */
	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		// Check if the player is logged in
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			// If the skull duration is set, there is no active timer, and the player is not in the Abyss
			if (config.skullDuration() != null && timerManager.getTimer() == null && !locationManager.isInAbyss())
			{
				// Add timer with the SkullDuration from the config and set cautiousTimer
				timerManager.addTimer(config.skullDuration(), config.cautiousTimer());
			}
			// Update current equipment for equipmentManager
			equipmentManager.updateCurrentEquipment();
		}
		// Check if the player is on the login screen or hopping worlds
		else if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.HOPPING)
		{
			// Check if timerManager has a timer
			if (timerManager.getTimer() != null)
			{
				// Log the remaining time in minutes
				log.debug("Skull timer paused with {} minutes remaining.", timerManager.getTimer().getRemainingTime().toMinutes());
				// Save the remaining time and remove the timer
				timerManager.removeTimer(true);
			}
			// Clear combat records from combatManager
			combatManager.clearRecords();
		}
	}

	/**
	 * This event is triggered if the player talks to the Emblem Trader.
	 *
	 * @param chatMessage The event that contains the chat message.
	 */
	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		//check the message type and content
		if (chatMessage.getType() == ChatMessageType.MESBOX && (chatMessage.getMessage().equalsIgnoreCase("Your PK skull will now last for the full 20 minutes.") ||
		chatMessage.getMessage().equalsIgnoreCase("You are now skulled.")))
		{
			// Add a 20-minute timer when the player receives a skull from the Emblem Trader
			timerManager.addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
		}
	}

	/**
	 * This event is used to remove the skull timer should the icon expire.
	 *
	 * @param gameTick The event that indicates a game tick has occurred.
	 */
	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (combatManager.getAttackRecords().containsKey(gameTickCounter))
		{
			combatManager.onTickOfExpectedHit(gameTickCounter, hasHitSplatOccurred);
			hasHitSplatOccurred = false;
		}

		gameTickCounter++;
		statusManager.checkSkulledStatus();

		SkulledTimer skulledTimer = timerManager.getTimer();
		boolean playerHasNoSkullIcon = client.getLocalPlayer().getSkullIcon() == SkullIcon.NONE;

		if (skulledTimer == null)
		{
			return;
		}

		//if the player does not have a skull icon or the timer has expired
		if (Instant.now().isAfter(skulledTimer.getEndTime()))
		{
			log.debug("Removing timer because it has expired. {}", playerHasNoSkullIcon  ? "Player no longer has a skull icon." : "Player still has a skull icon.");
		}
		else if (playerHasNoSkullIcon)
		{
			log.debug("Removing timer because player no longer has a skull icon. Time remaining: {} seconds.", skulledTimer.getRemainingTime().toSeconds());
		}
		else
		{
			return;
		}
		// Remove the timer and reset the cautious timer config
		timerManager.removeTimer(false);
		config.cautiousTimer(false);
	}

	/**
	 * This event is used for item checks.
	 *
	 * @param itemContainerChanged The event that indicates an item container has changed.
	 */
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		// checks to see if the changes made are to the equipment
		if (equipmentManager.getEquipment() != null && itemContainerChanged.getItemContainer() == equipmentManager.getEquipment() &&
			!equipmentManager.getModifiedItemSlotChanges().isEmpty())
		{
			if (client.getLocalPlayer().getSkullIcon() != SkullIcon.NONE)
			{
				equipmentManager.shouldTimerBeStarted(equipmentManager.getModifiedItemSlotChanges());
			}
		}
	}

	/**
	 * This event is used to confirm if the player has been teleported to the Abyss.
	 *
	 * @param overheadTextChanged The event that indicates overhead text has changed.
	 */
	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged overheadTextChanged)
	{
		if (overheadTextChanged.getActor().getName() != null &&
			overheadTextChanged.getActor().getName().equalsIgnoreCase("Mage of Zamorak") &&
		 	overheadTextChanged.getOverheadText().equalsIgnoreCase("Veniens! Sallakar! Rinnesset!"))
		{
			//sets one of the conditions to add the abyss timer.
			locationManager.setHasBeenTeleportedIntoAbyss(true);
		}
	}

	/**
	 * This event is triggered when an interaction changes in PVP scenarios
	 *
	 * @param interactingChanged The event that indicates an interaction has changed.
	 */
	@Subscribe
	public void onInteractingChanged(InteractingChanged interactingChanged)
	{
		//if the player is not in the wilderness then skip
		if (!locationManager.isInWilderness())
		{
			return;
		}

		Actor target = interactingChanged.getTarget();
		Actor source = interactingChanged.getSource();

		if (!(source instanceof Player))
		{
			return;
		}

		String sourceName = source.getName();

		boolean isTargetLocalPlayer = target instanceof Player && target.getName() != null &&
			target.getName().equalsIgnoreCase(client.getLocalPlayer().getName());

		combatManager.onPlayerInteractionChange(sourceName, isTargetLocalPlayer);
	}

	/**
	 * This event is triggered when a hitsplat is applied to an actor.
	 *
	 * @param hitsplatApplied The event that indicates a hitsplat has been applied.
	 */
	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		//if the local player is not in the wilderness or if the player hit is the local player
		if (!locationManager.isInWilderness() || hitsplatApplied.getActor() == null ||
			hitsplatApplied.getActor().getName() == null || !(hitsplatApplied.getActor() instanceof Player))
		{
			return;
		}

		Player playerHit = (Player) hitsplatApplied.getActor();
		Player localPlayer = client.getLocalPlayer();

		if (playerHit.getName().equalsIgnoreCase(localPlayer.getName()))
		{
			hasHitSplatOccurred = true;
		}
		//if the player attacks a player in the wilderness, and they have a skull icon
		else if (hitsplatApplied.getHitsplat().isMine() && localPlayer.getSkullIcon() != SkullIcon.NONE)
		{
			combatManager.onTargetHitsplat(playerHit, localPlayer, gameTickCounter);
		}
	}

	/**
	 * This event is triggered when an animation changes for an actor.
	 *
	 * @param animationChanged The event that indicates an animation has changed.
	 */
	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged)
	{
		Actor actor = animationChanged.getActor();

		if (!locationManager.isInWilderness() || !(actor instanceof Player) || actor.getName() == null ||
			actor.getName().equalsIgnoreCase(client.getLocalPlayer().getName()) || actor.getAnimation() == ExcludedAnimations.IDLE.getId())
		{
			return;
		}

		Player player = (Player) actor;
		int animationID = player.getAnimation();

		if (ExcludedAnimations.isExcluded(animationID))
		{
			log.debug("Animation is excluded. Ending animation processing.");
			return;
		}

		int distance = locationManager.calculateDistanceBetweenPlayers(client.getLocalPlayer(), player);
		int weaponID = player.getPlayerComposition().getEquipmentId(KitType.WEAPON);
		WeaponHitDelay weaponHitDelay = equipmentManager.getWeaponHitDelay(weaponID, animationID);

		if (weaponHitDelay == null)
		{
			log.warn("Weapon {} does not exist in weapons table.", weaponID);
		} else
		{
			int hitDelay = weaponHitDelay.calculateHitDelay(distance);
			AttackType attackType = weaponHitDelay.getAttackType();

			if (combatManager.addExpectedHitTick(player.getName(), gameTickCounter + hitDelay, attackType))
			{
				log.debug("Player {} has attacked using weapon {}. Distance {} with a hit delay of {} (current tick: {}, attack type: {}). Attack has been recorded.", player.getName(), weaponID, distance, hitDelay, gameTickCounter, attackType);
			}
		}
	}

	/**
	 * This event is triggered when a player despawns.
	 *
	 * @param playerDespawned The event that indicates a player has despawned.
	 */
	@Subscribe
	public void onPlayerDespawned(PlayerDespawned playerDespawned)
	{
		Player player = playerDespawned.getPlayer();

		if (player == null || player.getName() == null || !combatManager.getCombatRecords().containsKey(player.getName()))
		{
			return;
		}

		String playerName = player.getName();
		CombatInteraction combatInteraction = combatManager.getCombatRecords().get(playerName);

		if (combatInteraction.getCombatStatus() == CombatStatus.DEAD)
		{
			log.debug("Player {} despawned. Target has been set to dead status.", playerName);
		}
		else if (combatInteraction.hasRetaliated())
		{
			log.debug("Player {} was in combat. Target has been set to inactive.", playerName);
			combatInteraction.setCombatStatus(CombatStatus.INACTIVE);
		}
		else if (locationManager.hasPlayerLoggedOut(player))
		{
			log.debug("Player {} has logged out. Target has been set to logged out.", playerName);
			combatInteraction.setCombatStatus(CombatStatus.LOGGED_OUT);
		}
		else
		{
			log.debug("Player {} combat status set to unknown.", playerName);
			combatInteraction.setCombatStatus(CombatStatus.UNCERTAIN);
		}
	}

	/**
	 * This event is triggered when an actor dies.
	 *
	 * @param actorDeath The event that indicates an actor has died.
	 */
	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		if (actorDeath.getActor() instanceof Player && actorDeath.getActor().getName() != null)
		{
			String playerName = actorDeath.getActor().getName();
			//if the local player is the one who is killed, then remove all attacker logs (as this is reset)
			if (playerName.equalsIgnoreCase(client.getLocalPlayer().getName()))
			{
				log.debug("Player {} has died, resetting combat records.", playerName);
				combatManager.clearRecords();
			//if the player has killed their target, update their status
			}
			else if (combatManager.getCombatRecords().containsKey(playerName))
			{
				log.debug("Player {} has died, updating combat status to dead.", playerName);
				CombatInteraction combatInteraction = combatManager.getCombatRecords().get(playerName);
				if (combatInteraction != null)
				{
					combatInteraction.setCombatStatus(CombatStatus.DEAD);
				}
			}
		}
	}

	/**
	 * This event is triggered when the configuration changes.
	 *
	 * @param configChanged The event that indicates the configuration has changed.
	 */
	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (timerManager.getTimer() != null && client.getGameState() == GameState.LOGGED_IN)
		{
			timerManager.addTimer(timerManager.getTimer().getRemainingTime(), config.cautiousTimer());
		}
	}

	/**
	 * Provides the configuration for the Skull Timer plugin.
	 *
	 * @param configManager The configuration manager.
	 * @return The Skull Timer configuration.
	 */
	@Provides
	SkullTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkullTimerConfig.class);
	}
}
