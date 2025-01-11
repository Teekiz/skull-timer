package com.skulltimer.managers;

import com.skulltimer.SkulledTimer;
import com.skulltimer.enums.SkulledItems;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

/**
 * A class that is used to check a players worn equipment to identify if a skull timer is required.
 * <pr> This feature is based on the suggestion and code provided by @juusokarjanlahti (<a href="https://github.com/juusokarjanlahti">GitHub</a>).</pr>
 */
@Slf4j
public class EquipmentManager
{
	@Inject
	private final Client client;

	/** A {@link Boolean} value that is changed when a player equips an item which provides a skull (e.g. amulet of avarice). */
	private boolean hasEquippedIndefiniteSkullItem = false;

	/**
	 * The constructor for a {@link EquipmentManager} object.
	 * @param client Runelite's {@link Client} object.
	 */
	public EquipmentManager(Client client) {
		this.client = client;
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
	 * 1) {@code equipment} is null or the player is not wearing any matching equipment (returns {@code false}).<br>
	 * 2) The player is wearing equipment that provides a skulled status indefinitely (returns {@code false}).<br>
	 * 3) The player is wearing equipment that provides a skull immediately upon equipping. (returns {@code true}).<br>
	 * 4) The player has previously worn indefinite skulled equipment but it has been unequipped. (returns {@code true}).<br>
	 * 5) The player is wearing both permanent and temporary skulled items. (returns {@code false}).<br>
	 * </p>
	 * @return A {@link Boolean} value to indicate whether a {@link SkulledTimer} should be started:
	 * 		   {@code true} if a timer should be started (e.i. they are wearing or have worn equipment that provides a skull),
	 * 		   {@code false} if a timer should not be started (i.e. the player is not wearing any corresponding equipment).
	 */
	public boolean hasEquipmentChanged()
	{
		ItemContainer equipment = getEquipment();

		// Ensure the equipment is not null (e.g., during loading screens)
		if (equipment == null) {
			log.debug("Equipment is null.");
			return false;
		}

		boolean itemCheck = isWearingSkulledItem();

		// Player is wearing no items
		if (!itemCheck) {
			if (hasEquippedIndefiniteSkullItem) {
				log.debug("Skulled item unequipped. Starting 20-minute skull timer.");
				setHasEquippedIndefiniteSkullItem(false);
				return true;
			}
			return false;
		}

		// Player is wearing no permanent items
		return !hasEquippedIndefiniteSkullItem;
	}

	/**
	 * A method to identify if the player is wearing any armour that would provide a skulled status.
	 * @return A {@link Boolean} value to indicate if the player is wearing any armour that provides a skull:
	 * 			{@code true} if the player is wearing any matching armour in {@link SkulledItems},
	 * 			{@code false} if not.
	 */
	public boolean isWearingSkulledItem()
	{
		ItemContainer equipment = getEquipment();

		// Ensure the equipment is not null (e.g., during loading screens)
		if (equipment == null) {
			return false;
		}

		//sort the items to make sure that permanent items are found first.
		List<SkulledItems> sortedSkulledItems = Arrays.stream(SkulledItems.values())
			.sorted((item1, item2) -> Boolean.compare(item2.isSkullIndefinite(), item1.isSkullIndefinite()))
			.collect(Collectors.toList());

		//first check for permanent
		for (SkulledItems skulledItem : sortedSkulledItems) {
			Item containerItem = equipment.getItem(skulledItem.getItemSlot());
			//if the worn item matches a skulled item and if it matches the required check.
			if (containerItem != null && containerItem.getId() == skulledItem.getItemID()) {
				setHasEquippedIndefiniteSkullItem(skulledItem.isSkullIndefinite());
				return true;
			}
		}

		return false;
	}

	/**
	 * A method that is used to set the value of {@code hasEquippedIndefiniteSkullItem}.
	 * @param value The {@link Boolean} value to update {@code hasEquippedIndefiniteSkullItem} to.
	 */
	private void setHasEquippedIndefiniteSkullItem(boolean value){
		log.debug("Setting equipped items value to {}.", value);
		hasEquippedIndefiniteSkullItem = value;
	}
}
