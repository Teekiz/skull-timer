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
import com.skulltimer.data.PlayerInteraction;
import com.skulltimer.data.CombatInteraction;
import com.skulltimer.enums.CombatStatus;
import com.skulltimer.enums.TimerDurations;
import com.skulltimer.enums.equipment.AttackType;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import com.skulltimer.SkulledTimer;
import net.runelite.api.Client;
import net.runelite.api.GraphicID;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;

import static com.skulltimer.data.PlayerInteraction.defaultTickValue;

/**
 * An object that is used to manage combat scenarios to determine if a timer is required to be started.
 */
@Slf4j
public class CombatManager
{
	@Inject
	private final Client client;
	@Inject
	private final SkullTimerConfig config;
	private final TimerManager timerManager;
	@Getter
	private final HashMap<String, CombatInteraction> combatRecords;
	@Getter
	private final HashMap<String, PlayerInteraction> interactionRecords;
	/**
	 * The constructor for a {@link CombatManager} object.
	 * @param client Runelite's {@link Client} object.
	 * @param config The configuration file for the {@link SkullTimerPlugin}.
	 * @param timerManager The manager used to control the creation and deletion of {@link SkulledTimer} objects.
	 */
	public CombatManager(Client client, SkullTimerConfig config, TimerManager timerManager)
	{
		this.client = client;
		this.config = config;
		this.timerManager = timerManager;
		this.combatRecords = new HashMap<>();
		this.interactionRecords = new HashMap<>();
	}

	/**
	 * A method used to assign a {@code player} to the relevant record.
	 *
	 * <p>
	 * There are 3 records that a player could be assigned/upgraded to:
	 * <ol>
	 *     <li>If either an animation or interaction occurs on the same tick, a new {@link PlayerInteraction} record will be created and stored.</li>
	 *     <li>If an {@link PlayerInteraction} has already been created on the same {@code currentTick} value, the player can either be placed in either of the following records:</li>
	 *     <ol>
	 *         <li>If the player is a target but they have not retaliated, then their {@code TargetRecord} will be updated to reflect their {@code RETALIATED} status.</li>
	 *         <li>Otherwise if the player has either been a target but has logged out or they have a skull icon, then their record will be upgraded to attacker.</li>
	 *     </ol>
	 * </ol>
	 * </p>
	 *
	 * @param player The {@link Player} who has either interacted with animation has changed.
	 * @param currentTick The {@link Integer} value representing the current tick number.
	 * @param isAnimation {@code true} if the change was an animation. Otherwise {@code false}.
	 */
	public void onAnimationOrInteractionChange(Player player, int currentTick, boolean isAnimation){
		if (player == null || player.getName() == null || player.getName().isEmpty()){
			return;
		}

		//if the interaction has already occurred, just update the interaction record
		String playerName = player.getName();
		PlayerInteraction interaction = interactionRecords.get(playerName);

		if (interaction == null){
			log.debug("New interaction record created for player {}.", playerName);
			interaction = new PlayerInteraction();
			interactionRecords.put(playerName, interaction);
		}

		updateInteractionRecord(interaction, currentTick, isAnimation, playerName);

		if (interaction.hasInteractionAndAnimationOccurredOnTheSameTick()){
			onConfirmedInCombat(playerName);
		}
	}

