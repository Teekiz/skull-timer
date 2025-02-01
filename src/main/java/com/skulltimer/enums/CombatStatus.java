package com.skulltimer.enums;

import com.skulltimer.data.TargetInteraction;

/**
 * Represents the possible combat states of a player within a {@link TargetInteraction}.
 */
public enum CombatStatus
{
	DEFAULT,					// No special combat status
	RETALIATED,					// The player has retaliated against the local player.
	DEAD,						// The player has died.
	UNKNOWN,					// The players combat status is unknown.
	RETALIATED_UNKNOWN,			// The player has previously retaliated, but the current status is unknown.
	LOGGED_OUT,					// The player has logged out.
	RETALIATED_LOGGED_OUT		// The player has previously retaliated, but has since logged out.
}
