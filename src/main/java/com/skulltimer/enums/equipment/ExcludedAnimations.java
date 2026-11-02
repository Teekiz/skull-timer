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
import net.runelite.api.gameval.AnimationID;

/**
 * A list of animations that should not trigger an animation check.
 *
 * <p>This list maybe incomplete and the enum name may not be the exact weapon type, only the type of weapon/shield the animation corresponded to.</p>
 */

@Getter
public enum ExcludedAnimations
{
	IDLE(-1),
	UNARMED(AnimationID.HUMAN_UNARMEDBLOCK),
	DEFENDER(AnimationID.WARGUILD_PARRY_DEFEND),
	SHIELD(AnimationID.HUMAN_SHIELD_DEFENCE),
	BULWARK(AnimationID.HUMAN_DINHS_BULWARK_BLOCK),
	_1H_SWORD(AnimationID.HUMAN_SWORD_DEF),
	_2H_SWORD(AnimationID.DH_SWORD_UPDATE_DEFEND),
	STAFF(AnimationID.HUMAN_STAFFORB_BLOCK),
	HALBERD(AnimationID.HUMAN_SPEAR_BLOCK),
	FLAIL(AnimationID.IVANDIS_FLAIL_DEFEND),
	CHINCHOMPA(AnimationID.HUMAN_CHINCHOMPA_DEFEND),
	WAND(AnimationID.HUMAN_STAFF_BLOCK),
	WHIP(AnimationID.SLAYER_ABYSSAL_WHIP_DEFEND),
	TOOLS_PICKAXE_AXE(AnimationID.HUMAN_AXE_BLOCK),
	DAGGER(AnimationID.HUMAN_DDAGGER_BLOCK),
	BLUDGEON(AnimationID.SLAYER_GRANITE_MAUL_DEFEND),
	CHAINMACE(AnimationID.WILD_CAVE_CHAINMACE_DEFEND),
	MACE(AnimationID.HUMAN_BLUNT_BLOCK),
	BATTLEAXE(AnimationID.HUMAN_AXE_BLOCK),
	BONEMACE(AnimationID.BARROW_GUTHAN_DEFEND),
	ABBYSSAL_DAGGER(AnimationID.ABYSSAL_DAGGER_BLOCK),
	DRAGON_2H_SWORD(AnimationID.HUMAN_DHSWORD_BLOCK);


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
