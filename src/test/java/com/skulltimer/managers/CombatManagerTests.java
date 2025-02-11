package com.skulltimer.managers;

import com.skulltimer.data.CombatInteraction;
import com.skulltimer.data.ExpectedHit;
import com.skulltimer.data.PlayerInteraction;
import com.skulltimer.enums.equipment.AttackType;
import com.skulltimer.mocks.TimerMocks;
import com.skulltimer.enums.CombatStatus;
import com.skulltimer.enums.TimerDurations;
import java.util.HashMap;
import net.runelite.api.GraphicID;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

	HashMap<String, PlayerInteraction> expectedInteractions;

	PlayerInteraction playerInteraction;
	CombatInteraction combatInteraction;

	@Spy
	@InjectMocks
	CombatManager combatManager;

	int tickCounter = 0;


	@BeforeEach
	public void startUp()
	{
		when(player.getName()).thenReturn("PlayerOne");
		expectedInteractions = new HashMap<>();
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
		when(config.pvpToggle()).thenReturn(true);

		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithMultipleHits()
	{
		when(config.pvpToggle()).thenReturn(true);

		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(2)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithRetaliatedAttack()
	{
		when(config.pvpToggle()).thenReturn(true);

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
		combatManager.onAnimationOrInteractionChange(player, tickCounter, false);
		combatManager.onAnimationOrInteractionChange(player, tickCounter, true);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		//because the player attacked back, don't restart timer
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
		assertEquals(CombatStatus.ATTACKER, combatManager.getCombatRecords().get("PlayerOne").getCombatStatus());
	}

	@Test
	public void testUnprovokedAttackOnLocalPlayer_PlayerHadLoggedOut()
	{
		combatInteraction = new CombatInteraction();
		combatInteraction.setCombatStatus(CombatStatus.LOGGED_OUT);
		combatManager.getCombatRecords().put("PlayerOne", combatInteraction);

		combatManager.onAnimationOrInteractionChange(player, tickCounter, false);
		combatManager.onAnimationOrInteractionChange(player, tickCounter, true);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		//because the player had logged out, their attacking record would have been reset, causing them to become the aggressor.
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithPlayerBeingSetToCautious()
	{
		when(config.pvpToggle()).thenReturn(true);

		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.UNCERTAIN);
		combatManager.getCombatRecords().put("PlayerOne", interaction);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), true);
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithPlayerBeingSetToCautious_WithLocalPlayerNotSkulled()
	{
		when(localPlayer.getSkullIcon()).thenReturn(SkullIcon.NONE);

		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.UNCERTAIN);
		combatManager.getCombatRecords().put("PlayerOne", interaction);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration(), true);
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithPlayerInteractionAndNoAnimation()
	{
		when(config.pvpToggle()).thenReturn(true);

		combatManager.onAnimationOrInteractionChange(player, tickCounter, false);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		//because the player didn't attack back, restart timer
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
		assertEquals(0, combatManager.getCombatRecords().values().stream().filter(entry -> entry.getCombatStatus() == CombatStatus.ATTACKER).count());
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithPlayerAnimationAndNoInteraction()
	{
		when(config.pvpToggle()).thenReturn(true);

		combatManager.onAnimationOrInteractionChange(player, tickCounter, true);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		//because the player didn't attack back, restart timer
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
		assertEquals(0, combatManager.getCombatRecords().values().stream().filter(entry -> entry.getCombatStatus() == CombatStatus.ATTACKER).count());
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_PlayerHadLoggedOut_WithNoRetaliation()
	{
		when(config.pvpToggle()).thenReturn(true);

		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.LOGGED_OUT);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		//because the player attacked back, don't restart timer
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_PlayerHadLoggedOut_WithRetaliation()
	{
		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.INACTIVE);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
	}

	@Test
	public void PlayerWithInteractionAndAnimationOnDifferentTicks()
	{
		when(config.pvpToggle()).thenReturn(true);

		combatManager.onAnimationOrInteractionChange(player, tickCounter++, false);
		combatManager.onAnimationOrInteractionChange(player, tickCounter++, true);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		combatManager.onAnimationOrInteractionChange(player, tickCounter++, true);
		combatManager.onAnimationOrInteractionChange(player, tickCounter++, false);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		//because the player attacked back, don't restart timer
		verify(timerManager, times(2)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
		assertEquals(0, combatManager.getCombatRecords().values().stream().filter(entry -> entry.getCombatStatus() == CombatStatus.ATTACKER).count());
	}

	@Test
	public void PVPDisabledTest()
	{
		when(config.pvpToggle()).thenReturn(false);
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
		assertEquals(1, combatManager.getCombatRecords().size());
	}

	@Test
	public void targetHasDied()
	{
		when(config.pvpToggle()).thenReturn(true);

		combatInteraction = new CombatInteraction();
		combatInteraction.setCombatStatus(CombatStatus.DEAD);
		combatManager.getCombatRecords().put("PlayerOne", combatInteraction);

		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);
	}

	@Test
	public void targetHasDied_PreviouslyRetaliated()
	{
		when(config.pvpToggle()).thenReturn(true);

		combatInteraction = new CombatInteraction();
		combatInteraction.setCombatStatus(CombatStatus.DEAD);
		combatManager.getCombatRecords().put("PlayerOne", combatInteraction);

		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration(), false);

		assertFalse(combatInteraction.hasRetaliated());
	}

	@Test
	public void unprovokedAttack_ThenRetaliated_WithDefenceAnimation()
	{
		//initial attack
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);
		combatManager.onAnimationOrInteractionChange(player, tickCounter++, true);
		//simulated response
		combatManager.onAnimationOrInteractionChange(player, tickCounter, false);
		combatManager.onAnimationOrInteractionChange(player, tickCounter, true);
		//final attack
		combatManager.onTargetHitsplat(player, localPlayer, tickCounter++);

		assertEquals(CombatStatus.RETALIATED, combatManager.getCombatRecords().get("PlayerOne").getCombatStatus());
	}

	@Test
	public void addExpectedHitTick_WithNullRecord()
	{
		combatManager.addExpectedHitTick(player.getName(), 2, AttackType.OTHER);
	}

	@Test
	public void addExpectedHitTick_WithExistingRecord()
	{
		combatManager.addExpectedHitTick("New Player", 2, AttackType.MAGIC);
		ExpectedHit expectedHit = new ExpectedHit(player.getName(), AttackType.MELEE);
		combatManager.getInteractionRecords().put(player.getName(), playerInteraction);
		combatManager.addExpectedHitTick(player.getName(), 2, AttackType.MELEE);
		assertTrue(combatManager.getAttackRecords().get(2).contains(expectedHit));
	}

	@Test
	public void onTickOfExpectedHit_TickNumberLowerThanCurrentTick()
	{
		combatManager.addExpectedHitTick(player.getName(), 2, AttackType.MELEE);
		combatManager.onTickOfExpectedHit(3, true);
		assertEquals(1, combatManager.getCombatRecords().size());
	}

	@Test
	public void onTickOfExpectedHit_TickNumberEqualToCurrentTick()
	{
		combatManager.addExpectedHitTick(player.getName(), 3, AttackType.MELEE);
		combatManager.onTickOfExpectedHit(3, true);

		assertEquals(1, combatManager.getCombatRecords().size());
		verify(combatManager, times(1)).onConfirmedInCombat(player.getName());
	}

	@Test
	public void onTickOfExpectedHit_TickNumberGreaterThanCurrentTick()
	{
		combatManager.addExpectedHitTick(player.getName(), 4, AttackType.MELEE);
		assertEquals(1, combatManager.getAttackRecords().get(4).size());
		verify(combatManager, times(0)).onConfirmedInCombat(player.getName());
	}

	@Test
	public void onTickOfExpectedHitSplat_NoHit()
	{
		combatManager.addExpectedHitTick(player.getName(), 3, AttackType.MELEE);
		combatManager.onTickOfExpectedHit(3, false);

		assertEquals(1, combatManager.getAttackRecords().get(3).size());
		verify(combatManager, times(0)).onConfirmedInCombat(player.getName());
	}

	@Test
	public void onTickOfExpectedHitSplat_NoHit_WithMagicAttack_NoSplash()
	{
		combatManager.addExpectedHitTick(player.getName(), 3, AttackType.MAGIC);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.hasSpotAnim(GraphicID.SPLASH)).thenReturn(false);

		expectedInteractions.put(player.getName(), playerInteraction);
		combatManager.onTickOfExpectedHit(3, false);

		assertEquals(1, combatManager.getAttackRecords().get(3).size());
		verify(combatManager, times(0)).onConfirmedInCombat(player.getName());
	}

	@Test
	public void onTickOfExpectedHitSplat_NoHit_WithMagicAttack_WithSplash()
	{
		combatManager.addExpectedHitTick(player.getName(), 3, AttackType.MAGIC);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.hasSpotAnim(GraphicID.SPLASH)).thenReturn(true);

		expectedInteractions.put(player.getName(), playerInteraction);
		combatManager.onTickOfExpectedHit(3, false);

		assertEquals(1, combatManager.getAttackRecords().get(3).size());
		verify(combatManager, times(1)).onConfirmedInCombat(player.getName());
	}
}
