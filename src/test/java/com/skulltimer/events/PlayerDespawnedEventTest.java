package com.skulltimer.events;

import com.skulltimer.data.TargetInteraction;
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
	HashMap<String, TargetInteraction> targetRecords;
	@Mock
	TargetInteraction targetInteraction;

	@BeforeEach
	public void startUp() throws NoSuchFieldException
	{
		super.startUp();
		when(playerDespawned.getPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("Player");
		when(combatManager.getTargetRecords()).thenReturn(targetRecords);
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
		when(targetRecords.get("Player")).thenReturn(targetInteraction);
		when(targetInteraction.getCombatStatus()).thenReturn(CombatStatus.DEAD);

		eventBus.post(playerDespawned);
		verifyNoMoreInteractions(combatManager);
	}

	@Test
	public void playerWasInRecords_PlayerHasRetaliated()
	{
		when(targetRecords.containsKey("Player")).thenReturn(true);
		when(targetRecords.get("Player")).thenReturn(targetInteraction);
		when(targetInteraction.getCombatStatus()).thenReturn(CombatStatus.RETALIATED);
		when(targetInteraction.hasRetaliated()).thenReturn(true);

		eventBus.post(playerDespawned);
		verify(combatManager.getTargetRecords().get("Player"), times(1)).setCombatStatus(CombatStatus.RETALIATED_UNKNOWN);
	}

	@Test
	public void playerWasInRecords_PlayerHasNotRetaliated_PlayerIsAlive()
	{
		when(targetRecords.containsKey("Player")).thenReturn(true);
		when(targetRecords.get("Player")).thenReturn(targetInteraction);
		when(targetInteraction.getCombatStatus()).thenReturn(CombatStatus.DEFAULT);
		when(targetInteraction.hasRetaliated()).thenReturn(false);

		eventBus.post(playerDespawned);
		verify(combatManager.getTargetRecords().get("Player"), times(1)).setCombatStatus(CombatStatus.UNKNOWN);
	}
}
