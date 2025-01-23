package com.skulltimer.managers;

import com.skulltimer.data.PlayerInteraction;
import com.skulltimer.enums.TimerDurations;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;

@Slf4j
public class CombatManager
{
	private final TimerManager timerManager;
	private final HashMap<Player, Integer> attackerRecords;
	private final HashMap<Player, Integer> targetRecords;
	private final HashMap<Player, PlayerInteraction> interactionRecords;

	@Getter @Setter
	private int tickLastHitsplatOccurredOn = 0;
	private final int combatTimeout = 17; //0.6 * 17 = 10.2 seconds


	public CombatManager(TimerManager timerManager)
	{
		this.timerManager = timerManager;
		this.attackerRecords = new HashMap<>();
		this.targetRecords = new HashMap<>();
		this.interactionRecords = new HashMap<>();
	}

	public boolean hasInteractedWithPlayer(Player player)
	{
		return attackerRecords.containsKey(player);
	}

	public void onAnimationOrInteractionChange(Player player, int currentTick, boolean isAnimation){
		//if the player is already a target, ignore
		if (targetRecords.containsKey(player)){
			log.debug("Player {} already exists in target records. Skipping.", player.getId());
			return;
		}

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
					log.debug("Player {} exists in interaction records. Upgrading to attacker.", player.getId());
					attackerRecords.put(player, currentTick);
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
		if (!attackerRecords.containsKey(player) && !targetRecords.containsKey(player)){
			log.debug("New target record created for player ID {}.", player.getId());
			targetRecords.put(player, currentTick);
			timerManager.addTimer(TimerDurations.PVP_DURATION.getDuration());
		} else if (!attackerRecords.containsKey(player)){
			log.debug("Existing target record updated for player ID {}.", player.getId());
			targetRecords.put(player, currentTick);
		}
	}

	public boolean isOutOfCombat(int currentTick)
	{
		if (tickLastHitsplatOccurredOn < 0){
			return true;
		}

		return currentTick - tickLastHitsplatOccurredOn >= combatTimeout;
	}

	public void cleanupRecords()
	{
		if (!targetRecords.isEmpty() || !interactionRecords.isEmpty() || !attackerRecords.isEmpty()){
			log.debug("Clearing records.");
			targetRecords.clear();
			interactionRecords.clear();
			attackerRecords.clear();
		}

	}
}
