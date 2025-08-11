package com.skulltimer.enums.equipment;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum AbyssalBracelets
{
	BRACELET_5(11095, 5),
	BRACELET_4(11097, 4),
	BRACELET_3(11099, 3),
	BRACELET_2(11101, 2),
	BRACELET_1(11103, 1);

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
