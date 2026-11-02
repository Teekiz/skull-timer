package com.skulltimer.enums.equipment;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;

@Slf4j
@Getter
public enum AbyssalBracelets
{
	BRACELET_5(ItemID.JEWL_RUNERUNNING_BRACELET_5, 5),
	BRACELET_4(ItemID.JEWL_RUNERUNNING_BRACELET_4, 4),
	BRACELET_3(ItemID.JEWL_RUNERUNNING_BRACELET_3, 3),
	BRACELET_2(ItemID.JEWL_RUNERUNNING_BRACELET_2, 2),
	BRACELET_1(ItemID.JEWL_RUNERUNNING_BRACELET_1, 1);

	private final int itemID;
	private final int charge;

	AbyssalBracelets(int itemID, int charge){
		this.itemID = itemID;
		this.charge = charge;
	}

	private static final Map<Integer, AbyssalBracelets> lookup = new HashMap<>();

	static
	{
		for (AbyssalBracelets bracelets : AbyssalBracelets.values())
		{
			lookup.put(bracelets.itemID, bracelets);
		}
	}

	public static AbyssalBracelets getByItemID(int itemID) {
		return lookup.get(itemID);
	}
}
