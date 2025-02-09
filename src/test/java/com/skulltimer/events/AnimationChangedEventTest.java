package com.skulltimer.events;

import com.skulltimer.enums.config.Sensitivity;
import com.skulltimer.enums.equipment.WeaponHitDelay;
import com.skulltimer.mocks.PluginMocks;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.kit.KitType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnimationChangedEventTest extends PluginMocks
{
	@Mock
	AnimationChanged animationChanged;
	@Mock
	Player player;
	@Mock
	Player localPlayer;
	@Mock
	NPC npc;
	@Mock
	PlayerComposition playerComposition;

	@Test
	public void playerIsNotInWilderness()
	{
		when(locationManager.isInWilderness()).thenReturn(false);
		eventBus.post(animationChanged);
		verify(combatManager, times(0)).onAnimationOrInteractionChange(any(Player.class), anyInt(), anyBoolean());
	}

	@Test
	public void playerAnimationIsDefensive()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(animationChanged.getActor()).thenReturn(player);
		when(player.getAnimation()).thenReturn(-1);

		eventBus.post(animationChanged);
		verify(combatManager, times(0)).onAnimationOrInteractionChange(any(Player.class), anyInt(), anyBoolean());
	}

	@Test
	public void actorIsNull()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(animationChanged.getActor()).thenReturn(null);

		eventBus.post(animationChanged);
		verify(combatManager, times(0)).onAnimationOrInteractionChange(any(Player.class), anyInt(), anyBoolean());
	}

	@Test
	public void actorIsNotPlayerObject()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(animationChanged.getActor()).thenReturn(npc);
		when(npc.getAnimation()).thenReturn(100);

		eventBus.post(animationChanged);
		verify(combatManager, times(0)).onAnimationOrInteractionChange(any(Player.class), anyInt(), anyBoolean());
	}

	@Test
	public void actorIsLocalPlayer()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(animationChanged.getActor()).thenReturn(localPlayer);
		when(localPlayer.getAnimation()).thenReturn(100);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getName()).thenReturn("LocalPlayer");

		eventBus.post(animationChanged);
		verify(combatManager, times(0)).onAnimationOrInteractionChange(any(Player.class), anyInt(), anyBoolean());
	}

	@Test
	public void conditionsMet()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(animationChanged.getActor()).thenReturn(player);
		when(player.getName()).thenReturn("Player");
		when(player.getAnimation()).thenReturn(100);
		when(config.sensitivity()).thenReturn(Sensitivity.HIGH);

		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getName()).thenReturn("LocalPlayer");

		when(player.getPlayerComposition()).thenReturn(playerComposition);
		when(playerComposition.getEquipmentId(KitType.WEAPON)).thenReturn(1289);

		eventBus.post(animationChanged);
		verify(combatManager, times(1)).onAnimationOrInteractionChange(player, 0, true);
	}

	@Test
	public void conditionsMet_PlayersAreDistanceOfTen()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(animationChanged.getActor()).thenReturn(player);
		when(player.getName()).thenReturn("Player");
		when(player.getAnimation()).thenReturn(100);

		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getName()).thenReturn("LocalPlayer");

		when(player.getPlayerComposition()).thenReturn(playerComposition);
		when(playerComposition.getEquipmentId(KitType.WEAPON)).thenReturn(1289);
		when(equipmentManager.getWeaponHitDelay(1289, 100)).thenReturn(WeaponHitDelay.MELEE_STANDARD);

		when(locationManager.calculateDistanceBetweenPlayers(client.getLocalPlayer(), player)).thenReturn(10);

		eventBus.post(animationChanged);
		verify(combatManager, times(1)).setExpectedHitTick(player.getName(), 0);
	}

	@Test
	public void conditionsMet_WeaponIsNUll()
	{
		when(locationManager.isInWilderness()).thenReturn(true);
		when(animationChanged.getActor()).thenReturn(player);
		when(player.getName()).thenReturn("Player");
		when(player.getAnimation()).thenReturn(100);

		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getName()).thenReturn("LocalPlayer");

		when(player.getPlayerComposition()).thenReturn(playerComposition);
		when(playerComposition.getEquipmentId(KitType.WEAPON)).thenReturn(0);

		eventBus.post(animationChanged);
		verify(combatManager, times(0)).setExpectedHitTick(anyString(), anyInt());
	}
}
