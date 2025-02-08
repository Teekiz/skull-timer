package com.skulltimer.enums.equipment;

import java.util.Set;
import lombok.Getter;

//todo - teleblock

@Getter
public enum SpellAnimations
{
	/*
	 * Without Staff (Standard Spell Book):
	 * 	Strike, bolt, blast (711). Surge (7855),  Wave (727).
	 *  Confuse (716). Weaken (717). Curse, Vulnerability (718).
	 * 	Entangle, Snare, Bind (710).
	 *  Enfeeble (728), Stun (729), Crumble undead (724).
	 *
	 * With Staff (Standard Spell Book):
	 *  Strike, bolt, blast (1162). Surge (7855),  Wave (1167).
	 *  Confuse (1163). Weaken (1164). Curse, Vulnerability (1165).
	 *  Entangle, Snare, Bind (1161).
	 *  Enfeeble (1168), Stun (1169), Crumble undead (1166).
	 *  Iban's Blast (708), God Spells (811), Slayer Dart (1576).
	 *
	 * Ancient and Arceuus Spell Books:
	 *	Rush, Blitz (1978). Burst, Barrage (1979).
	 *  Grasp (8972). Demonbane (8977).
	 *
	 */
	STANDARD_NO_STAFF(WeaponHitDelay.MAGIC_STANDARD, Set.of(711, 7855, 727, 716, 717, 718, 710, 728, 729, 724)),
	STANDARD_WITH_STAFF(WeaponHitDelay.MAGIC_STANDARD, Set.of(1162, 7855, 1167, 1163, 1164, 1165, 1161, 1168, 1169, 1166, 708, 811, 1576)),
	ANCIENT(WeaponHitDelay.MAGIC_STANDARD, Set.of(1978, 1979)),
	GRASP_AND_DEMONBANE(WeaponHitDelay.MAGIC_GRASP_DEMONBANE_NIGHTMARE_STAFF_SPECIALS, Set.of(8972, 8977));
	private final WeaponHitDelay weaponHitDelay;
	private final Set<Integer> animationIDs;

	SpellAnimations(WeaponHitDelay weaponHitDelay, Set<Integer> animationIDs)
	{
		this.weaponHitDelay = weaponHitDelay;
		this.animationIDs = animationIDs;
	}

	public static WeaponHitDelay getSpellHitDelay(int animationID){
		for (SpellAnimations animation : SpellAnimations.values()){
			if (animation.getAnimationIDs().contains(animationID)){
				return animation.getWeaponHitDelay();
			}
		}

		return null;
	}
}
