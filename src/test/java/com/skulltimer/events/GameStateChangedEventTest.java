package com.skulltimer.events;

import com.skulltimer.mocks.PluginMocks;
import java.time.Duration;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameStateChangedEventTest extends PluginMocks
{
	@Mock
	GameStateChanged gameStateChanged;

	@Test
	public void loggingIn_WithNoTimerSaved()
	{
		when(config.skullDuration()).thenReturn(null);

		when(gameStateChanged.getGameState()).thenReturn(GameState.LOGGED_IN);
		eventBus.post(gameStateChanged);
		verify(timerManager, times(0)).addTimer(any(Duration.class), anyBoolean());
	}

	@Test
	public void loggingIn_WithTimerSavedButTimerAlreadyExists()
	{
		when(config.skullDuration()).thenReturn(Duration.ofMinutes(20));
		when(timerManager.getTimer()).thenReturn(skulledTimer);

		when(gameStateChanged.getGameState()).thenReturn(GameState.LOGGED_IN);
		eventBus.post(gameStateChanged);
		verify(timerManager, times(0)).addTimer(any(Duration.class), anyBoolean());
	}

	@Test
	public void loggingIn_WithTimerSavedButInAbyss()
	{
		when(config.skullDuration()).thenReturn(Duration.ofMinutes(20));
		when(timerManager.getTimer()).thenReturn(null);
		when(locationManager.isInAbyss()).thenReturn(true);

		when(gameStateChanged.getGameState()).thenReturn(GameState.LOGGED_IN);
		eventBus.post(gameStateChanged);
		verify(timerManager, times(0)).addTimer(any(Duration.class), anyBoolean());
	}

	@Test
	public void loggingIn_WithSavedConfig_WithNullTimer_WithNotInAbyss()
	{
		when(config.skullDuration()).thenReturn(Duration.ofMinutes(20));
		when(timerManager.getTimer()).thenReturn(null);
		when(locationManager.isInAbyss()).thenReturn(false);

		when(gameStateChanged.getGameState()).thenReturn(GameState.LOGGED_IN);
		eventBus.post(gameStateChanged);
		verify(timerManager, times(1)).addTimer(any(Duration.class), anyBoolean());
	}

	@Test
	public void loggingOut_WithValidTimer()
	{
		when(timerManager.getTimer()).thenReturn(skulledTimer);

		when(gameStateChanged.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		eventBus.post(gameStateChanged);
		verify(timerManager, times(1)).removeTimer(true);
	}

	@Test
	public void loggingOut_WithNullTimer()
	{
		when(timerManager.getTimer()).thenReturn(null);

		when(gameStateChanged.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		eventBus.post(gameStateChanged);
		verify(timerManager, times(0)).removeTimer(true);
	}

	@Test
	public void worldHopping_WithValidTimer()
	{
		when(timerManager.getTimer()).thenReturn(skulledTimer);

		when(gameStateChanged.getGameState()).thenReturn(GameState.HOPPING);
		eventBus.post(gameStateChanged);
		verify(timerManager, times(1)).removeTimer(true);
	}

	@Test
	public void worldHopping_WithNullTimer()
	{
		when(timerManager.getTimer()).thenReturn(null);

		when(gameStateChanged.getGameState()).thenReturn(GameState.HOPPING);
		eventBus.post(gameStateChanged);
		verify(timerManager, times(0)).removeTimer(true);
	}
}
