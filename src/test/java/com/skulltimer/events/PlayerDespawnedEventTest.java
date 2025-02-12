package com.skulltimer.events;

import com.skulltimer.data.CombatInteraction;
import com.skulltimer.enums.CombatStatus;
import com.skulltimer.mocks.PluginMocks;
import java.util.HashMap;
import net.runelite.api.Player;
import net.runelite.api.events.PlayerDespawned;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlayerDespawnedEventTest extends PluginMocks
{
	@Mock
	PlayerDespawned playerDespawned;
	@Mock
	Player player;
	@Mock
	HashMap<String, CombatInteraction> targetRecords;
	@Mock
	CombatInteraction combatInteraction;

	@BeforeEach
	public void startUp() throws NoSuchFieldException
	{
		super.startUp();
		when(playerDespawned.getPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("Player");
		when(combatManager.getCombatRecords()).thenReturn(targetRecords);
	}

	@Test
	public void playerWasNotInRecords()
	{
		when(targetRecords.containsKey("Player")).thenReturn(false);
		eventBus.post(playerDespawned);
		verifyNoMoreInteractions(combatManager);
	}

	@Test
	public void playerWasInRecords_PlayerHasDied()
	{
		when(targetRecords.containsKey("Player")).thenReturn(true);
		when(targetRecords.get("Player")).thenReturn(combatInteraction);
		when(combatInteraction.getCombatStatus()).thenReturn(CombatStatus.DEAD);

		eventBus.post(playerDespawned);
		verifyNoMoreInteractions(combatInteraction);
	}

	@Test
	public void playerWasInRecords_PlayerHasRetaliated()
	{
		when(targetRecords.containsKey("Player")).thenReturn(true);
		when(targetRecords.get("Player")).thenReturn(combatInteraction);
		when(combatInteraction.getCombatStatus()).thenReturn(CombatStatus.RETALIATED);
		when(combatInteraction.hasRetaliated()).thenReturn(true);

		eventBus.post(playerDespawned);
		verify(combatManager.getCombatRecords().get("Player"), times(1)).setCombatStatus(CombatStatus.INACTIVE);
	}

	@Test
	public void playerWasInRecords_PlayerHasNotRetaliated_PlayerIsAlive()
	{
		when(targetRecords.containsKey("Player")).thenReturn(true);
		when(targetRecords.get("Player")).thenReturn(combatInteraction);
		when(combatInteraction.getCombatStatus()).thenReturn(CombatStatus.ATTACKED);
		when(combatInteraction.hasRetaliated()).thenReturn(false);

		eventBus.post(playerDespawned);
		verify(combatManager.getCombatRecords().get("Player"), times(1)).setCombatStatus(CombatStatus.UNCERTAIN);
	}

	@Test
	public void playerLoggedOut_hasNotRetaliated()
	{
		when(targetRecords.containsKey("Player")).thenReturn(true);
		when(targetRecords.get("Player")).thenReturn(combatInteraction);
		when(combatInteraction.getCombatStatus()).thenReturn(CombatStatus.ATTACKED);
		when(locationManager.hasPlayerLoggedOut(player)).thenReturn(true);

		eventBus.post(playerDespawned);
		verify(combatManager.getCombatRecords().get("Player"), times(1)).setCombatStatus(CombatStatus.LOGGED_OUT);
	}

	@Test
	public void playerLoggedOut_hasRetaliated()
	{
		when(targetRecords.containsKey("Player")).thenReturn(true);
		when(targetRecords.get("Player")).thenReturn(combatInteraction);
		when(combatInteraction.hasRetaliated()).thenReturn(true);

		eventBus.post(playerDespawned);
		verify(combatManager.getCombatRecords().get("Player"), times(1)).setCombatStatus(CombatStatus.INACTIVE);
	}
}
