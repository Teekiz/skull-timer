package com.skulltimer.enums;

import lombok.Getter;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemID;

/**
 * An enumeration of items that provide skull statues when equipped.
 */
@Getter
public enum SkulledItems
{
	AMULET_OF_AVARICE(ItemID.AMULET_OF_AVARICE, EquipmentInventorySlot.AMULET.getSlotIdx(), true),
	CAPE_OF_SKULLS(ItemID.CAPE_OF_SKULLS, EquipmentInventorySlot.CAPE.getSlotIdx(), false);

	private final int itemID;
	private final int itemSlot;
	/** A {@link Boolean} to check if the item provides a skull for an unlimited amount of time while equipped. */
	private final boolean isSkullIndefinite;

	SkulledItems(int itemID, int itemSlot, boolean isSkullIndefinite){
		this.itemID = itemID;
		this.itemSlot = itemSlot;
		this.isSkullIndefinite = isSkullIndefinite;
	}
}
