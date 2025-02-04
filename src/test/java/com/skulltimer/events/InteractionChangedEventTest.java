package com.skulltimer.events;

import com.skulltimer.mocks.PluginMocks;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.InteractingChanged;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InteractionChangedEventTest extends PluginMocks
{
	@Mock
	InteractingChanged interactingChanged;
	@Mock
	NPC npc;
	@Mock
	Player player;
	@Mock
	Player localPlayer;

	@Test
	public void playerIsNotInWilderness()
	{
		when(locationManager.isInWilderness()).thenReturn(false);
		eventBus.post(interactingChanged);
		verify(combatManager, times(0)).onAnimationOrInteractionChange(any(Player.class), anyInt(), anyBoolean());
	}

	@Test
	public void targetIsNotPlayer()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(interactingChanged.getTarget()).thenReturn(npc);
		eventBus.post(interactingChanged);
		verify(combatManager, times(0)).onAnimationOrInteractionChange(any(Player.class), anyInt(), anyBoolean());
	}

	@Test
	public void sourceIsNotPlayer()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(interactingChanged.getTarget()).thenReturn(localPlayer);
		when(interactingChanged.getSource()).thenReturn(npc);
		eventBus.post(interactingChanged);
		verify(combatManager, times(0)).onAnimationOrInteractionChange(any(Player.class), anyInt(), anyBoolean());
	}

	@Test
	public void sourceIsLocalPlayer()
	{
		when(player.getName()).thenReturn("OtherPlayer");
		when(localPlayer.getName()).thenReturn("LocalPlayer");
		when(client.getLocalPlayer()).thenReturn(localPlayer);


		when(locationManager.isInWilderness()).thenReturn(true);
		when(interactingChanged.getTarget()).thenReturn(player);
		when(interactingChanged.getSource()).thenReturn(localPlayer);
		eventBus.post(interactingChanged);
		verify(combatManager, times(0)).onAnimationOrInteractionChange(any(Player.class), anyInt(), anyBoolean());
	}

	@Test
	public void targetIsLocalPlayer()
	{
		when(localPlayer.getName()).thenReturn("LocalPlayer");
		when(client.getLocalPlayer()).thenReturn(localPlayer);

		when(locationManager.isInWilderness()).thenReturn(true);
		when(interactingChanged.getTarget()).thenReturn(localPlayer);
		when(interactingChanged.getSource()).thenReturn(player);
		eventBus.post(interactingChanged);
		verify(combatManager, times(1)).onAnimationOrInteractionChange(player, 0, false);
	}
}
