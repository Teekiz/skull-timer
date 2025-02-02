package com.skulltimer.enums;

import com.skulltimer.data.CombatInteraction;

/**
 * Represents the possible combat states of a player within a {@link CombatInteraction}.
 */
public enum CombatStatus
{
	ATTACKED,					// The player has been attacked by the local player.
	RETALIATED,					// The player has retaliated against the local player.
	DEAD,						// The player has died.
	UNCERTAIN,					// The players combat status is unknown.
	LOGGED_OUT,					// The player has logged out.
	INACTIVE,					// The player has previously retaliated/attacked, but has since logged out/left the local players view.
	ATTACKER					// The player has attacked the local player.
}
