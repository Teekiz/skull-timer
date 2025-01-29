package com.skulltimer.managers;

import com.skulltimer.mocks.TimerMocks;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TimerManagerTests extends TimerMocks
{
	@InjectMocks
	TimerManager timerManager;

	@BeforeEach
	public void startUp()
	{
		lenient().when(config.cautiousTimerToggle()).thenReturn(true);
	}

	@Test
	public void startStandardTimer()
	{
		timerManager.addTimer(Duration.ofMinutes(5), false);
		assertNotNull(timerManager.getTimer());
		assertFalse(timerManager.getTimer().isCautious());
	}

	@Test
	public void startTimer_WithExistingTimerLessThanReplacement()
	{
		timerManager.addTimer(Duration.ofMinutes(5), false);
		timerManager.addTimer(Duration.ofMinutes(6), false);

		assertNotNull(timerManager.getTimer());
		assertEquals(Duration.ofMinutes(6), timerManager.getTimer().getDuration());
	}

	@Test
	public void startTimer_WithExistingTimerGreaterThanReplacement()
	{
		timerManager.addTimer(Duration.ofMinutes(10), false);
		timerManager.addTimer(Duration.ofMinutes(6), false);

		assertNotNull(timerManager.getTimer());
		assertEquals(Duration.ofMinutes(10), timerManager.getTimer().getDuration());
	}

	@Test
	public void startTimer_WithNegativeAndZeroTimeDuration()
	{
		timerManager.addTimer(Duration.ofMinutes(-15), false);
		assertNull(timerManager.getTimer());

		timerManager.addTimer(Duration.ofMinutes(0), false);
		assertNull(timerManager.getTimer());

		timerManager.addTimer(Duration.ZERO, false);
		assertNull(timerManager.getTimer());
	}

	@Test
	public void startCautiousTimer()
	{
		timerManager.addTimer(Duration.ofMinutes(12), true);
		assertNotNull(timerManager.getTimer());
		assertTrue(timerManager.getTimer().isCautious());
	}

	@Test
	public void stopTimer()
	{
		timerManager.addTimer(Duration.ofMinutes(12), false);
		assertNotNull(timerManager.getTimer());
		timerManager.removeTimer(true);
		assertNull(timerManager.getTimer());

		timerManager.addTimer(Duration.ofMinutes(5), true);
		assertNotNull(timerManager.getTimer());
		timerManager.removeTimer(false);
		assertNull(timerManager.getTimer());
	}

	@Test
	public void stopTimer_Configuration_Saving()
	{
		timerManager.addTimer(Duration.ofMinutes(12), false);
		timerManager.removeTimer(true);
		verify(config, times(1)).skullDuration(any(Duration.class));
		verify(config, times(2)).cautiousTimer(false);
	}

	@Test
	public void stopTimer_Configuration_SavingWithCautiousTimer()
	{
		timerManager.addTimer(Duration.ofMinutes(3), true);
		timerManager.removeTimer(true);
		verify(config, times(1)).skullDuration(any(Duration.class));
		verify(config, times(1)).cautiousTimer(true);
	}

	@Test
	public void stopTimer_Configuration_NotSaving()
	{
		timerManager.addTimer(Duration.ofMinutes(30), false);
		timerManager.removeTimer(false);
		verify(config, times(2)).skullDuration();
		verify(config, times(2)).cautiousTimer(false);
	}

	@Test
	public void stopTimer_Configuration_NotSavingWithCautiousTimer()
	{
		timerManager.addTimer(Duration.ofMinutes(6), true);
		timerManager.removeTimer(false);
		verify(config, times(2)).skullDuration();
		verify(config, times(1)).cautiousTimer(true);
	}
}
