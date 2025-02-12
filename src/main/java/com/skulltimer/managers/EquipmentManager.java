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

package com.skulltimer.managers;

import com.skulltimer.SkullTimerConfig;
import com.skulltimer.SkullTimerPlugin;
import com.skulltimer.SkulledTimer;
import com.skulltimer.enums.SkulledItems;
import com.skulltimer.enums.TimerDurations;
import com.skulltimer.enums.equipment.GenericWeapons;
import com.skulltimer.enums.equipment.SpellAnimations;
import com.skulltimer.enums.equipment.WeaponHitDelay;
import com.skulltimer.enums.equipment.Weapons;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;

/**
 * An object that is used to check a players worn equipment to identify if a skull timer is required.
 * <pr> This feature is based on the suggestion and code provided by @juusokarjanlahti (<a href="https://github.com/juusokarjanlahti">GitHub</a>).</pr>
 */
@Slf4j
public class EquipmentManager
{
	@Inject
	private final Client client;
	@Inject
	private final ItemManager itemManager;
	private final TimerManager timerManager;
	/** A {@link HashMap} value that is changed when a player equips an item which provides a skull (e.g. amulet of avarice). */
	private final HashMap<Integer, Item> equippedItems;

	/**
	 * The constructor for a {@link EquipmentManager} object.
	 * @param client Runelite's {@link Client} object.
	 * @param timerManager The manager used to control the creation and deletion of {@link SkulledTimer} objects.
	 * @param itemManager Runelite's {@link ItemManager} object.
	 */
	public EquipmentManager(Client client, TimerManager timerManager, ItemManager itemManager)
	{
		this.client = client;
		this.timerManager = timerManager;
		this.itemManager = itemManager;
		this.equippedItems = new HashMap<>();

		//gets the previously worn items in contained item slots.
		for (SkulledItems items : SkulledItems.values()){
			equippedItems.put(items.getItemSlot(), null);
		}
	}

	/**
	 * A method used to get the equipment inventory {@link ItemContainer}.
	 * @return The {@link ItemContainer} for the {@link Client}'s equipment tab.
	 */
	public ItemContainer getEquipment(){
		return client.getItemContainer(InventoryID.EQUIPMENT);
	}

	/**
	 * A method used to check if a timer should be started by checking for any equipment that would provide a skulled status.
	 *
	 * <p>
	 * There are 5 possible states that the players equipment can be in:<br>
	 * <ol>
	 *     <li>{@code equipment} is null or {@code changedItemSlotIDs} is empty. (No action taken).</li>
	 *     <li>The player is wearing equipment that provides a skulled status indefinitely (Any existing timer is stopped).</li>
	 *     <li>The player is wearing equipment that provides a skull status that is not indefinite and no permanent status equipment has been found (A timer is created).</li>
	 *     <li>The player has previously worn indefinite skulled equipment but it has been unequipped (A timer is created).</li>
	 *     <li>None of the conditions have been met. (No action taken).</li>
	 * </ol>
	 * </p>
	 *
	 * @param changedItemSlotIDs a list of {@link Integer}'s representing the IDs of any item slots that have been changed and need to be checked for skulled items.
	 */
	public void shouldTimerBeStarted(List<Integer> changedItemSlotIDs)
	{
		ItemContainer equipment = getEquipment();

		// Ensure the equipment is not null (e.g., during loading screens)
		if (equipment == null || changedItemSlotIDs.isEmpty()) {
			log.debug("Equipment is null.");
			return;
		}

		//updating the items
		HashMap<Integer, SkulledItems> previousItems = convertToSkulledItems(changedItemSlotIDs);
		log.debug("Previous items: {}", previousItems);

		updateCurrentEquipment();

		HashMap<Integer, SkulledItems> currentItems = convertToSkulledItems(changedItemSlotIDs);
		log.debug("Current items: {}", currentItems);

		//checks to see if the player is wearing an indefinite skull item
		for (Map.Entry<Integer, SkulledItems> entry : currentItems.entrySet()) {
			SkulledItems current = entry.getValue();
			SkulledItems previous = previousItems.get(entry.getKey());

			//checks for indefinite skulls in current items
			if (current != null && current.isSkullIndefinite()) {
				log.debug("Slot {} has an item with an indefinite skull: {}.", entry.getKey(), current);
				timerManager.removeTimer(false);
				return;
			}

			//checks if an indefinite skull was previously worn but is now removed
			else if (previous != null && previous.isSkullIndefinite() && hasNoIndefiniteSkullItem()) {
				log.debug("Slot {} previously had an item with an indefinite skull: {}. Returning true.", entry.getKey(), previous);
				timerManager.addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
				return;
			}

			//checks if the current item provides a skull (but not indefinitely)
			else if (current != null && hasNoIndefiniteSkullItem()) {
				log.debug("Slot {} has an item that provides a temporary skull: {}. Returning true.", entry.getKey(), current);
				timerManager.addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
				return;
			}
		}

		log.debug("No conditions met.");
	}

