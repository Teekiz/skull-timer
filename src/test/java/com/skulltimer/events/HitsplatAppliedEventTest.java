package com.skulltimer.events;

import com.skulltimer.mocks.PluginMocks;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.HitsplatApplied;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HitsplatAppliedEventTest extends PluginMocks
{
	@Mock
	HitsplatApplied hitsplatApplied;
	@Mock
	Player player;
	@Mock
	Player localPlayer;
	@Mock
	NPC npc;
	@Mock
	Hitsplat hitsplat;

	@Test
	public void playerIsNotInWilderness()
	{
		when(locationManager.isInWilderness()).thenReturn(false);
		eventBus.post(hitsplatApplied);
		verify(combatManager, times(0)).onTargetHitsplat(any(Player.class), anyInt());
	}

	@Test
	public void hitSplatOnLocalPlayer()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(hitsplatApplied.getActor()).thenReturn(player);
		when(player.getName()).thenReturn("Player");
		when(client.getLocalPlayer()).thenReturn(player);

		eventBus.post(hitsplatApplied);
		verify(combatManager, times(0)).onTargetHitsplat(any(Player.class), anyInt());
	}

	@Test
	public void hitSplat_NotByPlayer()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(hitsplatApplied.getActor()).thenReturn(player);
		when(hitsplatApplied.getHitsplat()).thenReturn(hitsplat);
		when(hitsplat.isMine()).thenReturn(false);
		when(player.getName()).thenReturn("Player");

		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getName()).thenReturn("NotPlayer");

		eventBus.post(hitsplatApplied);
		verify(combatManager, times(0)).onTargetHitsplat(any(Player.class), anyInt());
	}

	@Test
	public void hitSplat_OnNPC()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(hitsplatApplied.getActor()).thenReturn(npc);
		when(npc.getName()).thenReturn("npc");

		eventBus.post(hitsplatApplied);
		verify(combatManager, times(0)).onTargetHitsplat(any(Player.class), anyInt());
	}

	@Test
	public void HitSplatOnPlayer_LocalPlayerHasSkullIcon()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(hitsplatApplied.getActor()).thenReturn(player);
		when(hitsplatApplied.getHitsplat()).thenReturn(hitsplat);
		when(hitsplat.isMine()).thenReturn(true);
		when(player.getName()).thenReturn("Player");

		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getName()).thenReturn("NotPlayer");

		eventBus.post(hitsplatApplied);
		verify(combatManager, times(1)).onTargetHitsplat(player, 0);
	}

	@Test
	public void HitSplatOnLocalPlayer()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(localPlayer.getName()).thenReturn("Player");
		when(hitsplatApplied.getActor()).thenReturn(localPlayer);
		when(client.getLocalPlayer()).thenReturn(localPlayer);

		eventBus.post(hitsplatApplied);
		verify(combatManager, times(0)).onTargetHitsplat(player, 0);
	}
}
