package com.skulltimer.managers;

import com.skulltimer.mocks.TimerMocks;
import com.skulltimer.data.TargetInteraction;
import com.skulltimer.enums.CombatStatus;
import com.skulltimer.enums.TimerDurations;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CombatManagerTests extends TimerMocks
{
	@Mock
	Player player;
	@Mock
	Player localPlayer;

	CombatManager combatManager;
	int tickCounter;


	@BeforeEach
	public void startUp()
	{
		tickCounter = 0;
		when(player.getName()).thenReturn("PlayerOne");
		combatManager = new CombatManager(timerManager, true);
	}

	@Test
	public void playerIsNull()
	{
		when(player.getName()).thenReturn(null);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer()
	{
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithMultipleHits()
	{
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(2)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithRetaliatedAttack()
	{
		//initial attack
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		//simulated response
		combatManager.onAnimationOrInteractionChange(player, tickCounter, false);
		combatManager.onAnimationOrInteractionChange(player, tickCounter, true);
		//final attack
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		//because the player attacked back, don't restart timer
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
	}

	@Test
	public void testUnprovokedAttackOnLocalPlayer()
	{
		when(player.getSkullIcon()).thenReturn(SkullIcon.SKULL);
		combatManager.onAnimationOrInteractionChange(player, tickCounter, false);
		combatManager.onAnimationOrInteractionChange(player, tickCounter, true);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		//because the player attacked back, don't restart timer
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
		assertEquals(1, combatManager.getAttackerRecords().size());
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithPlayerBeingSetToCautious()
	{
		TargetInteraction interaction = new TargetInteraction();
		interaction.setCombatStatus(CombatStatus.UNKNOWN);
		combatManager.getTargetRecords().put("PlayerOne", interaction);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), true);
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithPlayerBeingSetToCautious_WithLocalPlayerNotSkulled()
	{
		when(localPlayer.getSkullIcon()).thenReturn(SkullIcon.NONE);

		TargetInteraction interaction = new TargetInteraction();
		interaction.setCombatStatus(CombatStatus.UNKNOWN);
		combatManager.getTargetRecords().put("PlayerOne", interaction);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration(), true);
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithPlayerInteractionAndNoAnimation()
	{
		combatManager.onAnimationOrInteractionChange(player, tickCounter, false);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		//because the player attacked back, don't restart timer
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
		assertEquals(0, combatManager.getAttackerRecords().size());
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithPlayerAnimationAndNoInteraction()
	{
		combatManager.onAnimationOrInteractionChange(player, tickCounter, true);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		//because the player attacked back, don't restart timer
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
		assertEquals(0, combatManager.getAttackerRecords().size());
	}

	@Test
	public void PlayerWithInteractionAndAnimationOnDifferentTicks()
	{
		combatManager.onAnimationOrInteractionChange(player, tickCounter++, false);
		combatManager.onAnimationOrInteractionChange(player, tickCounter++, true);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		combatManager.onAnimationOrInteractionChange(player, tickCounter++, true);
		combatManager.onAnimationOrInteractionChange(player, tickCounter++, false);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		//because the player attacked back, don't restart timer
		verify(timerManager, times(2)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
		assertEquals(0, combatManager.getAttackerRecords().size());
	}

	@Test
	public void PVPDisabledTest()
	{
		combatManager.setPVPEnabled(false);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
		assertEquals(1, combatManager.getTargetRecords().size());
		combatManager.setPVPEnabled(true);
	}

	@Test
	public void targetHasDied()
	{
		TargetInteraction interaction = new TargetInteraction();
		interaction.setCombatStatus(CombatStatus.DEAD);
		combatManager.getTargetRecords().put("PlayerOne", interaction);

		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
	}

	@Test
	public void targetHasDied_PreviouslyRetaliated()
	{
		TargetInteraction interaction = new TargetInteraction();
		interaction.setCombatStatus(CombatStatus.DEAD);
		combatManager.getTargetRecords().put("PlayerOne", interaction);

		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);

		assertFalse(interaction.hasRetaliated());
	}
}
