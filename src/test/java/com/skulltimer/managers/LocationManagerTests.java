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
		verify(timerManager, times(0)).addTimer(TimerDurations.ABYSS_DURATION.getDuration(), false);
	}

	@Test
	public void inAbyss_WithTeleport()
	{
		locationManager.setHasBeenTeleportedIntoAbyss(true);
		when(player.getWorldLocation()).thenReturn(new WorldPoint(3055, 4860, 0));
		locationManager.isInAbyss();
		verify(timerManager, times(1)).addTimer(TimerDurations.ABYSS_DURATION.getDuration(), false);
	}

	@Test
	public void inAbyss_WithoutSkull()
	{
		when(player.getWorldLocation()).thenReturn(new WorldPoint(3015, 4831, 0));
		lenient().when(player.getSkullIcon()).thenReturn(SkullIcon.NONE);
		locationManager.isInAbyss();
		verify(timerManager, times(0)).addTimer(TimerDurations.ABYSS_DURATION.getDuration(), false);
	}

	@Test
	public void outsideAbyss()
	{
		locationManager.setHasBeenTeleportedIntoAbyss(false);
		when(player.getWorldLocation()).thenReturn(new WorldPoint(2785, 3463, 0));
		locationManager.isInAbyss();
		verify(timerManager, times(0)).addTimer(TimerDurations.ABYSS_DURATION.getDuration(), false);
	}

	@Test
	public void outsideAbyss_WithTeleport()
	{
		locationManager.setHasBeenTeleportedIntoAbyss(true);
		when(player.getWorldLocation()).thenReturn(new WorldPoint(1496, 3040, 0));
		locationManager.isInAbyss();
		verify(timerManager, times(0)).addTimer(TimerDurations.ABYSS_DURATION.getDuration(), false);
	}

	@Test
	public void hasPlayerLoggedOut_InRadius_NoAnimation()
	{
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getWorldLocation()).thenReturn(new WorldPoint(1000, 1000, 1));

		when(player.getWorldLocation()).thenReturn(new WorldPoint(1002, 1004, 1));
		when(player.getAnimation()).thenReturn(0);

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
}
