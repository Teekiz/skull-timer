package com.skulltimer.enums.equipment;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.AnimationID;

@Getter
public enum ExcludedAnimations
{
	IDLE(AnimationID.IDLE),
	BLOCK(0),
	BLOCK_STAFF(0),
	BLOCK_SHIELD(0);

	private static final Map<Integer, ExcludedAnimations> EXCLUDED_MAP = new HashMap<>();

	static {
		for (ExcludedAnimations anim : values()) {
			EXCLUDED_MAP.put(anim.id, anim);
		}
	}

	private final int id;

	ExcludedAnimations(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static boolean isExcluded(int animationID) {
		return EXCLUDED_MAP.containsKey(animationID); // O(1)
	}
}
