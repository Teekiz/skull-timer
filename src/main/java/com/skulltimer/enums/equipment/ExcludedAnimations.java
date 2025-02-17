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

	static
	{
		for (ExcludedAnimations anim : values())
		{
			EXCLUDED_MAP.put(anim.id, anim);
		}
	}

	private final int id;

	ExcludedAnimations(int id)
	{
		this.id = id;
	}

	/**
	 * A method used to check if a given animation ID matches any excluded animation IDs.
	 * @param animationID The animation ID to check.
	 * @return {@code true} if the animation is to be excluded. Otherwise, returns {@code false}.
	 */
	public static boolean isExcluded(int animationID)
	{
		return EXCLUDED_MAP.containsKey(animationID);
	}
}
