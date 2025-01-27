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

import com.skulltimer.data.PlayerInteraction;
import com.skulltimer.data.TargetInteraction;
import com.skulltimer.enums.CombatStatus;
import com.skulltimer.enums.TimerDurations;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;

@Slf4j
public class CombatManager
{
	private final TimerManager timerManager;
	@Getter
	private final HashMap<String, Integer> attackerRecords;
	@Getter
	private final HashMap<String, TargetInteraction> targetRecords;
	@Getter
	private final HashMap<String, PlayerInteraction> interactionRecords;
	@Getter @Setter
	private boolean isPVPEnabled;

	public CombatManager(TimerManager timerManager, boolean isPVPEnabled)
	{
		this.timerManager = timerManager;
		this.attackerRecords = new HashMap<>();
		this.targetRecords = new HashMap<>();
		this.interactionRecords = new HashMap<>();
		this.isPVPEnabled = isPVPEnabled;
	}

	public void onAnimationOrInteractionChange(Player player, int currentTick, boolean isAnimation){
		if (player.getName() == null){
			return;
		}

		//if the attacker record already contains the player, update that instead
		if (attackerRecords.containsKey(player.getName())) {
			log.debug("Player {} already exists in attacker records. Updating existing record.", player.getName());
			attackerRecords.put(player.getName(), currentTick);
			return;
		}

		//if the interaction has already occurred, just update the interaction record
		PlayerInteraction interaction = interactionRecords.get(player.getName());

		if (interaction != null){
			if ((isAnimation && interaction.getTickNumberOfLastInteraction() == currentTick) ||
				(!isAnimation && interaction.getTickNumberOfLastAnimation() == currentTick)){

				//if the player attacking  has either previously retaliated and does not have a skull or is a new interaction, upgrade them
				TargetInteraction targetInteraction = targetRecords.get(player.getName());

				if (shouldUpdateTarget(targetInteraction, player)){
					log.debug("Player {} already exists in target records. {}", player.getName(), targetRecords.get(player.getName()).hasRetaliated() ? "" : "Updating record.");
					targetInteraction.setCombatStatus(CombatStatus.RETALIATED);
					targetInteraction.setHasPreviouslyRetaliated(true);
				} else if (shouldUpgradeToAttacker(player)){
					log.debug("Player {} exists in interaction records. Upgrading to attacker.", player.getName());
					attackerRecords.put(player.getName(), currentTick);
				}
			}
			interactionRecords.remove(player.getName());
		} else {
			log.debug("New interaction record created for player {}.", player.getName());
			interaction = new PlayerInteraction();
			interactionRecords.put(player.getName(), interaction);
			if (isAnimation){
				log.debug("isAnimation: (Name: {}).", player.getName());
				interaction.setAnimationTick(currentTick);
			} else {
				log.debug("isInteraction: (Name: {}).", player.getName());
				interaction.setInteractionTick(currentTick);
			}
		}
	}

	private boolean shouldUpgradeToAttacker(Player player){
		return player.getSkullIcon() != SkullIcon.NONE;
	}

	private boolean shouldUpdateTarget(TargetInteraction targetInteraction, Player player){
		return targetInteraction != null && ((targetInteraction.getCombatStatus() != CombatStatus.RETALIATED)
			|| targetInteraction.isHasPreviouslyRetaliated() && player.getSkullIcon() == SkullIcon.NONE);
	}

	public void onTargetHitsplat(Player player, Player localPlayer, int currentTick)
	{
		if (player.getName() == null){
			return;
		}

		if (!attackerRecords.containsKey(player.getName()) && !targetRecords.containsKey(player.getName())) {
			log.debug("Target record created for player {}.", player.getName());
			TargetInteraction targetInteraction = new TargetInteraction();
			targetInteraction.setTickNumberOfLastAttack(currentTick);
			targetRecords.put(player.getName(), targetInteraction);
			addTimerCheck(false);
		} else if (targetRecords.containsKey(player.getName()) && !targetRecords.get(player.getName()).hasRetaliated() && targetRecords.get(player.getName()).getCombatStatus() != CombatStatus.UNKNOWN){
			log.debug("Player {} has not retaliated. Starting timer.", player.getName());
			addTimerCheck(false);
		} else if (targetRecords.containsKey(player.getName()) && targetRecords.get(player.getName()).getCombatStatus() == CombatStatus.UNKNOWN && localPlayer.getSkullIcon() != SkullIcon.NONE) {
			log.debug("Player {} is unknown but {} has skull. Starting timer.", player.getName(), localPlayer.getName());
			addTimerCheck(true);
		}
	}

	private void addTimerCheck(boolean useCautious)
	{
		if (isPVPEnabled){
			timerManager.addTimer(TimerDurations.PVP_DURATION.getDuration(), useCautious);
		}
	}
}
