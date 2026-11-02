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

import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.AnimationID;

/**
 * A list of spells and their associated animation ID.
 */
@Slf4j
@Getter
public enum SpellAnimations
{
	/*
	 * Without Staff (Standard Spell Book):
	 * 	Strike, bolt, blast (711). Surge (7855),  Wave (727).
	 *  Confuse (716). Weaken (717). Curse, Vulnerability (718).
	 * 	Entangle, Snare, Bind (710).
	 *  Enfeeble (728), Stun (729), Crumble undead (724). Teleblock (1819).
	 *
	 * With Staff (Standard Spell Book):
	 *  Strike, bolt, blast (1162). Surge (7855),  Wave (1167).
	 *  Confuse (1163). Weaken (1164). Curse, Vulnerability (1165).
	 *  Entangle, Snare, Bind (1161).
	 *  Enfeeble (1168), Stun (1169), Crumble undead (1166).
	 *  Iban's Blast (708), God Spells (811), Slayer Dart (1576). Teleblock (1820).
	 *
	 * Ancient and Arceuus Spell Books:
	 *	Rush, Blitz (1978). Burst, Barrage (1979).
	 *  Grasp (8972). Demonbane (8977).
	 *
	 */
	STANDARD_NO_STAFF(WeaponHitDelay.MAGIC_STANDARD, Set.of(AnimationID.HUMAN_CASTSTRIKE, AnimationID.HUMAN_CAST_SURGE, AnimationID.HUMAN_CASTWAVE, AnimationID.HUMAN_CASTCONFUSE, AnimationID.HUMAN_CASTWEAKEN, AnimationID.HUMAN_CASTCURSE, AnimationID.HUMAN_CASTENTANGLE, AnimationID.HUMAN_CASTENFEEBLE, AnimationID.HUMAN_CASTSTUN, AnimationID.HUMAN_CASTCRUMBLEUNDEAD, AnimationID.HUMAN_CASTING_TELE_BLOCK)),
	STANDARD_WITH_STAFF(WeaponHitDelay.MAGIC_STANDARD, Set.of(AnimationID.HUMAN_CASTSTRIKE_STAFF, AnimationID.HUMAN_CAST_SURGE, AnimationID.HUMAN_CASTWAVE_STAFF, AnimationID.HUMAN_CASTCONFUSE_STAFF, AnimationID.HUMAN_CASTWEAKEN_STAFF, AnimationID.HUMAN_CASTCURSE_STAFF, AnimationID.HUMAN_CASTENTANGLE_STAFF, AnimationID.HUMAN_CASTENFEEBLE_STAFF, AnimationID.HUMAN_CASTSTUN_STAFF, AnimationID.HUMAN_CASTCRUMBLEUNDEAD_STAFF, AnimationID.HUMAN_CASTIBANBLAST, AnimationID.HUMAN_CASTING, AnimationID.SLAYER_MAGICDART_CAST, AnimationID.HUMAN_CASTING_TELE_BLOCK_STAFF)),
	ANCIENT(WeaponHitDelay.MAGIC_STANDARD, Set.of(AnimationID.ZAROS_CASTING, AnimationID.ZAROS_VERTICAL_CASTING)),
	GRASP_AND_DEMONBANE(WeaponHitDelay.MAGIC_GRASP_DEMONBANE_NIGHTMARE_STAFF_SPECIALS, Set.of(AnimationID.HUMAN_SPELLCAST_GRASP, AnimationID.HUMAN_SPELLCAST_DEMONBANE));

	private final WeaponHitDelay weaponHitDelay;
	private final Set<Integer> animationIDs;

	SpellAnimations(WeaponHitDelay weaponHitDelay, Set<Integer> animationIDs)
	{
		this.weaponHitDelay = weaponHitDelay;
		this.animationIDs = animationIDs;
	}

	public static WeaponHitDelay getSpellHitDelay(int animationID)
	{
		for (SpellAnimations animation : SpellAnimations.values())
		{
			if (animation.getAnimationIDs().contains(animationID))
			{
				log.debug("Spell animation found. Hit Delay: {}.", animation.getWeaponHitDelay());
				return animation.getWeaponHitDelay();
			}
		}

		return null;
	}
}
