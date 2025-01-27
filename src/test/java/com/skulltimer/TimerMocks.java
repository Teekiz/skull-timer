package com.skulltimer;

import com.skulltimer.managers.TimerManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class TimerMocks
{
	@Mock
	SkullTimerConfig config;
	@Mock
	InfoBoxManager infoBoxManager;
	@Mock
	ItemManager itemManager;
	@Mock
	SkullTimerPlugin skullTimerPlugin;
	@Mock
	TimerManager timerManager;
}
