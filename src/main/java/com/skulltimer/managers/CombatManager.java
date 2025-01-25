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
import com.skulltimer.enums.TimerDurations;
import java.util.HashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;

@Slf4j
public class CombatManager
{
	private final TimerManager timerManager;
	@Getter
	private final HashMap<Player, Integer> attackerRecords;
	@Getter
	private final HashMap<Player, TargetInteraction> targetRecords;
	@Getter
	private final HashMap<Player, PlayerInteraction> interactionRecords;

	public CombatManager(TimerManager timerManager)
	{
		this.timerManager = timerManager;
		this.attackerRecords = new HashMap<>();
		this.targetRecords = new HashMap<>();
		this.interactionRecords = new HashMap<>();
	}

	public void onAnimationOrInteractionChange(Player player, int currentTick, boolean isAnimation){
		//if the attacker record already contains the player, update that instead
		if (attackerRecords.containsKey(player)) {
			log.debug("Player {} already exists in attacker records. Updating existing record.", player.getId());
			attackerRecords.put(player, currentTick);
			return;
		}

		//if the interaction has already occurred, just update the interaction record
		PlayerInteraction interaction = interactionRecords.get(player);

		if (interaction != null){
			if ((isAnimation && interaction.getTickNumberOfLastInteraction() == currentTick) ||
				(!isAnimation && interaction.getTickNumberOfLastAnimation() == currentTick)){
				if (targetRecords.containsKey(player)){
					log.debug("Player {} already exists in target records. {}", player.getId(), targetRecords.get(player).isHasRetaliated() ? "" : "Updating record.");
					targetRecords.get(player).setHasRetaliated(true);
				} else if (player.getSkullIcon() != SkullIcon.NONE){
					log.debug("Player {} exists in interaction records. Upgrading to attacker.", player.getId());
					attackerRecords.put(player, currentTick);
				}
			}
			interactionRecords.remove(player);
		} else {
			log.debug("New interaction record created for player ID {}.", player.getId());
			interaction = new PlayerInteraction();
			interactionRecords.put(player, interaction);
			if (isAnimation){
				log.debug("isAnimation: (ID: {}).", player.getId());
				interaction.setAnimationTick(currentTick);
			} else {
				log.debug("isInteraction: (ID: {}).", player.getId());
				interaction.setInteractionTick(currentTick);
			}
		}
	}

	public void onTargetHitsplat(Player player, int currentTick)
	{
		if (!attackerRecords.containsKey(player) && !targetRecords.containsKey(player)) {
			log.debug("Target record created for player ID {}.", player.getId());
			TargetInteraction targetInteraction = new TargetInteraction();
			targetInteraction.setTickNumberOfLastAttack(currentTick);
			targetRecords.put(player, targetInteraction);
			timerManager.addTimer(TimerDurations.PVP_DURATION.getDuration());
		} else if (targetRecords.containsKey(player) && !targetRecords.get(player).isHasRetaliated()){
			log.debug("Player ID {} has not retaliated. Starting timer.", player.getId());
			timerManager.addTimer(TimerDurations.PVP_DURATION.getDuration());
		}
	}
}
