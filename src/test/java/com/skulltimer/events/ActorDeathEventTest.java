package com.skulltimer.events;

import com.skulltimer.data.CombatInteraction;
import com.skulltimer.enums.CombatStatus;
import com.skulltimer.mocks.PluginMocks;
import java.util.HashMap;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActorDeathEventTest extends PluginMocks
{
	@Mock
	ActorDeath actorDeath;
	@Mock
	Player player;
	@Mock
	Player localPlayer;
	@Mock
	NPC npc;
	@Mock
	HashMap<String, CombatInteraction> targetRecords;
	@Mock
	CombatInteraction combatInteraction;

	@Test
	public void npcDied()
	{
		when(actorDeath.getActor()).thenReturn(npc);
		eventBus.post(actorDeath);
		verifyNoInteractions(combatManager);
	}

	@Test
	public void playerDied_NameIsNull()
	{
		when(actorDeath.getActor()).thenReturn(player);
		when(player.getName()).thenReturn(null);
		eventBus.post(actorDeath);
		verifyNoInteractions(combatManager);
	}

	@Test
	public void localPlayerDied()
	{
		when(actorDeath.getActor()).thenReturn(localPlayer);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getName()).thenReturn("LocalPlayer");
		when(combatManager.getCombatRecords()).thenReturn(targetRecords);

		eventBus.post(actorDeath);
		verify(targetRecords, times(1)).clear();
	}

	@Test
	public void PlayerDied_NotLocalPlayer_InTargetRecords()
	{
		when(actorDeath.getActor()).thenReturn(player);
		when(player.getName()).thenReturn("Player");
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getName()).thenReturn("LocalPlayer");
		when(combatManager.getCombatRecords()).thenReturn(targetRecords);
		when(targetRecords.containsKey("Player")).thenReturn(true);
		when(targetRecords.get("Player")).thenReturn(combatInteraction);

		eventBus.post(actorDeath);
		verify(combatInteraction, times(1)).setCombatStatus(CombatStatus.DEAD);
	}

	@Test
	public void PlayerDied_NotLocalPlayer_NotInTargetRecords()
	{
		when(actorDeath.getActor()).thenReturn(player);
		when(player.getName()).thenReturn("Player");
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getName()).thenReturn("LocalPlayer");
		when(combatManager.getCombatRecords()).thenReturn(targetRecords);
		when(targetRecords.containsKey("Player")).thenReturn(false);

		eventBus.post(actorDeath);
		verifyNoInteractions(combatInteraction);
	}

	@Test
	public void PlayerDied_InTargetRecords_TargetInteractionIsNull()
	{
		when(actorDeath.getActor()).thenReturn(player);
		when(player.getName()).thenReturn("Player");
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getName()).thenReturn("LocalPlayer");
		when(combatManager.getCombatRecords()).thenReturn(targetRecords);
		when(targetRecords.containsKey("Player")).thenReturn(true);
		when(targetRecords.get("Player")).thenReturn(null);

		eventBus.post(actorDeath);
		verifyNoInteractions(combatInteraction);
	}
}
