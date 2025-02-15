package com.skulltimer.managers;

import com.skulltimer.mocks.TimerMocks;
import com.skulltimer.enums.TimerDurations;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import net.runelite.api.coords.WorldPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocationManagerTests extends TimerMocks
{
	@InjectMocks
	LocationManager locationManager;
	@Mock
	Player player;
	@Mock
	Player localPlayer;
	@Mock
	WorldPoint worldPointA;
	@Mock
	WorldPoint worldPointB;
	@BeforeEach
	public void startUp()
	{
		lenient().when(client.getLocalPlayer()).thenReturn(player);
	}

	@Test
	public void wildernessCheck()
	{
		when(client.getVarbitValue(5963)).thenReturn(0);
		assertFalse(locationManager.isInWilderness());

		when(client.getVarbitValue(5963)).thenReturn(1);
		assertTrue(locationManager.isInWilderness());
	}

	@Test
	public void inAbyss_WithoutTeleport()
	{
		locationManager.setHasBeenTeleportedIntoAbyss(false);
		when(player.getWorldLocation()).thenReturn(new WorldPoint(3019, 4814, 0));
		locationManager.isInAbyss();
		verify(timerManager, times(0)).addTimer(TimerDurations.ABYSS_DURATION.getDuration());
	}

	@Test
	public void inAbyss_WithTeleport()
	{
		locationManager.setHasBeenTeleportedIntoAbyss(true);
		when(player.getWorldLocation()).thenReturn(new WorldPoint(3055, 4860, 0));
		locationManager.isInAbyss();
		verify(timerManager, times(1)).addTimer(TimerDurations.ABYSS_DURATION.getDuration());
	}

	@Test
	public void inAbyss_WithoutSkull()
	{
		when(player.getWorldLocation()).thenReturn(new WorldPoint(3015, 4831, 0));
		lenient().when(player.getSkullIcon()).thenReturn(SkullIcon.NONE);
		locationManager.isInAbyss();
		verify(timerManager, times(0)).addTimer(TimerDurations.ABYSS_DURATION.getDuration());
	}

	@Test
	public void outsideAbyss()
	{
		locationManager.setHasBeenTeleportedIntoAbyss(false);
		when(player.getWorldLocation()).thenReturn(new WorldPoint(2785, 3463, 0));
		locationManager.isInAbyss();
		verify(timerManager, times(0)).addTimer(TimerDurations.ABYSS_DURATION.getDuration());
	}

	@Test
	public void outsideAbyss_WithTeleport()
	{
		locationManager.setHasBeenTeleportedIntoAbyss(true);
		when(player.getWorldLocation()).thenReturn(new WorldPoint(1496, 3040, 0));
		locationManager.isInAbyss();
		verify(timerManager, times(0)).addTimer(TimerDurations.ABYSS_DURATION.getDuration());
	}

	@Test
	public void hasPlayerLoggedOut_InRadius_NoAnimation()
	{
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getWorldLocation()).thenReturn(new WorldPoint(1000, 1000, 1));

		when(player.getWorldLocation()).thenReturn(new WorldPoint(1002, 1004, 1));
		when(player.getAnimation()).thenReturn(-1);

		assertTrue(locationManager.hasPlayerLoggedOut(player));
	}

	@Test
	public void hasPlayerLoggedOut_InRadius_WithAnimation()
	{
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getWorldLocation()).thenReturn(new WorldPoint(1000, 1000, 1));

		when(player.getWorldLocation()).thenReturn(new WorldPoint(1002, 1004, 1));
		when(player.getAnimation()).thenReturn(1);

		assertFalse(locationManager.hasPlayerLoggedOut(player));
	}

	@Test
	public void hasPlayerLoggedOut_OutOfRadius_X()
	{
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getWorldLocation()).thenReturn(new WorldPoint(1000, 1000, 1));

		when(player.getWorldLocation()).thenReturn(new WorldPoint(1020, 1004, 1));

		assertFalse(locationManager.hasPlayerLoggedOut(player));
	}

	@Test
	public void hasPlayerLoggedOut_OutOfRadius_Y()
	{
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getWorldLocation()).thenReturn(new WorldPoint(1000, 1000, 1));

		when(player.getWorldLocation()).thenReturn(new WorldPoint(1002, 800, 1));

		assertFalse(locationManager.hasPlayerLoggedOut(player));
	}

	@Test
	public void hasPlayerLoggedOut_OutOfRadius_XY()
	{
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getWorldLocation()).thenReturn(new WorldPoint(1000, 1000, 1));

		when(player.getWorldLocation()).thenReturn(new WorldPoint(2002, 600, 1));

		assertFalse(locationManager.hasPlayerLoggedOut(player));
	}

	@Test
	public void hasPlayerLoggedOut_PlayerIsNull()
	{
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getWorldLocation()).thenReturn(new WorldPoint(1000, 1000, 1));

		assertFalse(locationManager.hasPlayerLoggedOut(null));
	}

	@Test
	public void hasPlayerLoggedOut_PlayerIsNotNull_WorldPointNull()
	{
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getWorldLocation()).thenReturn(new WorldPoint(1000, 1000, 1));

		when(player.getWorldLocation()).thenReturn(null);

		assertFalse(locationManager.hasPlayerLoggedOut(player));
	}

	@Test
	public void calculateDistanceBetweenPlayers_PlayersAreNull()
	{
		assertEquals(0, locationManager.calculateDistanceBetweenPlayers(null, null));
		assertEquals(0, locationManager.calculateDistanceBetweenPlayers(player, null));
	}

	@Test
	public void calculateDistanceBetweenPlayers_PlayersLocationsAreNull()
	{
		when(player.getWorldLocation()).thenReturn(null);
		assertEquals(0, locationManager.calculateDistanceBetweenPlayers(player, localPlayer));

		when(player.getWorldLocation()).thenReturn(worldPointB);
		when(localPlayer.getWorldLocation()).thenReturn(null);
		assertEquals(0, locationManager.calculateDistanceBetweenPlayers(player, localPlayer));
	}

	@Test
	public void calculateDistanceBetweenPlayers_PlayerIsWithinMeleeRange()
	{
		when(player.getWorldLocation()).thenReturn(worldPointA);
		when(localPlayer.getWorldLocation()).thenReturn(worldPointB);

		when(worldPointA.getX()).thenReturn(10);
		when(worldPointA.getY()).thenReturn(290);

		when(worldPointB.getX()).thenReturn(11);
		when(worldPointB.getY()).thenReturn(291);

		assertEquals(1, locationManager.calculateDistanceBetweenPlayers(player, localPlayer));
	}

	@Test
	public void calculateDistanceBetweenPlayers_PlayerIsWithin_DistantX()
	{
		when(player.getWorldLocation()).thenReturn(worldPointA);
		when(localPlayer.getWorldLocation()).thenReturn(worldPointB);

		when(worldPointA.getX()).thenReturn(10);
		when(worldPointA.getY()).thenReturn(290);

		when(worldPointB.getX()).thenReturn(20);
		when(worldPointB.getY()).thenReturn(291);

		assertEquals(10, locationManager.calculateDistanceBetweenPlayers(player, localPlayer));
	}

	@Test
	public void calculateDistanceBetweenPlayers_PlayerIsWithin_DistantY()
	{
		when(player.getWorldLocation()).thenReturn(worldPointA);
		when(localPlayer.getWorldLocation()).thenReturn(worldPointB);

		when(worldPointA.getX()).thenReturn(10);
		when(worldPointA.getY()).thenReturn(391);

		when(worldPointB.getX()).thenReturn(11);
		when(worldPointB.getY()).thenReturn(291);

		assertEquals(100, locationManager.calculateDistanceBetweenPlayers(player, localPlayer));
	}
}