	/**
	 * A method used to update the interaction record for {@code playerName}.
	 * @param playerInteraction The record to be updated.
	 * @param currentTick The tick number that the update is occurring on.
	 * @param isAnimation Whether the animation or interaction tick will be updated.
	 * @param playerName The name of the player associated with this record.
	 */
	private void updateInteractionRecord(PlayerInteraction playerInteraction, int currentTick, boolean isAnimation, String playerName)
	{
		if (playerInteraction == null){
			return;
		}

		log.debug("Updating {} record for: {}.", isAnimation ? "animation" : "interaction", playerName);

		if (isAnimation){
			playerInteraction.setAnimationTick(currentTick);
		} else {
			playerInteraction.setInteractionTick(currentTick);
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

		PlayerInteraction playerInteraction = interactionRecords.get(playerName);
		if (playerInteraction != null){
			playerInteraction.setTickNumberOfExpectedHit(defaultTickValue);
		}
		//interactionRecords.remove(playerName);
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
	 * @param localPlayer The {@link Player} who inflected the hitsplat.
	 * @param currentTick The {@link Integer} value representing the current tick number.
	 */
	public void onTargetHitsplat(Player player, Player localPlayer, int currentTick)
	{
		if (player.getName() == null || player.getName().isEmpty()){
			return;
		}

		if (!combatRecords.containsKey(player.getName())) {
			log.debug("Target record created for player {}.", player.getName());
			CombatInteraction combatInteraction = new CombatInteraction();
			combatInteraction.setTickNumberOfLastAttack(currentTick);
			combatRecords.put(player.getName(), combatInteraction);
			addTimerCheck(false);
			return;
		}

		CombatInteraction combatInteraction = combatRecords.get(player.getName());

		//if the player has died at some point, even if they had retaliated, start a new timer
		if (combatInteraction.getCombatStatus() == CombatStatus.DEAD){
			log.debug("Player {} was previously killed. Starting timer.", player.getName());
			combatInteraction.setCombatStatus(CombatStatus.ATTACKED);
			addTimerCheck(false);
		}

		//if the player has logged out at some point - the player can't attack them unless they retaliated
		else if (combatInteraction.getCombatStatus() == CombatStatus.LOGGED_OUT) {
			log.debug("Player {} was previously logged out. Starting timer.", player.getName());
			combatInteraction.setCombatStatus(CombatStatus.ATTACKED);
			addTimerCheck(false);
		}

		//if the player has moved away from the player, set the status to unknown, as the timer will now possibly be out of sync
		else if (combatInteraction.getCombatStatus() == CombatStatus.UNCERTAIN && localPlayer.getSkullIcon() != SkullIcon.NONE) {
			log.debug("Player {} is unknown but {} has skull. Starting timer.", player.getName(), localPlayer.getName());
			addTimerCheck(true);
		}

		//if the target has retaliated at any point during the fight, then a new timer will not be started
		else if (!combatInteraction.hasRetaliated()) {
			log.debug("Player {} has not retaliated. Starting timer.", player.getName());
			addTimerCheck(false);
		}

		else
		{
			log.debug("Timer will not be started. {}'s combat status: {}.", player.getName(), combatInteraction.getCombatStatus());
		}
	}

	/**
	 * A method that is used to check if a hitsplat or splash has occurred when it was expected to.
	 * @param currentTick The current tick number.
	 * @param expectedInteractions A map of interactions that were expected to occur on {@code currentTick}.
	 * @param didHitSplatOccur A boolean to determine if a hitsplat occurred.
	 */
	public void onTickOfExpectedHit(int currentTick, Map<String, PlayerInteraction> expectedInteractions, boolean didHitSplatOccur)
	{
		for (Map.Entry<String, PlayerInteraction> interactions : expectedInteractions.entrySet()){
			String playerName = interactions.getKey();
			PlayerInteraction playerInteraction = interactions.getValue();
			int expectedHit = playerInteraction.getTickNumberOfExpectedHit();
			boolean isSplashHit = playerInteraction.doesApplySplash() && client.getLocalPlayer().hasSpotAnim(GraphicID.SPLASH);

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
				interactionRecords.remove(playerName);
			}
		}
	}

	/**
	 * A method used to get the expected hits on a given tick.
	 * @param currentTick The number of the current tick.
	 * @return A {@link HashMap} of player names and {@link PlayerInteraction} of records that are below or equal to {@code currentTick} and are not set to default.
	 */
	public Map<String, PlayerInteraction> getExpectedHits(int currentTick)
	{
		Map<String, PlayerInteraction> expectedInteractions = new HashMap<>();
		for (Map.Entry<String, PlayerInteraction> entry : interactionRecords.entrySet()) {
			PlayerInteraction interaction = entry.getValue();
			if ((interaction.getTickNumberOfExpectedHit() == currentTick ||
				interaction.getTickNumberOfExpectedHit() == currentTick - 1)  &&
				interaction.getTickNumberOfExpectedHit() != defaultTickValue) {
				expectedInteractions.put(entry.getKey(), interaction);
			}
		}
		return expectedInteractions;
	}

	/**
	 * A method used to set the expected hit value when an attack occurs.
	 * @param playerName The name of the player who started the animation.
	 * @param expectedHitTick The tick number of when the attack can be expected to land.
	 * @param attackType The type of attack style the hit applied.
	 */
	public void setExpectedHitTick(String playerName, int expectedHitTick, AttackType attackType){
		PlayerInteraction interaction = interactionRecords.get(playerName);

		//this is so that faster weapons do not override the existing attacks.
		if (interaction == null || interaction.getTickNumberOfExpectedHit() != defaultTickValue) {
			return;
		}

		interaction.setExpectedHitTick(expectedHitTick);
		interaction.setAttackType(attackType);
	}

	/**
	 * A method to determine if a timer should be started.
	 * @param useCautious a {@link Boolean} value if the timer should be created as a {@code cautious} timer.
	 */
	private void addTimerCheck(boolean useCautious)
	{
		if (config.pvpToggle()){
			timerManager.addTimer(TimerDurations.PVP_DURATION.getDuration(), useCautious);
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