	/**
	 * A method to update if the player is wearing any armour that would provide a skulled status.
	 */
	public void updateCurrentEquipment()
	{
		ItemContainer equipment = getEquipment();
		// Ensure the equipment is not null (e.g., during loading screens)
		if (equipment == null) {
			return;
		}

		for (Map.Entry<Integer, Item> entry : equippedItems.entrySet()){
			Item item = equipment.getItem(entry.getKey());
			equippedItems.put(entry.getKey(), item);
		}
	}

	/**
	 * A method used to convert items that are tracked by the {@code equippedItems} hashmap into skulled items.
	 * @param itemIDSlots the IDs of the item slots to convert.
	 * @return A {@link HashMap} containing the container ID and {@link SkulledItems}.
	 */
	private HashMap<Integer, SkulledItems> convertToSkulledItems(List<Integer> itemIDSlots){
		HashMap<Integer, SkulledItems> wornItems = new HashMap<>();
		for (Integer itemSlotID: itemIDSlots){
			wornItems.put(itemSlotID, convertToSkulledItem(equippedItems != null && equippedItems.get(itemSlotID) != null
				? equippedItems.get(itemSlotID).getId() : 0));
		}
		return wornItems;
	}

	/**
	 * A method used to convert an itemID into a corresponding {@link SkulledItems} value.
	 * @param itemID The ID of the item to identify.
	 * @return The {@link SkulledItems} value if the item matches. Returns {@code null} if there is no corresponding ID.
	 */
	private SkulledItems convertToSkulledItem(int itemID){
		return Arrays.stream(SkulledItems.values())
			.filter(skulledItems -> skulledItems.getItemID() == itemID)
			.findFirst().orElse(null);
	}

	/**
	 * A method used to track if tracked item slots have changed since last update and then return changed slots.
	 * @return A {@link List} of {@link Integer}s representing the IDs of the changed item slots.
	 */
	public List<Integer> getModifiedItemSlotChanges(){
		List<Integer> managedItemSlotsIDs  = new ArrayList<>();

		for (Map.Entry<Integer, Item> entry : equippedItems.entrySet()){
			Item currentItem = getEquipment().getItem(entry.getKey());

			if (hasItemSlotChanged(entry.getValue(), currentItem)) {
				managedItemSlotsIDs.add(entry.getKey());
				log.debug("Slot id {} has changed. ", entry.getKey());
			}
		}

		log.debug("{} Managed item slots have been changed.", managedItemSlotsIDs.size());

		return managedItemSlotsIDs;
	}

	/**
	 * A method to check if the provided items have changed.
	 * @param previousItem The first item to compare.
	 * @param currentItem The second item to compare.
	 * @return {@code true} if the items have changed. Returns {@code false} if they have not.
	 */
	private boolean hasItemSlotChanged(Item previousItem, Item currentItem){
		return
			//if the player adds an item
			(previousItem == null && currentItem != null) ||
			//if the player has removed an item
			(previousItem != null && currentItem == null) ||
			//if the player has swapped out the item.
			(previousItem != null && currentItem != null && previousItem.getId() != currentItem.getId());
	}

	/**
	 * A method to check if a Player is wearing any items that may provide a skull effect indefinitely.
	 * @return {@code true} if the equipment container is null or no indefinite skull items have been found. Returns {@code false} if
	 * the player is wearing an item that provides an indefinite skulled effect.
	 */
	private boolean hasNoIndefiniteSkullItem(){
		ItemContainer equipment = getEquipment();
		// Ensure the equipment is not null (e.g., during loading screens)
		if (equipment == null) {
			return true;
		}

		List<SkulledItems> indefiniteSkulledItems = Arrays.stream(SkulledItems.values())
			.filter(SkulledItems::isSkullIndefinite).collect(Collectors.toList());

		for (SkulledItems skulledItem : indefiniteSkulledItems){
			Item currentItem = getEquipment().getItem(skulledItem.getItemSlot());

			if (currentItem != null && currentItem.getId() == skulledItem.getItemID()){
				log.debug("Player is currently equipment that provides a permanent skull. Returning false.");
				return false;
			}
		}

		log.debug("Player is not wearing any equipment that provides a permanent skull. Returning false.");
		return true;
	}

	/**
	 * A method used to determine the correct weapon hit delay based on the weapon ID and animation ID.
	 * @param weaponID The ID of the weapon currently equipped by the player.
	 * @param animationID The ID of the animation started by the player.
	 * @return The corresponding {@link WeaponHitDelay}. If the weapon cannot be found, {@code null} is returned instead.
	 */
	public WeaponHitDelay getWeaponHitDelay(int weaponID, int animationID){
		//checks to see if the animation matches a spell animation.
		WeaponHitDelay spellHitDelay = SpellAnimations.getSpellHitDelay(animationID);

		//if it does, return the hit delay for that spell.
		if (spellHitDelay != null){
			return spellHitDelay;
		}

		Weapons weapon = Weapons.getByItemID(weaponID);

		if (weapon == null){
			String weaponName = itemManager.getItemComposition(weaponID).getName();
			return GenericWeapons.getWeaponTypeHitDelay(weaponName);
		}

		return (weapon.getSpecialHitDelay() != WeaponHitDelay.NOT_APPLICABLE
			&& weapon.doesAnimationMatchSpecialAnimation(animationID))
			? weapon.getSpecialHitDelay()
			: weapon.getStandardHitDelay();
	}
}
