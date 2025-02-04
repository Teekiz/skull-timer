/*
 * Copyright (c) 2023, Callum Rossiter
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
