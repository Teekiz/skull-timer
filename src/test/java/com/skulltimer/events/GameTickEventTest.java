package com.skulltimer.events;

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

import static org.mockito.ArgumentMatchers.anyBoolean;
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
	public void timerExpiresAfterCurrentTime()
	{
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(skulledTimer.getEndTime()).thenReturn(Instant.now().plus(Duration.ofMinutes(20)));
		eventBus.post(gameTick);
		verify(timerManager, times(0)).removeTimer(anyBoolean());
	}

	@Test
	public void timerHasExpired()
	{
		when(skulledTimer.getEndTime()).thenReturn(Instant.now().minusSeconds(1));
		eventBus.post(gameTick);
		verify(timerManager, times(1)).removeTimer(false);
	}

	@Test
	public void playerSkulledIconHasExpired()
	{
		when(timerManager.getTimer()).thenReturn(null);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getSkullIcon()).thenReturn(SkullIcon.NONE);
		eventBus.post(gameTick);
		verify(timerManager, times(1)).removeTimer(false);
	}
}
