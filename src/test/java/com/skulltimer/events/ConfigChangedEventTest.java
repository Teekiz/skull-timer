package com.skulltimer.events;

import com.skulltimer.SkulledTimer;
import com.skulltimer.mocks.PluginMocks;
import java.time.Duration;
import net.runelite.client.events.ConfigChanged;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConfigChangedEventTest extends PluginMocks
{
	@Mock
	ConfigChanged configChanged;
	@Mock
	SkulledTimer skulledTimer;

	@Test
	public void timerIsNull()
	{
		eventBus.post(configChanged);
		verify(timerManager, never()).addTimer(any(Duration.class), anyBoolean());
	}

	@Test
	public void timerIsNotNull()
	{
		when(timerManager.getTimer()).thenReturn(skulledTimer);
		eventBus.post(configChanged);
		verify(timerManager, times(1)).addTimer(any(Duration.class), anyBoolean());
	}
}
