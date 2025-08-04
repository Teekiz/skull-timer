package com.skulltimer.managers;

import com.skulltimer.SkulledTimer;
import com.skulltimer.enums.Notifications;
import com.skulltimer.mocks.TimerMocks;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TimerManagerTests extends TimerMocks
{
	@InjectMocks
	TimerManager timerManager;
	@Test
	public void startStandardTimer()
	{
		timerManager.addTimer(Duration.ofMinutes(5));
		assertNotNull(timerManager.getTimer());
	}

	@Test
	public void startTimer_WithExistingTimerLessThanReplacement()
	{
		timerManager.addTimer(Duration.ofMinutes(5));
		timerManager.addTimer(Duration.ofMinutes(6));

		assertNotNull(timerManager.getTimer());
		assertEquals(Duration.ofMinutes(6), timerManager.getTimer().getDuration());
	}

	@Test
	public void startTimer_WithExistingTimerGreaterThanReplacement()
	{
		timerManager.addTimer(Duration.ofMinutes(10));
		timerManager.addTimer(Duration.ofMinutes(6));

		assertNotNull(timerManager.getTimer());
		assertEquals(Duration.ofMinutes(10), timerManager.getTimer().getDuration());
	}

	@Test
	public void startTimer_WithNegativeAndZeroTimeDuration()
	{
		timerManager.addTimer(Duration.ofMinutes(-15));
		assertNull(timerManager.getTimer());

		timerManager.addTimer(Duration.ofMinutes(0));
		assertNull(timerManager.getTimer());

		timerManager.addTimer(Duration.ZERO);
		assertNull(timerManager.getTimer());
	}

	@Test
	public void stopTimer()
	{
		timerManager.addTimer(Duration.ofMinutes(12));
		assertNotNull(timerManager.getTimer());
		timerManager.removeTimer(true);
		assertNull(timerManager.getTimer());

		timerManager.addTimer(Duration.ofMinutes(5));
		assertNotNull(timerManager.getTimer());
		timerManager.removeTimer(false);
		assertNull(timerManager.getTimer());
	}

	@Test
	public void stopTimer_Configuration_Saving()
	{
		timerManager.addTimer(Duration.ofMinutes(12));
		timerManager.removeTimer(true);
		verify(config, times(2)).skullDuration(any(Duration.class));
	}

	@Test
	public void stopTimer_Configuration_NotSaving()
	{
		timerManager.addTimer(Duration.ofMinutes(30));
		timerManager.removeTimer(false);
		verify(config, times(2)).skullDuration(Duration.ZERO);
	}

	@Test
	public void timerExpired_withNotificationToggleOn()
	{
		when(config.expiredNotification()).thenReturn(true);
		timerManager.handleNotifications(Notifications.EXPIRED);
		verify(notifier, times(1)).notify(anyString());
	}

	@Test
	public void timerExpired_withNotificationToggleOff()
	{
		when(config.expiredNotification()).thenReturn(false);
		timerManager.handleNotifications(Notifications.EXPIRED);
		verify(notifier, times(0)).notify(anyString());
	}

	@Test
	public void timerExpiresSoon_withNotificationToggleOn()
	{
		when(config.expirationSoonNotification()).thenReturn(true);
		timerManager.handleNotifications(Notifications.EXPIRING_SOON);
		verify(notifier, times(1)).notify(anyString());
	}

	@Test
	public void timerExpiresSoon_withNotificationToggleOff()
	{
		when(config.expirationSoonNotification()).thenReturn(false);
		timerManager.handleNotifications(Notifications.EXPIRING_SOON);
		verify(notifier, times(0)).notify(anyString());
	}

}
