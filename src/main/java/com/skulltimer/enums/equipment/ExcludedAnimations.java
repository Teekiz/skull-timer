package com.skulltimer.enums.equipment;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.AnimationID;

/**
 * A list of animations that should not trigger an animation check.
 *
 * <p>This list maybe incomplete and the enum name may not be the exact weapon type, only the type of weapon/shield the animation corresponded to.</p>
 */

@Getter
public enum ExcludedAnimations
{
	IDLE(AnimationID.IDLE),
	UNARMED(424),
	DEFENDER(4177),
	SHIELD(1156),
	BULWARK(7512),
	_1H_SWORD(388),
	_2H_SWORD(7056),
	STAFF(420),
	HALBERD(430),
	FLAIL(8017),
	CHINCHOMPA(3176),
	WAND(415),
	WHIP(1659),
	TOOLS_PICKAXE_AXE(397),
	DAGGER(378),
	BLUDGEON(1666),
	CHAINMACE(7200),
	MACE(403),
	BATTLEAXE(397),
	BONEMACE(2063),
	ABBYSSAL_DAGGER(3295),
	DRAGON_2H_SWORD(410);


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

	public static boolean isExcluded(int animationID) {
		return EXCLUDED_MAP.containsKey(animationID);
	}
}
