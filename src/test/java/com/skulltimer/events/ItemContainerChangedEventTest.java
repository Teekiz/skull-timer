package com.skulltimer.events;

import com.skulltimer.mocks.PluginMocks;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import net.runelite.api.events.ItemContainerChanged;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemContainerChangedEventTest extends PluginMocks
{
	@Mock
	ItemContainerChanged itemContainerChanged;
	@Mock
	ItemContainer itemContainer;
	@Mock
	ItemContainer itemContainerTwo;
	@Mock
	Player player;

	List<Integer> changedInventorySlots;

	@BeforeEach
	public void startUp() throws NoSuchFieldException
	{
		super.startUp();
		changedInventorySlots = new ArrayList<>();
	}

	@Test
	public void notLoggedIn_EquipmentUnavailable()
	{
		eventBus.post(itemContainerChanged);
		verify(equipmentManager, times(0)).shouldTimerBeStarted(anyList());
	}

	@Test
	public void notLoggedIn_OtherEquipmentContainerChanged()
	{
		when(itemContainerChanged.getItemContainer()).thenReturn(itemContainerTwo);
		when(equipmentManager.getEquipment()).thenReturn(itemContainer);

		eventBus.post(itemContainerChanged);
		verify(equipmentManager, times(0)).shouldTimerBeStarted(anyList());
	}

	@Test
	public void itemContainerChanged_NoEquipmentHasChanged()
	{
		when(itemContainerChanged.getItemContainer()).thenReturn(itemContainer);
		when(equipmentManager.getEquipment()).thenReturn(itemContainer);

		when(equipmentManager.getModifiedItemSlotChanges()).thenReturn(changedInventorySlots);

		eventBus.post(itemContainerChanged);
		verify(equipmentManager, times(0)).shouldTimerBeStarted(anyList());
	}

	@Test
	public void playerHasSkullIcon()
	{
		changedInventorySlots.add(1);

		when(itemContainerChanged.getItemContainer()).thenReturn(itemContainer);
		when(equipmentManager.getEquipment()).thenReturn(itemContainer);
		when(equipmentManager.getModifiedItemSlotChanges()).thenReturn(changedInventorySlots);

		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getSkullIcon()).thenReturn(SkullIcon.SKULL);

		eventBus.post(itemContainerChanged);
		verify(equipmentManager, times(1)).shouldTimerBeStarted(anyList());
	}

	@Test
	public void playerDoesNotHaveSkullIcon()
	{
		changedInventorySlots.add(1);

		when(itemContainerChanged.getItemContainer()).thenReturn(itemContainer);
		when(equipmentManager.getEquipment()).thenReturn(itemContainer);
		when(equipmentManager.getModifiedItemSlotChanges()).thenReturn(changedInventorySlots);

		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getSkullIcon()).thenReturn(SkullIcon.NONE);

		eventBus.post(itemContainerChanged);
		verify(equipmentManager, times(0)).shouldTimerBeStarted(anyList());
	}
}
