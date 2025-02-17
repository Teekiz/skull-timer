package com.skulltimer.managers;

import com.skulltimer.data.CombatInteraction;
import com.skulltimer.data.ExpectedHit;
import com.skulltimer.enums.equipment.AttackType;
import com.skulltimer.enums.equipment.WeaponHitDelay;
import com.skulltimer.mocks.TimerMocks;
import com.skulltimer.enums.CombatStatus;
import com.skulltimer.enums.TimerDurations;
import java.time.Duration;
import net.runelite.api.GraphicID;
import net.runelite.api.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

	CombatInteraction combatInteraction;

	@Spy
	@InjectMocks
	CombatManager combatManager;

	int tickCounter = 0;


	@BeforeEach
	public void startUp()
	{
		when(player.getName()).thenReturn("PlayerOne");
	}

	@Test
	public void playerIsNull()
	{
		when(player.getName()).thenReturn(null);
		combatManager.onTargetHitsplat(player, tickCounter++);
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration());
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer()
	{
		when(config.pvpToggle()).thenReturn(true);

		combatManager.onTargetHitsplat(player, tickCounter++);
		verify(timerManager).addTimer(TimerDurations.PVP_DURATION.getDuration());
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithMultipleHits()
	{
		when(config.pvpToggle()).thenReturn(true);

		combatManager.onTargetHitsplat(player, tickCounter++);
		combatManager.onTargetHitsplat(player, tickCounter++);
		verify(timerManager, times(2)).addTimer(TimerDurations.PVP_DURATION.getDuration());
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithRetaliatedAttack()
	{
		when(config.pvpToggle()).thenReturn(true);

		//initial attack
		combatManager.onTargetHitsplat(player, tickCounter++);
		//simulated response
		combatManager.onConfirmedInCombat(player.getName());
		//final attack
		combatManager.onTargetHitsplat(player, tickCounter++);

		//because the player attacked back, don't restart timer
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration());
	}

	@Test
	public void testUnprovokedAttackOnLocalPlayer()
	{
		combatManager.onConfirmedInCombat(player.getName());
		combatManager.onTargetHitsplat(player, tickCounter++);

		//because the player attacked back, don't restart timer
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration());
		assertEquals(CombatStatus.ATTACKER, combatManager.getCombatRecords().get("PlayerOne").getCombatStatus());
	}

	@Test
	public void testUnprovokedAttackOnLocalPlayer_PlayerHadLoggedOut()
	{
		combatInteraction = new CombatInteraction();
		combatInteraction.setCombatStatus(CombatStatus.LOGGED_OUT);
		combatManager.getCombatRecords().put("PlayerOne", combatInteraction);

		combatManager.onConfirmedInCombat(player.getName());
		combatManager.onTargetHitsplat(player, tickCounter++);

		//because the player had logged out, their attacking record would have been reset, causing them to become the aggressor.
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration());
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_WithPlayerBeingSetToCautious_WithLocalPlayerNotSkulled()
	{
		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.UNCERTAIN);
		combatManager.getCombatRecords().put("PlayerOne", interaction);
		combatManager.onTargetHitsplat(player, tickCounter++);
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration());
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_PlayerHadLoggedOut_WithNoRetaliation()
	{
		when(config.pvpToggle()).thenReturn(true);

		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.LOGGED_OUT);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		combatManager.onTargetHitsplat(player, tickCounter++);

		//because the player attacked back, don't restart timer
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration());
	}

	@Test
	public void testUnprovokedAttackOnOtherPlayer_PlayerHadLoggedOut_WithRetaliation()
	{
		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.INACTIVE);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		combatManager.onTargetHitsplat(player, tickCounter++);
		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration());
	}

	@Test
	public void PVPDisabledTest()
	{
		when(config.pvpToggle()).thenReturn(false);
		combatManager.onTargetHitsplat(player, tickCounter++);

		verify(timerManager, times(0)).addTimer(TimerDurations.PVP_DURATION.getDuration());
		assertEquals(1, combatManager.getCombatRecords().size());
	}

	@Test
	public void targetHasDied()
	{
		when(config.pvpToggle()).thenReturn(true);

		combatInteraction = new CombatInteraction();
		combatInteraction.setCombatStatus(CombatStatus.DEAD);
		combatManager.getCombatRecords().put("PlayerOne", combatInteraction);

		combatManager.onTargetHitsplat(player, tickCounter++);
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration());
	}

	@Test
	public void targetHasDied_PreviouslyRetaliated()
	{
		when(config.pvpToggle()).thenReturn(true);

		combatInteraction = new CombatInteraction();
		combatInteraction.setCombatStatus(CombatStatus.DEAD);
		combatManager.getCombatRecords().put("PlayerOne", combatInteraction);

		combatManager.onTargetHitsplat(player, tickCounter++);
		verify(timerManager, times(1)).addTimer(TimerDurations.PVP_DURATION.getDuration());

		assertFalse(combatInteraction.hasRetaliated());
	}

	@Test
	public void addExpectedHitTick_WithNullRecord()
	{
		combatManager.addExpectedHitTick(player.getName(), 2, AttackType.OTHER);
	}

	@Test
	public void addExpectedHitTick_WithExistingRecord()
	{
		combatManager.onPlayerInteractionChange(player.getName(), true);
		combatManager.addExpectedHitTick("New Player", 2, AttackType.MAGIC);

		ExpectedHit expectedHit = new ExpectedHit(player.getName(), AttackType.MELEE);
		combatManager.addExpectedHitTick(player.getName(), 2, AttackType.MELEE);

		assertTrue(combatManager.getAttackRecords().get(2).contains(expectedHit));
	}

	@Test
	public void onTickOfExpectedHit_TickNumberLowerThanCurrentTick()
	{
		combatManager.onPlayerInteractionChange(player.getName(), true);
		combatManager.addExpectedHitTick(player.getName(), 2, AttackType.MELEE);
		combatManager.onTickOfExpectedHit(3, true);
		assertEquals(1, combatManager.getCombatRecords().size());
	}

	@Test
	public void onTickOfExpectedHit_TickNumberEqualToCurrentTick()
	{
		combatManager.onPlayerInteractionChange(player.getName(), true);
		combatManager.addExpectedHitTick(player.getName(), 3, AttackType.MELEE);
		combatManager.onTickOfExpectedHit(3, true);

		assertEquals(1, combatManager.getCombatRecords().size());
		verify(combatManager, times(1)).onConfirmedInCombat(player.getName());
	}

	@Test
	public void onTickOfExpectedHit_TickNumberGreaterThanCurrentTick()
	{
		combatManager.onPlayerInteractionChange(player.getName(), true);
		combatManager.addExpectedHitTick(player.getName(), 4, AttackType.MELEE);
		assertEquals(1, combatManager.getAttackRecords().get(4).size());
		verify(combatManager, times(0)).onConfirmedInCombat(player.getName());
	}

	@Test
	public void onTickOfExpectedHitSplat_NoHit()
	{
		combatManager.onPlayerInteractionChange(player.getName(), true);
		combatManager.addExpectedHitTick(player.getName(), 3, AttackType.MELEE);
		combatManager.onTickOfExpectedHit(3, false);

		assertEquals(1, combatManager.getAttackRecords().get(3).size());
		verify(combatManager, times(0)).onConfirmedInCombat(player.getName());
	}

	@Test
	public void onTickOfExpectedHitSplat_NoHit_WithMagicAttack_NoSplash()
	{
		combatManager.onPlayerInteractionChange(player.getName(), true);
		combatManager.addExpectedHitTick(player.getName(), 3, AttackType.MAGIC);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.hasSpotAnim(GraphicID.SPLASH)).thenReturn(false);

		combatManager.onTickOfExpectedHit(3, false);

		assertEquals(1, combatManager.getAttackRecords().get(3).size());
		verify(combatManager, times(0)).onConfirmedInCombat(player.getName());
	}

	@Test
	public void onTickOfExpectedHitSplat_NoHit_WithMagicAttack_WithSplash()
	{
		combatManager.onPlayerInteractionChange(player.getName(), true);
		combatManager.addExpectedHitTick(player.getName(), 3, AttackType.MAGIC);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.hasSpotAnim(GraphicID.SPLASH)).thenReturn(true);

		combatManager.onTickOfExpectedHit(3, false);

		assertEquals(1, combatManager.getAttackRecords().get(3).size());
		verify(combatManager, times(1)).onConfirmedInCombat(player.getName());
	}

	@Test
	public void onUnknownOrInactiveStatus_CurrentTickIsZero()
	{
		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.UNCERTAIN);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		combatManager.onTargetHitsplat(player, 0);

		assertEquals(CombatStatus.UNCERTAIN, interaction.getCombatStatus());
		verify(equipmentManager, times(0)).getWeaponHitDelay(any(Player.class));
		verify(timerManager, times(0)).addTimer(any(Duration.class));
	}

	@Test
	public void onUnknownOrInactiveStatus_WeaponHitDelayIsNull()
	{
		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.UNCERTAIN);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(equipmentManager.getWeaponHitDelay(localPlayer)).thenReturn(null);

		combatManager.onTargetHitsplat(player, 2);

		//Running the captured runnable to simulate tick end
		ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(clientThread).invokeAtTickEnd(runnableArgumentCaptor.capture());
		runnableArgumentCaptor.getValue().run();

		assertEquals(CombatStatus.UNCERTAIN, interaction.getCombatStatus());
		verify(equipmentManager, times(1)).getWeaponHitDelay(any(Player.class));
		verify(timerManager, times(0)).addTimer(any(Duration.class));
	}

	@Test
	public void onUnknownOrInactiveStatus_PlayerDoesNotHaveSkull()
	{
		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.UNCERTAIN);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		when(client.getLocalPlayer()).thenReturn(localPlayer);
		//RANGED_STANDARD hit delay should result in 3 ticks with a distance of 10.
		when(equipmentManager.getWeaponHitDelay(localPlayer)).thenReturn(WeaponHitDelay.RANGED_STANDARD);
		when(statusManager.getSkullIconTickStartTime()).thenReturn(37);
		when(statusManager.doesPlayerCurrentlyHaveSkullIcon()).thenReturn(false);

		combatManager.onTargetHitsplat(player, 40);

		//Running the captured runnable to simulate tick end
		ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(clientThread).invokeAtTickEnd(runnableArgumentCaptor.capture());
		runnableArgumentCaptor.getValue().run();


		assertEquals(CombatStatus.RETALIATED, interaction.getCombatStatus());
		verify(equipmentManager, times(1)).getWeaponHitDelay(any(Player.class));
		verify(timerManager, times(0)).addTimer(any(Duration.class));
	}

	@Test
	public void onUnknownOrInactiveStatus_PlayerHasSkull_WithinTimeRange()
	{
		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.UNCERTAIN);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		when(config.pvpToggle()).thenReturn(true);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		//RANGED_STANDARD hit delay should result in 5 ticks with a distance of 10.
		when(equipmentManager.getWeaponHitDelay(localPlayer)).thenReturn(WeaponHitDelay.RANGED_STANDARD);
		when(statusManager.getSkullIconTickStartTime()).thenReturn(37);
		when(statusManager.doesPlayerCurrentlyHaveSkullIcon()).thenReturn(true);

		combatManager.onTargetHitsplat(player, 40);

		//Running the captured runnable to simulate tick end
		ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(clientThread).invokeAtTickEnd(runnableArgumentCaptor.capture());
		runnableArgumentCaptor.getValue().run();

		assertEquals(CombatStatus.ATTACKED, interaction.getCombatStatus());
		verify(equipmentManager, times(1)).getWeaponHitDelay(any(Player.class));
		verify(timerManager, times(1)).addTimer(any(Duration.class));
	}

	@Test
	public void onUnknownOrInactiveStatus_PlayerHasSkull_BelowTimeRange()
	{
		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.UNCERTAIN);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		when(config.pvpToggle()).thenReturn(true);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		//RANGED_STANDARD hit delay should result in 5 ticks with a distance of 10.
		when(equipmentManager.getWeaponHitDelay(localPlayer)).thenReturn(WeaponHitDelay.RANGED_STANDARD);
		when(statusManager.getSkullIconTickStartTime()).thenReturn(33);
		when(statusManager.doesPlayerCurrentlyHaveSkullIcon()).thenReturn(true);

		combatManager.onTargetHitsplat(player, 40);

		//Running the captured runnable to simulate tick end
		ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(clientThread).invokeAtTickEnd(runnableArgumentCaptor.capture());
		runnableArgumentCaptor.getValue().run();

		assertEquals(CombatStatus.UNCERTAIN, interaction.getCombatStatus());
		verify(equipmentManager, times(1)).getWeaponHitDelay(any(Player.class));
		verify(timerManager, times(1)).addTimer(any(Duration.class));
	}

	@Test
	public void onUnknownOrInactiveStatus_PlayerHasSkull_AboveTimeRange()
	{
		//Under normal circumstances, this shouldn't occur
		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.UNCERTAIN);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		when(config.pvpToggle()).thenReturn(true);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		//RANGED_STANDARD hit delay should result in 3 ticks with a distance of 10.
		when(equipmentManager.getWeaponHitDelay(localPlayer)).thenReturn(WeaponHitDelay.RANGED_STANDARD);
		when(statusManager.getSkullIconTickStartTime()).thenReturn(43);
		when(statusManager.doesPlayerCurrentlyHaveSkullIcon()).thenReturn(true);

		combatManager.onTargetHitsplat(player, 40);

		//Running the captured runnable to simulate tick end
		ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(clientThread).invokeAtTickEnd(runnableArgumentCaptor.capture());
		runnableArgumentCaptor.getValue().run();

		assertEquals(CombatStatus.UNCERTAIN, interaction.getCombatStatus());
		verify(equipmentManager, times(1)).getWeaponHitDelay(any(Player.class));
		verify(timerManager, times(1)).addTimer(any(Duration.class));
	}

	@Test
	public void onUnknownOrInactiveStatus_PlayerHasSkull_OutsideOfTimeRanged_Inactive()
	{
		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.INACTIVE);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		when(client.getLocalPlayer()).thenReturn(localPlayer);
		//RANGED_STANDARD hit delay should result in 3 ticks with a distance of 10.
		when(equipmentManager.getWeaponHitDelay(localPlayer)).thenReturn(WeaponHitDelay.RANGED_STANDARD);
		when(statusManager.getSkullIconTickStartTime()).thenReturn(43);
		when(statusManager.doesPlayerCurrentlyHaveSkullIcon()).thenReturn(true);

		combatManager.onTargetHitsplat(player, 400);

		//Running the captured runnable to simulate tick end
		ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(clientThread).invokeAtTickEnd(runnableArgumentCaptor.capture());
		runnableArgumentCaptor.getValue().run();

		assertEquals(CombatStatus.INACTIVE, interaction.getCombatStatus());
		verify(equipmentManager, times(1)).getWeaponHitDelay(any(Player.class));
		verify(timerManager, times(0)).addTimer(any(Duration.class));
	}

	@Test
	public void onUnknownOrInactiveStatus_PlayerHasSkull_OutsideOfTimeRanged_Uncertain()
	{
		CombatInteraction interaction = new CombatInteraction();
		interaction.setCombatStatus(CombatStatus.UNCERTAIN);
		combatManager.getCombatRecords().put("PlayerOne", interaction);

		when(config.pvpToggle()).thenReturn(true);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		//RANGED_STANDARD hit delay should result in 3 ticks with a distance of 10.
		when(equipmentManager.getWeaponHitDelay(localPlayer)).thenReturn(WeaponHitDelay.RANGED_STANDARD);
		when(statusManager.getSkullIconTickStartTime()).thenReturn(43);
		when(statusManager.doesPlayerCurrentlyHaveSkullIcon()).thenReturn(true);

		combatManager.onTargetHitsplat(player, 400);

		//Running the captured runnable to simulate tick end
		ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(clientThread).invokeAtTickEnd(runnableArgumentCaptor.capture());
		runnableArgumentCaptor.getValue().run();

		assertEquals(CombatStatus.UNCERTAIN, interaction.getCombatStatus());
		verify(equipmentManager, times(1)).getWeaponHitDelay(any(Player.class));
		verify(timerManager, times(1)).addTimer(any(Duration.class));
	}
}
