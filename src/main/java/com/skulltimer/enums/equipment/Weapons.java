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

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum Weapons
{
	/*MELEE*/
	//TWO-HANDED-SWORD
	_3RD_AGE_LONGSWORD(ItemID._3RD_AGE_LONGSWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	ADAMANT_2H_SWORD(ItemID.ADAMANT_2H_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	ANCIENT_GODSWORD(ItemID.ANCIENT_GODSWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	ARMADYL_GODSWORD(ItemID.ARMADYL_GODSWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	ARMADYL_GODSWORD_OR(ItemID.ARMADYL_GODSWORD_OR, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	ARMADYL_GODSWORD_DEADMAN(ItemID.ARMADYL_GODSWORD_DEADMAN, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	BANDOS_GODSWORD(ItemID.BANDOS_GODSWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	BANDOS_GODSWORD_OR(ItemID.BANDOS_GODSWORD_OR, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	BLACK_2H_SWORD(ItemID.BLACK_2H_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	BRONZE_2H_SWORD(ItemID.BRONZE_2H_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	COLOSSAL_BLADE(ItemID.COLOSSAL_BLADE, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	CORRUPTED_ARMADYL_GODSWORD(ItemID.CORRUPTED_ARMADYL_GODSWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	DRAGON_2H_SWORD(ItemID.DRAGON_2H_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	DRAGON_2H_SWORD_CR(ItemID.DRAGON_2H_SWORD_CR, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	GIANT_BRONZE_DAGGER(ItemID.GIANT_BRONZE_DAGGER, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	GILDED_2H_SWORD(ItemID.GILDED_2H_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	IRON_2H_SWORD(ItemID.IRON_2H_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	MITHRIL_2H_SWORD(ItemID.MITHRIL_2H_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	RUNE_2H_SWORD(ItemID.RUNE_2H_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	SARADOMIN_GODSWORD(ItemID.SARADOMIN_GODSWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	SARADOMIN_GODSWORD_OR(ItemID.SARADOMIN_GODSWORD_OR, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	SARADOMIN_SWORD(ItemID.SARADOMIN_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	SARADOMIN_BLESSED_SWORD(ItemID.SARADOMINS_BLESSED_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	SHADOW_SWORD(ItemID.SHADOW_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	SPATULA(ItemID.SPATULA, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	STEEL_2H_SWORD(ItemID.STEEL_2H_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	THE_DOGSWORD(ItemID.THE_DOGSWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	WHITE_2H_SWORD(ItemID.WHITE_2H_SWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	ZAMORAK_GODSWORD(ItemID.ZAMORAK_GODSWORD, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE),
	ZAMORAK_GODSWORD_OR(ItemID.ZAMORAK_GODSWORD_OR, WeaponAnimations.UNDEFINED, WeaponHitDelay.MELEE_STANDARD, WeaponHitDelay.NOT_APPLICABLE)



	/*RANGED*/

	/*MAGIC*/
	;

	private final int weaponID;
	private final WeaponAnimations weaponAnimations;
	private final WeaponHitDelay standardHitDelay;
	private final WeaponHitDelay specialHitDelay;

	Weapons(int weaponID, WeaponAnimations weaponAnimations, WeaponHitDelay standardHitDelay, WeaponHitDelay specialHitDelay)
	{
		this.weaponID = weaponID;
		this.weaponAnimations = weaponAnimations;
		this.standardHitDelay = standardHitDelay;
		this.specialHitDelay = specialHitDelay;
	}
}
