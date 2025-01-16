package com.skulltimer.enums;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

/**
 * An enumeration of locations that provide skulls.
 */
@Getter
public enum WorldAreas
{
	ABYSS(new WorldPoint(3009, 4803, 0), new WorldPoint(3070, 4862, 0));

	private final WorldPoint x;
	private final WorldPoint y;
	WorldAreas(WorldPoint x, WorldPoint y)
	{
		this.x = x;
		this.y = y;
	}
}
