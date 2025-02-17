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

package com.skulltimer.managers;

import com.skulltimer.SkullTimerConfig;
import com.skulltimer.SkullTimerPlugin;
import com.skulltimer.data.ExpectedHit;
import com.skulltimer.data.CombatInteraction;
import com.skulltimer.enums.CombatStatus;
import com.skulltimer.enums.TimerDurations;
import com.skulltimer.enums.equipment.AttackType;
import com.skulltimer.enums.equipment.WeaponHitDelay;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import com.skulltimer.SkulledTimer;
import net.runelite.api.Client;
import net.runelite.api.GraphicID;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import net.runelite.client.callback.ClientThread;

/**
 * An object that is used to manage combat scenarios to determine if a timer is required to be started.
 */
@Slf4j
public class CombatManager
{
	@Inject
	private final Client client;
	@Inject
	private final ClientThread clientThread;
	@Inject
	private final SkullTimerConfig config;
	private final TimerManager timerManager;
	private final StatusManager statusManager;
	private final EquipmentManager equipmentManager;
	@Getter
	private final HashMap<String, CombatInteraction> combatRecords;
	@Getter
	private final HashSet<String> interactionRecords;
	@Getter
	private final HashMap<Integer, Set<ExpectedHit>> attackRecords;
	/**
	 * The constructor for a {@link CombatManager} object.
	 * @param client Runelite's {@link Client} object.
	 * @param clientThread Runelite's {@link ClientThread} object.
	 * @param config The configuration file for the {@link SkullTimerPlugin}.
	 * @param timerManager The manager used to control the creation and deletion of {@link SkulledTimer} objects.
	 * @param statusManager The manager used to manage the plugins interaction with the player characters status.
	 * @param equipmentManager The manager used to manage events related to the players equipment.
	 *
	 */
	public CombatManager(Client client, ClientThread clientThread, SkullTimerConfig config, TimerManager timerManager, StatusManager statusManager, EquipmentManager equipmentManager)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.config = config;
		this.timerManager = timerManager;
		this.statusManager = statusManager;
		this.equipmentManager = equipmentManager;
		this.combatRecords = new HashMap<>();
		this.interactionRecords = new HashSet<>();
		this.attackRecords = new HashMap<>();
	}

	/**
	 * A method to add/remove a record of any player who interacts with the local player.
	 * @param playerName The name of the player to add to the record.
	 * @param addPlayer A boolean to check if the player should be added or removed from the record.
	 */
	public void onPlayerInteractionChange(String playerName, boolean addPlayer)
	{
		if (addPlayer && interactionRecords.add(playerName)){
			log.debug("Adding {} to interaction records.", playerName);
		} else if (!addPlayer && interactionRecords.remove(playerName)){
			log.debug("Removing {} from interaction records.", playerName);
		}
	}

	/**
	 * A method that is used to update a players {@link CombatInteraction} in the {@code combatRecords}.
	 * @param playerName The name of the player who had this interaction.
	 */
	public void onConfirmedInCombat(String playerName)
	{
		CombatInteraction combatInteraction = combatRecords.get(playerName);

		//if the player does not already exist in the records, create a new record.
		if (combatInteraction == null){
			combatInteraction = new CombatInteraction();
			combatInteraction.setCombatStatus(CombatStatus.ATTACKER);
			combatRecords.put(playerName, combatInteraction);
		}

		//if the interaction is from a player who was in the target records, and they haven't previously responded, update their record
		if (shouldSetStatusToRetaliated(combatInteraction)){
			log.debug("Player {} already exists in target records. Updating target record to retaliated.", playerName);
			combatInteraction.setCombatStatus(CombatStatus.RETALIATED);
			//if the player is not a target then check if they should be classified as an attacker
		} else if (shouldSetStatusToAttacker(combatInteraction)){
			log.debug("Player {} exists in interaction records. Upgrading to attacker.", playerName);
			combatInteraction.setCombatStatus(CombatStatus.ATTACKER);
		}
	}

	/**
	 * A method to determine if the player meets the requirements to be considered an attacker.
	 * @param combatInteraction The {@link CombatInteraction} record if it exists.
	 * @return {@code true} if the player exists in {@code targetRecords} and their {@link CombatStatus} is {@code LOGGED_OUT}. Otherwise, returns {@code false}.
	 */
	private boolean shouldSetStatusToAttacker(CombatInteraction combatInteraction){
		//if the player had previously logged out, then
		return combatInteraction != null && combatInteraction.getCombatStatus() == CombatStatus.LOGGED_OUT;
	}

	/**
	 * A method to determine if the player meets the requirements to be set to the retaliated status.
	 * @param combatInteraction The {@link CombatInteraction} record if it exists.
	 * @return {@code true} if the {@code combatInteraction} is not {@code null} and their {@link CombatStatus} has not been set to {@code RETALIATED}.
	 */
	private boolean shouldSetStatusToRetaliated(CombatInteraction combatInteraction){
		//if the player the local player is targeting has a target interaction, and they have yet to be upgraded
		return combatInteraction != null && !combatInteraction.hasRetaliated();
	}

	/**
	 * A method to determine if the requirements are met to start a timer when the local player attacks another player.
	 *
	 * <p>
	 * The following are possible states the interaction could be in to determine if a timer should be started:
	 * <ol>
	 *     <li>The {@code player} does not exist in either the {@code attackerRecords} or {@code targetRecords}.</li>
	 *     <li>The {@code player} exists in the {@code targetRecords} and their {@link CombatStatus} is {@code DEAD}, {@code LOGGED_OUT} or variant of {@code RETALIATED}.</li>
	 *     <li>The {@code player} {@link CombatStatus} is a variant of {@code UNKNOWN} and the {@code localPlayer}'s {@link SkullIcon} is not {@code NONE}.</li>
	 * </ol>
	 * </p>
	 * @param player The {@link Player} who the hitsplat has been applied to.
	 * @param currentTick The {@link Integer} value representing the current tick number.
	 */
	public void onTargetHitsplat(Player player, int currentTick)
	{
		if (player.getName() == null || player.getName().isEmpty()){
			return;
		}

		String playerName = player.getName();

		if (!combatRecords.containsKey(playerName)) {
			log.debug("Target record created for player {}.", playerName);
			CombatInteraction combatInteraction = new CombatInteraction();
			combatRecords.put(player.getName(), combatInteraction);
			addTimerCheck();
			return;
		}

		CombatInteraction combatInteraction = combatRecords.get(playerName);

		//if the player has died at some point, even if they had retaliated, start a new timer
		if (combatInteraction.getCombatStatus() == CombatStatus.DEAD){
			log.debug("Player {} was previously killed. Starting timer.", playerName);
			combatInteraction.setCombatStatus(CombatStatus.ATTACKED);
			addTimerCheck();
		}

		//if the player has logged out at some point - the player can't attack them unless they retaliated
		else if (combatInteraction.getCombatStatus() == CombatStatus.LOGGED_OUT) {
			log.debug("Player {} was previously logged out. Starting timer.", playerName);
			combatInteraction.setCombatStatus(CombatStatus.ATTACKED);
			addTimerCheck();
		}

		//if the player has their status as uncertain or inactive, perform a further check.
		else if (combatInteraction.getCombatStatus() == CombatStatus.UNCERTAIN || combatInteraction.getCombatStatus() == CombatStatus.INACTIVE) {
			clientThread.invokeAtTickEnd(() -> onUnknownOrInactiveStatus(playerName, combatInteraction, currentTick));
		}

		//if the target has retaliated at any point during the fight, then a new timer will not be started
		else if (!combatInteraction.hasRetaliated()) {
			log.debug("Player {} has not retaliated. Starting timer.", playerName);
			addTimerCheck();
		}
		else
		{
			log.debug("Timer will not be started. {}'s combat status: {}.", playerName, combatInteraction.getCombatStatus());
		}
	}

	/**
	 * A method to try to identify what a players combat status should be set to if possible.
	 * If the local player doesn't have a skull after attacking the target player, the player's combat status is set to retaliated.
	 * If the local player does have a skull which started within the previous possible attack range, the players combat status is set to attacked and a timer is started.
	 * Otherwise, it is difficult to verify the players remaining time and therefore the timer may be inaccurate.
	 * @param targetPlayerName The name of the target player.
	 * @param combatInteraction The combat record associated with the player.
	 * @param currentTick The current tick number.
	 */
	private void onUnknownOrInactiveStatus(String targetPlayerName, CombatInteraction combatInteraction, int currentTick)
	{
		if (combatInteraction == null || currentTick == 0)
		{
			return;
		}

		Player localPlayer = client.getLocalPlayer();

		WeaponHitDelay weaponHitDelay = equipmentManager.getWeaponHitDelay(localPlayer);

		if (weaponHitDelay == null)
		{
			return;
		}

		int worstCaseDistance = 10;
		int worstCaseHitDelay = weaponHitDelay.calculateHitDelay(worstCaseDistance);
		int skullStatusStartTime = statusManager.getSkullIconTickStartTime();

		//checks to ensure that the hit occurred within the longest reasonable time a hit could have occurred.
		boolean isStartTimeWithinWorstCaseHitDelay = (skullStatusStartTime >= (currentTick - worstCaseHitDelay - 1)
			&& skullStatusStartTime <= currentTick);

		log.debug("Player has skull icon: {}. Is within worse case hit delay: {}. Worst case hit delay: {}. skullStatusStartTime: {}. Current tick: {}.",
			statusManager.doesPlayerCurrentlyHaveSkullIcon(), isStartTimeWithinWorstCaseHitDelay, worstCaseHitDelay, skullStatusStartTime, currentTick);

		//if player does not have skulled status - the player be considered retaliated - do not start timer.
		if (!statusManager.doesPlayerCurrentlyHaveSkullIcon())
		{
			log.debug("Player does not have skull. Setting {} status to retaliated.", targetPlayerName);
			combatInteraction.setCombatStatus(CombatStatus.RETALIATED);
		}
		//if the player does have skull status that started within worstCaseHitDelay - the player should be set to 'attacked' and timer started.
		else if (isStartTimeWithinWorstCaseHitDelay)
		{
			log.debug("Skull status started within expected time. Setting {} status to attacked.", targetPlayerName);
			combatInteraction.setCombatStatus(CombatStatus.ATTACKED);
			addTimerCheck();
		}
		//if the player has a skull, but the range is before then it's difficult to verify.
		else
		{
			//the previous combat status is used to determine what has the highest likelihood of being correct.
			CombatStatus combatStatus = combatInteraction.getCombatStatus();
			if (combatStatus == CombatStatus.INACTIVE) 
			{
				log.debug("Cannot verify {}'s status. Not starting timer but remaining inactive.", targetPlayerName);
			}
			else if (combatStatus == CombatStatus.UNCERTAIN)
			{
				log.debug("Cannot verify {}'s status. Starting timer but remaining uncertain.", targetPlayerName);
				addTimerCheck();
			}
		}
	}

	/**
	 * A method that is used to check if a hitsplat or splash has occurred when it was expected to.
	 * @param currentTick The current tick number.
	 * @param didHitSplatOccur A boolean to determine if a hitsplat occurred.
	 */
	public void onTickOfExpectedHit(int currentTick, boolean didHitSplatOccur)
	{
		for (Map.Entry<Integer, Set<ExpectedHit>> tick : getExpectedHits(currentTick).entrySet()){
			for (ExpectedHit hit : tick.getValue()){
				String playerName = hit.getPlayerName();
				int expectedHit = tick.getKey();
				boolean isSplashHit = hit.doesApplySplash() && client.getLocalPlayer().hasSpotAnim(GraphicID.SPLASH);

				//If the hit occurred either now or one tick late (because of the processing order delay), the attack will count as an attack
				if (didHitSplatOccur) {
					log.debug("Expected hit for player {} has occurred (current tick: {})", playerName, currentTick);
					onConfirmedInCombat(playerName);
				}
				//If there was not a hit, but the attack was magic based and splash was applied (and it was still within the expected time), this will also count
				else if (isSplashHit) {
					log.debug("Expected splash for player {} has occurred. (current tick: {}).", playerName, currentTick);
					onConfirmedInCombat(playerName);
				}
				//Due to PID delay, the attack can be delayed, so waiting an extra tick extra prevents premature deletion.
				else if (expectedHit == currentTick){
					log.debug("Expected hit for player {} did not occur (Expected: {} Current: {}).", playerName, expectedHit, currentTick);
				}
				//Remove the record.
				else {
					log.debug("Expected hit for player {} did not occur (Expected: {} Current: {}). Removing record.", playerName, expectedHit, currentTick);
					attackRecords.get(currentTick - 1).remove(hit);
				}
			}
		}
	}

	/**
	 * A method used to set the expected hit value when an attack occurs.
	 * @param playerName The name of the player who started the animation.
	 * @param expectedHitTick The tick number of when the attack can be expected to land.
	 * @param attackType The type of attack style the hit applied.
	 * @return {@code true} if the record was added successfully. Returns {@code false} if the interaction record does not contain the {@code playerName}.
	 */
	public boolean addExpectedHitTick(String playerName, int expectedHitTick, AttackType attackType)
	{
		if (interactionRecords.contains(playerName))
		{
			ExpectedHit expectedHit = new ExpectedHit(playerName, attackType);
			attackRecords.computeIfAbsent(expectedHitTick, r -> new HashSet<>()).add(expectedHit);
			return true;
		}
		return false;
	}

	/**
	 * A method used to get the expected hits on a given tick.
	 * @param currentTick The number of the current tick.
	 * @return A {@link HashMap} containing all {@link ExpectedHit} expected within the {@code currentTick} or {@code currentTick - 1}.
	 */
	private HashMap<Integer, Set<ExpectedHit>> getExpectedHits(int currentTick)
	{
		return attackRecords.entrySet().stream()
			.filter(entry -> entry.getKey() == currentTick || entry.getKey() == currentTick - 1)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, HashMap::new));
	}

	/**
	 * A method to determine if a timer should be started.
	 */
	private void addTimerCheck()
	{
		if (config.pvpToggle()){
			timerManager.addTimer(TimerDurations.PVP_DURATION.getDuration());
		}
	}

	/**
	 * A method used to clear the combat records of people who attacked the local player.
	 */
	public void clearRecords()
	{
		log.debug("Clearing records.");
		combatRecords.clear();
		interactionRecords.clear();
	}
}
