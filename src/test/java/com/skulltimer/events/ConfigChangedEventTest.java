package com.skulltimer.events;

import com.skulltimer.SkulledTimer;
import com.skulltimer.mocks.PluginMocks;
import java.time.Duration;
import net.runelite.api.GameState;
import net.runelite.client.events.ConfigChanged;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
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
		verify(timerManager, never()).addTimer(any(Duration.class));
	}

	@Test
	public void timerIsNotNull()
	{
		when(configChanged.getGroup()).thenReturn("skulledTimer");
		when(configChanged.getKey()).thenReturn("warningTextColour");
		when(timerManager.getTimer()).thenReturn(skulledTimer);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		eventBus.post(configChanged);
		verify(timerManager, times(1)).addTimer(any(Duration.class));
	}

	@Test
	public void timerIsNotNull_NotLoggedIn()
	{
		when(configChanged.getGroup()).thenReturn("skulledTimer");
		when(configChanged.getKey()).thenReturn("warningTextColour");
		when(timerManager.getTimer()).thenReturn(skulledTimer);
		when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		eventBus.post(configChanged);
		verify(timerManager, times(0)).addTimer(any(Duration.class));
	}

	@Test
	public void otherConfigGroupChanged()
	{
		when(configChanged.getGroup()).thenReturn("otherPlugin");
		eventBus.post(configChanged);
		verify(timerManager, times(0)).addTimer(any(Duration.class));
	}

	@Test
	public void timerDurationSaved()
	{
		when(configChanged.getGroup()).thenReturn("skulledTimer");
		when(configChanged.getKey()).thenReturn("skullDuration");
		eventBus.post(configChanged);
		verify(timerManager, times(0)).addTimer(any(Duration.class));
	}
}
