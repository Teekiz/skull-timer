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
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A list of weapon categories to be used in place of concrete weapon implementations in the event a weapon type cannot be found.
 */
@Slf4j
@Getter
public enum GenericWeapons
{
	MELEE(WeaponHitDelay.MELEE_STANDARD, Set.of("sword", "axe", "bludgeon", "maul", "jack", "hammer", "bulwark", "claw", "halberd", "banner", "mjolnir", "scythe", "rapier", "machete", "scimitar", "sickle", "spear", "hasta", "dagger", "mace", "club", "harpoon", "whip", "flail", "katana")),
	RANGED(WeaponHitDelay.RANGED_STANDARD, Set.of("bow", "ballista", "chinchompa", "dart", "thrown", "throwing", "pipe")),
	MAGIC(WeaponHitDelay.MAGIC_STANDARD, Set.of("staff", "stave", "wand"));

	private final WeaponHitDelay weaponHitDelay;
	private final Set<String> weaponNameTags;

	//a lookup map to increase lookup speed
	private static final Map<String, WeaponHitDelay> weaponLookupMap = new HashMap<>();

	static
	{
		for (GenericWeapons weapon : GenericWeapons.values())
		{
			for (String tag : weapon.weaponNameTags)
			{
				weaponLookupMap.put(tag, weapon.weaponHitDelay);
			}
		}
	}

	GenericWeapons(WeaponHitDelay weaponHitDelay, Set<String> weaponNameTags)
	{
		this.weaponHitDelay = weaponHitDelay;
		this.weaponNameTags = weaponNameTags;
	}

	/**
	 * A method to get the hit delay for the given weapon name.
	 * @param weaponName The name of the weapon whose hit delay is to be searched for.
	 * @return The corresponding {@link WeaponHitDelay} for the given {@code weaponName}.
	 */
	public static WeaponHitDelay getWeaponTypeHitDelay(String weaponName)
	{
		if (weaponName.isEmpty())
		{
			return null;
		}

		String weaponNameLowerCase = weaponName.toLowerCase();

		for (String key : weaponLookupMap.keySet())
		{
			if (weaponNameLowerCase.contains(key))
			{
				log.debug("Generic weapon type found. Hit Delay: {}.", weaponLookupMap.get(key));
				return weaponLookupMap.get(key);
			}
		}
		return null;
	}
}
