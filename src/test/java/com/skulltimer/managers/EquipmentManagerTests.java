package com.skulltimer.managers;

import com.skulltimer.enums.equipment.WeaponHitDelay;
import com.skulltimer.mocks.TimerMocks;
import com.skulltimer.enums.SkulledItems;
import com.skulltimer.enums.TimerDurations;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EquipmentManagerTests extends TimerMocks
{
	@InjectMocks
	EquipmentManager equipmentManager;
	@Mock
	ItemContainer equipment;
	@Mock
	Item amuletMock;
	@Mock
	Item capeMock;
	@Mock
	Item amuletMockTwo;
	@Mock
	Item capeMockTwo;
	@Mock
	ItemComposition itemComposition;

	List<Integer> changedItemIDSlots;

	@BeforeEach
	public void startUp()
	{
		changedItemIDSlots = new ArrayList<>();
	}

	@Test
	public void indefiniteSkullItemEquipped()
	{
		//equipping indefinite skulled item
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMock);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(null);
		when(amuletMock.getId()).thenReturn(SkulledItems.AMULET_OF_AVARICE.getItemID());

		//checking if the timer be started
		changedItemIDSlots.add(EquipmentInventorySlot.AMULET.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(0)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
		verify(timerManager, times(1)).removeTimer(false);
	}

	@Test
	public void indefiniteSkullItemUnequipped()
	{
		//equipping indefinite skulled item and checking
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMock);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(null);
		when(amuletMock.getId()).thenReturn(SkulledItems.AMULET_OF_AVARICE.getItemID());
		changedItemIDSlots.add(EquipmentInventorySlot.AMULET.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		//removing item
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(null);
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}

	@Test
	public void limitedSkullItemEquipped()
	{
		//equipping item
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(null);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);
		when(capeMock.getId()).thenReturn(SkulledItems.CAPE_OF_SKULLS.getItemID());

		//checking if the timer be started
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}

	@Test
	public void limitedSkullItemUnequipped()
	{
		//equipping item
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(null);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);
		when(capeMock.getId()).thenReturn(SkulledItems.CAPE_OF_SKULLS.getItemID());
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);

		//unequipped item
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(null);
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}

	@Test
	public void itemAlreadyEquipped()
	{
		//start up and game state changed method
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		equipmentManager.updateCurrentEquipment();
		verify(timerManager, times(0)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}

	@Test
	public void EquippingPermanentItemAfterLimitedItem()
	{
		//equipping limited skulled effect item
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(null);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);
		when(capeMock.getId()).thenReturn(SkulledItems.CAPE_OF_SKULLS.getItemID());
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);

		//equipping indefinite skulled effect item
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMock);
		when(amuletMock.getId()).thenReturn(SkulledItems.AMULET_OF_AVARICE.getItemID());
		changedItemIDSlots.clear();
		changedItemIDSlots.add(EquipmentInventorySlot.AMULET.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager).removeTimer(false);
	}

	@Test
	public void EquippingPermanentItemBeforeLimitedItem()
	{
		//equipping indefinite skulled item
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMock);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(null);
		when(amuletMock.getId()).thenReturn(SkulledItems.AMULET_OF_AVARICE.getItemID());
		changedItemIDSlots.add(EquipmentInventorySlot.AMULET.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager).removeTimer(false);

		//equipping limited skulled effect item
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);
		when(capeMock.getId()).thenReturn(SkulledItems.CAPE_OF_SKULLS.getItemID());
		changedItemIDSlots.clear();
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager, times(0)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}

	@Test
	public void RemovingBothSkulledEquipment_PermanentFirst()
	{
		//both items have been equipped
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMock);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);
		when(amuletMock.getId()).thenReturn(SkulledItems.AMULET_OF_AVARICE.getItemID());
		when(capeMock.getId()).thenReturn(SkulledItems.CAPE_OF_SKULLS.getItemID());
		equipmentManager.updateCurrentEquipment();

		//removing permanent item
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(null);
		changedItemIDSlots.add(EquipmentInventorySlot.AMULET.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);

		//remove none-permanent item
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(null);
		changedItemIDSlots.clear();
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}

	@Test
	public void RemovingBothSkulledEquipment_NonePermanentFirst()
	{
		//both items have been equipped
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMock);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);
		when(amuletMock.getId()).thenReturn(SkulledItems.AMULET_OF_AVARICE.getItemID());
		when(capeMock.getId()).thenReturn(SkulledItems.CAPE_OF_SKULLS.getItemID());
		equipmentManager.updateCurrentEquipment();

		//removing permanent first
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(null);
		changedItemIDSlots.add(EquipmentInventorySlot.AMULET.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);

		//removing none permanent
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(null);
		changedItemIDSlots.clear();
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}

	@Test
	public void UnequippedBothSlotsOnSameTick()
	{
		//both items have been equipped
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMock);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);
		when(amuletMock.getId()).thenReturn(SkulledItems.AMULET_OF_AVARICE.getItemID());
		when(capeMock.getId()).thenReturn(SkulledItems.CAPE_OF_SKULLS.getItemID());
		equipmentManager.updateCurrentEquipment();

		//unequipped
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(null);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(null);
		changedItemIDSlots.add(EquipmentInventorySlot.AMULET.getSlotIdx());
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());

		//method check
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}

	@Test
	public void EquippedBothSlotsOnSameTick()
	{
		//both items have been unequipped
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(null);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(null);
		equipmentManager.updateCurrentEquipment();

		//equipped
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMock);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);
		when(amuletMock.getId()).thenReturn(SkulledItems.AMULET_OF_AVARICE.getItemID());
		when(capeMock.getId()).thenReturn(SkulledItems.CAPE_OF_SKULLS.getItemID());
		changedItemIDSlots.add(EquipmentInventorySlot.AMULET.getSlotIdx());
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());

		//method check
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(1)).removeTimer(false);
	}

	@Test
	public void RemovingPermanent_ThenAddingTemporary()
	{
		//amulet has been equipped
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMock);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(null);
		when(amuletMock.getId()).thenReturn(SkulledItems.AMULET_OF_AVARICE.getItemID());
		equipmentManager.updateCurrentEquipment();

		//unequipped
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(null);
		changedItemIDSlots.add(EquipmentInventorySlot.AMULET.getSlotIdx());

		//method check
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);

		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);
		when(capeMock.getId()).thenReturn(SkulledItems.CAPE_OF_SKULLS.getItemID());
		changedItemIDSlots.clear();
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);

		verify(timerManager, times(2)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}

	@Test
	public void SwappingSkulledItemsForOtherItems()
	{
		//both items have been equipped
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMock);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);

		when(amuletMock.getId()).thenReturn(SkulledItems.AMULET_OF_AVARICE.getItemID());
		when(capeMock.getId()).thenReturn(SkulledItems.CAPE_OF_SKULLS.getItemID());
		when(amuletMockTwo.getId()).thenReturn(ItemID.OCCULT_NECKLACE);
		when(capeMockTwo.getId()).thenReturn(ItemID.INFERNAL_CAPE);

		equipmentManager.updateCurrentEquipment();

		//swapping cape first - shouldn't start timer
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMockTwo);
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());

		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(0)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);

		//swapped back
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(0)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);

		//swapping out amulet
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMockTwo);
		changedItemIDSlots.clear();
		changedItemIDSlots.add(EquipmentInventorySlot.AMULET.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);

		//swapping out cape
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMockTwo);
		changedItemIDSlots.clear();
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}

	@Test
	public void SwappingOtherItemsForSkulledItems()
	{
		//both items have been equipped
		when(equipmentManager.getEquipment()).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMockTwo);
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMockTwo);

		when(amuletMock.getId()).thenReturn(ItemID.AMULET_OF_AVARICE);
		when(capeMock.getId()).thenReturn(ItemID.CAPE_OF_SKULLS);
		when(amuletMockTwo.getId()).thenReturn(ItemID.AMULET_OF_GLORY);
		when(capeMockTwo.getId()).thenReturn(ItemID.IMBUED_ZAMORAK_CAPE);
		equipmentManager.updateCurrentEquipment();

		//swapping cape first - should start timer
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMock);
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());

		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);

		//swapped back
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMockTwo);
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);

		//swapping out amulet
		when(equipment.getItem(EquipmentInventorySlot.AMULET.getSlotIdx())).thenReturn(amuletMock);
		changedItemIDSlots.clear();
		changedItemIDSlots.add(EquipmentInventorySlot.AMULET.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(1)).removeTimer(false);

		//swapping out cape
		when(equipment.getItem(EquipmentInventorySlot.CAPE.getSlotIdx())).thenReturn(capeMockTwo);
		changedItemIDSlots.clear();
		changedItemIDSlots.add(EquipmentInventorySlot.CAPE.getSlotIdx());
		equipmentManager.shouldTimerBeStarted(changedItemIDSlots);
		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}

	@Test
	public void getWeaponHitDelay_NotSpell()
	{
		assertEquals(WeaponHitDelay.MAGIC_STANDARD_WITH_MELEE, equipmentManager.getWeaponHitDelay(20733, 1));
	}

	@Test
	public void getWeaponHitDelay_WithRangedWeapon()
	{
		assertEquals(WeaponHitDelay.RANGED_STANDARD, equipmentManager.getWeaponHitDelay(11785, 7552));
	}

	@Test
	public void getWeaponHitDelay_WithSpell()
	{
		assertEquals(WeaponHitDelay.MAGIC_STANDARD, equipmentManager.getWeaponHitDelay(11785, 727));
	}

	@Test
	public void getWeaponHitDelay_WithUndefinedWeapon()
	{
		when(itemManager.getItemComposition(1)).thenReturn(itemComposition);
		when(itemComposition.getName()).thenReturn("Armadyl Crossbow");
		assertEquals(WeaponHitDelay.RANGED_STANDARD, equipmentManager.getWeaponHitDelay(1, -1));
	}

	@Test
	public void getWeaponHitDelay_WithUndefinedWeapon_DoesNotExistInGenericWeapons()
	{
		when(itemManager.getItemComposition(1)).thenReturn(itemComposition);
		when(itemComposition.getName()).thenReturn("");
		assertNull(equipmentManager.getWeaponHitDelay(1, -1));
	}
}
