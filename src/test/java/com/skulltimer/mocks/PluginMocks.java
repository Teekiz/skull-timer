package com.skulltimer.mocks;

import com.skulltimer.SkullTimerConfig;
import com.skulltimer.SkullTimerPlugin;
import com.skulltimer.SkulledTimer;
import com.skulltimer.managers.CombatManager;
import com.skulltimer.managers.EquipmentManager;
import com.skulltimer.managers.LocationManager;
import com.skulltimer.managers.TimerManager;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;


public class PluginMocks
{
	@InjectMocks
	protected SkullTimerPlugin skullTimerPlugin;
	@Mock
	protected Client client;
	@Mock
	protected ClientThread clientThread;
	@Mock
	protected SkullTimerConfig config;
	@Mock
	protected InfoBoxManager infoBoxManager;
	@Mock
	protected ItemManager itemManager;
	@Mock
	protected TimerManager timerManager;
	@Mock
	protected LocationManager locationManager;
	@Mock
	protected EquipmentManager equipmentManager;
	@Mock
	protected CombatManager combatManager;
	@Mock
	protected SkulledTimer skulledTimer;

	protected EventBus eventBus;

	@BeforeEach
	public void startUp() throws NoSuchFieldException
	{
		eventBus = new EventBus();
		eventBus.register(skullTimerPlugin);}
}
