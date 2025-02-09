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

	static {
		for (GenericWeapons weapon : GenericWeapons.values()) {
			for (String tag : weapon.weaponNameTags) {
				weaponLookupMap.put(tag, weapon.weaponHitDelay);
			}
		}
	}

	GenericWeapons(WeaponHitDelay weaponHitDelay, Set<String> weaponNameTags)
	{
		this.weaponHitDelay = weaponHitDelay;
		this.weaponNameTags = weaponNameTags;
	}

	public static WeaponHitDelay getWeaponTypeHitDelay(String weaponName)
	{
		if (weaponName.isEmpty()){
			return null;
		}

		String weaponNameLowerCase = weaponName.toLowerCase();

		for (String key : weaponLookupMap.keySet()) {
			if (weaponNameLowerCase.contains(key)) {
				log.debug("Generic weapon type found. Hit Delay: {}.", weaponLookupMap.get(key));
				return weaponLookupMap.get(key);
			}
		}
		return null;
	}
}
