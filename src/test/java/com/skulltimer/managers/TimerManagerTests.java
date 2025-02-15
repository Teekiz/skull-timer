package com.skulltimer.managers;

import com.skulltimer.mocks.TimerMocks;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
}
