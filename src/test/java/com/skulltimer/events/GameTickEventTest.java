package com.skulltimer.events;

import com.skulltimer.enums.Notifications;
import com.skulltimer.mocks.PluginMocks;
import java.time.Duration;
import java.time.Instant;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import net.runelite.api.events.GameTick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameTickEventTest extends PluginMocks
{
	@Mock
	Player localPlayer;
	@Mock
	GameTick gameTick;

	@BeforeEach
	public void startUp() throws NoSuchFieldException
	{
		super.startUp();
		when(timerManager.getTimer()).thenReturn(skulledTimer);
	}

	@Test
	public void timerExpiresAtFutureTime_PlayerHasSkullIcon()
	{
		when(skulledTimer.getEndTime()).thenReturn(Instant.now().plus(Duration.ofMinutes(20)));
		when(statusManager.doesPlayerCurrentlyHaveSkullIcon()).thenReturn(true);
		eventBus.post(gameTick);
		verify(timerManager, times(0)).removeTimer(anyBoolean());
	}

	@Test
	public void timerHasExpired()
	{
		when(skulledTimer.getEndTime()).thenReturn(Instant.now().minusSeconds(1));
		when(statusManager.doesPlayerCurrentlyHaveSkullIcon()).thenReturn(false);
		eventBus.post(gameTick);
		verify(timerManager, times(1)).removeTimer(false);
	}

	@Test
	public void playerSkulledIconHasExpired()
	{
		when(skulledTimer.getEndTime()).thenReturn(Instant.now().plusSeconds(300));
		when(statusManager.doesPlayerCurrentlyHaveSkullIcon()).thenReturn(false);
		eventBus.post(gameTick);
		verify(timerManager, times(1)).removeTimer(false);
	}

	@Test
	public void doesNotifierCorrectlyStartWhenTimerReachesOneMinute()
	{
		when(skulledTimer.getRemainingTime()).thenReturn(Duration.ofMinutes(1));
		when(skulledTimer.getEndTime()).thenReturn(Instant.now().plusSeconds(60));
		eventBus.post(gameTick);
		verify(notifier, times(1)).notify(any(), eq(Notifications.EXPIRING_SOON.getMessage()));
	}

	@Test
	public void doesNotifierCorrectlyStartWhenTimerReachesZero()
	{
		when(skulledTimer.getEndTime()).thenReturn(Instant.now().minusSeconds(1));
		eventBus.post(gameTick);
		verify(notifier, times(1)).notify(any(), eq(Notifications.EXPIRED.getMessage()));
	}

	@Test
	public void areDuplicateExpiredSoonNotificationsSent()
	{
		when(skulledTimer.getRemainingTime()).thenReturn(Duration.ofMinutes(1));
		when(skulledTimer.getEndTime()).thenReturn(Instant.now().plusSeconds(60));

		eventBus.post(gameTick);
		eventBus.post(gameTick);
		verify(notifier, times(1)).notify(any(), eq(Notifications.EXPIRING_SOON.getMessage()));

		eventBus.post(gameTick);
		verify(notifier, times(2)).notify(any(), eq(Notifications.EXPIRING_SOON.getMessage()));
	}

	@Test
	public void areDuplicateExpiredNotificationsSent()
	{
		/*
			Testing the logic where two events should occur - either the timer loses the skull icon first, or timer is expired.

			Only one of the two should be used, with the timer expired taking priority.
		 */

		//timer expires first
		when(skulledTimer.getEndTime()).thenReturn(Instant.now().minusSeconds(1));
		when(client.getLocalPlayer()).thenReturn(localPlayer);

		eventBus.post(gameTick);
		verify(notifier, times(1)).notify(any(), eq(Notifications.EXPIRED.getMessage()));

		//skull expires - shouldn't send duplicate notification (plus seconds shouldn't occur but just to bypass the check)
		when(skulledTimer.getEndTime()).thenReturn(Instant.now().plusSeconds(2));
		when(localPlayer.getSkullIcon()).thenReturn(SkullIcon.NONE);
		eventBus.post(gameTick);
		verify(notifier, times(1)).notify(any(), eq(Notifications.EXPIRED.getMessage()));

		//new expiration - should send new notification
		when(localPlayer.getSkullIcon()).thenReturn(SkullIcon.NONE);
		eventBus.post(gameTick);
		verify(notifier, times(2)).notify(any(), eq(Notifications.EXPIRED.getMessage()));

		//timer should be removed at this point - no notification is called.
		eventBus.post(gameTick);
		verify(notifier, times(2)).notify(any(), eq(Notifications.EXPIRED.getMessage()));
	}
}
