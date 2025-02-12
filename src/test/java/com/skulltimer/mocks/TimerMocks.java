package com.skulltimer.mocks;

import com.skulltimer.SkullTimerConfig;
import com.skulltimer.SkullTimerPlugin;
import com.skulltimer.managers.StatusManager;
import com.skulltimer.managers.TimerManager;
import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.mockito.Mock;

public class TimerMocks
{
	@Mock
	protected Client client;
	@Mock
	protected SkullTimerConfig config;
	@Mock
	protected InfoBoxManager infoBoxManager;
	@Mock
	protected ItemManager itemManager;
	@Mock
	protected SkullTimerPlugin skullTimerPlugin;
	@Mock
	protected TimerManager timerManager;
	@Mock
	protected StatusManager statusManager;
}
