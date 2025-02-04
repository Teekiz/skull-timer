package com.skulltimer.managers;

import com.skulltimer.mocks.TimerMocks;
import net.runelite.api.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatusManagerTests extends TimerMocks
{
	@InjectMocks
	StatusManager statusManager;
	@Mock
	Player player;

	@BeforeEach
	public void setUp()
	{
		when(client.getLocalPlayer()).thenReturn(player);
	}
}
